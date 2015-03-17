package org.dcm4chee.archive.conf;

import java.io.Serializable;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;

@LDAP(objectClasses = "dcmMPPSEmulationRule", distinguishingField = "cn")
@ConfigurableClass
public final class MPPSEmulationRule implements Serializable {

    private static final long serialVersionUID = 8047202716204035254L;

    @ConfigurableProperty(name = "cn")
    private String commonName;

    @ConfigurableProperty(name = "dcmAETitle")
    private String[] sourceAETs;

    @ConfigurableProperty(name = "dicomAETitle")
    private String emulatorAET;

    @ConfigurableProperty(name = "dcmMPPSEmulationDelay")
    private int emulationDelay;

    @ConfigurableProperty(name = "dcmMPPSEmulationCreationRule",  defaultValue = "ALWAYS")
    private MPPSCreationRule creationRule = MPPSCreationRule.ALWAYS;    
    
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

    public MPPSCreationRule getCreationRule() {
        return creationRule;
    }

    public void setCreationRule(MPPSCreationRule creationRule) {
        this.creationRule = creationRule;
    }

}