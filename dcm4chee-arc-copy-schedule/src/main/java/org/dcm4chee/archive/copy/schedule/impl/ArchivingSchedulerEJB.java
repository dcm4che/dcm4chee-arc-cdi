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

package org.dcm4chee.archive.copy.schedule.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.ArchivingRule;
import org.dcm4chee.archive.dto.ActiveService;
import org.dcm4chee.archive.entity.ArchivingTask;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.hsm.LocationCopyContext;
import org.dcm4chee.archive.hsm.LocationCopyService;
import org.dcm4chee.archive.processing.ActiveProcessingService;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.remember.StoreAndRememberContext;
import org.dcm4chee.archive.store.remember.StoreAndRememberService;
import org.dcm4chee.archive.store.verify.StoreVerifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Stateless
public class ArchivingSchedulerEJB {

    private static final Logger LOG = LoggerFactory.getLogger(ArchivingSchedulerEJB.class);

    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    private LocationCopyService locationCopyService;

    @Inject
    private StoreAndRememberService storeAndRemeberService;

    @Inject
    private CodeService codeService;

    @Inject
    private ActiveProcessingService activeProcessingService;
    
    @Inject
    private Device device;
    
    @Inject
    private DicomConfiguration conf;

    public void onStoreInstance(StoreContext storeContext, ArchivingRule archivingRule) {
        Attributes attrs = storeContext.getAttributes();
        String seriesInstanceUID = attrs.getString(Tag.SeriesInstanceUID);
        Date archivingTime = new Date(System.currentTimeMillis()
                + archivingRule.getDelayAfterInstanceStored() * 1000L);
        List<ArchivingTask> alreadyScheduledTasks = em
                .createNamedQuery(ArchivingTask.FIND_BY_SERIES_INSTANCE_UID, ArchivingTask.class)
                .setParameter(1, seriesInstanceUID).getResultList();

        List<String> storageGroupTargets = new ArrayList<String>(
                archivingRule.getStorageSystemGroupIDs().length);
        for (String targetSystemGroupID : archivingRule.getStorageSystemGroupIDs()) {
            storageGroupTargets.add(targetSystemGroupID);
        }

        storageGroupTargets = filterAlreadyScheduledStorageGroupTargets(alreadyScheduledTasks,
                storageGroupTargets, seriesInstanceUID, archivingTime);

        for (String targetGroupID : storageGroupTargets) {
            createAndPersistStorageGroupArchivingTask(seriesInstanceUID, archivingTime,
                    storeContext.getFileRef().getStorageSystemGroupID(),
                    archivingRule.getDelayReasonCode(), targetGroupID);
        }

        List<String> extDeviceTargets = new ArrayList<String>(
                archivingRule.getExternalSystemsDeviceName().length);
        for (String targetExtDevice : archivingRule.getExternalSystemsDeviceName()) {
            extDeviceTargets.add(targetExtDevice);
        }

        extDeviceTargets = filterAlreadyScheduledExtDeviceTargets(alreadyScheduledTasks,
                extDeviceTargets, seriesInstanceUID, archivingTime);

        for (String extDeviceTarget : extDeviceTargets) {
            createAndPersistExtDeviceArchivingTask(seriesInstanceUID, archivingTime, storeContext
                    .getFileRef().getStorageSystemGroupID(), archivingRule.getDelayReasonCode(),
                    extDeviceTarget);
        }
    }

    private List<String> filterAlreadyScheduledStorageGroupTargets(
            List<ArchivingTask> alreadyScheduledTasks, List<String> extStorageGroupTargets,
            String seriesInstanceUID, Date archivingTime) {
        for (ArchivingTask task : alreadyScheduledTasks) {
            task.setArchivingTime(archivingTime);
            LOG.debug("Updates {}", task);
            if (extStorageGroupTargets.remove(task.getTargetStorageSystemGroupID())) {
                LOG.debug("Target StorageSystemGroup {} already scheduled!",
                        task.getTargetStorageSystemGroupID());
            }
        }

        return extStorageGroupTargets;
    }

    private List<String> filterAlreadyScheduledExtDeviceTargets(
            List<ArchivingTask> alreadyScheduledTasks, List<String> extDeviceTargets,
            String seriesInstanceUID, Date archivingTime) {

        for (ArchivingTask task : alreadyScheduledTasks) {
            task.setArchivingTime(archivingTime);
            LOG.debug("Updates {}", task);
            if (extDeviceTargets.remove(task.getTargetExternalDevice())) {
                LOG.debug("Target External Device {} already scheduled!",
                        task.getTargetExternalDevice());
            }
        }

        return extDeviceTargets;
    }

