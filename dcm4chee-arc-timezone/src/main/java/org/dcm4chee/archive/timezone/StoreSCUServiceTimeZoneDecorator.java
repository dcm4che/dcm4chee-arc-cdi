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

package org.dcm4chee.archive.timezone;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.DateUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.TimeZoneOption;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.decorators.DelegatingCStoreSCUService;
import org.dcm4chee.conf.decorators.DynamicDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@DynamicDecorator
public class StoreSCUServiceTimeZoneDecorator extends DelegatingCStoreSCUService {

    static Logger LOG = LoggerFactory
            .getLogger(StoreSCUServiceTimeZoneDecorator.class);

    @Override
    public void coerceFileBeforeMerge(ArchiveInstanceLocator inst, Attributes attrs,
                                      CStoreSCUContext context) throws DicomServiceException {
        getNextDecorator().coerceFileBeforeMerge(inst, attrs, context);
        ArchiveAEExtension arcAE = context.getArchiveAEExtension();
        if (arcAE.getApplicationEntity().getDevice().getDeviceExtension(ArchiveDeviceExtension.class)
                .getTimeZoneSupport() == TimeZoneOption.STORE_QUERY_RETRIEVE) {
            TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice()
                    .getTimeZoneOfDevice();

            if (archiveTimeZone == null) // no Timezone support configured
                return;

            String fileTimeZoneID = ((ArchiveInstanceLocator) inst)
                    .getFileTimeZoneID();
            if (fileTimeZoneID == null) {
                LOG.debug("Missing Timezone information for persisted object");
                return;
            }

            try {
                TimeZone timeZone = TimeZone.getTimeZone(fileTimeZoneID);
                if (!timeZone.hasSameRules(archiveTimeZone)) {
                    LOG.debug(
                            "Coerce persisted object attributes from Timezone {} to Archive Timezone {}",
                            timeZone.getID(), archiveTimeZone.getID());
                    attrs.setDefaultTimeZone(timeZone);
                    attrs.setTimezone(archiveTimeZone);
                }

            } catch (Exception e) {
                throw new DicomServiceException(Status.UnableToProcess, e);
            }
        }
    }

    @Override
    public void coerceAttributes(Attributes attrs, CStoreSCUContext context)
            throws DicomServiceException {
        getNextDecorator().coerceAttributes(attrs, context);

        ArchiveAEExtension arcAE = context.getArchiveAEExtension();
        if (arcAE.getApplicationEntity().getDevice().getDeviceExtension(ArchiveDeviceExtension.class)
                .getTimeZoneSupport() == TimeZoneOption.STORE_QUERY_RETRIEVE) {
            TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice()
                    .getTimeZoneOfDevice();
            if (archiveTimeZone == null) // no Timezone support configured
                return;

            try {
                TimeZone timeZone = context.getRemoteAE().getDevice().getTimeZoneOfDevice();
                if (timeZone == null) {
                    LOG.debug(
                            "{}: No Timezone configured for destination - assume Archive Timezone: {}",
                            context.getRemoteAE().getAETitle(),
                            archiveTimeZone.getID());
                    timeZone = archiveTimeZone;
                }
                if (!timeZone.hasSameRules(archiveTimeZone)) {
                    LOG.debug(
                            "{}: Coerce attributes from Archive Timezone {} to destination Timezone {}",
                            context.getRemoteAE().getAETitle(),
                            archiveTimeZone.getID(), timeZone.getID());
                    attrs.setDefaultTimeZone(archiveTimeZone);
                    attrs.setTimezone(timeZone);
                }
                if (!attrs.containsValue(Tag.TimezoneOffsetFromUTC)) {
                    String offsetFromUTC = DateUtils.formatTimezoneOffsetFromUTC(
                            timeZone, dateOf(attrs));
                    attrs.setString(Tag.TimezoneOffsetFromUTC, VR.SH, offsetFromUTC);
                    LOG.debug(
                            "{}: Supplement attributes with Timezone Offset From UTC {}",
                            context.getRemoteAE().getAETitle(), offsetFromUTC);
                }
            } catch (Exception e) {
                throw new DicomServiceException(Status.UnableToProcess, e);
            }
        }
    }

    private Date dateOf(Attributes attrs) {
        Date date = attrs.getDate(Tag.ContentDateAndTime, new DatePrecision(Calendar.SECOND));
        return date != null ? date : attrs.getDate(Tag.StudyDateAndTime, new DatePrecision(Calendar.SECOND));
    }
}
