package org.dcm4chee.archive.conf.olock;

import org.dcm4che3.conf.core.api.ConfigurableClassExtension;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.OptimisticLockException;
import org.dcm4che3.conf.core.normalization.DefaultsAndNullFilterDecorator;
import org.dcm4che3.conf.core.olock.HashBasedOptimisticLockingConfiguration;
import org.dcm4che3.conf.core.storage.InMemoryConfiguration;
import org.dcm4che3.conf.core.util.Extensions;
import org.dcm4che3.conf.dicom.CommonDicomConfiguration;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.conf.dicom.DicomPath;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4chee.archive.conf.*;
import org.dcm4chee.archive.conf.defaults.DefaultArchiveConfigurationFactory.FactoryParams;
import org.dcm4chee.archive.conf.defaults.DefaultDicomConfigInitializer;
import org.dcm4chee.archive.conf.defaults.ExtendedStudyDictionary;
import org.dcm4chee.archive.conf.defaults.test.DeepEquals;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.SyncPolicy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Roman K
 */
public class DicomConfigOptimisticLockingTests {


    private CommonDicomConfigurationWithHL7 dicomConfig;

    private Configuration bareStorage;


    @Before
    public void before() {

        // prepare storage
        ArrayList<ConfigurableClassExtension> defaultExtensions = ArchiveDeviceTest.getDefaultExtensions();
        defaultExtensions.add(new OlockedDeviceExtension());

        ArrayList<Class> allExtensionClasses = new ArrayList<Class>();

        for (ConfigurableClassExtension extension : defaultExtensions)
            allExtensionClasses.add(extension.getClass());

//        Configuration storage = new SingleJsonFileConfigurationStorage("target/config.json");
        Configuration storage = new InMemoryConfiguration();

        bareStorage = storage;

        storage = new HashBasedOptimisticLockingConfiguration(storage, allExtensionClasses, storage);

        storage = new DefaultsAndNullFilterDecorator(storage, allExtensionClasses, CommonDicomConfiguration.createDefaultDicomVitalizer());

        dicomConfig = new CommonDicomConfigurationWithHL7(
                storage,
                Extensions.getAMapOfExtensionsByBaseExtension(defaultExtensions)
        );


        // wipe out clean
        dicomConfig.purgeConfiguration();

        // init default config
        DefaultDicomConfigInitializer init = new DefaultDicomConfigInitializer();

        FactoryParams params = new FactoryParams();
        params.generateUUIDsBasedOnName = true;
        params.useGroupBasedTCConfig = true;

        init.persistDefaultConfig(dicomConfig, dicomConfig, params);

    }


    @Test
    public void optimisticLockingDemoTest() {

        //// three users load the config at the same time
        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");
        Device device3 = dicomConfig.findDevice("dcm4chee-arc");

        //// one user modifies the port of a connection
        device1.getConnections().get(0).setPort(12121);
        dicomConfig.merge(device1);

        //// second user tries to modify the same connection but fails
        device2.getConnections().get(0).setBindAddress("anotherhost");
        try {
            dicomConfig.merge(device2);
            Assert.fail("Should not succeed");
        } catch (OptimisticLockException ignored) {
        }

        //// third user adds a storage system. This change does not conflict with connection settings, so it succeeds
        // create new storage system
        StorageSystem fs2 = new StorageSystem();
        fs2.setStorageSystemID("fs2");
        fs2.setProviderName("org.dcm4chee.storage.filesystem");
        fs2.setStorageSystemPath("someNewPath");
        fs2.setAvailability(Availability.OFFLINE);
        fs2.setSyncPolicy(SyncPolicy.ON_ASSOCIATION_CLOSE);
        // add it
        device3.getDeviceExtension(StorageDeviceExtension.class).getStorageSystemGroup("DEFAULT").addStorageSystem(fs2);
        // merge
        dicomConfig.merge(device3);

        //// the resulting device includes both changes from first and third user
        Device loaded = dicomConfig.findDevice("dcm4chee-arc");
        Assert.assertEquals(12121, loaded.getConnections().get(0).getPort());
        Assert.assertNotNull(loaded.getDeviceExtension(StorageDeviceExtension.class).getStorageSystemGroup("DEFAULT").getStorageSystem("fs2"));

    }


