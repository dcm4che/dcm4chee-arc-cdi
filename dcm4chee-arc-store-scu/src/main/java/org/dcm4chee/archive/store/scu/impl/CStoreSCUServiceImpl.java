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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXTransformer.SetupTransformer;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ServiceQualifier;
import org.dcm4chee.archive.fetch.forward.FetchForwardService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUJMSMessage;
import org.dcm4chee.archive.store.scu.CStoreSCUResponse;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.dcm4chee.storage.service.RetrieveService;
import org.dcm4chee.task.WeightWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
@ApplicationScoped
public class CStoreSCUServiceImpl implements CStoreSCUService {

    private static final Logger LOG = LoggerFactory
            .getLogger(CStoreSCUServiceImpl.class);

    @Inject
    private FetchForwardService fetchForwardService;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName = "java:/queue/storescu")
    private Queue storeSCUQueue;

    @Inject
    private Device device;

    @Inject
    @Any
    private Event<CStoreSCUResponse> storeSCUEvent;

    @Inject
    private RetrieveService storageRetrieveService;

    @Inject
    private WeightWatcher weightWatcher;

    @Override
    public void cstore(String messageID, CStoreSCUContext context,
            List<ArchiveInstanceLocator> insts, int priority)
            throws DicomServiceException {
        try {
            ApplicationEntity localAE = device.getApplicationEntity(context.getLocalAE().getAETitle());
            ApplicationEntity remoteAE = context.getRemoteAE();
            if (localAE == null) {
                LOG.warn("Failed to store to {} - no such local AE for "
                        + "transaction {}", remoteAE.getAETitle(), messageID);
                return;
            }
            AAssociateRQ aarq = makeAAssociateRQ(localAE.getAETitle(),
                    remoteAE.getAETitle(), insts);
            Association storeas = localAE.connect(remoteAE, aarq);

            CStoreSCUImpl cstorescu = new CStoreSCUImpl(localAE, remoteAE, context.getService(), this, weightWatcher);
            BasicCStoreSCUResp storeRsp = cstorescu.cstore(insts, storeas, priority);

            storeSCUEvent.select(new ServiceQualifier(context.getService()))
                    .fire(new CStoreSCUResponse(storeRsp, insts,
                            messageID, localAE.getAETitle(), remoteAE.getAETitle()));

        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    private AAssociateRQ makeAAssociateRQ(String callingAET, String calledAET,
            List<ArchiveInstanceLocator> insts) {
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

    @Override
    public void scheduleStoreSCU(String messageID, CStoreSCUContext context,
            List<ArchiveInstanceLocator> insts, int retries, int priority,
            long delay) {
        try {
            Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false,
                        Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session
                        .createProducer(storeSCUQueue);
                ObjectMessage msg = session
                        .createObjectMessage(new CStoreSCUJMSMessage(
                                insts, context));
                msg.setIntProperty("Priority", priority);
                msg.setIntProperty("Retries", retries);
                msg.setStringProperty("MessageID", messageID);
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
    public void coerceAttributes(Attributes attrs, CStoreSCUContext context)
            throws DicomServiceException {
        try {
            if (context.getRemoteAE()!=null) {
                Templates tpl = context.getArchiveAEExtension()
                        .getAttributeCoercionTemplates(
                                attrs.getString(Tag.SOPClassUID), Dimse.C_STORE_RQ,
                                Role.SCU, context.getRemoteAE().getAETitle());
                if (tpl != null)
                    attrs.addAll(SAXTransformer.transform(attrs, tpl, false, false,
                            new CStoreSCUSetupTransformer(context.getLocalAE()
                                    .getAETitle(), context.getRemoteAE()
                                    .getAETitle())));
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public void coerceFileBeforeMerge(ArchiveInstanceLocator inst,
            Attributes attrs, CStoreSCUContext context)
            throws DicomServiceException {
    }

    @Override
    public ArchiveInstanceLocator applySuppressionCriteria(
            ArchiveInstanceLocator ref, Attributes attrs,
            String supressionCriteriaTemplateURI, CStoreSCUContext context) {

        try {
            Templates tpl = SAXTransformer
                    .newTemplates(new StreamSource(
                            StringUtils
                                    .replaceSystemProperties(supressionCriteriaTemplateURI)));
            if (tpl != null) {
                boolean eliminate;
                StringWriter resultWriter = new StringWriter();
                SAXWriter wr = SAXTransformer.getSAXWriter(tpl,
                        new StreamResult(resultWriter),
                        new CStoreSCUSetupTransformer(context.getLocalAE()
                                .getAETitle(), context.getRemoteAE()
                                .getAETitle()));
                wr.write(attrs);
                eliminate = (resultWriter.toString()
                        .compareToIgnoreCase("true") == 0 ? true : false);
                if (!eliminate) {
                    return ref;
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Applying Suppression Criteria on retrieve , using template: "
                            + StringUtils
                                    .replaceSystemProperties(supressionCriteriaTemplateURI)
                            + "\nRemoving Referenced Instance: "
                            + ref.iuid
                            + " from response");
                }
                return null;
            }

        } catch (Exception e) {
            LOG.error("Error applying supression criteria, {}", e);
            return ref;
        }
        return ref;
    }

    @Override
    public ArchiveInstanceLocator eliminateUnSupportedSOPClasses(
            ArchiveInstanceLocator ref, CStoreSCUContext context) {
        if (context.getRemoteAE() != null)
            try {
                // for wado source and destination are the same
                ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                        context.getRemoteAE().getTransferCapabilitiesWithRole(
                                Role.SCU));

                for (TransferCapability supportedTC : aeTCs) {
                    if (supportedTC.getSopClass().compareTo(ref.cuid) == 0) {
                        return ref;
                    }
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Applying UnSupported SOP Class Elimination"
                            + "\nRemoving Referenced Instance: " + ref.iuid);
                }
                return null;
            } catch (Exception e) {
                LOG.error("Exception while applying elimination, {}", e);
                return ref;
            }
        return ref;
    }

    @Override
    public Path getFile(ArchiveInstanceLocator inst) throws IOException {

        ArchiveInstanceLocator archInst = (ArchiveInstanceLocator) inst;

        org.dcm4chee.storage.RetrieveContext ctx = storageRetrieveService
                .createRetrieveContext(archInst.getStorageSystem());
        try {
            return archInst.getEntryName() == null ? storageRetrieveService
                    .getFile(ctx, archInst.getFilePath())
                    : storageRetrieveService.getFile(ctx,
                            archInst.getFilePath(), archInst.getEntryName());
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
    }

    @Override
    public FetchForwardService getFetchForwardService() {
        return fetchForwardService;
    }

    private class CStoreSCUSetupTransformer implements SetupTransformer {

        private String localAET, remoteAET;

        public CStoreSCUSetupTransformer(String localAET, String remoteAET) {
            this.localAET = localAET;
            this.remoteAET = remoteAET;
        }

        @Override
        public void setup(Transformer transformer) {
            Date date = new Date();
            String currentDate = DateUtils.formatDA(null, date);
            String currentTime = DateUtils.formatTM(null, date);
            transformer.setParameter("date", currentDate);
            transformer.setParameter("time", currentTime);
            transformer.setParameter("calling", localAET);
            transformer.setParameter("called", remoteAET);
        }
    }

}
