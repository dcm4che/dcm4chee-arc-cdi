//
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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4chee.archive.task.executor.impl;

import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

/**
 * Service to execute tasks asynchronously using the thread resources of the underlying platform.
 * 
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 */
@Stateless
public class PlatformTaskExecutor {

    public static enum RESULT_STATE { SUCCESS, ERROR };
    
    /**
     * Represents the result of an executed task.
     */
    public static class Result {
        private final RESULT_STATE state;
        private final Exception exception;
        
        private static Result success() {
            return new Result(RESULT_STATE.SUCCESS, null);
        }
        
        private static Result error(Exception e) {
            return new Result(RESULT_STATE.ERROR, e);
        }
        
        private Result(RESULT_STATE state, Exception e) {
            this.state = state;
            this.exception = e;
        }
        
        public RESULT_STATE getState() {
            return state;
        }
        
        public Exception getException() {
            return exception;
        }
    }
    
    /**
     * @param <K> 
     */
    public interface CompletionHandler<K> {
        /**
         * Called on successful completion of task execution
         * @param key Key that was registered when scheduling the task for execution
         */
        void onComplete(K key);
        
        /**
         * Called on error completion of task execution
         * @param key Key that was registered when scheduling the task for execution
         * @param e
         */
        void onException(K key, Exception e);
    }
    
    /**
     * Runs a given task asynchronously using a future-strategy
     * @param runnable Task to execute
     * @return Returns a future representation of the task execution state
     */
    @Asynchronous
    public Future<Result> asyncExecute(Runnable runnable) {
        try {
            runnable.run();
            return new AsyncResult<PlatformTaskExecutor.Result>(Result.success());
        } catch(Exception e) {
            return new AsyncResult<PlatformTaskExecutor.Result>(Result.error(e));
        }
    }
    
    /**
     * Runs a given task asynchronously using a callback-strategy.
     * @param key Allows to register arbitrary information that will be passed on into the completion handler
     * at the end of execution
     * @param runnable Task to execute
     * @param completionHandler Callback that will be called on successful / error completion of the task
     */
    @Asynchronous
    public <K> void asyncExecute(final K key, Runnable runnable, CompletionHandler<K> completionHandler) {
        try {
            runnable.run();
            completionHandler.onComplete(key);
        } catch(Exception e) {
           completionHandler.onException(key, e);
        }
    }
    
}
