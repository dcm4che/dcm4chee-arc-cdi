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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.storage.ContainerEntry;
import org.dcm4chee.storage.RetrieveContext;
import org.dcm4chee.storage.archiver.service.ArchiverContext;
import org.dcm4chee.storage.archiver.service.ArchiverService;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.dcm4chee.storage.service.RetrieveService;
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

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @Inject
    private Device device;

    @Inject
    private CodeService codeService;

    @Inject
    private ArchiverService archiverService;

    @Inject
    private RetrieveService retrieveService;

    public void onStoreInstance(StoreContext storeContext,
            ArchivingRule archivingRule) {
        Attributes attrs = storeContext.getAttributes();
        String seriesInstanceUID = attrs.getString(Tag.SeriesInstanceUID);
        Date archivingTime = new Date(System.currentTimeMillis()
                + archivingRule.getDelayAfterInstanceStored() * 1000L);
        ArchivingTask task;
        try {
            task = em.createNamedQuery(
                    ArchivingTask.FIND_BY_SERIES_INSTANCE_UID, ArchivingTask.class)
                    .setParameter(1, seriesInstanceUID)
                    .getSingleResult();
            task.setArchivingTime(archivingTime);
            LOG.debug("Updates {}", task);
        } catch (NoResultException nre) {
            task = new ArchivingTask();
            task.setSeriesInstanceUID(seriesInstanceUID);
            task.setArchivingTime(archivingTime);
            task.setSourceStorageSystemGroupID(storeContext.getFileRef().getStorageSystemGroupID());
            task.setTargetStorageSystemGroupID(archivingRule.getStorageSystemGroupID());
            task.setTargetName(getTargetName(archivingRule, attrs));
            Code delayReasonCode = archivingRule.getDelayReasonCode();
            if (delayReasonCode != null)
                task.setDelayReasonCode(codeService.findOrCreate(delayReasonCode));
            em.persist(task);
            LOG.info("Create {}", task);
        }
    }

	private String getTargetName(ArchivingRule archivingRule, Attributes attrs) {
		StorageSystemGroup grp = storageDeviceExtension().getStorageSystemGroup(archivingRule.getStorageSystemGroupID());
		String pattern = "";
		return AttributesFormat.valueOf(pattern).format(attrs);
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
                .createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID,
                        Instance.class)
                .setParameter(1, task.getSeriesInstanceUID())
                .getResultList();
        scheduleInstances(insts, task.getSourceStorageSystemGroupID(), task.getTargetStorageSystemGroupID(), task.getTargetName(), false);
        LOG.info("Scheduled {}", task);
    }
    
    public void scheduleStudy(String studyIUID, String sourceGroupID, String targetGroupID, String targetName, boolean deleteSource) throws IOException {
        LOG.info("Scheduling archiving study={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}", 
        		studyIUID, sourceGroupID, targetGroupID, deleteSource);
        List<String> seriesUIDs = em.createQuery("SELECT se.seriesInstanceUID FROM Series se JOIN FETCH se.study st WHERE st.studyInstanceUID = ?1",
                        String.class)
                .setParameter(1, studyIUID)
                .getResultList();
        for (String uid : seriesUIDs) {
        	scheduleSeries(uid, sourceGroupID, targetGroupID, targetName, deleteSource);
        }
        LOG.info("Scheduled archiving study={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}", 
        		new Object[] {studyIUID, sourceGroupID, targetGroupID, deleteSource});
    	
    }
    
    public void scheduleSeries(String seriesIUID, String sourceGroupID, String targetGroupID, String targetName, boolean deleteSource) throws IOException {
        LOG.info("Scheduling archiving series={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}", 
        		new Object[] {seriesIUID, sourceGroupID, targetGroupID, deleteSource});
        List<Instance> insts = em
                .createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID,
                        Instance.class)
                .setParameter(1, seriesIUID)
                .getResultList();
        scheduleInstances(insts, sourceGroupID, targetGroupID, targetName, deleteSource);
        LOG.info("Scheduled archiving series={}, sourceStorageGroupID={}, targetStorageGroupID={}, deleteSource={}", 
        		new Object[] {seriesIUID, sourceGroupID, targetGroupID, deleteSource});
    	
    }
    
    public void scheduleInstances(List<Instance> insts, String sourceGroupID, String targetGroupID, String targetName, boolean deleteSource) throws IOException {
        ArrayList<ContainerEntry> entries =
                new ArrayList<ContainerEntry>(insts.size());
        for (Instance inst : insts) {
            Location location = selectLocation(inst, sourceGroupID, targetGroupID);
            if (location != null) {
                StorageSystem storageSystem =
                        storageDeviceExtension().getStorageSystem(
                                location.getStorageSystemGroupID(),
                                location.getStorageSystemID());
                RetrieveContext ctx = retrieveService.createRetrieveContext(storageSystem);
                Path file = retrieveService.getFile(ctx, location.getStoragePath());
                ContainerEntry entry = new ContainerEntry(
                        inst.getSopInstanceUID(),
                        file,
                        location.getDigest());
                entry.setProperty(INSTANCE_PK, inst.getPk());
                entry.setProperty(DIGEST, location.getDigest());
                entry.setProperty(OTHER_ATTRS_DIGEST, location.getOtherAttsDigest());
                entry.setProperty(FILE_SIZE, location.getSize());
                entry.setProperty(TRANSFER_SYNTAX, location.getTransferSyntaxUID());
                entry.setProperty(TIME_ZONE, location.getTimeZone());
                entries.add(entry);
            }
        }
        String digestAlgorithm = null;
        if (digestAlgorithmSet.size() == 1) {
            digestAlgorithm = digestAlgorithmSet.iterator().next();
        } else {
            LOG.warn("No common digest Algorithm of entries: {} - cannot verify container",
                    digestAlgorithmSet);
        }
        ArchiverContext ctx = archiverService.createContext(
        		targetGroupID,
                targetName,
                digestAlgorithm);
        ctx.setEntries(entries);
        ctx.setProperty(DELETE_SOURCE, new Boolean(deleteSource));
        archiverService.scheduleStore(ctx);
    }

    private StorageDeviceExtension storageDeviceExtension() {
        return device.getDeviceExtension(StorageDeviceExtension.class);
    }

    private Location selectLocation(Instance inst, String sourceGroupID, String targetGroupID) {
        Location selected = null;
        Collection<Location> locations = inst.getLocations();
        for (Location location : locations) {
            String storageSystemGroupID = location.getStorageSystemGroupID();
            if (storageSystemGroupID.equals(targetGroupID)) {
                LOG.info("{} already archived to Storage System Group {} - skip from archiving",
                        inst, storageSystemGroupID);
                return null;
            }
            if (storageSystemGroupID.equals(sourceGroupID))
                selected = location;
        }
        if (selected == null)
            LOG.info("{} not available at Storage System Group {} - skip from archiving",
                    inst, sourceGroupID);
        return selected;
    }

    public void onContainerEntriesStored(ArchiverContext ctx) {
        List<ContainerEntry> entries = ctx.getEntries();
        for (ContainerEntry entry : entries) {
            Instance inst = em.find(Instance.class, entry.getProperty(INSTANCE_PK));
            Location location = new Location.Builder()
                .storageSystemGroupID(ctx.getGroupID())
                .storageSystemID(ctx.getStorageSystemID())
                .storagePath(ctx.getName())
                .entryName(entry.getName())
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
    }

}
