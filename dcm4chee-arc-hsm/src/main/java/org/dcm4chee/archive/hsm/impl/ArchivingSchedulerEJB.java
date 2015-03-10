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

package org.dcm4chee.archive.hsm.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchivingRule;
import org.dcm4chee.archive.entity.ArchivingTask;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.filemgmt.FileMgmt;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.storage.ContainerEntry;
import org.dcm4chee.storage.archiver.service.ArchiverContext;
import org.dcm4chee.storage.archiver.service.ArchiverService;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Stateless
public class ArchivingSchedulerEJB {

    private static final Logger LOG =
            LoggerFactory.getLogger(ArchivingSchedulerEJB.class);

    private static final String INSTANCE_PK = "instance_pk";
    private static final String DIGEST = "digest";
    private static final String OTHER_ATTRS_DIGEST = "otherAttrsDigest";
    private static final String FILE_SIZE = "fileSize";
    private static final String TRANSFER_SYNTAX = "transferSyntax";
    private static final String TIME_ZONE = "timeZone";
    private static final String DELETE_SOURCE = "deleteSource";
    private static final String LOCATION = "location";
    private static final String SOURCE_LOCATION_PKS_TO_DELETE = "srcLocationPksToDelete";

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @Inject
    private Device device;

    @Inject
    private CodeService codeService;

    @Inject
    private ArchiverService archiverService;

    @Inject
    private FileMgmt fileMgt;

    public void onStoreInstance(StoreContext storeContext,
            ArchivingRule archivingRule) {
        Attributes attrs = storeContext.getAttributes();
        String seriesInstanceUID = attrs.getString(Tag.SeriesInstanceUID);
        Date archivingTime = new Date(System.currentTimeMillis()
                + archivingRule.getDelayAfterInstanceStored() * 1000L);
        List<ArchivingTask> tasks = em.createNamedQuery(
                ArchivingTask.FIND_BY_SERIES_INSTANCE_UID, ArchivingTask.class)
                .setParameter(1, seriesInstanceUID)
                .getResultList();
        List<String> missingTargets = new ArrayList<String>(archivingRule.getStorageSystemGroupIDs().length);
        for (String target : archivingRule.getStorageSystemGroupIDs()) 
            missingTargets.add(target);
        for (ArchivingTask task : tasks) {
            task.setArchivingTime(archivingTime);
            LOG.debug("Updates {}", task);
            if (missingTargets.remove(task.getTargetStorageSystemGroupID())) {
                LOG.debug("Target StorageSystemGroup {} already scheduled!", task.getTargetStorageSystemGroupID());
            }
        }
        for (String targetGroupID : missingTargets) {
            ArchivingTask task = new ArchivingTask();
            task.setSeriesInstanceUID(seriesInstanceUID);
            task.setArchivingTime(archivingTime);
            task.setSourceStorageSystemGroupID(storeContext.getFileRef().getStorageSystemGroupID());
            task.setTargetStorageSystemGroupID(targetGroupID);
            task.setTargetName(getTargetName(attrs, targetGroupID));
            Code delayReasonCode = archivingRule.getDelayReasonCode();
            if (delayReasonCode != null)
                task.setDelayReasonCode(codeService.findOrCreate(delayReasonCode));
            em.persist(task);
            LOG.info("Create {}", task);
        }
    }

    public ArchivingTask scheduleNextArchivingTask() throws IOException {
        List<ArchivingTask> results = em.createNamedQuery(
                ArchivingTask.FIND_READY_TO_ARCHIVE, ArchivingTask.class)
                .setMaxResults(1)
                .getResultList();

        if (results.isEmpty())
            return null;

        ArchivingTask task = results.get(0);
        scheduleArchivingTask(task);
        em.remove(task);
        return task;
    }

