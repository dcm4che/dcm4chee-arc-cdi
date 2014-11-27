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

package org.dcm4chee.archive.conf.prefs;

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
import org.dcm4che3.conf.prefs.PreferencesDicomConfiguration;
import org.dcm4che3.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che3.conf.prefs.audit.PreferencesAuditLoggerConfiguration;
import org.dcm4che3.conf.prefs.audit.PreferencesAuditRecordRepositoryConfiguration;
import org.dcm4che3.conf.prefs.generic.PreferencesGenericConfigExtension;
import org.dcm4che3.conf.prefs.hl7.PreferencesHL7Configuration;
import org.dcm4che3.conf.prefs.hl7.PreferencesHL7ConfigurationExtension;
import org.dcm4che3.conf.prefs.imageio.PreferencesImageReaderConfiguration;
import org.dcm4che3.conf.prefs.imageio.PreferencesImageWriterConfiguration;
import org.dcm4chee.storage.conf.StorageDeviceExtension;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesArchiveConfigurationFactory {

    @Produces
    public static PreferencesDicomConfigurationExtension auditLoggerConfiguration() {
        return new PreferencesAuditLoggerConfiguration();
    }

    @Produces 
    public static PreferencesDicomConfigurationExtension auditRecordRepositoryConfiguration() {
        return new PreferencesAuditRecordRepositoryConfiguration();
    }

    @Produces
    public static PreferencesDicomConfigurationExtension imageReaderConfiguration() {
        return new PreferencesImageReaderConfiguration();
    }

    @Produces
    public static PreferencesDicomConfigurationExtension imageWriterConfiguration() {
        return new PreferencesImageWriterConfiguration();
    }

    @Produces
    public static PreferencesDicomConfigurationExtension storageConfiguration()
            throws ConfigurationException {
        return PreferencesGenericConfigExtension.create(StorageDeviceExtension.class);
    }

    @Produces
    public static PreferencesDicomConfigurationExtension hL7Configuration(
            Instance<PreferencesHL7ConfigurationExtension> hl7Exts) {
        PreferencesHL7Configuration hl7Conf = new PreferencesHL7Configuration();
        for (PreferencesHL7ConfigurationExtension hl7Ext : hl7Exts) {
            hl7Conf.addHL7ConfigurationExtension(hl7Ext);
        }
        return hl7Conf;
    }

    @Produces @ApplicationScoped
    public static DicomConfiguration createDicomConfiguration(
            Instance<PreferencesDicomConfigurationExtension> exts)
            throws ConfigurationException {
        PreferencesDicomConfiguration conf = new PreferencesDicomConfiguration();
        for (PreferencesDicomConfigurationExtension ext : exts) {
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
