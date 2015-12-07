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

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.dcm4chee.archive.entity.ExternalRetrieveLocation;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.StoreAndRemember;
import org.dcm4chee.archive.entity.StoreAndRememberStatus;
import org.dcm4chee.archive.entity.StoreVerifyStatus;
import org.dcm4chee.archive.store.remember.StoreAndRememberContext;
import org.dcm4chee.archive.store.remember.StoreAndRememberContextBuilder;
import org.dcm4chee.archive.store.verify.StoreVerifyService.STORE_VERIFY_PROTOCOL;
import org.dcm4chee.storage.conf.Availability;

/**
 * 
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 */
@Stateless
public class StoreAndRememberEJB {

    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;
    
    public StoreAndRememberContextBuilder augmentStoreAndRememberContext(String txUID, StoreAndRememberContextBuilder ctxBuilder) {
        List<StoreAndRemember> srs = getStoreRememberTxByUID(txUID);
        
        List<String> failedSopInstanceUIDs = new ArrayList<>();
        String localAE = null;
        String remoteAE = null;
        String extDeviceName = null;
        STORE_VERIFY_PROTOCOL storeVerifyProtocol = null;
        boolean remember = false;
        for (StoreAndRemember sr : srs) {
            localAE = sr.getLocalAE();
            remoteAE = sr.getRemoteAE();
            extDeviceName = sr.getExternalDeviceName();
            storeVerifyProtocol = STORE_VERIFY_PROTOCOL.valueOf(sr.getStoreVerifyProtocol());
            remember = sr.isRemember();
            
            if (StoreVerifyStatus.FAILED.equals(sr.getInstanceStatus())) {
                failedSopInstanceUIDs.add(sr.getSopInstanceUID());
            }
        }
        
        return ctxBuilder.localAE(localAE)
                .externalDeviceName(extDeviceName)
                .remoteAE(remoteAE)
                .storeVerifyProtocol(storeVerifyProtocol)
                .remember(remember)
                .instances(failedSopInstanceUIDs.toArray(new String[failedSopInstanceUIDs.size()]));
    }
    
    public String[] getNotExternallyArchivedStudyInstances(String studyIUID, String extDeviceName) {
        // TODO: ALEX: expensive query!
        List<String> sopInstanceUIDs = em.createQuery(
                "SELECT DISTINCT inst.sopInstanceUID "
                        + "FROM ExternalRetrieveLocation el "
                        + "RIGHT JOIN el.instance inst "
                        + "WHERE inst.series.study.studyInstanceUID = ?1 "
                        + "AND (el.retrieveDeviceName IS NULL "
                        + "OR el.retrieveDeviceName != ?2)",
                String.class)
                .setParameter(1, studyIUID)
                .setParameter(2, extDeviceName)
                .getResultList();
        return sopInstanceUIDs.toArray(new String[sopInstanceUIDs.size()]);
    }
    
    public String[] getNotExternallyArchivedSeriesInstances(String seriesIUID, String extDeviceName) {
        // TODO: ALEX: expensive query!
        List<String> sopInstanceUIDs =  em.createQuery(
                "SELECT DISTINCT inst.sopInstanceUID "
                + "FROM ExternalRetrieveLocation el "
                + "RIGHT JOIN el.instance inst "
                + "WHERE inst.series.seriesInstanceUID = ?1 "
                + "AND (el.retrieveDeviceName IS NULL "
                + "OR el.retrieveDeviceName != ?2)",
                String.class)
                .setParameter(1, seriesIUID)
                .setParameter(2, extDeviceName)
                .getResultList();
        return sopInstanceUIDs.toArray(new String[sopInstanceUIDs.size()]);
    }
    
    private List<StoreAndRemember> getStoreRememberTxByUID(String txUID) {
        TypedQuery<StoreAndRemember> query  = em.createNamedQuery(StoreAndRemember.GET_STORE_REMEMBER_BY_UID, StoreAndRemember.class);
        query.setParameter(1, txUID);
        List<StoreAndRemember> srs = query.getResultList();
        return srs;
    }
    
    public StoreAndRemember getStoreRememberTxByUIDs(String txUID, String sopInstanceUID) {
        TypedQuery<StoreAndRemember> query  = em.createNamedQuery(StoreAndRemember.GET_STORE_REMEMBER_BY_UIDS, StoreAndRemember.class);
        query.setParameter(1, txUID);
        query.setParameter(2, sopInstanceUID);
        StoreAndRemember sr = query.getSingleResult();
        return sr;
    }
    
