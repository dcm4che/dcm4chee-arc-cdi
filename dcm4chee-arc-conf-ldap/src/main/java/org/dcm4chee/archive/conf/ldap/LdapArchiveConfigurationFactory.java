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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4chee.archive.conf.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;

import org.dcm4che3.conf.api.ApplicationEntityCache;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.api.hl7.HL7ApplicationCache;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.api.hl7.IHL7ApplicationCache;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.conf.ldap.audit.LdapAuditLoggerConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditRecordRepositoryConfiguration;
import org.dcm4che3.conf.ldap.hl7.LdapHL7Configuration;
import org.dcm4che3.conf.ldap.hl7.LdapHL7ConfigurationExtension;
import org.dcm4che3.conf.ldap.imageio.LdapImageReaderConfiguration;
import org.dcm4che3.conf.ldap.imageio.LdapImageWriterConfiguration;
import org.dcm4che3.util.StreamUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapArchiveConfigurationFactory {

    private static final String LDAP_PROPERTIES_PROPERTY =
            "org.dcm4chee.archive.ldap";

    private static  Properties ldapEnv() throws ConfigurationException {
        String url = System.getProperty(LDAP_PROPERTIES_PROPERTY);

        Properties p = new Properties();
        try ( InputStream in = StreamUtils.openFileOrURL(url); ) {
            p.load(in);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
        return p;
    }

    @Produces
    public static LdapDicomConfigurationExtension auditLoggerConfiguration() {
        return new LdapAuditLoggerConfiguration();
    }

    @Produces 
    public static LdapDicomConfigurationExtension auditRecordRepositoryConfiguration() {
        return new LdapAuditRecordRepositoryConfiguration();
    }

    @Produces
    public static LdapDicomConfigurationExtension imageReaderConfiguration() {
        return new LdapImageReaderConfiguration();
    }

    @Produces
    public static LdapDicomConfigurationExtension imageWriterConfiguration() {
        return new LdapImageWriterConfiguration();
    }

    @Produces
    public static LdapDicomConfigurationExtension hL7Configuration(
            Instance<LdapHL7ConfigurationExtension> hl7Exts) {
        LdapHL7Configuration hl7Conf = new LdapHL7Configuration();
        for (LdapHL7ConfigurationExtension hl7Ext : hl7Exts) {
            hl7Conf.addHL7ConfigurationExtension(hl7Ext);
        }
        return hl7Conf;
    }

    @Produces @ApplicationScoped
    public static DicomConfiguration createDicomConfiguration(
            Instance<LdapDicomConfigurationExtension> exts)
            throws ConfigurationException {
        LdapDicomConfiguration conf = new LdapDicomConfiguration(ldapEnv());
        for (LdapDicomConfigurationExtension ext : exts) {
            conf.addDicomConfigurationExtension(ext);
        }
        return conf;
    }

    public static  void disposeDicomConfiguration(@Disposes DicomConfiguration conf) {
        conf.close();
    }

    @Produces @ApplicationScoped
    public static  IApplicationEntityCache getApplicationEntityCache(DicomConfiguration conf) {
        return new ApplicationEntityCache(conf);
    }

    @Produces @ApplicationScoped
    public static  IHL7ApplicationCache getHL7ApplicationCache(DicomConfiguration conf) {
        return new HL7ApplicationCache(
                conf.getDicomConfigurationExtension(HL7Configuration.class));
    }

}
