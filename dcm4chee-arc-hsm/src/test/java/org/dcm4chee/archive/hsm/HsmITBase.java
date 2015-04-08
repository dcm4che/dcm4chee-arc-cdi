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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Location.Status;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.event.StartStopReloadEvent;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.RetrieveContext;
import org.dcm4chee.storage.archiver.service.ArchiverContext;
import org.dcm4chee.storage.archiver.service.ContainerEntriesStored;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.Container;
import org.dcm4chee.storage.conf.FileCache;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.dcm4chee.storage.service.RetrieveService;
import org.dcm4chee.storage.spi.FileCacheProvider;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * 
 */

@RunWith(Arquillian.class)
public class HsmITBase {

    protected static final long DEFAULT_TASK_TIMEOUT = 1000l;
    protected static final long DEFAULT_WAIT_AFTER = 800;
    
    protected static final String TARGET_PATH = "target";
    protected static final String FS_NEARLINE_BASE_PATH = TARGET_PATH+"/test/fs/nearline";

    protected static final String FS_ONLINE_PATH = TARGET_PATH+"/test/fs/online";

    protected static final String TEST_ONLINE = "TEST_ONLINE";

    protected static final String TEST_NEARLINE_FLAT = "TEST_NEARLINE_FLAT";
    protected static final String TEST_NEARLINE_ZIP = "TEST_NEARLINE_ZIP";
    protected static final String TEST_NEARLINE_TAR = "TEST_NEARLINE_TAR";

    protected static final String SOURCE_AET = "HSM_TEST_SRC";

    protected static final String[] RETRIEVE_AETS = { "RETRIEVE_AET" };

    @Inject
    protected StoreService storeService; 

    @Inject
    protected Device device;
    @PersistenceContext(name="dcm4chee-arc")
    EntityManager em;

    @Inject
    UserTransaction utx;

    @Inject
    protected ArchivingScheduler scheduler; 

    @Inject @ArchiveServiceReloaded
    protected Event<StartStopReloadEvent> archiveServiceReloaded;

    @Inject
    protected RetrieveService retrieveService; 

    private static final Logger LOG = LoggerFactory.getLogger(HsmITBase.class);

    protected static final String[] RESOURCES_STUDY_1_2SERIES = {
        "testdata/study_1_series_1_1.xml",
        "testdata/study_1_series_1_2.xml",
        "testdata/study_1_series_1_3.xml",
        "testdata/study_1_series_2_1.xml",
        "testdata/study_1_series_2_2.xml",
    };
    protected static final String[] RESOURCES_STUDY_2_1SERIES = {
        "testdata/study_2_series_1_1.xml",
        "testdata/study_2_series_1_2.xml",
        "testdata/study_2_series_1_3.xml",
        "testdata/study_2_series_1_4.xml",
    };

    protected static final String[][] ALL_RESOURCES = {
        RESOURCES_STUDY_1_2SERIES,RESOURCES_STUDY_2_1SERIES};

    protected static final String FLAG_RESOURCE_KEEP = "flags/keep.flag";
    
    protected static final String BASE_UID = "1.2.40.0.13.1.1.99.777.";

    protected static final String STUDY_INSTANCE_UID_1 = BASE_UID+"1";
    protected static final String STUDY_INSTANCE_UID_2 = BASE_UID+"2";
    protected static final String SERIES_INSTANCE_UID_1_1 = STUDY_INSTANCE_UID_1+".1";
    protected static final String SERIES_INSTANCE_UID_1_2 = STUDY_INSTANCE_UID_1+".2";
    protected static final String SERIES_INSTANCE_UID_2_1 = STUDY_INSTANCE_UID_2+".1";
    protected static final String FIRST_INSTANCE_STUDY_1 = SERIES_INSTANCE_UID_1_1+".1";
    protected static final String FIRST_INSTANCE_STUDY_2 = SERIES_INSTANCE_UID_2_1+".1";

    private static ArrayList<String> finishedTargets = new ArrayList<String>();
    private Object waitObject = new Object();
    
    ArchiveAEExtension arcAEExt;