    public void createOrUpdateStoreRememberTx(StoreAndRememberContext cxt, String storeVerifyTxUID) {
        if(!updateStoreVerifyUIDOfStoreRemembers(cxt.getTransactionUID(), storeVerifyTxUID)) {
            addStoreRememberTx(cxt, storeVerifyTxUID);
        }
    }
    
    private void addStoreRememberTx(StoreAndRememberContext cxt, String storeVerifyTxUID) {
        for (String sopInstanceUID : cxt.getInstances()) {
            StoreAndRemember sr = new StoreAndRemember();
            sr.setTransactionUID(cxt.getTransactionUID());
            sr.setStoreVerifyTransactionUID(storeVerifyTxUID);
            sr.setStatus(StoreAndRememberStatus.PENDING);
            sr.setLocalAE(cxt.getLocalAE());
            sr.setRemoteAE(cxt.getRemoteAE());
            sr.setExternalDeviceName(cxt.getExternalDeviceName());
            sr.setStoreVerifyProtocol(cxt.getStoreVerifyProtocol().toString());
            sr.setRetriesLeft(cxt.getRetries());
            sr.setRemember(cxt.isRemember());
            sr.setSopInstanceUID(sopInstanceUID);
            sr.setInstanceStatus(StoreVerifyStatus.PENDING);
            em.persist(sr);
        }
    }
    
    public int updatePartialStoreRemembersAndCheckForRetry(String txUID) {
        List<StoreAndRemember> srs = getStoreRememberTxByUID(txUID);
        StoreAndRemember _sr = srs.iterator().next();
        
        int newRetriesLeft = 0;
        StoreAndRememberStatus newStatus = null;
        int retriesLeft = _sr.getRetriesLeft();
        if(retriesLeft > 0) {
            newRetriesLeft = retriesLeft - 1;
            newStatus = StoreAndRememberStatus.INCOMPLETE;
        } else {
            newStatus = StoreAndRememberStatus.FAILED;
        }
        
        for(StoreAndRemember sr : srs) {
            sr.setStatus(newStatus);
            sr.setRetriesLeft(newRetriesLeft);
            em.merge(sr);
        }
        
        return retriesLeft;
    }
    
    public void removeStoreRemembers(String txUID) {
        for(StoreAndRemember sr : getStoreRememberTxByUID(txUID)) {
            em.remove(sr);
        }
    }
    
    private boolean updateStoreVerifyUIDOfStoreRemembers(String txUID, String storeVerifyTxUID) {
        List<StoreAndRemember> srs = getStoreRememberTxByUID(txUID);
        for(StoreAndRemember sr : srs) {
            sr.setStoreVerifyTransactionUID(storeVerifyTxUID);
            em.merge(sr);
        }
        
        return srs.size() > 0;
    }
    
    public void updateStoreRemember(StoreAndRemember sr, StoreVerifyStatus status) {
        sr.setInstanceStatus(status);
        em.merge(sr);
    }
    
    public void rememberLocation(String iuid, String retrieveAET, String retrieveDeviceName, Availability availability) {
        ExternalRetrieveLocation location = new ExternalRetrieveLocation(
                retrieveDeviceName, availability);

        Instance instance = getInstanceByUID(iuid);
        instance.addRetrieveAET(retrieveAET);
        location.setInstance(instance);
        em.persist(location);
    }
    
    private Instance getInstanceByUID(String iuid) {
        TypedQuery<Instance> query = em.createQuery("select i from Instance i where i.sopInstanceUID = ?1", Instance.class);
        query.setParameter(1, iuid);
        Instance instance = query.getSingleResult();
        return instance;
    }
    
    public String getStoreRememberUIDByStoreVerifyUID(String storeVerifyTxUID) {
        TypedQuery<StoreAndRemember> query  = em.createNamedQuery(StoreAndRemember.GET_STORE_REMEMBER_BY_STORE_VERIFY_UID, 
                StoreAndRemember.class);
        query.setParameter(1, storeVerifyTxUID);
        List<StoreAndRemember> srs = query.getResultList();
        return (srs.isEmpty()) ? null : srs.iterator().next().getTransactionUID();
    }
 
}
