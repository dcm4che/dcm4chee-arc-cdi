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

package org.dcm4chee.archive.locationmgmt.Impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.bcel.generic.NEWARRAY;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.ArchiveServiceReloaded;
import org.dcm4chee.archive.ArchiveServiceStarted;
import org.dcm4chee.archive.ArchiveServiceStopped;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.entity.ExternalRetrieveLocation;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.event.StartStopReloadEvent;
import org.dcm4chee.archive.locationmgmt.DeleterService;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystemGroup;
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

    private int lastPollInterval;

    private ScheduledFuture<?> deleteTask;

    @Override
    public void freeUpSpaceDeleteSeries(String seriesInstanceUID) {
        // TODO Auto-generated method stub
    }

    @Override
    public void freeUpSpaceDeleteStudy(String studyInstanceUID) {
        // TODO Auto-generated method stub
    }

    @Override
    public void freeUpSpace(StorageSystemGroup groupToFree) {
        freeSpace(groupToFree);
    }

    @Override
    public void freeUpSpace() {

        StorageDeviceExtension stgDevExt = device.getDeviceExtension(StorageDeviceExtension.class);
        Map<String,StorageSystemGroup>groups = stgDevExt.getStorageSystemGroups();
        
        if(groups == null) {
            LOG.error("Location Deleter Service: No storage Groups configured, "
                    + " Malformed Configuration, some archive services might not function");
            return;
        }
        
        for(String groupID : groups.keySet()) {
            //check need to free up space
            //TODO
            StorageSystemGroup group = groups.get(groupID);
            freeSpace(group);
        }
    }

    @Override
    public boolean validateGroupForDeletion(StorageSystemGroup group) {
        if(group.getArchivedOnExternalSystems() != null 
                || group.getArchivedOnGroups() !=null
                || group.isArchivedAnyWhere())
            if(group.getDeletionThreshold() != null || group.isDeleteAsMuchAsPossible())
                if(group.getMinTimeStudyNotAccessed() >0 && group.getMinTimeStudyNotAccessedUnit() != null)
                    return true;
        return false;
    }

    public void onArchiveServiceStarted(
            @Observes @ArchiveServiceStarted StartStopReloadEvent start) {
        int pollInterval =device.getDeviceExtension(ArchiveDeviceExtension.class)
                .getDeletionServicePollInterval();
       if(pollInterval > 0)
           startPolling(pollInterval);
    }

    public void onArchiveServiceStopped(
            @Observes @ArchiveServiceStopped StartStopReloadEvent stop) {
        stopPolling();
    }

    public void onArchiveSeriviceReloaded(
            @Observes @ArchiveServiceReloaded StartStopReloadEvent reload) {
        int pollInterval = device.getDeviceExtension(
                ArchiveDeviceExtension.class).getDeletionServicePollInterval();
        if (lastPollInterval != pollInterval) {
            if(deleteTask != null) {
            stopPolling();
            startPolling(pollInterval);
            }
            else 
                startPolling(pollInterval);
        }
    }

    private void freeSpace(StorageSystemGroup group) {
        if(validateGroupForDeletion(group)){
        int minTimeToKeepStudy = group.getMinTimeStudyNotAccessed();
        String minTimeToKeppStudyUnit = group.getMinTimeStudyNotAccessedUnit();
        try{
        ArrayList<Instance> allInstancesDueDelete = (ArrayList<Instance>) locationManager
                .findInstancesDueDelete(minTimeToKeepStudy,
                        minTimeToKeppStudyUnit, group.getGroupID());
        
        if(allInstancesDueDelete != null) {
            Map<String,List<Instance>> actuallInstancesFoundOnGroup = getInstancesOnGroupPerStudyMap(allInstancesDueDelete, group);
            Map<String,List<Instance>> allInstancesFoundOnStudy = getInstanceFoundOnStudy(allInstancesDueDelete, group);
            for(String studyUID : actuallInstancesFoundOnGroup.keySet()) {
        if(passDeletionConstraints(actuallInstancesFoundOnGroup.get(studyUID), 
                actuallInstancesFoundOnGroup.get(studyUID), group))
            markCorrespondingStudiesAndScheduleForDeletion(instancesDueDelete);
        }
        }
        }
        catch(Exception e) {
            LOG.error("Location Deleter Service : Unexpected "
                    + "error while attempting location deletion - reason {}", e);
        }
        }
    }



    private boolean passDeletionConstraints(List<Instance> allInstancesOfStudy,
            List<Instance> instancesFoundOnGroup, StorageSystemGroup group) {
        if(group.isDeleteAsMuchAsPossible()) {
            return true;
        }
        if(needsFreeSpace(group.getDeletionThreshold()))
            return true;
        return false;
    }

    private boolean needsFreeSpace(String deletionThreshold) {
        Date now = new Date();
        getCurrentDateThresholdInBytes();
        return false;
    }

    private void getCurrentDateThresholdInBytes() {
        
    }

    private Map<String, List<Instance>> getInstanceFoundOnStudy(
            ArrayList<Instance> allInstancesDueDelete, StorageSystemGroup group) {
        Map<String,List<Instance>> instancesPerStudyMap = new HashMap<String, List<Instance>>();
        for(Instance inst : allInstancesDueDelete) {
            String studyUID = inst.getSeries().getStudy().getStudyInstanceUID();
            if(!instancesPerStudyMap.containsKey(studyUID))
                instancesPerStudyMap.put(studyUID, new ArrayList<Instance>());
            instancesPerStudyMap.get(studyUID).add(inst);
        }
        return instancesPerStudyMap;
    }

    private Map<String,List<Instance>> getInstancesOnGroupPerStudyMap(
            ArrayList<Instance> allInstancesDueDelete, StorageSystemGroup group) {
        Map<String,List<Instance>> instancesOnGroupPerStudyMap = new HashMap<String, List<Instance>>();
        for(Instance inst : allInstancesDueDelete) {
            String studyUID = inst.getSeries().getStudy().getStudyInstanceUID();
            if(!instancesOnGroupPerStudyMap.containsKey(studyUID))
                instancesOnGroupPerStudyMap.put(studyUID, new ArrayList<Instance>());
            for(Location loc : inst.getLocations()) {
                if(loc.getStorageSystemGroupID().compareTo(group.getGroupID()) == 0)
                    instancesOnGroupPerStudyMap.get(studyUID).add(inst);
            }
        }
        return instancesOnGroupPerStudyMap;
    }

    private synchronized void startPolling(int pollInterval) {
        if (deleteTask == null) {
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


    private void markCorrespondingStudiesAndScheduleForDeletion(
            ArrayList<Instance> instancesDueDelete) {
        // TODO Auto-generated method stub
        
    }

    private List<Instance> filterCopiesExist(
            ArrayList<Instance> instancesDueDeleteOnGroup, StorageSystemGroup group) {
        List<String> hasToBeOnSystems = Arrays.asList(group.getArchivedOnExternalSystems());
        List<String> hasToBeOnGroups = Arrays.asList(group.getArchivedOnGroups());
        List<Instance> filteredOnMany = new ArrayList<Instance>();
        boolean archivedAnyWhere = group.isArchivedAnyWhere();
        if(archivedAnyWhere) {
            return filterOneCopyExists(instancesDueDeleteOnGroup, group.getGroupID());
        }
        else {
            if((hasToBeOnSystems == null || hasToBeOnSystems.isEmpty())
                    && (hasToBeOnGroups == null || hasToBeOnGroups.isEmpty()))
                return new ArrayList<Instance>();
        if( hasToBeOnSystems != null && !hasToBeOnSystems.isEmpty()) {
            filteredOnMany = filterOnExternalSystem(hasToBeOnSystems, instancesDueDeleteOnGroup);
        }
        if(hasToBeOnGroups != null && !hasToBeOnGroups.isEmpty()) {
            if(!filteredOnMany.isEmpty())
                return filterOnGroups(hasToBeOnGroups, filteredOnMany);
            else
                return filterOnGroups(hasToBeOnGroups, instancesDueDeleteOnGroup);
        }
        return filteredOnMany;
        }
    }

    private List<Instance> filterOnGroups(List<String> hasToBeOnGroups,
            List<Instance> filteredOnMany) {
        ArrayList<String> tmpFoundOnGroups = new ArrayList<String>();
        ArrayList<Instance> foundOnConfiguredGroups = new ArrayList<Instance>();
        for (Instance inst : filteredOnMany) {
            for (Location loc : inst.getLocations()) {
                if (!tmpFoundOnGroups.contains(loc.getStorageSystemGroupID()))
                    tmpFoundOnGroups.add(loc.getStorageSystemGroupID());
            }
            if (tmpFoundOnGroups.contains(hasToBeOnGroups))
                foundOnConfiguredGroups.add(inst);
        }
        return foundOnConfiguredGroups;
    }

    private List<Instance> filterOnExternalSystem(
            List<String> hasToBeOnSystems, ArrayList<Instance> filteredOnMany) {
        ArrayList<String> tmpFoundOnSystems = new ArrayList<String>();
        ArrayList<Instance> foundOnConfiguredSystems = new ArrayList<Instance>();
        for (Instance inst : filteredOnMany) {
            for (ExternalRetrieveLocation extLoc : inst.getExternalRetrieveLocations()) {
                if (!tmpFoundOnSystems.contains(extLoc.getRetrieveDeviceName()))
                    tmpFoundOnSystems.add(extLoc.getRetrieveDeviceName());
            }
            if (tmpFoundOnSystems.contains(hasToBeOnSystems))
                foundOnConfiguredSystems.add(inst);
        }
        return foundOnConfiguredSystems;
    }

    private ArrayList<Instance> filterOneCopyExists(
            ArrayList<Instance> instancesDueDeleteOnGroup, String  groupID) {
        ArrayList<Instance> foundOnAtleastOneGroup = new ArrayList<Instance>();
        for(Instance inst : instancesDueDeleteOnGroup) {
            if(inst.getExternalRetrieveLocations() != null 
                    && !inst.getExternalRetrieveLocations().isEmpty()) {
                foundOnAtleastOneGroup.add(inst);
                continue;
            }
            for(Location loc : inst.getLocations()) {
                if(!loc.isWithoutBulkData() 
                        && loc.getStorageSystemGroupID().compareTo(groupID) != 0) {
                    foundOnAtleastOneGroup.add(inst);
                }
            }
        }
        return foundOnAtleastOneGroup;
    }

    private List<String> toSystems(List<ExternalRetrieveLocation> extLocs) {
        List<String> systems = new ArrayList<String>();
        for(ExternalRetrieveLocation loc : extLocs)
            if(loc.getAvailability() != Availability.OFFLINE 
            || loc.getAvailability() != Availability.UNAVAILABLE)
            systems.add(loc.getRetrieveDeviceName());
        return systems;
    }

}
