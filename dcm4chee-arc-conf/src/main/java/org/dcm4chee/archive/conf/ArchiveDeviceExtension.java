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

import org.dcm4che3.conf.api.extensions.ReconfiguringIterator;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurableProperty.ConfigurablePropertyType;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.Code;
import org.dcm4che3.io.TemplatesCache;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4che3.util.StringUtils;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@LDAP(objectClasses = "dcmArchiveDevice", noContainerNode = true)
@ConfigurableClass
public class ArchiveDeviceExtension extends DeviceExtension {

    private static final long serialVersionUID = -3611223780276386740L;

    @ConfigurableProperty(type = ConfigurablePropertyType.OptimisticLockingHash)
    private String olockHash;

    @ConfigurableProperty(name = "dcmVisibleImageClasses")
    private String[] visibleImageSRClasses = {};
    
    @ConfigurableProperty(name = "dcmNonVisibleImageClasses")
    private String[] nonVisibleImageSRClasses = {};
    
    @ConfigurableProperty(name = "dcmUseWhitelistOfVisibleImageSRClasses", defaultValue = "true")
    private boolean useWhitelistOfVisibleImageSRClasses;

    @ConfigurableProperty(name = "dcmDisabledDecorators")
    private String[] disabledDecorators = {};
    
    @ConfigurableProperty(name = "dcmIncorrectWorklistEntrySelectedCode")
    private Code incorrectWorklistEntrySelectedCode;

    @ConfigurableProperty(name = "dcmFuzzyAlgorithmClass")
    private String fuzzyAlgorithmClass;

    @LDAP(noContainerNode = true)
    @ConfigurableProperty(name = "dcmAttributeFilter")
    private final Map<Entity, AttributeFilter> attributeFilters = new EnumMap<Entity, AttributeFilter>(Entity.class);

    @ConfigurableProperty(name = "dcmConfigurationStaleTimeout", defaultValue = "0")
    private int configurationStaleTimeout;

    @ConfigurableProperty(name = "dcmWadoAttributesStaleTimeout", defaultValue = "0")
    private int wadoAttributesStaleTimeout;


    @ConfigurableProperty(name = "dcmHostnameAEResolution", defaultValue = "false")
    private boolean hostnameAEResolution;

    @ConfigurableProperty(name = "dcmDeIdentifyLogs", defaultValue = "false")
    private boolean deIdentifyLogs;

    @ConfigurableProperty(name = "dcmUpdateDbRetries", defaultValue = "3")
    private int updateDbRetries = 3;

    @ConfigurableProperty(name = "dcmUpdateDbDelay", defaultValue = "1000")
    private int updateDbDelay = 1000;

    @LDAP(noContainerNode = true)
    @ConfigurableProperty(name = "dcmPrivateDerivedFields")
    private final PrivateDerivedFields privateDerivedFields = new PrivateDerivedFields();

    @LDAP(
            distinguishingField = "dicomHostName",
            mapValueAttribute = "dicomAETitle",
            mapEntryObjectClass= "dcmHostNameAEEntry"
    )
    @ConfigurableProperty(name = "HostNameAETitleMap",
        description = "Maps remote hostnames/IP addresses to AE titles. Used for determining the source AE within Web services (STOW/QIDO/...). \"*\" can be used for a default/fallback mapping.")
    private final Map<String, String> hostNameToAETitleMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    @LDAP(
            distinguishingField = "dcmDeviceName",
            mapValueAttribute = "dcmFetchPriority",
            mapEntryObjectClass= "dcmExternalArchiveEntry"
    )
    @ConfigurableProperty(name = "ExternalArchivesMap")
    private final Map<String, String> externalArchivesMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    @LDAP(noContainerNode = true)
    @ConfigurableProperty(name = "rejectionParams")
    private RejectionParam[] rejectionParams = {};

    @LDAP(noContainerNode = true)
    @ConfigurableProperty(name = "queryRetrieveViews")
    private QueryRetrieveView[] queryRetrieveViews = {};

    @ConfigurableProperty(name = "dcmRejectedObjectsCleanUpPollInterval")
    private int rejectedObjectsCleanUpPollInterval;

    @ConfigurableProperty(name = "dcmRejectedObjectsCleanUpMaxNumberOfDeletes")
    private int rejectedObjectsCleanUpMaxNumberOfDeletes;

    @ConfigurableProperty(name = "dcmMppsEmulationPollInterval",
            description = "Interval in seconds that should be used to poll for finished study updates (and therefore mpps emulation candidates)" +
                    "If set to 0, disables polling.",
            defaultValue = "0")
    private int mppsEmulationPollInterval;

