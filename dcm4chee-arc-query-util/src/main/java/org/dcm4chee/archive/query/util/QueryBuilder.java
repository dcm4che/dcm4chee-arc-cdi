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

import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.QCode;
import org.dcm4chee.archive.entity.QContentItem;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QIssuer;
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.QPatientID;
import org.dcm4chee.archive.entity.QPersonName;
import org.dcm4chee.archive.entity.QRequestAttributes;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.QStudy;
import org.dcm4chee.archive.entity.QVerifyingObserver;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.hibernate.HibernateSubQuery;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.expr.SimpleExpression;
import com.mysema.query.types.expr.StringExpression;
import com.mysema.query.types.path.BeanPath;
import com.mysema.query.types.path.CollectionPath;
import com.mysema.query.types.path.StringPath;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@gmail.com>
 */
public class QueryBuilder {

    private QueryBuilder() {
    }

    public static StringPath[] stringPathOf(int tag, QueryRetrieveLevel qrLevel) {
        switch (qrLevel) {
        case FRAME:
        case IMAGE:
            switch (tag) {
            case Tag.SOPInstanceUID:
                return arrayOf(QInstance.instance.sopInstanceUID);
            case Tag.SOPClassUID:
                return arrayOf(QInstance.instance.sopClassUID);
            case Tag.InstanceNumber:
                return arrayOf(QInstance.instance.instanceNumber);
            case Tag.VerificationFlag:
                return arrayOf(QInstance.instance.verificationFlag);
            case Tag.CompletionFlag:
                return arrayOf(QInstance.instance.completionFlag);
            case Tag.ContentDate:
                return arrayOf(QInstance.instance.contentDate);
            case Tag.ContentTime:
                return arrayOf(QInstance.instance.contentTime);
            }
        case SERIES:
            switch (tag) {
            case Tag.SeriesInstanceUID:
                return arrayOf(QSeries.series.seriesInstanceUID);
            case Tag.SeriesNumber:
                return arrayOf(QSeries.series.seriesNumber);
            case Tag.Modality:
                return arrayOf(QSeries.series.modality);
            case Tag.BodyPartExamined:
                return arrayOf(QSeries.series.bodyPartExamined);
            case Tag.Laterality:
                return arrayOf(QSeries.series.laterality);
            case Tag.PerformedProcedureStepStartDate:
                return arrayOf(QSeries.series.performedProcedureStepStartDate);
            case Tag.PerformedProcedureStepStartTime:
                return arrayOf(QSeries.series.performedProcedureStepStartTime);
            case Tag.PerformingPhysicianName:
                return arrayOf(
                        QSeries.series.performingPhysicianName.familyName,
                        QSeries.series.performingPhysicianName.givenName,
                        QSeries.series.performingPhysicianName.middleName);
            case Tag.SeriesDescription:
                return arrayOf(QSeries.series.seriesDescription);
            case Tag.StationName:
                return arrayOf(QSeries.series.stationName);
            case Tag.InstitutionName:
                return arrayOf(QSeries.series.institutionName);
            case Tag.InstitutionalDepartmentName:
                return arrayOf(QSeries.series.institutionalDepartmentName);
            }
        case STUDY:
            switch (tag) {
            case Tag.StudyInstanceUID:
                return arrayOf(QStudy.study.studyInstanceUID);
            case Tag.StudyID:
                return arrayOf(QStudy.study.studyID);
            case Tag.StudyDate:
                return arrayOf(QStudy.study.studyDate);
            case Tag.StudyTime:
                return arrayOf(QStudy.study.studyTime);
            case Tag.ReferringPhysicianName:
                return arrayOf(
                        QStudy.study.referringPhysicianName.familyName,
                        QStudy.study.referringPhysicianName.givenName,
                        QStudy.study.referringPhysicianName.middleName);
            case Tag.StudyDescription:
                return arrayOf(QStudy.study.studyDescription);
            case Tag.AccessionNumber:
                return arrayOf(QStudy.study.accessionNumber);
            case Tag.ModalitiesInStudy:
                return arrayOf(QStudy.study.modalitiesInStudy);
            }
        case PATIENT:
            switch (tag) {
            case Tag.PatientName:
                return arrayOf(
                        QPatient.patient.patientName.familyName,
                        QPatient.patient.patientName.givenName,
                        QPatient.patient.patientName.middleName);
            case Tag.PatientSex:
                return arrayOf(QPatient.patient.patientSex);
            case Tag.PatientBirthDate:
                return arrayOf(QPatient.patient.patientBirthDate);
            }
        }
        throw new IllegalArgumentException("tag: " + TagUtils.toString(tag));
    }

