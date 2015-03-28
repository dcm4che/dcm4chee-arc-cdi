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

package org.dcm4chee.archive.conf;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.dcm4che3.conf.api.AttributeCoercions;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.CompressionRules;
import org.dcm4che3.net.*;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4che3.util.ResourceLocator;
import org.dcm4chee.archive.conf.DeepEquals.CustomDeepEquals;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.junit.Before;
import org.junit.Test;

public class ArchiveDeviceTest extends DeviceMocker {

    @Before
    public void setUp() throws Exception {
        keystore = SSLManagerFactory.loadKeyStore("JKS",
                ResourceLocator.resourceURL("cacerts.jks"), "secret");

        DicomConfigurationBuilder builder;
        if (System.getProperty("ldap") != null) {
            Properties env = new Properties();
            try (InputStream inStream = Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream("ldap.properties")) {
                env.load(inStream);
            }
            builder = DicomConfigurationBuilder.newLdapConfigurationBuilder(env);
        } else {
            builder = DicomConfigurationBuilder.newJsonConfigurationBuilder(
                    "../dcm4chee-arc-conf/src/main/config/configuration/dcm4chee-arc/sample-config.json");
        }
        builder.registerDeviceExtension(ArchiveDeviceExtension.class);
        builder.registerDeviceExtension(StorageDeviceExtension.class);
        builder.registerDeviceExtension(HL7DeviceExtension.class);
        builder.registerDeviceExtension(ImageReaderExtension.class);
        builder.registerDeviceExtension(ImageWriterExtension.class);
        builder.registerDeviceExtension(AuditRecordRepository.class);
        builder.registerDeviceExtension(AuditLogger.class);
        builder.registerAEExtension(ArchiveAEExtension.class);
        builder.registerHL7ApplicationExtension(ArchiveHL7ApplicationExtension.class);

        CommonDicomConfigurationWithHL7 configWithHL7 = builder
                .cache(false)
                .persistDefaults(false)
                .build();
        config = configWithHL7;
        hl7Config = configWithHL7;

        cleanUp();
    }



    
    @Test
    public void test() throws Exception {
        for (int i = 0; i < OTHER_AES.length; i++) {
            String aet = OTHER_AES[i];
            config.registerAETitle(aet);
            config.persist(createDevice(OTHER_DEVICES[i], OTHER_ISSUER[i], OTHER_INST_CODES[i],
                    aet, "localhost", OTHER_PORTS[i << 1], OTHER_PORTS[(i << 1) + 1]));
        }
        hl7Config.registerHL7Application(PIX_MANAGER);
        for (int i = OTHER_AES.length; i < OTHER_DEVICES.length; i++)
            config.persist(createDevice(OTHER_DEVICES[i]));
        config.persist(createHL7Device("hl7rcv", SITE_A, INST_A, PIX_MANAGER,
                "localhost", 2576, 12576));
        Device arrDevice = createARRDevice("syslog", Protocol.SYSLOG_UDP, 514);
        config.persist(arrDevice);
        config.registerAETitle("DCM4CHEE");
        config.registerAETitle("DCM4CHEE_ADMIN");
        config.registerAETitle("DCM4CHEE_TRASH");

        Device arc = createArchiveDevice("dcm4chee-arc", arrDevice);

        
//        StorageOptions storageOptions = new StorageOptions();
//        storageOptions.setDigitalSignatureSupport(StorageOptions.DigitalSignatureSupport.LEVEL_3);
//        TransferCapability dcm4CHEE = arc.getApplicationEntity("DCM4CHEE").getTransferCapabilities().iterator().next();
//        dcm4CHEE.setStorageOptions(storageOptions);

        config.persist(arc);

        config.sync();

        ApplicationEntity ae = config.findApplicationEntity("DCM4CHEE");

        Device arcLoaded = ae.getDevice();


        // register custom deep equals methods
        DeepEquals.customDeepEquals = new HashMap<Class<?>, CustomDeepEquals>();
        DeepEquals.customDeepEquals.put(CompressionRules.class, new CustomEquals.CompressionRulesDeepEquals());
        DeepEquals.customDeepEquals.put(AttributeCoercions.class, new CustomEquals.AttributeCoercionsDeepEquals());

        boolean res = DeepEquals.deepEquals(arc, arcLoaded);

        if (!res) {
            System.out.println(DeepEquals.lastClass);
            System.out.println(DeepEquals.lastDualKey);

            // trace
            System.out.println("'Path' in the object tree where inequality is located:");
            for (DeepEquals.DualKey dualKey : DeepEquals.lastDualKey.getTrace()) {
                System.out.println(dualKey.getFieldName());
            }
        }

        assertTrue("Store/read failed for an attribute. See console output.", res);

        // Reconfiguration test
        Device anotherArc = createArchiveDevice("dcm4chee-arc", arrDevice);
        anotherArc.removeApplicationEntity("DCM4CHEE");

        ApplicationEntity anotherAe = createAnotherAE("DCM4CHEE",
                IMAGE_TSUIDS, VIDEO_TSUIDS, OTHER_TSUIDS, null, PIX_MANAGER);
        anotherArc.addApplicationEntity(anotherAe);

        //anotherAe.getAEExtension(ArchiveAEExtension.class).reconfigure(from);

        arcLoaded.reconfigure(anotherArc);

        res = DeepEquals.deepEquals(anotherArc, arcLoaded);

        assertTrue("Reconfigure", res);

    }

    protected void cleanUp() throws Exception {
        config.unregisterAETitle("DCM4CHEE");
        config.unregisterAETitle("DCM4CHEE_ADMIN");
        config.unregisterAETitle("DCM4CHEE_TRASH");
        for (String aet : OTHER_AES)
            config.unregisterAETitle(aet);
        hl7Config.unregisterHL7Application(PIX_MANAGER);
        try {
            config.removeDevice("dcm4chee-arc");
        } catch (ConfigurationNotFoundException e) {
        }
        try {
            config.removeDevice("syslog");
        } catch (ConfigurationNotFoundException e) {
        }
        try {
            config.removeDevice("hl7rcv");
        } catch (ConfigurationNotFoundException e) {
        }
        for (String name : OTHER_DEVICES)
            try {
                config.removeDevice(name);
            } catch (ConfigurationNotFoundException e) {
            }
    }

}
