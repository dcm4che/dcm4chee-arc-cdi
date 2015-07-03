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

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyOnStorageSystemGroup;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@Stateless
public class LocationMgmtEJB implements LocationMgmt {

    private static final Logger LOG = LoggerFactory
            .getLogger(LocationMgmtEJB.class);

    @Inject
    private Device device;

    @Inject
    private StorageService storageService;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName = "java:/queue/delete")
    private Queue deleteQueue;

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Override
    public void scheduleDelete(Collection<Location> refs, int delay)
            throws Exception {
        ArrayList<Long> refPks = new ArrayList<Long>(refs.size());
        for (Location ref : refs)
            refPks.add(ref.getPk());
        scheduleDeleteByPks(refPks, delay);
    }

    @Override
    public void scheduleDeleteByPks(Collection<Long> refPks, int delay)
            throws Exception {
        try {
            Connection conn = connFactory.createConnection();
            try {
                Session session = conn.createSession(false,
                        Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(deleteQueue);
                ObjectMessage msg = session
                        .createObjectMessage((Serializable) refPks);
                if (delay > 0)
                    msg.setLongProperty("_HQ_SCHED_DELIVERY",
                            System.currentTimeMillis() + delay);
                producer.send(msg);
            } finally {
                conn.close();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void failDelete(Location ref) {
        ref.setStatus(Location.Status.DELETE_FAILED);
        LOG.warn(
                "Failed to delete file {}, setting file reference status to {}",
                ref.getStoragePath(), ref.getStatus());
    }

    @Override
    public boolean doDelete(Location ref) {
        if (ref.getInstances().size() > 0) {
            LOG.warn(
                    "Deletion failed! Location {} is still referenced by instances:{}",
                    ref, ref.getInstances());
            return false;
        }
        try {
            StorageDeviceExtension ext = device
                    .getDeviceExtensionNotNull(StorageDeviceExtension.class);
            StorageSystem storageSystem = ext.getStorageSystem(
                    ref.getStorageSystemGroupID(), ref.getStorageSystemID());
            StorageContext cxt = storageService
                    .createStorageContext(storageSystem);
            storageService.deleteObject(cxt, ref.getStoragePath());
        } catch (IOException e) {
            return false;
        }
        removeDeadFileRef(ref);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int doDelete(Collection<Long> refPks) {
        LOG.debug("Called doDelete refPks:{}", refPks);
        if (refPks == null || refPks.isEmpty())
            return 0;
        int count = 0;
        Query query = em.createQuery(
                "SELECT l FROM Location l WHERE l.pk IN :pks", Location.class);
        query.setParameter("pks", refPks);
        List<Location> result = query.getResultList();
        HashMap<String, Location> containerLocations = new HashMap<String, Location>();
        Location ref;
        count += result.size();
        for (int i = 0, len = result.size(); i < len; i++) {
            ref = result.get(i);
            if (ref.getEntryName() == null) {
                LOG.debug("Location is not in a container, we can delete stored object and entity!");
                if (!doDelete(ref)) {
                    LOG.warn(
                            "Deletion of {} failed! Mark Location as DELETION_FAILED",
                            ref);
                    failDelete(ref);
                    count--;
                }
            } else {
                LOG.debug("Location is in a container, we can not delete the container at this moment!");
                String key = ref.getStorageSystemID() + "_&_"
                        + ref.getStoragePath();
                if (containerLocations.containsKey(key)) {
                    removeDeadFileRef(ref);
                } else {
                    containerLocations.put(key, ref);
                    count--;
                }
            }
        }
        if (containerLocations.size() > 0) {
            count += deleteContainer(containerLocations);
        }
        return count;
    }

    @Override
    public Location getLocation(Long pk) {
        return em.find(Location.class, pk);
    }


    @Override
    public void findOrCreateStudyOnStorageGroup(String studyUID, String groupID) {
        Study study = findStudy(studyUID);
        if(study != null)
        findOrCreateStudyOnStorageGroup(study, groupID);
    }

    @Override
    public void findOrCreateStudyOnStorageGroup(Study study, String groupID) {
        try {
            String studyUID = study.getStudyInstanceUID();
            StudyOnStorageSystemGroup studyOnStgSysGrp = em
                    .createNamedQuery(
                            StudyOnStorageSystemGroup.FIND_BY_STUDY_INSTANCE_UID_AND_GRP_UID,
                            StudyOnStorageSystemGroup.class)
                    .setParameter(1, studyUID).setParameter(2, groupID)
                    .getSingleResult();
            studyOnStgSysGrp
                    .setAccessTime(new Date(System.currentTimeMillis()));
            studyOnStgSysGrp.setMarkedForDeletion(false);
        } catch (NoResultException e) {
            StudyOnStorageSystemGroup studyOnStgSysGrp = new StudyOnStorageSystemGroup();
            studyOnStgSysGrp.setStudy(study);
            studyOnStgSysGrp.setStorageSystemGroupID(groupID);
            studyOnStgSysGrp.setMarkedForDeletion(false);
            studyOnStgSysGrp
                    .setAccessTime(new Date(System.currentTimeMillis()));
            em.persist(studyOnStgSysGrp);
        }
    }

    public List<Instance> findInstancesDueDelete(int studyRetention, 
            String studyRetentionUnit, String groupID) {
        Timestamp studyDueDate = getStudyDueDate(studyRetention, studyRetentionUnit);
        List<Instance> locationsToDelete;
        Query query = em.createNamedQuery(StudyOnStorageSystemGroup.FIND_INSTANCES_DUE_DELETE);
        query.setParameter(1, groupID).setParameter("dueDate", studyDueDate);
        locationsToDelete = query.getResultList();
        return locationsToDelete == null ? new ArrayList<Instance>() : locationsToDelete;
    }

    private Timestamp getStudyDueDate(int studyRetention, String studyRetentionUnit) {
        Timestamp dueDate;
        long now = System.currentTimeMillis();
        switch (TimeUnit.valueOf(studyRetentionUnit)) {
          case DAYS:
              dueDate = new Timestamp(now - 86400000 * studyRetention);
            break;
          case HOURS:
              dueDate = new Timestamp(now - 3600000 * studyRetention);
            break;
          case MINUTES:
              dueDate = new Timestamp(now - 60000 * studyRetention);
            break;
          default:
              dueDate = new Timestamp(now - 86400000 * studyRetention);
            break;
        }
        return dueDate;
    }

    private void removeDeadFileRef(Location ref) {

        try {
            em.remove(ref);
        } catch (Exception e) {
            LOG.error("Failed to remove File Ref {}", ref.toString());
        }
    }

    private Study findStudy(String studyUID) {
        Study study = null;
        try {
            study = em.createNamedQuery(
                    Study.FIND_BY_STUDY_INSTANCE_UID_EAGER,
                    Study.class).setParameter(1, studyUID)
                    .getSingleResult();
        }
        catch(NoResultException e) {
            LOG.error("Unable to find study {} - failure reason {}",
                    studyUID, e);
        }
        return study;
    }

    @SuppressWarnings("unchecked")
    private int deleteContainer(HashMap<String, Location> containerLocations) {
        List<Location> result;
        int count = 0;
        Query queryRemaining = em
                .createQuery(
                        "SELECT l from Location l WHERE l.storageSystemGroupID = :storageSystemGroupID"
                                + " AND l.storageSystemID = :storageID AND l.storagePath = :storagePath",
                        Location.class);
        for (Location l : containerLocations.values()) {
            queryRemaining.setParameter("storageSystemGroupID",
                    l.getStorageSystemGroupID());
            queryRemaining.setParameter("storageID", l.getStorageSystemID());
            queryRemaining.setParameter("storagePath", l.getStoragePath());
            result = queryRemaining.getResultList();
            LOG.debug("Remaining Locations for container: {}", result);
            if (result.size() == 1 && result.get(0).getPk() == l.getPk()) {
                LOG.debug(
                        "Only one Location (from the delete request) reference the container {}. We can delete the container",
                        queryRemaining.getParameters());
                if (doDelete(l)) {
                    count++;
                } else {
                    LOG.warn(
                            "Deletion of {} failed! mark Location as DELETION_FAILED",
                            l);
                    failDelete(l);
                }
            } else {
                LOG.debug("Container is still referenced by other Locations. Deletion skipped!");
                removeDeadFileRef(l);
                count++;
            }
        }
        return count;
    }

}
