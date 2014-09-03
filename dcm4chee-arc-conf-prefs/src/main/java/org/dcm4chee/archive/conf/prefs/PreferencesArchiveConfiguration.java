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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.DiffWriter;
import org.dcm4che3.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che3.conf.prefs.PreferencesUtils;
import org.dcm4che3.conf.prefs.generic.PrefsConfigReader;
import org.dcm4che3.conf.prefs.generic.PrefsConfigWriter;
import org.dcm4che3.conf.prefs.generic.PrefsDiffWriter;
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
        PreferencesUtils.storeNotNull(prefs,
                "dcmRejectedForQualityReasonsCode",
                arcDev.getRejectedForQualityReasonsCode());
        PreferencesUtils.storeNotNull(prefs,
                "dcmRejectedForPatientSafetyReasonsCode",
                arcDev.getRejectedForPatientSafetyReasonsCode());
        PreferencesUtils.storeNotNull(prefs,
                "dcmIncorrectModalityWorklistEntryCode",
                arcDev.getIncorrectModalityWorklistEntryCode());
        PreferencesUtils.storeNotNull(prefs,
                "dcmDataRetentionPeriodExpiredCode",
                arcDev.getDataRetentionPeriodExpiredCode());
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
    }

    @Override
    protected void storeChilds(Device device, Preferences deviceNode) {
        ArchiveDeviceExtension arcDev = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (arcDev == null)
            return;

        Preferences afsNode = deviceNode.node("dcmAttributeFilter");
        for (Entity entity : Entity.values())
            storeTo(arcDev.getAttributeFilter(entity),
                    afsNode.node(entity.name()));

        Preferences hostNameMap = deviceNode
                .node(ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE);
        hostNameMap.put("dcmHostNameAEFallBackAE", arcDev
                .getHostNameAEFallBackEntry().getAeTitle());
        HostNameAEEntry[] tmpMap = new HostNameAEEntry[arcDev.getHostNameAEList().size()];
        for(int i=0;i<arcDev.getHostNameAEList().size();i++)
        {
            tmpMap[i]=arcDev.getHostNameAEList().get(i);
        }
        storeTo(tmpMap, hostNameMap);
    }

    private void storeTo(HostNameAEEntry[] entries, Preferences prefs) {
        for (HostNameAEEntry entry : entries)
            storeTo(entry, prefs.node(entry.getHostName()));
    }

    private void storeTo(HostNameAEEntry entry, Preferences prefs) {
        prefs.put("dicomAETitle", entry.getAeTitle());
        PreferencesUtils.storeNotNull(prefs, "dicomHostname",
                entry.getHostName());
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
        arcdev.setRejectedForQualityReasonsCode(new Code(prefs.get(
                "dcmRejectedForQualityReasonsCode", null)));
        arcdev.setRejectedForPatientSafetyReasonsCode(new Code(prefs.get(
                "dcmRejectedForPatientSafetyReasonsCode", null)));
        arcdev.setIncorrectModalityWorklistEntryCode(new Code(prefs.get(
                "dcmIncorrectModalityWorklistEntryCode", null)));
        arcdev.setDataRetentionPeriodExpiredCode(new Code(prefs.get(
                "dcmDataRetentionPeriodExpiredCode", null)));
        arcdev.setFuzzyAlgorithmClass(prefs.get("dcmFuzzyAlgorithmClass", null));
        arcdev.setConfigurationStaleTimeout(prefs.getInt(
                "dcmConfigurationStaleTimeout", 0));
        arcdev.setWadoAttributesStaleTimeout(prefs.getInt(
                "dcmWadoAttributesStaleTimeout", 0));
        arcdev.setHostnameAEresoultion(prefs.getBoolean(
                "dcmHostNameAEResolution", false));
        arcdev.setDeIdentifyLogs(prefs.getBoolean(
                "dcmDeIdentifyLogs", false));        
    }

    @Override
    protected void loadChilds(Device device, Preferences deviceNode)
            throws BackingStoreException, ConfigurationException {
        ArchiveDeviceExtension arcdev = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (arcdev == null)
            return;

        loadAttributeFilters(arcdev, deviceNode);
        Preferences prefs = deviceNode
                .node(ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE);
        ArrayList<HostNameAEEntry> hostNameAEList = new ArrayList<HostNameAEEntry>();
        if (prefs.get("dcmHostNameAEFallBackAE", null) == null)
            prefs.put("dcmHostNameAEFallBackAE", "FALLBACK");
        arcdev.setHostNameAEFallBackEntry(new HostNameAEEntry("*",
                (String) prefs.get("dcmHostNameAEFallBackAE", "FALLBACK")));
        for (String hostname : prefs.childrenNames())
            hostNameAEList.add(loadHostNameAEEntry(prefs.node(hostname)));

        arcdev.setHostNameAEList(hostNameAEList);
    }

    private HostNameAEEntry loadHostNameAEEntry(Preferences prefs) {
        return new HostNameAEEntry(prefs.get("dicomHostname", null), prefs.get(
                "dicomAETitle", null));
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
        PreferencesUtils.storeDiff(prefs, "dcmRejectedForQualityReasonsCode",
                aa.getRejectedForQualityReasonsCode(),
                bb.getRejectedForQualityReasonsCode());
        PreferencesUtils.storeDiff(prefs,
                "dcmRejectedForPatientSafetyReasonsCode",
                aa.getRejectedForPatientSafetyReasonsCode(),
                bb.getRejectedForPatientSafetyReasonsCode());
        PreferencesUtils.storeDiff(prefs,
                "dcmIncorrectModalityWorklistEntryCode",
                aa.getIncorrectModalityWorklistEntryCode(),
                bb.getIncorrectModalityWorklistEntryCode());
        PreferencesUtils.storeDiff(prefs, "dcmDataRetentionPeriodExpiredCode",
                aa.getDataRetentionPeriodExpiredCode(),
                bb.getDataRetentionPeriodExpiredCode());
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

        Preferences afsNode = deviceNode.node("dcmAttributeFilter");
        for (Entity entity : Entity.values())
            storeDiffs(afsNode.node(entity.name()),
                    aa.getAttributeFilter(entity),
                    bb.getAttributeFilter(entity));

        Preferences hostNameAEMap = deviceNode
                .node(ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE);
        if (aa.getHostNameAEFallBackEntry() != null) {
            PreferencesUtils.storeDiff(hostNameAEMap,
                    "dcmHostNameAEFallBackAE", aa.getHostNameAEFallBackEntry()
                            .getAeTitle(), bb.getHostNameAEFallBackEntry()
                            .getAeTitle());
        } else
            hostNameAEMap.put("dcmHostNameAEFallBackAE", bb
                    .getHostNameAEFallBackEntry().getAeTitle());
        storeDiffs(hostNameAEMap, aa.getHostNameAEList(),
                bb.getHostNameAEList());
    }

    private void storeDiffs(Preferences prefs,
            ArrayList<HostNameAEEntry> prevList,
            ArrayList<HostNameAEEntry> current) throws BackingStoreException {
        for (HostNameAEEntry entry : prevList) {
            String host = entry.getHostName();
            if (!current.contains(host)) {
                Preferences node = prefs.node(host);
                node.removeNode();
                node.flush();
            }
        }
        for (HostNameAEEntry entry : current) {
            String host = entry.getHostName();
            storeDiffs(prefs.node(host), getMatchingEntry(entry, prevList),
                    entry);
        }
    }

    private HostNameAEEntry getMatchingEntry(HostNameAEEntry entry,
            ArrayList<HostNameAEEntry> lst) {
        for (HostNameAEEntry item : lst)
            if (entry.getHostName().compareTo(item.getHostName()) == 0)
                return item;

        return null;
    }

    private void storeDiffs(Preferences prefs, HostNameAEEntry prev,
            HostNameAEEntry current) {
        if (prev != null) {
            PreferencesUtils.storeDiff(prefs, "dicomHostname",
                    prev.getHostName(), current.getHostName());
            PreferencesUtils.storeDiff(prefs, "dicomAETitle",
                    prev.getHostName(), current.getHostName());
        } else
            storeTo(current, prefs);
    }

    private void storeDiffs(Preferences prefs, AttributeFilter prev,
            AttributeFilter filter) {
        storeTags(prefs, "dcmTag", filter.getSelection());
        storeDiffTags(prefs, "dcmTag", prev.getSelection(),
                filter.getSelection());
        PreferencesUtils.storeDiff(prefs, "dcmCustomAttribute1",
                prev.getCustomAttribute1(), filter.getCustomAttribute1());
        PreferencesUtils.storeDiff(prefs, "dcmCustomAttribute2",
                prev.getCustomAttribute2(), filter.getCustomAttribute2());
        PreferencesUtils.storeDiff(prefs, "dcmCustomAttribute3",
                prev.getCustomAttribute3(), filter.getCustomAttribute3());
    }

    private void storeDiffTags(Preferences prefs, String key, int[] prevs,
            int[] vals) {
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