    public void scheduleArchivingTask(ArchivingTask task) throws IOException {
        LOG.info("Scheduling {}", task);
        List<Instance> insts = em
                .createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID, Instance.class)
                .setParameter(1, task.getSeriesInstanceUID()).getResultList();
        scheduleInstances(insts, task.getSourceStorageSystemGroupID(), task.getTargetStorageSystemGroupID(), task.getTargetName(), false);
        LOG.info("Scheduled {}", task);
    }

    public void scheduleStudy(String studyIUID, String sourceGroupID, String targetGroupID, boolean deleteSource) throws IOException {
        LOG.info("Scheduling archiving study={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}", 
                studyIUID, sourceGroupID, targetGroupID, deleteSource);
        List<String> seriesUIDs = em.createQuery("SELECT se.seriesInstanceUID FROM Series se JOIN se.study st WHERE st.studyInstanceUID = ?1",
                String.class)
                .setParameter(1, studyIUID)
                .getResultList();
        for (String uid : seriesUIDs) {
            scheduleSeries(uid, sourceGroupID, targetGroupID, deleteSource);
        }
        LOG.info("Scheduled archiving study={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}", 
                new Object[] {studyIUID, sourceGroupID, targetGroupID, deleteSource});

    }

    public void scheduleSeries(String seriesIUID, String sourceGroupID, String targetGroupID, boolean deleteSource) throws IOException {
        LOG.info("Scheduling archiving series={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}", 
                new Object[] {seriesIUID, sourceGroupID, targetGroupID, deleteSource});
        List<Instance> insts = em
                .createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID,
                        Instance.class)
                        .setParameter(1, seriesIUID)
                        .getResultList();
        if (insts.size() > 0) {
            Instance instance = insts.get(0);
            Attributes attrs = Utils.mergeAndNormalize(instance.getSeries().getStudy().getAttributes(),instance.getSeries().getAttributes(),instance.getAttributes());
            scheduleInstances(insts, sourceGroupID, targetGroupID, getTargetName(attrs, targetGroupID), deleteSource);
            LOG.info("Scheduled archiving {} instances of series={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}", 
                    new Object[] {insts.size(), seriesIUID, sourceGroupID, targetGroupID, deleteSource});
        }    	
    }

    public void scheduleInstances(List<Instance> insts, String sourceGroupID, String targetStorageSystemGroupID, String targetName, boolean deleteSource) throws IOException {
        Location selected;
        String storageSystemGroupID;
        List<ContainerEntry> entries = new ArrayList<ContainerEntry>(insts.size());
        LocationDeleteContext srcLocationToDeleteCtx = deleteSource ? new LocationDeleteContext(insts.size()) : null;
        boolean instOnTarget;
        inst: for (Instance inst : insts) {
            selected = null;
            instOnTarget = false;
            for (Location location : inst.getLocations()) {
                storageSystemGroupID = location.getStorageSystemGroupID();
                if (storageSystemGroupID.equals(targetStorageSystemGroupID)) {
                    LOG.info("{} already archived to Storage System Group {} - skip from archiving",
                            inst, storageSystemGroupID);
                    if (!deleteSource) {
                        continue inst;
                    } else {
                        instOnTarget = true;
                    }
                } else if (storageSystemGroupID.equals(sourceGroupID)) {
                    selected = location;
                }
            }
            if (selected == null) {
                LOG.info("{} not available at Storage System Group {} - skip from archiving", inst, sourceGroupID);
            } else {
                if (deleteSource)
                    srcLocationToDeleteCtx.add(selected.getPk(), inst.getPk());
                if (!instOnTarget) {
                    ContainerEntry entry = new ContainerEntry.Builder(inst.getSopInstanceUID(),
                            selected.getDigest())
                    .setSourceStorageSystemGroupID(selected.getStorageSystemGroupID())
                    .setSourceStorageSystemID(selected.getStorageSystemID())
                    .setSourceName(selected.getStoragePath())
                    .setSourceEntryName(selected.getEntryName())
                    .setProperty(INSTANCE_PK, inst.getPk())
                    .setProperty(DIGEST, selected.getDigest())
                    .setProperty(OTHER_ATTRS_DIGEST, selected.getOtherAttsDigest())
                    .setProperty(FILE_SIZE, selected.getSize())
                    .setProperty(TRANSFER_SYNTAX, selected.getTransferSyntaxUID())
                    .setProperty(TIME_ZONE, selected.getTimeZone())
                    .setProperty(LOCATION, selected).build();
    
                    entries.add(entry);
                }
            }
        }
        if (entries.size() > 0) {
            ArchiverContext ctx = archiverService.createContext(targetStorageSystemGroupID, targetName);
            ctx.setEntries(entries);
            ctx.setProperty(DELETE_SOURCE, new Boolean(deleteSource));
            if (deleteSource)
                ctx.setProperty(SOURCE_LOCATION_PKS_TO_DELETE, srcLocationToDeleteCtx);
            archiverService.scheduleStore(ctx);
        } else {
            LOG.info("No source Locations found! Skip copy/move of {} instances from {} to {}.", insts.size(), sourceGroupID, targetStorageSystemGroupID);
            if (deleteSource && srcLocationToDeleteCtx.size() > 0) {
                LOG.debug("Deletion of source Locations:{}",srcLocationToDeleteCtx.getLocationPks());
                deleteLocations(srcLocationToDeleteCtx);
            }
        }
    }

    private StorageDeviceExtension storageDeviceExtension() {
        return device.getDeviceExtension(StorageDeviceExtension.class);
    }

    private String getTargetName(Attributes attrs, String groupID) {
        StorageSystemGroup grp = storageDeviceExtension().getStorageSystemGroup(groupID);
        String pattern = grp.getStorageFilePathFormat();
        return AttributesFormat.valueOf(pattern).format(attrs);
    }

    public void onContainerEntriesStored(ArchiverContext ctx) {
        List<ContainerEntry> entries = ctx.getEntries();
        boolean notInContainer = ctx.isNotInContainer();
        for (ContainerEntry entry : entries) {
            Instance inst = em.find(Instance.class, entry.getProperty(INSTANCE_PK));
            Location location = new Location.Builder()
            .storageSystemGroupID(ctx.getStorageSystemGroupID())
            .storageSystemID(ctx.getStorageSystemID())
            .storagePath(notInContainer ? entry.getNotInContainerName() : ctx.getName())
            .entryName(notInContainer ? null : entry.getName())
            .digest((String) entry.getProperty(DIGEST))
            .otherAttsDigest((String) entry.getProperty(OTHER_ATTRS_DIGEST))
            .size((Long) entry.getProperty(FILE_SIZE))
            .transferSyntaxUID((String) entry.getProperty(TRANSFER_SYNTAX))
            .timeZone((String) entry.getProperty(TIME_ZONE))
            .build();
            inst.getLocations().add(location);
            em.persist(location);
            LOG.info("Create {}", location);
        }
        em.flush();
        LocationDeleteContext srcLocationPksToDelete = (LocationDeleteContext) ctx.getProperty(SOURCE_LOCATION_PKS_TO_DELETE);
        if (srcLocationPksToDelete != null) {
            LOG.info("Source Locations to delete:{}", srcLocationPksToDelete);
            deleteLocations(srcLocationPksToDelete);
        }
    }
    
    private void deleteLocations(LocationDeleteContext srcLocationToDeleteCtx) {
        List<Location> locations = em.createQuery("SELECT l FROM Location l JOIN FETCH l.instances WHERE l.pk IN :locationPks",
                Location.class)
                .setParameter("locationPks", srcLocationToDeleteCtx.locationPks)
                .getResultList();
        ArrayList<Location> locationToDeletePks = new ArrayList<Location>(locations.size());
        for (Location l : locations) {
            long instPk = srcLocationToDeleteCtx.getInstancePk(l.getPk());
            Instance inst;
            for (Iterator<Instance> it = l.getInstances().iterator() ; it.hasNext() ;) {
                inst = it.next();
                if (inst.getPk() == instPk) {
                    for (Iterator<Location> itL = inst.getLocations().iterator() ; itL.hasNext() ;) {
                        if (itL.next().getPk() == l.getPk()) {
                            itL.remove();
                            break;
                        }
                    }
                    it.remove();
                }
            }
            em.merge(l);
            if (l.getInstances().size() == 0) {
                locationToDeletePks.add(l);
                LOG.info("Add Location {} to deletionPk list!", l);
            }
        }
        em.flush();
        if (locationToDeletePks.isEmpty()) {
            LOG.info("No unreferenced Location to delete!");
        } else {
            LOG.debug("Schedule deletion of source Locations:{}",locations);
            try {
                fileMgt.scheduleDelete(locationToDeletePks, 100);
            } catch (Exception x) {
                LOG.error("Schedule deletion of source Locations failed! locations:{}", locations, x);
            }
        }
    }
}
