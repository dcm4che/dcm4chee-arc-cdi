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
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.QStudy;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
class SeriesQuery extends AbstractQuery<Series> {

    private static final Expression<?>[] SELECT = {
        QStudy.study.pk,                                   // (0)
        QSeries.series.pk,                                 // (1)
        QStudy.study.numberOfSeries1,                      // (2)
        QStudy.study.numberOfSeries2,                      // (3)
        QStudy.study.numberOfSeries3,                      // (4)
        QStudy.study.numberOfInstances1,                   // (5)
        QStudy.study.numberOfInstances2,                   // (6)
        QStudy.study.numberOfInstances3,                   // (7)
        QSeries.series.numberOfInstances1,                 // (8)
        QSeries.series.numberOfInstances2,                 // (9)
        QSeries.series.numberOfInstances3,                 // (10)
        QStudy.study.modalitiesInStudy,                    // (11)
        QStudy.study.sopClassesInStudy,                    // (12)
        QSeries.series.retrieveAETs,                       // (13)
        QSeries.series.externalRetrieveAET,                // (14)
        QSeries.series.availability,                       // (15)
        QSeries.series.attributesBlob.encodedAttributes,   // (16)
        QStudy.study.attributesBlob.encodedAttributes,     // (17)
        QPatient.patient.attributesBlob.encodedAttributes  // (18)
    };

    private Long studyPk;
    private Attributes studyAttrs;

    public SeriesQuery(QueryContext context, StatelessSession session) {
        super(context, session, QSeries.series);
    }

    @Override
    protected Expression<?>[] select() {
        return SELECT;
    }

    @Override
    protected HibernateQuery applyJoins(HibernateQuery query) {
        query = QueryBuilder.applySeriesLevelJoins(query,
                context.getKeys(),
                context.getQueryParam());
        query = QueryBuilder.applyStudyLevelJoins(query,
                context.getKeys(),
                context.getQueryParam());
        query = QueryBuilder.applyPatientLevelJoins(query,
                context.getKeys(),
                context.getQueryParam());
        return query;
    }

    @Override
    protected Predicate predicate() {
        BooleanBuilder builder = new BooleanBuilder();
        QueryBuilder.addPatientLevelPredicates(builder,
                context.getPatientIDs(),
                context.getKeys(),
                context.getQueryParam());
        QueryBuilder.addStudyLevelPredicates(builder,
                context.getKeys(),
                context.getQueryParam());
        QueryBuilder.addSeriesLevelPredicates(builder,
                context.getKeys(),
                context.getQueryParam());
        return builder;
    }

    @Override
    public Attributes toAttributes(ScrollableResults results) {
        Long studyPk = results.getLong(0);
        Long seriesPk = results.getLong(1);
        int cacheSlot = context.getQueryParam().getNumberOfInstancesCacheSlot();

        int numberOfSeriesRelatedInstances = cacheSlot > 0
                ? results.getInteger(cacheSlot+7)
                : -1;
        if (numberOfSeriesRelatedInstances < 0) {
            numberOfSeriesRelatedInstances = context.getQueryService()
                    .calculateNumberOfSeriesRelatedInstance(seriesPk,
                            context.getQueryParam());
        }
        // skip match for empty Series
        if (numberOfSeriesRelatedInstances == 0)
            return null;

        String retrieveAETs = results.getString(13);
        String externalRetrieveAET = results.getString(14);
        Availability availability = (Availability) results.get(15);
        byte[] seriesAttributes = results.getBinary(16);
        if (!studyPk.equals(this.studyPk)) {
            this.studyAttrs = toStudyAttributes(studyPk, results);
            this.studyPk = studyPk;
        }
        Attributes seriesAttrs = new Attributes();
        Utils.decodeAttributes(seriesAttrs, seriesAttributes);
        Attributes attrs = Utils.mergeAndNormalize(studyAttrs, seriesAttrs);
        Utils.setSeriesQueryAttributes(attrs, numberOfSeriesRelatedInstances);
        Utils.setRetrieveAET(attrs, retrieveAETs, externalRetrieveAET);
        Utils.setAvailability(attrs, availability);

        return attrs;
    }

    private Attributes toStudyAttributes(Long studyPk, ScrollableResults results) {
        int cacheSlot = context.getQueryParam().getNumberOfInstancesCacheSlot();
        int numberOfStudyRelatedSeries = cacheSlot > 0
                ? results.getInteger(cacheSlot+1)
                : -1;
        if (numberOfStudyRelatedSeries < 0) {
            numberOfStudyRelatedSeries = context.getQueryService()
                    .calculateNumberOfStudyRelatedSeries(studyPk,
                            context.getQueryParam());
        }

        int numberOfStudyRelatedInstances = cacheSlot > 0
                ? results.getInteger(cacheSlot+4)
                : -1;
        if (numberOfStudyRelatedInstances < 0) {
            numberOfStudyRelatedInstances = context.getQueryService()
                    .calculateNumberOfStudyRelatedInstance(studyPk,
                            context.getQueryParam());
        }
        String modalitiesInStudy = results.getString(11);
        String sopClassesInStudy = results.getString(12);
        byte[] studyByteAttributes = results.getBinary(17);
        byte[] patientByteAttributes = results.getBinary(18);
        Attributes patientAttrs = new Attributes();
        Attributes studyAttrs = new Attributes();
        Utils.decodeAttributes(patientAttrs, patientByteAttributes);
        Utils.decodeAttributes(studyAttrs, studyByteAttributes);
        Attributes attrs = Utils.mergeAndNormalize(patientAttrs, studyAttrs);
        Utils.setStudyQueryAttributes(attrs,
                numberOfStudyRelatedSeries,
                numberOfStudyRelatedInstances,
                modalitiesInStudy,
                sopClassesInStudy);

        return attrs;
    }
}
