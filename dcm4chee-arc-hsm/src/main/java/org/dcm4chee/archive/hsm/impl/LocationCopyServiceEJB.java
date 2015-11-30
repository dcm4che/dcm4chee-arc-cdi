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
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Location.Status;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.hsm.LocationCopyContext;
import org.dcm4chee.archive.hsm.LocationsCopied;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.storage.ContainerEntry;
import org.dcm4chee.storage.archiver.service.ArchiverContext;
import org.dcm4chee.storage.archiver.service.ArchiverService;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Steve Kroetsch <stevekroetsch@hotmail.com>
 *
 */
@Stateless
public class LocationCopyServiceEJB {

    private static final Logger LOG = LoggerFactory.getLogger(LocationCopyServiceEJB.class);

    private static final String INSTANCE_PK = "instance_pk";
    private static final String DIGEST = "digest";
    private static final String OTHER_ATTRS_DIGEST = "otherAttrsDigest";
    private static final String FILE_SIZE = "fileSize";
    private static final String TRANSFER_SYNTAX = "transferSyntax";
    private static final String TIME_ZONE = "timeZone";
    private static final String DELETE_SOURCE = "deleteSource";
    private static final String LOCATION = "location";
    private static final String SOURCE_LOCATION_PKS_TO_DELETE = "srcLocationPksToDelete";
    private static final String LOCATION_COPY_CONTEXT = "locationCopyContext";

    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    private Device device;

    @Inject
    private ArchiverService archiverService;

    @Inject
    private LocationMgmt locationMgmt;

    @LocationsCopied
    @Inject
    private Event<LocationCopyContext> locationsCopied;

    public void scheduleStudy(LocationCopyContext ctx, String studyIUID, long delay)
            throws IOException {
        LOG.info(
                "Scheduling archiving study={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}",
                studyIUID, ctx.getSourceStorageSystemGroupID(),
                ctx.getTargetStorageSystemGroupID(), ctx.getDeleteSourceLocaton());
        List<String> seriesUIDs = em
                .createQuery(
                        "SELECT se.seriesInstanceUID FROM Series se JOIN se.study st WHERE st.studyInstanceUID = ?1",
                        String.class).setParameter(1, studyIUID).getResultList();
        for (String uid : seriesUIDs) {
            scheduleSeries(ctx, uid, delay);
        }
        LOG.info(
                "Scheduled archiving study={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}",
                new Object[] { studyIUID, ctx.getSourceStorageSystemGroupID(),
                        ctx.getTargetStorageSystemGroupID(), ctx.getDeleteSourceLocaton() });

    }

