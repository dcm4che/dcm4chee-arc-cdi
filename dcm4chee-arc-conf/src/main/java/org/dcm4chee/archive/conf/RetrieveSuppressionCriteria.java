package org.dcm4chee.archive.conf;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;

@LDAP(objectClasses = "dcmRetrieveSuppressionCriteria")
@ConfigurableClass
public class RetrieveSuppressionCriteria implements Serializable {
private static final long serialVersionUID = -7215371541145445328L;

@ConfigurableProperty(name = "dcmCheckTransferCapabilities", defaultValue = "false")
private boolean checkTransferCapabilities;


@LDAP(
        distinguishingField = "dicomAETitle",
        mapEntryObjectClass = "dcmRetrieveSuppressionCriteriaEntry",
        mapValueAttribute = "labeledURI"
)
@ConfigurableProperty(name = "dcmRetrieveSuppressionCriteriaMap")
private Map<String, String> suppressionCriteriaMap = new LinkedHashMap<String, String>();

public boolean isCheckTransferCapabilities() {
    return checkTransferCapabilities;
}

public void setCheckTransferCapabilities(boolean checkTransferCapabilities) {
    this.checkTransferCapabilities = checkTransferCapabilities;
}

public Map<String, String> getSuppressionCriteriaMap() {
    return suppressionCriteriaMap;
}

public void setSuppressionCriteriaMap(Map<String, String> suppressionCriteriaMap) {
    this.suppressionCriteriaMap = suppressionCriteriaMap;
}

}