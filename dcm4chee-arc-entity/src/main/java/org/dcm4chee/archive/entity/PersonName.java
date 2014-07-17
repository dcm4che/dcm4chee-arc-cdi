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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4chee.archive.entity;

import static org.dcm4che3.data.PersonName.Group;
import static org.dcm4che3.data.PersonName.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.dcm4che3.soundex.FuzzyStr;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Entity
@Table(name = "person_name")
public class PersonName {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Column(name = "family_name")
    private String familyName;

    @Column(name = "given_name")
    private String givenName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "suffix")
    private String suffix;

    @Column(name = "i_family_name")
    private String ideographicFamilyName;

    @Column(name = "i_given_name")
    private String ideographicGivenName;

    @Column(name = "i_middle_name")
    private String ideographicMiddleName;

    @Column(name = "i_prefix")
    private String ideographicPrefix;

    @Column(name = "i_suffix")
    private String ideographicSuffix;

    @Column(name = "p_family_name")
    private String phoneticFamilyName;

    @Column(name = "p_given_name")
    private String phoneticGivenName;

    @Column(name = "p_middle_name")
    private String phoneticMiddleName;

    @Column(name = "p_prefix")
    private String phoneticPrefix;

    @Column(name = "p_suffix")
    private String phoneticSuffix;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "person_name_fk", referencedColumnName = "pk")
    private Collection<SoundexCode> soundexCodes;

    public PersonName() {
    }

    public PersonName(org.dcm4che3.data.PersonName pn, FuzzyStr fuzzyStr) {
        familyName = pn.get(Group.Alphabetic, Component.FamilyName);
        givenName = pn.get(Group.Alphabetic, Component.GivenName);
        middleName = pn.get(Group.Alphabetic, Component.MiddleName);
        prefix = pn.get(Group.Alphabetic, Component.NamePrefix);
        suffix = pn.get(Group.Alphabetic, Component.NameSuffix);
        ideographicFamilyName = pn.get(Group.Ideographic, Component.FamilyName);
        ideographicGivenName = pn.get(Group.Ideographic, Component.GivenName);
        ideographicMiddleName = pn.get(Group.Ideographic, Component.MiddleName);
        ideographicPrefix = pn.get(Group.Ideographic, Component.NamePrefix);
        ideographicSuffix = pn.get(Group.Ideographic, Component.NameSuffix);
        phoneticFamilyName = pn.get(Group.Phonetic, Component.FamilyName);
        phoneticGivenName = pn.get(Group.Phonetic, Component.GivenName);
        phoneticMiddleName = pn.get(Group.Phonetic, Component.MiddleName);
        phoneticPrefix = pn.get(Group.Phonetic, Component.NamePrefix);
        phoneticSuffix = pn.get(Group.Phonetic, Component.NameSuffix);
        soundexCodes = createSoundexCodes(familyName, givenName, middleName,
                fuzzyStr);
    }

    public static PersonName valueOf(String s, FuzzyStr fuzzyStr) {
        if (s == null)
            return null;

        org.dcm4che3.data.PersonName pn = new org.dcm4che3.data.PersonName(s, true);
        if (pn.isEmpty())
            return null;

        return new PersonName(pn, fuzzyStr);
    }

    private Collection<SoundexCode> createSoundexCodes(String familyName,
            String givenName, String middleName, FuzzyStr fuzzyStr) {
        Collection<SoundexCode> codes = new ArrayList<SoundexCode>();
        addSoundexCodesTo(Component.FamilyName, familyName, fuzzyStr, codes);
        addSoundexCodesTo(Component.GivenName, givenName, fuzzyStr, codes);
        addSoundexCodesTo(Component.MiddleName, middleName, fuzzyStr, codes);
        return codes;
   }

    private void addSoundexCodesTo(Component component, String name,
            FuzzyStr fuzzyStr, Collection<SoundexCode> codes) {
        if (name == null)
            return;

        Iterator<String> parts = tokenizePersonNameComponent(name);
        for (int i = 0; parts.hasNext(); i++)
            codes.add(new SoundexCode(component, i,
                    fuzzyStr.toFuzzy(parts.next())));
    }

    public static Iterator<String> tokenizePersonNameComponent(String name) {
        final StringTokenizer stk = new StringTokenizer(name, " ,-\t");
        return new Iterator<String>() {

            @Override
            public boolean hasNext() {
                return stk.hasMoreTokens();
            }

            @Override
            public String next() {
                return stk.nextToken();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }};
    }

    public long getPk() {
        return pk;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getIdeographicFamilyName() {
        return ideographicFamilyName;
    }

    public void setIdeographicFamilyName(String ideographicFamilyName) {
        this.ideographicFamilyName = ideographicFamilyName;
    }

    public String getIdeographicGivenName() {
        return ideographicGivenName;
    }

    public void setIdeographicGivenName(String ideographicGivenName) {
        this.ideographicGivenName = ideographicGivenName;
    }

    public String getIdeographicMiddleName() {
        return ideographicMiddleName;
    }

    public void setIdeographicMiddleName(String ideographicMiddleName) {
        this.ideographicMiddleName = ideographicMiddleName;
    }

    public String getIdeographicPrefix() {
        return ideographicPrefix;
    }

    public void setIdeographicPrefix(String ideographicPrefix) {
        this.ideographicPrefix = ideographicPrefix;
    }

    public String getIdeographicSuffix() {
        return ideographicSuffix;
    }

    public void setIdeographicSuffix(String ideographicSuffix) {
        this.ideographicSuffix = ideographicSuffix;
    }

    public String getPhoneticFamilyName() {
        return phoneticFamilyName;
    }

    public void setPhoneticFamilyName(String phoneticFamilyName) {
        this.phoneticFamilyName = phoneticFamilyName;
    }

    public String getPhoneticGivenName() {
        return phoneticGivenName;
    }

    public void setPhoneticGivenName(String phoneticGivenName) {
        this.phoneticGivenName = phoneticGivenName;
    }

    public String getPhoneticMiddleName() {
        return phoneticMiddleName;
    }

    public void setPhoneticMiddleName(String phoneticMiddleName) {
        this.phoneticMiddleName = phoneticMiddleName;
    }

    public String getPhoneticPrefix() {
        return phoneticPrefix;
    }

    public void setPhoneticPrefix(String phoneticPrefix) {
        this.phoneticPrefix = phoneticPrefix;
    }

    public String getPhoneticSuffix() {
        return phoneticSuffix;
    }

    public void setPhoneticSuffix(String phoneticSuffix) {
        this.phoneticSuffix = phoneticSuffix;
    }
}
