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

package org.dcm4chee.archive.store.scu.impl;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.service.BasicCStoreSCU;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4chee.archive.store.scu.CStoreSCU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
@ApplicationScoped
public class CStoreSCUImpl implements CStoreSCU {

    private static final Logger LOG = LoggerFactory
            .getLogger(CStoreSCUImpl.class);

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName = "java:/queue/storescu")
    private Queue storeSCUQueue;

    @Inject
    private Device device;

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    private Event<BasicCStoreSCUResp> storeSCUEvent;

    @Override
    public void cstore(String localAET, String remoteAET,
            List<InstanceLocator> insts, int priority, int retries)
            throws DicomServiceException {
        try {
            ApplicationEntity localAE = device.getApplicationEntity(localAET);
            ApplicationEntity destAE = aeCache.findApplicationEntity(remoteAET);
            if (localAE == null) {
                LOG.warn("Failed to store to {} - no such local AE: {}",
                        remoteAET, localAET);
                return;
            }
            AAssociateRQ aarq = makeAAssociateRQ(localAET, remoteAET, insts);
            Association storeas = localAE.connect(destAE, aarq);
            BasicCStoreSCU<InstanceLocator> cstorescu = new BasicCStoreSCU<>(
                    insts, storeas, priority);
            BasicCStoreSCUResp storeRsp = cstorescu.store();
            LOG.info("Instances stored:{} - success:{}", insts.size(),
                    storeRsp.getCompleted());
            storeSCUEvent.fire(storeRsp);

        } catch (ConfigurationException e) {
            throw new DicomServiceException(Status.MoveDestinationUnknown,
                    "Unknown Store/Move Destination: " + remoteAET);
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess,
                    "Unable to Process: " + e.getMessage());
        }

    }

    @Override
    public void scheduleStoreSCU(String localAET, String remoteAET,
            List<? extends InstanceLocator> insts, int retries, int priority,
            long delay) {
        try {
            Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false,
                        Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session
                        .createProducer(storeSCUQueue);
                ObjectMessage msg = session
                        .createObjectMessage((Serializable) insts);
                msg.setStringProperty("LocalAET", localAET);
                msg.setStringProperty("RemoteAET", remoteAET);
                msg.setIntProperty("Priority", priority);
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

    private AAssociateRQ makeAAssociateRQ(String callingAET, String calledAET,
            List<InstanceLocator> insts) {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.setCalledAET(calledAET);
        aarq.setCallingAET(callingAET);
        for (InstanceLocator inst : insts) {
            if (aarq.addPresentationContextFor(inst.cuid, inst.tsuid)) {
                if (!UID.ExplicitVRLittleEndian.equals(inst.tsuid))
                    aarq.addPresentationContextFor(inst.cuid,
                            UID.ExplicitVRLittleEndian);
                if (!UID.ImplicitVRLittleEndian.equals(inst.tsuid))
                    aarq.addPresentationContextFor(inst.cuid,
                            UID.ImplicitVRLittleEndian);
            }
        }
        return aarq;
    }
}
