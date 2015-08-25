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
import javax.inject.Inject;
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
import org.dcm4chee.archive.store.verify.StoreVerifyService.STORE_VERIFY_PROTOCOL;
import org.dcm4chee.storage.archiver.service.ArchivingQueueProvider;
import org.dcm4chee.storage.archiver.service.ExternalDeviceArchiverContext;
import org.dcm4chee.storage.conf.Availability;

/**
 * 
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 */
@Stateless
public class StoreAndRememberEJB {
    private static final String ARCHIVING_MSG_TYPE_PROP = "archiving_msg_type";
    
    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;
    
    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Inject
    private ArchivingQueueProvider queueProvider;
    
    public StoreAndRememberContext createStoreAndRememberContext(String txUID, int retries) {
        List<StoreAndRemember> srs = getStoreRememberTxByUID(txUID);
        
        List<String> failedSopInstanceUIDs = new ArrayList<>();
        String extDeviceName = null;
        long delay = 0;
        STORE_VERIFY_PROTOCOL storeVerifyProtocol = null;
        for (StoreAndRemember sr : srs) {
            extDeviceName = sr.getExternalDeviceName();
            storeVerifyProtocol = STORE_VERIFY_PROTOCOL.valueOf(sr.getStoreVerifyProtocol());
            delay = sr.getDelay();
            
            if (StoreVerifyStatus.FAILED.equals(sr.getInstanceStatus())) {
                failedSopInstanceUIDs.add(sr.getSopInstanceUID());
            }
        }
        
        return new StoreAndRememberContext(txUID, extDeviceName, storeVerifyProtocol, 
                failedSopInstanceUIDs.toArray(new String[failedSopInstanceUIDs.size()]), retries, delay);
    }
    
    public void scheduleStoreAndRememberOfStudy(String studyIUID, String externalDeviceName, STORE_VERIFY_PROTOCOL storeVerifyProtocol, int retry, long delay) {
        List<String> seriesUIDs = em.createQuery("SELECT se.seriesInstanceUID FROM Series se JOIN se.study st WHERE st.studyInstanceUID = ?1",
                String.class)
                .setParameter(1, studyIUID)
                .getResultList();
        for (String seriesUID : seriesUIDs) {
            scheduleStoreAndRememberOfSeries(seriesUID, externalDeviceName, storeVerifyProtocol, retry, delay);
        }
    }
    
    public void scheduleStoreAndRememberOfSeries(String seriesIUID, String externalDeviceName, STORE_VERIFY_PROTOCOL storeVerifyProtocol, int retry, long delay) {
        List<Instance> insts = em.createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID, Instance.class)
                .setParameter(1, seriesIUID)
                .getResultList();
        if (insts.size() > 0) {
            String[] instanceUIDs = new String[insts.size()];
            int i = 0;
            for(Instance inst : insts) {
                instanceUIDs[i++] = inst.getSopInstanceUID();
            }
            StoreAndRememberContext ctx = new StoreAndRememberContext(externalDeviceName, storeVerifyProtocol, instanceUIDs, retry, delay);
            scheduleStoreAndRemember(ctx);
        }       
    }
    
    public void scheduleStoreAndRemember(StoreAndRememberContext ctx) {
        ExternalDeviceArchiverContext extContext = Helper.convert(ctx);
        
        try {
            Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue archivingQueue = queueProvider.getQueue(extContext);
                if(archivingQueue != null) {
                    MessageProducer producer = session.createProducer(archivingQueue);
                    ObjectMessage msg = session.createObjectMessage(extContext);
                    msg.setIntProperty("Retries", ctx.getRetries());
                    // set message type -> Receiving MDBs might filter messages based on type
                    msg.setStringProperty(ARCHIVING_MSG_TYPE_PROP, extContext.getClass().getName());
                    long delay = ctx.getDelay();
                    msg.setLongProperty("delay", delay);
                    if (delay > 0) {
                        msg.setLongProperty("_HQ_SCHED_DELIVERY", System.currentTimeMillis() + delay);
                    }
                    producer.send(msg);
                }
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
    
    public void addStoreRememberTx(String txUID, String storeVerifyTxUID, String externalDeviceName, 
            STORE_VERIFY_PROTOCOL protocol, String[] sopInstanceUIDs, int retries, long delay) {
        for (String sopInstanceUID : sopInstanceUIDs) {
            StoreAndRemember sr = new StoreAndRemember();
            sr.setTransactionUID(txUID);
            sr.setStoreVerifyTransactionUID(storeVerifyTxUID);
            sr.setStatus(StoreAndRememberStatus.PENDING);
            sr.setExternalDeviceName(externalDeviceName);
            sr.setStoreVerifyProtocol(protocol.toString());
            sr.setRetriesLeft(retries);
            sr.setDelay(delay);
            
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
    
    public void updateStoreVerifyUIDOfStoreRemembers(String txUID, String storeVerifyTxUID) {
        List<StoreAndRemember> srs = getStoreRememberTxByUID(txUID);
        for(StoreAndRemember sr : srs) {
            sr.setStoreVerifyTransactionUID(storeVerifyTxUID);
            em.merge(sr);
        }
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
