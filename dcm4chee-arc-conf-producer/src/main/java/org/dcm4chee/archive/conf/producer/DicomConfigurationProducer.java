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

package org.dcm4chee.archive.conf.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.dcm4che3.conf.api.ApplicationEntityCache;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.api.hl7.HL7ApplicationCache;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.api.hl7.IHL7ApplicationCache;
import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.api.internal.ExtendedDicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.net.ExternalArchiveAEExtension;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.ArchiveHL7ApplicationExtension;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@ApplicationScoped
public class DicomConfigurationProducer {

    private static final Logger LOG =
            LoggerFactory.getLogger(DicomConfigurationProducer.class);
    
    @Inject
    private DicomConfigurationBuilder builder;

    @Produces
    @ApplicationScoped
    public DicomConfigurationManager getDicomConfiguration()
            throws ConfigurationException {
        builder.registerDeviceExtension(ArchiveDeviceExtension.class);
        builder.registerDeviceExtension(StorageDeviceExtension.class);
        builder.registerDeviceExtension(HL7DeviceExtension.class);
        builder.registerDeviceExtension(ImageReaderExtension.class);
        builder.registerDeviceExtension(ImageWriterExtension.class);
        builder.registerDeviceExtension(AuditRecordRepository.class);
        builder.registerDeviceExtension(AuditLogger.class);
        builder.registerAEExtension(ArchiveAEExtension.class);
        builder.registerAEExtension(WebServiceAEExtension.class);
        builder.registerAEExtension(ExternalArchiveAEExtension.class);
        builder.registerAEExtension(TCGroupConfigAEExtension.class);
        builder.registerHL7ApplicationExtension(ArchiveHL7ApplicationExtension.class);
        return builder.build();
    }


    public static void dispose(@Disposes ExtendedDicomConfiguration conf) {
        conf.close();
    }

    @Produces
    @ApplicationScoped
    public static IApplicationEntityCache getApplicationEntityCache(ExtendedDicomConfiguration conf) {
        return new ApplicationEntityCache(conf);
    }

    @Produces
    @ApplicationScoped
    public static IHL7ApplicationCache getHL7ApplicationCache(ExtendedDicomConfiguration conf) {
        return new HL7ApplicationCache(
                conf.getDicomConfigurationExtension(HL7Configuration.class));
    }
}
