package org.dcm4chee.archive.conf.defaults;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.TCConfiguration;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roman K
 */
public class DefaultDicomConfigInitializer {

    private static Logger log = LoggerFactory.getLogger(DefaultDicomConfigInitializer.class);

    private Device arrDevice;
    private Device arc;


    public DefaultDicomConfigInitializer() {
    }

    public Device getArrDevice() {
        return arrDevice;
    }

    public Device getArc() {
        return arc;
    }

    public DefaultDicomConfigInitializer persistDefaultConfig(DicomConfiguration config,
                                                              HL7Configuration hl7Config,
                                                              String baseStoragePath,
                                                              boolean isUseGroupBasedTCConfig) throws Exception {


        log.debug("Creating external devices with AEs");

        for (int i = 0; i < DefaultDeviceFactory.OTHER_AES.length; i++) {
            String aet = DefaultDeviceFactory.OTHER_AES[i];
            config.persist(DefaultDeviceFactory.createDevice(DefaultDeviceFactory.OTHER_DEVICES[i], DefaultDeviceFactory.OTHER_ISSUER[i], DefaultDeviceFactory.OTHER_INST_CODES[i],
                    aet, "localhost", DefaultDeviceFactory.OTHER_PORTS[i << 1], DefaultDeviceFactory.OTHER_PORTS[(i << 1) + 1]));
        }

        log.debug("Creating other external devices");

        hl7Config.registerHL7Application(DefaultDeviceFactory.PIX_MANAGER);
        for (int i = DefaultDeviceFactory.OTHER_AES.length; i < DefaultDeviceFactory.OTHER_DEVICES.length; i++)
            config.persist(DefaultDeviceFactory.createDevice(DefaultDeviceFactory.OTHER_DEVICES[i]));

        config.persist(DefaultDeviceFactory.createHL7Device("hl7rcv", DefaultDeviceFactory.SITE_A, DefaultDeviceFactory.INST_A, DefaultDeviceFactory.PIX_MANAGER,
                "localhost", 2576, 12576));

        log.debug("Creating arr device");
        arrDevice = DefaultDeviceFactory.createARRDevice("syslog", Connection.Protocol.SYSLOG_UDP, 514);
        config.persist(arrDevice);

        log.debug("Creating archive device");
        DefaultDeviceFactory defaultDeviceFactory = new DefaultDeviceFactory();
        defaultDeviceFactory.setBaseStoragePath(baseStoragePath);
        defaultDeviceFactory.setUseGroupBasedTCConfig(isUseGroupBasedTCConfig);
        arc = defaultDeviceFactory.createArchiveDevice("dcm4chee-arc", arrDevice);
        config.persist(arc);



        // create TC Group config extension
        if (isUseGroupBasedTCConfig) {
            log.debug("Creating transfer capability groups");
            TCConfiguration.persistDefaultTCGroups(config);
        }

        return this;
    }
}
