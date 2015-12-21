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

package org.dcm4chee.archive.sc.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionSynchronizationRegistry;

import org.dcm4chee.archive.sc.StructuralChangeContext;
import org.dcm4chee.archive.sc.StructuralChangeRejectedException;
import org.dcm4chee.util.TransactionSynchronization;

/**
 * Aggregates structural changes that happen within a transaction.
 * 
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 */
@ApplicationScoped
public class StructuralChangeTransactionAggregator {
    private static final String ON_SC_COMMIT_RUNNER_TX_RESOURCE = OnStructuralChangesCommitRunner.class.getName();
    
    @Inject
    private TransactionSynchronization txSnychronization;
    
    @Inject
    private StructuralChangeHookExecutor scChangeHookExecutor;
    
    /**
     * Aggregates the result state of a QC operation to the current QC workflow.
     * @param qcEvent Result state of QC operation
     */
    public void aggregate(StructuralChangeContext changeContext) {
        StructuralChangeContainerImpl changeContainer = initOrGetChangeContainer();
        if (changeContainer != null) {
            changeContainer.addContext(changeContext);
        }
    }
    
    private StructuralChangeContainerImpl initOrGetChangeContainer() {
        try {
            Transaction tx = txSnychronization.getTransactionManager().getTransaction();
            if (tx == null) {
                return null;
            }
            
            TransactionSynchronizationRegistry txSyncRegistry = txSnychronization.getSynchronizationRegistry();

            OnStructuralChangesCommitRunner  onStructuralChangesCommitRunner  = (OnStructuralChangesCommitRunner)txSyncRegistry.getResource(ON_SC_COMMIT_RUNNER_TX_RESOURCE);
            if (onStructuralChangesCommitRunner == null) {
                onStructuralChangesCommitRunner = new OnStructuralChangesCommitRunner();
                tx.registerSynchronization(onStructuralChangesCommitRunner);
                txSyncRegistry.putResource(ON_SC_COMMIT_RUNNER_TX_RESOURCE, onStructuralChangesCommitRunner);
            }
            return onStructuralChangesCommitRunner.getStructuralChangeContainer();
        } catch (SystemException | IllegalStateException e) {
            throw new RuntimeException("Could not register transaction synchronizer for aggregating structural changes within transaction");
        } catch (RollbackException e) {
            return null;
        } 
    }
    
    /*
     * Transaction synchronization hook that executes logic in the before-commit and after-commit
     * phases of a transaction with structural changes
     */
    private class OnStructuralChangesCommitRunner implements Synchronization {
        private StructuralChangeContainerImpl changeContainer = new StructuralChangeContainerImpl();

        private StructuralChangeContainerImpl getStructuralChangeContainer() {
            return changeContainer;
        }
        
        /*
         * Method ONLY CALLED for ACTIVE (not-rolled-back) transactions!
         * 
         * Reason for setting the context classloader: the beforeCompletion() might be called
         * when committing a transaction started / associated with another deployment (EAR). 
         * CDI (Weld) would use the context classloader from the other EAR to resolve scChangeHookExecutor 
         * => fails with exception.
         */
        @Override
        public void beforeCompletion() {
            if (changeContainer != null) {
                ClassLoader origCtxClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(StructuralChangeTransactionAggregator.class.getClassLoader());
                    if (!scChangeHookExecutor.executeBeforeCommitStructuralChangeHooks(changeContainer)) {
                        // let transaction fail
                        throw new StructuralChangeRejectedException();
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(origCtxClassLoader);
                }
            }
        }
        
        @Override
        public void afterCompletion(int status) {
            if (changeContainer != null) {
                ClassLoader origCtxClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(StructuralChangeTransactionAggregator.class.getClassLoader());
                    if(status == Status.STATUS_COMMITTED) {
                        scChangeHookExecutor.asyncExecuteAfterCommitStructuralChangeHooks(changeContainer);
                    }
                } finally {
                    changeContainer = null;
                    Thread.currentThread().setContextClassLoader(origCtxClassLoader);
                }
            }
        }
       
    }

}
