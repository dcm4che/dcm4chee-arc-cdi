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

package org.dcm4chee.archive.query.util;

import java.util.Iterator;

import org.dcm4che3.data.PersonName;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.QPersonName;
import org.dcm4chee.archive.entity.QSoundexCode;
import org.dcm4chee.archive.entity.SoundexCode;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.hibernate.HibernateSubQuery;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.StringPath;

/**
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
class MatchPersonName {

    public static Predicate match(QPersonName qpn, String value,
            QueryParam queryParam, String nullValue) {

        if (value == null || value.equals(nullValue))
            return null;

        PersonName pn = new PersonName(value, true);
        Predicate predicate = queryParam.isFuzzySemanticMatching()
                        ? fuzzyMatch(qpn, pn, queryParam)
                        : literalMatch(qpn, pn, queryParam);

        if (queryParam.isMatchUnknown())
            predicate = ExpressionUtils.or(predicate, qpn.isNull());

        return predicate;
    }

    private static Predicate literalMatch(QPersonName qpn,
            PersonName pn, QueryParam param) {
         String nullValue = param.getNullValueForQueryFields();
         BooleanBuilder builder = new BooleanBuilder();
         if (!pn.contains(PersonName.Group.Ideographic)
                && !pn.contains(PersonName.Group.Phonetic)) {
             builder.or(match(
                     qpn.familyName,
                     qpn.givenName,
                     qpn.middleName, 
                     pn, PersonName.Group.Alphabetic, true, nullValue));
             builder.or(match(
                     qpn.ideographicFamilyName,
                     qpn.ideographicGivenName,
                     qpn.ideographicMiddleName,
                     pn, PersonName.Group.Alphabetic, false, nullValue));
             builder.or(match(
                     qpn.phoneticFamilyName,
                     qpn.phoneticGivenName,
                     qpn.phoneticMiddleName,
                     pn, PersonName.Group.Alphabetic, false, nullValue));
        } else {
            builder.and(match(
                    qpn.familyName,
                    qpn.givenName,
                    qpn.middleName, 
                    pn, PersonName.Group.Alphabetic, true, nullValue));
            builder.and(match(
                    qpn.ideographicFamilyName,
                    qpn.ideographicGivenName,
                    qpn.ideographicMiddleName,
                    pn, PersonName.Group.Ideographic, false, nullValue));
            builder.and(match(
                    qpn.phoneticFamilyName,
                    qpn.phoneticGivenName,
                    qpn.phoneticMiddleName,
                    pn, PersonName.Group.Phonetic, false, nullValue));
        }
        return builder;
    }

    private static Predicate match(StringPath familyName,
            StringPath givenName, StringPath middleName,
            PersonName pn, PersonName.Group group, boolean ignoreCase, String nullValue) {
        if (!pn.contains(group))
            return null;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QueryBuilder.wildCard(familyName,
                pn.get(group, PersonName.Component.FamilyName), false, ignoreCase, nullValue));
        builder.and(QueryBuilder.wildCard(givenName,
                pn.get(group, PersonName.Component.GivenName), false, ignoreCase, nullValue));
        builder.and(QueryBuilder.wildCard(middleName,
                pn.get(group, PersonName.Component.MiddleName), false, ignoreCase, nullValue));
        return builder;
    }

    private static Predicate fuzzyMatch(QPersonName qpn,
            PersonName pn, QueryParam param) {
        BooleanBuilder builder = new BooleanBuilder();
        fuzzyMatch(qpn, pn, PersonName.Component.FamilyName, param, builder);
        fuzzyMatch(qpn, pn, PersonName.Component.GivenName, param, builder);
        fuzzyMatch(qpn, pn, PersonName.Component.MiddleName, param, builder);
        return builder;
    }

    private static void fuzzyMatch(QPersonName qpn, PersonName pn,
            PersonName.Component c, QueryParam param, BooleanBuilder builder) {
        String name = StringUtils.maskNull(pn.get(c), "*");
        if (name.equals("*"))
            return;

        Iterator<String> parts = SoundexCode
                .tokenizePersonNameComponent(name);
        for (int i = 0; parts.hasNext(); ++i)
            fuzzyMatch(qpn, c, i, parts.next(), param, builder);
    }

    private static void fuzzyMatch(QPersonName qpn, PersonName.Component c,
            int partIndex, String name, QueryParam param, BooleanBuilder builder) {
        boolean wc = name.endsWith("*");
        if (wc) {
            name = name.substring(0, name.length()-1);
            if (name.isEmpty())
                return;
        }
        FuzzyStr fuzzyStr = param.getFuzzyStr();
        String fuzzyName = fuzzyStr.toFuzzy(name);
        if (fuzzyName.isEmpty())
            if (wc)
                return;
            else // code "" is stored as "*"
                fuzzyName = "*";

        Predicate pred = wc 
                ? QSoundexCode.soundexCode.codeValue.startsWith(fuzzyName)
                : QSoundexCode.soundexCode.codeValue.eq(fuzzyName);
        if (!param.isPersonNameComponentOrderInsensitiveMatching()) {
            pred = ExpressionUtils.allOf(pred,
                    QSoundexCode.soundexCode.personNameComponent.eq(c),
                    QSoundexCode.soundexCode.componentPartIndex.eq(partIndex));
        }
        HibernateSubQuery subquery = new HibernateSubQuery()
                        .from(QSoundexCode.soundexCode)
                        .where(qpn.eq(QSoundexCode.soundexCode.personName),
                                pred);
        builder.and(subquery.exists());
    }

}
