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
package org.dcm4chee.archive.conf.producer;

/**
 * @author Roman K
 */

import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.ExternalArchiveAEExtension;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4chee.storage.conf.StorageDeviceExtension;

import javax.enterprise.inject.Produces;

/**
 * Library modules do not support CDI so we need these explicit producers for device extensions that come from the library.
 */
public class LibraryConfigExtensionsProducer {

    @Produces
    AuditLogger getAuditLogger() {
        return new AuditLogger();
    }

    @Produces
    AuditRecordRepository getAuditRecordRepository() {
        return new AuditRecordRepository();
    }

    @Produces
    ImageWriterExtension getImageWriterExtension() {
        return new ImageWriterExtension();
    }

    @Produces
    ImageReaderExtension getImageReaderExtension() {
        return new ImageReaderExtension();
    }

    @Produces
    HL7DeviceExtension getHL7DeviceExtension() {
        return new HL7DeviceExtension();
    }

    @Produces
    WebServiceAEExtension getWebServiceAEExtension() {
        return new WebServiceAEExtension();
    }

    @Produces
    ExternalArchiveAEExtension getExternalArchiveAEExtension() {
        return new ExternalArchiveAEExtension();
    }

    @Produces
    TCGroupConfigAEExtension getTcGroupConfigAEExtension() {return new TCGroupConfigAEExtension();}
}
