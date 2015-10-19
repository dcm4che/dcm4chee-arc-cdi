package org.dcm4chee.archive.conf.olock;

import org.dcm4che3.conf.core.api.ConfigurableClassExtension;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.OptimisticLockException;
import org.dcm4che3.conf.core.normalization.DefaultsAndNullFilterDecorator;
import org.dcm4che3.conf.core.olock.HashBasedOptimisticLockingConfiguration;
import org.dcm4che3.conf.core.storage.InMemoryConfiguration;
import org.dcm4che3.conf.core.util.Extensions;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceTest;
import org.dcm4chee.archive.conf.defaults.DefaultArchiveConfigurationFactory.FactoryParams;
import org.dcm4chee.archive.conf.defaults.DefaultDicomConfigInitializer;
import org.dcm4chee.archive.conf.defaults.test.DeepEquals;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Roman K
 */
public class DicomConfigOptimisticLockingTests {


    private CommonDicomConfigurationWithHL7 dicomConfig;


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

        storage = new HashBasedOptimisticLockingConfiguration(storage, allExtensionClasses, storage);

        storage = new DefaultsAndNullFilterDecorator(storage, allExtensionClasses);

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
        device1.getDeviceExtension(AuditLogger.class).getConnections().clear();
        dicomConfig.merge(device1);

        //device2.getConnections().get(1).setSocketCloseDelay(12345);
//        dicomConfig.merge(device2);


        Device loaded = dicomConfig.findDevice("dcm4chee-arc");
        loaded.setOlockHash(null);
        device1.setOlockHash(null);
        boolean b = DeepEquals.deepEquals(loaded, device1);
        if (!b) DeepEquals.printOutInequality();
        Assert.assertTrue(b);


    }

        // add 2 AEs

        // add 2 AEs with concurrent changes in other AEs

        // remove 2 AEs


        // eg some items added, some deleted from the list


    @Test
    public void testNulls() {

        // what if we pesist null-object somehwere

        // what if we pesist something where null-object was before


    }


    // TODO play around with adding/removing extensions

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
