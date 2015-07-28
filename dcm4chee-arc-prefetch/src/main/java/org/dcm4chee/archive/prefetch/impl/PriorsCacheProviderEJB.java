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
package org.dcm4chee.archive.prefetch.impl;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.archive.prefetch.Fetched;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.StorageSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Steve Kroetsch<stevekroetsch@hotmail.com>
 *
 */
@Stateless
public class PriorsCacheProviderEJB {

    static Logger LOG = LoggerFactory.getLogger(PriorsCacheProviderEJB.class);

    @Inject
    private Device device;

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    private LocationMgmt locationMgmt;

    @Inject
    @Fetched
    private Event<Location> fetched;

    public void register(StorageSystem sourceStorageSystem,
            String sourceStoragePath, StorageSystem targetStorageSystem,
            String targetStoragePath, String iuid, Availability availability) {
        String sourceGroupID = sourceStorageSystem.getStorageSystemGroup().getGroupID();
        String targetGroupID = targetStorageSystem.getStorageSystemGroup().getGroupID();
        if (sourceGroupID.equals(targetGroupID))
            throw new IllegalArgumentException(
                    "Source and target Storage System Groups are the same ("
                            + sourceGroupID + ")");

        Instance inst = findInstance(iuid);
        if (inst == null) {
            LOG.warn("Instance {} not found - may have been deleted", iuid);
            return;
        }

        String sourceSystemID = sourceStorageSystem.getStorageSystemID();
        String targetSystemID = targetStorageSystem.getStorageSystemID();
        Location sourceLocation = null;
        Collection<Location> duplicates = new ArrayList<Location>();
        for (Iterator<Location> iter = inst.getLocations().iterator(); iter.hasNext();) {
            Location location = iter.next();
            if (location.getStorageSystemGroupID().equals(sourceGroupID)
                    && location.getStorageSystemID().equals(sourceSystemID)
                    && location.getStoragePath().equals(sourceStoragePath)) {
                sourceLocation = location;
            }

            if (location.getStorageSystemGroupID().equals(targetGroupID)) {
                LOG.info(
                        "{} already cached to Storage System Group {} - schedule for delete",
                        location, targetGroupID);
                duplicates.add(location);
                location.getInstances().remove(inst);
                iter.remove();
            }
        }

        if (sourceLocation == null) {
            throw new IllegalStateException("Source location not found [groupID="
                    + sourceGroupID + ", systemID=" + sourceSystemID + ", path="
                    + targetStoragePath + "]");
        }

        updateAvailability(inst, availability);

        updateStudyAccessTime(inst, targetGroupID);

        Location newLocation = new Location.Builder().storageSystemGroupID(targetGroupID)
                .storageSystemID(targetSystemID).storagePath(targetStoragePath)
                .transferSyntaxUID(sourceLocation.getTransferSyntaxUID())
                .size(sourceLocation.getSize()).digest(sourceLocation.getDigest())
                .timeZone(sourceLocation.getTimeZone())
                .otherAttsDigest(sourceLocation.getOtherAttsDigest()).build();
        LOG.info("Create {}", newLocation);
        em.persist(newLocation);

        inst.getLocations().add(newLocation);

        em.flush();
        em.detach(newLocation);
        fetched.fire(newLocation);

        // delete duplicates
        if (duplicates.size() > 0) {
            try {
                ArchiveDeviceExtension devExt = device
                        .getDeviceExtension(ArchiveDeviceExtension.class);
                locationMgmt.scheduleDelete(duplicates,
                        devExt.getPriorsCacheDeleteDuplicateLocationsDelay() * 1000, false);
            } catch (Exception e) {
                LOG.error("Schedule delete failed for duplicate locations: {}",
                        duplicates, e);
            }
        }
    }

    private Instance findInstance(String iuid) {
        try {
            return em
                    .createNamedQuery(Instance.FIND_BY_SOP_INSTANCE_UID_EAGER,
                            Instance.class).setParameter(1, iuid).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private void updateAvailability(Instance inst, Availability availability) {
        if (availability.ordinal() < inst.getAvailability().ordinal())
            inst.setAvailability(availability);
    }

    private void updateStudyAccessTime(Instance inst, String groupID) {
        Study study = inst.getSeries().getStudy();
        locationMgmt.findOrCreateStudyOnStorageGroup(study, groupID);
    }

    public void clearCache(String groupID) throws IOException {
        ArchiveDeviceExtension devExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        int maxResults = devExt.getPriorsCacheClearMaxLocationsPerDelete();
        int offset = 0;
        List<Location> locations;
        while ((locations = selectLocationsFromGroup(groupID, maxResults, offset)).size() > 0) {
            offset += locations.size();
            for (Location location : locations) {
                for (Instance instance : location.getInstances())
                    instance.getLocations().remove(location);
                location.getInstances().clear();
            }
            em.flush();
            try {
                locationMgmt.scheduleDelete(locations, 0, false);
            } catch (Exception e) {
                LOG.error("Schedule delete failed for Storage System Group {}", groupID);
            }
        }
    }

    private List<Location> selectLocationsFromGroup(String groupID, int maxResults, int offset) {
        return em
                .createQuery(
                        "SELECT l FROM Location l where l.storageSystemGroupID = ?1",
                        Location.class).setParameter(1, groupID).setFirstResult(offset)
                .setMaxResults(maxResults).getResultList();
    }
}
