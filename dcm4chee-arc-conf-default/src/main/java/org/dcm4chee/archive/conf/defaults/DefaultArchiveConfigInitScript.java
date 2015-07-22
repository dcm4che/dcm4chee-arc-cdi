package org.dcm4chee.archive.conf.defaults;

import org.dcm4che3.conf.api.upgrade.UpgradeScript;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;

/**
 * This script initializes default archive configuration in case there are no devices yet in the config
 *
 * @author Roman K
 */
public class DefaultArchiveConfigInitScript implements UpgradeScript {
    @Override
    public void upgrade(UpgradeContext upgradeContext) throws ConfigurationException {


        // run only if no version is specified
        if (upgradeContext.getFromVersion().equals(NO_VERSION)) {

            try {
                DefaultArchiveConfigurationFactory.FactoryParams params = new DefaultArchiveConfigurationFactory.FactoryParams();
                params.baseStoragePath=upgradeContext.getProperties().getProperty("org.dcm4che.config.init.baseStorageDir", "/var/local/dcm4chee-arc/");
                params.useGroupBasedTCConfig = true;


                new DefaultDicomConfigInitializer()
                        .persistDefaultConfig(
                                upgradeContext.getDicomConfiguration(),
                                upgradeContext.getDicomConfiguration().getDicomConfigurationExtension(HL7Configuration.class),
                                params);
            } catch (Exception e) {
                throw new ConfigurationException("Cannot initialize default config",e);
            }

        };


    }
}
