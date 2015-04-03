/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.archive.conf;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;

import org.dcm4che3.conf.api.AttributeCoercion;
import org.dcm4che3.conf.api.AttributeCoercions;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.conf.api.extensions.ReconfiguringIterator;
import org.dcm4che3.imageio.codec.CompressionRule;
import org.dcm4che3.imageio.codec.CompressionRules;
import org.dcm4che3.io.TemplatesCache;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.dto.ReferenceUpdateOnRetrieveScope;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */

@LDAP(objectClasses = "dcmArchiveNetworkAE", noContainerNode = true)
@ConfigurableClass
public class ArchiveAEExtension extends AEExtension {

    private static final long serialVersionUID = -2390448404282661045L;

    public static final String DEF_RETRY_INTERVAL = "60";

    @ConfigurableProperty(name = "dcmModifyingSystem")
    private String modifyingSystem;

    @ConfigurableProperty(name = "dcmWadoSupportedSRClasses")
    private String[] wadoSupportedSRClasses = {};

    @ConfigurableProperty(name = "dcmWadoOverlayRendering", defaultValue = "true")
    private boolean wadoOverlayRendering;

    @ConfigurableProperty(name = "dcmRetrieveAET")
    private String[] retrieveAETs = {};

    @ConfigurableProperty(name = "dcmExternalRetrieveAET")
    private String externalRetrieveAET;

    @ConfigurableProperty(name = "dcmStorageSystemGroupID")
    private String storageSystemGroupID;

    @ConfigurableProperty(name= "dcmQidoClientAcceptMediaType", defaultValue="application/json")
    private String qidoClientAcceptType = "application/json";

    @ConfigurableProperty(name= "dcmQidoClientSupportFuzzyMatching", defaultValue="false")
    private boolean qidoClientSupportFuzzyMatching;

    @ConfigurableProperty(name= "dcmQidoClientSupportTimeZoneAdjustment", defaultValue="false")
    private boolean qidoClientSupportTimeZoneAdjustment;

    @ConfigurableProperty(name = "dcmSpoolDirectoryPath")
    private String spoolDirectoryPath;

    @ConfigurableProperty(name = "dcmMetaDataStorageSystemGroupID")
    private String metaDataStorageSystemGroupID;

    @ConfigurableProperty(name = "dcmSuppressWarningCoercionOfDataElements", defaultValue = "false")
    private boolean suppressWarningCoercionOfDataElements;
    
    @ConfigurableProperty(name = "dcmCheckNonDBAttributesOnStorage", defaultValue = "false")
    private boolean checkNonDBAttributesOnStorage;
    
    @ConfigurableProperty(name = "dcmIgnoreDuplicatesOnStorage", defaultValue = "true")
    private boolean ignoreDuplicatesOnStorage;

    @ConfigurableProperty(name = "dcmPreserveSpoolFileOnFailure", defaultValue = "false")
    private boolean preserveSpoolFileOnFailure;

    @ConfigurableProperty(name = "dcmPersonNameComponentOrderInsensitiveMatching", defaultValue = "false")
    private boolean personNameComponentOrderInsensitiveMatching;

    @ConfigurableProperty(name = "dcmMatchUnknown", defaultValue = "false")
    private boolean matchUnknown;

    @ConfigurableProperty(name = "dcmMatchLinkedPatientIDs", defaultValue = "false")
    private boolean matchLinkedPatientIDs;

    @ConfigurableProperty(name = "dcmSendPendingCGet", defaultValue = "false")
    private boolean sendPendingCGet;

    @ConfigurableProperty(name = "dcmSendPendingCMoveInterval", defaultValue = "0")
    private int sendPendingCMoveInterval;

    @ConfigurableProperty(name = "dcmStgCmtDelay", defaultValue = "0")
    private int storageCommitmentDelay;

    @ConfigurableProperty(name = "dcmStgCmtMaxRetries", defaultValue = "0")
    private int storageCommitmentMaxRetries;

    @ConfigurableProperty(name = "dcmStgCmtRetryInterval", defaultValue = DEF_RETRY_INTERVAL)
    private int storageCommitmentRetryInterval = Integer
            .parseInt(DEF_RETRY_INTERVAL);

    @ConfigurableProperty(name = "dcmFwdMppsDestination")
    private String[] forwardMPPSDestinations = {};

    @ConfigurableProperty(name = "dcmFwdMppsMaxRetries", defaultValue = "0")
    private int forwardMPPSMaxRetries;

    @ConfigurableProperty(name = "dcmFwdMppsRetryInterval", defaultValue = DEF_RETRY_INTERVAL)
    private int forwardMPPSRetryInterval = Integer.parseInt(DEF_RETRY_INTERVAL);

