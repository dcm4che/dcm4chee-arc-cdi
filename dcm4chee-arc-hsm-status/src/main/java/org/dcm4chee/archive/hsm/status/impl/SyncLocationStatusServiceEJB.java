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
package org.dcm4chee.archive.hsm.status.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Location.Status;
import org.dcm4chee.storage.ContainerEntry;
import org.dcm4chee.storage.RetrieveContext;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.service.RetrieveService;
import org.dcm4chee.storage.service.VerifyContainerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Steve Kroetsch<stevekroetsch@hotmail.com>
 *
 */
@Stateless
public class SyncLocationStatusServiceEJB {

    private static final Logger LOG = LoggerFactory
            .getLogger(SyncLocationStatusServiceEJB.class);

    @Inject
    private Device device;

    @Inject
    private RetrieveService retrieveService;

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    public int checkStatus() throws IOException {
        ArchiveDeviceExtension devExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        String[] groupIDs = devExt.getSyncLocationStatusStorageSystemGroupIDs();
        if (groupIDs == null || groupIDs.length == 0)
            return 0;
        int maxResults = devExt.getSyncLocationStatusMaxNumberPerTask();
        Timestamp before = new Timestamp(System.currentTimeMillis()
                - TimeUnit.MINUTES.toMillis(devExt.getSyncLocationStatusCheckDelay()));
        List<Location> locations = em
                .createNamedQuery(Location.FIND_BY_STATUS_AND_STORAGE_GROUP_IDS,
                        Location.class)
                .setParameter(1, Status.TO_ARCHIVE)
                .setParameter(2, before)
                .setParameter(3, Arrays.asList(groupIDs))
                .setMaxResults(maxResults).getResultList();
        int count = 0;
        Map<ContainerKey, Status> checkedContainers = new HashMap<ContainerKey, Status>();
        for (Location location : locations) {
            if (checkStatus(location, checkedContainers))
                count++;
        }
        return count;
    }

    private boolean checkStatus(Location location,
            Map<ContainerKey, Status> checkedContainers) throws IOException {
        ContainerKey key = null;
        if (location.getEntryName() != null) {
            key = new ContainerKey(location);
            Status status = checkedContainers.get(key);
            if (status != null)
                return updateStatus(location, status);
            if (checkedContainers.containsKey(key))
                return false;
        }

        Status status = queryStatus(location);
        if (key != null) {
            checkedContainers.put(key, status);
        }
        return status == null ? false : updateStatus(location, status);
    }

    private boolean isVerifyArchived() {
        return device.getDeviceExtension(ArchiveDeviceExtension.class)
                .isSyncLocationStatusVerifyArchived();
    }

    private Status queryStatus(Location location) throws IOException  {
        StorageSystem storageSystem = retrieveService.getStorageSystem(
                location.getStorageSystemGroupID(), location.getStorageSystemID());
        RetrieveContext ctx = retrieveService.createRetrieveContext(storageSystem);
        Status status = null;
        try {
             status = retrieveService.queryStatus(ctx, location.getStoragePath(),
                Status.class);
        } catch(IOException e) {
            LOG.error("{} query status failed for {}", storageSystem, location, e);
            status = Status.QUERY_FAILED;
        }
        if (Status.ARCHIVED.equals(status) && isVerifyArchived()) {
            if (!verify(location, ctx))
                status = Status.VERIFY_FAILED;
        }
        return status;
    }

    private boolean verify(Location location, RetrieveContext ctx)
            throws IOException {
        if (location.getEntryName() == null) {
            if (!retrieveService.calculateDigestAndMatch(ctx, location.getDigest(),
                    location.getStoragePath())) {
                LOG.error("Checksum does not match for {}", location);
                return false;
            }
            return true;
        }
        try {
            retrieveService.verifyContainer(ctx, location.getStoragePath(),
                    Collections.<ContainerEntry> emptyList());
        } catch (VerifyContainerException e) {
            LOG.error("Verify failed for container {}", location, e);
            return false;
        }
        return true;
    }

    private boolean updateStatus(Location location, Status status) {
        if (location.getStatus() != status) {
            LOG.info("Change status of {} to {}", location, status);
            location.setStatus(status);
            return true;
        }
        return false;
    }

    private static class ContainerKey {
        private final String groupID;
        private final String storageSystemID;
        private final String name;
        private final int hash;

        ContainerKey(Location location) {
            this.groupID = location.getStorageSystemGroupID();
            this.storageSystemID = location.getStorageSystemID();
            this.name = location.getStoragePath();
            this.hash = 47 * (47 * groupID.hashCode() + storageSystemID.hashCode())
                    + name.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ContainerKey))
                return false;

            ContainerKey other = (ContainerKey) obj;
            return hash == other.hash && groupID.equals(other.groupID)
                    && storageSystemID.equals(other.storageSystemID)
                    && name.equals(other.name);
        }
    }
}
