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

import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.core.api.ConfigurableClassExtension;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.ExternalArchiveAEExtension;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4chee.archive.conf.defaults.test.DeepEquals;
import org.dcm4chee.archive.conf.defaults.DefaultArchiveConfigurationFactory;
import org.dcm4chee.archive.conf.defaults.DefaultDicomConfigInitializer;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class ArchiveDeviceTest {

    protected DicomConfiguration config;
    protected HL7Configuration hl7Config;

    @Before
    public void setUp() throws Exception {

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

        for (ConfigurableClassExtension extension : getDefaultExtensions())
            builder.registerExtensionForBaseExtension(extension.getClass(), extension.getBaseClass());



        CommonDicomConfigurationWithHL7 configWithHL7 = builder
                .cache(true)
                .build();
        config = configWithHL7;
        hl7Config = configWithHL7;

        cleanUp();
    }

    public static ArrayList<ConfigurableClassExtension> getDefaultExtensions() {
        ArrayList<ConfigurableClassExtension> extensions = new ArrayList<>();

        extensions.add(new ArchiveDeviceExtension());
        extensions.add(new StorageDeviceExtension());
        extensions.add(new NoneIOCMChangeRequestorExtension());
        extensions.add(new StorageDeviceExtension());
        extensions.add(new HL7DeviceExtension());
        extensions.add(new ImageReaderExtension());
        extensions.add(new ImageWriterExtension());
        extensions.add(new AuditRecordRepository());
        extensions.add(new AuditLogger());
        extensions.add(new ArchiveAEExtension());
        extensions.add(new ExternalArchiveAEExtension());
        extensions.add(new WebServiceAEExtension());
        extensions.add(new TCGroupConfigAEExtension());
        extensions.add(new ArchiveHL7ApplicationExtension());
        return extensions;
    }


    @Test
    public void test() throws Exception {

        DefaultArchiveConfigurationFactory.FactoryParams factoryParams = new DefaultArchiveConfigurationFactory.FactoryParams();
        factoryParams.generateUUIDsBasedOnName = true;

        DefaultDicomConfigInitializer defaultDicomConfigInitializer = new DefaultDicomConfigInitializer().persistDefaultConfig(config, hl7Config, factoryParams);
        Device arc = defaultDicomConfigInitializer.getArc();
        Device arrDevice = defaultDicomConfigInitializer.getArrDevice();

        config.sync();

        ApplicationEntity ae = config.findApplicationEntity("DCM4CHEE");

        Device arcLoaded = ae.getDevice();


        // register custom deep equals methods

        boolean res = DeepEquals.deepEquals(arc, arcLoaded);

        if (!res) {
            DeepEquals.printOutInequality();
        }

        assertTrue("Store/read failed for an attribute. See console output.", res);

        // Reconfiguration test

        DefaultArchiveConfigurationFactory deviceFactory = new DefaultArchiveConfigurationFactory(factoryParams);

        Device anotherArc = deviceFactory.createArchiveDevice("dcm4chee-arc", arrDevice);
        anotherArc.removeApplicationEntity("DCM4CHEE");

        ApplicationEntity anotherAe = deviceFactory.createAnotherAE("DCM4CHEE1",
                null, DefaultArchiveConfigurationFactory.PIX_MANAGER);
        anotherArc.addApplicationEntity(anotherAe);

        //anotherAe.getAEExtension(ArchiveAEExtension.class).reconfigure(from);

        anotherArc.reconfigure(arc);

        res = DeepEquals.deepEquals(anotherArc, arc);

        if (!res) {
            DeepEquals.printOutInequality();
        }

        assertTrue("Reconfigure", res);


    }

    protected void cleanUp() throws Exception {
        hl7Config.unregisterHL7Application(DefaultArchiveConfigurationFactory.PIX_MANAGER);
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
        for (String name : DefaultArchiveConfigurationFactory.OTHER_DEVICES)
            try {
                config.removeDevice(name);
            } catch (ConfigurationNotFoundException e) {
            }
    }

}
