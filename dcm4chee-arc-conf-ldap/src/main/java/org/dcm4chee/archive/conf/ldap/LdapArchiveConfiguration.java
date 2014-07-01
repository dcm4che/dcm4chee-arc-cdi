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

package org.dcm4chee.archive.conf.ldap;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.DiffWriter;
import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.conf.ldap.generic.LdapConfigReader;
import org.dcm4che3.conf.ldap.generic.LdapConfigWriter;
import org.dcm4che3.conf.ldap.generic.LdapDiffWriter;
import org.dcm4che3.conf.ldap.imageio.LdapCompressionRulesConfiguration;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.ValueSelector;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.AttributesFormat;
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
public class LdapArchiveConfiguration extends LdapDicomConfigurationExtension {

    @Override
    protected void storeTo(Device device, Attributes attrs) {
        ArchiveDeviceExtension arcDev = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (arcDev == null)
            return;

        attrs.get("objectclass").add("dcmArchiveDevice");
        LdapUtils.storeNotNull(attrs, "dcmIncorrectWorklistEntrySelectedCode",
                arcDev.getIncorrectWorklistEntrySelectedCode());
        LdapUtils.storeNotNull(attrs, "dcmRejectedForQualityReasonsCode",
                arcDev.getRejectedForQualityReasonsCode());
        LdapUtils.storeNotNull(attrs, "dcmRejectedForPatientSafetyReasonsCode",
                arcDev.getRejectedForPatientSafetyReasonsCode());
        LdapUtils.storeNotNull(attrs, "dcmIncorrectModalityWorklistEntryCode",
                arcDev.getIncorrectModalityWorklistEntryCode());
        LdapUtils.storeNotNull(attrs, "dcmDataRetentionPeriodExpiredCode",
                arcDev.getDataRetentionPeriodExpiredCode());
        LdapUtils.storeNotNull(attrs, "dcmFuzzyAlgorithmClass",
                arcDev.getFuzzyAlgorithmClass());
        LdapUtils.storeNotDef(attrs, "dcmConfigurationStaleTimeout",
                arcDev.getConfigurationStaleTimeout(), 0);
        LdapUtils.storeNotDef(attrs, "dcmWadoAttributesStaleTimeout",
                arcDev.getWadoAttributesStaleTimeout(), 0);
        LdapUtils.storeNotDef(attrs, "dcmHostNameAEResolution",
                arcDev.isHostnameAEresoultion(),false);
    }

    @Override
    protected void storeChilds(String deviceDN, Device device)
            throws NamingException {
        ArchiveDeviceExtension arcDev = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (arcDev == null)
            return;

        for (Entity entity : Entity.values())
            config.createSubcontext(
                    LdapUtils.dnOf("dcmEntity", entity.toString(), deviceDN),
                    storeTo(arcDev.getAttributeFilter(entity), entity,
                            new BasicAttributes(true)));

        for (HostNameAEEntry entry : arcDev.getHostNameAEList()) {
            Attributes attrs = new BasicAttributes(false);
            attrs.put("objectclass", "dcmHostNameAEEntry");
            attrs.put("dicomAETitle", entry.getAeTitle());
            attrs.put("dicomHostname", entry.getHostName());
            config.createSubcontext(LdapUtils.dnOf("cn",
                    arcDev.ARCHIVE_HOST_AE_MAP_NODE, deviceDN), attrs);
        }
    }

    @Override
    protected void storeChilds(String aeDN, ApplicationEntity ae)
            throws NamingException {
        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcAE == null)
            return;

