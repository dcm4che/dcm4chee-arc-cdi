package org.dcm4chee.archive.conf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.conf.api.generic.ConfigClass;
import org.dcm4che3.conf.api.generic.ConfigField;

@ConfigClass(objectClass = "dcmRetrieveSuppressionCriteria")
public class RetrieveSuppressionCriteria implements Serializable {
private static final long serialVersionUID = -7215371541145445328L;

@ConfigField(name = "dcmCheckTransferCapabilities", def = "false")
private boolean checkTransferCapabilities;

@ConfigField(mapName = "dcmRetrieveSuppressionCriteriaMap", mapKey = "dicomAETitle", name = "labeledURI", mapElementObjectClass = "dcmRetrieveSuppressionCriteriaEntry", failIfNotPresent=false)
private Map<String, String> suppressionCriteriaMap = new HashMap<String, String>();

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