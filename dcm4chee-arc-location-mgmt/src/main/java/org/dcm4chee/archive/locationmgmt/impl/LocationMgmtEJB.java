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
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QStudyOnStorageSystemGroup;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyOnStorageSystemGroup;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.storage.ObjectNotFoundException;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.dcm4chee.storage.conf.StorageSystemStatus;
import org.dcm4chee.storage.service.StorageService;
import org.dcm4chee.storage.spi.StorageSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;

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

    @Inject
    private javax.enterprise.inject.Instance<StorageSystemProvider> storageSystemProviders;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connFactory;

    @Resource(mappedName = "java:/queue/delete")
    private Queue deleteQueue;

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Override
    public void scheduleDelete(Collection<Location> refs, int delay,
            boolean checkStudyMarked) throws JMSException {
        List<Long> refPks = new ArrayList<Long>(refs.size());
        
        for (Location ref : refs) {
            refPks.add(ref.getPk());
        }
        scheduleDeleteByPks(refPks, delay, checkStudyMarked);
    }


    @Override
    public void scheduleDeleteByPks(Collection<Long> refPks, int delay, boolean checkStudyMarked)
            throws JMSException {
        if (refPks!=null && refPks.size()>0) {
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
                    msg.setBooleanProperty("checkStudyMarked", checkStudyMarked);
                    producer.send(msg);
                } finally {
                    conn.close();
                }
            } catch (JMSException e) {
                throw e;
            }
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
        if (!ref.getInstances().isEmpty()) {
            LOG.warn(
                    "Deletion failed! Location {} is still referenced by instances:{}",
                    ref, ref.getInstances());
            return false;
        }
        StorageDeviceExtension ext = null;
        try {
            ext = device
                    .getDeviceExtensionNotNull(StorageDeviceExtension.class);
            StorageSystem storageSystem = ext.getStorageSystem(
                    ref.getStorageSystemGroupID(), ref.getStorageSystemID());
            StorageContext cxt = storageService
                    .createStorageContext(storageSystem);
            storageService.deleteObject(cxt, ref.getStoragePath());
        }
        catch(ObjectNotFoundException e1) {
            LOG.error("Couldn't find the file to delete {} on file system"
                    + " will delete location safely - reason {}", ref, e1);
        }
        catch (IOException e) {
            LOG.error("Error deleting location {} - reason {}", ref, e);
            return false;
        }
        removeDeadFileRef(ref);
        try {
            unflagDirtySystemsCleaned(ext.getStorageSystemGroup(ref
                    .getStorageSystemGroupID()));
        } catch (IOException e) {
            LOG.debug("Failed to restore systems emergently flagged on group {} - reason {}"
                    , ref.getStorageSystemGroupID(), e);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int doDelete(Collection<Long> refPks, boolean checkStudyMarked) {
        LOG.debug("Called doDelete refPks:{}", refPks);
        if (refPks == null || refPks.isEmpty())
            return 0;
        int count = 0;
        Query query = em.createQuery(
                "SELECT l FROM Location l WHERE l.pk IN :pks", Location.class);
        query.setParameter("pks", refPks);
        List<Location> result = query.getResultList();
        List<Location> locations = checkStudyMarked 
                ? filterForMarkedStudies(result) : result;
        Map<String, Location> containerLocations = new HashMap<String, Location>();
        Set<Series> seriesToPurge = new HashSet<Series>();
        Location ref;
        count += locations.size();
        for (int i = 0, len = locations.size(); i < len; i++) {
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
            for(Instance inst : ref.getInstances()) {
                if(inst.getSeries().isRejected())
                    seriesToPurge.add(inst.getSeries());
            }
        }
        if (containerLocations.isEmpty()) {
            count += deleteContainer(containerLocations);
        }
        
        for(Series series : seriesToPurge) {
            try{
            em.remove(series);
            }
            catch(Exception e) {
                LOG.error("Unable to remove purged series {} - reason {}", series, e);
            }
        }
        return count;
    }

    @Override
    public Location getLocation(Long pk) {
        Query query = em.createQuery("SELECT l from Location l left join fetch l.instances where l.pk = ?1");
        query.setParameter(1, pk);
        Location l = (Location) query.getSingleResult();
        return em.merge(l);
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
            StudyOnStorageSystemGroup studyOnStgSysGrp = findStudyOnStorageGroup(studyUID, groupID);
            studyOnStgSysGrp
                    .setAccessTime(new Date(System.currentTimeMillis()));
            studyOnStgSysGrp.setMarkedForDeletion(false);
        } catch (NoResultException e) {
            LOG.debug("Error retrieving StudyOnStorageGroup entry - "
                    + "reason {}, creating new entry", e);
            StudyOnStorageSystemGroup studyOnStgSysGrp = new StudyOnStorageSystemGroup();
            studyOnStgSysGrp.setStudy(study);
            studyOnStgSysGrp.setStorageSystemGroupID(groupID);
            studyOnStgSysGrp.setMarkedForDeletion(false);
            studyOnStgSysGrp
                    .setAccessTime(new Date(System.currentTimeMillis()));
            em.persist(studyOnStgSysGrp);
        }
    }

    @Override
    public List<Instance> findInstancesDueDelete(int studyRetention, 
            String studyRetentionUnit, String groupID, String studyInstanceUID, String seriesInstanceUID) {
        Timestamp studyDueDate = getStudyDueDate(studyRetention, studyRetentionUnit);
        JPAQuery query = new JPAQuery(em);
        
        query.from(QInstance.instance).from(QStudyOnStorageSystemGroup.studyOnStorageSystemGroup)
        .leftJoin(QInstance.instance.locations).fetch()
        .join(QInstance.instance.series)
        .join(QInstance.instance.series.study);
        
        query.where(QStudyOnStorageSystemGroup.studyOnStorageSystemGroup.markedForDeletion.isFalse())
        .where(QStudyOnStorageSystemGroup.studyOnStorageSystemGroup.accessTime.before(studyDueDate))
        .where(QStudyOnStorageSystemGroup.studyOnStorageSystemGroup.storageSystemGroupID.eq(groupID))
        .where(QStudyOnStorageSystemGroup.studyOnStorageSystemGroup.study.studyInstanceUID
                .eq(QInstance.instance.series.study.studyInstanceUID))
                .where(QInstance.instance.rejectionNoteCode.isNull());
        if(studyInstanceUID != null)
        query.where(QInstance.instance.series.study.studyInstanceUID.eq(studyInstanceUID));
        if(seriesInstanceUID != null)
        query.where(QInstance.instance.series.seriesInstanceUID.eq(seriesInstanceUID));
        
        query.orderBy(QStudyOnStorageSystemGroup.studyOnStorageSystemGroup.accessTime.asc());
        List<Tuple> tuples = query.list(QInstance.instance, QStudyOnStorageSystemGroup.studyOnStorageSystemGroup);
        List<Instance> locationsToDelete = new ArrayList<>();
        for(Tuple tuple: tuples) {
            if(!locationsToDelete.contains(tuple.get(QInstance.instance)))
            locationsToDelete.add(tuple.get(QInstance.instance));
        }
        return locationsToDelete;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Location> findFailedToDeleteLocations(String groupID) {
        Query query = em.createQuery("SELECT l FROM Location l WHERE "
                + "l.storageSystemGroupID = ?1 AND l.status = 1"
                , Location.class);
        query.setParameter(1, groupID);
        return query.getResultList();
    }

    @Override
    public long calculateDataVolumePerDayInBytes(String groupID,int dvdAverageOnNDays) {
        Date nDaysAgo = new Date(System.currentTimeMillis() - 
                (dvdAverageOnNDays * 1000 * 60 * 60 * 24));
        Query query = em.createNamedQuery(Location.CALCULATE_SUM_DATA_VOLUME_PER_DAY);
        query.setParameter(1,groupID).setParameter(2, nDaysAgo);
        Long result = (Long) query.getSingleResult();
        if(result == null)
            return 0L;
        return result/dvdAverageOnNDays;
    }

    @Override
    public boolean isMarkedForDelete(String studyInstanceUID, String groupID) {
        Query query = em.createNamedQuery(StudyOnStorageSystemGroup
                .FIND_BY_STUDY_INSTANCE_UID_AND_GRP_UID_MARKED);
        query.setParameter(1, studyInstanceUID).setParameter(2, groupID);
        try{
            query.getSingleResult();
            return true;
        }
        catch (NoResultException e) {
            LOG.debug("Unable to find marked study - reason {}", e);
            return false;
        }
    }

    @Override
    public void markForDeletion(String studyInstanceUID, String groupID) throws NoResultException{
            StudyOnStorageSystemGroup studyOnStgSysGrp = findStudyOnStorageGroup(studyInstanceUID, groupID);
            studyOnStgSysGrp
                    .setAccessTime(new Date(System.currentTimeMillis()));
            studyOnStgSysGrp.setMarkedForDeletion(true);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<Location> detachInstanceOnGroup(long instancePK
            , String groupID) {
        Instance inst  = em.find(Instance.class, instancePK);
        List<Location> locationsOnGroup = new ArrayList<>(); 
        for(Iterator<Location> iter = inst.getLocations().iterator(); iter.hasNext();){
            Location loc = iter.next();
            loc = em.find(Location.class, loc.getPk());
            if(loc.getStorageSystemGroupID().compareTo(groupID) == 0){
                locationsOnGroup.add(loc);
                for(Iterator<Instance> iterInst = loc.getInstances().iterator(); iterInst.hasNext();){
                  iterInst.next();
                  iterInst.remove();
                }
                iter.remove();
            }
        }
        em.flush();
        return locationsOnGroup;
    }

    private StudyOnStorageSystemGroup findStudyOnStorageGroup(
            String studyInstanceUID, String groupID) throws NoResultException{
        return em
                .createNamedQuery(
                        StudyOnStorageSystemGroup.FIND_BY_STUDY_INSTANCE_UID_AND_GRP_UID,
                        StudyOnStorageSystemGroup.class)
                .setParameter(1, studyInstanceUID).setParameter(2, groupID)
                .getSingleResult();
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
            LOG.error("Failed to remove File Ref {} - reason {}", ref.toString(), e);
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
    private int deleteContainer(Map<String, Location> containerLocations) {
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

    private List<Location> filterForMarkedStudies(
            List<Location> refs) {
        List<Location> filteredLocations = new ArrayList<Location>();
        for (Location loc : refs) {
            int markedCnt = 0;
            for (Instance inst : loc.getInstances())
                if (isMarkedForDelete(inst.getSeries().getStudy()
                        .getStudyInstanceUID(), loc.getStorageSystemGroupID())) {
                    markedCnt++;
                }
            if (markedCnt == loc.getInstances().size())
                filteredLocations.add(loc);
        }
        return filteredLocations;
    }

    private void unflagDirtySystemsCleaned(StorageSystemGroup group) throws IOException{
        for(String systemID : group.getStorageSystems().keySet()) {
            StorageSystem system = group.getStorageSystem(systemID);
            StorageSystemProvider provider = system
                    .getStorageSystemProvider(storageSystemProviders);
            if(system.getMinFreeSpaceInBytes() == -1L)
                system.setMinFreeSpaceInBytes(provider.getTotalSpace()
                        * Integer.parseInt(system.getMinFreeSpace()
                                .replace("%", ""))/100);
            if(provider.getUsableSpace() 
                    > system.getMinFreeSpaceInBytes()) {
                system.setStorageSystemStatus(StorageSystemStatus.OK);
                system.getStorageDeviceExtension().setDirty(false);
            LOG.info("System {} is now ready for use, emergency condition passed", system);
            }
        }
    }

}