    private static StringPath[] arrayOf(StringPath... paths) {
        return paths;
    }

    public static void addPatientLevelPredicates(BooleanBuilder builder,
            IDWithIssuer[] pids, Attributes keys, QueryParam queryParam) {

        boolean matchUnknown = queryParam.isMatchUnknown();

        builder.and(pids(pids, matchUnknown));

        if (keys == null)
            return;

        builder.and(MatchPersonName.match(QPatient.patient.patientName,
                keys.getString(Tag.PatientName, "*"), queryParam));
        builder.and(wildCard(QPatient.patient.patientSex,
                keys.getString(Tag.PatientSex, "*").toUpperCase(),
                matchUnknown, false));
        builder.and(MatchDateTimeRange.rangeMatch(
                QPatient.patient.patientBirthDate, keys, Tag.PatientBirthDate,
                MatchDateTimeRange.FormatDate.DA, matchUnknown));
        AttributeFilter attrFilter = queryParam
                .getAttributeFilter(Entity.Patient);
        builder.and(wildCard(
                QPatient.patient.patientCustomAttribute1,
                AttributeFilter.selectStringValue(keys,
                        attrFilter.getCustomAttribute1(), "*"), matchUnknown,
                true));
        builder.and(wildCard(
                QPatient.patient.patientCustomAttribute2,
                AttributeFilter.selectStringValue(keys,
                        attrFilter.getCustomAttribute2(), "*"), matchUnknown,
                true));
        builder.and(wildCard(
                QPatient.patient.patientCustomAttribute3,
                AttributeFilter.selectStringValue(keys,
                        attrFilter.getCustomAttribute3(), "*"), matchUnknown,
                true));
    }

    public static void addStudyLevelPredicates(BooleanBuilder builder,
            Attributes keys, QueryParam queryParam) {
        if (keys != null) {
            boolean matchUnknown = queryParam.isMatchUnknown();
            boolean combinedDatetimeMatching = queryParam
                    .isCombinedDatetimeMatching();
            builder.and(uids(QStudy.study.studyInstanceUID,
                    keys.getStrings(Tag.StudyInstanceUID), false));
            builder.and(wildCard(QStudy.study.studyID,
                    keys.getString(Tag.StudyID, "*"), matchUnknown, false));
            builder.and(MatchDateTimeRange.rangeMatch(QStudy.study.studyDate,
                    QStudy.study.studyTime, Tag.StudyDate, Tag.StudyTime,
                    Tag.StudyDateAndTime, keys, combinedDatetimeMatching,
                    matchUnknown));
            builder.and(MatchPersonName.match(
                    QStudy.study.referringPhysicianName,
                    keys.getString(Tag.ReferringPhysicianName, "*"), queryParam));
            builder.and(wildCard(QStudy.study.studyDescription,
                    keys.getString(Tag.StudyDescription, "*"), matchUnknown,
                    true));
            String accNo = keys.getString(Tag.AccessionNumber, "*");
            if (!accNo.equals("*")) {
                Issuer issuer = Issuer.valueOf(keys
                        .getNestedDataset(Tag.IssuerOfAccessionNumberSequence));
                if (issuer == null)
                    issuer = queryParam.getDefaultIssuerOfAccessionNumber();
                builder.and(matchUnknown(
                        idWithIssuer(QStudy.study.accessionNumber, accNo,
                                issuer), QStudy.study.accessionNumber,
                        matchUnknown));
            }
            builder.and(modalitiesInStudy(
                    keys.getString(Tag.ModalitiesInStudy, "*").toUpperCase(),
                    matchUnknown));
            builder.and(code(QStudy.study.procedureCodes,
                    keys.getNestedDataset(Tag.ProcedureCodeSequence),
                    matchUnknown));
            AttributeFilter attrFilter = queryParam
                    .getAttributeFilter(Entity.Study);
            builder.and(wildCard(
                    QStudy.study.studyCustomAttribute1,
                    AttributeFilter.selectStringValue(keys,
                            attrFilter.getCustomAttribute1(), "*"),
                    matchUnknown, true));
            builder.and(wildCard(
                    QStudy.study.studyCustomAttribute2,
                    AttributeFilter.selectStringValue(keys,
                            attrFilter.getCustomAttribute2(), "*"),
                    matchUnknown, true));
            builder.and(wildCard(
                    QStudy.study.studyCustomAttribute3,
                    AttributeFilter.selectStringValue(keys,
                            attrFilter.getCustomAttribute3(), "*"),
                    matchUnknown, true));
        }
        builder.and(permission(queryParam.getAccessControlIDs()));
    }

