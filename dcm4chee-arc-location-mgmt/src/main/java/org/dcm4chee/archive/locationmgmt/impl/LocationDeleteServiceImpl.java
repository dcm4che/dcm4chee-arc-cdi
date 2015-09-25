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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4chee.archive.locationmgmt.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.ArchiveServiceReloaded;
import org.dcm4chee.archive.ArchiveServiceStarted;
import org.dcm4chee.archive.ArchiveServiceStopped;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.DeletionRule;
import org.dcm4chee.archive.dto.ActiveService;
import org.dcm4chee.archive.entity.ExternalRetrieveLocation;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.event.StartStopReloadEvent;
import org.dcm4chee.archive.locationmgmt.DeleterService;
import org.dcm4chee.archive.locationmgmt.LocationDeleteResult;
import org.dcm4chee.archive.locationmgmt.LocationDeleteResult.DeletionStatus;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.archive.processing.ActiveProcessingService;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.dcm4chee.storage.conf.StorageSystemStatus;
import org.dcm4chee.storage.spi.StorageSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@ApplicationScoped
public class LocationDeleteServiceImpl implements DeleterService {

    private static final Logger LOG = LoggerFactory
            .getLogger(LocationDeleteServiceImpl.class);

    @Inject
    private Device device;

    @Inject
    private LocationMgmt locationManager;

    @Inject
    private javax.enterprise.inject.Instance<StorageSystemProvider> storageSystemProviders;

    @Inject
    private ActiveProcessingService activeProcessingService;

    private int lastPollInterval;

    private int deletionRetries;

    private ScheduledFuture<?> deleteTask;

    private Map<String, Date> lastDVDCalculationDateMap;

    private Map<String, Long> lastCalculatedDVDInBytesMap;
    
    private static final List<ActiveService> ACTIVE_ARCHIVE_OR_DELETER_SERVICES = Arrays.asList(ActiveService.LOCAL_ARCHIVING, 
            ActiveService.DELETER_SERVICE, ActiveService.STORE_REMEMBER_ARCHIVING);

    @PostConstruct
    public void init() {
        lastDVDCalculationDateMap = new ConcurrentHashMap<String, Date>();
        lastCalculatedDVDInBytesMap = new ConcurrentHashMap<String, Long>();
    }

    @Override
    public void freeUpSpaceDeleteSeries(String seriesInstanceUID, String groupID) {
        freeSpaceOnRequest(null, seriesInstanceUID, groupID);
    }

    @Override
    public void freeUpSpaceDeleteStudy(String studyInstanceUID, String groupID) {
        freeSpaceOnRequest(studyInstanceUID, null, groupID);
    }

    @Override
    public void freeUpSpace(String groupID) {
        ArchiveDeviceExtension arcExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        DeletionRule rule = arcExt.getDeletionRules().findByStorageSystemGroupID(groupID);
        if (rule != null) {
            freeSpace(rule);
        }
    }

    @Override
    public void freeUpSpace() {
        ArchiveDeviceExtension arcExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        List<DeletionRule> deletionRules = arcExt.getDeletionRules().getList();
        if(deletionRules.isEmpty()) {
            LOG.error("Location Deleter Service: No deletion rules configured, "
                    + " Malformed Configuration, some archive services might not function");
            return;
        }

        for(DeletionRule rule : deletionRules) {
            //check need to free up spacee
            try {
                if (canDeleteNow(rule.getStorageSystemGroupID())
                        || emergencyReached(rule.getStorageSystemGroupID()))
                    freeSpace(rule);
            } catch (IOException e) {
                LOG.error("Unable to calculate emergency case, "
                        + "error calculating emergency for group "
                        + "{} - reason {}", rule.getStorageSystemGroupID(), e);
            }
            catch (Throwable t) {                LOG.error("Exception occured while attempting to "
                        + "freespace from group {} - reason {}", rule.getStorageSystemGroupID(), t);
            }
        }
    }

    @Override
    public boolean validateGroupForDeletion(String groupID) {
        ArchiveDeviceExtension arcExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        DeletionRule rule = arcExt.getDeletionRules().findByStorageSystemGroupID(groupID);
        return rule == null ? false : rule.validate();
    }

