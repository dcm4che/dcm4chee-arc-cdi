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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4chee.archive.ian.scu.impl;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.QStudy;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.ian.scu.IANSCU;
//import org.dcm4chee.archive.iocm.RejectionEvent;
//import org.dcm4chee.archive.iocm.RejectionType;
import org.dcm4chee.archive.mpps.event.MPPSEvent;
import org.dcm4chee.archive.mpps.event.MPPSFinal;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.hibernate.HibernateQuery;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
public class IANSCUImpl implements IANSCU {

    private static final Logger LOG = LoggerFactory.getLogger(IANSCUImpl.class);

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @Resource(mappedName="java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName="java:/queue/ianscu")
    private Queue ianSCUQueue;

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    private Device device;

    public void onMPPSReceive(@Observes @MPPSFinal MPPSEvent event) {
        ApplicationEntity ae = event.getApplicationEntity();
        MPPS mpps = event.getPerformedProcedureStep();
        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcAE != null && arcAE.getIANDestinations().length > 0
                && !isIncorrectWorklistEntrySelected(mpps)) {
            IANBuilder builder = createIANBuilder(mpps);
            if (builder.numberOfOutstandingInstances() == 0)
                scheduleSendIAN(ae.getAETitle(), arcAE.getIANDestinations(),
                        builder.getIAN());
        }
    }

    private boolean isIncorrectWorklistEntrySelected(MPPS mpps) {
        ArchiveDeviceExtension arcDev = 
                device.getDeviceExtension(ArchiveDeviceExtension.class);
        Code incorrectWorklistEntrySelected = arcDev != null
                ? (Code) arcDev.getIncorrectWorklistEntrySelectedCode()
                : null;
        return incorrectWorklistEntrySelected != null
                && incorrectWorklistEntrySelected.equals(mpps.getDiscontinuationReasonCode());
    }

    private void scheduleSendIAN(String localAET, String[] remoteAETs,
            Attributes ian) {
        String iuid = UIDUtils.createUID();
        for (String remoteAET : remoteAETs) {
            scheduleSendIAN(localAET, remoteAET, iuid, ian, 0, 0);
        }
    }

    private IANBuilder createIANBuilder(MPPS pps) {
        IANBuilder builder = new IANBuilder();
        Attributes ppsattrs = pps.getAttributes();
        builder.setReferencedMPPS(pps.getSopInstanceUID(), ppsattrs);
        Sequence perfSeriesSeq = ppsattrs.getSequence(Tag.PerformedSeriesSequence);
        for (Attributes series : perfSeriesSeq) {
            String seriesIUID = series.getString(Tag.SeriesInstanceUID);
            if (seriesIUID == null)
                throw new IllegalArgumentException(
                        "Missing Series Instance UID");

            List<Tuple> list = new HibernateQuery(
                    em.unwrap(org.hibernate.Session.class))
                .from(QInstance.instance)
                .innerJoin(QInstance.instance.series, QSeries.series)
                .innerJoin(QSeries.series.study, QStudy.study)
                .where(QSeries.series.seriesInstanceUID.eq(seriesIUID),
                        QInstance.instance.rejectionNoteCode.isNull())
                .list(QStudy.study.studyInstanceUID,
                        QInstance.instance.sopInstanceUID,
                        QInstance.instance.sopClassUID,
                        QInstance.instance.availability,
                        QInstance.instance.retrieveAETs,
                        QInstance.instance.externalRetrieveLocations);

            for (Tuple tuple : list) {
                builder.addReferencedInstance(
                        tuple.get(QStudy.study.studyInstanceUID),
                        seriesIUID,
                        tuple.get(QInstance.instance.sopInstanceUID),
                        tuple.get(QInstance.instance.sopClassUID),
                        tuple.get(QInstance.instance.availability),
                        Utils.decodeAETs(
                            tuple.get(QInstance.instance.retrieveAETs)));
            }
        }
        return builder;
    }

    public void onStoreInstance(@Observes StoreContext storeContext) {
        if (storeContext.getStoreAction() != StoreAction.STORE)
            return;

        StoreSession storeSession = storeContext.getStoreSession();
        ArchiveAEExtension arcAE = storeSession.getArchiveAEExtension();
        if (arcAE == null || arcAE.getIANDestinations().length == 0)
            return;

        MPPS mpps = (MPPS) storeContext.getProperty(MPPS.class.getName());
        if (mpps != null && mpps.getStatus() != MPPS.Status.IN_PROGRESS
                && !isIncorrectWorklistEntrySelected(mpps))
            scheduleIANForMPPS(storeContext, mpps);
    }