    private static Predicate permission(String[] accessControlIDs) {
        return accessControlIDs == null || accessControlIDs.length == 0 ? QStudy.study.accessControlID
                .isNull() : ExpressionUtils.or(
                QStudy.study.accessControlID.isNull(),
                QStudy.study.accessControlID.in(accessControlIDs));
    }

    public static void addSeriesLevelPredicates(BooleanBuilder builder,
            Attributes keys, QueryParam queryParam) {
        if (keys == null)
            return;

        boolean matchUnknown = queryParam.isMatchUnknown();
        builder.and(uids(QSeries.series.seriesInstanceUID,
                keys.getStrings(Tag.SeriesInstanceUID), false));
        builder.and(wildCard(QSeries.series.seriesNumber,
                keys.getString(Tag.SeriesNumber, "*"), matchUnknown, false));
        builder.and(wildCard(QSeries.series.modality,
                keys.getString(Tag.Modality, "*").toUpperCase(), matchUnknown,
                false));
        builder.and(wildCard(QSeries.series.bodyPartExamined,
                keys.getString(Tag.BodyPartExamined, "*").toUpperCase(),
                matchUnknown, false));
        builder.and(wildCard(QSeries.series.laterality,
                keys.getString(Tag.Laterality, "*").toUpperCase(),
                matchUnknown, false));
        builder.and(MatchDateTimeRange.rangeMatch(
                QSeries.series.performedProcedureStepStartDate,
                QSeries.series.performedProcedureStepStartTime,
                Tag.PerformedProcedureStepStartDate,
                Tag.PerformedProcedureStepStartTime,
                Tag.PerformedProcedureStepStartDateAndTime, keys,
                queryParam.isCombinedDatetimeMatching(), matchUnknown));
        builder.and(MatchPersonName.match(
                QSeries.series.performingPhysicianName,
                keys.getString(Tag.PerformingPhysicianName, "*"), queryParam));
        builder.and(wildCard(QSeries.series.seriesDescription,
                keys.getString(Tag.SeriesDescription, "*"), matchUnknown, true));
        builder.and(wildCard(QSeries.series.stationName,
                keys.getString(Tag.StationName, "*"), matchUnknown, true));
        builder.and(wildCard(QSeries.series.institutionName,
                keys.getString(Tag.InstitutionalDepartmentName, "*"),
                matchUnknown, true));
        builder.and(wildCard(QSeries.series.institutionalDepartmentName,
                keys.getString(Tag.InstitutionName, "*"), matchUnknown, true));
        builder.and(requestAttributes(
                keys.getNestedDataset(Tag.RequestAttributesSequence),
                queryParam));
        builder.and(code(QSeries.series.institutionCode,
                keys.getNestedDataset(Tag.InstitutionCodeSequence),
                matchUnknown));
        AttributeFilter attrFilter = queryParam
                .getAttributeFilter(Entity.Series);
        builder.and(wildCard(
                QSeries.series.seriesCustomAttribute1,
                AttributeFilter.selectStringValue(keys,
                        attrFilter.getCustomAttribute1(), "*"), matchUnknown,
                true));
        builder.and(wildCard(
                QSeries.series.seriesCustomAttribute2,
                AttributeFilter.selectStringValue(keys,
                        attrFilter.getCustomAttribute2(), "*"), matchUnknown,
                true));
        builder.and(wildCard(
                QSeries.series.seriesCustomAttribute3,
                AttributeFilter.selectStringValue(keys,
                        attrFilter.getCustomAttribute3(), "*"), matchUnknown,
                true));
    }