    @ConfigurableProperty(name = "dcmDeletionServicePollInterval", defaultValue = "0")
    private int deletionServicePollInterval;

    @ConfigurableProperty(name = "dcmArchivingSchedulerPollInterval", defaultValue = "0")
    private int archivingSchedulerPollInterval;

    @ConfigurableProperty(name = "dcmIocmConfig")
    private IOCMConfig iocmConfig;

    @ConfigurableProperty(name = "dcmSyncLocationStatusPollInterval", defaultValue = "0")
    private int syncLocationStatusPollInterval;

    @ConfigurableProperty(name = "dcmSyncLocationStatusCheckDelay", defaultValue = "1440")
    private int syncLocationStatusCheckDelay = 1440;

    @ConfigurableProperty(name = "dcmSyncLocationStatusMaxNumberPerTask", defaultValue = "1000")
    private int syncLocationStatusMaxNumberPerTask = 1000;

    @ConfigurableProperty(name = "dcmSyncLocationStatusStorageSystemGroupID")
    private String[] syncLocationStatusStorageSystemGroupIDs;

    @ConfigurableProperty(name = "dcmSyncLocationStatusVerifyArchived", defaultValue = "true")
    private boolean syncLocationStatusVerifyArchived = true;

    @ConfigurableProperty(name = "dcmFetchAETitle")
    private String fetchAETitle = "DCM4CHEE_FETCH";

    @ConfigurableProperty(name = "dcmPriorsCacheMaxResolvedPathEntries", defaultValue = "100")
    private int priorsCacheMaxResolvedPathEntries = 100;

    @ConfigurableProperty(name = "dcmPriorsCacheDeleteDuplicateLocationsDelay", defaultValue = "60")
    private int priorsCacheDeleteDuplicateLocationsDelay = 60;

    @ConfigurableProperty(name = "dcmPriorsCacheClearMaxLocationsPerDelete", defaultValue = "1000")
    private int priorsCacheClearMaxLocationsPerDelete = 1000;

    @ConfigurableProperty(name = "dcmUseNullForEmptyQueryFields", defaultValue = "true")
    private boolean useNullForEmptyQueryFields = true;

    @LDAP(noContainerNode=true)
    @ConfigurableProperty(name = "dcmDeletionRules")
    private final DeletionRules deletionRules = new DeletionRules();

    @ConfigurableProperty(name = "dcmMaxDeleteServiceRetries", defaultValue = "0")
    private int maxDeleteServiceRetries;

    @ConfigurableProperty(name = "dcmDeleteServiceAllowedInterval")
    private String deleteServiceAllowedInterval;

    @ConfigurableProperty(name = "dcmDataVolumePerDayCalculationRange", defaultValue = "23-0")
    private String dataVolumePerDayCalculationRange = "23-0";

    @ConfigurableProperty(name = "dcmDataVolumePerDayAverageOnNDays", defaultValue = "1")
    private int dataVolumePerDayAverageOnNDays = 1;

    @LDAP(noContainerNode=true)
    @ConfigurableProperty(
            label = "MPPS emulation and Study update rules",
            name = "dcmMPPSEmulationRules")
    private final List<MPPSEmulationAndStudyUpdateRule> mppsEmulationAndStudyUpdateRules = new ArrayList<MPPSEmulationAndStudyUpdateRule>();

    @ConfigurableProperty(name = "dcmIgnoreSeriesStudyMissmatchErrorsAETs",
    		description="List of Application Entities which will not get a C-STORE rejected, if a Series/Study mismatch is detected",
    		collectionOfReferences=true)
    private Collection<ApplicationEntity> ignoreSeriesStudyMissmatchErrorsAETs = new ArrayList<ApplicationEntity>();

    @ConfigurableProperty
    private WeightWatcherConfiguration weightWatcherConfiguration = new WeightWatcherConfiguration();

    private transient FuzzyStr fuzzyStr;
    private transient TemplatesCache templatesCache;

    public String getOlockHash() {
        return olockHash;
    }

    public void setOlockHash(String olockHash) {
        this.olockHash = olockHash;
    }

    public List<MPPSEmulationAndStudyUpdateRule> getMppsEmulationAndStudyUpdateRules() {
        return mppsEmulationAndStudyUpdateRules;
    }

    private final Map<String, MPPSEmulationAndStudyUpdateRule> mppsEmulationRuleMap =
            new HashMap<String, MPPSEmulationAndStudyUpdateRule>();

