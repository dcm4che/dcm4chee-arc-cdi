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

package org.dcm4chee.archive.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
@NamedQueries({
    @NamedQuery(
            name=StoreAndRemember.GET_STORE_REMEMBER_BY_UID,
            query="select sr from StoreAndRemember sr where sr.transactionUID = ?1"),
    @NamedQuery(
            name=StoreAndRemember.GET_STORE_REMEMBER_BY_UIDS,
            query="select sr from StoreAndRemember sr where sr.transactionUID = ?1 and sr.sopInstanceUID = ?2"),
    @NamedQuery(
            name=StoreAndRemember.GET_STORE_REMEMBER_BY_STORE_VERIFY_UID,
            query="select sr from StoreAndRemember sr where sr.storeVerifyTransactionUID = ?1")
})
@Entity
@Table(name="store_and_remember")
public class StoreAndRemember {
    public static final String GET_STORE_REMEMBER_BY_UID = "StoreRememberTransaction.getStoreRememberTransactionByUID";
    public static final String GET_STORE_REMEMBER_BY_UIDS = "StoreRememberTransaction.getStoreRememberTransactionByUIDs";
    public static final String GET_STORE_REMEMBER_BY_STORE_VERIFY_UID = "StoreRememberTransaction.getStoreRememberTransactionByStoreVerifyUID";
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;
    
    @Column(name="tx_uid")
    private String transactionUID;
    
    @Column(name="store_verify_tx_uid")
    private String storeVerifyTransactionUID;
    
    @Column(name="store_verify_protocol")
    private String storeVerifyProtocol;
   
    @Column(name="retries_left")
    private int retriesLeft;
    
    @Column(name="delay")
    private long delay;
    
    @Column(name="local_ae")
    private String localAE;
    
    @Column(name="ext_device_name")
    private String externalDeviceName;
    
    @Column(name="status")
    private StoreAndRememberStatus status;
    
    @Column(name="sop_instance_uid")
    private String sopInstanceUID;
    
    @Column(name="instance_status")
    private StoreVerifyStatus instanceStatus;

    public long getPk() {
        return pk;
    }
    
    public String getTransactionUID() {
        return transactionUID;
    }

    public void setTransactionUID(String transactionUID) {
        this.transactionUID = transactionUID;
    }
    
    public String getStoreVerifyTransactionUID() {
        return storeVerifyTransactionUID;
    }

    public void setStoreVerifyTransactionUID(String storeVerifyTransactionUID) {
        this.storeVerifyTransactionUID = storeVerifyTransactionUID;
    }

    public String getStoreVerifyProtocol() {
        return storeVerifyProtocol;
    }

    public void setStoreVerifyProtocol(String storeVerifyProtocol) {
        this.storeVerifyProtocol = storeVerifyProtocol;
    }

    public int getRetriesLeft() {
        return retriesLeft;
    }
    
    public void setDelay(long delay) {
        this.delay = delay;
    }
    
    public long getDelay() {
        return delay;
    }

    public void setRetriesLeft(int retriesLeft) {
        this.retriesLeft = retriesLeft;
    }

    public String getLocalAE() {
        return localAE;
    }

    public void setLocalAE(String localAE) {
        this.localAE = localAE;
    }

    public String getExternalDeviceName() {
        return externalDeviceName;
    }

    public void setExternalDeviceName(String externalDeviceName) {
        this.externalDeviceName = externalDeviceName;
    }

    public StoreAndRememberStatus getStatus() {
        return status;
    }

    public void setStatus(StoreAndRememberStatus status) {
        this.status = status;
    }
    
    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setSopInstanceUID(String sopInstanceUID) {
        this.sopInstanceUID = sopInstanceUID;
    }

    public StoreVerifyStatus getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(StoreVerifyStatus instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

}
