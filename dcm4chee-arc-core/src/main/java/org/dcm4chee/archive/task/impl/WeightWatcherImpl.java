/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.archive.task.impl;

import org.dcm4chee.archive.conf.WeightWatcherConfiguration;
import org.dcm4chee.task.MemoryConsumingTask;
import org.dcm4chee.task.WeightWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of {@link WeightWatcher}.
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class WeightWatcherImpl implements WeightWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(WeightWatcher.class);

    private Lock lock = new ReentrantLock(true); // fair lock to avoid starvation

    private WeightWatcherConfiguration config;

    /**
     * Total memory of the system.
     */
    private final long totalSystemMemory;

    /**
     * Memory that is beeing managed by this Weight Watcher. Always <= totalSystemMemory.
     */
    private long totalManagedMemory;

    /**
     * Number of currently running tasks.
     */
    private int runningTasks = 0;

    /**
     * Memory used by currently running tasks.
     */
    private long usedMemory = 0;

    // taskTypesMap and taskTypes always need to be kept in sync
    private Map<String, TaskTypeInformation> taskTypesMap = new HashMap<>();
    private List<TaskTypeInformation> taskTypes = new ArrayList<>();

    /**
     * Counter for round-robin picking of task types with queued tasks.
     */
    private int roundRobinTaskTypesIndex = 0;

    /**
     * Task type for which the oldest queued tasks should be run next.
     */
    private TaskTypeInformation nextTaskType = null;

    public WeightWatcherImpl(WeightWatcherConfiguration config, long totalSystemMemory) {
        this.totalSystemMemory = totalSystemMemory;
        reconfigure(config);
    }

    @Override
    public <V> V execute(MemoryConsumingTask<V> task) throws InterruptedException, Exception {

        long estimatedNeededMemory = task.getEstimatedWeight();

        if (estimatedNeededMemory < 0)
            throw new IllegalArgumentException("Estimated memory must not be negative");

        if (estimatedNeededMemory == 0) {
            // if the task says that it doesn't need any memory, we do not apply any limits (also no concurrency limits)
            // this is to simplify code where the task decides that it does not actually need to consume a lot of memory
            // in a particular case (e.g. rendition not required, because rendered image loaded from cache)
            LOG.info("Bypassing zero memory task {}", getTaskLogId(task));
            return runTask(task);
        }

        TaskTypeInformation taskTypeInfo;
        lock.lock();
        try {
            taskTypeInfo = provideTaskTypeInformation(task.getTaskType().name());

            QueuedTask queuedTask = null;
            while (!canTaskProceed(task, taskTypeInfo, estimatedNeededMemory)) {
                if (queuedTask == null) {
                    queuedTask = new QueuedTask(task, taskTypeInfo, estimatedNeededMemory);
                    taskTypeInfo.queuedTasks.addLast(queuedTask); // add at the end of the queue

                    LOG.info(formatLogMessage("Blocking", task, estimatedNeededMemory));
                }

                try {
                    queuedTask.waitForBetterTimes();
                } catch (InterruptedException interruptedException) {
                    // someone canceled the task
                    taskTypeInfo.queuedTasks.remove(queuedTask); // unqueue
                    wakeUpNextQueuedTask(); // ensure other queued tasks run, in the case this was the first queued one
                    throw interruptedException; // propagate (caller should handle the cancelling of its task)
                }
            }

            LOG.info(formatLogMessage("Starting", task, estimatedNeededMemory));

            updateStateForTaskStart(estimatedNeededMemory, taskTypeInfo);

            warnIfOverAvailableMemory(task, estimatedNeededMemory);

            if (queuedTask != null) {
                QueuedTask removedQueuedTask = nextTaskType.queuedTasks.removeFirst();
                if (removedQueuedTask != queuedTask) // assertion (otherwise implementation is wrong)
                    throw new IllegalStateException("Queued tasks always need to run in order");
                nextTaskType = null;
            }

            // there might be more queued tasks, which could also run now in parallel ->
            // wake up the first of them, which might then also wake up its predecessor, and so on
            wakeUpNextQueuedTask();
        } finally {
            lock.unlock();
        }

        V returnValue;
        try {
            returnValue = runTask(task);
        } finally {

            lock.lock();
            try {
                updateStateForTaskFinished(estimatedNeededMemory, taskTypeInfo);

                LOG.info(formatLogMessage("Finished", task, estimatedNeededMemory));

                // only wake up the one queued task. once that one checked its conditions and is allowed to proceed it
                // will wake up its predecessor, and so on
                wakeUpNextQueuedTask();
            } finally {
                lock.unlock();
            }

        }

        return returnValue;
    }

    private void updateStateForTaskStart(long estimatedNeededMemory, TaskTypeInformation taskTypeInfo) {
        runningTasks++;
        usedMemory += estimatedNeededMemory;
        taskTypeInfo.typeRunningTasks++;
        taskTypeInfo.typeUsedMemory += estimatedNeededMemory;
    }

    private void updateStateForTaskFinished(long estimatedNeededMemory, TaskTypeInformation taskTypeInfo) {
        runningTasks--;
        usedMemory -= estimatedNeededMemory;
        taskTypeInfo.typeRunningTasks--;
        taskTypeInfo.typeUsedMemory -= estimatedNeededMemory;
    }

    private <V> V runTask(MemoryConsumingTask<V> task) throws Exception {
        return task.call();
    }

    private <V> String formatLogMessage(String msg, MemoryConsumingTask<V> task, long estimatedNeededMemory) {
        String enabled = config.isWeightWatcherEnabled() ? "" : "DISABLED, ";
        String state = String.format(enabled + "estimated: %.1fm, managed: %.1fm, used: %.1fm, running: %d", toMebibyte(estimatedNeededMemory), toMebibyte(totalManagedMemory), toMebibyte(usedMemory), runningTasks);
        return String.format("%s %s [%s]", msg, getTaskLogId(task), state);
    }

    private double toMebibyte(long estimatedNeededMemory) {
        return estimatedNeededMemory / 1024.0 / 1024.0;
    }

    private String getTaskLogId(MemoryConsumingTask<?> task) {
        String className = task.getClass().getName();
        className = className.substring(className.lastIndexOf('.') + 1);
        return className + "#" + System.identityHashCode(task) + "(" + task.getTaskType().name() + ")";
    }

    private void warnIfOverAvailableMemory(MemoryConsumingTask<?> task, long estimatedNeededMemory) {
        if (usedMemory > totalManagedMemory) {
            LOG.warn(formatLogMessage("OVER MEMORY LIMIT", task, estimatedNeededMemory));
        }
    }

    private void wakeUpNextQueuedTask() {
        if (nextTaskType == null || nextTaskType.queuedTasks.isEmpty())
            nextTaskType = pickNextTaskType();
        // (else) next task was already assigned before, but couldn't be scheduled yet, should be tried again

        if (nextTaskType != null)
            nextTaskType.queuedTasks.getFirst().wakeUp();
        // (else) no queued tasks
    }

    private TaskTypeInformation pickNextTaskType() {
        // pick first task type where queue is non-empty (round robin)
        for (int i = 0; i < taskTypes.size(); i++) {
            roundRobinTaskTypesIndex = (roundRobinTaskTypesIndex + 1) % taskTypes.size();

            TaskTypeInformation taskTypeInfo = taskTypes.get(roundRobinTaskTypesIndex);
            if (!taskTypeInfo.queuedTasks.isEmpty()) {
                return taskTypeInfo;
            }
        }

        return null; // no queued tasks
    }

    public void reconfigure(WeightWatcherConfiguration newConfig) {
        Objects.requireNonNull(newConfig);

        lock.lock();
        try {
            // create copy to avoid that others can modify our config while we use it
            config = new WeightWatcherConfiguration(newConfig);

            totalManagedMemory = percentageOf(totalSystemMemory, config.getMemoryUsageFactor());
        } finally {
            lock.unlock();
        }
    }

    private long percentageOf(long total, double factor) {
        return Math.round(total * Math.max(0.0, Math.min(factor, 1.0)));
    }

    private TaskTypeInformation provideTaskTypeInformation(String taskTypeName) {
        Objects.requireNonNull(taskTypeName);

        TaskTypeInformation taskTypeInfo = taskTypesMap.get(taskTypeName);
        if (taskTypeInfo == null) {
            taskTypeInfo = new TaskTypeInformation(); // defaults, if we didn't encounter this task type before
            taskTypesMap.put(taskTypeName, taskTypeInfo);
            taskTypes.add(taskTypeInfo);
        }
        return taskTypeInfo;
    }

    private boolean canTaskProceed(MemoryConsumingTask<?> task, TaskTypeInformation taskTypeInfo, long estimatedNeededMemory) {

        if (!config.isWeightWatcherEnabled())
            return true; // WeightWatcher disabled

        // others are already queued -> don't let this one proceed to avoid starvation of older tasks (unless it is the oldest task of course)
        if (tasksQueued() && !isFirstQueuedTask(task))
            return false;

        // let the task proceed if we are below the concurrency limit
        // note: this could also mean that we schedule a task that is over the memory limit
        if (isBelowMinimumConcurrency())
            return true;

        if (isConcurrencyLimitReached())
            return false;

        if (isMemoryLimitReached(estimatedNeededMemory))
            return false;

        // let the task proceed
        return true;
    }

    // checks if the given task is the first of the queued ones
    private boolean isFirstQueuedTask(MemoryConsumingTask<?> task) {
        return nextTaskType != null && nextTaskType.queuedTasks.getFirst().task == task;
    }

    private boolean tasksQueued() {
        for (TaskTypeInformation taskTypeInfo : taskTypes) {
            if (!taskTypeInfo.queuedTasks.isEmpty())
                return true;
        }
        return false;
    }

    private boolean isBelowMinimumConcurrency() {
        return runningTasks < 1; // one task is always allowed to proceed (even if it consumes too much memory)
    }

    private boolean isMemoryLimitReached(long estimatedNeededMemory) {
        return usedMemory + estimatedNeededMemory > totalManagedMemory;
    }

    private boolean isConcurrencyLimitReached() {
        return runningTasks >= getTotalConcurrentTasksLimit();
    }

    private int getTotalConcurrentTasksLimit() {
        int limit = config.getTotalConcurrentTasksLimit();
        if (limit <= 0) // 0 or negative means no limit
            return Integer.MAX_VALUE;
        return limit;
    }

    // ONLY USED FOR UNIT TESTING!
    boolean isTaskQueued(MemoryConsumingTask<?> task) {
        lock.lock();
        try {
            for (TaskTypeInformation taskTypeInfo : taskTypes) {
                for (QueuedTask queuedTask : taskTypeInfo.queuedTasks) {
                    if (queuedTask.task == task)
                        return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stores runtime information for a specific task type.
     */
    private class TaskTypeInformation {

        /**
         * Tasks of this task type that have been queued because they would be over the memory or concurrency limit.
         */
        public LinkedList<QueuedTask> queuedTasks = new LinkedList<>();

        /**
         * Currently running tasks of this task type.
         */
        public int typeRunningTasks = 0;

        /**
         * Currently used memory by tasks of this task type.
         */
        public int typeUsedMemory = 0;
    }

    private class QueuedTask {

        public final MemoryConsumingTask<?> task;
        public final TaskTypeInformation taskTypeInfo;
        public final long estimatedNeededMemory;

        private final long queuedSinceNanoTime;
        private long lastLoggedNanoTime;

        private boolean sleeping = true;
        private final Condition wakeUpCondition = lock.newCondition();

        public QueuedTask(MemoryConsumingTask<?> task, TaskTypeInformation taskTypeInfo, long estimatedNeededMemory) {
            this.task = task;
            this.taskTypeInfo = taskTypeInfo;
            this.estimatedNeededMemory = estimatedNeededMemory;
            this.queuedSinceNanoTime = System.nanoTime();
            this.lastLoggedNanoTime = queuedSinceNanoTime;
        }

        public void waitForBetterTimes() throws InterruptedException {
            sleeping = true;

            while (sleeping) {
                int warnTime = config.getBlockedWarnTimeMillis();

                if (warnTime >= 0) {
                    long nanoTime = System.nanoTime();
                    long sinceLastLogged = nanoTime - lastLoggedNanoTime;

                    if (sinceLastLogged > warnTime * 1000L * 1000L) {
                        long blockedSince = nanoTime - queuedSinceNanoTime;
                        LOG.warn(formatLogMessage("Blocked since " + (blockedSince / 1000L / 1000L / 1000L) + "s", task, estimatedNeededMemory));
                        lastLoggedNanoTime = nanoTime;
                    }

                    wakeUpCondition.await(warnTime + 1, TimeUnit.MILLISECONDS);
                } else {
                    wakeUpCondition.await();
                }
            }
        }

        public void wakeUp() {
            sleeping = false;
            wakeUpCondition.signal(); // always just one
        }
    }
}
