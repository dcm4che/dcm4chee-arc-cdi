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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ConfigurableClass
public class ArchivingRules implements Iterable<ArchivingRule>, Serializable{

    private static final long serialVersionUID = 7012295456106319169L;

    @LDAP(noContainerNode = true)
    @ConfigurableProperty
    private List<ArchivingRule> list = new ArrayList<ArchivingRule>();

    public void add(ArchivingRule rule) {
        if (findByCommonName(rule.getCommonName()) != null)
            throw new IllegalStateException("ArchivingRule with cn: '"
                    + rule.getCommonName() + "' already exists");
        list.add(rule);
    }

    public List<ArchivingRule> getList() {
        return list;
    }

    public void setList(List<ArchivingRule> list) {
        this.list.clear();
        for (ArchivingRule rule : list)
            add(rule);
    }

    public void add(ArchivingRules rules) {
         for (ArchivingRule rule : rules)
             add(rule);
    }

    public boolean remove(ArchivingRule ac) {
        return list.remove(ac);
    }

    public void clear() {
        list.clear();
    }

    public ArchivingRule findByCommonName(String commonName) {
        for (ArchivingRule rule : list)
            if (commonName.equals(rule.getCommonName()))
                return rule;
        return null;
    }

    public List<ArchivingRule> findArchivingRule(String deviceName, String aeTitle,
            Attributes attrs) {
        return findArchivingRule(deviceName, aeTitle,
                attrs.getString(Tag.InstitutionName),
                attrs.getString(Tag.InstitutionalDepartmentName),
                attrs.getString(Tag.Modality));
    }

    public List<ArchivingRule> findArchivingRule(String deviceName,
            String aeTitle, String institutionName, String institutionalDepartmentName,
            String modality) {
        List<ArchivingRule> matchingRules = new ArrayList<ArchivingRule>();
        for (ArchivingRule rule : list)
            if (rule.matchesCondition(deviceName, aeTitle, institutionName,
                    institutionalDepartmentName, modality))
                matchingRules.add(rule);
        return matchingRules;
    }

    @Override
    public Iterator<ArchivingRule> iterator() {
        return list.iterator();
    }
}