    @Test
    public void persistNodeHavingIntegerKeyedMap() {
        Device device = dicomConfig.findDevice("dcm4chee-arc");
        ArchiveDeviceExtension archExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        Map<Integer, String> STUDY_PRIVATE_ATTRS = new TreeMap<Integer, String>();
        STUDY_PRIVATE_ATTRS.put(ExtendedStudyDictionary.StudyLastUpdateDateTime, "EXTENDED STUDY");
        AttributeFilter filter = new AttributeFilter(new int[]{Tag.StudyID}, STUDY_PRIVATE_ATTRS);

        // This will set the dcmPrivateTag in AttributeFilter which is a map
        // that has an Integer key type
        archExt.setAttributeFilter(Entity.Study, filter);

        dicomConfig.merge(device);
    }

    @Test
    public void testPersistDeviceNoHash() {

        // Make changes, then persist original node without hashes - should fully overwrite the existing node

        Object exportedDeviceNode = bareStorage.getConfigurationNode(DicomPath.DeviceByName.set("deviceName", "dcm4chee-arc").path(), null);

        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        device1.setDescription("a new description");
        device1.getApplicationEntity("DCM4CHEE").setDescription("another new desc");
        device1.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).setIANMaxRetries(100);

        dicomConfig.merge(device1);

        // check if changes persisted
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");
        Assert.assertEquals(device1.getDescription(), device2.getDescription());
        Assert.assertEquals(device1.getApplicationEntity("DCM4CHEE").getDescription(), device2.getApplicationEntity("DCM4CHEE").getDescription());
        Assert.assertEquals(
                device1.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getIANMaxRetries(),
                device2.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getIANMaxRetries()
        );

        // persist original node with no hashes
        dicomConfig.getConfigurationStorage().persistNode(
                DicomPath.DeviceByName.set("deviceName", "dcm4chee-arc").path(),
                (Map<String, Object>) exportedDeviceNode,
                Device.class
        );

