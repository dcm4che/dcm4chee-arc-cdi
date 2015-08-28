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
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;
    
    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName = "java:/queue/storeremember")
    private Queue storeAndRememberQueue;

    public StoreAndRememberContextBuilder augmentStoreAndRememberContext(String txUID, StoreAndRememberContextBuilder ctxBuilder) {
        List<StoreAndRemember> srs = getStoreRememberTxByUID(txUID);
        
        List<String> failedSopInstanceUIDs = new ArrayList<>();
        String localAE = null;
        String extDeviceName = null;
        long delay = 0;
        STORE_VERIFY_PROTOCOL storeVerifyProtocol = null;
        for (StoreAndRemember sr : srs) {
            localAE = sr.getLocalAE();
            extDeviceName = sr.getExternalDeviceName();
            storeVerifyProtocol = STORE_VERIFY_PROTOCOL.valueOf(sr.getStoreVerifyProtocol());
            delay = sr.getDelay();
            
            if (StoreVerifyStatus.FAILED.equals(sr.getInstanceStatus())) {
                failedSopInstanceUIDs.add(sr.getSopInstanceUID());
            }
        }
        
        return ctxBuilder.localAE(localAE)
                .externalDeviceName(extDeviceName)
                .storeVerifyProtocol(storeVerifyProtocol).delayMs(delay)
                .instances(failedSopInstanceUIDs.toArray(new String[failedSopInstanceUIDs.size()]));
    }
    
    public String[] getStudyInstances(String studyIUID) {
        List<String> sopInstanceUIDs = em.createQuery(
                "SELECT inst.sopInstanceUID FROM Series se "
                + "JOIN se.study st "
                + "JOIN se.instances insts"        
                + "JOIN WHERE st.studyInstanceUID = ?1",
                String.class)
                .setParameter(1, studyIUID)
                .getResultList();
        
        return sopInstanceUIDs.toArray(new String[sopInstanceUIDs.size()]);
    }
    
    public String[] getSeriesInstances(String seriesIUID) {
        List<String> sopInstanceUIDs =  em.createQuery(
                "SELECT i.sopInstanceUID FROM Instance i "
                + "WHERE i.series.seriesInstanceUID = ?1",
                String.class)
                .setParameter(1, seriesIUID)
                .getResultList();
        return sopInstanceUIDs.toArray(new String[sopInstanceUIDs.size()]);
    }
    
    public void scheduleStoreAndRemember(StoreAndRememberContext ctx) {
        try {
            Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(storeAndRememberQueue);
                ObjectMessage msg = session.createObjectMessage(ctx);
                msg.setIntProperty("Retries", ctx.getRetries());
                long delay = ctx.getDelay();
                msg.setLongProperty("delay", delay);
                if (delay > 0) {
                    msg.setLongProperty("_HQ_SCHED_DELIVERY", System.currentTimeMillis() + delay);
                }
                producer.send(msg);
            } finally {
                conn.close();
            }
        } catch (JMSException e) {
            throw new RuntimeException("Error while scheduling archiving JMS message", e);
        }
    }

    private List<StoreAndRemember> getStoreRememberTxByUID(String txUID) {
        Query query  = em.createNamedQuery(StoreAndRemember.GET_STORE_REMEMBER_BY_UID);
        query.setParameter(1, txUID);
        @SuppressWarnings("unchecked")
        List<StoreAndRemember> srs = query.getResultList();
        return srs;
    }
    
    private StoreAndRemember getStoreRememberTxByUIDs(String txUID, String sopInstanceUID) {
        Query query  = em.createNamedQuery(StoreAndRemember.GET_STORE_REMEMBER_BY_UIDS);
        query.setParameter(1, txUID);
        query.setParameter(2, sopInstanceUID);
        StoreAndRemember sr = (StoreAndRemember)query.getSingleResult();
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
            sr.setExternalDeviceName(cxt.getExternalDeviceName());
            sr.setStoreVerifyProtocol(cxt.getStoreVerifyProtocol().toString());
            sr.setRetriesLeft(cxt.getRetries());
            sr.setDelay(cxt.getDelay());
            
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
    
    public void updateStoreRemember(String txUID, String sopInstanceUID, StoreVerifyStatus status) {
        StoreAndRemember sr = getStoreRememberTxByUIDs(txUID, sopInstanceUID);
        sr.setInstanceStatus(status);
        em.merge(sr);
    }
    
    public void addExternalLocation(String iuid, String retrieveAET, String retrieveDeviceName, Availability availability) {
        ExternalRetrieveLocation location = new ExternalRetrieveLocation(
                retrieveDeviceName, availability);

        Instance instance = getInstanceByUID(iuid);
        ArrayList<String> currentRetrieveAETs = new ArrayList<String>(
                Arrays.asList(instance.getRetrieveAETs()));
        currentRetrieveAETs.add(retrieveAET);
        String[] updatedRetrieveAETs = new String[currentRetrieveAETs.size()];
        instance.setRetrieveAETs(currentRetrieveAETs
                .toArray(updatedRetrieveAETs));
        location.setInstance(instance);
        em.persist(location);
    }
    
    private Instance getInstanceByUID(String iuid) {
        Query query = em.createQuery("select i from Instance i where i.sopInstanceUID = ?1");
        query.setParameter(1, iuid);
        Instance instance = (Instance) query.getSingleResult();
        return instance;
    }
    
    public String getStoreRememberUIDByStoreVerifyUID(String storeVerifyTxUID) {
        Query query  = em.createNamedQuery(StoreAndRemember.GET_STORE_REMEMBER_BY_STORE_VERIFY_UID);
        query.setParameter(1, storeVerifyTxUID);
        @SuppressWarnings("unchecked")
        List<StoreAndRemember> srs = query.getResultList();
        return (srs.isEmpty()) ? null : srs.iterator().next().getTransactionUID();
    }
 
}
