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

package org.dcm4chee.archive.conf.prefs;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;
import org.dcm4che3.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che3.conf.prefs.PreferencesUtils;
import org.dcm4che3.conf.prefs.generic.PrefsConfigReader;
import org.dcm4che3.conf.prefs.generic.PrefsConfigWriter;
import org.dcm4che3.conf.prefs.imageio.PreferencesCompressionRulesConfiguration;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.ValueSelector;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.HostNameAEEntry;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.conf.RejectionParam;
import org.dcm4chee.archive.conf.StoreAction;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class PreferencesArchiveConfiguration extends
        PreferencesDicomConfigurationExtension {

    @Override
    protected void storeTo(Device device, Preferences prefs) {
        ArchiveDeviceExtension arcDev = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (arcDev == null)
            return;

        prefs.putBoolean("dcmArchiveDevice", true);
        PreferencesUtils.storeNotNull(prefs,
                "dcmIncorrectWorklistEntrySelectedCode",
                arcDev.getIncorrectWorklistEntrySelectedCode());
        PreferencesUtils.storeNotNull(prefs, "dcmFuzzyAlgorithmClass",
                arcDev.getFuzzyAlgorithmClass());
        PreferencesUtils.storeNotDef(prefs, "dcmConfigurationStaleTimeout",
                arcDev.getConfigurationStaleTimeout(), 0);
        PreferencesUtils.storeNotDef(prefs, "dcmWadoAttributesStaleTimeout",
                arcDev.getWadoAttributesStaleTimeout(), 0);
        PreferencesUtils.storeNotDef(prefs, "dcmHostNameAEResolution",
                arcDev.isHostnameAEresoultion(), false);
        PreferencesUtils.storeNotDef(prefs, "dcmDeIdentifyLogs",
                arcDev.isDeIdentifyLogs(), false);
        PreferencesUtils.storeNotDef(prefs, "dcmRejectedObjectsCleanUpPollInterval", 
                arcDev.getRejectedObjectsCleanUpPollInterval(), 0);

    }

    @Override
    protected void storeChilds(Device device, Preferences deviceNode) {
        ArchiveDeviceExtension arcDev = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (arcDev == null)
            return;

        storeAttributeFilters(arcDev, deviceNode);
        storeHostNameAEList(arcDev, deviceNode);
        storeRejectionParams(arcDev, deviceNode);
        storeQueryRetrieveViews(arcDev, deviceNode);
    }

    private static void storeAttributeFilters(ArchiveDeviceExtension arcDev,
            Preferences deviceNode) {
        Preferences afsNode = deviceNode.node("dcmAttributeFilter");
        for (Entity entity : Entity.values())
            storeTo(arcDev.getAttributeFilter(entity),
                    afsNode.node(entity.name()));
    }

    private static void storeHostNameAEList(ArchiveDeviceExtension arcDev,
            Preferences deviceNode) {
        Preferences hostNameMap = deviceNode
                .node(ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE);
        for (HostNameAEEntry entry : arcDev.getHostNameAEList())
            storeTo(entry, hostNameMap.node(entry.getHostName()));
    }

    private static void storeTo(HostNameAEEntry entry, Preferences prefs) {
        prefs.put("dicomAETitle", entry.getAeTitle());
        PreferencesUtils.storeNotNull(prefs, "dicomHostname",
                entry.getHostName());
    }

    private static void storeRejectionParams(ArchiveDeviceExtension arcDev,
            Preferences deviceNode) {
        Preferences rnNode = deviceNode.node("dcmRejectionNote");
        int rnIndex = 1;
        for (RejectionParam rejectionNote : arcDev.getRejectionParams())
            storeTo(rejectionNote, rnNode.node("" + rnIndex++));
    }

    private static void storeTo(RejectionParam rn, Preferences prefs) {
        PreferencesUtils.storeNotNull(prefs, "dcmRejectionNoteTitle",
                rn.getRejectionNoteTitle());
        PreferencesUtils.storeNotDef(prefs, "dcmRevokeRejection",
                rn.isRevokeRejection(), false);
        PreferencesUtils.storeNotNull(prefs, "dcmAcceptPreviousRejectedInstance",
                rn.getAcceptPreviousRejectedInstance());
        PreferencesUtils.storeNotEmpty(prefs, "dcmOverwritePreviousRejection",
                rn.getOverwritePreviousRejection());
        PreferencesUtils.storeNotNull(prefs, "dcmRejectedObjectRetentionTime", rn.getRetentionTime());
        PreferencesUtils.storeNotNull(prefs, "dcmRejectedObjectRetentionTimeUnit", rn.getRetentionTimeUnit());
    }

    private void storeQueryRetrieveViews(ArchiveDeviceExtension arcDev,
            Preferences deviceNode) {
        Preferences prefs = deviceNode.node("dcmQueryRetrieveView");
        for (QueryRetrieveView view : arcDev.getQueryRetrieveViews()) {
            storeTo(view, prefs.node(view.getViewID()));
        }
    }

    private void storeTo(QueryRetrieveView view, Preferences prefs) {
        PreferencesUtils.storeNotEmpty(prefs, "dcmShowInstancesRejectedByCode",
                view.getShowInstancesRejectedByCodes());
        PreferencesUtils.storeNotEmpty(prefs, "dcmHideRejectionNoteWithCode",
                view.getHideRejectionNotesWithCodes());
        PreferencesUtils.storeNotDef(prefs, "dcmHideNotRejectedInstances",
                view.isHideNotRejectedInstances(), false);
    }

    @Override
    protected void storeChilds(ApplicationEntity ae, Preferences aeNode) {
        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcAE == null)
            return;

        config.store(arcAE.getAttributeCoercions(), aeNode);
        PreferencesCompressionRulesConfiguration.store(
                arcAE.getCompressionRules(), aeNode);
    }

    private static void storeTo(AttributeFilter filter, Preferences prefs) {
        storeTags(prefs, "dcmTag", filter.getSelection());
        PreferencesUtils.storeNotNull(prefs, "dcmCustomAttribute1",
                filter.getCustomAttribute1());
        PreferencesUtils.storeNotNull(prefs, "dcmCustomAttribute2",
                filter.getCustomAttribute2());
        PreferencesUtils.storeNotNull(prefs, "dcmCustomAttribute3",
                filter.getCustomAttribute3());
    }

    private static void storeTags(Preferences prefs, String key, int[] tags) {
        if (tags.length != 0) {
            int count = 0;
            for (int tag : tags)
                prefs.put(key + '.' + (++count), TagUtils.toHexString(tag));
            prefs.putInt(key + ".#", count);
        }
    }

    @Override
    protected void storeTo(ApplicationEntity ae, Preferences prefs) {
        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcAE == null)
            return;

        prefs.putBoolean("dcmArchiveNetworkAE", true);

        try {
            ConfigWriter prefsWriter = new PrefsConfigWriter(prefs);
            ReflectiveConfig rc = new ReflectiveConfig(null, config);
            rc.storeConfig(arcAE, prefsWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void loadFrom(Device device, Preferences prefs)
            throws CertificateException, BackingStoreException {
        if (!prefs.getBoolean("dcmArchiveDevice", false))
            return;

        ArchiveDeviceExtension arcdev = new ArchiveDeviceExtension();
        device.addDeviceExtension(arcdev);

        arcdev.setIncorrectWorklistEntrySelectedCode(new Code(prefs.get(
                "dcmIncorrectWorklistEntrySelectedCode", null)));
        arcdev.setFuzzyAlgorithmClass(prefs.get("dcmFuzzyAlgorithmClass", null));
        arcdev.setConfigurationStaleTimeout(prefs.getInt(
                "dcmConfigurationStaleTimeout", 0));
        arcdev.setWadoAttributesStaleTimeout(prefs.getInt(
                "dcmWadoAttributesStaleTimeout", 0));
        arcdev.setHostnameAEresoultion(prefs.getBoolean(
                "dcmHostNameAEResolution", false));
        arcdev.setDeIdentifyLogs(prefs.getBoolean(
                "dcmDeIdentifyLogs", false));
        arcdev.setRejectedObjectsCleanUpPollInterval(prefs.getInt(
                "dcmRejectedObjectsCleanUpPollInterval", 0));
    }

    @Override
    protected void loadChilds(Device device, Preferences deviceNode)
            throws BackingStoreException, ConfigurationException {
        ArchiveDeviceExtension arcdev = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (arcdev == null)
            return;

        loadAttributeFilters(arcdev, deviceNode);
        loadHostNameAEList(arcdev, deviceNode);
        loadRejectionParams(arcdev, deviceNode);
        loadQueryRetrieveViews(arcdev, deviceNode);
    }

    private static void loadHostNameAEList(ArchiveDeviceExtension arcdev,
            Preferences deviceNode) throws BackingStoreException {
        Preferences prefs = deviceNode
                .node(ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE);
        Collection<HostNameAEEntry> hostNameAEList = new ArrayList<HostNameAEEntry>();
       
        for (String hostname : prefs.childrenNames())
            hostNameAEList.add(loadHostNameAEEntry(prefs.node(hostname)));

        arcdev.setHostNameAEList(hostNameAEList);
    }

    private static HostNameAEEntry loadHostNameAEEntry(Preferences prefs) {
        return new HostNameAEEntry(
                prefs.get("dicomHostname", null),
                prefs.get("dicomAETitle", null));
    }

    private static void loadRejectionParams(ArchiveDeviceExtension arcdev,
            Preferences deviceNode) throws BackingStoreException {
        Preferences prefs = deviceNode.node("dcmRejectionNote");
        Collection<RejectionParam> rejectionNotes = new ArrayList<RejectionParam>();
        for (String nodeName : prefs.childrenNames())
            rejectionNotes.add(loadRejectionNote(prefs.node(nodeName)));

        arcdev.setRejectionParams(rejectionNotes.toArray(
                new RejectionParam[rejectionNotes.size()]));
    }

    private static RejectionParam loadRejectionNote(Preferences prefs) {
        RejectionParam param = new RejectionParam();
        param.setRejectionNoteTitle(new Code(prefs.get("dcmRejectionNoteTitle", null)));
        param.setRevokeRejection(prefs.getBoolean("dcmRevokeRejection", false));
        param.setAcceptPreviousRejectedInstance(
                storeActionOf(prefs, "dcmAcceptPreviousRejectedInstance"));
        param.setOverwritePreviousRejection(
                PreferencesUtils.codeArray(prefs, "dcmOverwritePreviousRejection"));
        param.setRetentionTime(prefs.getInt("dcmRejectedObjectRetentionTime",-1));
        param.setRetentionTimeUnit(TimeUnit.valueOf(prefs.get("dcmRejectedObjectRetentionTimeUnit","DAYS")));
        return param;
    }

    private static StoreAction storeActionOf(Preferences prefs, String key) {
        String name = prefs.get(key, null);
        return name != null ? StoreAction.valueOf(name) : null;
    }

    private void loadQueryRetrieveViews(ArchiveDeviceExtension arcdev,
            Preferences deviceNode) throws BackingStoreException {
        Preferences prefs = deviceNode.node("dcmQueryRetrieveView");
        Collection<QueryRetrieveView> views = new ArrayList<QueryRetrieveView>();
        for (String viewID : prefs.childrenNames())
            views.add(loadQueryRetrieveView(prefs.node(viewID)));

        arcdev.setQueryRetrieveViews(
                views.toArray(new QueryRetrieveView[views.size()]));
    }

    private QueryRetrieveView loadQueryRetrieveView(Preferences prefs) {
        QueryRetrieveView view = new QueryRetrieveView();
        view.setViewID(prefs.name());
        view.setShowInstancesRejectedByCodes(PreferencesUtils.codeArray(
                prefs, "dcmShowInstancesRejectedByCode"));
        view.setHideRejectionNotesWithCodes(PreferencesUtils.codeArray(
                prefs, "dcmHideRejectionNoteWithCode"));
        view.setHideNotRejectedInstances(
                prefs.getBoolean("dcmHideNotRejectedInstances", false));
        return view;
    }

    @Override
    protected void loadFrom(ApplicationEntity ae, Preferences prefs) {
        if (!prefs.getBoolean("dcmArchiveNetworkAE", false))
            return;

        ArchiveAEExtension arcae = new ArchiveAEExtension();
        ae.addAEExtension(arcae);

        try {
            ConfigReader prefsReader = new PrefsConfigReader(prefs);
            ReflectiveConfig rc = new ReflectiveConfig(null, config);
            rc.readConfig(arcae, prefsReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void loadChilds(ApplicationEntity ae, Preferences aeNode)
            throws BackingStoreException {
        ArchiveAEExtension arcae = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcae == null)
            return;

        config.load(arcae.getAttributeCoercions(), aeNode);
        PreferencesCompressionRulesConfiguration.load(
                arcae.getCompressionRules(), aeNode);
    }

    private static void loadAttributeFilters(ArchiveDeviceExtension device,
            Preferences deviceNode) throws BackingStoreException {
        Preferences afsNode = deviceNode.node("dcmAttributeFilter");
        for (String entity : afsNode.childrenNames()) {
            Preferences acNode = afsNode.node(entity);
            AttributeFilter filter = new AttributeFilter(tags(acNode, "dcmTag"));
            filter.setCustomAttribute1(valueSelectorOf(acNode,
                    "dcmCustomAttribute1"));
            filter.setCustomAttribute2(valueSelectorOf(acNode,
                    "dcmCustomAttribute2"));
            filter.setCustomAttribute3(valueSelectorOf(acNode,
                    "dcmCustomAttribute3"));
            device.setAttributeFilter(Entity.valueOf(entity), filter);
        }
    }

    private static ValueSelector valueSelectorOf(Preferences acNode, String key) {
        String s = acNode.get(key, null);
        return s != null ? ValueSelector.valueOf(s) : null;
    }

    private static int[] tags(Preferences prefs, String key) {
        int n = prefs.getInt(key + ".#", 0);
        int[] is = new int[n];
        for (int i = 0; i < n; i++)
            is[i] = Integer.parseInt(prefs.get(key + '.' + (i + 1), null), 16);
        return is;
    }

    @Override
    protected void storeDiffs(Device a, Device b, Preferences prefs) {
        ArchiveDeviceExtension aa = a
                .getDeviceExtension(ArchiveDeviceExtension.class);
        ArchiveDeviceExtension bb = b
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (aa == null || bb == null)
            return;

        PreferencesUtils.storeDiff(prefs,
                "dcmIncorrectWorklistEntrySelectedCode",
                aa.getIncorrectWorklistEntrySelectedCode(),
                bb.getIncorrectWorklistEntrySelectedCode());
        PreferencesUtils.storeDiff(prefs, "dcmFuzzyAlgorithmClass",
                aa.getFuzzyAlgorithmClass(), bb.getFuzzyAlgorithmClass());
        PreferencesUtils.storeDiff(prefs, "dcmConfigurationStaleTimeout",
                aa.getConfigurationStaleTimeout(),
                bb.getConfigurationStaleTimeout(), 0);
        PreferencesUtils.storeDiff(prefs, "dcmWadoAttributesStaleTimeout",
                aa.getWadoAttributesStaleTimeout(),
                bb.getWadoAttributesStaleTimeout(), 0);
        PreferencesUtils.storeDiff(prefs, "dcmHostNameAEResolution",
                aa.isHostnameAEresoultion(), bb.isHostnameAEresoultion(), false);
        PreferencesUtils.storeDiff(prefs, "dcmDeIdentifyLogs",
                aa.isDeIdentifyLogs(), bb.isDeIdentifyLogs(), false);
        PreferencesUtils.storeDiff(prefs, "dcmRejectedObjectsCleanUpPollInterval",
                aa.getRejectedObjectsCleanUpPollInterval(), 
                bb.getRejectedObjectsCleanUpPollInterval(),0);
    }

    @Override
    protected void storeDiffs(ApplicationEntity a, ApplicationEntity b,
            Preferences prefs) {
        ArchiveAEExtension aa = a.getAEExtension(ArchiveAEExtension.class);
        ArchiveAEExtension bb = b.getAEExtension(ArchiveAEExtension.class);
        if (aa == null || bb == null)
            return;

        try {
            ConfigWriter prefsDiffWriter = new PrefsConfigWriter(prefs);
            ReflectiveConfig rc = new ReflectiveConfig(null, config);
            rc.storeConfigDiffs(a, b, prefsDiffWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    protected void mergeChilds(Device prev, Device device,
            Preferences deviceNode) throws BackingStoreException {
        ArchiveDeviceExtension aa = prev
                .getDeviceExtension(ArchiveDeviceExtension.class);
        ArchiveDeviceExtension bb = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (aa == null || bb == null)
            return;

        mergeAttributeFilters(aa, bb, deviceNode);
        mergeHostnameAEList(aa, bb, deviceNode);
        mergeRejectionParams(aa, bb, deviceNode);
        mergeQueryRetrieveViews(aa, bb, deviceNode);
    }

    private static void mergeHostnameAEList(ArchiveDeviceExtension prev,
            ArchiveDeviceExtension arcDev, Preferences deviceNode)
            throws BackingStoreException {
        Preferences hostNameAEMap = deviceNode
                .node(ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE);

        storeDiffs(hostNameAEMap, prev.getHostNameAEList(),
                arcDev.getHostNameAEList());
    }

    private static void mergeAttributeFilters(ArchiveDeviceExtension prev,
            ArchiveDeviceExtension arcDev, Preferences deviceNode) {
        Preferences afsNode = deviceNode.node("dcmAttributeFilter");
        for (Entity entity : Entity.values())
            storeDiffs(afsNode.node(entity.name()),
                    prev.getAttributeFilter(entity),
                    arcDev.getAttributeFilter(entity));
    }

    private static void storeDiffs(Preferences prefs,
            Collection<HostNameAEEntry> prevList,
            Collection<HostNameAEEntry> current) throws BackingStoreException {
        for (HostNameAEEntry entry : prevList) {
            String host = entry.getHostName();
            if (findWithEqualHostname(current,host) == null) {
                Preferences node = prefs.node(host);
                node.removeNode();
                node.flush();
            }
        }
        for (HostNameAEEntry entry : current) {
            String host = entry.getHostName();
            HostNameAEEntry entryOld = findWithEqualHostname(prevList,host);
            if(entryOld == null)
                storeTo(entry, prefs);
            else
                storeDiffs(prefs.node(host), entryOld, entry);
        }
    }

    private static HostNameAEEntry findWithEqualHostname(
            Collection<HostNameAEEntry> from, String hostName) {
        for(HostNameAEEntry e: from)
            if(e.getHostName().equalsIgnoreCase(hostName))
                return e;
        return null;
    }

    private static void storeDiffs(Preferences prefs, HostNameAEEntry prev,
            HostNameAEEntry current) {
        if (prev != null) {
            PreferencesUtils.storeDiff(prefs, "dicomHostname",
                    prev.getHostName(), current.getHostName());
            PreferencesUtils.storeDiff(prefs, "dicomAETitle",
                    prev.getHostName(), current.getHostName());
        } else
            storeTo(current, prefs);
    }

    private static void mergeRejectionParams(ArchiveDeviceExtension prev,
            ArchiveDeviceExtension arcDev, Preferences deviceNode)
                    throws BackingStoreException {
        Preferences prefs = deviceNode.node("dcmRejectionNote");
        storeDiffs(prefs, prev.getRejectionParams(),
                arcDev.getRejectionParams());
    }

    private static void storeDiffs(Preferences prefs, RejectionParam[] prev,
            RejectionParam[] rejectionNotes) throws BackingStoreException {
        int n = Math.min(prev.length, rejectionNotes.length);
        for (int i = 0; i < n; i++) {
            storeDiffs(prev[i], rejectionNotes[i], prefs.node("" + (i+1)));
        }
        for (int i = n; i < rejectionNotes.length; i++) {
            storeTo(rejectionNotes[i], prefs.node("" + (i+1)));
        }
        for (int i = n; i < prev.length; i++) {
            Preferences node = prefs.node("" + (i+1));
            node.removeNode();
            node.flush();
        }
    }

    private static void storeDiffs(RejectionParam prev, RejectionParam rn,
            Preferences prefs) {
        PreferencesUtils.storeDiff(prefs, "dcmRejectionNoteTitle",
                prev.getRejectionNoteTitle(),
                rn.getRejectionNoteTitle());
        PreferencesUtils.storeDiff(prefs, "dcmRevokeRejection",
                prev.isRevokeRejection(),
                rn.isRevokeRejection(),
                false);
        PreferencesUtils.storeDiff(prefs, "dcmAcceptPreviousRejectedInstance",
                prev.getAcceptPreviousRejectedInstance(),
                rn.getAcceptPreviousRejectedInstance());
        PreferencesUtils.storeDiff(prefs, "dcmAcceptPreviousRejectedInstance",
                prev.getOverwritePreviousRejection(),
                rn.getOverwritePreviousRejection());
        PreferencesUtils.storeDiff(prefs, "dcmRejectedObjectRetentionTime", prev.getRetentionTime(), rn.getRetentionTime());
        PreferencesUtils.storeDiff(prefs, "dcmRejectedObjectRetentionTimeUnit", prev.getRetentionTimeUnit(), rn.getRetentionTimeUnit());
    }

    private void mergeQueryRetrieveViews(ArchiveDeviceExtension prev,
            ArchiveDeviceExtension arcDev, Preferences deviceNode)
                    throws BackingStoreException {
        Preferences prefs = deviceNode.node("dcmQueryRetrieveView");
        for (QueryRetrieveView entry : prev.getQueryRetrieveViews()) {
            String viewID = entry.getViewID();
            if (arcDev.getQueryRetrieveView(viewID) == null)
                prefs.node(viewID).removeNode();
        }
        for (QueryRetrieveView entryNew : arcDev.getQueryRetrieveViews()) {
            String viewID = entryNew.getViewID();
            Preferences node = prefs.node(viewID);
            QueryRetrieveView entryOld = prev.getQueryRetrieveView(viewID);
            if (entryOld == null) {
                storeTo(entryNew, node);
            } else{
                storeDiffs(node, entryOld, entryNew);
            }
        }
        prefs.flush();
    }

    private void storeDiffs(Preferences prefs, QueryRetrieveView prev,
            QueryRetrieveView view) {
        PreferencesUtils.storeDiff(prefs, "dcmShowInstancesRejectedByCode",
                prev.getShowInstancesRejectedByCodes(),
                view.getShowInstancesRejectedByCodes());
        PreferencesUtils.storeDiff(prefs, "dcmHideRejectionNoteWithCode",
                prev.getHideRejectionNotesWithCodes(),
                view.getHideRejectionNotesWithCodes());
        PreferencesUtils.storeDiff(prefs, "dcmHideNotRejectedInstances",
                prev.isHideNotRejectedInstances(),
                view.isHideNotRejectedInstances(),
                false);
        
    }

    private static void storeDiffs(Preferences prefs, AttributeFilter prev,
            AttributeFilter filter) {
        storeDiffTags(prefs, "dcmTag", prev.getSelection(),
                filter.getSelection());
        PreferencesUtils.storeDiff(prefs, "dcmCustomAttribute1",
                prev.getCustomAttribute1(), filter.getCustomAttribute1());
        PreferencesUtils.storeDiff(prefs, "dcmCustomAttribute2",
                prev.getCustomAttribute2(), filter.getCustomAttribute2());
        PreferencesUtils.storeDiff(prefs, "dcmCustomAttribute3",
                prev.getCustomAttribute3(), filter.getCustomAttribute3());
    }

    private static void storeDiffTags(Preferences prefs, String key,
            int[] prevs, int[] vals) {
        if (!Arrays.equals(prevs, vals)) {
            PreferencesUtils.removeKeys(prefs, key, vals.length, prevs.length);
            storeTags(prefs, key, vals);
        }
    }

    @Override
    protected void mergeChilds(ApplicationEntity prev, ApplicationEntity ae,
            Preferences aePrefs) throws BackingStoreException {
        ArchiveAEExtension aa = prev.getAEExtension(ArchiveAEExtension.class);
        ArchiveAEExtension bb = ae.getAEExtension(ArchiveAEExtension.class);
        if (aa == null || bb == null)
            return;

        config.merge(aa.getAttributeCoercions(), bb.getAttributeCoercions(),
                aePrefs);
        PreferencesCompressionRulesConfiguration.merge(
                aa.getCompressionRules(), bb.getCompressionRules(), aePrefs);

    }

}