    private void scheduleIANForMPPS(StoreContext storeContext, MPPS mpps) {
        StoreSession storeSession = storeContext.getStoreSession();
        IANBuilder builder = (IANBuilder) storeSession.getProperty(IANBuilder.class.getName());
        if (builder == null
                || !builder.getMPPSInstanceUID().equals(mpps.getSopInstanceUID())) {
            builder = createIANBuilder(mpps);
            storeSession.setProperty(IANBuilder.class.getName(), builder);
        } else {
            Instance inst = storeContext.getInstance();
            Series series = inst.getSeries();
            Study study = series.getStudy();
            builder.addReferencedInstance(
                    study.getStudyInstanceUID(),
                    series.getSeriesInstanceUID(),
                    inst.getSopInstanceUID(),
                    inst.getSopClassUID(),
                    inst.getAvailability(),
                    inst.getAllRetrieveAETs());
        }
        if (builder.numberOfOutstandingInstances() == 0)
            scheduleSendIAN(storeSession.getLocalAET(),
                    storeSession.getArchiveAEExtension().getIANDestinations(),
                    builder.getIAN());
    }

//    public void onRejectInstances(@Observes RejectionEvent event) {
//        StoreContext storeContext = event.getStoreContext();
//        StoreSession storeSession = storeContext.getStoreSession();
//        ArchiveAEExtension arcAE = storeSession.getArchiveAEExtension();
//        if (arcAE == null || arcAE.getIANDestinations().length == 0)
//            return;
//
//        try {
//            Attributes attrs = storeContext.getAttributes();
//            if (event.getRejectionType() == RejectionType.IncorrectModalityWorklistEntry) {
//                for (String mppsIUID : event.getPerformedProcedureStepIUIDs()) {
//                    MPPS mpps = em.createNamedQuery(MPPS.FIND_BY_SOP_INSTANCE_UID, MPPS.class)
//                            .setParameter(1, mppsIUID)
//                            .getSingleResult();
//                    scheduleSendIAN(storeSession.getLocalAET(),
//                            storeSession.getArchiveAEExtension().getIANDestinations(),
//                            createIANBuilder(mpps).getIAN());
//                }
//            } else {
//                IANBuilder builder = new IANBuilder();
//                for (SOPInstanceReference sopRef : em.createNamedQuery(
//                                Instance.SOP_INSTANCE_REFERENCE_BY_STUDY_INSTANCE_UID,
//                                SOPInstanceReference.class)
//                            .setParameter(1, attrs.getString(Tag.StudyInstanceUID))
//                            .getResultList()) {
//                    builder.addReferencedInstance(
//                            sopRef.studyInstanceUID, 
//                            sopRef.seriesInstanceUID,
//                            sopRef.sopInstanceUID,
//                            sopRef.sopClassUID,
//                            sopRef.availability,
//                            sopRef.getRetrieveAETs());
//                }
//                scheduleSendIAN(storeSession.getLocalAET(),
//                        storeSession.getArchiveAEExtension().getIANDestinations(),
//                        builder.getIAN());
//            }
//        } catch (Exception e) {
//            Instance inst = storeContext.getInstance();
//            LOG.warn("{}: Failed to schedule IAN for Rejection Note[iuid={}, code={}]",
//                    storeSession,
//                    inst.getSopInstanceUID(),
//                    inst.getConceptNameCode(),
//                    e);
//        }
//    }

    private void scheduleSendIAN(String localAET, String remoteAET,
            String iuid, Attributes attrs, int retries, long delay) {
        try {
            Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(ianSCUQueue);
                ObjectMessage msg = session.createObjectMessage(attrs);
                msg.setStringProperty("SOPInstancesUID", iuid);
                msg.setStringProperty("LocalAET", localAET);
                msg.setStringProperty("RemoteAET", remoteAET);
                msg.setIntProperty("Retries", retries);
                if (delay > 0)
                    msg.setLongProperty("_HQ_SCHED_DELIVERY",
                            System.currentTimeMillis() + delay);
                producer.send(msg);
            } finally {
                conn.close();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendIAN(String localAET, String remoteAET,
            String iuid, Attributes attrs, int retries) {
        ApplicationEntity localAE = device
                .getApplicationEntity(localAET);
        if (localAE == null) {
            LOG.warn("Failed to send IAN to {} - no such local AE: {}",
                    remoteAET, localAET);
            return;
        }
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.addPresentationContext(
                        new PresentationContext(
                                1,
                                UID.InstanceAvailabilityNotificationSOPClass,
                                UID.ExplicitVRLittleEndian,
                                UID.ImplicitVRLittleEndian));
        try {
            ApplicationEntity remoteAE = aeCache
                    .findApplicationEntity(remoteAET);
            Association as = localAE.connect(remoteAE, aarq);
            DimseRSP rsp = as.ncreate(
                    UID.InstanceAvailabilityNotificationSOPClass,
                    iuid, attrs, null);
            rsp.next();
            try {
                as.release();
            } catch (IOException e) {
                LOG.info("{}: Failed to release Association to {}", as, remoteAET);
            }
        } catch (Exception e) {
            ArchiveAEExtension aeExt = localAE.getAEExtension(ArchiveAEExtension.class);
            if (aeExt != null && retries < aeExt.getIANMaxRetries()) {
                int delay = aeExt.getIANRetryInterval();
                LOG.info("Failed to send IAN to {} - retry in {}s: {}",
                        remoteAET, delay, e);
                scheduleSendIAN(localAET, remoteAET, iuid, attrs,
                        retries + 1, delay * 1000L);
            } else {
                LOG.warn("Failed to send IAN to {}: {}",
                        remoteAET, e);
            }
        }
    }

}
