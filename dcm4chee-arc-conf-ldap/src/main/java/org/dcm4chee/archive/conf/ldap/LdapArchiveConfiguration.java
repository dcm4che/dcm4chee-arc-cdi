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
import java.util.Collection;
import java.util.List;

import javax.naming.NameNotFoundException;
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
import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.conf.ldap.generic.LdapConfigIO;
import org.dcm4che3.conf.ldap.generic.LdapConfigWriter;
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
import org.dcm4chee.archive.conf.RejectionParam;
import org.dcm4chee.archive.conf.StoreAction;

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
        LdapUtils.storeNotNull(attrs, "dcmFuzzyAlgorithmClass",
                arcDev.getFuzzyAlgorithmClass());
        LdapUtils.storeNotDef(attrs, "dcmConfigurationStaleTimeout",
                arcDev.getConfigurationStaleTimeout(), 0);
        LdapUtils.storeNotDef(attrs, "dcmWadoAttributesStaleTimeout",
                arcDev.getWadoAttributesStaleTimeout(), 0);
        LdapUtils.storeNotDef(attrs, "dcmHostNameAEResolution",
                arcDev.isHostnameAEresoultion(), false);
        LdapUtils.storeNotDef(attrs, "dcmDeIdentifyLogs",
                arcDev.isDeIdentifyLogs(), false);
    }

    @Override
    protected void storeChilds(String deviceDN, Device device)
            throws NamingException {
        ArchiveDeviceExtension arcDev = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        if (arcDev == null)
            return;
        
        storeAttributeFilter(deviceDN, arcDev);
        storeHostNameAEList(deviceDN, arcDev);
        storeRejectionParams(deviceDN, arcDev);
    }

    private void storeAttributeFilter(String deviceDN,
            ArchiveDeviceExtension arcDev) throws NamingException {
        for (Entity entity : Entity.values())
            config.createSubcontext(
                    LdapUtils.dnOf("dcmEntity", entity.toString(), deviceDN),
                    storeTo(arcDev.getAttributeFilter(entity), entity,
                            new BasicAttributes(true)));
    }

    private void storeHostNameAEList(String deviceDN,
            ArchiveDeviceExtension arcDev) throws NamingException {
        String containerDN = LdapUtils.dnOf("cn",
                ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE, deviceDN);
        config.createSubcontext(containerDN,
                LdapUtils.attrs("dcmHostNameAEMap", "cn",
                        ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE));
        for (HostNameAEEntry entry : arcDev.getHostNameAEList()) {
            config.createSubcontext(
                LdapUtils.dnOf("dicomHostname", entry.getHostName(), containerDN),
                storeTo(entry, new BasicAttributes(true)));
        }
    }

    private Attributes storeTo(HostNameAEEntry entry, BasicAttributes attrs) {
        attrs.put("objectclass", "dcmHostNameAEEntry");
        attrs.put("dicomHostname", entry.getHostName());
        attrs.put("dicomAETitle", entry.getAeTitle());
        return attrs;
    }

    private void storeRejectionParams(String deviceDN,
            ArchiveDeviceExtension arcDev) throws NamingException {
        for (RejectionParam rejectionType : arcDev.getRejectionParams()) {
            Code rejectionCode = rejectionType.getRejectionNoteTitle();
            String cn = rejectionCode.getCodeMeaning();
            config.createSubcontext(
                    LdapUtils.dnOf("cn", cn, deviceDN),
                    storeTo(rejectionType, new BasicAttributes(true)));
        }
    }

    private Attributes storeTo(RejectionParam rejectionParam,
            BasicAttributes attrs) {
        Code rejectionCode = rejectionParam.getRejectionNoteTitle();
        String cn = rejectionCode.getCodeMeaning();
        attrs.put("objectclass", "dcmRejectionNote");
        attrs.put("cn", cn);
        attrs.put("dcmRejectionNoteTitle", rejectionCode.toString());
        LdapUtils.storeNotDef(attrs, "dcmRevokeRejection",
                rejectionParam.isRevokeRejection(), false);
        LdapUtils.storeNotNull(attrs, "dcmAcceptPreviousRejectedInstance",
                rejectionParam.getAcceptPreviousRejectedInstance());
        LdapUtils.storeNotEmpty(attrs, "dcmOverwritePreviousRejection",
                rejectionParam.getOverwritePreviousRejection());
        return attrs;
    }

    @Override
    protected void storeChilds(String aeDN, ApplicationEntity ae)
            throws NamingException {
        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcAE == null)
            return;

        
        // use modified reflective writer to store any child nodes, but not the attributes on AE level
        try {
            ConfigWriter writer = new LdapConfigIO(new BasicAttributes(), aeDN ,config) {
                @Override
                public void flushWriter() throws ConfigurationException {
                    //noop since we stored the attributes on AE level in storeTo already 
                }
            };
            ReflectiveConfig rc = new ReflectiveConfig(null, config);
            rc.storeConfig(arcAE, writer);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        
        
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
        arcdev.setFuzzyAlgorithmClass(LdapUtils.stringValue(
                attrs.get("dcmFuzzyAlgorithmClass"), null));
        arcdev.setConfigurationStaleTimeout(LdapUtils.intValue(
                attrs.get("dcmConfigurationStaleTimeout"), 0));
        arcdev.setWadoAttributesStaleTimeout(LdapUtils.intValue(
                attrs.get("dcmWadoAttributesStaleTimeout"), 0));
        arcdev.setHostnameAEresoultion(LdapUtils.booleanValue(attrs.get("dcmHostNameAEResolution"), false));
        arcdev.setDeIdentifyLogs(LdapUtils.booleanValue(attrs.get("dcmDeIdentifyLogs"), false));

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
        loadRejectionParams(arcdev, deviceDN);
    }

    private void loadRejectionParams(ArchiveDeviceExtension arcdev,
            String deviceDN) throws NamingException {
        ArrayList<RejectionParam> list = new ArrayList<RejectionParam>();
        NamingEnumeration<SearchResult> ne = config.search(deviceDN,
                "(objectclass=dcmRejectionNote)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                RejectionParam param = new RejectionParam();
                param.setRejectionNoteTitle(new Code(LdapUtils
                        .stringValue(attrs.get("dcmRejectionNoteTitle"),
                        null)));
                param.setRevokeRejection(LdapUtils
                        .booleanValue(attrs.get("dcmRevokeRejection"),
                        false));
                param.setAcceptPreviousRejectedInstance(
                        storeActionOf(attrs.get("dcmAcceptPreviousRejectedInstance")));
                param.setOverwritePreviousRejection(LdapUtils.codeArray(
                        attrs.get("dcmOverwritePreviousRejection")));
                list.add(param);
            }
        } finally {
            LdapUtils.safeClose(ne);
        }
        arcdev.setRejectionParams(list.toArray(new RejectionParam[list.size()]));
    }

    protected StoreAction storeActionOf(Attribute attr)
            throws NamingException {
        return attr != null ? StoreAction.valueOf((String) attr.get()) : null;
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
        NamingEnumeration<SearchResult> map = null;
        String containerDN = LdapUtils.dnOf(
                "cn", ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE, deviceDN);
        try {
            config.getAttributes(containerDN);
        } catch (NameNotFoundException e)  {
            return;
        }
        try {
            map = config.search(containerDN, "(objectclass=dcmHostNameAEEntry)");
            ArrayList<HostNameAEEntry> tmpMap = new ArrayList<HostNameAEEntry>();
            while (map.hasMore()) {
                SearchResult entry = map.next();
                Attributes entryAttrs = entry.getAttributes();
                tmpMap.add(new HostNameAEEntry(
                        (String) entryAttrs.get("dicomHostname").get(),
                        (String) entryAttrs.get("dicomAETitle").get()));
            }
            device.setHostNameAEList(tmpMap);
        }
        finally {
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

    }

    @Override
    protected void loadChilds(ApplicationEntity ae, String aeDN)
            throws NamingException {
        ArchiveAEExtension arcae = ae.getAEExtension(ArchiveAEExtension.class);
        if (arcae == null)
            return;

        // use reflective config to read the AE extension
        try {
            ConfigReader ldapReader = new LdapConfigIO(config.getAttributes(aeDN),aeDN,config);
            ReflectiveConfig rc = new ReflectiveConfig(null, config);
            rc.readConfig(arcae, ldapReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
                bb.isHostnameAEresoultion(), false);
        LdapUtils.storeDiff(mods, "dcmDeIdentifyLogs",
                aa.isDeIdentifyLogs(),
                bb.isDeIdentifyLogs(), false);
    }

    @Override
    protected void storeDiffs(ApplicationEntity a, ApplicationEntity b,
            List<ModificationItem> mods) {
        ArchiveAEExtension aa = a.getAEExtension(ArchiveAEExtension.class);
        ArchiveAEExtension bb = b.getAEExtension(ArchiveAEExtension.class);
        if (aa == null || bb == null)
            return;

        // store AE level attributes with reflective config
        try {
            LdapConfigWriter ldapDiffWriter = new LdapConfigWriter(mods);
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

        mergeHostNameAEList(aa, bb, deviceDN);
        mergeRejectionParams(aa, bb, deviceDN);
    }

    private void mergeHostNameAEList(ArchiveDeviceExtension prev,
            ArchiveDeviceExtension arcDev, String deviceDN)
            throws NamingException {
        String contatinerDN = LdapUtils.dnOf("cn",
                ArchiveDeviceExtension.ARCHIVE_HOST_AE_MAP_NODE, deviceDN);
        for (HostNameAEEntry entry : prev.getHostNameAEList()) {
            String hostName = entry.getHostName();
            if (findWithEqualHostname(arcDev.getHostNameAEList(), hostName) == null)
                config.destroySubcontext(
                        LdapUtils.dnOf("dicomHostname", hostName, contatinerDN));
        }
        for (HostNameAEEntry entryNew : arcDev.getHostNameAEList()) {
            String hostName = entryNew.getHostName();
            String dn = LdapUtils.dnOf("dicomHostname", hostName, contatinerDN);
            HostNameAEEntry entryOld = findWithEqualHostname(
                    prev.getHostNameAEList(), hostName);
            if (entryOld == null) {
                config.createSubcontext(dn,
                        storeTo(entryNew, new BasicAttributes(true)));
            } else{
                config.modifyAttributes(dn, 
                        storeDiffs(entryOld, entryNew, new ArrayList<ModificationItem>()));
            }
        }
    }

    private HostNameAEEntry findWithEqualHostname(
            Collection<HostNameAEEntry> from, String hostName) {
        for(HostNameAEEntry e: from)
            if(e.getHostName().equalsIgnoreCase(hostName))
                return e;
        return null;
    }

    private void mergeRejectionParams(ArchiveDeviceExtension prev,
            ArchiveDeviceExtension arcDev, String deviceDN) throws NamingException {
        for (RejectionParam entry : prev.getRejectionParams()) {
            Code code = entry.getRejectionNoteTitle();
            String meaning = code.getCodeMeaning();
            if (findWithCodeMeaning(arcDev.getRejectionParams(), meaning) == null)
                config.destroySubcontext(
                        LdapUtils.dnOf("cn", meaning, deviceDN));
        }
        for (RejectionParam entryNew : arcDev.getRejectionParams()) {
            Code code = entryNew.getRejectionNoteTitle();
            String meaning = code.getCodeMeaning();
            String dn = LdapUtils.dnOf("cn", meaning, deviceDN);
            RejectionParam entryOld = findWithCodeMeaning(
                    prev.getRejectionParams(), meaning);
            if (entryOld == null) {
                config.createSubcontext(dn,
                        storeTo(entryNew, new BasicAttributes(true)));
            } else{
                config.modifyAttributes(dn, 
                        storeDiffs(entryOld, entryNew,
                                new ArrayList<ModificationItem>()));
            }
        }
    }

    private List<ModificationItem> storeDiffs(RejectionParam prev,
            RejectionParam rejectionType, ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dcmRejectionNoteTitle",
                prev.getRejectionNoteTitle(),
                rejectionType.getRejectionNoteTitle());
        LdapUtils.storeDiff(mods, "dcmRevokeRejection",
                prev.isRevokeRejection(),
                rejectionType.isRevokeRejection(),
                false);
        LdapUtils.storeDiff(mods, "dcmAcceptPreviousRejectedInstance",
                prev.getAcceptPreviousRejectedInstance(),
                rejectionType.getAcceptPreviousRejectedInstance());
        LdapUtils.storeDiff(mods, "dcmOverwritePreviousRejection",
                prev.getOverwritePreviousRejection(),
                rejectionType.getOverwritePreviousRejection());
        return null;
    }

    private RejectionParam findWithCodeMeaning(RejectionParam[] rejectionTypes,
            String meaning) {
        for (RejectionParam type : rejectionTypes) {
            if (type.getRejectionNoteTitle().getCodeMeaning().equals(meaning))
                return type;
        }
        return null;
    }

    @Override
    protected void mergeChilds(ApplicationEntity prev, ApplicationEntity ae,
            String aeDN) throws NamingException {
        ArchiveAEExtension aa = prev.getAEExtension(ArchiveAEExtension.class);
        ArchiveAEExtension bb = ae.getAEExtension(ArchiveAEExtension.class);
        if (aa == null || bb == null)
            return;

        // use modified reflective diffwriter to store any child nodes, but not the attributes on AE level
        try {
            ConfigWriter diffWriter = new LdapConfigIO(new ArrayList<ModificationItem>(), aeDN ,config) {
                @Override
                public void flushDiffs() throws ConfigurationException {
                    //noop since we diffed the attributes on AE level in storeDiffs already 
                }
            };
            ReflectiveConfig rc = new ReflectiveConfig(null, config);
            rc.storeConfigDiffs(aa, bb, diffWriter);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }

        
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