    public void setMppsEmulationAndStudyUpdateRules(List<MPPSEmulationAndStudyUpdateRule> rules) {
        // exception
        if (rules == mppsEmulationAndStudyUpdateRules) return;

        this.mppsEmulationAndStudyUpdateRules.clear();
        this.mppsEmulationRuleMap.clear();
        for (MPPSEmulationAndStudyUpdateRule rule : rules) {
            addMppsEmulationRule(rule);
        }
    }

    public MPPSEmulationAndStudyUpdateRule getMppsEmulationRule(String sourceAET) {
        return mppsEmulationRuleMap.get(sourceAET) != null
                ? mppsEmulationRuleMap.get(sourceAET)
                : (mppsEmulationRuleMap.get("*") != null ? mppsEmulationRuleMap.get("*") : null);
    }


    public void addMppsEmulationRule(MPPSEmulationAndStudyUpdateRule rule) {
        mppsEmulationAndStudyUpdateRules.add(rule);

        // populate rules by source AE for easy lookup
        for (ApplicationEntity applicationEntity : rule.getSourceAEs())
            mppsEmulationRuleMap.put(applicationEntity.getAETitle(), rule);

        // if no AEs specified - consider it a default rule
        if (rule.getSourceAEs().isEmpty())
            mppsEmulationRuleMap.put("*", rule);
    }

    public boolean isHostnameAEResolution() {
        return hostnameAEResolution;
    }

    public void setHostnameAEResolution(boolean hostnameAEResolution) {
        this.hostnameAEResolution = hostnameAEResolution;
    }

    public Map<String, String> getHostNameToAETitleMap() {
        return hostNameToAETitleMap;
    }

    public Map<String, String> getExternalArchivesMap() {
        return externalArchivesMap;
    }

    public void setHostNameToAETitleMap(Map<String, String> hostNameToAETitleMap) {
        this.hostNameToAETitleMap.clear();
        if (hostNameToAETitleMap!=null)
            this.hostNameToAETitleMap.putAll(hostNameToAETitleMap);
    }

    public void setExternalArchivesMap(Map<String, String> externalArchivesMap) {
        this.externalArchivesMap.clear();
        if (externalArchivesMap!=null)
            this.externalArchivesMap.putAll(externalArchivesMap);
    }

    public Code getIncorrectWorklistEntrySelectedCode() {
        return incorrectWorklistEntrySelectedCode;
    }

    public void setIncorrectWorklistEntrySelectedCode(Code code) {
        this.incorrectWorklistEntrySelectedCode = code;
    }

    public RejectionParam[] getRejectionParams() {
        return rejectionParams;
    }

    public void setRejectionParams(RejectionParam... rejectionParams) {
        this.rejectionParams = rejectionParams;
    }

    public String getFuzzyAlgorithmClass() {
        return fuzzyAlgorithmClass;
    }

    public void setFuzzyAlgorithmClass(String fuzzyAlgorithmClass) {
        this.fuzzyStr = fuzzyStr(fuzzyAlgorithmClass);
        this.fuzzyAlgorithmClass = fuzzyAlgorithmClass;
    }

    public boolean isDeIdentifyLogs() {
        return deIdentifyLogs;
    }

    public void setDeIdentifyLogs(boolean deIdentifyLogs) {
        this.deIdentifyLogs = deIdentifyLogs;
    }
    
    public int getUpdateDbRetries() {
        return updateDbRetries;
    }

    public void setUpdateDbRetries(int updateDbRetries) {
        this.updateDbRetries = updateDbRetries;
    }

    public int getUpdateDbDelay() {
        return updateDbDelay;
    }

    public void setUpdateDbDelay(int updateDbDelay) {
        this.updateDbDelay = updateDbDelay;
    }

    public FuzzyStr getFuzzyStr() {
        if (fuzzyStr == null)
            if (fuzzyAlgorithmClass == null)
                throw new IllegalStateException("No Fuzzy Algorithm Class configured");
            else
                fuzzyStr = fuzzyStr(fuzzyAlgorithmClass);
        return fuzzyStr;
    }

    private static FuzzyStr fuzzyStr(String s) {
        try {
            return (FuzzyStr) Class.forName(s).newInstance();
         } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }
    public int getConfigurationStaleTimeout() {
        return configurationStaleTimeout;
    }

    public void setConfigurationStaleTimeout(int configurationStaleTimeout) {
        this.configurationStaleTimeout = configurationStaleTimeout;
    }

