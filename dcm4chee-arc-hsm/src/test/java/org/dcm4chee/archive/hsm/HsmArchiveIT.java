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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.ArchiveServiceReloaded;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.ArchivingRule;
import org.dcm4chee.archive.conf.ArchivingRules;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.entity.ArchivingTask;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.event.StartStopReloadEvent;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.archiver.service.ArchiverContext;
import org.dcm4chee.storage.archiver.service.ContainerEntriesStored;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.Container;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HsmArchiveIT {

    private static final String TARGET_PATH = "target";
    private static final String FS_NEARLINE_BASE_PATH = TARGET_PATH+"/test/fs/nearline";

    private static final String FS_ONLINE_PATH = TARGET_PATH+"/test/fs/online";

    private static final String TEST_ONLINE = "TEST_ONLINE";

    private static final String TEST_NEARLINE = "TEST_NEARLINE";

    private static final String SOURCE_AET = "HSM_TEST_SRC";

    private static final String[] RETRIEVE_AETS = { "RETRIEVE_AET" };

    @Inject
    private StoreService storeService; 

    @Inject
    private Device device;
    @PersistenceContext(name="dcm4chee-arc")
    EntityManager em;

    @Inject
    UserTransaction utx;

    @Inject
    private ArchivingScheduler scheduler; 

    @Inject @ArchiveServiceReloaded
    private Event<StartStopReloadEvent> archiveServiceReloaded;

    private static final Logger log = Logger
            .getLogger(HsmArchiveIT.class);

    private static final String[] RESOURCES_STUDY_1 = {
        "testdata/study_1_series_1_1.xml",
        "testdata/study_1_series_1_2.xml",
        "testdata/study_1_series_1_3.xml",
        "testdata/study_1_series_2_1.xml",
        "testdata/study_1_series_2_2.xml",
    };
    private static final String[] RESOURCES_STUDY_2 = {
        "testdata/study_2_series_1_1.xml",
        "testdata/study_2_series_1_2.xml",
        "testdata/study_2_series_1_3.xml",
        "testdata/study_2_series_1_4.xml",
    };

    private static final String[][] ALL_RESOURCES = {
        RESOURCES_STUDY_1,RESOURCES_STUDY_2};

    private static final String BASE_UID = "1.2.40.0.13.1.1.99.777.";

    private static final String STUDY_INSTANCE_UID_1 = BASE_UID+"1";
    private static final String STUDY_INSTANCE_UID_2 = BASE_UID+"2";
    private static final String SERIES_INSTANCE_UID_1_1 = STUDY_INSTANCE_UID_1+".1";
    private static final String SERIES_INSTANCE_UID_1_2 = STUDY_INSTANCE_UID_1+".2";
    private static final String SERIES_INSTANCE_UID_2_1 = STUDY_INSTANCE_UID_2+".1";
    private static final String FIRST_INSTANCE_STUDY_1 = SERIES_INSTANCE_UID_1_1+".1";
    private static final String FIRST_INSTANCE_STUDY_2 = SERIES_INSTANCE_UID_2_1+".1";

    private static ArrayList<String> finishedTargets = new ArrayList<String>();

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war= ShrinkWrap.create(WebArchive.class, "test.war");
        war.addClass(HsmArchiveIT.class);
        war.addClass(ParamFactory.class);
        JavaArchive[] archs =   Maven.resolver()
                .loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies()
                .resolve().withoutTransitivity()
                .as(JavaArchive.class);
        for(JavaArchive a: archs) {
            a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            war.addAsLibrary(a);
        }

        for (int i = 0 ; i < ALL_RESOURCES.length ; i++) {
            for (int j = 0 ; j < ALL_RESOURCES[i].length ; j++) {
                war.addAsResource(ALL_RESOURCES[i][j]);
            }
        }

        war.as(ZipExporter.class).exportTo(
                new File("test.war"), true);
        return war;
    }

    @Before
    public void init() throws Exception {
        prepareDevice();
        clearFS();
        clearDB();
    }

    /*
     * Archiving tests
     */

    @Test
    public void testStoreSingleNoArchivingRule() throws Exception {
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension((ArchivingRule[])null);
        store(RESOURCES_STUDY_1[0], arcAEExt);
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_1);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_1, 1, locations.size());
    }
    @Test
    public void testStoreStudyNoArchivingRule() throws Exception {
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension((ArchivingRule[])null);
        store(RESOURCES_STUDY_1, arcAEExt);
        checkLocations(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1.length, 1);
    }

    @Test
    public void testStoreOneSeriesOneArchivingRule() throws Exception {
        ArchivingRule rule = new ArchivingRule();
        rule.setAeTitles(new String[]{SOURCE_AET});
        rule.setDelayAfterInstanceStored(1);
        rule.setStorageSystemGroupIDs(TEST_NEARLINE+"1");
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension(rule);
        store(RESOURCES_STUDY_2, arcAEExt);
        List<ArchivingTask> tasks = getArchivingTasks(SERIES_INSTANCE_UID_2_1);
        assertEquals("#ArchivingTasks for "+SERIES_INSTANCE_UID_2_1, 1, tasks.size());
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 1, locations.size());
        waitForFinishedTasks(2, 2000l, 5);
        checkLocations(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2.length, 2);
    }

    @Test
    public void testStoreTwoSeriesOneArchivingRule() throws Exception {
        ArchivingRule rule = new ArchivingRule();
        rule.setAeTitles(new String[]{SOURCE_AET});
        rule.setDelayAfterInstanceStored(1);
        rule.setStorageSystemGroupIDs(TEST_NEARLINE+"1");
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension(rule);
        store(RESOURCES_STUDY_1, arcAEExt);
        List<ArchivingTask> tasks = getArchivingTasks(SERIES_INSTANCE_UID_1_1);
        assertEquals("#ArchivingTasks for "+SERIES_INSTANCE_UID_1_1, 1, tasks.size());
        tasks = getArchivingTasks(SERIES_INSTANCE_UID_1_2);
        assertEquals("#ArchivingTasks for "+SERIES_INSTANCE_UID_1_2, 1, tasks.size());
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_1);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_1, 1, locations.size());
        waitForFinishedTasks(2, 4000l, 5);
        checkLocations(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1.length, 2);
    }
    
 /*
    @Test
    public void testStoreOneRuleTwoTargets() throws Exception {
        log.info("##################################################################################");
        log.info("##################################################################################");
        log.info("TEST: testStoreOneRuleTwoTargets");
        ArchivingRule rule = new ArchivingRule();
        rule.setAeTitles(new String[]{SOURCE_AET});
        rule.setDelayAfterInstanceStored(1);
        rule.setStorageSystemGroupIDs(TEST_NEARLINE+"1",TEST_NEARLINE+"2");
        ArchivingRule rule2 = new ArchivingRule();
        rule2.setAeTitles(new String[]{SOURCE_AET});
        rule2.setDelayAfterInstanceStored(1);
        rule2.setStorageSystemGroupIDs(TEST_NEARLINE+"2");
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension(rule);
        store(RESOURCES_STUDY_2, arcAEExt);
        List<ArchivingTask> tasks = getArchivingTasks(SERIES_INSTANCE_UID_2_1);
        assertEquals("#ArchivingTasks for "+SERIES_INSTANCE_UID_2_1, 2, tasks.size());
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 1, locations.size());
        waitForFinishedTasks(2, 4000l, 10);
        locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 3, locations.size());
        log.info("##################################################################################");
        log.info("##################################################################################");
    }
/*_*/
    @Test
    public void testCopyStudyOneSeries() throws Exception {
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension((ArchivingRule[])null);
        store(RESOURCES_STUDY_2, arcAEExt);
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_2);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_2, 1, locations.size());
        scheduler.copyStudy(STUDY_INSTANCE_UID_2, TEST_ONLINE, TEST_NEARLINE+"1");
        waitForFinishedTasks(1, 2000l, 5);
        checkLocations(STUDY_INSTANCE_UID_2, RESOURCES_STUDY_2.length, 2);
    }

    @Test
    public void testCopyStudyTwoSeries() throws Exception {
        ArchiveAEExtension arcAEExt = getConfiguredAEExtension((ArchivingRule[])null);
        store(RESOURCES_STUDY_1, arcAEExt);
        List<Location> locations = getLocations(FIRST_INSTANCE_STUDY_1);
        assertEquals("#Locations for "+FIRST_INSTANCE_STUDY_1, 1, locations.size());
        scheduler.copyStudy(STUDY_INSTANCE_UID_1, TEST_ONLINE, TEST_NEARLINE+"1");
        waitForFinishedTasks(2, 2000l, 5);
        checkLocations(STUDY_INSTANCE_UID_1, RESOURCES_STUDY_1.length, 2);
    }
    
    @SuppressWarnings("unchecked")
    private List<Location> getLocations(String sopInstanceUID) {
        Query query = em.createQuery("SELECT i.locations FROM Instance"
                + " i where i.sopInstanceUID = ?1");
        query.setParameter(1, sopInstanceUID);
        return query.getResultList();
    }
    
    private void checkLocations(String studyInstanceUID, int nrOfInstances, int nrOfLocationsPerInstance) {
        Query query = em.createQuery("SELECT i.sopInstanceUID, count(l) FROM Instance i LEFT JOIN i.series.study st LEFT JOIN i.locations l"
                + " where st.studyInstanceUID = ?1 GROUP BY i.sopInstanceUID");
        query.setParameter(1, studyInstanceUID);
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        log.info("result.size:"+result.size());
        assertEquals("Number of Instances of study "+studyInstanceUID, nrOfInstances, result.size());
        for ( Object[] oa : result) {
            log.info("i.pk:"+oa[0]+"  count(l):"+oa[1]);
            assertEquals("Number of Locations for instance "+oa[0], (long)nrOfLocationsPerInstance, (long)oa[1]);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ArchivingTask> getArchivingTasks(String seriesInstanceUID) {
        Query query = em.createQuery("SELECT t FROM ArchivingTask t"
                + " where t.seriesInstanceUID = ?1");
        query.setParameter(1, seriesInstanceUID);
        return query.getResultList();
    }

    private boolean store(String[] dicomResources, ArchiveAEExtension arcAEExt) throws SecurityException, IllegalStateException, NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        for (String s : dicomResources) {
            if (!store(s, arcAEExt)) {
                log.error("Store of dicom resource failed:"+s);
                return false;
            }
        }
        return true;
    }

    private boolean store(String dicomResource, ArchiveAEExtension arcAEExt) throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        utx.begin();
        try {
            StoreParam storeParam = ParamFactory.createStoreParam();
            storeParam.setRetrieveAETs(RETRIEVE_AETS);
            StoreSession session = storeService.createStoreSession(storeService); 
            session = storeService.createStoreSession(storeService);
            session.setSource(new GenericParticipant("", "hsmTest"));
            session.setRemoteAET(SOURCE_AET);
            session.setArchiveAEExtension(arcAEExt);
            storeService.initStorageSystem(session);
            storeService.initSpoolDirectory(session);
            StoreContext context = storeService.createStoreContext(session);
            Attributes fmi = new Attributes();
            fmi.setString(Tag.TransferSyntaxUID, VR.UI, "1.2.840.10008.1.2");
            storeService.writeSpoolFile(context, fmi, load(dicomResource));
            storeService.parseSpoolFile(context);
            log.info("#### call storeService.store()!");
            storeService.store(context);
            log.info("#### call storeService.store() finished!");
        } catch(Exception e) {
            log.error("store failed!", e);
            return false;
        } finally {
            utx.commit();
        }
        return true;
    }

    public void onContainerEntriesStored(@Observes @ContainerEntriesStored ArchiverContext archiverContext) {
        log.info("###### onContainerEntriesStored for "+archiverContext.getStorageSystemGroupID());
        synchronized (finishedTargets) {
            finishedTargets.add(archiverContext.getStorageSystemGroupID()+":"+archiverContext.getName());
            finishedTargets.notifyAll();
        }
        log.info("after notifyAll");
    }

    private Attributes load(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return SAXReader.parse(cl.getResource(name).toString());
    }

    private ArchiveAEExtension getConfiguredAEExtension(ArchivingRule... rule) {
        ArchiveAEExtension arcAEExt = device.getApplicationEntity("DCM4CHEE")
                .getAEExtension(ArchiveAEExtension.class);
        ArchivingRules rules = arcAEExt.getArchivingRules();
        rules.clear();
        if (rule != null && rule.length > 0) {
            for (ArchivingRule r : rule) 
                rules.add(r);
        }
        return arcAEExt;
    }

    private void prepareDevice() {
        StorageDeviceExtension storageExtension = device.getDeviceExtension(StorageDeviceExtension.class);
        storageExtension.addStorageSystemGroup(getOnlineStorageGroup() );
        Container zipContainer = new Container();
        zipContainer.setProviderName("org.dcm4chee.storage.zip");
        storageExtension.addStorageSystemGroup(getNearlineStorageGroup("1", zipContainer));
        Container tarContainer = new Container();
        tarContainer.setProviderName("org.dcm4chee.storage.tar");
        storageExtension.addStorageSystemGroup(getNearlineStorageGroup("2", zipContainer));
        ArchiveAEExtension aeExtension = device.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class);
        aeExtension.setStorageSystemGroupID(TEST_ONLINE);
        device.getDeviceExtension(ArchiveDeviceExtension.class).setArchivingSchedulerPollInterval(3);
        archiveServiceReloaded.fire(new StartStopReloadEvent(device, new GenericParticipant("", "hsmTest")));
    }

    private StorageSystemGroup getOnlineStorageGroup() {
        StorageSystemGroup testOnlineGroup = new StorageSystemGroup();
        testOnlineGroup.setGroupID(TEST_ONLINE);
        testOnlineGroup.setStorageFilePathFormat("{now,date,yyyy/MM/dd}/{0020000D,hash}/{0020000E,hash}/{00080018,hash}");
        StorageSystem onlineFS = new StorageSystem();
        onlineFS.setStorageSystemID("test_fs1");
        onlineFS.setAvailability(Availability.ONLINE);
        onlineFS.setStorageSystemPath(Paths.get(FS_ONLINE_PATH).toAbsolutePath().toString());
        onlineFS.setProviderName("org.dcm4chee.storage.filesystem");
        testOnlineGroup.addStorageSystem(onlineFS);
        testOnlineGroup.activate(onlineFS, true);
        return testOnlineGroup;
    }
    private StorageSystemGroup getNearlineStorageGroup(String postfix, Container container) {
        StorageSystemGroup grp = new StorageSystemGroup();
        grp.setGroupID(TEST_NEARLINE+postfix);
        grp.setStorageFilePathFormat("{now,date,yyyy/MM/dd}/{0020000D,hash}/{0020000E,hash}/{00080018,hash}");
        StorageSystem fs = new StorageSystem();
        fs.setStorageSystemID("test_nearline"+postfix);
        fs.setAvailability(Availability.NEARLINE);
        fs.setStorageSystemPath(Paths.get(FS_NEARLINE_BASE_PATH,"/fs_"+postfix).toAbsolutePath().toString());
        fs.setProviderName("org.dcm4chee.storage.filesystem");
        grp.addStorageSystem(fs);
        grp.setContainer(container);
        grp.activate(fs, true);
        return grp;
    }
    
    
    @After
    public void after() throws Exception {
        if (!Boolean.getBoolean("keep")) {
            clearDB();
            clearFS();
        }
    }

    private void clearDB() throws Exception {
        utx.begin();
        Query query = em.createQuery("SELECT s from Study s where s.studyInstanceUID like ?1");
        query.setParameter(1, BASE_UID+"%");
        @SuppressWarnings("unchecked")
        List<Study> studies = query.getResultList();
        HashSet<Patient> patients = new HashSet<Patient>();
        for (Study study : studies) {
            em.remove(study);
            log.info("Study removed:"+study);
            patients.add(study.getPatient());
        }
        for (Patient p : patients) {
            em.remove(p);
            log.info("Patient removed:"+p);
        }
        //remove ArchingTasks
        query = em.createQuery("SELECT t from ArchivingTask t where t.seriesInstanceUID like ?1");
        query.setParameter(1, BASE_UID+"%");
        @SuppressWarnings("unchecked")
        List<ArchivingTask> tasks = query.getResultList();
        for (ArchivingTask task : tasks) {
            em.remove(task);
            log.info("ArchivingTask removed:"+task);
        }
        //remove Locations
        query = em.createQuery("DELETE from Location l where l.storageSystemGroupID like ?1");
        query.setParameter(1, "TEST_%");
        int x = query.executeUpdate();
        log.info(x+" Locations removed!");
        utx.commit();
    }
    
    private void clearFS() {
        log.info(deleteFiles(Paths.get(FS_ONLINE_PATH))+" Files deleted from "+FS_ONLINE_PATH);
        log.info(deleteFiles(Paths.get(FS_NEARLINE_BASE_PATH))+" Files deleted from "+FS_NEARLINE_BASE_PATH);
        deleteFiles(Paths.get(TARGET_PATH));
    }
    private int deleteFiles(Path path) {
        final int[] count = new int[]{0};
        if (Files.exists(path)) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        count[0]++;
                        return FileVisitResult.CONTINUE;
                    }
    
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                 });
            } catch (IOException x) {
                log.error("Failed to delete directory "+path, x);
            }
        }
        return count[0];
    }

    private void waitForFinishedTasks(int numberOfFinishedTasks, long waitTime, int maxTries) throws InterruptedException {
        log.info("############# wait for "+numberOfFinishedTasks+" finished tasks!");
        synchronized (finishedTargets) {
            finishedTargets.clear();
            int tries = 0;
            while (finishedTargets.size() < numberOfFinishedTasks && tries++ < maxTries) {
                finishedTargets.wait(waitTime);
                log.info("####### Copies on "+finishedTargets);
            }
            log.info("########## wait additional "+waitTime+"ms!");
            finishedTargets.wait(waitTime);
            log.info("################ wait DONE!");
        }
    }

}