    protected static WebArchive createDeployment(Class testClass) {
        WebArchive war= ShrinkWrap.create(WebArchive.class, "test.war");
        war.addClass(HsmITBase.class);
        war.addClass(testClass);
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
        
        if ( ! "false".equals(System.getProperty("keep", "false"))) {
            war.addAsResource(FLAG_RESOURCE_KEEP);
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
        arcAEExt = getConfiguredAEExtension((ArchivingRule[])null);
    }

    /*
     * Archiving tests
     */

    @SuppressWarnings("unchecked")
    protected List<Location> getLocations(String sopInstanceUID) {
        Query query = em.createQuery("SELECT i.locations FROM Instance"
                + " i where i.sopInstanceUID = ?1");
        query.setParameter(1, sopInstanceUID);
        return query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
    protected List<Location> getLocationsOnStorageGroup(String grpID) {
        Query query = em.createQuery("SELECT l FROM Location l WHERE l.storageSystemGroupID =?1");
        query.setParameter(1, grpID);
        return query.getResultList();
    }
    @SuppressWarnings("unchecked")
    protected List<Location> getStudyLocationsOnStorageGroup(String studyInstanceUID, String grpID) {
        Query query = em.createQuery("SELECT DISTINCT l FROM Location l LEFT JOIN FETCH l.instances JOIN l.instances i JOIN i.series.study st WHERE l.storageSystemGroupID =?1"+
                " AND st.studyInstanceUID =?2", Location.class);
        query.setParameter(1, grpID);
        query.setParameter(2, studyInstanceUID);
        return query.getResultList();
    }
    
    
    protected Set<String> checkLocationsOfStudy(String studyInstanceUID, int nrOfInstances, int nrOfLocationsPerInstance) {
        List<Instance> result = getInstancesOfStudy(studyInstanceUID);
        LOG.info("result.size:"+result.size());
        assertEquals("Number of Instances of study "+studyInstanceUID, nrOfInstances, result.size());
        return checkLocationsOfInstances(result, nrOfLocationsPerInstance);
    }

    protected List<Instance> getInstancesOfStudy(String studyInstanceUID) {
        Query query = em.createQuery("SELECT DISTINCT i FROM Instance i LEFT JOIN i.series.study st LEFT JOIN FETCH i.locations l"
                + " where st.studyInstanceUID = ?1", Instance.class);
        query.setParameter(1, studyInstanceUID);
        @SuppressWarnings("unchecked")
        List<Instance> result = query.getResultList();
        return result;
    }
    protected Set<String> checkLocationsOfSeries(String seriesInstanceUID, int nrOfInstances, int nrOfLocationsPerInstance) {
        Query query = em.createQuery("SELECT DISTINCT i FROM Instance i LEFT JOIN i.series s LEFT JOIN FETCH i.locations l"
                + " where s.seriesInstanceUID = ?1", Instance.class);
        query.setParameter(1, seriesInstanceUID);
        @SuppressWarnings("unchecked")
        List<Instance> result = query.getResultList();
        LOG.info("result.size:"+result.size());
        assertEquals("Number of Instances of series "+seriesInstanceUID, nrOfInstances, result.size());
        return checkLocationsOfInstances(result, nrOfLocationsPerInstance);
    }
    
    protected Set<String> checkLocationsOfInstances(List<Instance> instances, int nrOfLocationsPerInstance) {
        HashSet<String> groupIDs = new HashSet<String>(nrOfLocationsPerInstance);
        if (instances.size() > 0)
            clearFileCache(instances.get(0).getLocations());
        for ( Instance inst : instances) {
            assertEquals("Number of Locations for instance "+inst, nrOfLocationsPerInstance, inst.getLocations().size());
            LOG.info("Instance.locations:{}",inst.getLocations());
            for (Location ref : inst.getLocations()) {
                groupIDs.add(ref.getStorageSystemGroupID());
                RetrieveContext ctx = retrieveService.createRetrieveContext(this.getStorageSystem(ref));
                try {
                    Path file = ref.getEntryName() == null ? retrieveService.getFile(ctx, ref.getStoragePath()) :
                        retrieveService.getFile(ctx, ref.getStoragePath(), ref.getEntryName());
                    LOG.info("Location file:{}", file);
                    if (!Files.isRegularFile(file)) {
                        LOG.info("Location is not a regular file! file:{}", file);
                        fail("File "+file+" for location "+ref+" is not a regular file!");
                    }
                    assertEquals("Filesize of Location "+ref, ref.getSize(), Files.size(file));
                } catch (Exception e) {
                    LOG.error("getFile for location {} failed!", ref, e);
                    fail("getFile for location "+ref+" failed!");
                }
            }
        }
        return groupIDs;
    }
    
    protected void checkStorageSystemGroups(Set<String> groupIDs, boolean onlyOnExpected, String... expectedGroupIDs) {
        if (onlyOnExpected && groupIDs.size() > expectedGroupIDs.length) {
            for (String grpID : expectedGroupIDs)
                groupIDs.remove(grpID);
            fail("On additional StorageGroup(s) available:"+groupIDs);
        }
        for (String grpID : expectedGroupIDs) {
            assertTrue("Missing StorageGroup:"+grpID, groupIDs.contains(grpID));
        }
    }

    protected void checkLocationsDeleted(Collection<Location> locations, boolean allowStatusDeletionFailed) {
        clearFileCache(locations);
        for (Location ref : locations) {
            RetrieveContext ctx = retrieveService.createRetrieveContext(this.getStorageSystem(ref));
            try {
                Path file = ref.getEntryName() == null ? retrieveService.getFile(ctx, ref.getStoragePath()) :
                    retrieveService.getFile(ctx, ref.getStoragePath(), ref.getEntryName());
                if (Files.exists(file)) {
                    LOG.info("Location file still exists! file:{}", file);
                    Location l = em.find(Location.class, ref.getPk());
                    LOG.info("Current Location (pk={}):{}", ref.getPk(), l);
                    if (l == null) {
                        fail("File "+file+" for Location "+ref+" is not deleted! file still exists but Location is removed!");
                    } else if (l.getStatus() == Status.DELETE_FAILED) {
                        if (allowStatusDeletionFailed) {
                            LOG.info("Location still exists with Location.status DELETE_FAILED! (NOT a failure) location:{}", l);
                        } else {
                            fail("File "+file+" for location "+ref+" is not deleted! file still exists and Location.status is DELETION_FAILED!");
                        }
                    } else {
                        fail("File "+file+" for location "+ref+" is not deleted! file still exists and Location.status is not DELETION_FAILED!");
                    }
                }
                LOG.info("Location file not found as expected! file:{}", file);
            } catch (Exception ignore) {
                LOG.info("getFile for location {} failed as expected!", ref);
            }
        }

    }

    private void clearFileCache(Collection<Location> locations) {
        for (Location l : locations) {
            RetrieveContext ctx = retrieveService.createRetrieveContext(this.getStorageSystem(l));
            FileCacheProvider cache = ctx.getFileCacheProvider();
            if (cache != null) {
                try {
                    cache.clearCache();
                } catch (IOException e) {
                    LOG.warn("Clear File Cache failed!", e);
                }
                break;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected List<ArchivingTask> getArchivingTasks(String seriesInstanceUID) {
        Query query = em.createQuery("SELECT t FROM ArchivingTask t"
                + " where t.seriesInstanceUID = ?1");
        query.setParameter(1, seriesInstanceUID);
        return query.getResultList();
    }

    protected boolean store(String[] dicomResources, ArchiveAEExtension arcAEExt) throws SecurityException, IllegalStateException, NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        for (String s : dicomResources) {
            if (!store(s, arcAEExt)) {
                LOG.error("Store of dicom resource failed:"+s);
                return false;
            }
        }
        return true;
    }

    protected boolean store(String dicomResource, ArchiveAEExtension arcAEExt) throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        utx.begin();
        try {
            StoreParam storeParam = ParamFactory.createStoreParam();
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
            LOG.info("Call storeService.store()!");
            storeService.store(context);
            LOG.info("Call storeService.store() finished!");
        } catch(Exception e) {
            LOG.error("store failed!", e);
            return false;
        } finally {
            utx.commit();
        }
        return true;
    }

    public void onContainerEntriesStored(@Observes @ContainerEntriesStored ArchiverContext archiverContext) {
        if (this.getClass() == HsmITBase.class)
            return;
        synchronized (finishedTargets) {
            finishedTargets.add(archiverContext.getStorageSystemGroupID()+":"+archiverContext.getName());
            finishedTargets.notifyAll();
        }
    }

    protected Attributes load(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return SAXReader.parse(cl.getResource(name).toString());
    }

    protected ArchiveAEExtension getConfiguredAEExtension(ArchivingRule... rule) {
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
        Container tarContainer = new Container();
        tarContainer.setProviderName("org.dcm4chee.storage.tar");
        storageExtension.addStorageSystemGroup(getNearlineStorageGroup(TEST_NEARLINE_FLAT, null));
        storageExtension.addStorageSystemGroup(getNearlineStorageGroup(TEST_NEARLINE_ZIP, zipContainer));
        storageExtension.addStorageSystemGroup(getNearlineStorageGroup(TEST_NEARLINE_TAR, tarContainer));
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
    private StorageSystemGroup getNearlineStorageGroup(String groupID, Container container) {
        StorageSystemGroup grp = new StorageSystemGroup();
        grp.setGroupID(groupID);
        grp.setStorageFilePathFormat("{now,date,yyyy/MM/dd}/{0020000D,hash}/{0020000E,hash}/{00080018,hash}");
        StorageSystem fs = new StorageSystem();
        fs.setStorageSystemID(groupID.toLowerCase()+"1");
        fs.setAvailability(Availability.NEARLINE);
        fs.setStorageSystemPath(Paths.get(FS_NEARLINE_BASE_PATH,"/fs_"+groupID.toLowerCase()+"1").toAbsolutePath().toString());
        fs.setProviderName("org.dcm4chee.storage.filesystem");
        grp.addStorageSystem(fs);
        if (container != null) {
            grp.setContainer(container);
            FileCache fileCache = new FileCache();
            fileCache.setProviderName("org.dcm4chee.storage.filecache");
            fileCache.setFileCacheRootDirectory("target/filecache");
            fileCache.setJournalRootDirectory("target/journaldir");
            grp.setFileCache(fileCache);
        }
        grp.activate(fs, true);
        return grp;
    }
    
    
    @After
    public void after() throws Exception {
        if (Thread.currentThread().getContextClassLoader().getResource(FLAG_RESOURCE_KEEP) == null) {
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
            LOG.info("Study removed:"+study);
            patients.add(study.getPatient());
        }
        for (Patient p : patients) {
            em.remove(p);
            LOG.info("Patient removed:"+p);
        }
        //remove ArchingTasks
        query = em.createQuery("SELECT t from ArchivingTask t where t.seriesInstanceUID like ?1");
        query.setParameter(1, BASE_UID+"%");
        @SuppressWarnings("unchecked")
        List<ArchivingTask> tasks = query.getResultList();
        for (ArchivingTask task : tasks) {
            em.remove(task);
            LOG.info("ArchivingTask removed:"+task);
        }
        //remove Locations
        query = em.createQuery("DELETE from Location l where l.storageSystemGroupID like ?1");
        query.setParameter(1, "TEST_%");
        int x = query.executeUpdate();
        LOG.info(x+" Locations removed!");
        utx.commit();
    }
    
    private void clearFS() {
        LOG.info(deleteFiles(Paths.get(FS_ONLINE_PATH))+" Files deleted from "+FS_ONLINE_PATH);
        LOG.info(deleteFiles(Paths.get(FS_NEARLINE_BASE_PATH))+" Files deleted from "+FS_NEARLINE_BASE_PATH);
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
                LOG.error("Failed to delete directory "+path, x);
            }
        }
        return count[0];
    }

    protected void waitForFinishedTasks(int numberOfFinishedTasks, long waitTime, int maxTries, long waitAfter) throws InterruptedException {
        LOG.info("Wait for "+numberOfFinishedTasks+" finished tasks!");
        synchronized (finishedTargets) {
            finishedTargets.clear();
            int tries = 0;
            while (finishedTargets.size() < numberOfFinishedTasks && tries++ < maxTries) {
                finishedTargets.wait(waitTime);
                LOG.info("Copies on "+finishedTargets);
            }
        }
        if (waitAfter > 0) {
            LOG.info("Wait additional "+waitAfter+"ms after finished tasks!");
            synchronized (waitObject) {
                waitObject.wait(waitAfter);
            }
        }
        LOG.info("Wait DONE!");
    }

    public StorageSystem getStorageSystem(Location ref) {
        StorageDeviceExtension devExt = device.getDeviceExtension(StorageDeviceExtension.class);
        return devExt.getStorageSystem(ref.getStorageSystemGroupID(), ref.getStorageSystemID());
    }

}