    private void createAndPersistStorageGroupArchivingTask(String seriesInstanceUID,
            Date archivingTime, String sourceStorageGroupID, Code delayReasonCode,
            String targetStorageGroupID) {
        ArchivingTask task = new ArchivingTask();
        task.setSeriesInstanceUID(seriesInstanceUID);
        task.setArchivingTime(archivingTime);
        task.setSourceStorageSystemGroupID(sourceStorageGroupID);
        task.setTargetStorageSystemGroupID(targetStorageGroupID);
        if (delayReasonCode != null)
            task.setDelayReasonCode(codeService.findOrCreate(delayReasonCode));
        flagOrUnflagSeriesAsActiveProcess(seriesInstanceUID, ActiveService.LOCAL_ARCHIVING, true);
        em.persist(task);
        LOG.info("Create {}", task);
    }

    private void createAndPersistExtDeviceArchivingTask(String seriesInstanceUID,
            Date archivingTime, String sourceStorageGroupID, Code delayReasonCode,
            String targetExtDevice) {
        ArchivingTask task = new ArchivingTask();
        task.setSeriesInstanceUID(seriesInstanceUID);
        task.setArchivingTime(archivingTime);
        task.setSourceStorageSystemGroupID(sourceStorageGroupID);
        task.setTargetExternalDevice(targetExtDevice);
        if (delayReasonCode != null)
            task.setDelayReasonCode(codeService.findOrCreate(delayReasonCode));
        flagOrUnflagSeriesAsActiveProcess(seriesInstanceUID, ActiveService.STORE_REMEMBER_ARCHIVING, true);
        em.persist(task);
        LOG.info("Create {}", task);
    }

    public ArchivingTask scheduleNextArchivingTask() throws IOException {
        List<ArchivingTask> results = em
                .createNamedQuery(ArchivingTask.FIND_READY_TO_ARCHIVE, ArchivingTask.class)
                .setMaxResults(1).getResultList();
        if (results.isEmpty()) {
            return null;
        }

        ArchivingTask task = results.get(0);
        LOG.info("Scheduling {}", task);
        if (task.getTargetStorageSystemGroupID() != null) {
            scheduleCopyToHsm(task);
        } else if (task.getTargetExternalDevice() != null) {
            scheduleStoreAndRemember(task);
        } else {
            throw new IllegalStateException("Invalid archiving task");
        }

        LOG.info("Scheduled {}", task);
        flagOrUnflagSeriesAsActiveProcess(task.getSeriesInstanceUID(), 
                task.getTargetStorageSystemGroupID() != null? ActiveService.LOCAL_ARCHIVING 
                        : ActiveService.STORE_REMEMBER_ARCHIVING, false);
        em.remove(task);
        return task;
    }

    private void scheduleCopyToHsm(ArchivingTask task) throws IOException {
        LocationCopyContext ctx = locationCopyService.createContext(task
                .getTargetStorageSystemGroupID());
        ctx.setSourceStorageSystemGroupID(task.getSourceStorageSystemGroupID());
        locationCopyService.scheduleCopySeries(ctx, task.getSeriesInstanceUID(), 0);
    }

    private void scheduleStoreAndRemember(ArchivingTask task) throws IOException {
        String extDevice = task.getTargetExternalDevice();

        String remoteAE = determineRemoteAETitle(extDevice);
        if (remoteAE == null) {
            throw new IOException("Could not determine remote AE title for Store-and-Remember "
                    + "task to external device " + extDevice);
        }

        StoreAndRememberContext storeRememberCxt = storeAndRemeberService.createContextBuilder()
                .seriesUID(task.getSeriesInstanceUID())
                .externalDeviceName(extDevice).remoteAE(remoteAE)
                .storeVerifyProtocol(StoreVerifyService.STORE_VERIFY_PROTOCOL.AUTO).build();
        storeAndRemeberService.scheduleStoreAndRemember(storeRememberCxt, 0);
    }

    private String determineRemoteAETitle(String extDeviceName) {
        String remoteAE = null;
        try {
            //TODO: find smarter way which AE of external device should be used for Store&Remember
            Device extDevice = conf.findDevice(extDeviceName);
            Collection<ApplicationEntity> aes = extDevice.getApplicationEntities();
            remoteAE = aes.iterator().next().getAETitle();
        } catch(Exception e) {
        }
        
        return remoteAE;
    }

    private void flagOrUnflagSeriesAsActiveProcess(String seriesInstanceUID, ActiveService service, boolean flag) {
        Query query = em.createNamedQuery(Series.FIND_BY_SERIES_INSTANCE_UID_EAGER);
        query.setParameter(1, seriesInstanceUID);
        Series series = (Series) query.getSingleResult();
        for(Instance inst : series.getInstances()) {
            if(flag)
                activeProcessingService.addActiveProcess(series.getStudy()
                        .getStudyInstanceUID(), seriesInstanceUID,
                        inst.getSopInstanceUID(), service);
            else
                activeProcessingService
                .deleteActiveProcessBySOPInstanceUIDandService(
                        inst.getSopInstanceUID(), service);
        }
    }

}