    public static void addInstanceLevelPredicates(BooleanBuilder builder,
            Attributes keys, QueryParam queryParam) {
        if (keys == null)
            return;

        boolean matchUnknown = queryParam.isMatchUnknown();
        boolean combinedDatetimeMatching = queryParam
                .isCombinedDatetimeMatching();
        builder.and(uids(QInstance.instance.sopInstanceUID,
                keys.getStrings(Tag.SOPInstanceUID), false));
        builder.and(uids(QInstance.instance.sopClassUID,
                keys.getStrings(Tag.SOPClassUID), false));
        builder.and(wildCard(QInstance.instance.instanceNumber,
                keys.getString(Tag.InstanceNumber, "*"), matchUnknown, false));
        builder.and(wildCard(QInstance.instance.verificationFlag, keys
                .getString(Tag.VerificationFlag, "*").toUpperCase(),
                matchUnknown, false));
        builder.and(wildCard(QInstance.instance.completionFlag,
                keys.getString(Tag.CompletionFlag, "*").toUpperCase(),
                matchUnknown, false));
        builder.and(MatchDateTimeRange.rangeMatch(
                QInstance.instance.contentDate, QInstance.instance.contentTime,
                Tag.ContentDate, Tag.ContentTime, Tag.ContentDateAndTime, keys,
                combinedDatetimeMatching, matchUnknown));
        builder.and(code(QInstance.instance.conceptNameCode,
                keys.getNestedDataset(Tag.ConceptNameCodeSequence),
                matchUnknown));
        builder.and(verifyingObserver(
                keys.getNestedDataset(Tag.VerifyingObserverSequence),
                queryParam));
        Sequence contentSeq = keys.getSequence(Tag.ContentSequence);
        if (contentSeq != null)
            for (Attributes item : contentSeq)
                builder.and(contentItem(item));
        AttributeFilter attrFilter = queryParam
                .getAttributeFilter(Entity.Instance);
        builder.and(wildCard(
                QInstance.instance.instanceCustomAttribute1,
                AttributeFilter.selectStringValue(keys,
                        attrFilter.getCustomAttribute1(), "*"), matchUnknown,
                true));
        builder.and(wildCard(
                QInstance.instance.instanceCustomAttribute2,
                AttributeFilter.selectStringValue(keys,
                        attrFilter.getCustomAttribute2(), "*"), matchUnknown,
                true));
        builder.and(wildCard(
                QInstance.instance.instanceCustomAttribute3,
                AttributeFilter.selectStringValue(keys,
                        attrFilter.getCustomAttribute3(), "*"), matchUnknown,
                true));
        builder.and(QInstance.instance.replaced.isFalse());
        builder.and(hideRejectedInstance(queryParam));
        builder.and(hideRejectionNotes(queryParam));
    }