    @ConfigurableProperty(name = "dcmIanDestination")
    private String[] IANDestinations = {};

    @ConfigurableProperty(name = "dcmIanMaxRetries", defaultValue = "0")
    private int IANMaxRetries;

    @ConfigurableProperty(name = "dcmIanRetryInterval", defaultValue = DEF_RETRY_INTERVAL)
    private int IANRetryInterval = Integer.parseInt(DEF_RETRY_INTERVAL);

    @LDAP(noContainerNode = true)
    @ConfigurableProperty(name = "dcmAttributeCoercions")
    private AttributeCoercions attributeCoercions = new AttributeCoercions();

    @LDAP(noContainerNode = true)
    @ConfigurableProperty(name = "dcmCompressionRules")
    private CompressionRules compressionRules = new CompressionRules();

    @ConfigurableProperty(name = "dcmReturnOtherPatientIDs", defaultValue = "false")
    private boolean returnOtherPatientIDs;

    @ConfigurableProperty(name = "dcmReturnOtherPatientNames", defaultValue = "false")
    private boolean returnOtherPatientNames;

    @ConfigurableProperty(name = "hl7PIXManagerApplication")
    private String remotePIXManagerApplication;

    @ConfigurableProperty(name = "hl7PIXConsumerApplication")
    private String localPIXConsumerApplication;

    @ConfigurableProperty(name = "dcmQidoMaxNumberOfResults", defaultValue = "0")
    private int QIDOMaxNumberOfResults;

    @ConfigurableProperty(name = "dcmWadoSRTemplateURI")
    private String wadoSRTemplateURI;

    @ConfigurableProperty(name = "dcmQueryRetrieveViewID")
    private String queryRetrieveViewID;

    @ConfigurableProperty(name = "dcmQCUpdateReferencesOnRetrieve", defaultValue="DEACTIVATE",
            label="QC Update Reference on Retrieve",
            description="Sets the scope for the QC retrieve service decorator "
                    + "can be DEACTIVATE, STUDY or PATIENT "
                    + ", the update check is performed according to that scope")
    private ReferenceUpdateOnRetrieveScope qcUpdateReferencesOnRetrieve = ReferenceUpdateOnRetrieveScope.DEACTIVATE;

    @ConfigurableProperty(name = "dcmRetrieveSuppressionCriteria")
    private RetrieveSuppressionCriteria retrieveSuppressionCriteria = new RetrieveSuppressionCriteria();

    public RetrieveSuppressionCriteria getRetrieveSuppressionCriteria() {
        return retrieveSuppressionCriteria;
    }

    public void setRetrieveSuppressionCriteria(
            RetrieveSuppressionCriteria retrieveSuppressionCriteria) {
        this.retrieveSuppressionCriteria = retrieveSuppressionCriteria;
    }

    @LDAP(noContainerNode=true)
    @ConfigurableProperty(name = "dcmMPPSEmulationRules")
    private List<MPPSEmulationRule> mppsEmulationRules = new ArrayList<MPPSEmulationRule>();

    private Map<String, MPPSEmulationRule> mppsEmulationRuleMap =
            new HashMap<String, MPPSEmulationRule>();

    @LDAP(noContainerNode=true)
    @ConfigurableProperty(name = "dcmArchivingRules")
    private ArchivingRules archivingRules = new ArchivingRules();

    @ConfigurableProperty(name = "dcmPatientSelector")
    private PatientSelectorConfig patientSelectorConfig;

    public PatientSelectorConfig getPatientSelectorConfig() {
        return patientSelectorConfig;
    }

    public void setPatientSelectorConfig(
            PatientSelectorConfig patientSelectorConfig) {
        this.patientSelectorConfig = patientSelectorConfig;
    }

    public String[] getWadoSupportedSRClasses() {
        return wadoSupportedSRClasses;
    }

    public void setWadoSupportedSRClasses(String[] wadoSupportedSRClasses) {
        this.wadoSupportedSRClasses = wadoSupportedSRClasses;
    }

    public boolean isWadoOverlayRendering() {
        return wadoOverlayRendering;
    }

    public void setWadoOverlayRendering(boolean wadoOverlayRendering) {
        this.wadoOverlayRendering = wadoOverlayRendering;
    }

    public AttributeCoercion getAttributeCoercion(String sopClass, Dimse dimse,
                                                  Role role, String aeTitle) {
        return attributeCoercions.findAttributeCoercion(sopClass, dimse, role,
                aeTitle);
    }

    public AttributeCoercions getAttributeCoercions() {
        return attributeCoercions;
    }