        // all changes should be reverted
        Device device3 = dicomConfig.findDevice("dcm4chee-arc");
        Assert.assertNotEquals(device1.getDescription(), device3.getDescription());
        Assert.assertNotEquals(device1.getApplicationEntity("DCM4CHEE").getDescription(), device3.getApplicationEntity("DCM4CHEE").getDescription());
        Assert.assertNotEquals(
                device1.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getIANMaxRetries(),
                device3.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getIANMaxRetries()
        );


    }


    @Test
    public void testFullImportExport() {

        // Make changes, then persist original ROOT node without hashes - should fully overwrite the existing config

        Object exportedConfig = dicomConfig.getConfigurationStorage().getConfigurationNode("/", null);
//        Object exportedConfig = dicomConfig.getConfigurationStorage().getConfigurationRoot();


        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        device1.setDescription("a new description");
        device1.getApplicationEntity("DCM4CHEE").setDescription("another new desc");
        device1.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).setIANMaxRetries(100);

        dicomConfig.merge(device1);

        // check if changes persisted
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");
        Assert.assertEquals(device1.getDescription(), device2.getDescription());
        Assert.assertEquals(device1.getApplicationEntity("DCM4CHEE").getDescription(), device2.getApplicationEntity("DCM4CHEE").getDescription());
        Assert.assertEquals(
                device1.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getIANMaxRetries(),
                device2.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getIANMaxRetries()
        );

        // persist original root node with no hashes
        dicomConfig.getConfigurationStorage().persistNode("/", (Map<String, Object>) exportedConfig, null);

        // all changes should be reverted
        Device device3 = dicomConfig.findDevice("dcm4chee-arc");
        Assert.assertNotEquals(device1.getDescription(), device3.getDescription());
        Assert.assertNotEquals(device1.getApplicationEntity("DCM4CHEE").getDescription(), device3.getApplicationEntity("DCM4CHEE").getDescription());
        Assert.assertNotEquals(
                device1.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getIANMaxRetries(),
                device3.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getIANMaxRetries()
        );


    }

    @Test
    public void testMultiDepthMerge() {


        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");
        Device device3 = dicomConfig.findDevice("dcm4chee-arc");
        Device device4 = dicomConfig.findDevice("dcm4chee-arc");

        // modify device and ae ext
        device1.setLimitOpenAssociations(10);
        device1.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).setModifyingSystem("asdgf");


        dicomConfig.merge(device1);

        // modify ae param
        device2.getApplicationEntity("DCM4CHEE").getConnections().remove(0);
        device2.getApplicationEntity("DCM4CHEE").setAeInstalled(false);
        dicomConfig.merge(device2);


        // none of conflicting should succeed
        device3.setDescription("bla");
        try {
            dicomConfig.merge(device3);
            Assert.fail("Should have failed");
        } catch (OptimisticLockException e) {
            //noop
        }

        device4.getApplicationEntity("DCM4CHEE").setDescription("blabla");
        try {
            dicomConfig.merge(device4);
            Assert.fail("Should have failed");
        } catch (OptimisticLockException e) {
            //noop
        }


        // all 4 changes should make it

        Device loaded = dicomConfig.findDevice("dcm4chee-arc");

        Assert.assertEquals(10, loaded.getLimitOpenAssociations());
        Assert.assertEquals("asdgf", loaded.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getModifyingSystem());
        Assert.assertEquals(1, device2.getApplicationEntity("DCM4CHEE").getConnections().size());
        Assert.assertEquals(false, device2.getApplicationEntity("DCM4CHEE").getAeInstalled());


        // make sure nothing else changed but what we changed and the hashes
        device2.setLimitOpenAssociations(10);
        device2.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).setModifyingSystem("asdgf");
        device2.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).setOlockHash(
                loaded.getApplicationEntity("DCM4CHEE").getAEExtension(ArchiveAEExtension.class).getOlockHash()
        );
        device2.getApplicationEntity("DCM4CHEE").setOlockHash(
                loaded.getApplicationEntity("DCM4CHEE").getOlockHash()
        );
        device2.setOlockHash(loaded.getOlockHash());
        boolean condition = DeepEquals.deepEquals(loaded, device2);
        if (!condition) DeepEquals.printOutInequality();
        Assert.assertTrue(condition);
    }


    @Test
    public void renameAEtest() {

        // rename AE

        Device device1 = dicomConfig.findDevice("dcm4chee-arc");

        device1.getApplicationEntity("DCM4CHEE").setAETitle("DCM4CHEE-NEW");

        dicomConfig.merge(device1);

        Device loaded = dicomConfig.findDevice("dcm4chee-arc");

        Assert.assertEquals(4, loaded.getApplicationAETitles().size());
        Assert.assertNotEquals(null, loaded.getApplicationEntity("DCM4CHEE-NEW"));
        Assert.assertEquals(null, loaded.getApplicationEntity("DCM4CHEE"));

    }


    @Test
    public void renameAEconcurrentModTest() {

        // rename AE with concurrent change in that AE

        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");

        device1.getApplicationEntity("DCM4CHEE").setAETitle("DCM4CHEE-NEW");

        dicomConfig.merge(device1);


        device2.getApplicationEntity("DCM4CHEE").setDescription("A new descr");
        dicomConfig.merge(device2);


        Device loaded = dicomConfig.findDevice("dcm4chee-arc");
        loaded.getApplicationEntity("DCM4CHEE-NEW").setOlockHash(null);
        device1.getApplicationEntity("DCM4CHEE-NEW").setOlockHash(null);
        loaded.setOlockHash(null);
        device1.setOlockHash(null);
        boolean b = DeepEquals.deepEquals(loaded, device1);
        if (!b) DeepEquals.printOutInequality();
        Assert.assertTrue("Changes in device2 should be ignored, nothing should change", b);

        // now first make change, then rename AE

        before();

        device1 = dicomConfig.findDevice("dcm4chee-arc");
        device2 = dicomConfig.findDevice("dcm4chee-arc");


        device2.getApplicationEntity("DCM4CHEE").setDescription("A new descr");
        dicomConfig.merge(device2);

        device1.getApplicationEntity("DCM4CHEE").setAETitle("DCM4CHEE-NEW");
        dicomConfig.merge(device1);


        loaded = dicomConfig.findDevice("dcm4chee-arc");
        loaded.getApplicationEntity("DCM4CHEE-NEW").setOlockHash(null);
        device1.getApplicationEntity("DCM4CHEE-NEW").setOlockHash(null);
        loaded.setOlockHash(null);
        device1.setOlockHash(null);
        b = DeepEquals.deepEquals(loaded, device1);
        if (!b) DeepEquals.printOutInequality();
        Assert.assertTrue("Changes in device2 should be ignored, nothing should change", b);

    }

    @Test
    public void doubleRenameAETest() {
        // rename AE with concurrent change in that AE

        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");

        device1.getApplicationEntity("DCM4CHEE").setAETitle("DCM4CHEE-NEW");

        dicomConfig.merge(device1);


        device2.getApplicationEntity("DCM4CHEE").setAETitle("DCM4CHEE-NEW-BUT-DIFF");


        try {
            dicomConfig.merge(device2);
            Assert.fail();
        } catch (OptimisticLockException ignored) {
        }


        Device loaded = dicomConfig.findDevice("dcm4chee-arc");
        loaded.getApplicationEntity("DCM4CHEE-NEW").setOlockHash(null);
        device1.getApplicationEntity("DCM4CHEE-NEW").setOlockHash(null);
        loaded.setOlockHash(null);
        device1.setOlockHash(null);
        boolean b = DeepEquals.deepEquals(loaded, device1);
        if (!b) DeepEquals.printOutInequality();
        Assert.assertTrue("State in the storage should be eq to device1", b);


    }

    @Test
    public void renameAEAndConcurrentlyModifyOtherAE() {

        // rename AE with concurrent change in that AE

        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");

        device1.getApplicationEntity("DCM4CHEE").setAETitle("DCM4CHEE-NEW");

        dicomConfig.merge(device1);


        device2.getApplicationEntity("DCM4CHEE_TRASH").setDescription("A new descr");
        dicomConfig.merge(device2);


        Device loaded = dicomConfig.findDevice("dcm4chee-arc");


        Assert.assertNotEquals("both changes should make it", null, loaded.getApplicationEntity("DCM4CHEE-NEW"));
        Assert.assertEquals("both changes should make it", "A new descr", loaded.getApplicationEntity("DCM4CHEE_TRASH").getDescription());


        loaded.getApplicationEntity("DCM4CHEE-NEW").setOlockHash(null);
        device1.getApplicationEntity("DCM4CHEE-NEW").setOlockHash(null);
        loaded.getApplicationEntity("DCM4CHEE_TRASH").setOlockHash(null);
        device1.getApplicationEntity("DCM4CHEE_TRASH").setOlockHash(null);
        loaded.getApplicationEntity("DCM4CHEE_TRASH").setDescription(null);
        loaded.setOlockHash(null);
        device1.setOlockHash(null);
        boolean b = DeepEquals.deepEquals(loaded, device1);
        if (!b) DeepEquals.printOutInequality();
        Assert.assertTrue("nothing else should change", b);

        // now first make change, then rename AE

    }

    public void extensionsTest() {

        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");


    }

    @Test
    public void oneModifiesConnectionAnotherRemovesApreviousConnection() {

        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");


        device1.getConnections().remove(0);
        for (ApplicationEntity entity : device1.getApplicationEntities()) {
            entity.getConnections().remove(0);
        }

        device1.getDeviceExtension(AuditLogger.class).getConnections().clear();
        dicomConfig.merge(device1);

        device2.getConnections().get(1).setSocketCloseDelay(12345);
        device2.getConnections().get(0).setSocketCloseDelay(23456);
        dicomConfig.merge(device2);


        Device loaded = dicomConfig.findDevice("dcm4chee-arc");
        Assert.assertEquals(12345, loaded.getConnections().get(0).getSocketCloseDelay());


        // the rest should be identical

        loaded.setOlockHash(null);
        device1.setOlockHash(null);

        loaded.getConnections().get(0).setSocketCloseDelay(50);

        for (ApplicationEntity entity : loaded.getApplicationEntities()) {
            entity.setOlockHash(null);
            for (Connection connection : entity.getConnections()) connection.setOlockHash(null);

        }

        for (ApplicationEntity entity : device1.getApplicationEntities()) {
            entity.setOlockHash(null);
            for (Connection connection : entity.getConnections()) connection.setOlockHash(null);
        }


        boolean b = DeepEquals.deepEquals(loaded, device1);
        if (!b) DeepEquals.printOutInequality();
        Assert.assertTrue(b);


    }


    @Test
    public void twoModifiedConnectionsOneAddedOneFails() {

        Device device1 = dicomConfig.findDevice("dcm4chee-arc");
        Device device2 = dicomConfig.findDevice("dcm4chee-arc");
        Device device3 = dicomConfig.findDevice("dcm4chee-arc");
        Device device4 = dicomConfig.findDevice("dcm4chee-arc");


        Connection conn = new Connection();
        conn.setCommonName("newConn");
        conn.setPort(12346);
        device1.addConnection(conn);
        dicomConfig.merge(device1);

        device2.getConnections().get(0).setSocketCloseDelay(23456);
        dicomConfig.merge(device2);

        device3.getConnections().get(1).setSocketCloseDelay(12345);
        dicomConfig.merge(device3);

        device4.getConnections().remove(0);

        try {
            dicomConfig.merge(device4);
            Assert.fail();
        } catch (OptimisticLockException ignored) {
        }


        Device loaded = dicomConfig.findDevice("dcm4chee-arc");
        Assert.assertEquals(23456, loaded.getConnections().get(0).getSocketCloseDelay());
        Assert.assertEquals(12345, loaded.getConnections().get(1).getSocketCloseDelay());
        Assert.assertEquals(6, loaded.getConnections().size());


        // the rest should be identical

        loaded.setOlockHash(null);
        device1.setOlockHash(null);

        loaded.getConnections().get(0).setSocketCloseDelay(50);
        loaded.getConnections().get(1).setSocketCloseDelay(50);

        for (Connection connection : device1.getConnections()) connection.setOlockHash(null);
        for (Connection connection : loaded.getConnections()) connection.setOlockHash(null);

        for (ApplicationEntity entity : loaded.getApplicationEntities()) {
            entity.setOlockHash(null);

        }

        for (ApplicationEntity entity : device1.getApplicationEntities()) {
            entity.setOlockHash(null);
        }


        boolean b = DeepEquals.deepEquals(loaded, device1);
        if (!b) DeepEquals.printOutInequality();
        Assert.assertTrue(b);


    }


    @Test
    public void testRandomScenarios() throws Exception {

        Device device = dicomConfig.findDevice("dcm4chee-arc");
        Device deviceCopy = dicomConfig.findDevice("dcm4chee-arc");
        Device device3 = dicomConfig.findDevice("dcm4chee-arc");

        // change ae ref
        device.setDefaultAE(device.getApplicationEntity("DCM4CHEE_ADMIN"));
        dicomConfig.merge(device);

        // conflicting change
        deviceCopy.setLimitOpenAssociations(25);


        try {
            dicomConfig.merge(deviceCopy);
            Assert.fail("Should have failed");
        } catch (OptimisticLockException e) {
            //it correct
        }

        // no changes
        dicomConfig.merge(device3);

    }
}
