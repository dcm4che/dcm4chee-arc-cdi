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

package org.dcm4chee.archive.audit.test;

import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4chee.archive.audit.StartStopAudit;
import org.dcm4chee.archive.dto.Source;
import org.dcm4chee.archive.impl.LocalSource;
import org.junit.Test;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
public class StartStopTest {
    
    private static String ARR_HOST =  "10.231.161.39";
    private static int ARR_PORT =  4000;
    
    
    @Test
    public void testStartStopSampleSend () throws Exception {

        Device arr = new Device("testARR");
        arr.addApplicationEntity(new ApplicationEntity("ARR"));
        arr.setInstalled(true);
        Connection c = new Connection("testARR", ARR_HOST, ARR_PORT);
        c.setProtocol(Protocol.SYSLOG_UDP);
        c.setInstalled(true);
        AuditRecordRepository arrExt = new AuditRecordRepository();
        arrExt.getConnections().add(c);
        arr.addDeviceExtension(arrExt);
        arr.addConnection(c);
        
        Device test = new Device("testSendingDevice");
        test.addApplicationEntity(new ApplicationEntity("TEST"));
        test.setInstalled(true);
        AuditLogger auditLogger = new AuditLogger();
        auditLogger.setAuditRecordRepositoryDevice(arr);
        Connection clientconn = new Connection("testARRClient", "localhost");
        clientconn.setProtocol(Protocol.SYSLOG_UDP);
        clientconn.setInstalled(true);
        auditLogger.getConnections().add(clientconn);
        test.addDeviceExtension(auditLogger);
        test.addConnection(clientconn);
        
        
        Source source = new LocalSource();
        AuditMessage auditMessage = new StartStopAudit(true, auditLogger, source);
        sendAuditMessage(auditMessage, auditLogger);
    }

    private void sendAuditMessage(AuditMessage msg, AuditLogger logger) {

        if (msg == null)
            return;

        if (logger == null || !logger.isInstalled())
            return;

        try {
            
                System.out.println("Send Audit Log message to [" + 
                        logger.getRemoteActiveConnection().getHostname() +
                        ":" +
                        logger.getRemoteActiveConnection().getPort() +"]:" +
                        AuditMessages.toXML(msg));

            logger.write(logger.timeStamp(), msg);

        } catch (Exception e) {
            
            System.out.println("Failed to write audit log message:" + e.getMessage());
        }
    }
    
}
