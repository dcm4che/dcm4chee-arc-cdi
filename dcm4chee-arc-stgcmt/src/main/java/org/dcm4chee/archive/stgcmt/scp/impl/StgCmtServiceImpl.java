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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4chee.archive.stgcmt.scp.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXTransformer.SetupTransformer;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.DateUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.stgcmt.scp.CommitEvent;
import org.dcm4chee.archive.stgcmt.scp.StgCmtService;
import org.dcm4chee.storage.RetrieveContext;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.service.RetrieveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.Tuple;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

@ApplicationScoped
public class StgCmtServiceImpl implements StgCmtService {

    private static final Logger LOG = LoggerFactory
            .getLogger(StgCmtServiceImpl.class);

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connFactory;
 
    @Resource(mappedName = "java:/queue/stgcmtscp")
    private Queue stgcmtSCPQueue;

    @Inject
    private StgCmtEJB stgCmtEJB;

    @Inject
    private RetrieveService storageRetrieveService;

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    private Device device;

    @Inject
    @Any
    private Event<CommitEvent> commitEvent; 

    private int eventTypeId(Attributes eventInfo) {
        return eventInfo.containsValue(Tag.FailedSOPSequence) ? 2 : 1;
    }

    @Override
    public Attributes calculateResult(Attributes actionInfo) {

        List<Tuple> foundMatches = stgCmtEJB.lookupMatches(actionInfo);
        return stgCmtEJB.calculateResult(checkForDigestAndAdjust(foundMatches),
                actionInfo);
    }

    private List<Tuple> checkForDigestAndAdjust(List<Tuple> foundMatches) {

        for (java.util.Iterator<Tuple> iter = foundMatches.iterator(); iter
                .hasNext();) {
            Tuple tuple = iter.next();

            String digest = tuple.get(3, String.class);
            String filePath = tuple.get(4, String.class);
            String storageSystemID = tuple.get(5, String.class);
            String storageGroupID = tuple.get(6, String.class);
            StorageDeviceExtension devExt = device
                    .getDeviceExtension(StorageDeviceExtension.class);
            StorageSystem storageSystem = devExt.getStorageSystem(
                    storageGroupID, storageSystemID);
            RetrieveContext ctx = storageRetrieveService
                    .createRetrieveContext(storageSystem);
            try {
                if (!storageRetrieveService.calculateDigestAndMatch(ctx,
                        digest, filePath)) {
                    iter.remove();
                }
            } catch (IOException e) {
                LOG.error(
                        "Failed to calculate digest on storage commitment request"
                                + ", no digest check is performed, {}", e);
                return foundMatches;

            }
        }
        return foundMatches;

    }

