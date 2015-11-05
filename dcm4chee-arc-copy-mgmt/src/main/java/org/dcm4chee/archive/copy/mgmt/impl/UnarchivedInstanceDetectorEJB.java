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

package org.dcm4chee.archive.copy.mgmt.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4chee.archive.entity.ArchivingTask;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.StudyOnStorageSystemGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Steve Kroetsch<stevekroetsch@hotmail.com>
 *
 */
@Stateless
public class UnarchivedInstanceDetectorEJB {

    private static final Logger LOG = LoggerFactory
            .getLogger(UnarchivedInstanceDetectorServiceImpl.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    public void filterTargetsWithInstancesAlreadyStored(Series series,
            List<String> storageSystemGroupIDs, List<String> externalDeviceNames) {
        int numInstances = countInstancesForSeries(series);
        for (Iterator<String> iterator = storageSystemGroupIDs.iterator(); iterator.hasNext();) {
            String groupID = iterator.next();
            int unarchivedCount = numInstances
                    - countInstancesForSeriesOnStorageSystemGroup(series, groupID);
            if (unarchivedCount == 0) {
                iterator.remove();
            } else {
                LOG.info("Found {} instances not archived to Storage System Group: {} for {}",
                        unarchivedCount, groupID, series);
            }
        }
        for (Iterator<String> iterator = externalDeviceNames.iterator(); iterator.hasNext();) {
            String deviceName = iterator.next();
            int unarchivedCount = numInstances
                    - countInstancesForSeriesOnExternalDevice(series, deviceName);
            if (unarchivedCount == 0) {
                iterator.remove();
            } else {
                LOG.info("Found {} instances not archived to External Device: {} for {}",
                        unarchivedCount, deviceName, series);
            }
        }
    }

    public List<Series> findSeriesByCreatedTime(Timestamp startTime, Timestamp endTime, int offset,
            int maxResults) {
        LOG.info("Querying for series from {} to {}, batch starting at "
                + "record {}, limiting to {} records", startTime, endTime, offset, maxResults);
        return em.createNamedQuery(Series.FIND_BY_CREATED_TIME_RANGE, Series.class)
                .setParameter(1, startTime).setParameter(2, endTime).setFirstResult(offset)
                .setMaxResults(maxResults).getResultList();
    }

    public int countInstancesForSeries(Series series) {
        int count = em.createNamedQuery(Instance.COUNT_BY_SERIES_INSTANCE_UID, Number.class)
                .setParameter(1, series.getSeriesInstanceUID()).getSingleResult().intValue();
        return count;
    }

    public int countInstancesForSeriesOnStorageSystemGroup(Series series, String groupID) {
        int count = em
                .createQuery(
                        "SELECT COUNT(i) FROM Instance i "
                                + "JOIN FETCH i.locations l "
                                + "WHERE i.series.seriesInstanceUID = ?1 "
                                + "AND l.storageSystemGroupID = ?2", Number.class)
                .setParameter(1, series.getSeriesInstanceUID()).setParameter(2, groupID)
                .getSingleResult().intValue();
        return count;
    }

    public int countInstancesForSeriesOnExternalDevice(Series series, String deviceName) {
        int count = em
                .createQuery(
                        "SELECT COUNT(i) FROM Instance i "
                                + "JOIN FETCH i.externalRetrieveLocations l"
                                + "WHERE i.series.seriesInstanceUID = ?1 "
                                + "AND l.retrieveDeviceName = ?2", Number.class)
                .setParameter(1, series.getSeriesInstanceUID()).setParameter(2, deviceName)
                .getSingleResult().intValue();
        return count;
    }

    public Date findMinAccessTimeFromStudyOnStorageSystem(String storageSystemGroupID) {
        Date date = em.createNamedQuery(StudyOnStorageSystemGroup.FIND_MIN_ACCESS_TIME, Date.class)
                .setParameter(1, storageSystemGroupID).getSingleResult();
        return date;
    }

    public List<ArchivingTask> findArchivingTasksforSeries(Series series) {
        List<ArchivingTask> alreadyScheduledTasks = em
                .createNamedQuery(ArchivingTask.FIND_BY_SERIES_INSTANCE_UID, ArchivingTask.class)
                .setParameter(1, series.getSeriesInstanceUID()).getResultList();
        return alreadyScheduledTasks;
    }

    public void filterTargetsWithArchivingTaskScheduled(Series series,
            List<String> storageSystemGroupIDs, List<String> externalDeviceNames) {
        List<ArchivingTask> archivingTasks = findArchivingTasksforSeries(series);
        for (ArchivingTask archivingTask : archivingTasks) {
            if (archivingTask.getTargetStorageSystemGroupID() != null) {
                storageSystemGroupIDs.remove(archivingTask.getTargetStorageSystemGroupID());
            } else if (archivingTask.getTargetExternalDevice() != null) {
                externalDeviceNames.remove(archivingTask.getTargetExternalDevice());
            } else {
                LOG.warn("Invalid archiving task {}", archivingTask);
            }
        }
    }
}