    public int getWadoAttributesStaleTimeout() {
        return wadoAttributesStaleTimeout;
    }

    public void setWadoAttributesStaleTimeout(int wadoAttributesStaleTimeout) {
        this.wadoAttributesStaleTimeout = wadoAttributesStaleTimeout;
    }

    public int getRejectedObjectsCleanUpPollInterval() {
        return rejectedObjectsCleanUpPollInterval;
    }

    public void setRejectedObjectsCleanUpPollInterval(
            int rejectedObjectsCleanUpPollInterval) {
        this.rejectedObjectsCleanUpPollInterval = rejectedObjectsCleanUpPollInterval;
    }

    public int getRejectedObjectsCleanUpMaxNumberOfDeletes() {
        return rejectedObjectsCleanUpMaxNumberOfDeletes;
    }

    public void setRejectedObjectsCleanUpMaxNumberOfDeletes(
            int rejectedObjectsCleanUpMaxNumberOfDeletes) {
        this.rejectedObjectsCleanUpMaxNumberOfDeletes = rejectedObjectsCleanUpMaxNumberOfDeletes;
    }

    public int getMppsEmulationPollInterval() {
        return mppsEmulationPollInterval;
    }

    public void setMppsEmulationPollInterval(int mppsEmulationPollInterval) {
        this.mppsEmulationPollInterval = mppsEmulationPollInterval;
    }

    public int getArchivingSchedulerPollInterval() {
        return archivingSchedulerPollInterval;
    }

    public void setArchivingSchedulerPollInterval(int archivingPollInterval) {
        this.archivingSchedulerPollInterval = archivingPollInterval;
    }

    public int getSyncLocationStatusPollInterval() {
        return syncLocationStatusPollInterval;
    }

    public void setSyncLocationStatusPollInterval(int syncLocationStatusPollInterval) {
        this.syncLocationStatusPollInterval = syncLocationStatusPollInterval;
    }

    public int getSyncLocationStatusCheckDelay() {
        return syncLocationStatusCheckDelay;
    }

    public void setSyncLocationStatusCheckDelay(int syncLocationStatusCheckDelay) {
        this.syncLocationStatusCheckDelay = syncLocationStatusCheckDelay;
    }

    public int getSyncLocationStatusMaxNumberPerTask() {
        return syncLocationStatusMaxNumberPerTask;
    }

    public void setSyncLocationStatusMaxNumberPerTask(
            int syncLocationStatusMaxNumberPerTask) {
        this.syncLocationStatusMaxNumberPerTask = syncLocationStatusMaxNumberPerTask;
    }

    public String[] getSyncLocationStatusStorageSystemGroupIDs() {
        return syncLocationStatusStorageSystemGroupIDs;
    }

    public void setSyncLocationStatusStorageSystemGroupIDs(
            String... syncLocationStatusStorageSystemGroupIDs) {
        this.syncLocationStatusStorageSystemGroupIDs = syncLocationStatusStorageSystemGroupIDs;
    }

    public String[] getDisabledDecorators() {
        return disabledDecorators;
    }
    
    public void setDisabledDecorators(String ... disabledDecorators) {
        this.disabledDecorators = disabledDecorators;
    }

    public boolean isSyncLocationStatusVerifyArchived() {
        return syncLocationStatusVerifyArchived;
    }

    public void setSyncLocationStatusVerifyArchived(
            boolean syncLocationStatusVerifyArchived) {
        this.syncLocationStatusVerifyArchived = syncLocationStatusVerifyArchived;
    }

    public void clearTemplatesCache() {
        TemplatesCache cache = templatesCache;
        if (cache != null)
            cache.clear();
    }

    public Templates getTemplates(String uri) throws TransformerConfigurationException {
        TemplatesCache tmp = templatesCache;
        if (tmp == null)
            templatesCache = tmp = new TemplatesCache();
        return tmp.get(StringUtils.replaceSystemProperties(uri));
    }

    public void setAttributeFilter(Entity entity, AttributeFilter filter) {
        attributeFilters.put(entity,filter);
    }

    public AttributeFilter getAttributeFilter(Entity entity) {
        return attributeFilters.get(entity);
    }

    public Map<Entity, AttributeFilter> getAttributeFilters() {
        return attributeFilters;
    }

    public QueryRetrieveView[] getQueryRetrieveViews() {
        return queryRetrieveViews;
    }

    public void setAttributeFilters(Map<Entity, AttributeFilter> attributeFilters) {
        this.attributeFilters.clear();
        this.attributeFilters.putAll(attributeFilters);
    }