    public void addAttributeCoercion(AttributeCoercion ac) {
        attributeCoercions.add(ac);
    }

    public void setAttributeCoercions(AttributeCoercions acs) {
        attributeCoercions.clear();
        if (acs!=null)
            attributeCoercions.add(acs);
    }

    public boolean removeAttributeCoercion(AttributeCoercion ac) {
        return attributeCoercions.remove(ac);
    }

    public CompressionRules getCompressionRules() {
        return compressionRules;
    }

    public void addCompressionRule(CompressionRule rule) {
        compressionRules.add(rule);
    }

    public void setCompressionRules(CompressionRules rules) {
        compressionRules.clear();
        if (rules != null)
            compressionRules.add(rules);
    }

    public boolean removeCompressionRule(CompressionRule ac) {
        return compressionRules.remove(ac);
    }

    public String getModifyingSystem() {
        return modifyingSystem;
    }

    public String getEffectiveModifyingSystem() {
        return modifyingSystem != null ? modifyingSystem
                : getApplicationEntity().getDevice().getDeviceName();
    }

    public void setModifyingSystem(String modifyingSystem) {
        this.modifyingSystem = modifyingSystem;
    }

    public String[] getRetrieveAETs() {
        return retrieveAETs;
    }

    public void setRetrieveAETs(String... retrieveAETs) {
        this.retrieveAETs = retrieveAETs;
    }

    public String getExternalRetrieveAET() {
        return externalRetrieveAET;
    }

    public void setExternalRetrieveAET(String externalRetrieveAET) {
        this.externalRetrieveAET = externalRetrieveAET;
    }

    public String getStorageSystemGroupID() {
        return storageSystemGroupID;
    }

    public void setStorageSystemGroupID(String storageSystemGroupID) {
        this.storageSystemGroupID = storageSystemGroupID;
    }

    public String getSpoolDirectoryPath() {
        return spoolDirectoryPath;
    }

    public void setSpoolDirectoryPath(String spoolDirectoryPath) {
        this.spoolDirectoryPath = spoolDirectoryPath;
    }

    public String getMetaDataStorageSystemGroupID() {
        return metaDataStorageSystemGroupID;
    }

    public void setMetaDataStorageSystemGroupID(String metaDataStorageSystemGroupID) {
        this.metaDataStorageSystemGroupID = metaDataStorageSystemGroupID;
    }

    public Templates getAttributeCoercionTemplates(String cuid, Dimse dimse,
                                                   TransferCapability.Role role, String aet)
            throws TransformerConfigurationException {
        AttributeCoercion ac = getAttributeCoercion(cuid, dimse, role, aet);
        return ac != null ? TemplatesCache.getDefault().get(
                StringUtils.replaceSystemProperties(ac.getURI())) : null;
    }

    public boolean isSuppressWarningCoercionOfDataElements() {
        return suppressWarningCoercionOfDataElements;
    }

    public void setSuppressWarningCoercionOfDataElements(
            boolean suppressWarningCoercionOfDataElements) {
        this.suppressWarningCoercionOfDataElements = suppressWarningCoercionOfDataElements;
    }
    
    public boolean isCheckNonDBAttributesOnStorage() {
        return checkNonDBAttributesOnStorage;
    }

    public void setCheckNonDBAttributesOnStorage(
            boolean checkNonDBAttributesOnStorage) {
        this.checkNonDBAttributesOnStorage = checkNonDBAttributesOnStorage;
    }
    
    public boolean isIgnoreDuplicatesOnStorage() {
		return ignoreDuplicatesOnStorage;
	}

	public void setIgnoreDuplicatesOnStorage(boolean ignoreDuplicatesOnStorage) {
		this.ignoreDuplicatesOnStorage = ignoreDuplicatesOnStorage;
	}

	public boolean isPreserveSpoolFileOnFailure() {
        return preserveSpoolFileOnFailure;
    }

    public void setPreserveSpoolFileOnFailure(boolean preserveSpoolFileOnFailure) {
        this.preserveSpoolFileOnFailure = preserveSpoolFileOnFailure;
    }

    public boolean isPersonNameComponentOrderInsensitiveMatching() {
        return personNameComponentOrderInsensitiveMatching;
    }

    public void setPersonNameComponentOrderInsensitiveMatching(
            boolean personNameComponentOrderInsensitiveMatching) {
        this.personNameComponentOrderInsensitiveMatching = personNameComponentOrderInsensitiveMatching;
    }

    public boolean isMatchUnknown() {
        return matchUnknown;
    }

    public void setMatchUnknown(boolean matchUnknown) {
        this.matchUnknown = matchUnknown;
    }

