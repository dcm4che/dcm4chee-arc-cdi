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

import java.util.Arrays;
import java.util.List;

import org.dcm4chee.archive.entity.Location;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * 
 */

@RunWith(Arquillian.class)
public class HsmCopyIT extends HsmITBase {

    @Deployment
    public static WebArchive createDeployment() {
        return createDeployment(HsmCopyIT.class);
    }
    @Test
    public void testCopyStudyOneSeries() throws Exception {
        store(RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 1, locations.size());
        copyStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 2), true, TEST_ONLINE, TEST_NEARLINE_ZIP);
    }

    @Test
    public void testCopyStudyTwoSeries() throws Exception {
        store(RESOURCES_STUDY_1_2SERIES, arcAEExt);
        copyStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 2);
    }
    
    @Test
    public void testCopyStudyTwoSeriesAfterEachSeries() throws Exception {
        store(Arrays.copyOfRange(RESOURCES_STUDY_1_2SERIES, 0, 3), arcAEExt);
        copyStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkLocationsOfStudy(STUDY_INSTANCE_UID_1, 3, 2);
        store(Arrays.copyOfRange(RESOURCES_STUDY_1_2SERIES, 3, 5), arcAEExt);
        copyStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 2);
    }

    @Test
    public void testCopyStudyTwoSeriesToTar() throws Exception {
        store(RESOURCES_STUDY_1_2SERIES, arcAEExt);
        copyStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE_TAR);
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 2), true, TEST_ONLINE, TEST_NEARLINE_TAR);
    }

    @Test
    public void testCopyStudyOneSeriesToFlat() throws Exception {
        store(RESOURCES_STUDY_2_1SERIES, arcAEExt);
        copyStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE_FLAT);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 2), true, TEST_ONLINE, TEST_NEARLINE_FLAT);
    }

    @Test
    public void testCopyOneOfTwoSeries() throws Exception {
        store(RESOURCES_STUDY_1_2SERIES, arcAEExt);
        copySeries(SERIES_INSTANCE_UID_1_1, TEST_ONLINE, TEST_NEARLINE_ZIP);
        waitForFinishedTasks(1, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkStorageSystemGroups(checkLocationsOfSeries(SERIES_INSTANCE_UID_1_1, 3, 2), true, TEST_ONLINE, TEST_NEARLINE_ZIP);
        checkStorageSystemGroups(checkLocationsOfSeries(SERIES_INSTANCE_UID_1_2, 2, 1), true, TEST_ONLINE);
    }
}