    public void setQueryRetrieveViews(QueryRetrieveView... queryRetrieveViews) {
        this.queryRetrieveViews = queryRetrieveViews;
    }

    public QueryRetrieveView getQueryRetrieveView(String viewID) {
        for (QueryRetrieveView view : queryRetrieveViews) {
            if (view.getViewID().equals(viewID))
                return view;
        }
        return null;
    }

    public IOCMConfig getIocmConfig() {
        return iocmConfig;
    }

    public void setIocmConfig(IOCMConfig cfg) {
        iocmConfig = cfg;
    }

    @Override
    public void reconfigure(DeviceExtension from) {
        ArchiveDeviceExtension arcdev = (ArchiveDeviceExtension) from;
        ReconfiguringIterator.reconfigure(arcdev, this, ArchiveDeviceExtension.class);
    }

    public StoreParam getStoreParam() {
        StoreParam storeParam = new StoreParam();
        storeParam.setFuzzyStr(getFuzzyStr());
        storeParam.setAttributeFilters(attributeFilters);
        storeParam.setDeIdentifyLogs(isDeIdentifyLogs());
        storeParam.setNullValueForQueryFields(getNullValueForQueryFields());
        storeParam.setIssuerOfPatientID(getDevice().getIssuerOfPatientID());
        return storeParam;
        
    }

    public QueryParam getQueryParam() {
        QueryParam queryParam = new QueryParam();
        queryParam.setFuzzyStr(getFuzzyStr());
        queryParam.setAttributeFilters(attributeFilters);
        queryParam.setDeIdentifyLogs(isDeIdentifyLogs());
        queryParam.setNullValueForQueryFields(getNullValueForQueryFields());
        return queryParam;
    }

    public String getFetchAETitle() {
        return fetchAETitle;
    }

    public void setFetchAETitle(String fetchAETitle) {
        this.fetchAETitle = fetchAETitle;
    }

    public PrivateDerivedFields getPrivateDerivedFields() {
        return privateDerivedFields;
    }

    public void addPrivateDerivedField(PrivateTag tag) {
        privateDerivedFields.add(tag);
    }

    public void setPrivateDerivedFields(PrivateDerivedFields tags) {
        privateDerivedFields.clear();
        if (tags != null)
            privateDerivedFields.add(tags);
    }

    public boolean removePrivateDerivedField(PrivateTag tag) {
        return privateDerivedFields.remove(tag);
    }

    /**
     * The property was moved to device level and converted to AE reference. Please access it directly through {@link Device#getDefaultAE()}
     */
    @Deprecated
    public String getDefaultAETitle() {
        return getDevice().getDefaultAE() == null ? "" : getDevice().getDefaultAE().getAETitle();
    }

    public int getPriorsCacheMaxResolvedPathEntries() {
        return priorsCacheMaxResolvedPathEntries;
    }

    public void setPriorsCacheMaxResolvedPathEntries(
            int priorsCacheMaxResolvedPathEntries) {
        this.priorsCacheMaxResolvedPathEntries = priorsCacheMaxResolvedPathEntries;
    }

    public int getPriorsCacheDeleteDuplicateLocationsDelay() {
        return priorsCacheDeleteDuplicateLocationsDelay;
    }

    public void setPriorsCacheDeleteDuplicateLocationsDelay(
            int priorsCacheDeleteDuplicateLocationsDelay) {
        this.priorsCacheDeleteDuplicateLocationsDelay = priorsCacheDeleteDuplicateLocationsDelay;
    }

    public int getPriorsCacheClearMaxLocationsPerDelete() {
        return priorsCacheClearMaxLocationsPerDelete;
    }

    public void setPriorsCacheClearMaxLocationsPerDelete(
            int priorsCacheClearMaxLocationsPerDelete) {
        this.priorsCacheClearMaxLocationsPerDelete = priorsCacheClearMaxLocationsPerDelete;
    }

    public int getDeletionServicePollInterval() {
        return deletionServicePollInterval;
    }

    public void setDeletionServicePollInterval(int deletionServicePollInterval) {
        this.deletionServicePollInterval = deletionServicePollInterval;
    }
    
    public String[] getVisibleImageSRClasses() {
        return visibleImageSRClasses;
    }

    public void setVisibleImageSRClasses(String[] visibleImageSRClasses) {
        this.visibleImageSRClasses = visibleImageSRClasses;
    }
    
    public String[] getNonVisibleImageSRClasses() {
        return nonVisibleImageSRClasses;
    }