    public static Predicate hideRejectionNotes(QueryParam queryParam) {
        ArrayList<Code> codes = new ArrayList<Code>(4);
        if (!queryParam.isShowRejectedForQualityReasons()
                && queryParam.getRejectedForQualityReasonsCode() != null)
            codes.add((Code) queryParam.getRejectedForQualityReasonsCode());
        if (queryParam.getRejectedForPatientSafetyReasonsCode() != null)
            codes.add((Code) queryParam
                    .getRejectedForPatientSafetyReasonsCode());
        if (queryParam.getIncorrectModalityWorklistEntryCode() != null)
            codes.add((Code) queryParam.getIncorrectModalityWorklistEntryCode());
        if (queryParam.getDataRetentionPeriodExpiredCode() != null)
            codes.add((Code) queryParam.getDataRetentionPeriodExpiredCode());
        if (codes.isEmpty())
            return null;
        return QInstance.instance.conceptNameCode.isNull().or(
                QInstance.instance.conceptNameCode.in(codes).not());
    }

    public static Predicate hideRejectedInstance(QueryParam queryParam) {
        BooleanExpression result = QInstance.instance.rejectionNoteCode
                .isNull();
        if (queryParam.isShowRejectedForQualityReasons()
                && queryParam.getRejectedForQualityReasonsCode() != null)
            result = result.or(QInstance.instance.rejectionNoteCode
                    .eq((Code) queryParam.getRejectedForQualityReasonsCode()));
        return result;
    }

    public static Predicate pids(IDWithIssuer[] pids, boolean matchUnknown) {
        if (pids == null || pids.length == 0)
            return null;

        BooleanBuilder result = new BooleanBuilder();
        for (IDWithIssuer pid : pids)
            result.or(idWithIssuer(QPatientID.patientID.id, pid.getID(),
                    pid.getIssuer()));

        if (!result.hasValue())
            return null;

        BooleanExpression matchingIDsExists = new HibernateSubQuery()
                .from(QPatientID.patientID)
                .leftJoin(QPatientID.patientID.issuer, QIssuer.issuer)
                .where(ExpressionUtils.and(
                        QPatientID.patientID.patient.eq(QPatient.patient),
                        result)).exists();

        return matchUnknown ? ExpressionUtils.or(matchingIDsExists,
                QPatient.patient.patientIDUnkown.isTrue()) : matchingIDsExists;
    }

    static Predicate idWithIssuer(StringPath idPath, String id, Issuer issuer) {
        Predicate predicate = wildCard(idPath, id);
        if (predicate == null)
            return null;

        if (issuer != null) {
            String entityID = issuer.getLocalNamespaceEntityID();
            String entityUID = issuer.getUniversalEntityID();
            String entityUIDType = issuer.getUniversalEntityIDType();
            if (!isUniversalMatching(entityID))
                predicate = ExpressionUtils.and(predicate, ExpressionUtils.or(
                        QIssuer.issuer.localNamespaceEntityID.isNull(),
                        QIssuer.issuer.localNamespaceEntityID.eq(entityID)));
            if (!isUniversalMatching(entityUID))
                predicate = ExpressionUtils.and(predicate, ExpressionUtils.or(
                        QIssuer.issuer.universalEntityID.isNull(),
                        ExpressionUtils.and(QIssuer.issuer.universalEntityID
                                .eq(entityUID),
                                QIssuer.issuer.universalEntityIDType
                                        .eq(entityUIDType))));
        }
        return predicate;
    }

    static Predicate wildCard(StringPath path, String value) {
        if (isUniversalMatching(value))
            return null;

        if (!containsWildcard(value))
            return path.eq(value);

        String pattern = toLikePattern(value);
        if (pattern.equals("%"))
            return null;

        return path.like(pattern, '!');
    }

    static boolean isUniversalMatching(String value) {
        return value == null || value.equals("*");
    }

    static Predicate wildCard(StringPath path, String value,
            boolean matchUnknown, boolean ignoreCase) {
        if (isUniversalMatching(value))
            return null;

        Predicate predicate;
        StringExpression expr = ignoreCase && StringUtils.isUpperCase(value) ? path
                .toUpperCase() : path;
        if (containsWildcard(value)) {
            String pattern = toLikePattern(value);
            if (pattern.equals("%"))
                return null;

            predicate = expr.like(pattern, '!');
        } else
            predicate = expr.eq(value);

        return matchUnknown(predicate, path, matchUnknown);
    }

