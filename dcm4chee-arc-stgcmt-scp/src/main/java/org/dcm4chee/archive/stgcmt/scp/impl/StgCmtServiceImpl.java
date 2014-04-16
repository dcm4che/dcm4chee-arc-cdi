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

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.stgcmt.scp.StgCmtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@ApplicationScoped
public class StgCmtServiceImpl implements StgCmtService {

    private static final Logger LOG = LoggerFactory.getLogger(StgCmtServiceImpl.class);

    @Resource(mappedName="java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName="java:/queue/stgcmtscp")
    private Queue stgcmtSCPQueue;

    @Inject
    private StgCmtEJB stgCmtEJB;

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    private Device device;

    private int eventTypeId(Attributes eventInfo) {
        return eventInfo.containsValue(Tag.FailedSOPSequence) ? 2 : 1;
    }

    @Override
    public Attributes calculateResult(Attributes actionInfo) {
        return stgCmtEJB.calculateResult(actionInfo);
    }

    public void scheduleNEventReport(String localAET, String remoteAET,
            Attributes eventInfo, int retries, long delay) {
        try (JMSContext jmsContext = connFactory.createContext();) {
            JMSProducer producer = jmsContext.createProducer();
            producer.setProperty("LocalAET", localAET);
            producer.setProperty("RemoteAET", remoteAET);
            producer.setProperty("Retries", retries);
            producer.setDeliveryDelay(delay);
            producer.send(stgcmtSCPQueue, eventInfo);
        }
    }

    @Override
    public void sendNEventReport(String localAET, String remoteAET,
            Attributes eventInfo, int retries) {
        ApplicationEntity localAE = device
                .getApplicationEntity(localAET);
        if (localAE == null) {
            LOG.warn("Failed to return Storage Commitment Result to {} - no such local AE: {}",
                    remoteAET, localAET);
            return;
        }
        TransferCapability tc = localAE.getTransferCapabilityFor(
                UID.StorageCommitmentPushModelSOPClass, TransferCapability.Role.SCP);
        if (tc == null) {
            LOG.warn("Failed to return Storage Commitment Result to {} - "
                   + "local AE: {} does not support Storage Commitment Push Model in SCP Role",
                    remoteAET, localAET);
            return;
        }
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.addPresentationContext(
                        new PresentationContext(
                                1,
                                UID.StorageCommitmentPushModelSOPClass,
                                tc.getTransferSyntaxes()));
        aarq.addRoleSelection(
                new RoleSelection(UID.StorageCommitmentPushModelSOPClass, false, true));
        try {
            ApplicationEntity remoteAE = aeCache
                    .findApplicationEntity(remoteAET);
            Association as = localAE.connect(remoteAE, aarq);
            try {
                DimseRSP neventReport = as.neventReport(
                        UID.StorageCommitmentPushModelSOPClass,
                        UID.StorageCommitmentPushModelSOPInstance,
                        eventTypeId(eventInfo),
                        eventInfo, null);
                neventReport.next();
            } finally {
                try {
                    as.release();
                } catch (IOException e) {
                    LOG.info("{}: Failed to release association to {}", as, remoteAET);
                }
            }
        } catch (Exception e) {
            ArchiveAEExtension aeExt = localAE.getAEExtension(ArchiveAEExtension.class);
            if (aeExt != null && retries < aeExt.getStorageCommitmentMaxRetries()) {
                int delay = aeExt.getStorageCommitmentRetryInterval();
                LOG.info("Failed to return Storage Commitment Result to {} - retry in {}s: {}",
                        remoteAET, delay, e);
                scheduleNEventReport(localAET, remoteAET, eventInfo, retries + 1, delay * 1000L);
            } else {
                LOG.warn("Failed to return Storage Commitment Result to {}: {}",
                        remoteAET, e);
            }
        }
    }

}
