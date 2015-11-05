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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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

package org.dcm4chee.archive.copy.mgmt.impl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.ArchiveServiceReloaded;
import org.dcm4chee.archive.ArchiveServiceStarted;
import org.dcm4chee.archive.ArchiveServiceStopped;
import org.dcm4chee.archive.copy.mgmt.UnarchivedInstanceDetectorService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.ArchivingRule;
import org.dcm4chee.archive.conf.UnarchivedInstanceDetector;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.event.StartStopReloadEvent;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.dcm4chee.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Steve Kroetsch<stevekroetsch@hotmail.com>
 *
 */
@ApplicationScoped
public class UnarchivedInstanceDetectorServiceImpl implements UnarchivedInstanceDetectorService {

    private static final Logger LOG = LoggerFactory
            .getLogger(UnarchivedInstanceDetectorServiceImpl.class);

    @Inject
    private Device device;

    @Inject
    private UnarchivedInstanceDetectorEJB ejb;

    @Inject
    private DicomConfiguration dicomConfiguration;

    @Inject
    private StorageService storageService;

    @Inject
    private IApplicationEntityCache aeCache;

    private int currentScanInterval;
    private ScheduledFuture<?> scanning;

    private AtomicBoolean scanIsRunning;

    public void onArchiveServiceStarted(@Observes @ArchiveServiceStarted StartStopReloadEvent start) {
        startScanning(scanInterval());
    }

    public void onArchiveServiceStopped(@Observes @ArchiveServiceStopped StartStopReloadEvent stop) {
        stopScanning();
    }

    public void onArchiveSeriviceReloaded(
            @Observes @ArchiveServiceReloaded StartStopReloadEvent reload) {
        int scanInterval = scanInterval();
        if (currentScanInterval != scanInterval) {
            stopScanning();
            startScanning(scanInterval);
        }
    }

