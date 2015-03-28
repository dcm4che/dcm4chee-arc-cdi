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

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.conf.api.extensions.ReconfiguringIterator;
import org.dcm4che3.data.Code;
import org.dcm4che3.io.TemplatesCache;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4che3.util.StringUtils;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@LDAP(objectClasses = "dcmArchiveDevice", noContainerNode = true)
@ConfigurableClass
public class ArchiveDeviceExtension extends DeviceExtension {

    private static final long serialVersionUID = -3611223780276386740L;

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

    @ConfigurableProperty(name = "dcmUpdateDbRetries", defaultValue = "1")
    private int updateDbRetries = 1;

    @LDAP(
            distinguishingField = "dicomHostName",
            mapValueAttribute = "dicomAETitle",
            mapEntryObjectClass= "dcmHostNameAEEntry"
    )
    @ConfigurableProperty(name = "HostNameAETitleMap")
    private final Map<String, String> hostNameToAETitleMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

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

    @ConfigurableProperty(name = "dcmMppsEmulationPollInterval", defaultValue = "0")
    private int mppsEmulationPollInterval;

    @ConfigurableProperty(name = "dcmArchivingSchedulerPollInterval", defaultValue = "0")
    private int archivingSchedulerPollInterval;

    private transient FuzzyStr fuzzyStr;
    private transient TemplatesCache templatesCache;

    public boolean isHostnameAEResolution() {
        return hostnameAEResolution;
    }

    public void setHostnameAEResolution(boolean hostnameAEResolution) {
        this.hostnameAEResolution = hostnameAEResolution;
    }

    public Map<String, String> getHostNameToAETitleMap() {
        return hostNameToAETitleMap;
    }

    public void setHostNameToAETitleMap(Map<String, String> hostNameToAETitleMap) {
        this.hostNameToAETitleMap.clear();
        if (hostNameToAETitleMap!=null)
            this.hostNameToAETitleMap.putAll(hostNameToAETitleMap);
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
        return storeParam;
        
    }

    public QueryParam getQueryParam() {
        QueryParam queryParam = new QueryParam();
        queryParam.setFuzzyStr(getFuzzyStr());
        queryParam.setAttributeFilters(attributeFilters);
        queryParam.setDeIdentifyLogs(isDeIdentifyLogs());
        return queryParam;
    }

}
