package org.dcm4chee.archive.conf;

import org.dcm4che3.conf.api.generic.ConfigClass;
import org.dcm4che3.conf.api.generic.ConfigField;

@ConfigClass(objectClass = "dcmMPPSEmulationRule")
public final class MPPSEmulationRule {

    @ConfigField(name = "cn")
    private String commonName;

    @ConfigField(name = "dcmAETitle")
    private String[] sourceAETs;

    @ConfigField(name = "dicomAETitle")
    private String emulatorAET;

    @ConfigField(name = "dcmMPPSEmulationDelay")
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