    public void onArchiveServiceStarted(
            @Observes @ArchiveServiceStarted StartStopReloadEvent start) {

        int pollInterval = device.getDeviceExtension(
                ArchiveDeviceExtension.class).getDeletionServicePollInterval();
        startPolling(pollInterval);
    }

    public void onArchiveServiceStopped(
            @Observes @ArchiveServiceStopped StartStopReloadEvent stop) {

        stopPolling();
    }

    public void onArchiveSeriviceReloaded(
            @Observes @ArchiveServiceReloaded StartStopReloadEvent reload) {

        init();
        int pollInterval = device.getDeviceExtension(
                ArchiveDeviceExtension.class).getDeletionServicePollInterval();
        if (lastPollInterval != pollInterval) {
            if (deleteTask != null) {
                stopPolling();
                startPolling(pollInterval);
            } else
                startPolling(pollInterval);
        }
    }

    @Override
    public long calculateDataVolumePerDayInBytes(String groupID) {
        StorageDeviceExtension stgDevExt = device
                .getDeviceExtension(StorageDeviceExtension.class);
        StorageSystemGroup group = stgDevExt.getStorageSystemGroup(groupID);
        if (group == null) {
            LOG.error("Location Deleter Service: Group {} not configured, "
                            + " Malformed Configuration, some archive services"
                            + " might not function", groupID);
            return lastCalculatedDVDInBytesMap.get(groupID) != null
                    ? lastCalculatedDVDInBytesMap.get(groupID) : 0L;
        }
        if (!isDueCalculation(dataVolumePerDayCalculationRange(),
                lastDVDCalculationDateMap.get(groupID))) {
            return lastCalculatedDVDInBytesMap.get(groupID);
        } else {
            long dvdInBytes = locationManager.calculateDataVolumePerDayInBytes(
                    groupID, dataVolumePerDayAverageOnNDays());
            lastCalculatedDVDInBytesMap.put(groupID, dvdInBytes);
            lastDVDCalculationDateMap.put(groupID, new Date());
            return dvdInBytes;
        }
    }

    private LocationDeleteResult freeSpaceOnRequest(String studyInstanceUID, String seriesInstanceUID,
            String groupID) {
        ArchiveDeviceExtension arcExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        DeletionRule rule = arcExt.getDeletionRules().findByStorageSystemGroupID(groupID);
        if (validateGroupForDeletion(groupID)) {
            try{
            int minTimeToKeepStudy = rule.getMinTimeStudyNotAccessed();
            String minTimeToKeppStudyUnit = rule
                    .getMinTimeStudyNotAccessedUnit();
                List<Instance> allInstancesDueDeleteOnGroup = (ArrayList<Instance>) locationManager
                        .findInstancesDueDelete(minTimeToKeepStudy,
                                minTimeToKeppStudyUnit, rule.getStorageSystemGroupID(),
                                studyInstanceUID, seriesInstanceUID);
            List<Instance> actualInstancesToDelete = (ArrayList<Instance>) filterCopiesExist(
                    (ArrayList<Instance>) allInstancesDueDeleteOnGroup, rule);
            
            markCorrespondingStudyAndScheduleForDeletion(studyInstanceUID,
                    rule, removePendingArchivingOrDeletion(studyInstanceUID, actualInstancesToDelete));
            
            handleFailedToDeleteLocations(rule.getStorageSystemGroupID());
            }
            catch (Exception e) {
                return new LocationDeleteResult(DeletionStatus.FAILED,
                        "Deletion Failed, Reason " + getFailureReason(e));
            }
            return new LocationDeleteResult(DeletionStatus.SCHEDULED, "No Failure");
        }
        return new LocationDeleteResult(DeletionStatus.CRITERIA_NOT_MET,
                "Validation Criteria not met, rule on group " + rule.getStorageSystemGroupID()
                        + "can not be applied");
    }

    private String getFailureReason(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        LOG.info("Error scheduling delete requests, reason - {}", e);
        return sw.getBuffer().toString();
    }

