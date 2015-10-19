package org.dcm4chee.archive.conf;

import org.dcm4che3.conf.core.api.ConfigurableClassExtension;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.storage.InMemoryConfiguration;
import org.dcm4che3.conf.core.util.Extensions;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.defaults.DefaultArchiveConfigurationFactory;
import org.dcm4chee.archive.conf.defaults.DefaultArchiveConfigurationFactory.FactoryParams;
import org.dcm4chee.archive.conf.defaults.DefaultDicomConfigInitializer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Roman K
 */
@Ignore
public class ArcConfPerformanceTest {

    private CommonDicomConfigurationWithHL7 dicomConfig;

    @Before
    public void before() {

        // prepare storage
        ArrayList<Class> allExtensionClasses = new ArrayList<Class>();
        for (ConfigurableClassExtension extension : ArchiveDeviceTest.getDefaultExtensions())
            allExtensionClasses.add(extension.getClass());

//        Configuration storage = new SingleJsonFileConfigurationStorage("target/config.json");
        Configuration storage = new InMemoryConfiguration();

//        storage = new OptimisticLockingConfiguration(storage, allExtensionClasses, storage);

        dicomConfig = new CommonDicomConfigurationWithHL7(
                storage,
                Extensions.getAMapOfExtensionsByBaseExtension(ArchiveDeviceTest.getDefaultExtensions())
        );


        // wipe out clean
        dicomConfig.purgeConfiguration();

    }

    ;

    @Test
    public void manyDevices() {

        //persist default
        DefaultDicomConfigInitializer init = new DefaultDicomConfigInitializer();
        FactoryParams params = new FactoryParams();
        params.generateUUIDsBasedOnName = true;
        params.useGroupBasedTCConfig = false;

        DefaultArchiveConfigurationFactory defaultArchiveConfigurationFactory = new DefaultArchiveConfigurationFactory(params);
        Device myArr = defaultArchiveConfigurationFactory.createARRDevice("myArr", Protocol.SYSLOG_UDP, 105);
        dicomConfig.persist(myArr);

        for (int i = 0; i < 500; i++) {
            dicomConfig.persist(defaultArchiveConfigurationFactory.createArchiveDevice("archive" + i, myArr));
        }

        System.out.println("persisted");

        for (int i = 0; i < 10; i++) {
            System.out.println("querying");
            System.out.println(dicomConfig.listDeviceNames());
        }

        for (int j = 0; j < 10; j++)
            for (int i = 0; i < 500; i++) {
                dicomConfig.findDevice("archive" + i);
                System.out.println("device " + i + " loaded");
            }


        }
    }