    public boolean isMatchLinkedPatientIDs() {
        return matchLinkedPatientIDs;
    }

    public void setMatchLinkedPatientIDs(boolean matchLinkedPatientIDs) {
        this.matchLinkedPatientIDs = matchLinkedPatientIDs;
    }

    public boolean isSendPendingCGet() {
        return sendPendingCGet;
    }

    public void setSendPendingCGet(boolean sendPendingCGet) {
        this.sendPendingCGet = sendPendingCGet;
    }

    public int getSendPendingCMoveInterval() {
        return sendPendingCMoveInterval;
    }

    public void setSendPendingCMoveInterval(int sendPendingCMoveInterval) {
        this.sendPendingCMoveInterval = sendPendingCMoveInterval;
    }

    public final int getStorageCommitmentDelay() {
        return storageCommitmentDelay;
    }

    public final void setStorageCommitmentDelay(int storageCommitmentDelay) {
        this.storageCommitmentDelay = storageCommitmentDelay;
    }

    public final int getStorageCommitmentMaxRetries() {
        return storageCommitmentMaxRetries;
    }

    public final void setStorageCommitmentMaxRetries(
            int storageCommitmentMaxRetries) {
        this.storageCommitmentMaxRetries = storageCommitmentMaxRetries;
    }

    public final int getStorageCommitmentRetryInterval() {
        return storageCommitmentRetryInterval;
    }

    public final void setStorageCommitmentRetryInterval(
            int storageCommitmentRetryInterval) {
        this.storageCommitmentRetryInterval = storageCommitmentRetryInterval;
    }

    public final String[] getForwardMPPSDestinations() {
        return forwardMPPSDestinations;
    }

    public final void setForwardMPPSDestinations(
            String[] forwardMPPSDestinations) {
        this.forwardMPPSDestinations = forwardMPPSDestinations;
    }

    public final int getForwardMPPSMaxRetries() {
        return forwardMPPSMaxRetries;
    }

    public final void setForwardMPPSMaxRetries(int forwardMPPSMaxRetries) {
        this.forwardMPPSMaxRetries = forwardMPPSMaxRetries;
    }

    public final int getForwardMPPSRetryInterval() {
        return forwardMPPSRetryInterval;
    }

    public final void setForwardMPPSRetryInterval(int forwardMPPSRetryInterval) {
        this.forwardMPPSRetryInterval = forwardMPPSRetryInterval;
    }

    public String[] getIANDestinations() {
        return IANDestinations;
    }

    public void setIANDestinations(String[] ianDestinations) {
        this.IANDestinations = ianDestinations;
    }

    public boolean hasIANDestinations() {
        return IANDestinations.length > 0;
    }

    public int getIANMaxRetries() {
        return IANMaxRetries;
    }

    public void setIANMaxRetries(int ianMaxRetries) {
        this.IANMaxRetries = ianMaxRetries;
    }

    public int getIANRetryInterval() {
        return IANRetryInterval;
    }

    public void setIANRetryInterval(int ianRetryInterval) {
        this.IANRetryInterval = ianRetryInterval;
    }

    public boolean isReturnOtherPatientIDs() {
        return returnOtherPatientIDs;
    }

    public void setReturnOtherPatientIDs(boolean returnOtherPatientIDs) {
        this.returnOtherPatientIDs = returnOtherPatientIDs;
    }

    public boolean isReturnOtherPatientNames() {
        return returnOtherPatientNames;
    }

    public void setReturnOtherPatientNames(boolean returnOtherPatientNames) {
        this.returnOtherPatientNames = returnOtherPatientNames;
    }

    public String getRemotePIXManagerApplication() {
        return remotePIXManagerApplication;
    }

    public void setRemotePIXManagerApplication(String appName) {
        this.remotePIXManagerApplication = appName;
    }

    public String getLocalPIXConsumerApplication() {
        return localPIXConsumerApplication;
    }

    public void setLocalPIXConsumerApplication(String appName) {
        this.localPIXConsumerApplication = appName;
    }

    public int getQIDOMaxNumberOfResults() {
        return QIDOMaxNumberOfResults;
    }

    public void setQIDOMaxNumberOfResults(int qidoMaxNumberOfResults) {
        this.QIDOMaxNumberOfResults = qidoMaxNumberOfResults;
    }

    public ArchivingRules getArchivingRules() {
        return archivingRules;
    }

    public void addArchivingRule(ArchivingRule rule) {
        archivingRules.add(rule);
    }

    public void setArchivingRules(ArchivingRules rules) {
        archivingRules.clear();
        if (rules != null)
            archivingRules.add(rules);
    }

