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

package org.dcm4chee.archive.stow.client.impl;

import java.util.Collection;
import java.util.Date;

import javax.annotation.Resource;
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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXTransformer.SetupTransformer;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.DateUtils;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.Service;
import org.dcm4chee.archive.dto.ServiceQualifier;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.stow.client.StowClient;
import org.dcm4chee.archive.stow.client.StowClientService;
import org.dcm4chee.archive.stow.client.StowContext;
import org.dcm4chee.archive.stow.client.StowJMSMessage;
import org.dcm4chee.archive.stow.client.StowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
public class StowClientServiceImpl implements StowClientService {

    private static final Logger LOG = LoggerFactory.getLogger(StowClientServiceImpl.class);
    
    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName = "java:/queue/stowclient")
    private Queue stowQueue;

    @Inject
    @Any
    private Event<StowResponse> storeRememberEvent;

    @Override 
    public void scheduleStow(String transactionID, StowContext ctx
            ,Collection<ArchiveInstanceLocator> insts, int retries
            , int priority, long delay) {
        try {
            Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false,
                        Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session
                        .createProducer(stowQueue);
                ObjectMessage msg = session
                        .createObjectMessage(new StowJMSMessage(
                                insts, ctx));
                msg.setIntProperty("Priority", priority);
                msg.setIntProperty("Retries", retries);
                msg.setStringProperty("TransactionID", transactionID);
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
    public void coerceAttributes(Attributes attrs
            , final StowContext context)
            throws DicomServiceException {
        try {
            Templates tpl = context.getArchiveAEExtension()
                    .getAttributeCoercionTemplates(
                            attrs.getString(Tag.SOPClassUID), Dimse.C_STORE_RQ,
                            Role.SCU, context.getRemoteAE().getAETitle());
            
            if (tpl != null) {
                attrs.addAll(SAXTransformer.transform(attrs, tpl, false, false,
                        new SetupTransformer() {

                            @Override
                            public void setup(Transformer tr) {
                                Date date = new Date();
                                String currentDate = DateUtils.formatDA(
                                        null, date);
                                String currentTime = DateUtils.formatTM(
                                        null, date);
                                tr.setParameter("date", currentDate);
                                tr.setParameter("time", currentTime);
                                tr.setParameter("calling"
                                        , context.getRemoteAE()
                                        .getAETitle());
                                tr.setParameter("called"
                                        , context.getLocalAE()
                                        .getAETitle());
                            }
                        }));
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }

    }

    @Override
    public StowClient createStowRSClient(
            StowClientService service, StowContext ctx) {
        return new StowClient(service, ctx);
    }

    @Override
    public void notify(StowContext context, StowResponse rsp) {
        storeRememberEvent.select(new ServiceQualifier(
                context.getService())).fire(rsp);
    }

}
