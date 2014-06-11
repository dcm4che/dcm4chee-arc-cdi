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

import java.util.EnumSet;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;

import org.dcm4che3.conf.api.AttributeCoercion;
import org.dcm4che3.conf.api.AttributeCoercions;
import org.dcm4che3.conf.api.generic.ConfigField;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.imageio.codec.CompressionRule;
import org.dcm4che3.imageio.codec.CompressionRules;
import org.dcm4che3.io.TemplatesCache;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */


public class ArchiveAEExtension extends AEExtension {

    private static final long serialVersionUID = -2390448404282661045L;

    public static final String DEF_RETRY_INTERVAL = "60";

    
    @ConfigField(name = "dcmModifyingSystem")
    private String modifyingSystem;
    
    @ConfigField(name = "dcmWadoSupportedSRClasses")
    private String[] wadoSupportedSRClasses;

    @ConfigField(name = "dcmRetrieveAET")
    private String[] retrieveAETs;
    

    @ConfigField(name = "dcmExternalRetrieveAET")
    private String externalRetrieveAET;
    
    
    @ConfigField(name = "dcmFileSystemGroupID")
    private String fileSystemGroupID;
    
    
    @ConfigField(name = "dcmInitFileSystemURI")
    private String initFileSystemURI;
    
    
    @ConfigField(name = "dcmDigestAlgorithm")
    private String digestAlgorithm;
    
    
    @ConfigField(name = "dcmSpoolDirectoryPath")
    private String spoolDirectoryPath;
    
    
    @ConfigField(name = "dcmStorageFilePathFormat")
    private AttributesFormat storageFilePathFormat;
    
    
    @ConfigField(name = "dcmSuppressWarningCoercionOfDataElements", def="false")
    private boolean suppressWarningCoercionOfDataElements;

    
    @ConfigField(name = "dcmPreserveSpoolFileOnFailure", def="false")
    private boolean preserveSpoolFileOnFailure;
    
    
    @ConfigField(name = "dcmMatchUnknown", def="false")
    private boolean matchUnknown;
    
    
    @ConfigField(name = "dcmSendPendingCGet", def="false")
    private boolean sendPendingCGet;


    @ConfigField(name = "dcmSendPendingCMoveInterval", def="0")
    private int sendPendingCMoveInterval;
    
    
    @ConfigField(name = "dcmStgCmtDelay", def="0")
    private int storageCommitmentDelay;
    
    
    @ConfigField(name = "dcmStgCmtMaxRetries", def="0")
    private int storageCommitmentMaxRetries;
    
    
    @ConfigField(name = "dcmStgCmtRetryInterval", def=DEF_RETRY_INTERVAL)
    private int storageCommitmentRetryInterval = Integer.parseInt(DEF_RETRY_INTERVAL);

    
    @ConfigField(name = "dcmFwdMppsDestination")
    private String[] forwardMPPSDestinations = {};

    
    @ConfigField(name = "dcmFwdMppsMaxRetries", def="0")
    private int forwardMPPSMaxRetries;
    
    
    @ConfigField(name = "dcmFwdMppsRetryInterval", def=DEF_RETRY_INTERVAL)
    private int forwardMPPSRetryInterval = Integer.parseInt(DEF_RETRY_INTERVAL);
    
    
    @ConfigField(name = "dcmIanDestination")
    private String[] IANDestinations = {};
    
    
    @ConfigField(name = "dcmIanMaxRetries", def="0")
    private int IANMaxRetries;
    
    
    @ConfigField(name = "dcmIanRetryInterval", def=DEF_RETRY_INTERVAL)
    private int IANRetryInterval = Integer.parseInt(DEF_RETRY_INTERVAL);
    
    
    private final AttributeCoercions attributeCoercions = new AttributeCoercions();
    private final CompressionRules compressionRules = new CompressionRules();
    
    
    @ConfigField(name = "dcmReturnOtherPatientIDs", def="false")
    private boolean returnOtherPatientIDs;

    
    @ConfigField(name = "dcmReturnOtherPatientNames", def="false")
    private boolean returnOtherPatientNames;

    
    @ConfigField(name = "dcmShowRejectedInstances", def="false")
    private boolean showRejectedForQualityReasons;
    
    
    @ConfigField(name = "hl7PIXManagerApplication")
    private String remotePIXManagerApplication;
    
    
    @ConfigField(name = "hl7PIXConsumerApplication")
    private String localPIXConsumerApplication;
    
    
    @ConfigField(name = "dcmQidoMaxNumberOfResults", def="0")
    private int QIDOMaxNumberOfResults;
    
    @ConfigField(name = "dcmIsTimeZoneSupported", def="false")    
    private boolean timeZoneSupported;


    public String[] getWadoSupportedSRClasses() {
        return wadoSupportedSRClasses;
    }

    public void setWadoSupportedSRClasses(String[] wadoSupportedSRClasses) {
        this.wadoSupportedSRClasses = wadoSupportedSRClasses;
    }
    
