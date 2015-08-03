package org.dcm4chee.archive.conf;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.net.ApplicationEntity;

@LDAP(objectClasses = "dcmMPPSEmulationRule", distinguishingField = "cn")
@ConfigurableClass
public final class MPPSEmulationAndStudyUpdateRule implements Serializable {

    private static final long serialVersionUID = 8047202716204035254L;

    @ConfigurableProperty(name = "cn")
    private String commonName;

    @ConfigurableProperty(name = "dcmAETitle",
            label = "Source AEs",
            description= "Source Application Entities for which this rule applies. If empty, will be used as a default rule",
            collectionOfReferences = true)
    private Set<ApplicationEntity> sourceAEs = new HashSet<>();

    @ConfigurableProperty(name = "dcmMPPSEmulationDelay",
            label = "Study update/MPPS emulator delay",
            description = "After how many seconds the study update notification and MPPS emulation should be triggered")
    private int emulationDelay;

    @ConfigurableProperty(name = "dcmMPPSEmulationCreationRule", defaultValue = "ALWAYS")
    private MPPSCreationRule creationRule = MPPSCreationRule.ALWAYS;

    public MPPSEmulationAndStudyUpdateRule() {
    }

    public MPPSEmulationAndStudyUpdateRule(String commonName, Set<ApplicationEntity> sourceAEs, int emulationDelay, MPPSCreationRule creationRule) {
        this.commonName = commonName;
        this.sourceAEs = sourceAEs;
        this.emulationDelay = emulationDelay;
        this.creationRule = creationRule;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Set<ApplicationEntity> getSourceAEs() {
        return sourceAEs;
    }

    public void setSourceAEs(Set<ApplicationEntity> sourceAEs) {
        this.sourceAEs = sourceAEs;
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