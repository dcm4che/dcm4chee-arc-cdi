package org.dcm4chee.archive.conf;

import java.io.Serializable;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.Code;
import org.dcm4che3.util.AttributesFormat;

@LDAP(objectClasses = "dcmArchivingRule", distinguishingField = "cn")
@ConfigurableClass
public final class ArchivingRule
    implements Comparable<ArchivingRule>, Serializable {

    private static final long serialVersionUID = -9026606766186878147L;

    @ConfigurableProperty(name = "cn")
    private String commonName;

    @LDAP(noContainerNode = true)
    @ConfigurableProperty
    private Condition condition = new Condition();

    @ConfigurableProperty(name = "dcmDelayAfterInstanceStored")
    private int delayAfterInstanceStored;

    @ConfigurableProperty(name = "dcmStorageSystemGroupID")
    private String[] storageSystemGroupIDs;

    @ConfigurableProperty(name = "dcmStorageFilePathFormat")
    private AttributesFormat storageFilePathFormat;

    @ConfigurableProperty(name = "dcmDelayReasonCode")
    private Code delayReasonCode;

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String[] getStorageSystemGroupIDs() {
        return storageSystemGroupIDs;
    }

    public void setStorageSystemGroupIDs(String ... storageSystemGroupIDs) {
        this.storageSystemGroupIDs = storageSystemGroupIDs;
    }

    public AttributesFormat getStorageFilePathFormat() {
        return storageFilePathFormat;
    }

    public void setStorageFilePathFormat(AttributesFormat storageFilePathFormat) {
        this.storageFilePathFormat = storageFilePathFormat;
    }

    public int getDelayAfterInstanceStored() {
        return delayAfterInstanceStored;
    }

    public void setDelayAfterInstanceStored(int delayAfterInstanceStored) {
        this.delayAfterInstanceStored = delayAfterInstanceStored;
    }

    public Code getDelayReasonCode() {
        return delayReasonCode;
    }

    public void setDelayReasonCode(Code delayReasonCode) {
        this.delayReasonCode = delayReasonCode;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition != null ? condition : new Condition();
    }

    public boolean matchesCondition(String deviceName, String aeTitle,
            String institutionName, String institutionalDepartmentName,
            String modality) {
        return condition.matches(deviceName, aeTitle, institutionName,
                institutionalDepartmentName, modality);
    }

    public String[] getDeviceNames() {
        return condition.getDeviceNames();
    }

    public void setDeviceNames(String[] deviceNames) {
        condition.setDeviceNames(deviceNames);
    }

    public String[] getAeTitles() {
        return condition.getAeTitles();
    }

    public void setAeTitles(String[] aeTitles) {
        condition.setAeTitles(aeTitles);
    }

    public String[] getInstitutionNames() {
        return condition.getInstitutionNames();
    }

    public void setInstitutionNames(String[] institutionNames) {
        condition.setInstitutionNames(institutionNames);
    }

    public String[] getInstitutionalDepartmentNames() {
        return condition.getInstitutionalDepartmentNames();
    }

    public void setInstitutionalDepartmentNames(
            String[] institutionalDepartmentNames) {
        condition.setInstitutionalDepartmentNames(institutionalDepartmentNames);
    }

    public String[] getModalities() {
        return condition.getModalities();
    }

    public void setModalities(String[] modalities) {
        condition.setModalities(modalities);
    }

    @Override
    public int compareTo(ArchivingRule o) {
        return condition.compareTo(o.condition);
    }

    @ConfigurableClass
    public static class Condition
            implements Comparable<Condition>, Serializable {

        private static final long serialVersionUID = 1986950374484515200L;

        @ConfigurableProperty(name = "dcmDeviceName")
        private String[] deviceNames;
    
        @ConfigurableProperty(name = "dcmAETitle")
        private String[] aeTitles;

        @ConfigurableProperty(name = "dicomInstitutionName")
        private String[] institutionNames;

        @ConfigurableProperty(name = "dicomInstitutionalDepartmentName")
        private String[] institutionalDepartmentNames;

        @ConfigurableProperty(name = "dcmModality")
        private String[] modalities;

        int weight = -1;

        public Condition() {
        }

        public String[] getDeviceNames() {
            return deviceNames;
        }

        public void setDeviceNames(String[] deviceNames) {
            this.deviceNames = deviceNames;
            weight = -1;
        }

        public String[] getAeTitles() {
            return aeTitles;
        }

        public void setAeTitles(String[] aeTitles) {
            this.aeTitles = aeTitles;
            weight = -1;
        }

        public String[] getInstitutionNames() {
            return institutionNames;
        }

        public void setInstitutionNames(String[] institutionNames) {
            this.institutionNames = institutionNames;
            weight = -1;
       }

        public String[] getInstitutionalDepartmentNames() {
            return institutionalDepartmentNames;
        }

        public void setInstitutionalDepartmentNames(
                String[] institutionalDepartmenNames) {
            this.institutionalDepartmentNames = institutionalDepartmenNames;
            weight = -1;
        }

        public String[] getModalities() {
            return modalities;
        }

        public void setModalities(String[] modalities) {
            this.modalities = modalities;
            weight = -1;
        }

        private int weight() {
            if (weight < 0)
                weight = (aeTitles.length != 0 ? 16 : 0)
                    + (deviceNames.length != 0 ? 8 : 0)
                    + (institutionNames.length != 0 ? 4 : 0)
                    + (institutionalDepartmentNames.length != 0 ? 2 : 0)
                    + (modalities.length != 0 ? 1 : 0);
            return weight;
        }

        @Override
        public int compareTo(Condition o) {
            return o.weight() - weight();
        }

        public boolean matches(String deviceName, String aeTitle,
                String institutionName, String institutionalDepartmenName,
                String modality) {
            return isEmptyOrContains(this.deviceNames, deviceName)
                    && isEmptyOrContains(this.aeTitles, aeTitle)
                    && isEmptyOrContains(this.institutionNames, institutionName)
                    && isEmptyOrContains(this.institutionalDepartmentNames,
                            institutionalDepartmenName)
                    && isEmptyOrContains(this.modalities, modality);

        }
    }

    private static boolean isEmptyOrContains(Object[] a, Object o) {
        if (o == null || a == null || a.length == 0)
            return true;

        for (int i = 0; i < a.length; i++)
            if (o.equals(a[i]))
                return true;

        return false;
    }

}