    static boolean containsWildcard(String s) {
        return s.indexOf('*') >= 0 || s.indexOf('?') >= 0;
    }

    static Predicate matchUnknown(Predicate predicate, StringPath path,
            boolean matchUnknown) {
        return matchUnknown ? ExpressionUtils.or(predicate, path.eq("*"))
                : predicate;
    }

    static <T> Predicate matchUnknown(Predicate predicate, BeanPath<T> path,
            boolean matchUnknown) {
        return matchUnknown ? ExpressionUtils.or(predicate, path.isNull())
                : predicate;
    }

    static <E, Q extends SimpleExpression<? super E>> Predicate matchUnknown(
            Predicate predicate, CollectionPath<E, Q> path, boolean matchUnknown) {
        return matchUnknown ? ExpressionUtils.or(predicate, path.isEmpty())
                : predicate;
    }

    static String toLikePattern(String s) {
        StringBuilder like = new StringBuilder(s.length());
        char[] cs = s.toCharArray();
        char p = 0;
        for (char c : cs) {
            switch (c) {
            case '*':
                if (c != p)
                    like.append('%');
                break;
            case '?':
                like.append('_');
                break;
            case '_':
            case '%':
            case '!':
                like.append('!');
                // fall through
            default:
                like.append(c);
            }
            p = c;
        }
        return like.toString();
    }

    public static Predicate uids(StringPath path, String[] values,
            boolean matchUnknown) {
        if (values == null || values.length == 0 || values[0].equals("*"))
            return null;

        return matchUnknown(path.in(values), path, matchUnknown);
    }

    static Predicate modalitiesInStudy(String modality, boolean matchUnknown) {
        if (modality.equals("*"))
            return null;

        return new HibernateSubQuery()
                .from(QSeries.series)
                .where(QSeries.series.study.eq(QStudy.study),
                        wildCard(QSeries.series.modality, modality,
                                matchUnknown, false)).exists();
    }

    static Predicate code(Attributes item) {
        if (item == null || item.isEmpty())
            return null;

        return ExpressionUtils.allOf(
                wildCard(QCode.code.codeValue,
                        item.getString(Tag.CodeValue, "*")),
                wildCard(QCode.code.codingSchemeDesignator,
                        item.getString(Tag.CodingSchemeDesignator, "*")),
                wildCard(QCode.code.codingSchemeVersion,
                        item.getString(Tag.CodingSchemeVersion, "*")));
    }

    static Predicate code(QCode code, Attributes item, boolean matchUnknown) {
        Predicate predicate = code(item);
        if (predicate == null)
            return null;

        return matchUnknown(
                new HibernateSubQuery().from(QCode.code)
                        .where(QCode.code.eq(code), predicate).exists(), code,
                matchUnknown);
    }

    static Predicate code(CollectionPath<Code, QCode> codes, Attributes item,
            boolean matchUnknown) {
        Predicate predicate = code(item);
        if (predicate == null)
            return null;

        return matchUnknown(
                new HibernateSubQuery().from(QCode.code)
                        .where(codes.contains(QCode.code), predicate).exists(),
                codes, matchUnknown);
    }

    public static void andNotInCodes(BooleanBuilder builder, QCode code,
            List<Code> codes) {
        if (codes != null && !codes.isEmpty())
            builder.and(ExpressionUtils.or(code.isNull(), code.notIn(codes)));
    }

