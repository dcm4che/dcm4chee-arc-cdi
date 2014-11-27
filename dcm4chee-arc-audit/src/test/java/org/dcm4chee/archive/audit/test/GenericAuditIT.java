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

import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
public class GenericAuditIT {
    
    protected static String ARR_HOST =  "10.231.163.243";
    protected static int ARR_PORT_UNSECURE =  514;
    protected static int ARR_PORT_SECURE =  6514;
    protected static String PROXY = "localhost:3128";
    protected boolean TLS = false;
    protected String KEYSTORE = "/keystore_ihe_europe.jks";
    protected String KEYSTORE_PASSWORD = "changeit";
    
    protected Device getARRDevice() {
        
        // AUDIT RECORD REPOSITORY Device
        Device arr = new Device("testARR");
        arr.addApplicationEntity(new ApplicationEntity("ARR"));
        arr.setInstalled(true);
        Connection c;

        if (TLS) {
            c = new Connection("testARR", ARR_HOST, ARR_PORT_SECURE);
            c.setProtocol(Protocol.SYSLOG_TLS);
            c.setTlsCipherSuites("TLS_RSA_WITH_AES_128_CBC_SHA");
            c.setHttpProxy(PROXY);
        }
        else {
            c = new Connection("testARR", ARR_HOST, ARR_PORT_UNSECURE);
            c.setProtocol(Protocol.SYSLOG_UDP);
        }
        c.setConnectionInstalled(true);
        AuditRecordRepository arrExt = new AuditRecordRepository();
        arrExt.getConnections().add(c);
        arr.addDeviceExtension(arrExt);
        arr.addConnection(c);
        
        return arr;
    }
    
    protected Device getLocalTestDevice() throws URISyntaxException{
        
        // TEST Device sending audits
        Device test = new Device("testSendingDevice");
        test.addApplicationEntity(new ApplicationEntity("TEST"));
        test.setInstalled(true);

        Connection clientconn = new Connection("testARRClient", "localhost");
        if (TLS) {
            test.setTrustStoreURL(Paths.get(getClass().getResource(KEYSTORE).toURI()).toString());
            test.setTrustStoreType("JKS");
            test.setTrustStorePin(KEYSTORE_PASSWORD);
            test.setKeyStoreURL(Paths.get(getClass().getResource(KEYSTORE).toURI()).toString());
            test.setKeyStorePin(KEYSTORE_PASSWORD);
            test.setKeyStoreType("JKS");
            clientconn.setProtocol(Protocol.SYSLOG_TLS);
            clientconn.setTlsCipherSuites("TLS_RSA_WITH_AES_128_CBC_SHA");
        }
        else {
            clientconn.setProtocol(Protocol.SYSLOG_UDP);
        }
        clientconn.setConnectionInstalled(true);
        test.addConnection(clientconn);
        
        return test;
    }
    
    protected void sendAuditMessage(AuditMessage msg, AuditLogger logger) {

        if (msg == null)
            return;

        if (logger == null || !logger.isInstalled())
            return;

        try {

            logger.write(logger.timeStamp(), msg);

        } catch (Exception e) {
            
            System.out.println("Failed to write audit log message:" + e.getMessage());
        }
    }

    
}
