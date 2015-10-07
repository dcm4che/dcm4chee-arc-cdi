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

package org.dcm4chee.task;

import java.util.concurrent.Callable;

/**
 * A task the consumes large amounts of memory.
 * <p>
 * The needed memory needs to be estimable BEFORE the task gets started.
 *
 * @param <V> <V> the result type of method {@link #call}
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public interface MemoryConsumingTask<V> extends Callable<V> {

    /**
     * @return type of the task. Typically one of {@link ImageProcessingTaskTypes}. (But extensible through the
     * interface {@link TaskType} for new types)
     */
    TaskType getTaskType();

    /**
     * Calculates the estimated weight of the task, i.e. maximum memory it will consume in bytes at any moment in time
     * during the invocation of its <code>call</code> method.
     * Of course, the estimation has to be done, without actually allocating this memory yet.
     * <p>
     * The information will be used by the executing {@link WeightWatcher} to apply memory and concurrency limits.
     * <p>
     * If the returned value is 0 then this means that no memory and concurrency limits should be
     * applied. This can be used to support the case where a task decides that it does not actually need to consume
     * a lot of memory (e.g. rendition not required, because rendered image loaded from cache).
     *
     * @return estimated weight of the task in bytes
     */
    long getEstimatedWeight();

    /**
     * Allocates memory and computes a result (or throws an exception if unable to do so).
     * <p>
     * BEFORE the end of this method all the used memory needs to be freed again (needs to be eligible for garbage
     * collection).
     *
     * @return computed result
     *
     * @throws Exception if unable to compute a result
     */
    @Override
    V call() throws Exception;

}
