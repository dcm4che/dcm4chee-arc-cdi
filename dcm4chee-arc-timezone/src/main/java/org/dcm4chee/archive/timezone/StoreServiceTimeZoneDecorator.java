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

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
@Decorator
public abstract class StoreServiceTimeZoneDecorator implements StoreService {

    static Logger LOG = LoggerFactory
            .getLogger(StoreServiceTimeZoneDecorator.class);

    @Inject
    @Delegate
    StoreService storeService;

    @Inject
    private IApplicationEntityCache aeCache;

    @Override
    public void coerceAttributes(StoreContext context)
            throws DicomServiceException {
        storeService.coerceAttributes(context);
        StoreSession session = context.getStoreSession();
        ArchiveAEExtension arcAE = session.getArchiveAEExtension();
        Attributes attrs = context.getAttributes();
        TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice()
                .getTimeZoneOfDevice();
        TimeZone remoteAETimeZone = null;
        try {
            // Time zone store adjustments
            if (archiveTimeZone != null) {
                try {
                    ApplicationEntity remoteAE =aeCache.get(session.getRemoteAET()); 
                    remoteAETimeZone = remoteAE!=null?remoteAE.getDevice().getTimeZoneOfDevice():null;
                    if(remoteAETimeZone!=null){
                    LOG.debug("(TimeZone Support):Loaded Device for remote Application entity AETitle: "
                            + remoteAE.getAETitle()
                            + " and device name: "
                            + remoteAE.getDevice().getDeviceName());
                    LOG.debug("(TimeZone Support):-with Time zone: "
                            + remoteAETimeZone
                                    .getID());
                    }
                } catch (ConfigurationException e1) {
                    LOG.warn(
                            "(TimeZone Support): Failed to access configuration for query source {} - no Timezone support:",
                            session.getRemoteAET(), e1);
                }
                if (remoteAETimeZone != null)
                    context.setSourceTimeZone(remoteAETimeZone);
                else
                    context.setSourceTimeZone(archiveTimeZone);

                if (attrs.containsValue(Tag.TimezoneOffsetFromUTC)) {
                    LOG.debug("(TimeZone Support):Found TimezoneOffsetFromUTC Attribute. \n "
                            + "(TimeZone Support): With value: "
                            + attrs.getString(Tag.TimezoneOffsetFromUTC)
                            + "(TimeZone Support): Setting sourceTimeZoneCache \n"
                            + "(TimeZone Support): Setting time zone to archive time.");
                    context.setSourceTimeZone(attrs.getTimeZone());
                    attrs.setTimezone(archiveTimeZone);
                } else if (context.getSourceTimeZone() == null) {
                    LOG.debug("(TimeZone Support): SourceTimeZoneCache is null \n "
                            + "(TimeZone Support): No device time zone"
                            + "(TimeZone Support): No TimezoneOffsetFromUTC Attribute."
                            + "Using archive time zone");
                    context.setSourceTimeZone(archiveTimeZone);
                }
                LOG.debug("(TimeZone Support): converting time zone from source time zone \n "
                        + context.getSourceTimeZone().getID());
                attrs.setDefaultTimeZone(context.getSourceTimeZone());
                LOG.debug("(TimeZone Support): converting time zone to archive time zone \n "
                        + archiveTimeZone.getID());
                attrs.setTimezone(archiveTimeZone);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }
}
