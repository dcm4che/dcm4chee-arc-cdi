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
                                                              DefaultArchiveConfigurationFactory.FactoryParams params) {

        DefaultArchiveConfigurationFactory defaultArchiveConfigurationFactory = new DefaultArchiveConfigurationFactory(params);

        log.debug("Creating external devices with AEs");

        for (int i = 0; i < DefaultArchiveConfigurationFactory.OTHER_AES.length; i++) {
            String aet = DefaultArchiveConfigurationFactory.OTHER_AES[i];
            config.persist(defaultArchiveConfigurationFactory.createDevice(
                    DefaultArchiveConfigurationFactory.OTHER_DEVICES[i],
                    DefaultArchiveConfigurationFactory.OTHER_ISSUER[i],
                    DefaultArchiveConfigurationFactory.OTHER_INST_CODES[i],
                    aet,
                    "localhost",
                    DefaultArchiveConfigurationFactory.OTHER_PORTS[i << 1],
                    DefaultArchiveConfigurationFactory.OTHER_PORTS[(i << 1) + 1]));
        }

        log.debug("Creating other external devices");

        hl7Config.registerHL7Application(DefaultArchiveConfigurationFactory.PIX_MANAGER);
        for (int i = DefaultArchiveConfigurationFactory.OTHER_AES.length; i < DefaultArchiveConfigurationFactory.OTHER_DEVICES.length; i++)
            config.persist(defaultArchiveConfigurationFactory.createDevice(DefaultArchiveConfigurationFactory.OTHER_DEVICES[i]));

        config.persist(defaultArchiveConfigurationFactory.createHL7Device("hl7rcv", DefaultArchiveConfigurationFactory.SITE_A, DefaultArchiveConfigurationFactory.INST_A, DefaultArchiveConfigurationFactory.PIX_MANAGER,
                "localhost", 2576, 12576));

        log.debug("Creating arr device");
        arrDevice = defaultArchiveConfigurationFactory.createARRDevice("syslog", Connection.Protocol.SYSLOG_UDP, 514);
        config.persist(arrDevice);

        log.debug("Creating archive device");

        arc = defaultArchiveConfigurationFactory.createArchiveDevice("dcm4chee-arc", arrDevice);
        config.persist(arc);



        // create TC Group config extension
        if (params.useGroupBasedTCConfig) {
            log.debug("Creating transfer capability groups");
            TCConfiguration.persistDefaultTCGroups(config);
        }

        return this;
    }
}