    public void setNonVisibleImageSRClasses(String[] nonVisibleImageSRClasses) {
        this.nonVisibleImageSRClasses = nonVisibleImageSRClasses;
    }
    
    public boolean getUseWhitelistOfVisibleImageSRClasses() {
        return useWhitelistOfVisibleImageSRClasses;
    }

    public void setUseWhitelistOfVisibleImageSRClasses(boolean useWhitelistOfVisibleImageSRClasses) {
        this.useWhitelistOfVisibleImageSRClasses = useWhitelistOfVisibleImageSRClasses;
    }


    public boolean isVisibleSOPClass(String sopClassUID) {
        if (getUseWhitelistOfVisibleImageSRClasses()) {
            return doesImageSRListContainSOPClass(sopClassUID, getVisibleImageSRClasses());
        } else {
            return !doesImageSRListContainSOPClass(sopClassUID, getNonVisibleImageSRClasses());
        }
    }

    public boolean isUseNullForEmptyQueryFields() {
        return useNullForEmptyQueryFields;
    }

    public String getNullValueForQueryFields() {
        return isUseNullForEmptyQueryFields() ? null : "*";
    }


    public void setUseNullForEmptyQueryFields(boolean useNullForEmptyQueryFields) {
        this.useNullForEmptyQueryFields = useNullForEmptyQueryFields;
    }

    private boolean doesImageSRListContainSOPClass(String sopClassUID, String[] arrayOfSOPClasses) {
        return arrayOfSOPClasses != null && arrayOfSOPClasses.length>0 &&
                Arrays.asList(arrayOfSOPClasses).contains(sopClassUID);
    }

    public DeletionRules getDeletionRules() {
        return deletionRules;
    }

    public void addDeletionRule(DeletionRule rule) {
        deletionRules.add(rule);
    }

    public void setDeletionRules(DeletionRules rules) {
        deletionRules.clear();
        if (rules != null)
            deletionRules.add(rules);
    }

    public boolean removeDeletionRule(DeletionRule rule) {
        return deletionRules.remove(rule);
    }

    public int getMaxDeleteServiceRetries() {
        return maxDeleteServiceRetries;
    }

    public void setMaxDeleteServiceRetries(int maxDeleteServiceRetries) {
        this.maxDeleteServiceRetries = maxDeleteServiceRetries;
    }

    public String getDeleteServiceAllowedInterval() {
        return deleteServiceAllowedInterval;
    }

    public void setDeleteServiceAllowedInterval(String deleteServiceAllowedInterval) {
        this.deleteServiceAllowedInterval = deleteServiceAllowedInterval;
    }

    public int getDataVolumePerDayAverageOnNDays() {
        return dataVolumePerDayAverageOnNDays;
    }

    public void setDataVolumePerDayAverageOnNDays(int dataVolumePerDayAverageOnNDays) {
        this.dataVolumePerDayAverageOnNDays = dataVolumePerDayAverageOnNDays;
    }

    public String getDataVolumePerDayCalculationRange() {
        return dataVolumePerDayCalculationRange;
    }

    public void setDataVolumePerDayCalculationRange(
            String dataVolumePerDayCalculationRange) {
        this.dataVolumePerDayCalculationRange = dataVolumePerDayCalculationRange;
    }

	public Collection<ApplicationEntity> getIgnoreSeriesStudyMissmatchErrorsAETs() {
		return ignoreSeriesStudyMissmatchErrorsAETs;
	}

	public void setIgnoreSeriesStudyMissmatchErrorsAETs(Collection<ApplicationEntity> ignoreSeriesStudyMissmatchErrorsAETs) {
		this.ignoreSeriesStudyMissmatchErrorsAETs = ignoreSeriesStudyMissmatchErrorsAETs;
	}
	
	public void addIgnoreSeriesStudyMissmatchErrorsAET(ApplicationEntity ae) {
		this.ignoreSeriesStudyMissmatchErrorsAETs.add(ae);
	}
	public boolean removeIgnoreSeriesStudyMissmatchErrorsAET(ApplicationEntity ae) {
		return this.ignoreSeriesStudyMissmatchErrorsAETs.remove(ae);
	}

    public WeightWatcherConfiguration getWeightWatcherConfiguration() {
        return weightWatcherConfiguration;
    }

    public void setWeightWatcherConfiguration(WeightWatcherConfiguration weightWatcherConfiguration) {
        this.weightWatcherConfiguration = weightWatcherConfiguration;
    }
}