    private boolean emergencyReached(String groupID) throws IOException {
        StorageDeviceExtension stgExt = device
                .getDeviceExtension(StorageDeviceExtension.class);
        StorageSystemGroup group = stgExt.getStorageSystemGroup(groupID);
        List<StorageSystem> tmpFlaggedsystems = new ArrayList<StorageSystem>();
        for(String systemID : group.getStorageSystems().keySet()) {
            StorageSystem system = group.getStorageSystem(systemID);
            StorageSystemProvider provider = system
                    .getStorageSystemProvider(storageSystemProviders);
            if(isUsableSystem(system)) {
                if (system.getMinFreeSpace() != null
                        && system.getMinFreeSpaceInBytes() == -1L)
                    system.setMinFreeSpaceInBytes(provider.getTotalSpace()
                            * Integer.parseInt(system.getMinFreeSpace()
                                    .replace("%", ""))/100);
                long usableSpace = provider.getUsableSpace() ;
                long minSpaceRequired = system.getMinFreeSpaceInBytes();
                if(usableSpace < minSpaceRequired) {
                    system.setStorageSystemStatus(StorageSystemStatus.FULL);
                    system.getStorageDeviceExtension().setDirty(true);
                LOG.info("System {} is about to fill up, "
                        + "emergency deletion in order, currently flagged dirty", system);
                tmpFlaggedsystems.add(system);
                }
            }
        }
        return tmpFlaggedsystems.isEmpty() ?  false : true;
    }

    private boolean canDeleteNow(String groupID) {

        String deleteServiceAllowedInterval = deleteServiceAllowedInterval();
        if(deleteServiceAllowedInterval == null)
            return false;
        if(deleteServiceAllowedInterval.split("-").length < 1) {
            LOG.error("Location Deleter Service: Allowed interval for deletion"
                    + " is not configured properly, service will not try to"
                    + " free up disk space on group {}", groupID);
            return false;
        }
        try{
        int min = Integer.parseInt(deleteServiceAllowedInterval.split("-")[0]);
        int max = Integer.parseInt(deleteServiceAllowedInterval.split("-")[1]);
        int hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        return hourNow > min && hourNow < max ? true : false;
        } catch (Exception e) {
            LOG.error("Location Deleter Service: Unable to decide allowed"
                    + " deletion interval , service will not attempt to"
                    + " free up disk space on group {} - reason {}",
                    groupID, e);
            return false;
        }
    }

    private boolean isDueCalculation(String dataVolumePerDayCalculationRange,
            Date lastDVDCalculationDate) {

        String dvdRange;
        if(lastDVDCalculationDate == null)
            return true;
        
        Calendar lastCalculatedOn = Calendar.getInstance();
        lastCalculatedOn.setTime(lastDVDCalculationDate);
        
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        
        if(lastCalculatedOn.get(Calendar.DAY_OF_MONTH) == currentTime
                .get(Calendar.DAY_OF_MONTH))
            return false;
        
        if (dataVolumePerDayCalculationRange.split("-").length < 1) {
            LOG.error("Location Deleter Service: Error calculating "
                    + "data volume per day, configuration "
                    + "Invalid data volume per day calculation range"
                    + "Using 23-0");
            dvdRange = "23-0" ;
        }
        else
            dvdRange = dataVolumePerDayCalculationRange;
        
        int min = Integer.parseInt(dvdRange
                .split("-")[0]);
        int max = Integer.parseInt(dvdRange
                .split("-")[1]);
        int now = currentTime.get(Calendar.HOUR_OF_DAY);
        
        if(now > min && now < max)
            return true;
        
        return false;
    }

