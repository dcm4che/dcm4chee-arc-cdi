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

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.dcm4chee.archive.sc.StructuralChangeContext;
import org.dcm4chee.archive.sc.StructuralChangeRejectedException;

/**
 * Aggregates structural changes that happen within a transaction.
 * 
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 */
@ApplicationScoped
public class StructuralChangeTransactionAggregator {
    private static final String CHANGE_CONTAINER_TX_RESOURCE = "StructuralChangeContainer";
    
    @Resource(lookup="java:jboss/TransactionManager")
    private TransactionManager tmManager; 
    
    @Resource(lookup="java:jboss/TransactionSynchronizationRegistry")
    private TransactionSynchronizationRegistry tsRegistry;
    
    @Inject
    private StructuralChangeHookExecutor beforeCommitExecutor;
    
    /**
     * Aggregates the result state of a QC operation to the current QC workflow.
     * @param qcEvent Result state of QC operation
     */
    public void aggregate(StructuralChangeContext changeContext) {
        StructuralChangeContainerImpl changeContainer = initOrGetWorkflow();
        if (changeContainer != null) {
            changeContainer.addContext(changeContext);
        }
    }
    
    private StructuralChangeContainerImpl initOrGetWorkflow() {
        try {
            Transaction tx = tmManager.getTransaction();
            if (tx == null) {
                return null;
            }

            StructuralChangeContainerImpl changeContainer = (StructuralChangeContainerImpl)tsRegistry.getResource(CHANGE_CONTAINER_TX_RESOURCE);
            if (changeContainer == null) {
                tx.registerSynchronization(new OnStructuralChangesCommitRunner());
                changeContainer = new StructuralChangeContainerImpl();
                tsRegistry.putResource(CHANGE_CONTAINER_TX_RESOURCE, changeContainer);
            }
            return changeContainer;
        } catch (SystemException | IllegalStateException e) {
            throw new RuntimeException("Could not register transaction synchronizer for aggregating structural changes within transaction");
        } catch (RollbackException e) {
            return null;
        } 
    }
    
    /*
     * Transaction synchronization hook that executes logic in the before-commit
     * phase of a transaction with structural changes
     */
    private class OnStructuralChangesCommitRunner implements Synchronization {

        // only called for active (not-rolled-back) transactions
        @Override
        public void beforeCompletion() {
            StructuralChangeContainerImpl changeContainer = (StructuralChangeContainerImpl)tsRegistry.getResource(CHANGE_CONTAINER_TX_RESOURCE);
            if (changeContainer != null) {
                if (!beforeCommitExecutor.executeBeforCommitStructuralChangeHooks(changeContainer)) {
                    // let transaction fail
                    throw new StructuralChangeRejectedException();
                }
            }
        }
        
        @Override
        public void afterCompletion(int status) {
            // nop
        }
       
    }

}
