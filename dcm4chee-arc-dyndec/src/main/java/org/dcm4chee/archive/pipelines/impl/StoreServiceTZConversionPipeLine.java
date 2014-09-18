package org.dcm4chee.archive.pipelines.impl;

import java.util.TimeZone;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.dcm4che3.data.Tag;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.dyndec.pipelines.BasicStorePipeLine;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class StoreServiceTZConversionPipeLine implements BasicStorePipeLine {
    static Logger LOG = LoggerFactory
            .getLogger(StoreServiceTZConversionPipeLine.class);

    @Inject
    private IApplicationEntityCache aeCache;

    @Override
    public void coerceAttributes(StoreContext context)
            throws DicomServiceException {
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
                    ApplicationEntity remoteAE = aeCache.get(session
                            .getRemoteAET());
                    remoteAETimeZone = remoteAE != null ? remoteAE.getDevice()
                            .getTimeZoneOfDevice() : null;
                    if (remoteAETimeZone != null) {
                        LOG.debug("(TimeZone Support):Loaded Device for remote Application entity AETitle: "
                                + remoteAE.getAETitle()
                                + " and device name: "
                                + remoteAE.getDevice().getDeviceName());
                        LOG.debug("(TimeZone Support):-with Time zone: "
                                + remoteAETimeZone.getID());
                    }
                } catch (ConfigurationException e1) {
                    LOG.warn(
                            "(TimeZone Support): Failed to access configuration for query source {} - no Timezone support:",
                            session.getRemoteAET(), e1);
                }
                if (remoteAETimeZone != null)
                    session.setSourceTimeZone(remoteAETimeZone);
                else
                    session.setSourceTimeZone(archiveTimeZone);

                if (attrs.containsValue(Tag.TimezoneOffsetFromUTC)) {
                    LOG.debug("(TimeZone Support):Found TimezoneOffsetFromUTC Attribute. \n "
                            + "(TimeZone Support): With value: "
                            + attrs.getString(Tag.TimezoneOffsetFromUTC)
                            + "(TimeZone Support): Setting sourceTimeZoneCache \n"
                            + "(TimeZone Support): Setting time zone to archive time.");
                    session.setSourceTimeZone(attrs.getTimeZone());
                    attrs.setTimezone(archiveTimeZone);
                } else if (session.getSourceTimeZone() == null) {
                    LOG.debug("(TimeZone Support): SourceTimeZoneCache is null \n "
                            + "(TimeZone Support): No device time zone"
                            + "(TimeZone Support): No TimezoneOffsetFromUTC Attribute."
                            + "Using archive time zone");
                    session.setSourceTimeZone(archiveTimeZone);
                }
                LOG.debug("(TimeZone Support): converting time zone from source time zone \n "
                        + session.getSourceTimeZone().getID());
                attrs.setDefaultTimeZone(session.getSourceTimeZone());
                LOG.debug("(TimeZone Support): converting time zone to archive time zone \n "
                        + archiveTimeZone.getID());
                attrs.setTimezone(archiveTimeZone);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

}
