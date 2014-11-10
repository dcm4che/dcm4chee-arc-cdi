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

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.dcm4che3.conf.api.AttributeCoercions;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditLoggerConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditRecordRepositoryConfiguration;
import org.dcm4che3.conf.ldap.hl7.LdapHL7Configuration;
import org.dcm4che3.conf.ldap.imageio.LdapImageReaderConfiguration;
import org.dcm4che3.conf.ldap.imageio.LdapImageWriterConfiguration;
import org.dcm4che3.conf.prefs.PreferencesDicomConfiguration;
import org.dcm4che3.conf.prefs.audit.PreferencesAuditLoggerConfiguration;
import org.dcm4che3.conf.prefs.audit.PreferencesAuditRecordRepositoryConfiguration;
import org.dcm4che3.conf.prefs.hl7.PreferencesHL7Configuration;
import org.dcm4che3.conf.prefs.imageio.PreferencesImageReaderConfiguration;
import org.dcm4che3.conf.prefs.imageio.PreferencesImageWriterConfiguration;
import org.dcm4che3.imageio.codec.CompressionRules;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.SSLManagerFactory;
import org.dcm4che3.util.ResourceLocator;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.conf.DeepEquals.CustomDeepEquals;
import org.dcm4chee.archive.conf.ldap.LdapArchiveConfiguration;
import org.dcm4chee.archive.conf.ldap.LdapArchiveHL7Configuration;
import org.dcm4chee.archive.conf.prefs.PreferencesArchiveConfiguration;
import org.dcm4chee.archive.conf.prefs.PreferencesArchiveHL7Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArchiveDeviceTest extends DeviceMocker {

    @Before
    public void setUp() throws Exception {
        keystore = SSLManagerFactory.loadKeyStore("JKS", 
                ResourceLocator.resourceURL("cacerts.jks"), "secret");
        
        config = System.getProperty("ldap") == null
                ? newPreferencesArchiveConfiguration()
                : newLdapArchiveConfiguration();
        cleanUp();
    }

    private DicomConfiguration newLdapArchiveConfiguration()
            throws ConfigurationException {
        LdapDicomConfiguration config = new LdapDicomConfiguration();
        LdapHL7Configuration hl7Config = new LdapHL7Configuration();
        hl7Config.addHL7ConfigurationExtension(
                new LdapArchiveHL7Configuration());
        config.addDicomConfigurationExtension(hl7Config);
        config.addDicomConfigurationExtension(
                new LdapArchiveConfiguration());
        config.addDicomConfigurationExtension(
                new LdapAuditLoggerConfiguration());
        config.addDicomConfigurationExtension(
                new LdapAuditRecordRepositoryConfiguration());
        config.addDicomConfigurationExtension(
                new LdapImageReaderConfiguration());
        config.addDicomConfigurationExtension(
                new LdapImageWriterConfiguration());
        this.hl7Config = hl7Config;
        return config;
    }

    private DicomConfiguration newPreferencesArchiveConfiguration() {
        PreferencesDicomConfiguration config = new PreferencesDicomConfiguration();
        PreferencesHL7Configuration hl7Config = new PreferencesHL7Configuration();
        hl7Config.addHL7ConfigurationExtension(
                new PreferencesArchiveHL7Configuration());
        config.addDicomConfigurationExtension(hl7Config);
        config.addDicomConfigurationExtension(
                new PreferencesArchiveConfiguration());
        config.addDicomConfigurationExtension(
                new PreferencesAuditLoggerConfiguration());
        config.addDicomConfigurationExtension(
                new PreferencesAuditRecordRepositoryConfiguration());
        config.addDicomConfigurationExtension(
                new PreferencesImageReaderConfiguration());
        config.addDicomConfigurationExtension(
                new PreferencesImageWriterConfiguration());
        this.hl7Config = hl7Config;
        return config;
    }

    @After
    public void tearDown() throws Exception {
        if (System.getProperty("keep") == null)
            cleanUp();
        config.close();
    }


    @Test
    public void test() throws Exception {
        for (int i = 0; i < OTHER_AES.length; i++) {
            String aet = OTHER_AES[i];
            config.registerAETitle(aet);
            config.persist(createDevice(OTHER_DEVICES[i], OTHER_ISSUER[i], OTHER_INST_CODES[i],
                    aet, "localhost", OTHER_PORTS[i<<1], OTHER_PORTS[(i<<1)+1]));
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

        Device arc = createArchiveDevice("dcm4chee-arc", arrDevice );
        config.persist(arc);
        ApplicationEntity ae = config.findApplicationEntity("DCM4CHEE");

        Device arcLoaded = ae.getDevice();
        
        
        if (config instanceof PreferencesDicomConfiguration)
            export(System.getProperty("export"));
        
        // register custom deep equals methods
        DeepEquals.customDeepEquals = new HashMap<Class<?>, CustomDeepEquals>();
        DeepEquals.customDeepEquals.put(CompressionRules.class, new CustomEquals.CompressionRulesDeepEquals());
        DeepEquals.customDeepEquals.put(AttributeCoercions.class, new CustomEquals.AttributeCoercionsDeepEquals());
        
        boolean res = DeepEquals.deepEquals(arc, arcLoaded); 

        if (!res) {
        	System.out.println(DeepEquals.lastClass);
        	System.out.println(DeepEquals.lastDualKey);
        }
        
        assertTrue("Store/read failed for an attribute. See console output.", res);
        
        // Reconfiguration test
        Device anotherArc = createArchiveDevice("dcm4chee-arc", arrDevice );
        anotherArc.removeApplicationEntity("DCM4CHEE");
        
        ApplicationEntity anotherAe = createAnotherAE("DCM4CHEE",
                IMAGE_TSUIDS, VIDEO_TSUIDS, OTHER_TSUIDS, null, PIX_MANAGER);
        anotherArc.addApplicationEntity(anotherAe);

        //anotherAe.getAEExtension(ArchiveAEExtension.class).reconfigure(from);
        
        arcLoaded.reconfigure(anotherArc);
        
        res = DeepEquals.deepEquals(anotherArc, arcLoaded); 
        
        assertTrue("Reconfigure",res);        
        
    }

    private void cleanUp() throws Exception {
        config.unregisterAETitle("DCM4CHEE");
        config.unregisterAETitle("DCM4CHEE_ADMIN");
        config.unregisterAETitle("DCM4CHEE_TRASH");
        for (String aet : OTHER_AES)
            config.unregisterAETitle(aet);
        hl7Config.unregisterHL7Application(PIX_MANAGER);
        try {
            config.removeDevice("dcm4chee-arc");
        } catch (ConfigurationNotFoundException e) {}
        try {
            config.removeDevice("syslog");
        } catch (ConfigurationNotFoundException e) {}
        try {
            config.removeDevice("hl7rcv");
        } catch (ConfigurationNotFoundException e) {}
        for (String name : OTHER_DEVICES)
            try {
                config.removeDevice(name);
            }  catch (ConfigurationNotFoundException e) {}
    }

    private void export(String name) throws Exception {
        if (name == null)
            return;

        OutputStream os = new FileOutputStream(name);
        try {
            ((PreferencesDicomConfiguration) config)
                    .getDicomConfigurationRoot().exportSubtree(os);
        } finally {
            SafeClose.close(os);
        }
    }


}