    private void freeSpace(DeletionRule rule) {

          if (validateGroupForDeletion(rule.getStorageSystemGroupID())) {
            int minTimeToKeepStudy = rule.getMinTimeStudyNotAccessed();
            String minTimeToKeppStudyUnit = rule
                    .getMinTimeStudyNotAccessedUnit();
            List<Instance> allInstancesDueDeleteOnGroup = (ArrayList<Instance>) 
                    locationManager.findInstancesDueDelete(minTimeToKeepStudy
                            , minTimeToKeppStudyUnit, rule.getStorageSystemGroupID(), null, null);
            
                Map<String, List<Instance>> mapInstancesFoundOnGroupToStudy = 
                        getInstancesOnGroupPerStudyMap(allInstancesDueDeleteOnGroup, rule);
                    for (String studyUID : mapInstancesFoundOnGroupToStudy
                            .keySet()) {
                        if (!rule.isDeleteAsMuchAsPossible() 
                                && !needsFreeSpace(rule.getStorageSystemGroupID(), calculateExpectedDataVolumePerDay(rule)))
                                break;
                    markCorrespondingStudyAndScheduleForDeletion(
                            studyUID,
                            rule,
                            (ArrayList<Instance>) filterCopiesExist(
                                    (ArrayList<Instance>) mapInstancesFoundOnGroupToStudy
                                            .get(studyUID), rule));
                }
            handleFailedToDeleteLocations(rule.getStorageSystemGroupID());
        }
    }

    private void handleFailedToDeleteLocations(String groupID) {
        //handle failedToDeleteLocations
        LOG.debug("Finding locations that previously failed deletions");
        List<Location> failedToDeleteLocations = (ArrayList<Location>)
                locationManager.findFailedToDeleteLocations(groupID);
        try {
            locationManager.scheduleDelete(
                    failedToDeleteLocations, 1000, false);
        } catch (JMSException e) {
            LOG.error(
                    "Location Deleter Service: Failed to delete locations "
                    + "previously failing deletion - reason {}", e);
        }
    }

    private boolean needsFreeSpace(String groupID, long thresholdInBytes) {
        StorageDeviceExtension stgExt = device.getDeviceExtension(StorageDeviceExtension.class);
        StorageSystemGroup group = stgExt.getStorageSystemGroup(groupID);
        for(String systemID : group.getStorageSystems().keySet()) {
            StorageSystem system = group.getStorageSystem(systemID);
            StorageSystemProvider provider = system
                    .getStorageSystemProvider(storageSystemProviders);
            if(provider == null) {
                LOG.info("Location Deleter Service : system {}'s "
                        + "has no configured provider, deletion "
                        + "will not apply", system);
                return false;
            }
            if(isUsableSystem(system)) {
                try {
                if(system.getMinFreeSpace() != null && system.getMinFreeSpaceInBytes() == -1L)
                    system.setMinFreeSpaceInBytes(provider.getTotalSpace()
                            * Integer.parseInt(system.getMinFreeSpace()
                                    .replace("%", ""))/100);
                    if(provider.getUsableSpace() 
                            < system.getMinFreeSpaceInBytes() + thresholdInBytes) 
                    return true;
                } catch (IOException e) {
                    LOG.error("Location Deleter Service : "
                            + "failed to determine usable/total space on "
                            + "volume configured for system {} - reason {}"
                            , system, e);
                    return false;
                }
            }
        }
        return false;
    }

    private long calculateExpectedDataVolumePerDay(DeletionRule rule) {
        long dvdInBytes = calculateDataVolumePerDayInBytes(rule.getStorageSystemGroupID());
        ThresholdInterval currentInterval = null;
        String deletionThreshold = rule.getDeletionThreshold();
        List<ThresholdInterval> intervals =  
                createthresholdDurations(deletionThreshold, dvdInBytes);
        int now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        for(ThresholdInterval interval : intervals){
            if(interval.end == 0)
                interval.end = 24;
            if(now >= interval.start && now < interval.end)
                currentInterval = interval;
        }
        return currentInterval.expectedInBytes;
    }

