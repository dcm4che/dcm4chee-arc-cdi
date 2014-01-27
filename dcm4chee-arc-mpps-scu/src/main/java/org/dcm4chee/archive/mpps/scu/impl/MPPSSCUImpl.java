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

package org.dcm4chee.archive.mpps.scu.impl;

import java.io.IOException;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;

import org.dcm4che.conf.api.IApplicationEntityCache;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.UID;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.Device;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseRSP;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.mpps.event.MPPSEvent;
import org.dcm4chee.archive.mpps.scu.MPPSSCU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
public class MPPSSCUImpl implements MPPSSCU {

    private static final Logger LOG = LoggerFactory.getLogger(MPPSSCUImpl.class);

    @Resource(mappedName="java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName="java:/queue/mppsscu")
    private Queue mppsSCUQueue;

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    private Device device;

    @SuppressWarnings("unused")
    private void onMPPSReceive(@Observes MPPSEvent event) {
        ArchiveAEExtension arcAE = event.getArchiveAEExtension();
        String localAET = arcAE.getApplicationEntity().getAETitle();
        String iuid = event.getPerformedProcedureStep().getSopInstanceUID();
        Attributes attrs = event.getAttributes();
        for (String remoteAET : arcAE.getForwardMPPSDestinations()) {
            scheduleForwardMPPS(event.getDIMSE(), localAET, remoteAET,
                    iuid, attrs, 0, 0);
        }
    }

    private void scheduleForwardMPPS(Dimse dimse, String localAET, String remoteAET,
            String iuid, Attributes attrs, int retries, long delay) {
        try (JMSContext jmsContext = connFactory.createContext();) {
            JMSProducer producer = jmsContext.createProducer();
            producer.setProperty("CommandField", dimse.name());
            producer.setProperty("SOPInstancesUID", iuid);
            producer.setProperty("LocalAET", localAET);
            producer.setProperty("RemoteAET", remoteAET);
            producer.setProperty("Retries", retries);
            producer.setDeliveryDelay(delay);
            producer.send(mppsSCUQueue, attrs);
        }
    }

    @Override
    public void sendMPPS(Dimse dimse, String localAET, String remoteAET,
            String iuid, Attributes attrs, int retries) {
        ApplicationEntity localAE = device
                .getApplicationEntity(localAET);
        if (localAE == null) {
            LOG.warn("Failed to MMPS to {} - no such local AE: {}",
                    remoteAET, localAET);
            return;
        }
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.addPresentationContext(
                        new PresentationContext(
                                1,
                                UID.ModalityPerformedProcedureStepSOPClass,
                                UID.ExplicitVRLittleEndian,
                                UID.ImplicitVRLittleEndian));
        try {
            ApplicationEntity remoteAE = aeCache
                    .findApplicationEntity(remoteAET);
            Association as = localAE.connect(remoteAE, aarq);
            DimseRSP rsp = sendMPPS(as, dimse, iuid, attrs);
            rsp.next();
            try {
                as.release();
            } catch (IOException e) {
                LOG.info("{}: Failed forward MPPS to {}", as, remoteAET);
            }
        } catch (Exception e) {
            ArchiveAEExtension aeExt = localAE.getAEExtension(ArchiveAEExtension.class);
            if (aeExt != null && retries < aeExt.getStorageCommitmentMaxRetries()) {
                int delay = aeExt.getStorageCommitmentRetryInterval();
                LOG.info("Failed to return Storage Commitment Result to {} - retry in {}s: {}",
                        remoteAET, delay, e);
                scheduleForwardMPPS(dimse, localAET, remoteAET, iuid, attrs,
                        retries + 1, delay * 1000L);
            } else {
                LOG.warn("Failed to return Storage Commitment Result to {}: {}",
                        remoteAET, e);
            }
        }
    }

    private DimseRSP sendMPPS(Association as, Dimse dimse, String iuid,
            Attributes attrs) throws IOException, InterruptedException {
        switch(dimse) {
        case N_CREATE_RQ:
            return as.ncreate(
                    UID.ModalityPerformedProcedureStepSOPClass,
                    iuid, attrs, null);
        case N_SET_RQ:
            return as.nset(
                    UID.ModalityPerformedProcedureStepSOPClass,
                    iuid, attrs, null);
        default:
            throw new IllegalArgumentException("dimse: " + dimse);
        }
        
    }

}
