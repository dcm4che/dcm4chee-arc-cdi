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

import java.util.List;

import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchivingRule;
import org.dcm4chee.archive.entity.ArchivingTask;
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
public class HsmArchiveIT extends HsmITBase {

    @Deployment
    public static WebArchive createDeployment() {
        return createDeployment(HsmArchiveIT.class);
    }

    @Test
    public void testStoreSingleNoArchivingRule() throws Exception {
        store(RESOURCES_STUDY_1_2SERIES[0], arcAEExt);
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_1);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_1, 1, locations.size());
    }
    @Test
    public void testStoreStudyNoArchivingRule() throws Exception {
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension((ArchivingRule[])null);
        store(RESOURCES_STUDY_1_2SERIES, arcAEExt);
        checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 1);
    }

    @Test
    public void testStoreOneSeriesOneArchivingRule() throws Exception {
        ArchivingRule rule = new ArchivingRule();
        rule.setAeTitles(new String[]{SOURCE_AET});
        rule.setDelayAfterInstanceStored(1);
        rule.setStorageSystemGroupIDs(TEST_NEARLINE_ZIP);
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension(rule);
        store(RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<ArchivingTask> tasks = getArchivingTasks(SERIES_INSTANCE_UID_2_1);
        assertEquals("#ArchivingTasks for "+SERIES_INSTANCE_UID_2_1, 1, tasks.size());
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 1, locations.size());
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 2);
    }

    @Test
    public void testStoreTwoSeriesOneArchivingRule() throws Exception {
        ArchivingRule rule = new ArchivingRule();
        rule.setAeTitles(new String[]{SOURCE_AET});
        rule.setDelayAfterInstanceStored(1);
        rule.setStorageSystemGroupIDs(TEST_NEARLINE_ZIP);
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension(rule);
        store(RESOURCES_STUDY_1_2SERIES, arcAEExt);
        List<ArchivingTask> tasks = getArchivingTasks(SERIES_INSTANCE_UID_1_1);
        assertEquals("#ArchivingTasks for "+SERIES_INSTANCE_UID_1_1, 1, tasks.size());
        tasks = getArchivingTasks(SERIES_INSTANCE_UID_1_2);
        assertEquals("#ArchivingTasks for "+SERIES_INSTANCE_UID_1_2, 1, tasks.size());
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 5, DEFAULT_WAIT_AFTER);
        checkLocationsOfStudy(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1_2SERIES.length, 2);
    }
    
    @Test
    public void testStoreTwoRules() throws Exception {
        ArchivingRule rule1 = new ArchivingRule();
        rule1.setAeTitles(new String[]{SOURCE_AET});
        rule1.setDelayAfterInstanceStored(1);
        rule1.setStorageSystemGroupIDs(TEST_NEARLINE_ZIP);
        rule1.setCommonName(TEST_NEARLINE_ZIP);
        ArchivingRule rule2 = new ArchivingRule();
        rule2.setAeTitles(new String[]{SOURCE_AET});
        rule2.setDelayAfterInstanceStored(1);
        rule2.setStorageSystemGroupIDs(TEST_NEARLINE_TAR);
        rule2.setCommonName(TEST_NEARLINE_TAR);
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension(rule1, rule2);
        store(HsmITBase.RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<ArchivingTask> tasks = getArchivingTasks(SERIES_INSTANCE_UID_2_1);
        assertEquals("#ArchivingTasks for "+SERIES_INSTANCE_UID_2_1, 2, tasks.size());
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 1, locations.size());
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 10, DEFAULT_WAIT_AFTER);
        locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 3, locations.size());
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 3),
                true, TEST_ONLINE, TEST_NEARLINE_ZIP, TEST_NEARLINE_TAR);
    }

    @Test
    public void testStoreOneRuleTwoTargets() throws Exception {
        ArchivingRule rule = new ArchivingRule();
        rule.setAeTitles(new String[]{SOURCE_AET});
        rule.setDelayAfterInstanceStored(1);
        rule.setStorageSystemGroupIDs(TEST_NEARLINE_ZIP,TEST_NEARLINE_TAR);
        rule.setCommonName("ZIPandTAR");
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension(rule);
        store(HsmITBase.RESOURCES_STUDY_2_1SERIES, arcAEExt);
        List<ArchivingTask> tasks = getArchivingTasks(SERIES_INSTANCE_UID_2_1);
        assertEquals("#ArchivingTasks for "+SERIES_INSTANCE_UID_2_1, 2, tasks.size());
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 1, locations.size());
        waitForFinishedTasks(2, DEFAULT_TASK_TIMEOUT, 10, DEFAULT_WAIT_AFTER);
        locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 3, locations.size());
        checkStorageSystemGroups(checkLocationsOfStudy(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2_1SERIES.length, 3),
                true, TEST_ONLINE, TEST_NEARLINE_ZIP, TEST_NEARLINE_TAR);
    }
}
