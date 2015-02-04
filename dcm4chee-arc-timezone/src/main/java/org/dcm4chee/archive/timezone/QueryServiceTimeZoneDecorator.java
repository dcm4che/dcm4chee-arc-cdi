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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
@Decorator
public abstract class QueryServiceTimeZoneDecorator implements QueryService {

    static Logger LOG = LoggerFactory
            .getLogger(QueryServiceTimeZoneDecorator.class);

    @Inject
    @Delegate
    QueryService queryService;

    @Override
    public void coerceRequestAttributes(QueryContext context)
            throws DicomServiceException {

        queryService.coerceRequestAttributes(context);
        try {
            ArchiveAEExtension arcAE = context.getArchiveAEExtension();
            Attributes keys = context.getKeys();
            TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice()
                    .getTimeZoneOfDevice();
            if (archiveTimeZone != null) {
                TimeZone sourceTimeZone = getSourceTimeZone(keys, context);
                if (sourceTimeZone != null) {
                    LOG.debug("(TimeZone Support): Query request with a requested timezone. \n "
                            + "(TimeZone Support): Converting to archive time and setting the requested time zone");
                    keys.setDefaultTimeZone(sourceTimeZone);
                    keys.setTimezone(archiveTimeZone);
                    context.setRequestedTimeZone(sourceTimeZone);
                }
            }
            LOG.debug("coerced attributes" + keys.toString());
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }

    }

    private TimeZone getSourceTimeZone(Attributes keys, QueryContext context) {
        if (keys.containsValue(Tag.TimezoneOffsetFromUTC))
            return keys.getTimeZone();
            
            TimeZone sourceTimeZone = context.getRemoteDevice().getTimeZoneOfDevice(); 
            if (sourceTimeZone != null ) {
                LOG.debug("Loaded Device for remote Application entity AETitle: "
                        + context.getRemoteAET()
                        + " and device name: "
                        + context.getRemoteDevice().getDeviceName());
                LOG.debug("with Time zone: "
                        + context.getRemoteDevice().getTimeZoneOfDevice().getID());
                return context.getRemoteDevice().getTimeZoneOfDevice();
            }
        return null;
    }

    @Override
    public void coerceResponseAttributes(QueryContext context, Attributes match)
            throws DicomServiceException {
        try {

            queryService.coerceResponseAttributes(context, match);
            ArchiveAEExtension arcAE = context.getArchiveAEExtension();
            Attributes attrs = match;
            TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice()
                    .getTimeZoneOfDevice();
            // Time zone query rsp adjustments
            if (archiveTimeZone != null) {
                attrs.setDefaultTimeZone(archiveTimeZone);
                LOG.debug("(TimeZone Support): Converting to requester time zone = "
                        + context.getRequestedTimeZone());
                if (context.getRequestedTimeZone() != null) {
                    attrs.setTimezone(context.getRequestedTimeZone());
                    LOG.debug("(TimeZone Support): Query response Found a requested timezone. \n "
                            + "(TimeZone Support): Converting to requester time zone");
                }
                if (!attrs.containsValue(Tag.TimezoneOffsetFromUTC)) {
                    attrs.setString(Tag.TimezoneOffsetFromUTC, VR.SH, DateUtils
                            .formatTimezoneOffsetFromUTC(StringUtils.maskNull(
                                    context.getRequestedTimeZone(),
                                    archiveTimeZone), attrs
                                    .getDate(Tag.StudyDateAndTime)));
                    LOG.debug("(TimeZone Support): In query response, adding TimezoneOffsetFromUTC. \n ");
                }
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

}