    @Override
    public void scheduleNEventReport(String localAET, String remoteAET,
            Attributes eventInfo, int retries, long delay) {
        try {
            Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false,
                        Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session
                        .createProducer(stgcmtSCPQueue);
                ObjectMessage msg = session.createObjectMessage(eventInfo);
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
    public void sendNEventReport(String localAET, String remoteAET,
            Attributes eventInfo, int retries) {
        ApplicationEntity localAE = device.getApplicationEntity(localAET);
        if (localAE == null) {
            LOG.warn(
                    "Failed to return Storage Commitment Result to {} - no such local AE: {}",
                    remoteAET, localAET);
            return;
        }
        TransferCapability tc = localAE.getTransferCapabilityFor(
                UID.StorageCommitmentPushModelSOPClass,
                TransferCapability.Role.SCP);
        if (tc == null) {
            LOG.warn(
                    "Failed to return Storage Commitment Result to {} - "
                            + "local AE: {} does not support Storage Commitment Push Model in SCP Role",
                    remoteAET, localAET);
            return;
        }
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.addPresentationContext(new PresentationContext(1,
                UID.StorageCommitmentPushModelSOPClass, tc
                        .getTransferSyntaxes()));
        aarq.addRoleSelection(new RoleSelection(
                UID.StorageCommitmentPushModelSOPClass, false, true));
        try {
            ApplicationEntity remoteAE = aeCache
                    .findApplicationEntity(remoteAET);
            Association as = localAE.connect(remoteAE, aarq);
            try {
                DimseRSP neventReport = as.neventReport(
                        UID.StorageCommitmentPushModelSOPClass,
                        UID.StorageCommitmentPushModelSOPInstance,
                        eventTypeId(eventInfo), eventInfo, null);
                neventReport.next();
            } finally {
                try {
                    as.release();
                } catch (IOException e) {
                    LOG.info("{}: Failed to release association to {}", as,
                            remoteAET);
                }
            }
        } catch (Exception e) {
            ArchiveAEExtension aeExt = localAE
                    .getAEExtension(ArchiveAEExtension.class);
            if (aeExt != null
                    && retries < aeExt.getStorageCommitmentMaxRetries()) {
                int delay = aeExt.getStorageCommitmentRetryInterval();
                LOG.info(
                        "Failed to return Storage Commitment Result to {} - retry in {}s: {}",
                        remoteAET, delay, e);
                scheduleNEventReport(localAET, remoteAET, eventInfo,
                        retries + 1, delay * 1000L);
            } else {
                LOG.warn(
                        "Failed to return Storage Commitment Result to {}: {}",
                        remoteAET, e);
            }
        }
    }

    @Override
    public N_ACTION_REQ_STATE sendNActionRequest(String localAET, String remoteAET,
            List<ArchiveInstanceLocator> insts, String transactionUID) {

        ApplicationEntity localAE = device.getApplicationEntity(localAET);
        if (localAE == null) {
            LOG.error("Invalid Storage Commitment Request [{} -> {}]: no such local AE: '{}'",
                    localAET, remoteAET, localAET);
            return N_ACTION_REQ_STATE.INVALID_REQ;
        }
        
        TransferCapability tc = localAE.getTransferCapabilityFor(
                UID.StorageCommitmentPushModelSOPClass,
                TransferCapability.Role.SCU);
        if (tc == null) {
            LOG.error("Invalid Storage Commitment Request [{}->{}]: "
                    + "local AE '{}' does not support Storage Commitment Push Model in SCU Role",
                    localAET, remoteAET, localAE);
            return N_ACTION_REQ_STATE.INVALID_REQ;
        }
        
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.addPresentationContext(new PresentationContext(1,
                UID.StorageCommitmentPushModelSOPClass, tc
                        .getTransferSyntaxes()));
        aarq.addRoleSelection(new RoleSelection(
                UID.StorageCommitmentPushModelSOPClass, true, false));

        Attributes action = createAction(insts, transactionUID);

        try {
            ApplicationEntity remoteAE = aeCache
                    .findApplicationEntity(remoteAET);
            Association as = localAE.connect(remoteAE, aarq);
            try {
                DimseRSP rsp = as.naction(
                        UID.StorageCommitmentPushModelSOPClass,
                        UID.StorageCommitmentPushModelSOPInstance, 1, action,
                        null);
                rsp.next();
            } finally {
                try {
                    as.release();
                } catch (IOException e) {
                    LOG.info("{}: Failed to release association to {}", as,
                            remoteAET);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to send Storage Commitment Request [{}->{}]: {}", localAET, remoteAET, e.getMessage());
            return N_ACTION_REQ_STATE.SEND_REQ_FAILED;
        }
        
        return N_ACTION_REQ_STATE.SEND_REQ_OK;
    }

    private Attributes createAction(List<ArchiveInstanceLocator> insts,
            String transactionUID) {
        Attributes rsp = new Attributes();
        rsp.setString(Tag.TransactionUID, VR.UI, transactionUID);
        Sequence isntSeq = rsp.newSequence(Tag.ReferencedSOPSequence, insts.size());
        for (ArchiveInstanceLocator inst : insts) {
            Attributes instAtt = new Attributes();
            instAtt.setString(Tag.ReferencedSOPClassUID, VR.UI, inst.cuid);
            instAtt.setString(Tag.ReferencedSOPInstanceUID, VR.UI, inst.iuid);
            isntSeq.add(instAtt);
        }
        return rsp;
    }

    @Override
    public void coerceAttributes(Attributes attrs, final String remoteAET,
            final ArchiveAEExtension arcAE, Role role)
            throws DicomServiceException {
        try {
            Attributes modified = new Attributes();
            Templates tpl = remoteAET != null ? arcAE
                    .getAttributeCoercionTemplates(
                            attrs.getString(Tag.SOPClassUID),
                            Dimse.N_EVENT_REPORT_RQ, role, remoteAET) : null;
            if (tpl != null) {
                attrs.update(SAXTransformer.transform(attrs, tpl, false, false,
                        new SetupTransformer() {

                            @Override
                            public void setup(Transformer transformer) {
                                Date date = new Date();
                                String currentDate = DateUtils.formatDA(null,
                                        date);
                                String currentTime = DateUtils.formatTM(null,
                                        date);
                                transformer.setParameter("date", currentDate);
                                transformer.setParameter("time", currentTime);
                                transformer.setParameter("calling", remoteAET);
                                transformer.setParameter("called", arcAE
                                        .getApplicationEntity().getAETitle());
                            }
                        }), modified);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public void notify(CommitEvent commitEvt) {
        commitEvent.fire(commitEvt);
    }
    
}
