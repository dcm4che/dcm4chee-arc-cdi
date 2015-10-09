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

import com.google.common.util.concurrent.SettableFuture;
import org.dcm4chee.archive.conf.WeightWatcherConfiguration;
import org.dcm4chee.task.MemoryConsumingTask;
import org.dcm4chee.task.TaskType;
import org.dcm4chee.task.WeightWatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unit tests for {@link WeightWatcher}.
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class WeightWatcherTest {

    private enum TestTaskTypes implements TaskType {
        TEST_TASK_TYPE1,
        TEST_TASK_TYPE2
    }

    private ExecutorService executor;

    private WeightWatcherImpl weightWatcher;

    @Before
    public void before() {
        if (executor != null)
            executor.shutdownNow();
        executor = Executors.newCachedThreadPool(); // unbounded

        int totalMemory = 20; // simulate a JVM with 20 bytes maximum "heap" size

        WeightWatcherConfiguration config = new WeightWatcherConfiguration();
        config.setMemoryUsageFactor(0.5); // weight watcher may use 50% of the total memory -> 10 bytes
        config.setTotalConcurrentTasksLimit(5);

        weightWatcher = new WeightWatcherImpl(config, totalMemory);
    }

    @After
    public void after() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        executor = null;
        weightWatcher = null;
    }

    @Test
    public void testExecute() throws Exception {
        // the most simple test: check that WeightWatcher executes the task

        TestTask task1 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 10);
        task1.mayProceed();
        task1.expectFinished();
    }

    @Test
    public void testConcurrentExecute() throws Exception {
        // tasks within memory limits are executed concurrently

        TestTask task1 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 6);
        task1.expectStarted();

        TestTask task2 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 4);
        task2.expectStarted();
    }

    @Test
    public void testTotalConcurrencyLimit() throws Exception {
        TestTask task1 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 1);
        TestTask task2 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 1);
        TestTask task3 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 1);
        TestTask task4 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 1);
        TestTask task5 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 1);
        task1.expectStarted();
        task2.expectStarted();
        task3.expectStarted();
        task4.expectStarted();
        task5.expectStarted();

        // 6 concurrent tasks is the max, this one will be blocked
        TestTask task6 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 1);
        task6.expectBlocked();

        // this one will be blocked too of course
        TestTask task7 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 1);
        task7.expectBlocked();

        // let task1 proceed -> task6 can then be started
        task1.mayProceed();
        task6.expectStarted();
        task7.expectBlocked();

        // also let some other task proceed -> task7 can then be started
        task4.mayProceed();
        task7.expectStarted();
    }

    @Test
    public void testTotalMemoryLimit() throws Exception {
        TestTask task1 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 9);
        task1.expectStarted();

        // total memory limit is 10, therefore task2 should be blocked
        TestTask task2 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 2);
        task2.expectBlocked();

        // let task1 proceed -> task2 can then be started
        task1.mayProceed();
        task2.expectStarted();
    }

    @Test
    public void testTotalMemoryLimit2() throws Exception {
        TestTask task1 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 9);
        task1.expectStarted();

        // total memory limit is 10, therefore task2 should be blocked
        TestTask task2 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 9);
        task2.expectBlocked();

        // task3 is of course also blocked
        TestTask task3 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 9);
        task2.expectBlocked();
        task3.expectBlocked();

        // let task1 proceed -> task2 can then be started
        task1.mayProceed();
        task2.expectStarted();
        task3.expectBlocked(); // task3 still blocked

        // now also let task2 proceed, then task3 can finally start
        task2.mayProceed();
        task3.expectStarted();
    }

    @Test
    public void testOverMemoryLimit() throws Exception {
        // check that WeightWatcher will execute a task even if it is above the memory threshold,
        // if it is the only one.
        // but there should be a warning in the log then (which is not checked in this unit test)

        TestTask task1 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 11);
        task1.expectStarted();

        // a second concurrent task will be blocked of course
        TestTask task2 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 1);
        task2.expectBlocked();

        // let task1 proceed -> task2 can then be started
        task1.mayProceed();
        task2.expectStarted();
    }

    @Test
    public void testNoStarvation() throws Exception {
        TestTask task1 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 5);
        task1.expectStarted();

        // above total memory limit -> blocked
        TestTask task2 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 7);
        task2.expectBlocked();

        // a lighter task comes in, we could actually execute it, but we don't because we don't want the previously
        // blocked one to starve -> also blocked
        TestTask task3 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 4);
        task3.expectBlocked();

        // let task1 proceed, task1 can then be started, but task3 stays blocked, because together they would be over
        // the memory limit again
        task1.mayProceed();
        task2.expectStarted();
        task3.expectBlocked();

        // let task2 proceed -> task3 can be started
        task2.mayProceed();
        task3.expectStarted();
    }

    @Test
    public void testFairness() throws Exception {
        // test fair round-robin behavior for queued tasks of different types

        TestTask task1 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 10);
        task1.expectStarted();

        TestTask task2 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 10);
        task2.expectBlocked();

        task1.mayProceed();
        task2.expectStarted();

        TestTask task3 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 10);
        task3.expectBlocked();

        TestTask task4 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 10);
        task4.expectBlocked();

        TestTask task5 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE2, 10);
        task5.expectBlocked();

        task2.mayProceed();
        // as the last blocked task that was started was TEST_TASK_TYPE1, we now expect the TEST_TASK_TYPE2 to get started
        task5.expectStarted();
        task3.expectBlocked();

        // check that order within one task type is of course still preserved
        task5.mayProceed();
        task3.expectStarted();
        task4.expectBlocked();
    }

    @Test
    public void testCanceling() throws Exception {
        TestTask task1 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 10);
        task1.expectStarted();

        TestTask task2 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 10);
        task2.expectBlocked();

        task2.cancelByInterrupt();

        // ugly, wait a bit, so that we are sure that the interrupt was processed
        uglyPause();

        task1.mayProceed();

        TestTask task3 = submitTestTask(TestTaskTypes.TEST_TASK_TYPE1, 10);
        task3.mayProceed();
        task3.expectFinished();

        task2.expectNotStartedAndNotBlocked();
    }

    @Test
    public void testForbidNestedExecution() throws Exception {

        // check that WeightWatcher doesn't allow any nested execution (starting a task within a task), because it is
        // likely to lead to deadlocks, if memory/concurrency limits are reached

        try {
            weightWatcher.execute(new MemoryConsumingTask<Void>() {
                @Override
                public TaskType getTaskType() {
                    return TestTaskTypes.TEST_TASK_TYPE1;
                }

                @Override
                public long getEstimatedWeight() {
                    return 10;
                }

                @Override
                public Void call() throws Exception {

                    // nested call
                    return weightWatcher.execute(new MemoryConsumingTask<Void>() {
                        @Override
                        public TaskType getTaskType() {
                            return TestTaskTypes.TEST_TASK_TYPE2;
                        }

                        @Override
                        public long getEstimatedWeight() {
                            return 10;
                        }

                        @Override
                        public Void call() throws Exception {
                            return null;
                        }
                    });
                }
            });
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Nested execution is forbidden")) {
                return; // // expected exception
            }
        }
        Assert.fail("Expecting IllegalStateException with specific message");
    }

    private TestTask submitTestTask(TestTaskTypes taskType, long weight) {
        return new TestTask(taskType, weightWatcher, weight).submit(executor);
    }

    private static class TestTask {

        private final WeightWatcherImpl weightWatcher;
        private final long weight;
        private final TestTaskTypes taskType;

        private MemoryConsumingTask<Boolean> watchedTask;

        private volatile boolean submitted = false;
        private volatile boolean mayProceed = false;
        private SettableFuture<Boolean> started = SettableFuture.create();
        private Future<Boolean> taskFinished;

        private Lock lock = new ReentrantLock();
        private Condition mayProceedCondition = lock.newCondition();

        public TestTask(TestTaskTypes taskType, WeightWatcherImpl weightWatcher, long weight) {
            this.taskType = taskType;
            this.weightWatcher = weightWatcher;
            this.weight = weight;
        }

        public TestTask submit(ExecutorService executor) {
            taskFinished = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    submitted = true;

                    watchedTask = new MemoryConsumingTask<Boolean>() {

                        @Override
                        public TaskType getTaskType() {
                            return taskType;
                        }

                        @Override
                        public long getEstimatedWeight() {
                            return weight;
                        }

                        @Override
                        public Boolean call() throws Exception {
                            started.set(true);

                            lock.lock();
                            try {
                                while (!mayProceed) {
                                    mayProceedCondition.await();
                                }
                            } finally {
                                lock.unlock();
                            }

                            return true;
                        }
                    };

                    return weightWatcher.execute(watchedTask);
                }
            });

            while (!submitted)
                Thread.yield(); // wait for the task submission

            return this;
        }

        public void mayProceed() {
            lock.lock();
            try {
                mayProceed = true;
                mayProceedCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public void cancelByInterrupt() {
            taskFinished.cancel(true);
        }

        public void expectFinished() throws InterruptedException, ExecutionException, TimeoutException {
            Assert.assertTrue(taskFinished.get(3, TimeUnit.SECONDS));
        }

        public void expectStarted() throws InterruptedException, ExecutionException, TimeoutException {
            Assert.assertTrue(started.get(3, TimeUnit.SECONDS));
        }

        public void expectBlocked() throws InterruptedException, ExecutionException, TimeoutException {

            // this is ugly, but its hard to find out otherwise on whether a task has been "not" started
            uglyPause();

            Assert.assertTrue("Expecting task blocked", weightWatcher.isTaskQueued(watchedTask));
            Assert.assertFalse("Expecting task not started", started.isDone());
        }

        public void expectNotStartedAndNotBlocked() throws InterruptedException, ExecutionException, TimeoutException {

            // this is ugly, but its hard to find out otherwise on whether a task has been "not" started
            uglyPause();

            Assert.assertFalse("Expecting task not blocked", weightWatcher.isTaskQueued(watchedTask));
            Assert.assertFalse("Expecting task not started", started.isDone());
        }
    }

    private static void uglyPause() throws InterruptedException {
        Thread.yield();
        Thread.sleep(50);
        Thread.yield();
    }
}
