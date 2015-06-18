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
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.echo.scu.impl.CEchoSCUServiceImpl;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans11.BeansDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Tests {@link CEchoSCUService}.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@RunWith(Arquillian.class)
public class CEchoSCUServiceTest {

    private static final String LOCAL_AET = "cecho_test_local";
    private static final String REMOTE_AET = "cecho_test_rmt";
    private static final String REMOTE_AET2 = "cecho_test_rmt2";

    private static int portForRemoteEchoSCP;
    static {
        try {
            portForRemoteEchoSCP = getAvailablePort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deployment
    public static JavaArchive createDeployment() {
        BeansDescriptor beansXml = Descriptors.create(BeansDescriptor.class);

        JavaArchive addAsManifestResource = ShrinkWrap.create(JavaArchive.class)
                .addClass(CEchoSCUServiceImpl.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, beansXml.getDescriptorName());

        return addAsManifestResource;
    }

    @Produces
    public static Device localDevice() throws Exception {
        // fake local device

        Device localDevice = new Device("CECHO-TEST");
        Connection localConnection = new Connection();
        localDevice.addConnection(localConnection);
        ApplicationEntity localAE = new ApplicationEntity(LOCAL_AET);
        localAE.addConnection(localConnection);

        localDevice.addApplicationEntity(localAE);

        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        localDevice.setScheduledExecutor(scheduledExecutorService);
        localDevice.setExecutor(executorService);

        localDevice.bindConnections();

        return localDevice;
    }

    @Produces
    public static IApplicationEntityCache mockApplicationEntityCache() throws Exception {
        // we setup a mocked AE cache that contains a remote AE

        Device remoteDevice = new Device("CECHO-TEST-REMOTE");
        Connection remoteConnection = new Connection();
        remoteConnection.setHostname("localhost");
        remoteConnection.setPort(portForRemoteEchoSCP);
        remoteDevice.addConnection(remoteConnection);
        ApplicationEntity remoteAE = new ApplicationEntity(REMOTE_AET);
        remoteAE.addConnection(remoteConnection);

        remoteDevice.addApplicationEntity(remoteAE);

        IApplicationEntityCache aeCacheMock = Mockito.mock(IApplicationEntityCache.class);
        Mockito.when(aeCacheMock.findApplicationEntity(REMOTE_AET)).thenReturn(remoteAE);

        return aeCacheMock;
    }

    @Inject
    private CEchoSCUService service;

    @Test
    public void testCEchoSuccess_KnownRemoteAE() throws Exception {

        // test cecho to a known (configured in application entity cache) remote AE

        long time;

        // start up a remote ECHO-SCP that we can test against
        CEchoSCPTool scpTool = new CEchoSCPTool(REMOTE_AET, "localhost", portForRemoteEchoSCP);
        scpTool.start();
        try {

            time = service.cecho(LOCAL_AET, REMOTE_AET);

        } finally {
            scpTool.stop();
        }

        Assert.assertTrue(time >= 0);
    }

    @Test
    public void testCEchoSuccess_NewRemoteAE() throws Exception {

        // test cecho to a new (not yet configured) remote AE

        long time;

        // start up a remote ECHO-SCP that we can test against
        CEchoSCPTool scpTool = new CEchoSCPTool(REMOTE_AET2, "localhost", portForRemoteEchoSCP);
        scpTool.start();
        try {

            time = service.cecho(LOCAL_AET, REMOTE_AET2, new Connection("remoteConnection", "localhost", portForRemoteEchoSCP));

        } finally {
            scpTool.stop();
        }

        Assert.assertTrue(time >= 0);
    }

    @Test
    public void testCEcho_ConnectionRefused() throws Exception {
        // we do NOT start up the remote SCP now and should therefore get an exception (connection refused)

        try {
            service.cecho(LOCAL_AET, REMOTE_AET);
        } catch (DicomServiceException e)
        {
            // we expect this
            return;
        }

        Assert.fail();
    }

    private static int getAvailablePort() throws IOException {
        // open a temporary server socket, just to get a free port
        try (ServerSocket serverSocket = new ServerSocket(0))
        {
            return serverSocket.getLocalPort();
        }
    }
}