        config.store(arcAE.getAttributeCoercions(), aeDN);
        new LdapCompressionRulesConfiguration(config).store(
                arcAE.getCompressionRules(), aeDN);
    }

    private static Attributes storeTo(AttributeFilter filter, Entity entity,
            BasicAttributes attrs) {
        attrs.put("objectclass", "dcmAttributeFilter");
        attrs.put("dcmEntity", entity.name());
        attrs.put(tagsAttr("dcmTag", filter.getSelection()));
        LdapUtils.storeNotNull(attrs, "dcmCustomAttribute1",
                filter.getCustomAttribute1());
        LdapUtils.storeNotNull(attrs, "dcmCustomAttribute2",
                filter.getCustomAttribute2());
        LdapUtils.storeNotNull(attrs, "dcmCustomAttribute3",
                filter.getCustomAttribute3());
        return attrs;
    }

    private static Attribute tagsAttr(String attrID, int[] tags) {
        Attribute attr = new BasicAttribute(attrID);
        for (int tag : tags)
            attr.add(TagUtils.toHexString(tag));
        return attr;
    }

    @Override
    protected void storeTo(ApplicationEntity ae, Attributes attrs) {
        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcAE == null)
            return;

        attrs.get("objectclass").add("dcmArchiveNetworkAE");

        try {

            ConfigWriter ldapWriter = new LdapConfigWriter(attrs);
            ReflectiveConfig.store(arcAE, ldapWriter);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void loadFrom(Device device, Attributes attrs)
            throws NamingException, CertificateException {
        if (!LdapUtils.hasObjectClass(attrs, "dcmArchiveDevice"))
            return;

        ArchiveDeviceExtension arcdev = new ArchiveDeviceExtension();
        device.addDeviceExtension(arcdev);
        arcdev.setIncorrectWorklistEntrySelectedCode(new Code(LdapUtils
                .stringValue(
                        attrs.get("dcmIncorrectWorklistEntrySelectedCode"),
                        null)));
        arcdev.setRejectedForQualityReasonsCode(new Code(LdapUtils.stringValue(
                attrs.get("dcmRejectedForQualityReasonsCode"), null)));
        arcdev.setRejectedForPatientSafetyReasonsCode(new Code(LdapUtils
                .stringValue(
                        attrs.get("dcmRejectedForPatientSafetyReasonsCode"),
                        null)));
        arcdev.setIncorrectModalityWorklistEntryCode(new Code(LdapUtils
                .stringValue(
                        attrs.get("dcmIncorrectModalityWorklistEntryCode"),
                        null)));
        arcdev.setDataRetentionPeriodExpiredCode(new Code(LdapUtils
                .stringValue(attrs.get("dcmDataRetentionPeriodExpiredCode"),
                        null)));
        arcdev.setFuzzyAlgorithmClass(LdapUtils.stringValue(
                attrs.get("dcmFuzzyAlgorithmClass"), null));
        arcdev.setConfigurationStaleTimeout(LdapUtils.intValue(
                attrs.get("dcmConfigurationStaleTimeout"), 0));
        arcdev.setWadoAttributesStaleTimeout(LdapUtils.intValue(
                attrs.get("dcmWadoAttributesStaleTimeout"), 0));
        arcdev.setHostnameAEresoultion(LdapUtils.booleanValue(attrs.get("dcmHostNameAEResolution"), true));
    }

    @Override
    protected void loadChilds(Device device, String deviceDN)
            throws NamingException, ConfigurationException {
        ArchiveDeviceExtension arcdev = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (arcdev == null)
            return;

        loadAttributeFilters(arcdev, deviceDN);
        loadWebClientMappings(arcdev, deviceDN);
    }

    private void loadAttributeFilters(ArchiveDeviceExtension device,
            String deviceDN) throws NamingException {
        NamingEnumeration<SearchResult> ne = config.search(deviceDN,
                "(objectclass=dcmAttributeFilter)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                AttributeFilter filter = new AttributeFilter(
                        tags(attrs.get("dcmTag")));
                filter.setCustomAttribute1(valueSelector(attrs
                        .get("dcmCustomAttribute1")));
                filter.setCustomAttribute2(valueSelector(attrs
                        .get("dcmCustomAttribute2")));
                filter.setCustomAttribute3(valueSelector(attrs
                        .get("dcmCustomAttribute3")));
                device.setAttributeFilter(
                        Entity.valueOf(LdapUtils.stringValue(
                                attrs.get("dcmEntity"), null)), filter);
            }
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

    private void loadWebClientMappings(ArchiveDeviceExtension device,
            String deviceDN) throws NamingException {
        NamingEnumeration<SearchResult> map = config.search(LdapUtils.dnOf("cn",
                ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE, deviceDN),
                "(objectclass=dcmHostNameAEEntry)");
        try {
            ArrayList<HostNameAEEntry> tmpMap = new ArrayList<HostNameAEEntry>();
            while (map.hasMore()) {
                SearchResult entry = map.next();
                Attributes entryAttrs = entry.getAttributes();
                tmpMap.add(new HostNameAEEntry((String) entryAttrs.get(
                        "dicomHostname").get(), (String) entryAttrs.get(
                        "dicomAETitle").get()));
            }
            device.setHostNameAEList(tmpMap);
        } finally {
            LdapUtils.safeClose(map);
        }
    }

    private static ValueSelector valueSelector(Attribute attr)
            throws NamingException {
        return attr != null ? ValueSelector.valueOf((String) attr.get()) : null;
    }

    private static AttributesFormat attributesFormat(Attribute attr)
            throws NamingException {
        return attr != null ? new AttributesFormat((String) attr.get()) : null;
    }

    protected static int[] tags(Attribute attr) throws NamingException {
        int[] is = new int[attr.size()];
        for (int i = 0; i < is.length; i++)
            is[i] = Integer.parseInt((String) attr.get(i), 16);

        return is;
    }

    @Override
    protected void loadFrom(ApplicationEntity ae, Attributes attrs)
            throws NamingException {
        if (!LdapUtils.hasObjectClass(attrs, "dcmArchiveNetworkAE"))
            return;

        ArchiveAEExtension arcae = new ArchiveAEExtension();
        ae.addAEExtension(arcae);

        try {
            ConfigReader ldapReader = new LdapConfigReader(attrs);
            ReflectiveConfig.read(arcae, ldapReader);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

    @Override
    protected void loadChilds(ApplicationEntity ae, String aeDN)
            throws NamingException {
        ArchiveAEExtension arcae = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcae == null)
            return;

        config.load(arcae.getAttributeCoercions(), aeDN);
        new LdapCompressionRulesConfiguration(config).load(
                arcae.getCompressionRules(), aeDN);

    }

    @Override
    protected void storeDiffs(Device a, Device b, List<ModificationItem> mods) {
        ArchiveDeviceExtension aa = a
                .getDeviceExtension(ArchiveDeviceExtension.class);
        ArchiveDeviceExtension bb = b
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (aa == null || bb == null)
            return;

        LdapUtils.storeDiff(mods, "dcmIncorrectWorklistEntrySelectedCode",
                aa.getIncorrectWorklistEntrySelectedCode(),
                bb.getIncorrectWorklistEntrySelectedCode());
        LdapUtils.storeDiff(mods, "dcmRejectedForQualityReasonsCode",
                aa.getRejectedForQualityReasonsCode(),
                bb.getRejectedForQualityReasonsCode());
        LdapUtils.storeDiff(mods, "dcmRejectedForPatientSafetyReasonsCode",
                aa.getRejectedForPatientSafetyReasonsCode(),
                bb.getRejectedForPatientSafetyReasonsCode());
        LdapUtils.storeDiff(mods, "dcmIncorrectModalityWorklistEntryCode",
                aa.getIncorrectModalityWorklistEntryCode(),
                bb.getIncorrectModalityWorklistEntryCode());
        LdapUtils.storeDiff(mods, "dcmDataRetentionPeriodExpiredCode",
                aa.getDataRetentionPeriodExpiredCode(),
                bb.getDataRetentionPeriodExpiredCode());
        LdapUtils.storeDiff(mods, "dcmFuzzyAlgorithmClass",
                aa.getFuzzyAlgorithmClass(), bb.getFuzzyAlgorithmClass());
        LdapUtils.storeDiff(mods, "dcmConfigurationStaleTimeout",
                aa.getConfigurationStaleTimeout(),
                bb.getConfigurationStaleTimeout(), 0);
        LdapUtils.storeDiff(mods, "dcmWadoAttributesStaleTimeout",
                aa.getWadoAttributesStaleTimeout(),
                bb.getWadoAttributesStaleTimeout(), 0);
        LdapUtils.storeDiff(mods, "dcmHostNameAEResolution",
                aa.isHostnameAEresoultion(),
                bb.isHostnameAEresoultion());
    }

    @Override
    protected void storeDiffs(ApplicationEntity a, ApplicationEntity b,
            List<ModificationItem> mods) {
        ArchiveAEExtension aa = a.getAEExtension(ArchiveAEExtension.class);
        ArchiveAEExtension bb = b.getAEExtension(ArchiveAEExtension.class);
        if (aa == null || bb == null)
            return;

        try {

            DiffWriter ldapDiffWriter = new LdapDiffWriter(mods);
            ReflectiveConfig.storeAllDiffs(a, b, ldapDiffWriter);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }

    }

    @Override
    protected void mergeChilds(Device prev, Device device, String deviceDN)
            throws NamingException {
        ArchiveDeviceExtension aa = prev
                .getDeviceExtension(ArchiveDeviceExtension.class);
        ArchiveDeviceExtension bb = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (aa == null || bb == null)
            return;

        for (Entity entity : Entity.values())
            config.modifyAttributes(
                    LdapUtils.dnOf("dcmEntity", entity.toString(), deviceDN),
                    storeDiffs(aa.getAttributeFilter(entity),
                            bb.getAttributeFilter(entity),
                            new ArrayList<ModificationItem>()));
        
        for (HostNameAEEntry entry : aa.getHostNameAEList()) {
            for(HostNameAEEntry entryNew: bb.getHostNameAEList())
            config.modifyAttributes(LdapUtils.dnOf("dicomHostname",entry.getHostName(),LdapUtils.dnOf("cn",
                    ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE, deviceDN).toString()), storeDiffs(entry, entryNew, new ArrayList<ModificationItem>()));
        }
    }

    @Override
    protected void mergeChilds(ApplicationEntity prev, ApplicationEntity ae,
            String aeDN) throws NamingException {
        ArchiveAEExtension aa = prev.getAEExtension(ArchiveAEExtension.class);
        ArchiveAEExtension bb = ae.getAEExtension(ArchiveAEExtension.class);
        if (aa == null || bb == null)
            return;

        config.merge(aa.getAttributeCoercions(), bb.getAttributeCoercions(),
                aeDN);
        new LdapCompressionRulesConfiguration(config).merge(
                aa.getCompressionRules(), bb.getCompressionRules(), aeDN);
    }

    private List<ModificationItem> storeDiffs(AttributeFilter prev,
            AttributeFilter filter, List<ModificationItem> mods) {
        storeDiffTags(mods, "dcmTag", prev.getSelection(),
                filter.getSelection());
        LdapUtils.storeDiff(mods, "dcmCustomAttribute1",
                prev.getCustomAttribute1(), filter.getCustomAttribute1());
        LdapUtils.storeDiff(mods, "dcmCustomAttribute2",
                prev.getCustomAttribute2(), filter.getCustomAttribute2());
        LdapUtils.storeDiff(mods, "dcmCustomAttribute3",
                prev.getCustomAttribute3(), filter.getCustomAttribute3());
        return mods;
    }

    private List<ModificationItem> storeDiffs(HostNameAEEntry prev,
            HostNameAEEntry current, List<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dicomAETitle", prev.getAeTitle(),
                current.getAeTitle());
        LdapUtils.storeDiff(mods, "dicomHostname", prev.getHostName(),
                current.getHostName());
        return mods;
    }

    private void storeDiffTags(List<ModificationItem> mods, String attrId,
            int[] prevs, int[] vals) {
        if (!Arrays.equals(prevs, vals))
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    tagsAttr(attrId, vals)));
    }

}