    public boolean isTimeZoneSupported() {
        return timeZoneSupported;
    }

    public void setTimeZoneSupported(boolean timeZoneSupported) {
        this.timeZoneSupported = timeZoneSupported;
    }

    public AttributeCoercion getAttributeCoercion(String sopClass,
            Dimse dimse, Role role, String aeTitle) {
        return attributeCoercions.findAttributeCoercion(sopClass, dimse, role, aeTitle);
    }

    public AttributeCoercions getAttributeCoercions() {
        return attributeCoercions;
    }

    public void addAttributeCoercion(AttributeCoercion ac) {
        attributeCoercions.add(ac);
    }

    public void setAttributeCoercions(AttributeCoercions acs) {
        attributeCoercions.clear();
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
        compressionRules.add(rules);
    }

    public boolean removeCompressionRule(CompressionRule ac) {
        return compressionRules.remove(ac);
    }

    public String getModifyingSystem() {
        return modifyingSystem;
    }

    public String getEffectiveModifyingSystem() {
        return modifyingSystem != null 
                ? modifyingSystem
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

    public String getFileSystemGroupID() {
        return fileSystemGroupID;
    }

    public void setFileSystemGroupID(String fileSystemGroupID) {
        this.fileSystemGroupID = fileSystemGroupID;
    }

    public String getInitFileSystemURI() {
        return initFileSystemURI;
    }

    public void setInitFileSystemURI(String initFileSystemURI) {
        this.initFileSystemURI = initFileSystemURI;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getSpoolDirectoryPath() {
        return spoolDirectoryPath;
    }

    public void setSpoolDirectoryPath(String spoolDirectoryPath) {
        this.spoolDirectoryPath = spoolDirectoryPath;
    }

    public AttributesFormat getStorageFilePathFormat() {
        return storageFilePathFormat;
    }

    public void setStorageFilePathFormat(AttributesFormat storageFilePathFormat) {
        this.storageFilePathFormat = storageFilePathFormat;
    }

    public Templates getAttributeCoercionTemplates(String cuid, Dimse dimse,
            TransferCapability.Role role, String aet) throws TransformerConfigurationException {
        AttributeCoercion ac = getAttributeCoercion(cuid, dimse, role, aet);
        return ac != null 
                ? TemplatesCache.getDefault().get(
                        StringUtils.replaceSystemProperties(ac.getURI()))
                : null;
    }

    public boolean isSuppressWarningCoercionOfDataElements() {
        return suppressWarningCoercionOfDataElements;
    }

    public void setSuppressWarningCoercionOfDataElements(
            boolean suppressWarningCoercionOfDataElements) {
        this.suppressWarningCoercionOfDataElements = suppressWarningCoercionOfDataElements;
    }

    public boolean isPreserveSpoolFileOnFailure() {
        return preserveSpoolFileOnFailure;
    }

    public void setPreserveSpoolFileOnFailure(boolean preserveSpoolFileOnFailure) {
        this.preserveSpoolFileOnFailure = preserveSpoolFileOnFailure;
    }

    public boolean isMatchUnknown() {
        return matchUnknown;
    }

    public void setMatchUnknown(boolean matchUnknown) {
        this.matchUnknown = matchUnknown;
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

    public final void setStorageCommitmentMaxRetries(int storageCommitmentMaxRetries) {
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

    public final void setForwardMPPSDestinations(String[] forwardMPPSDestinations) {
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

    public boolean isShowRejectedForQualityReasons() {
        return showRejectedForQualityReasons;
    }

    public void setShowRejectedForQualityReasons(boolean showRejectedForQualityReasons) {
        this.showRejectedForQualityReasons = showRejectedForQualityReasons;
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

    @Override
    public void reconfigure(AEExtension from) {
        ArchiveAEExtension arcae = (ArchiveAEExtension) from;
        
        ReflectiveConfig.reconfigure(arcae, this);
        
        setAttributeCoercions(arcae.getAttributeCoercions());
        setCompressionRules(arcae.getCompressionRules());
    }

    public StoreParam getStoreParam() {
        StoreParam storeParam = ae.getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class)
                .getStoreParam();
        storeParam.setModifyingSystem(getEffectiveModifyingSystem());
        storeParam.setRetrieveAETs(retrieveAETs);
        storeParam.setExternalRetrieveAET(externalRetrieveAET);
        return storeParam;
    }

    public QueryParam getQueryParam(EnumSet<QueryOption> queryOpts,
            String[] accessControlIDs) {
        QueryParam queryParam = ae.getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class)
                .getQueryParam();
        queryParam.setCombinedDatetimeMatching(queryOpts
                .contains(QueryOption.DATETIME));
        queryParam.setFuzzySemanticMatching(queryOpts
                .contains(QueryOption.FUZZY));
        queryParam.setMatchUnknown(matchUnknown);
        queryParam.setAccessControlIDs(accessControlIDs);
        queryParam.setShowRejectedForQualityReasons(showRejectedForQualityReasons);
        return queryParam;
    }

}
