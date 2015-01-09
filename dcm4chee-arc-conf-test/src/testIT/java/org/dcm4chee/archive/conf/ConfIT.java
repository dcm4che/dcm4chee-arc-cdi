/*
 * **** BEGIN LICENSE BLOCK *****
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
package org.dcm4chee.archive.conf;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.core.Configuration;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;

/**
 * @author Roman K
 */
@RunWith(Arquillian.class)
public class ConfIT extends ArchiveDeviceTest {

    @Inject
    Configuration dbConfigStorage;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");

        war.addClass(ConfIT.class);
        war.addClass(ArchiveDeviceTest.class);
        war.addClass(DeviceMocker.class);
        war.addClass(DeepEquals.class);
        war.addClass(CustomEquals.class);

        war.addAsManifestResource(new FileAsset(new File("src/test/resources/META-INF/MANIFEST.MF")), "MANIFEST.MF");
        JavaArchive[] archs = Maven.resolver()
                .loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies()
                .resolve().withoutTransitivity()
                .as(JavaArchive.class);

        for (JavaArchive a : archs) {
            war.addAsLibrary(a);
        }

        war.as(ZipExporter.class).exportTo(
                new File("test.war"), true);
        return war;
    }

    @Override
    public void setUp() throws Exception {
        DicomConfigurationBuilder builder = new DicomConfigurationBuilder();

        builder.registerCustomConfigurationStorage(dbConfigStorage);
        builder.cache();

        builder.registerDeviceExtension(ArchiveDeviceExtension.class);
        builder.registerDeviceExtension(StorageDeviceExtension.class);
        builder.registerDeviceExtension(HL7DeviceExtension.class);
        builder.registerDeviceExtension(ImageReaderExtension.class);
        builder.registerDeviceExtension(ImageWriterExtension.class);
        builder.registerDeviceExtension(AuditRecordRepository.class);
        builder.registerDeviceExtension(AuditLogger.class);
        builder.registerAEExtension(ArchiveAEExtension.class);
        builder.registerHL7ApplicationExtension(ArchiveHL7ApplicationExtension.class);


        CommonDicomConfigurationWithHL7 configWithHL7 = builder.build();
        config = configWithHL7;
        hl7Config = configWithHL7;

        cleanUp();
    }





    @Test
    public void performanceTest() throws Exception {


        Device arrDevice = createARRDevice("syslog", Connection.Protocol.SYSLOG_UDP, 514);
        config.persist(arrDevice);


        //persist
        System.out.println("persisting devices");
        for (int i = 0; i < 50; i++) {
            String name = "dcm4chee-arc" + i;
            if (i % 10 == 0) System.out.println(i);
            Device arc = createArchiveDevice(name, arrDevice);
            config.persist(arc);
        }

        System.out.println("loading devices");
        config.sync();
        for (int i = 0; i < 50; i++)
            config.findDevice("dcm4chee-arc" + i);

        System.out.println("removing devices");
        for (int i = 0; i < 50; i++) {
            String name = "dcm4chee-arc" + i;
            config.removeDevice(name);
        }

    }

    //@Test
    public void leakTest() throws Exception {

        System.out.println("look for leaks");
        Device arrDevice = createARRDevice("syslog", Connection.Protocol.SYSLOG_UDP, 514);
        config.persist(arrDevice);

        for (int i = 0; i < 200; i++) {
            Device arc = createArchiveDevice("leakTestDevice", arrDevice);
            config.persist(arc);
            config.sync();
            config.findDevice("leakTestDevice");
            config.removeDevice("leakTestDevice");
            config.sync();
        }

    }


}
