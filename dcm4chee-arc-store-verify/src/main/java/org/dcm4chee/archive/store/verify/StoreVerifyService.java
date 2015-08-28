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
package org.dcm4chee.archive.store.verify;

import java.util.List;

import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.stow.client.StowContext;

/**
 * Store-verify-service that allows the perform a reliable store by using a protocol that 
 * first stores the data to a target and after that verifies that the storage operation on the target was successful. 
 * The supported store-verify protocols are:
 * <ul>
 *   <li>DICOM C-Store plus Storage-Commitment: CStore is used to store the data on a DICOM target. After that a storage-commitment is requested
 *   for all instances stored.
 *   <li>STOW-RS and QIDO-RS: STOW is used to store the data on a DICOM target. After storage QIDO-requests are sent to the target 
 *   to guarantee the target knows about every stored instance.
 * </ul>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public interface StoreVerifyService {
    
    public static enum STORE_VERIFY_PROTOCOL { CSTORE_PLUS_STGCMT, STOW_PLUS_QIDO, AUTO }
    
    /**
     * Executes a 'store-and-verify' workflow using the STOW and QIDO services.
     * @param transactionUID Optional transaction UID that can be set by the caller for the purpose of matching 
     * the {@link StoreVerifyResponse} received later with this request. The response will contain the same transaction UID. 
     * </p>
     * If the given transaction UID is <code>null</code> then the service will create a
     * unique transaction UID.
     * @param context
     * @param insts
     */
    void store(String transactionUID, StowContext context, List<ArchiveInstanceLocator> insts);
    
    /**
     * Schedules a 'store-and-verify' workflow using the STOW and QIDO services.
     * @param transactionUID Optional transaction UID that can be set by the caller for the purpose of matching 
     * the {@link StoreVerifyResponse} received later with this request. The response will contain the same transaction UID. 
     * </p>
     * If the given transaction UID is <code>null</code> then the service will create a
     * unique transaction UID.
     * @param context
     * @param insts
     */
    void scheduleStore(String transactionUID, StowContext context, List<ArchiveInstanceLocator> insts);
    
    /**
     * Executes a 'store-and-verify' workflow using the C-Store-SCU and Storage-Commitment services.
     * @param transactionUID Optional transaction UID that can be set by the caller for the purpose of matching 
     * the {@link StoreVerifyResponse} received later with this request. The response will contain the same transaction UID. 
     * </p>
     * If the given transaction UID is <code>null</code> then the service will create a
     * unique transaction UID.
     * @param context
     * @param insts
     */
    void store(String transactionUID, CStoreSCUContext context, List<ArchiveInstanceLocator> insts);

    /**
     * Schedules a 'store-and-verify' workflow using the C-Store-SCU and Storage-Commitment services.
     * @param transactionUID Optional transaction UID that can be set by the caller for the purpose of matching 
     * the {@link StoreVerifyResponse} received later with this request. The response will contain the same transaction UID. 
     * </p>
     * If the given transaction UID is <code>null</code> then the service will create a
     * unique transaction UID.
     * @param context
     * @param insts
     */
    void scheduleStore(String transactionUID, CStoreSCUContext context, List<ArchiveInstanceLocator> insts);

    /**
     * Generates a unique store-verify transaction UID that can be supplied to the store-methods.
     * @param dimse
     * @return
     */
    String generateTransactionUID(boolean dimse);

}
