package org.dcm4chee.archive.conf.defaults;

import org.dcm4che3.conf.api.upgrade.ScriptVersion;
import org.dcm4che3.conf.api.upgrade.UpgradeScript;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This script initializes default archive configuration in case there are no devices yet in the config
 *
 * @author Roman K
 */
@ScriptVersion("08.2015")
public class DefaultArchiveConfigInitScript implements UpgradeScript {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultArchiveConfigInitScript.class);

    @Override
    public void upgrade(UpgradeContext upgradeContext) throws ConfigurationException {

        LOG.info("Running default config init script. Last executed version is " + upgradeContext.getUpgradeScriptMetadata().getLastVersionExecuted());

        // run only if no version is specified
        if (upgradeContext.getFromVersion().equals(NO_VERSION)) {

            LOG.info("Persisting default devices...");

            try {
                DefaultArchiveConfigurationFactory.FactoryParams params = new DefaultArchiveConfigurationFactory.FactoryParams();
                params.baseStoragePath=upgradeContext.getProperties().getProperty("org.dcm4che.config.init.baseStorageDir", "/var/local/dcm4chee-arc/");

                if (upgradeContext.getProperties().containsKey("timeout")) {
                    params.socketTimeout = Integer.valueOf(upgradeContext.getProperties().getProperty("timeout"));
                }

                params.useGroupBasedTCConfig = true;

                new DefaultDicomConfigInitializer()
                        .persistDefaultConfig(
                                upgradeContext.getDicomConfiguration(),
                                upgradeContext.getDicomConfiguration().getDicomConfigurationExtension(HL7Configuration.class),
                                params);
            } catch (Exception e) {
                throw new ConfigurationException("Cannot initialize default config",e);
            }

        } else {
            LOG.info("Configuration version is non-null ({}), not persisting any devices", upgradeContext.getFromVersion());
        };


    }
}
