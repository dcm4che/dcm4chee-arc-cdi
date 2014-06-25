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

package org.dcm4chee.archive.query.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.entity.Availability;
import org.dcm4chee.archive.entity.QIssuer;
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.QPersonName;
import org.dcm4chee.archive.entity.QRequestAttributes;
import org.dcm4chee.archive.entity.QStudy;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.types.Expression;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
class StudyQuery extends AbstractQuery {

    public StudyQuery(QueryContext context, StatelessSession session) {
        super(context, session);
    }

    @Override
    protected Expression<?>[] select() {
        return context.getQueryParam().isShowRejectedForQualityReasons()
                ? new Expression<?>[] {
                    QStudy.study.pk,                        // (0)
                    QStudy.study.numberOfSeriesA,           // (1)
                    QStudy.study.numberOfInstancesA,        // (2)
                    QStudy.study.modalitiesInStudy,         // (3)
                    QStudy.study.sopClassesInStudy,         // (4)
                    QStudy.study.retrieveAETs,              // (5)
                    QStudy.study.externalRetrieveAET,       // (6)
                    QStudy.study.availability,              // (7)
                    QStudy.study.encodedAttributes,         // (8)
                    QPatient.patient.encodedAttributes      // (9)
                }
                : new Expression<?>[] {
                    QStudy.study.pk,                        // (0)
                    QStudy.study.numberOfSeries,            // (1)
                    QStudy.study.numberOfInstances,         // (2)
                    QStudy.study.modalitiesInStudy,         // (3)
                    QStudy.study.sopClassesInStudy,         // (4)
                    QStudy.study.retrieveAETs,              // (5)
                    QStudy.study.externalRetrieveAET,       // (6)
                    QStudy.study.availability,              // (7)
                    QStudy.study.encodedAttributes,         // (8)
                    QPatient.patient.encodedAttributes      // (9)
                };
    }

    @Override
    protected HibernateQuery createQuery(QueryContext context) {
        BooleanBuilder builder = new BooleanBuilder();
        QueryBuilder.addPatientLevelPredicates(builder,
                context.getPatientIDs(),
                context.getKeys(),
                context.getQueryParam());
        QueryBuilder.addStudyLevelPredicates(builder,
                context.getKeys(),
                context.getQueryParam());
        return new HibernateQuery(session)
            .from(QStudy.study)
            .innerJoin(QStudy.study.patient, QPatient.patient)
            .leftJoin(QStudy.study.issuerOfAccessionNumber, QIssuer.issuer)
//            .leftJoin(QStudy.study.referringPhysicianName, QPersonName.personName)
//            .leftJoin(QPatient.patient.patientName, QPersonName.personName)
            .where(builder);
    }

    @Override
    public Attributes toAttributes(ScrollableResults results) {
        Long studyPk = results.getLong(0);
        int numberOfStudyRelatedSeries = results.getInteger(1);
        if (numberOfStudyRelatedSeries < 0) {
            numberOfStudyRelatedSeries = context.getQueryService()
                    .calculateNumberOfStudyRelatedSeries(studyPk,
                                context.getQueryParam());
        }
        // skip match for empty Study
        if (numberOfStudyRelatedSeries == 0)
            return null;

        int numberOfStudyRelatedInstances = results.getInteger(2);
        if (numberOfStudyRelatedInstances < 0) {
            numberOfStudyRelatedInstances = context.getQueryService()
                    .calculateNumberOfStudyRelatedInstance(studyPk,
                        context.getQueryParam());
        }
        String modalitiesInStudy = results.getString(3);
        String sopClassesInStudy = results.getString(4);
        String retrieveAETs = results.getString(5);
        String externalRetrieveAET = results.getString(6);
        Availability availability = (Availability) results.get(7);
        byte[] studyAttributes = results.getBinary(8);
        byte[] patientAttributes = results.getBinary(9);
        Attributes attrs = new Attributes();
        Utils.decodeAttributes(attrs, patientAttributes);
        Utils.decodeAttributes(attrs, studyAttributes);

        Utils.setStudyQueryAttributes(attrs,
                numberOfStudyRelatedSeries,
                numberOfStudyRelatedInstances,
                modalitiesInStudy,
                sopClassesInStudy);
        Utils.setRetrieveAET(attrs, retrieveAETs, externalRetrieveAET);
        Utils.setAvailability(attrs, availability);

        return attrs;
    }

}
