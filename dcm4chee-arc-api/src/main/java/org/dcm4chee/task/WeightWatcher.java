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

/**
 * The Weight Watcher coordinates tasks that consume a lot of memory, e.g. Compression and Decompression tasks.
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public interface WeightWatcher {

    /**
     * Executes the given task on the calling thread.
     * <p>
     * The execution might be blocked for a certain time to prevent the application from using too much memory.
     * <p>
     * For this to work correctly, it is important for the task to ensure that BEFORE the end of its <code>call</code>
     * method all the used memory is freed again (is eligible for garbage collection).
     *
     * @param task the memory consuming task
     * @param <V>  return type of the task
     *
     * @throws InterruptedException thrown if the task had to be blocked, but the blocking was interrupted
     * @throws Exception            exceptions thrown by the {@link MemoryConsumingTask#call} method
     */
    <V> V execute(MemoryConsumingTask<V> task) throws InterruptedException, Exception;

}
