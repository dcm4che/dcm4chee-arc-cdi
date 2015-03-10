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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4chee.archive.hsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.hsm.impl.ArchivingSchedulerEJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * 
 */

@RunWith(Arquillian.class)
public class HsmMoveIT extends HsmITBase {

    private static final Logger LOG = LoggerFactory.getLogger(HsmMoveIT.class);
    
    @Inject
    private ArchivingSchedulerEJB ejb;

    @Deployment
    public static WebArchive createDeployment() {
        return createDeployment(HsmMoveIT.class);
    }
    @Test
    public void testMoveStudyOneSeries() throws Exception {
        store(RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_2);
        List<Location> onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        assertEquals("Number of Locations for "+FIRST_INSTANCE_STUDY_2, 1, locations.size());
        scheduler.moveStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 1), true, TEST_NEARLINE_ZIP);
        checkLocationsDeleted(onlineRefs, false);
    }

    @Test
    public void testMoveStudyTwoSeries() throws Exception {
        store(RESOURCES_STUDY_1_2SERIES, arcAEExt);
        List<Location> onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        scheduler.moveStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 1), true, TEST_NEARLINE_ZIP);
        checkLocationsDeleted(onlineRefs, true);
    }

    @Test
    public void testMoveStudyOneSeriesToTar() throws Exception {
        store(RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<Location> onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        scheduler.moveStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_TAR);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 1), true, TEST_NEARLINE_TAR);
        checkLocationsDeleted(onlineRefs, true);
    }

    @Test
    public void testMoveStudyTwoSeriesToFlat() throws Exception {
        store(RESOURCES_STUDY_1_2SERIES, arcAEExt);
        List<Location> onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        scheduler.moveStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE_FLAT);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 1), true, TEST_NEARLINE_FLAT);
        checkLocationsDeleted(onlineRefs, true);
    }

    @Test
    public void testMoveStudyTwoSeriesAfterEachSeries() throws Exception {
        store(Arrays.copyOfRange(RESOURCES_STUDY_1_2SERIES, 0, 3), arcAEExt);
        List<Location> onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        scheduler.moveStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_1, 3, 1), true, TEST_NEARLINE_ZIP);
        checkLocationsDeleted(onlineRefs, true);
        store(Arrays.copyOfRange(RESOURCES_STUDY_1_2SERIES, 3, 5), arcAEExt);
        checkStorageSystemGroups(checkLocationsOfSeries(SERIES_INSTANCE_UID_1_1, 3, 1), true, TEST_NEARLINE_ZIP);
        checkStorageSystemGroups(checkLocationsOfSeries(SERIES_INSTANCE_UID_1_2, 2, 1), true, TEST_ONLINE);
        onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        scheduler.moveStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 1), true, TEST_NEARLINE_ZIP);
        checkLocationsDeleted(onlineRefs, true);
    }

    @Test
    public void testMoveStudyTwoSeriesFromZipToFlat() throws Exception {
        store(RESOURCES_STUDY_1_2SERIES, arcAEExt);
        List<Location> onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        assertEquals("ONLINE Refs", 5, onlineRefs.size());
        scheduler.moveStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 1), true, TEST_NEARLINE_ZIP);
        checkLocationsDeleted(onlineRefs, true);
        List<Location> zipRefs = getLocationsOnStorageGroup(TEST_NEARLINE_ZIP);
        scheduler.moveStudy(STUDY_INSTANCE_UID_1, TEST_NEARLINE_ZIP, TEST_NEARLINE_FLAT);
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 1), true, TEST_NEARLINE_FLAT);
        checkLocationsDeleted(zipRefs, true);
    }
    
    @Test
    public void testMoveStudyKeepSourceContainer() throws Exception {
        store(RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<Location> onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        assertEquals("ONLINE Refs", 4, onlineRefs.size());
        scheduler.moveStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 1), true, TEST_NEARLINE_ZIP);
        checkLocationsDeleted(onlineRefs, true);
        List<Location> zipRefs = getLocationsOnStorageGroup(TEST_NEARLINE_ZIP);
        String targetName = zipRefs.get(0).getStoragePath()+"_inst";
        List<Instance> instances = getInstancesOfStudy(STUDY_INSTANCE_UID_2);
        ejb.scheduleInstances(instances.subList(0, 2), TEST_NEARLINE_ZIP, TEST_NEARLINE_FLAT, targetName, true);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        this.checkLocationsOfInstances(instances, 1);//check if sources are still available (container not deleted)
        scheduler.moveStudy(STUDY_INSTANCE_UID_2, TEST_NEARLINE_ZIP, TEST_NEARLINE_FLAT);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 1), true, TEST_NEARLINE_FLAT);
        checkLocationsDeleted(zipRefs, true);
    }
    
    @Test
    public void testMoveMovedStudy() throws Exception {
        store(RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<Location> onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        assertEquals("ONLINE Refs", 4, onlineRefs.size());
        scheduler.moveStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER+10000);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 1), true, TEST_NEARLINE_ZIP);
        checkLocationsDeleted(onlineRefs, true);
        List<Location> zipRefs = getLocationsOnStorageGroup(TEST_NEARLINE_ZIP);
        scheduler.moveStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER+2000);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 1), true, TEST_NEARLINE_ZIP);
        List<Location> zipRefsAfter = getLocationsOnStorageGroup(TEST_NEARLINE_ZIP);
        assertEquals("ZIP Refs after 2nd move.", zipRefs.size(), zipRefsAfter.size());
        loop: for (Location l1 : zipRefsAfter) {
            for (Location l2: zipRefs) {
                if (l1.getPk() == l2.getPk())
                    continue loop;
            }
            fail("Locations after 2nd move are different! new Location:"+l1);
        }
    }

    @Test
    public void testStoreCopyMoveStudy() throws Exception {
        store(RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<Location> onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        assertEquals("ONLINE Refs", 4, onlineRefs.size());
        scheduler.copyStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER+10000);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 2), true, TEST_ONLINE, TEST_NEARLINE_ZIP);
        List<Location> zipRefs = getLocationsOnStorageGroup(TEST_NEARLINE_ZIP);
        scheduler.moveStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER+2000);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 1), true, TEST_NEARLINE_ZIP);
        List<Location> zipRefsAfter = getLocationsOnStorageGroup(TEST_NEARLINE_ZIP);
        assertEquals("ZIP Refs after move.", zipRefs.size(), zipRefsAfter.size());
        loop: for (Location l1 : zipRefsAfter) {
            for (Location l2: zipRefs) {
                if (l1.getPk() == l2.getPk())
                    continue loop;
            }
            fail("Locations after move are different! new Location:"+l1);
        }
        onlineRefs = getLocationsOnStorageGroup(TEST_ONLINE);
        assertEquals("ONLINE Refs after move", 0, onlineRefs.size());
    }
    @Test
    public void testMoveStudyLocationWithTwoInstances() throws Exception {
        store(RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<Location> onlineRefs = getStudyLocationsOnStorageGroup(STUDY_INSTANCE_UID_2, TEST_ONLINE);
        assertEquals("ONLINE Refs Study 2", RESOURCES_STUDY_2_1SERIES.length, onlineRefs.size());
        store(RESOURCES_STUDY_1_2SERIES, arcAEExt);
        List<Instance> instancesStudy1 = getInstancesOfStudy(STUDY_INSTANCE_UID_1);
        assertEquals("Instances Study 1", RESOURCES_STUDY_1_2SERIES.length, instancesStudy1.size());
        List<Location> onlineRefsTot = getLocationsOnStorageGroup(TEST_ONLINE);
        assertEquals("ONLINE Refs Study 1 & 2", RESOURCES_STUDY_2_1SERIES.length+RESOURCES_STUDY_1_2SERIES.length, onlineRefsTot.size());

        utx.begin();
        instancesStudy1.get(0).getLocations().add(onlineRefs.get(0));
        em.merge(instancesStudy1.get(0));
        em.flush();
        utx.commit();
        
        scheduler.moveStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER+2000);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 1), true, TEST_NEARLINE_ZIP);
        List<Location> zipRefs = getStudyLocationsOnStorageGroup(STUDY_INSTANCE_UID_2, TEST_NEARLINE_ZIP);
        assertEquals("ZIP Refs after move.", RESOURCES_STUDY_2_1SERIES.length, zipRefs.size());
        List<Location>onlineRefsAfter = getLocationsOnStorageGroup(TEST_ONLINE);
        assertEquals("ONLINE Refs after move (study 2 + 1 additional Location of first instance of Study 2)", RESOURCES_STUDY_1_2SERIES.length + 1, onlineRefsAfter.size());
    }
}