    private synchronized void startScanning(int scanInterval) {
        if (scanning == null && scanInterval > 0 && isScannable()) {
            scanning = device.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    performScan();
                }
            }, scanInterval, scanInterval, TimeUnit.SECONDS);
            currentScanInterval = scanInterval;
            LOG.info("Unarchived Instance Detector: start scaning for "
                    + "unarchived instances with interval {}s", scanInterval);
        }
    }

    private synchronized void stopScanning() {
        if (scanning != null) {
            scanning.cancel(false);
            scanning = null;
            LOG.info("Unarchived Instance Detector: stop scaning for unarchived instances");
        }
    }

    private int scanInterval() {
        return device.getDeviceExtension(ArchiveDeviceExtension.class)
                .getUnarchivedInstanceDetector().getScanInterval();
    }

    private int batchSize() {
        return device.getDeviceExtension(ArchiveDeviceExtension.class)
                .getUnarchivedInstanceDetector().getBatchSize();
    }

    private Timestamp scanMarker() {
        return device.getDeviceExtension(ArchiveDeviceExtension.class)
                .getUnarchivedInstanceDetector().getScanMarkerTimestamp();
    }

    private long scanDuration() {
        return device.getDeviceExtension(ArchiveDeviceExtension.class)
                .getUnarchivedInstanceDetector().getScanDurationInMs();
    }

    private long nonScannablePeriod() {
        return device.getDeviceExtension(ArchiveDeviceExtension.class)
                .getUnarchivedInstanceDetector().getNonScanablePeriodInMs();
    }

    public static Timestamp truncate(long millis, String format) throws ParseException {
        Timestamp fullDate = new Timestamp(millis);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return new Timestamp(dateFormat.parse(dateFormat.format(fullDate)).getTime());
    }

    public static int msToDays(long timeInMs) {
        // Converts time in milliseconds to whole days, without rounding up
        return (int) timeInMs / 24 / 60 / 60 / 1000;
    }

    @Override
    public boolean isScannable() {
        // a new system will not have a marker set
        Timestamp marker;
        if ((marker = scanMarker()) == null) {
            try {
                resetMarker();
            } catch (Exception e) {
                LOG.error("Error resetting Marker", e);
            }
            // if this is still null, study_on_stg_sys has no entries
            if ((marker = scanMarker()) == null) {
                LOG.debug("Not searching for unarchived instances - unable to determine Marker");
                return false;
            }
        }

        try {
            Timestamp beginUnscanable = truncate(System.currentTimeMillis() - nonScannablePeriod(),
                    UnarchivedInstanceDetector.MARKER_DATE_FORMAT);
            if (marker.equals(beginUnscanable) || marker.after(beginUnscanable)) {
                LOG.debug("Not searching for unarchived instances - Marker is "
                        + "currently at or beyond the non scannable demarcation point");
                return false;
            }
        } catch (ParseException e) {
            LOG.error("Error examining non scannable period", e);
            return false;
        }

        if (batchSize() <= 0) {
            LOG.debug("Not searching for unarchived instances - BatchSize is 0");
            return false;
        }

        if (scanDuration() <= 0) {
            LOG.debug("Not searching for unarchived instances - ScanDuration is 0");
            return false;
        }

        return true;
    }

    @Override
    public void performScan() {
        if (!scanIsRunning.compareAndSet(false, true)) {
            LOG.info("Scan already running");
            return;
        }

        Timestamp startMarker = null;
        Timestamp endMarker = null;
        try {
            startMarker = scanMarker();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startMarker.getTime());
            cal.add(Calendar.DAY_OF_MONTH, msToDays(scanDuration()));
            endMarker = truncate(cal.getTimeInMillis(),
                    UnarchivedInstanceDetector.MARKER_DATE_FORMAT);
            endMarker = truncate(startMarker.getTime() + cal.getTimeInMillis(),
                    UnarchivedInstanceDetector.MARKER_DATE_FORMAT);
            Timestamp beginUnscanable = truncate(System.currentTimeMillis() - nonScannablePeriod(),
                    UnarchivedInstanceDetector.MARKER_DATE_FORMAT);
            if (endMarker.after(beginUnscanable)) {
                endMarker = beginUnscanable;
                LOG.debug("End marker set to beginning of non-scanable period");
            } else {
                LOG.debug("End marker set to start marker plus scan duration");
            }
            int offset = 0;
            int maxResults = batchSize();
            List<Series> seriesList;
            while ((seriesList = ejb.findSeriesByCreatedTime(startMarker, endMarker, offset,
                    maxResults)).size() > 0) {
                offset += seriesList.size();
                for (Series series : seriesList) {
                    List<String> storageSystemGroupIDs = new ArrayList<String>();
                    List<String> externalDeviceNames = new ArrayList<String>();
                    populateTargetsFromArchivingRules(series, storageSystemGroupIDs,
                            externalDeviceNames);
                    if (storageSystemGroupIDs.size() > 0 || externalDeviceNames.size() > 0) {
                        ejb.filterTargetsWithInstancesAlreadyStored(series, storageSystemGroupIDs,
                                externalDeviceNames);
                        scheduleForArchiving(series, storageSystemGroupIDs, externalDeviceNames);
                    } else {
                        LOG.warn("No storage targets found for {}", series);
                    }
                }

            }
        } catch (Exception e) {
            LOG.error("Error during check for unarchived instances from {} to {}", startMarker,
                    endMarker, e);
        } finally {
            if (endMarker != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        UnarchivedInstanceDetector.MARKER_DATE_FORMAT);
                try {
                    persistMarker(dateFormat.format(endMarker));
                } catch (Exception e) {
                    LOG.error("Error setting marker to end marker {}", endMarker, e);
                }
            }
            LOG.info("Completed checking for unarchived instances from {} to {}", startMarker,
                    endMarker);
            scanIsRunning.set(false);
        }
    }

    @Override
    public void populateTargetsFromArchivingRules(Series series,
            List<String> storageSystemGroupIDs, List<String> externalDeviceNames) throws Exception {
        ArchiveAEExtension arcAE = device.getDefaultAE().getAEExtension(ArchiveAEExtension.class);
        String sourceAET = series.getSourceAET();
        ApplicationEntity ae = sourceAET != null ? ae = aeCache.get(series.getSourceAET()) : null;
        Device device = ae != null ? ae.getDevice() : null;
        String deviceName = device != null ? device.getDeviceName() : null;
        List<ArchivingRule> archivingRules = arcAE.getArchivingRules().findArchivingRule(
                deviceName, sourceAET, series.getAttributes());
        for (ArchivingRule rule : archivingRules) {
            storageSystemGroupIDs.addAll(Arrays.asList(rule.getStorageSystemGroupIDs()));
            externalDeviceNames.addAll(Arrays.asList(rule.getExternalSystemsDeviceName()));
        }
        if (storageSystemGroupIDs.size() > 0 || externalDeviceNames.size() > 0) {
            ejb.filterTargetsWithArchivingTaskScheduled(series, storageSystemGroupIDs,
                    externalDeviceNames);
        }
    }

    @Override
    public void scheduleForArchiving(Series series, List<String> storageSystemGroupIDs,
            List<String> externalDeviceNames) {
        // TODO:
    }

    @Override
    public void resetMarker() {
        ArchiveAEExtension arcAE = device.getDefaultAE().getAEExtension(ArchiveAEExtension.class);
        String groupID = arcAE.getStorageSystemGroupID();
        if (groupID == null) {
            String groupType = arcAE.getStorageSystemGroupType();
            if (groupType != null) {
                StorageSystemGroup group = storageService.selectBestStorageSystemGroup(groupType);
                if (group != null) {
                    groupID = group.getGroupID();
                }
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                UnarchivedInstanceDetector.MARKER_DATE_FORMAT);
        Date date = ejb.findMinAccessTimeFromStudyOnStorageSystem(groupID);
        persistMarker(dateFormat.format(date));
    }

    @Override
    public void persistMarker(String marker) {
        try {
            ArchiveDeviceExtension devExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
            devExt.getUnarchivedInstanceDetector().setScanMarker(marker);
            dicomConfiguration.merge(device);
        } catch (ConfigurationException | ParseException e) {
            LOG.error("Error persisting scan marker", e);
        }
    }
}
