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
 * Portions created by the Initial Developer are Copyright (C) 2015
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

package org.dcm4chee.archive.prefetch;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.enterprise.context.RequestScoped;
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
import javax.xml.parsers.ParserConfigurationException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.RetrieveContext;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.FileCache;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.dcm4chee.storage.service.RetrieveService;
import org.dcm4chee.storage.spi.FileCacheProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Steve Kroetsch <stevekroetsch@hotmail.com>
 *
 */
@RunWith(Arquillian.class)
public class PriorsFileCacheProviderIT {

    static Logger LOG = LoggerFactory.getLogger(PriorsFileCacheProviderIT.class);

    private static final String DEFAULT_STORAGE_SYSTEM_GROUP_ID = "DEFAULT";
    private static final String DEFAULT_STORAGE_SYSTEM_ID = "default";
    private static final Path DEFAULT_STORAGE_SYSTEM_PATH = Paths
            .get("target/test/default");

    private static final String PRIORS_STORAGE_SYSTEM_GROUP_ID = "PRIORS";
    private static final String PRIORS_STORAGE_SYSTEM_ID = "priors";
    private static final Path PRIORS_STORAGE_SYSTEM_PATH = Paths
            .get("target/test/priors");

    private static final String[] INSTANCE_RESOURCES = { "data/instance1.xml",
            "data/instance2.xml", "data/instance3.xml", "data/instance4.xml",
            "data/instance5.xml", };

    private static final String[] DELETE_QUERIES = { "DELETE FROM rel_instance_location",
            "DELETE FROM location", "DELETE FROM content_item",
            "DELETE FROM verify_observer", "DELETE FROM instance",
            "DELETE FROM series_query_attrs", "DELETE FROM series_req",
            "DELETE FROM series", "DELETE FROM study_query_attrs",
            "DELETE FROM rel_study_pcode", "DELETE FROM study",
            "DELETE FROM rel_linked_patient_id", "DELETE FROM patient_id",
            "DELETE FROM id_issuer", "DELETE FROM patient", "DELETE FROM soundex_code",
            "DELETE FROM person_name", "DELETE FROM code", "DELETE FROM dicomattrs" };

    private static final Executor DIRECT_EXECUTOR = new Executor() {
        public void execute(Runnable r) {
            r.run();
        }
    };

    @Inject
    private StoreService storeService;

    @Inject
    private UserTransaction utx;

    @Inject
    private RetrieveService retrieveService;

    @Inject
    private Device device;

    @Inject
    private FetchedObserver observer;

    @PersistenceContext(name = "dcm4chee-arc", unitName="dcm4chee-arc")
    private EntityManager em;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
        JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies().resolve().withoutTransitivity()
                .as(JavaArchive.class);
        for (JavaArchive a : archs) {
            a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
             war.addAsLibrary(a);
        }

