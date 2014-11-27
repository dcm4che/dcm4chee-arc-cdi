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
 *  Portions created by the Initial Developer are Copyright (C) 2014
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
package org.dcm4che3.conf.ldap;

import org.junit.Assert;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7ApplicationExtension;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.ArchiveHL7ApplicationExtension;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

/**
 * @author: Roman K
 */
@RunWith(JUnit4.class)
public class LdapUnitTests {

    public LdapConfigurationStorage getLdapConfig() throws ConfigurationException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.provider.url", "ldap://localhost:389/dc=example,dc=com");
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.ldap.attributes.binary", "dicomVendorData");
        env.put("java.naming.security.principal", "cn=Directory Manager ");
        env.put("java.naming.security.credentials", "1");


        Class[] deviceExtensionClasses = {
                ArchiveDeviceExtension.class,
                HL7DeviceExtension.class,
                ImageReaderExtension.class,
                ImageWriterExtension.class,
                AuditRecordRepository.class,
                AuditLogger.class};

        Class[] aeExtensionClasses = {
                ArchiveAEExtension.class
        };

        Class[] hl7AppExtensionClasses = {
                ArchiveHL7ApplicationExtension.class
        };

        List deviceExtensionClassList = Arrays.<Class<DeviceExtension>>asList(deviceExtensionClasses);
        List aeExtensionClassList = Arrays.<Class<AEExtension>>asList(aeExtensionClasses);
        List hl7ApplicationExtensionClassList = Arrays.<Class<HL7ApplicationExtension>>asList(hl7AppExtensionClasses);

        List<Class<?>> allExtensionClasses = new ArrayList<>();
        allExtensionClasses.addAll(deviceExtensionClassList);
        allExtensionClasses.addAll(aeExtensionClassList);
        allExtensionClasses.addAll(hl7ApplicationExtensionClassList);

        return new LdapConfigurationStorage(env, allExtensionClasses);

    }

    @Test
    public void testRefToDn() throws ConfigurationException {

        String baseDn = "";

        Map<String, String> m = new HashMap<>();
        m.put("/dicomConfigurationRoot/dicomDevicesRoot[@name='dcm4chee-arc']/deviceExtensions/ArchiveDeviceExtension",
                "dicomDeviceName=dcm4chee-arc,cn=Devices,cn=DICOM Configuration,dc=example,dc=com");
        m.put("/dicomConfigurationRoot/dicomDevicesRoot/*[dicomDeviceName='dcm4chee-arc']/dicomConnection[cn='dicom']",
                "cn=dicom,dicomDeviceName=dcm4chee-arc,cn=Devices,cn=DICOM Configuration,dc=example,dc=com");


        LdapConfigurationStorage ldapConfig = getLdapConfig();
        for (Map.Entry<String, String> entry : m.entrySet()) {

            Assert.assertEquals(entry.getValue(), LdapConfigUtils.refToLdapDN(entry.getKey(), ldapConfig));

        }

        // /dicomConfigurationRoot/dicomDevicesRoot[@name='dcm4chee-arc']/deviceExtensions/ArchiveDeviceExtension
        // /dicomConfigurationRoot/dicomDevicesRoot[@name='dcm4chee-arc']/dicomNetworkAE[@name='DCM4CHEE']/aeExtensions/ArchiveAEExtension
        // /dicomConfigurationRoot/dicomDevicesRoot[@name='dcm4chee-arc']/deviceExtensions/HL7DeviceExtension/hl7Apps[@name='*']/hl7AppExtensions/ArchiveHL7ApplicationExtension

    }
}
