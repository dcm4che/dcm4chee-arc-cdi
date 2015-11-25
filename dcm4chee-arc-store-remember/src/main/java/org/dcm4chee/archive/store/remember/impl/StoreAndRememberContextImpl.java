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

package org.dcm4chee.archive.store.remember.impl;

import org.dcm4chee.archive.store.remember.StoreAndRememberContext;
import org.dcm4chee.archive.store.verify.StoreVerifyService.STORE_VERIFY_PROTOCOL;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
class StoreAndRememberContextImpl implements StoreAndRememberContext {
    private static final long serialVersionUID = 7658929872168L;
    
    private String transactionUID;
    private String localAE;
    private String remoteAE;
    private String externalDeviceName;
    private STORE_VERIFY_PROTOCOL storeVerifyProtocol;
    private String[] instances;
    private int retries;
    private Boolean remember;
    
    public void setTransactionUID(String transactionUID) {
        this.transactionUID = transactionUID;
    }

    public void setLocalAE(String localAE) {
        this.localAE = localAE;
    }
    
    public void setRemoteAE(String remoteAE) {
        this.remoteAE = remoteAE;
    }

    public void setExternalDeviceName(String externalDeviceName) {
        this.externalDeviceName = externalDeviceName;
    }

    public void setStoreVerifyProtocol(STORE_VERIFY_PROTOCOL storeVerifyProtocol) {
        this.storeVerifyProtocol = storeVerifyProtocol;
    }

    public void setInstances(String[] instances) {
        this.instances = instances;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }
    
    public void setRemember(boolean remember) {
        this.remember = remember;
    }

    @Override
    public String getTransactionUID() {
        return transactionUID;
    }
    
    @Override
    public String getLocalAE() {
        return localAE;
    }
    
    @Override
    public String getRemoteAE() {
        return remoteAE;
    }
    
    @Override
    public String getExternalDeviceName() {
        return externalDeviceName;
    }
    
    @Override
    public STORE_VERIFY_PROTOCOL getStoreVerifyProtocol() {
        return storeVerifyProtocol;
    }
    
    @Override
    public String[] getInstances() {
        return instances;
    }
    
    @Override
    public int getRetries() {
        return retries;
    }
    
    @Override
    public Boolean isRemember() {
        return remember;
    }
    
    @Override
    public String toString() {
        return "StoreAndRememberContext[" +
                "transactionUID=" + transactionUID +
                ", localAE=" + localAE +
                ", externalDeviceName=" + externalDeviceName +
                ", remoteAE=" + remoteAE + 
                ", storeVerifyProtocol="+ storeVerifyProtocol +
                ", #instances=" + instances.length +
                ", retries=" + retries
                +"]";
    }
    
}