        war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        for (String resourceName : INSTANCE_RESOURCES)
            war.addAsResource(resourceName);
        return war;
    }

    @Before
    public void setup() throws Exception {
        configureDevice();
        deleteDir(PRIORS_STORAGE_SYSTEM_PATH);
        deleteDir(DEFAULT_STORAGE_SYSTEM_PATH);
        clearDatabase();
    }

    @Test
    public void testToPath() throws Exception {
        createCacheLocations();

        FileCacheProvider provider = fileCacheProvider();
        RetrieveContext ctx = createRetrieveContext(DEFAULT_STORAGE_SYSTEM_GROUP_ID,
                DEFAULT_STORAGE_SYSTEM_ID);
        List<Location> sourceLocations = selectLocations(DEFAULT_STORAGE_SYSTEM_GROUP_ID);
        Assert.assertEquals(INSTANCE_RESOURCES.length, sourceLocations.size());
        for (Location location : sourceLocations) {
            Path path = provider.toPath(ctx, location.getStoragePath());
            Assert.assertTrue(Files.exists(path));
        }
    }

    @Test
    public void testRegister() throws Exception {
        createCacheLocations();

        // Check cache locations were registered
        List<Location> sourceLocations = selectLocations(DEFAULT_STORAGE_SYSTEM_GROUP_ID);
        Assert.assertEquals(INSTANCE_RESOURCES.length, sourceLocations.size());
        List<Location> cacheLocations = selectLocations(PRIORS_STORAGE_SYSTEM_GROUP_ID);
        Assert.assertEquals(sourceLocations.size(), cacheLocations.size());
        Assert.assertEquals(cacheLocations.size(), observer.getFetched().size());

        // Create copy of each cache location
        for (Location location : sourceLocations) {
            RetrieveContext ctx = createRetrieveContext(DEFAULT_STORAGE_SYSTEM_GROUP_ID,
                    DEFAULT_STORAGE_SYSTEM_ID);
            FileCacheProvider provider = fileCacheProvider();
            Path source = provider.toPath(ctx, location.getStoragePath());
            Path copy = source.resolveSibling(source.getFileName() + ".copy");
            Files.copy(source, copy);
            provider.register(ctx, location.getStoragePath(), copy);
        }

        // Check previous cache locations are deleted
        cacheLocations = selectLocations(PRIORS_STORAGE_SYSTEM_GROUP_ID);
        for (int i = 0; i < 10 && cacheLocations.size() > sourceLocations.size(); i++) {
            LOG.info("Waiting for {} cache location(s) to be deleted",
                    cacheLocations.size() - sourceLocations.size());
            Thread.sleep(500);
            cacheLocations = selectLocations(PRIORS_STORAGE_SYSTEM_GROUP_ID);
        }
        Assert.assertEquals(sourceLocations.size(), cacheLocations.size());
    }

    @Test
    public void testClearCache() throws Exception {
        createCacheLocations();

        fileCacheProvider().clearCache();
        List<Location> cacheLocations = selectLocations(PRIORS_STORAGE_SYSTEM_GROUP_ID);
        for (int i = 0; i < 10 && cacheLocations.size() > 0; i++) {
            LOG.info("Waiting for {} cache location(s) to be deleted",
                    cacheLocations.size());
            Thread.sleep(500);
            cacheLocations = selectLocations(PRIORS_STORAGE_SYSTEM_GROUP_ID);
        }
        Assert.assertEquals(0, cacheLocations.size());
    }

    private void configureDevice() {
        device.setExecutor(DIRECT_EXECUTOR);
        StorageDeviceExtension storageExt = device
                .getDeviceExtension(StorageDeviceExtension.class);
        storageExt.addStorageSystemGroup(createPriorsStorageSystemGroup());
        storageExt.addStorageSystemGroup(createDefaultStorageSystemGroup());
        ArchiveAEExtension aeExt = device.getApplicationEntity("DCM4CHEE")
                .getAEExtension(ArchiveAEExtension.class);
        aeExt.setStorageSystemGroupID(DEFAULT_STORAGE_SYSTEM_GROUP_ID);
        ArchiveDeviceExtension archExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        archExt.setPriorsCacheDeleteDuplicateLocationsDelay(0);
    }

    private StorageSystemGroup createDefaultStorageSystemGroup() {
        StorageSystemGroup group = new StorageSystemGroup();
        group.setGroupID(DEFAULT_STORAGE_SYSTEM_GROUP_ID);
        group.setStorageFilePathFormat("{00080018}"); // SOP Instance UID
        StorageSystem fs = new StorageSystem();
        fs.setStorageSystemID(DEFAULT_STORAGE_SYSTEM_ID);
        fs.setStorageSystemPath(DEFAULT_STORAGE_SYSTEM_PATH.toString());
        fs.setProviderName("org.dcm4chee.storage.filesystem");
        group.addStorageSystem(fs);
        FileCache fileCache = new FileCache();
        fileCache.setProviderName("org.dcm4chee.archive.prefetch.priorscache");
        fileCache.setStorageSystemGroupID(PRIORS_STORAGE_SYSTEM_GROUP_ID);
        group.setFileCache(fileCache);
        group.activate(fs, true);
        return group;
    }

    private StorageSystemGroup createPriorsStorageSystemGroup() {
        StorageSystemGroup group = new StorageSystemGroup();
        group.setGroupID(PRIORS_STORAGE_SYSTEM_GROUP_ID);
        StorageSystem fs = new StorageSystem();
        fs.setStorageSystemID(PRIORS_STORAGE_SYSTEM_ID);
        fs.setAvailability(Availability.ONLINE);
        fs.setStorageSystemPath(PRIORS_STORAGE_SYSTEM_PATH.toString());
        fs.setProviderName("org.dcm4chee.storage.filesystem");
        group.addStorageSystem(fs);
        group.activate(fs, true);
        return group;
    }

    private void createCacheLocations() throws Exception {
        // Store and retrieve from DEFAULT storage group to create cache
        // locations
        for (String resource : INSTANCE_RESOURCES)
            storeInstance(resource);
        RetrieveContext ctx = createRetrieveContext(DEFAULT_STORAGE_SYSTEM_GROUP_ID,
                DEFAULT_STORAGE_SYSTEM_ID);
        List<Location> sourceLocations = selectLocations(DEFAULT_STORAGE_SYSTEM_GROUP_ID);
        for (Location location : sourceLocations) {
            retrieveService.getFile(ctx, location.getStoragePath());
        }
    }

    private RetrieveContext createRetrieveContext(String groupID, String systemID) {
        StorageSystem storageSystem = retrieveService.getStorageSystem(groupID, systemID);
        return retrieveService.createRetrieveContext(storageSystem);
    }

    private FileCacheProvider fileCacheProvider() {
        return createRetrieveContext(DEFAULT_STORAGE_SYSTEM_GROUP_ID,
                DEFAULT_STORAGE_SYSTEM_ID).getFileCacheProvider();
    }

    private List<Location> selectLocations(String groupID) {
        return em
                .createQuery("SELECT l FROM Location l WHERE l.storageSystemGroupID =?1",
                        Location.class).setParameter(1, groupID).getResultList();
    }

    private void storeInstance(String resourceName) throws Exception {
        ArchiveAEExtension arcAEExt = device.getApplicationEntity("DCM4CHEE")
                .getAEExtension(ArchiveAEExtension.class);
        utx.begin();
        em.joinTransaction();
        StoreSession session = storeService.createStoreSession(storeService);
        session = storeService.createStoreSession(storeService);
        session.setSource(new GenericParticipant("", "priorsFileCacheTest"));
        session.setRemoteAET("none");
        session.setArchiveAEExtension(arcAEExt);
        storeService.init(session);
        StoreContext context = storeService.createStoreContext(session);
        Attributes attrs = loadAttributes(resourceName);
        Attributes fmi = attrs.createFileMetaInformation(UID.ImplicitVRLittleEndian);
        storeService.writeSpoolFile(context, fmi, attrs);
        storeService.parseSpoolFile(context);
        storeService.store(context);
        utx.commit();
        em.clear();
    }

    private Attributes loadAttributes(String name) throws ParserConfigurationException,
            SAXException, IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return SAXReader.parse(cl.getResource(name).toString());
    }

    private static void deleteDir(Path dir) throws IOException {
        if (!Files.exists(dir))
            return;

        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                FileVisitResult result = super.visitFile(file, attrs);
                if (result == FileVisitResult.CONTINUE)
                    Files.delete(file);
                return result;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                FileVisitResult result = super.postVisitDirectory(dir, exc);
                if (result == FileVisitResult.CONTINUE)
                    Files.delete(dir);
                return result;
            }
        });
    }

    private void clearDatabase() throws NotSupportedException, SystemException,
            SecurityException, IllegalStateException, RollbackException,
            HeuristicMixedException, HeuristicRollbackException {
        utx.begin();
        for (String queryStr : DELETE_QUERIES) {
            Query query = em.createNativeQuery(queryStr);
            query.executeUpdate();
        }
        utx.commit();
    }

    @RequestScoped
    static class FetchedObserver {
        private List<Location> fetched = new ArrayList<Location>();

        public void onFetched(@Observes @Fetched Location location) {
            fetched.add(location);
        }

        List<Location> getFetched() {
            return fetched;
        }
    }
}
