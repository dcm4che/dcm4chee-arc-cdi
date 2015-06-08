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

import java.util.Date;
import java.util.TimeZone;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;
import org.dcm4chee.archive.query.decorators.DelegatingQueryService;
import org.dcm4chee.conf.decorators.DynamicDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@DynamicDecorator
public class QueryServiceTimeZoneDecorator extends DelegatingQueryService {

    static Logger LOG = LoggerFactory
            .getLogger(QueryServiceTimeZoneDecorator.class);

    @Override
    public void coerceRequestAttributes(QueryContext context)
            throws DicomServiceException {

        getNextDecorator().coerceRequestAttributes(context);
        ArchiveAEExtension arcAE = context.getArchiveAEExtension();
        TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice().getTimeZoneOfDevice();
        if (archiveTimeZone == null)    // no Timezone support configured
            return;

        Attributes keys = context.getKeys();
        if (!keys.containsTimezoneOffsetFromUTC()) {
            TimeZone remoteAETimeZone = context.getRemoteDeviceTimeZone();
            if (remoteAETimeZone != null) {
                LOG.debug("{}: No Timezone Offset in query request - use configured Timezone: {}",
                        context.getRemoteAET(), remoteAETimeZone.getID());
                keys.setDefaultTimeZone(remoteAETimeZone);
            } else {
                LOG.debug("{}: No Timezone configured for remote AE - assume Archive Timezone: {}",
                        context.getRemoteAET(), archiveTimeZone.getID());
                keys.setDefaultTimeZone(archiveTimeZone);
            }
        }
        try {
            TimeZone timeZone = keys.getTimeZone();
            context.setRequestedTimeZone(timeZone);
            if (!timeZone.hasSameRules(archiveTimeZone)) {
                LOG.debug("{}: Coerce query request from Timezone {} to Archive Timezone {}",
                        context.getRemoteAET(), timeZone.getID(), archiveTimeZone.getID());
                keys.setTimezone(archiveTimeZone);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public void coerceResponseAttributes(QueryContext context, Attributes match)
            throws DicomServiceException {
        getNextDecorator().coerceResponseAttributes(context, match);
        ArchiveAEExtension arcAE = context.getArchiveAEExtension();
        TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice().getTimeZoneOfDevice();
        if (archiveTimeZone == null)    // no Timezone support configured
            return;

        match.setDefaultTimeZone(archiveTimeZone);
        try {
            TimeZone timeZone = context.getRequestedTimeZone();
            if (!timeZone.hasSameRules(archiveTimeZone)) {
                LOG.debug("{}: Coerce query response from Archive Timezone {} to Timezone {}",
                        context.getRemoteAET(), timeZone.getID(), archiveTimeZone.getID());
                match.setTimezone(timeZone);
            }
            if (!match.containsValue(Tag.TimezoneOffsetFromUTC)) {
                String offsetFromUTC = DateUtils.formatTimezoneOffsetFromUTC(timeZone, dateOf(match));
                match.setString(Tag.TimezoneOffsetFromUTC, VR.SH, offsetFromUTC);
                LOG.debug("{}: Supplement query response with Timezone Offset From UTC {}",
                        context.getRemoteAET(), offsetFromUTC);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    private Date dateOf(Attributes match) {
        Date date = match.getDate(Tag.ContentDateAndTime);
        return date != null ? date : match.getDate(Tag.StudyDateAndTime);
    }

}
