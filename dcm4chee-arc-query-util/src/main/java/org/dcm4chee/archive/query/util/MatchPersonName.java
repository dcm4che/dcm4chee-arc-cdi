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


    public static Predicate match(QPersonName qpn, String value, QueryParam queryParam) {

        if (value.equals("*"))
            return null;

        PersonName pn = new PersonName(value);
 
        return matchUnknown(
                    queryParam.isFuzzySemanticMatching()
                        ? fuzzyMatch(qpn, pn, queryParam)
                        : literalMatch(qpn, pn, queryParam),
                    qpn,
                    queryParam.isMatchUnknown());
    }

    private static Predicate matchUnknown(Predicate predicate, QPersonName qpn,
            boolean matchUnknown) {
        return matchUnknown
                ? ExpressionUtils.or(predicate, qpn.noPersonName.isTrue())
                : predicate;
    }

    private static Predicate literalMatch(QPersonName qpn,
            PersonName pn, QueryParam param) {
         BooleanBuilder builder = new BooleanBuilder();
         if (!pn.contains(PersonName.Group.Ideographic)
                && !pn.contains(PersonName.Group.Phonetic)) {
             builder.or(match(
                     qpn.familyName,
                     qpn.givenName,
                     qpn.middleName, 
                     pn, PersonName.Group.Alphabetic));
             builder.or(match(
                     qpn.ideographicFamilyName,
                     qpn.ideographicGivenName,
                     qpn.ideographicMiddleName,
                     pn, PersonName.Group.Alphabetic));
             builder.or(match(
                     qpn.phoneticFamilyName,
                     qpn.phoneticGivenName,
                     qpn.phoneticMiddleName,
                     pn, PersonName.Group.Alphabetic));
        } else {
            builder.and(match(
                    qpn.familyName,
                    qpn.givenName,
                    qpn.middleName, 
                    pn, PersonName.Group.Alphabetic));
            builder.and(match(
                    qpn.ideographicFamilyName,
                    qpn.ideographicGivenName,
                    qpn.ideographicMiddleName,
                    pn, PersonName.Group.Ideographic));
            builder.and(match(
                    qpn.phoneticFamilyName,
                    qpn.phoneticGivenName,
                    qpn.phoneticMiddleName,
                    pn, PersonName.Group.Phonetic));
        }
        return builder;
    }

    private static Predicate match(StringPath familyName,
            StringPath givenName, StringPath middleName,
            PersonName pn, PersonName.Group group) {
        if (!pn.contains(group))
            return null;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QueryBuilder.wildCard(familyName,
                pn.get(group, PersonName.Component.FamilyName), false, true));
        builder.and(QueryBuilder.wildCard(givenName,
                pn.get(group, PersonName.Component.GivenName), false, true));
        builder.and(QueryBuilder.wildCard(middleName,
                pn.get(group, PersonName.Component.MiddleName), false, true));
        return builder;
    }

    private static Predicate fuzzyMatch(QPersonName qpn,
            PersonName pn, QueryParam param) {
        FuzzyStr fuzzyStr = param.getFuzzyStr();
        BooleanBuilder builder = new BooleanBuilder();
        fuzzyMatch(qpn, pn, PersonName.Component.FamilyName, fuzzyStr, builder);
        fuzzyMatch(qpn, pn, PersonName.Component.GivenName, fuzzyStr, builder);
        fuzzyMatch(qpn, pn, PersonName.Component.MiddleName, fuzzyStr, builder);
        return builder;
    }

    private static void fuzzyMatch(QPersonName qpn, PersonName pn,
            PersonName.Component c,
            FuzzyStr fuzzyStr, BooleanBuilder builder) {
        String name = StringUtils.maskNull(pn.get(c), "*");
        if (name.equals("*"))
            return;

        Iterator<String> parts = org.dcm4chee.archive.entity.PersonName
                .tokenizePersonNameComponent(name);
        while (parts.hasNext())
            fuzzyMatch(qpn, parts.next(), fuzzyStr, builder);
    }

    private static void fuzzyMatch(QPersonName qpn, String name,
            FuzzyStr fuzzyStr, BooleanBuilder builder) {
        boolean wc = name.endsWith("*");
        if (wc) {
            name = name.substring(0, name.length()-1);
            if (name.isEmpty())
                return;
        }
        String fuzzyName = fuzzyStr.toFuzzy(name);
        Predicate pred = wc 
                ? QSoundexCode.soundexCode.codeValue.startsWith(fuzzyName)
                : QSoundexCode.soundexCode.codeValue.eq(fuzzyName);
        HibernateSubQuery subquery = new HibernateSubQuery()
                        .from(QSoundexCode.soundexCode)
                        .where(qpn.eq(QSoundexCode.soundexCode.personName),
                                pred);
        builder.and(subquery.exists());
    }

}