    static Predicate requestAttributes(Attributes item, QueryParam queryParam) {
        if (item == null || item.isEmpty())
            return null;

        boolean matchUnknown = queryParam.isMatchUnknown();
        BooleanBuilder builder = new BooleanBuilder();
        String accNo = item.getString(Tag.AccessionNumber, "*");
        if (!accNo.equals("*")) {
            Issuer issuer = Issuer.valueOf(item
                    .getNestedDataset(Tag.IssuerOfAccessionNumberSequence));
            if (issuer == null)
                issuer = queryParam.getDefaultIssuerOfAccessionNumber();
            builder.and(matchUnknown(
                    idWithIssuer(
                            QRequestAttributes.requestAttributes.accessionNumber,
                            accNo, issuer),
                    QRequestAttributes.requestAttributes.accessionNumber,
                    matchUnknown));
        }
        builder.and(wildCard(
                QRequestAttributes.requestAttributes.requestingService,
                item.getString(Tag.RequestingService, "*"), matchUnknown, true));
        builder.and(MatchPersonName.match(
                QRequestAttributes.requestAttributes.requestingPhysician,
                item.getString(Tag.ReferringPhysicianName, "*"), queryParam));
        builder.and(wildCard(
                QRequestAttributes.requestAttributes.requestedProcedureID,
                item.getString(Tag.RequestedProcedureID, "*"), matchUnknown,
                false));
        builder.and(uids(QRequestAttributes.requestAttributes.studyInstanceUID,
                item.getStrings(Tag.StudyInstanceUID), matchUnknown));
        builder.and(wildCard(
                QRequestAttributes.requestAttributes.scheduledProcedureStepID,
                item.getString(Tag.ScheduledProcedureStepID, "*"),
                matchUnknown, false));

        if (!builder.hasValue())
            return null;

        return matchUnknown(
                new HibernateSubQuery()
                        .from(QRequestAttributes.requestAttributes)
                        .leftJoin(
                                QRequestAttributes.requestAttributes.requestingPhysician,
                                QPersonName.personName)
                        .leftJoin(
                                QRequestAttributes.requestAttributes.issuerOfAccessionNumber,
                                QIssuer.issuer)
                        .where(QSeries.series.requestAttributes
                                .contains(QRequestAttributes.requestAttributes),
                                builder).exists(),
                QSeries.series.requestAttributes, matchUnknown);
    }

    static Predicate verifyingObserver(Attributes item, QueryParam queryParam) {
        if (item == null || item.isEmpty())
            return null;

        boolean matchUnknown = queryParam.isMatchUnknown();
        Predicate predicate = ExpressionUtils
                .allOf(MatchDateTimeRange
                        .rangeMatch(
                                QVerifyingObserver.verifyingObserver.verificationDateTime,
                                item, Tag.VerificationDateTime,
                                MatchDateTimeRange.FormatDate.DT, matchUnknown),
                        MatchPersonName
                                .match(QVerifyingObserver.verifyingObserver.verifyingObserverName,
                                        item.getString(
                                                Tag.VerifyingObserverName, "*"),
                                        queryParam));

        if (predicate == null)
            return null;

        return matchUnknown(
                new HibernateSubQuery()
                        .from(QVerifyingObserver.verifyingObserver)
                        .leftJoin(
                                QVerifyingObserver.verifyingObserver.verifyingObserverName,
                                QPersonName.personName)
                        .where(QInstance.instance.verifyingObservers
                                .contains(QVerifyingObserver.verifyingObserver),
                                predicate).exists(),
                QInstance.instance.verifyingObservers, matchUnknown);
    }

    static Predicate contentItem(Attributes item) {
        String valueType = item.getString(Tag.ValueType);
        if (!("CODE".equals(valueType) || "TEXT".equals(valueType)))
            return null;

        Predicate predicate = ExpressionUtils.allOf(
                code(QContentItem.contentItem.conceptName,
                        item.getNestedDataset(Tag.ConceptNameCodeSequence),
                        false),
                wildCard(QContentItem.contentItem.relationshipType, item
                        .getString(Tag.RelationshipType, "*").toUpperCase()),
                code(QContentItem.contentItem.conceptCode,
                        item.getNestedDataset(Tag.ConceptCodeSequence), false),
                wildCard(QContentItem.contentItem.textValue,
                        item.getString(Tag.TextValue, "*"), false, true));
        if (predicate == null)
            return null;

        return new HibernateSubQuery()
                .from(QContentItem.contentItem)
                .where(QInstance.instance.contentItems
                        .contains(QContentItem.contentItem),
                        predicate).exists();
    }
}
