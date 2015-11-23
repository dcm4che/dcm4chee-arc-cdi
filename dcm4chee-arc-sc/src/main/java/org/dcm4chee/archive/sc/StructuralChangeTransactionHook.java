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

package org.dcm4chee.archive.sc;



/**
 * Hook that allows to add arbitrary logic to life-cycle phases of a transaction 
 * that includes structural changes.
 * 
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 */
public interface StructuralChangeTransactionHook {

    /**
     * Transaction life-cycle method. It is called before a current transaction 
     * that is associated with structural changes is committed (= BEFORE-COMMIT phase).
     * </p>
     * The method implementation is allowed to fail the transaction by either marking it as 'set-rollback-only' internally or
     * by returning <code>false</code>.
     * </p>
     * If no structural changes are associated with a current transaction then this method is NOT called.
     * @param changeContainer
     * @return Returns <code>true</code> if the current transaction should proceed, returns <code>false</code> if the current
     * transaction should be rolled back
     */
    boolean beforeCommitStructuralChanges(StructuralChangeContainer changeContainer);
    
    /**
     * Transaction life-cycle method. It is called after a transaction associated with structural changes has been
     * committed successfully (= AFTER-COMMIT phase).
     * <p>
     * <ul>
     *   <li>The method is executed outside any transaction context. 
     *   If the logic implemented by this method is supposed to run inside a transaction then it is the 
     *   responsibility of the implementation to ensure this</li>
     *   <li>The method is executed asynchronously in a different thread than the one that committed the SC transaction</li>
     *   <li>The method is not called for failed (=rolled-back) transactions</li>
     * </ul>
     * @param changeContainer
     */
    void afterCommitStructuralChanges(StructuralChangeContainer changeContainer);
    
}