    private List<ThresholdInterval> createthresholdDurations(
            String deletionThreshold, long dvdInBytes) {
        List<ThresholdInterval> intervals = new ArrayList<ThresholdInterval>();
        if (deletionThreshold == null)
            return intervals;
        String[] thresholds = deletionThreshold.split(";");
        
        Arrays.sort(thresholds, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.parseInt(o1.split(":")[0]) < Integer.parseInt(o2
                        .split(":")[0]) ? -1
                        : Integer.parseInt(o1.split(":")[0]) == Integer
                                .parseInt(o2.split(":")[0]) ? 0 : 1;
                }
        });

        if (thresholds.length == 0)
            return intervals;
        int end = 0, start;
        for (int i = 0; i < thresholds.length; i++) {
            if (thresholds[i].contains(":"))
                if (i+1<thresholds.length
                        && thresholds[i + 1].contains(":")) {
                    end = Integer.parseInt(thresholds[i+1].split(":")[0]);
                }
                else {
                    end = 0;
                }
                    start = Integer.parseInt(thresholds[i].split(":")[0]);
                    long value = Long.parseLong(thresholds[0].split(":")[1]
                            .replaceAll("[GBgbmbMBhH]", ""));
                    long bytes = toValueInBytes(value,
                                    thresholds[i].split(":")[1].replaceAll("\\d+",
                                            ""), dvdInBytes);
                    ThresholdInterval newInterval = new ThresholdInterval(
                            start, end, bytes);
                    intervals.add(newInterval);
        }
        if(intervals.get(0).start != 0) {
            ThresholdInterval firstInterval = new ThresholdInterval(0,
                    intervals.get(0).start,
                    intervals.get(thresholds.length - 1).expectedInBytes);
            intervals.add(0, firstInterval);
        }
        return intervals;
    }

    private boolean isUsableSystem(StorageSystem system) {
        return !system.isReadOnly() 
                && system.getAvailability() != Availability.OFFLINE
                && system.getAvailability() != Availability.UNAVAILABLE;
    }

    private Map<String,List<Instance>> getInstancesOnGroupPerStudyMap(
            List<Instance> allInstancesDueDelete
            , DeletionRule rule) {

        Map<String,List<Instance>> instancesOnGroupPerStudyMap = new HashMap<String, List<Instance>>();
        
        if(allInstancesDueDelete == null)
            return instancesOnGroupPerStudyMap;
        
        for(Instance inst : allInstancesDueDelete) {
            
            String studyUID = inst.getSeries().getStudy().getStudyInstanceUID();
            if(!instancesOnGroupPerStudyMap.containsKey(studyUID))
                instancesOnGroupPerStudyMap.put(studyUID, new ArrayList<Instance>());
            for(Location loc : inst.getLocations()) {
                if(loc.getStorageSystemGroupID().compareTo(rule.getStorageSystemGroupID()) == 0)
                    instancesOnGroupPerStudyMap.get(studyUID).add(inst);
            }
        }
        return removePendingArchivingOrDeletion(instancesOnGroupPerStudyMap);
    }

    private Map<String, List<Instance>> removePendingArchivingOrDeletion(
            Map<String, List<Instance>> instancesOnGroupPerStudyMap) {
        Map<String, List<Instance>> adjustedInstancesOnGroupPerStudyMap = 
                new HashMap<String, List<Instance>>();
        for(String studyUID : instancesOnGroupPerStudyMap.keySet()) {
            if (!activeProcessingService.isStudyUnderProcessingByServices(studyUID, ACTIVE_ARCHIVE_OR_DELETER_SERVICES)) {
                adjustedInstancesOnGroupPerStudyMap.put(studyUID, 
                        instancesOnGroupPerStudyMap.get(studyUID));
            }
        }
        
        return adjustedInstancesOnGroupPerStudyMap;
    }

    private List<Instance> removePendingArchivingOrDeletion(String studyIUID, List<Instance>instancesOnGroup) {
            if (activeProcessingService.isStudyUnderProcessingByServices(studyIUID, ACTIVE_ARCHIVE_OR_DELETER_SERVICES)) {
                return Collections.emptyList();
            }
        
        return instancesOnGroup;
    }

    private synchronized void startPolling(int pollInterval) {
        if (deleteTask == null && pollInterval > 0) {
            deleteTask = device.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    freeUpSpace();
                }
            }, pollInterval, pollInterval, TimeUnit.SECONDS);
            lastPollInterval = pollInterval;
            LOG.info(
                    "Location Deleter Service: started deletion task with interval {}s",
                    pollInterval);
        }
    }

    private synchronized void stopPolling() {
        if (deleteTask != null) {
            deleteTask.cancel(false);
            deleteTask = null;
            LOG.info("Location Deleter Service: stopped deletion task, last interval {}", lastPollInterval);
        }
    }

    private void markCorrespondingStudyAndScheduleForDeletion(
            String studyInstanceUID, DeletionRule rule,
            List<Instance> instancesDueDelete) {
        deletionRetries = maxDeleteServiceRetries();
        if (!instancesDueDelete.isEmpty())
            locationManager.markForDeletion(studyInstanceUID,
                    rule.getStorageSystemGroupID());
            List<Instance> tmpInstancesScheduled = new ArrayList<Instance>();
            for(int i = -1; i<deletionRetries; i++) {
                instancesDueDelete.removeAll(tmpInstancesScheduled);
                tmpInstancesScheduled.clear();
            for (Instance inst : instancesDueDelete)
                try {
                    locationManager.scheduleDelete(
                            getLocationsOnGroup(inst, rule.getStorageSystemGroupID()), 1000, true);
                    tmpInstancesScheduled.add(inst);
                    activeProcessingService.addActiveProcess(studyInstanceUID, 
                            inst.getSeries().getSeriesInstanceUID(), 
                            inst.getSopInstanceUID(), 
                            ActiveService.DELETER_SERVICE);
                } catch (JMSException e) {
                        LOG.error("Location Deleter Service: error scheduling "
                                + "deletion, attemting retry no {} - reason {}"
                                , i, e);
                        activeProcessingService.deleteActiveProcessBySOPInstanceUIDandService( 
                                inst.getSopInstanceUID(), 
                                ActiveService.DELETER_SERVICE);
                    break;
                }
            }
    }

    private Collection<Location> getLocationsOnGroup(Instance inst, String groupID) {
        Collection<Location> locationsOnGroup = new ArrayList<Location>();
        for(Location loc : inst.getLocations())
            if(loc.getStorageSystemGroupID().compareTo(groupID) == 0)
                locationsOnGroup.add(loc);
        return locationsOnGroup;
    }

    private List<Instance> filterCopiesExist(
            List<Instance> instancesDueDeleteOnGroup, DeletionRule rule) {
        List<String> hasToBeOnSystems = Arrays.asList(rule.getArchivedOnExternalSystems());
        List<String> hasToBeOnGroups = Arrays.asList(rule.getArchivedOnGroups());
        List<Instance> filteredOnMany = new ArrayList<Instance>();
        if((Integer.parseInt(rule.getNumberOfArchivedCopies()) == 0)) {
            return instancesDueDeleteOnGroup;
        }
        else if(Integer.parseInt(rule.getNumberOfArchivedCopies()) > 0) {
            return filterNCopiesExist(instancesDueDeleteOnGroup, rule);
        }
        else {
        if( hasToBeOnSystems != null && !hasToBeOnSystems.isEmpty()) {
            filteredOnMany = filterOnExternalSystem(hasToBeOnSystems, instancesDueDeleteOnGroup);
        }
        if(hasToBeOnGroups != null && !hasToBeOnGroups.isEmpty()) {
                return filterOnGroups(hasToBeOnGroups,!filteredOnMany.isEmpty()
                        ? filteredOnMany : instancesDueDeleteOnGroup);
        }
        return filteredOnMany;
        }
    }

    private List<Instance> filterOnGroups(List<String> hasToBeOnGroups,
            List<Instance> filteredOnMany) {
        List<String> tmpFoundOnGroups = new ArrayList<String>();
        List<Instance> foundOnConfiguredGroups = new ArrayList<Instance>();
        for (Instance inst : filteredOnMany) {
            for (Location loc : inst.getLocations()) {
                if (!tmpFoundOnGroups.contains(loc.getStorageSystemGroupID()))
                    tmpFoundOnGroups.add(loc.getStorageSystemGroupID());
            }
            if (tmpFoundOnGroups.containsAll(hasToBeOnGroups))
                foundOnConfiguredGroups.add(inst);
        }
        return foundOnConfiguredGroups;
    }

    private List<Instance> filterOnExternalSystem(
            List<String> hasToBeOnSystems, List<Instance> filteredOnMany) {
        List<String> tmpFoundOnSystems = new ArrayList<String>();
        List<Instance> foundOnConfiguredSystems = new ArrayList<Instance>();
        for (Instance inst : filteredOnMany) {
            for (ExternalRetrieveLocation extLoc : inst.getExternalRetrieveLocations()) {
                if (!tmpFoundOnSystems.contains(extLoc.getRetrieveDeviceName()))
                    tmpFoundOnSystems.add(extLoc.getRetrieveDeviceName());
            }
            if (tmpFoundOnSystems.containsAll(hasToBeOnSystems))
                foundOnConfiguredSystems.add(inst);
        }
        return foundOnConfiguredSystems;
    }

    private List<Instance> filterNCopiesExist(
            List<Instance> instancesDueDeleteOnGroup, DeletionRule rule) {
        String groupID = rule.getStorageSystemGroupID();
        List<Instance> foundOnNSafeLocations = new ArrayList<Instance>();
        int found = 0;
        for (Instance inst : instancesDueDeleteOnGroup) {
            if (inst.getExternalRetrieveLocations() != null 
                    && !inst.getExternalRetrieveLocations().isEmpty()) {
                for(int i=0; i<inst.getExternalRetrieveLocations().size(); i++)
                found++;
            }
            for(Location loc : inst.getLocations()) {
                StorageSystemGroup locationGroup = device
                        .getDeviceExtension(StorageDeviceExtension.class)
                        .getStorageSystemGroup(loc.getStorageSystemGroupID());
                if(!loc.isWithoutBulkData() 
                        && locationGroup.getGroupID().compareTo(groupID) != 0
                        && (locationGroup.getStorageSystemGroupType() == null ||
                                locationGroup.getStorageSystemGroupType()
                                .compareTo(rule.getSafeArchivingType()) == 0
                        || rule.getSafeArchivingType().compareTo("*") == 0)) {
                    found++;
                }
            }
            if(found >= Integer.parseInt(rule.getNumberOfArchivedCopies()))
                foundOnNSafeLocations.add(inst);
        }
        return foundOnNSafeLocations;
    }

    private long toValueInBytes(long value, String unit, long dvdInBytes) {
        if ("GB".equalsIgnoreCase(unit))
            return value * 1000000000;
        if("GIB".equalsIgnoreCase(unit))
            return value * 125000000;
        else if ("MB".equalsIgnoreCase(unit))
            return value * 1000000;
        else if ("MIB".equalsIgnoreCase(unit))
            return value * 125000;
        else if ("KB".equalsIgnoreCase(unit))
            return value * 1000;
        else if ("KIB".equalsIgnoreCase(unit))
            return value * 125;
        else if ("H".equalsIgnoreCase(unit))
            return (dvdInBytes * value)/24;
        else if ("D".equalsIgnoreCase(unit))
            return dvdInBytes * value;
        else
            return value;
    }

    private String deleteServiceAllowedInterval() {
        ArchiveDeviceExtension arcExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        return arcExt.getDeleteServiceAllowedInterval();
    }

    private int maxDeleteServiceRetries() {
        ArchiveDeviceExtension arcExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        return arcExt.getMaxDeleteServiceRetries();
    }

    private String dataVolumePerDayCalculationRange() {
        ArchiveDeviceExtension arcExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        return arcExt.getDataVolumePerDayCalculationRange();
    }

    private int dataVolumePerDayAverageOnNDays() {
        ArchiveDeviceExtension arcExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        return arcExt.getDataVolumePerDayAverageOnNDays();
    }

    protected class ThresholdInterval {
        int start, end;
        long expectedInBytes;

        public ThresholdInterval(int start, int end, long bytes) {
            this.start = start;
            this.end = end;
            this.expectedInBytes = bytes;
        }
    }
}
