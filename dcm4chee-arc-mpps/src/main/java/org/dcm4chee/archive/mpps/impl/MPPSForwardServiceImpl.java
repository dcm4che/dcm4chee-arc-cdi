/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4chee.archive.mpps.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.mpps.MPPSForwardService;
import org.dcm4chee.archive.mpps.event.MPPSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Default implementation of {@link org.dcm4chee.archive.mpps.MPPSForwardService}.
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@ApplicationScoped
public class MPPSForwardServiceImpl implements MPPSForwardService {

    @Inject
    private Device device;

    @Resource(mappedName="java:/JmsXA")
    private ConnectionFactory connFactory;

    @Resource(mappedName="java:/queue/mppsscu")
    private Queue mppsSCUQueue;

    @Override
    public void scheduleForwardMPPS(MPPSEvent event) {
        ApplicationEntity ae = device.getApplicationEntityNotNull(event.getContext().getReceivingAET());

        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcAE == null)
            return;

        String iuid = event.getContext().getMppsSopInstanceUID();
        Attributes attrs = event.getAttributes();
        for (ApplicationEntity remoteAE : arcAE.getForwardMPPSDestinations()) {
            scheduleForwardMPPS(event.getContext().getDimse(), ae.getAETitle(), remoteAE.getAETitle(), iuid, attrs);
        }
    }

    private void scheduleForwardMPPS(Dimse dimse, String localAET, String remoteAET,
                                     String iuid, Attributes attrs) {
        try {
            Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(mppsSCUQueue);
                ObjectMessage msg = session.createObjectMessage(attrs);
                msg.setStringProperty("CommandField", dimse.name());
                msg.setStringProperty("SOPInstancesUID", iuid);
                msg.setStringProperty("LocalAET", localAET);
                msg.setStringProperty("RemoteAET", remoteAET);
                producer.send(msg);
            } finally {
                conn.close();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