    public void scheduleSeries(LocationCopyContext ctx, String seriesIUID, long delay)
            throws IOException {
        LOG.info(
                "Scheduling archiving series={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}",
                new Object[] { seriesIUID, ctx.getSourceStorageSystemGroupID(),
                        ctx.getTargetStorageSystemGroupID(), ctx.getDeleteSourceLocaton() });
        List<Instance> insts = em
                .createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID, Instance.class)
                .setParameter(1, seriesIUID).getResultList();
        if (insts.size() > 0) {
            scheduleInstances(ctx, insts, delay);
            LOG.info(
                    "Scheduled archiving {} instances of series={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}",
                    new Object[] { seriesIUID, ctx.getSourceStorageSystemGroupID(),
                            ctx.getTargetStorageSystemGroupID(), ctx.getDeleteSourceLocaton() });
        }
    }

    public void scheduleInstances(LocationCopyContext ctx, List<Instance> insts, long delay)
            throws IOException {
        Instance instance = insts.get(0);
        Attributes attrs = Utils.mergeAndNormalize(instance.getSeries().getStudy().getAttributes(),
                instance.getSeries().getAttributes(), instance.getAttributes());
        String targetName = getTargetName(attrs, ctx.getTargetStorageSystemGroupID());
        scheduleInstances(ctx, insts, targetName, delay);
    }

    public void scheduleInstances(LocationCopyContext ctx, List<Instance> insts, String targetName,
            long delay) throws IOException {
        List<ContainerEntry> entries = new ArrayList<ContainerEntry>(insts.size());
        LocationDeleteContext deleteCtx = ctx.getDeleteSourceLocaton() ? new LocationDeleteContext(
                insts.size()) : null;
        for (Instance inst : filterInstancesAlreadyArchived(insts,
                ctx.getTargetStorageSystemGroupID(), ctx.getDeleteSourceLocaton())) {
            Location selected = (ctx.getSourceStorageSystemGroupID() == null) ? selectBestAvailableLocation(inst)
                    : selectLocationFromStorageGroup(inst, ctx.getSourceStorageSystemGroupID());

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
                    .setProperty(TIME_ZONE, selected.getTimeZone()).setProperty(LOCATION, selected)
                    .build();
            entries.add(entry);

            if (deleteCtx != null) {
                deleteCtx.add(selected.getPk(), inst.getPk());
            }
        }

        if (entries.size() > 0) {
            ArchiverContext archiverCtx = archiverService.createContext(archiverService,
                    ctx.getTargetStorageSystemGroupID(), targetName);
            archiverCtx.setEntries(entries);
            archiverCtx.setProperty(DELETE_SOURCE, new Boolean(ctx.getDeleteSourceLocaton()));
            if (deleteCtx != null) {
                ctx.setProperty(SOURCE_LOCATION_PKS_TO_DELETE, deleteCtx);
            }
            archiverCtx.setProperty(LOCATION_COPY_CONTEXT, ctx);
            archiverService.scheduleStore(archiverCtx, delay);
        }
    }

    public List<Instance> filterInstancesAlreadyArchived(List<Instance> insts,
            String targetGroupID, boolean deleteSource) {
        LocationDeleteContext deleteCtx = deleteSource ? new LocationDeleteContext() : null;
        List<Instance> filtered = new ArrayList<Instance>();
        inst: for (Instance inst : insts) {
            for (Location location : inst.getLocations()) {
                if (location.getStorageSystemGroupID().equals(targetGroupID)) {
                    LOG.info(
                            "{} already archived to Storage System Group {} - skip from archiving",
                            inst, targetGroupID);
                    if (deleteCtx != null) {
                        deleteCtx.add(location.getPk(), inst.getPk());
                    }
                    continue inst;
                }
            }
            filtered.add(inst);
        }
        if (deleteCtx != null && deleteCtx.size() > 0) {
            LOG.debug("Deleting source locations for instances already archived:{}",
                    deleteCtx.getLocationPks());
            deleteLocations(deleteCtx);
        }
        return insts;
    }

    private Location selectLocationFromStorageGroup(Instance inst, String sourceGroupID) {
        for (Location location : inst.getLocations()) {
            String groupID = location.getStorageSystemGroupID();
            if (groupID.equals(sourceGroupID)) {
                return location;
            }
        }
        LOG.info("{} not available at Storage System Group {} - skip from archiving", inst,
                sourceGroupID);
        return null;
    }

    private Location selectBestAvailableLocation(Instance inst) {
        Location selected = null;
        StorageSystemGroup bestGroup = null;
        for (Location location : inst.getLocations()) {
            String groupID = location.getStorageSystemGroupID();
            StorageDeviceExtension stgExt = storageDeviceExtension();
            StorageSystemGroup group = stgExt.getStorageSystemGroup(groupID);
            if (bestGroup == null
                    || bestGroup.getStorageAccessTime() > group.getStorageAccessTime()) {
                bestGroup = group;
                selected = location;
            }
        }
        if (selected == null) {
            LOG.info("Location could not be selected for {} - skip from archiving", inst);
        }
        return selected;
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
        LOG.debug("onContainerEntriesStored for {} called", ctx.getStorageSystemGroupID());

        LocationCopyContext event = (LocationCopyContext) ctx.getProperty(LOCATION_COPY_CONTEXT);

        List<ContainerEntry> entries = ctx.getEntries();
        boolean notInContainer = ctx.isNotInContainer();
        for (ContainerEntry entry : entries) {
            Instance inst = em.find(Instance.class, entry.getProperty(INSTANCE_PK));
            updateStudyAccessTime(inst, ctx.getStorageSystemGroupID());
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
                    .status(ctx.getObjectStatus() != null ? Status.valueOf(ctx.getObjectStatus())
                            : Status.ARCHIVED).build();
            location.addInstance(inst);
            LOG.info("Create {}", location);
            em.persist(location);
            event.addCopy(location);
        }
        em.flush();

        LocationDeleteContext srcLocationPksToDelete = (LocationDeleteContext) ctx
                .getProperty(SOURCE_LOCATION_PKS_TO_DELETE);
        if (srcLocationPksToDelete != null) {
            LOG.info("Source Locations to delete:{}", srcLocationPksToDelete.getLocationPks());
            deleteLocations(srcLocationPksToDelete);
        }

        locationsCopied.fire(event);

        LOG.debug("onContainerEntriesStored for {} finished", ctx.getStorageSystemGroupID());
    }

    private void updateStudyAccessTime(Instance inst, String storageSystemGroupID) {
        Study study = inst.getSeries().getStudy();
        locationMgmt.findOrCreateStudyOnStorageGroup(study, storageSystemGroupID);
    }

    private void deleteLocations(LocationDeleteContext srcLocationToDeleteCtx) {
        List<Location> locations = em
                .createQuery(
                        "SELECT l FROM Location l JOIN FETCH l.instances WHERE l.pk IN :locationPks",
                        Location.class)
                .setParameter("locationPks", srcLocationToDeleteCtx.getLocationPks())
                .getResultList();
        ArrayList<Location> locationToDeletePks = new ArrayList<Location>(locations.size());
        for (Location l : locations) {
            long instPk = srcLocationToDeleteCtx.getInstancePk(l.getPk());
            Instance inst;
            for (Iterator<Instance> it = l.getInstances().iterator(); it.hasNext();) {
                inst = it.next();
                if (inst.getPk() == instPk) {
                    for (Iterator<Location> itL = inst.getLocations().iterator(); itL.hasNext();) {
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
            LOG.debug("Schedule deletion of source Locations:{}", locations);
            try {
                locationMgmt.scheduleDelete(locationToDeletePks, 100, false);
            } catch (Exception x) {
                LOG.error("Schedule deletion of source Locations failed! locations:{}", locations,
                        x);
            }
        }
    }
}
