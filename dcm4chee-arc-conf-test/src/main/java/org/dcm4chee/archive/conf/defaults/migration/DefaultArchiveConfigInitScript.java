package org.dcm4chee.archive.conf.defaults.migration;

import org.dcm4che3.conf.api.migration.MigrationScript;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4chee.archive.conf.defaults.DefaultDicomConfigInitializer;

/**
 * This script initializes default archive configuration in case there are no devices yet in the config
 *
 * @author Roman K
 */
public class DefaultArchiveConfigInitScript implements MigrationScript {
    @Override
    public void migrate(MigrationContext migrationContext) throws ConfigurationException {

        // run only if no version is specified
        if (migrationContext.getFromVersion().equals(NO_VERSION)) {

            try {
                new DefaultDicomConfigInitializer()
                        .persistDefaultConfig(
                                migrationContext.getDicomConfiguration(),
                                migrationContext.getDicomConfiguration().getDicomConfigurationExtension(HL7Configuration.class)
                        );
            } catch (Exception e) {
                throw new ConfigurationException("Cannot initialize default config");
            }

        };


    }
}
