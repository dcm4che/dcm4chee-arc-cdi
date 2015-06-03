//
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

package org.dcm4chee.archive.echo.scu;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple CEcho SCP used for tests.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class CEchoSCPTool {

    private static final Logger log = LoggerFactory.getLogger(CEchoSCUServiceTest.class);

    private final String aet;
    private final String hostname;
    private final int port;

    private final Device device = new Device("TEST-CEchoSCP");

    public CEchoSCPTool(String aet, String hostname, int port) {
        this.aet = aet;
        this.hostname = hostname;
        this.port = port;
    }

    public void start() throws IOException, GeneralSecurityException {

        log.info("Starting Test Echo-SCP: {}@{}:{}", aet, hostname, port);

        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();

        serviceRegistry.addDicomService(new BasicCEchoSCP());

        device.setDimseRQHandler(serviceRegistry);

        Connection connection = new Connection();
        connection.setHostname(hostname);
        connection.setPort(port);
        device.addConnection(connection);
        ApplicationEntity ae = new ApplicationEntity(aet);
        device.addApplicationEntity(ae);
        ae.addConnection(connection);

        // accept all
        ae.addTransferCapability(new TransferCapability(null, "*", TransferCapability.Role.SCP, "*"));

        ae.setAssociationAcceptor(true);

        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        device.setScheduledExecutor(scheduledExecutorService);
        device.setExecutor(executorService);

        device.bindConnections();

    }

    public void stop() {
        device.unbindConnections();
    }
}