    public boolean removeArchivingRule(ArchivingRule ac) {
        return archivingRules.remove(ac);
    }

    @Override
    public void reconfigure(AEExtension from) {
        ArchiveAEExtension arcae = (ArchiveAEExtension) from;

        ReconfiguringIterator.reconfigure(arcae, this, ArchiveAEExtension.class);

        setAttributeCoercions(arcae.getAttributeCoercions());
        setCompressionRules(arcae.getCompressionRules());
        setArchivingRules(arcae.getArchivingRules());
    }

    public StoreParam getStoreParam() {
        StoreParam storeParam = ae.getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class)
                .getStoreParam();
        storeParam.setModifyingSystem(getEffectiveModifyingSystem());
        storeParam.setRetrieveAETs(retrieveAETs);
        storeParam.setExternalRetrieveAET(externalRetrieveAET);
        storeParam.setPatientSelectorConfig(getPatientSelectorConfig());
        return storeParam;
    }

    public QueryParam getQueryParam(EnumSet<QueryOption> queryOpts,
                                    String[] accessControlIDs) {
        ArchiveDeviceExtension arcDev = ae.getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class);
        QueryParam queryParam = arcDev.getQueryParam();
        queryParam.setCombinedDatetimeMatching(queryOpts
                .contains(QueryOption.DATETIME));
        queryParam.setFuzzySemanticMatching(queryOpts
                .contains(QueryOption.FUZZY));
        queryParam.setPersonNameComponentOrderInsensitiveMatching(
                personNameComponentOrderInsensitiveMatching);
        queryParam.setMatchUnknown(matchUnknown);
        queryParam.setMatchLinkedPatientIDs(matchLinkedPatientIDs);
        queryParam.setAccessControlIDs(accessControlIDs);
        queryParam.setQueryRetrieveView(
                arcDev.getQueryRetrieveView(queryRetrieveViewID));
        return queryParam;
    }

    public String getWadoSRTemplateURI() {
        return wadoSRTemplateURI;
    }

    public void setWadoSRTemplateURI(String wadoSRTemplateURI) {
        this.wadoSRTemplateURI = wadoSRTemplateURI;
    }

    public String getQueryRetrieveViewID() {
        return queryRetrieveViewID;
    }

    public void setQueryRetrieveViewID(String queryRetrieveViewID) {
        this.queryRetrieveViewID = queryRetrieveViewID;
    }

    public List<MPPSEmulationRule> getMppsEmulationRules() {
        return mppsEmulationRules;
    }

    public void setMppsEmulationRules(List<MPPSEmulationRule> rules) {
        this.mppsEmulationRules.clear();
        this.mppsEmulationRuleMap.clear();
        for (MPPSEmulationRule rule : rules) {
            addMppsEmulationRule(rule);
        }
    }

    public MPPSEmulationRule getMppsEmulationRule(String sourceAET) {
        return mppsEmulationRuleMap.get(sourceAET);
    }

    public void addMppsEmulationRule(MPPSEmulationRule rule) {
        mppsEmulationRules.add(rule);
        for (String sourceAET : rule.getSourceAETs()) {
            mppsEmulationRuleMap.put(sourceAET, rule);
        }
    }

    public ReferenceUpdateOnRetrieveScope getQcUpdateReferencesOnRetrieve() {
        return qcUpdateReferencesOnRetrieve;
    }

    public void setQcUpdateReferencesOnRetrieve(
            ReferenceUpdateOnRetrieveScope qcUpdateReferencesOnRetrieve) {
        this.qcUpdateReferencesOnRetrieve = qcUpdateReferencesOnRetrieve;
    }

    public String getQidoClientAcceptType() {
        return qidoClientAcceptType;
    }

    public boolean isQidoClientSupportFuzzyMatching() {
        return qidoClientSupportFuzzyMatching;
    }

    public boolean isQidoClientSupportTimeZoneAdjustment() {
        return qidoClientSupportTimeZoneAdjustment;
    }

    public void setQidoClientAcceptType(String qidoClientAcceptType) {
        this.qidoClientAcceptType = qidoClientAcceptType;
    }

    public void setQidoClientSupportFuzzyMatching(
            boolean qidoClientSupportFuzzyMatching) {
        this.qidoClientSupportFuzzyMatching = qidoClientSupportFuzzyMatching;
    }

    public void setQidoClientSupportTimeZoneAdjustment(
            boolean qidoClientSupportTimeZoneAdjustment) {
        this.qidoClientSupportTimeZoneAdjustment = qidoClientSupportTimeZoneAdjustment;
    }

}
