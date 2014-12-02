package org.dcm4chee.archive.conf;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;

@LDAP(objectClasses = "dcmMPPSEmulationRule", distinguishingField = "cn")
@ConfigurableClass
public final class MPPSEmulationRule {

    @ConfigurableProperty(name = "cn")
    private String commonName;

    @ConfigurableProperty(name = "dcmAETitle")
    private String[] sourceAETs;

    @ConfigurableProperty(name = "dicomAETitle")
    private String emulatorAET;

    @ConfigurableProperty(name = "dcmMPPSEmulationDelay")
    private int emulationDelay;

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String[] getSourceAETs() {
        return sourceAETs;
    }

    public void setSourceAETs(String... sourceAETs) {
        this.sourceAETs = sourceAETs;
    }

    public String getEmulatorAET() {
        return emulatorAET;
    }

    public void setEmulatorAET(String emulatorAET) {
        this.emulatorAET = emulatorAET;
    }

    public int getEmulationDelay() {
        return emulationDelay;
    }

    public void setEmulationDelay(int emulationDelay) {
        this.emulationDelay = emulationDelay;
    }

}