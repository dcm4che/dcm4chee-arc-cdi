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

package org.dcm4chee.archive.query.impl;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.Availability;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.QSeriesQueryAttributes;
import org.dcm4chee.archive.entity.QStudy;
import org.dcm4chee.archive.entity.QStudyQueryAttributes;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.SeriesQueryAttributes;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyQueryAttributes;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.dcm4chee.mysema.query.jpa.hibernate.DetachedHibernateQueryFactory;
import org.hibernate.Session;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Stateless
public class QueryServiceEJB {

    static final Expression<?>[] PATIENT_STUDY_SERIES_ATTRS = {
        QStudy.study.pk,
        QSeriesQueryAttributes.seriesQueryAttributes.numberOfInstances,
        QStudyQueryAttributes.studyQueryAttributes.numberOfInstances,
        QStudyQueryAttributes.studyQueryAttributes.numberOfSeries,
        QStudyQueryAttributes.studyQueryAttributes.modalitiesInStudy,
        QStudyQueryAttributes.studyQueryAttributes.sopClassesInStudy,
        QueryBuilder.seriesAttributesBlob.encodedAttributes,
        QueryBuilder.studyAttributesBlob.encodedAttributes,
        QueryBuilder.patientAttributesBlob.encodedAttributes
    };

    static final Expression<?>[] CALC_STUDY_QUERY_ATTRS = {
        QSeries.series.pk,
        QSeries.series.modality,
        QInstance.instance.sopClassUID,
        QInstance.instance.retrieveAETs,
        QInstance.instance.externalRetrieveAET,
        QInstance.instance.availability
    };

    static final Expression<?>[] CALC_SERIES_QUERY_ATTRS = {
        QInstance.instance.retrieveAETs,
        QInstance.instance.externalRetrieveAET,
        QInstance.instance.availability
    };

    @PersistenceContext(unitName = "dcm4chee-arc")
    EntityManager em;

    @Inject
    DetachedHibernateQueryFactory queryFactory;

    public Attributes getSeriesAttributes(Long seriesPk, QueryParam queryParam) {
        String viewID = queryParam.getQueryRetrieveView().getViewID();
        Tuple result = queryFactory.query(em.unwrap(Session.class))
            .from(QSeries.series)
            .join(QSeries.series.attributesBlob, QueryBuilder.seriesAttributesBlob)
            .leftJoin(QSeries.series.queryAttributes, QSeriesQueryAttributes.seriesQueryAttributes)
                .on(QSeriesQueryAttributes.seriesQueryAttributes.viewID.eq(viewID))
            .join(QSeries.series.study, QStudy.study)
            .join(QStudy.study.attributesBlob, QueryBuilder.studyAttributesBlob)
            .leftJoin(QStudy.study.queryAttributes, QStudyQueryAttributes.studyQueryAttributes)
                .on(QStudyQueryAttributes.studyQueryAttributes.viewID.eq(viewID))
            .join(QStudy.study.patient, QPatient.patient)
            .join(QPatient.patient.attributesBlob, QueryBuilder.patientAttributesBlob)
            .where(QSeries.series.pk.eq(seriesPk))
            .singleResult(PATIENT_STUDY_SERIES_ATTRS);

        Integer numberOfSeriesRelatedInstances =
                result.get(QSeriesQueryAttributes.seriesQueryAttributes.numberOfInstances);
        if (numberOfSeriesRelatedInstances == null) {
            SeriesQueryAttributes seriesQueryAttributes =
                    calculateSeriesQueryAttributes(seriesPk, queryParam);
            numberOfSeriesRelatedInstances = seriesQueryAttributes.getNumberOfInstances();
        }

        int numberOfStudyRelatedSeries;
        String modalitiesInStudy;
        String sopClassesInStudy;
        Integer numberOfStudyRelatedInstances =
                result.get(QStudyQueryAttributes.studyQueryAttributes.numberOfInstances);
        if (numberOfStudyRelatedInstances == null) {
            StudyQueryAttributes studyQueryAttributes =
                    calculateStudyQueryAttributes(result.get(QStudy.study.pk), queryParam);
            numberOfStudyRelatedInstances = studyQueryAttributes.getNumberOfInstances();
            numberOfStudyRelatedSeries = studyQueryAttributes.getNumberOfSeries();
            modalitiesInStudy = studyQueryAttributes.getRawModalitiesInStudy();
            sopClassesInStudy = studyQueryAttributes.getRawSOPClassesInStudy();
        } else {
            numberOfStudyRelatedSeries =
                    result.get(QStudyQueryAttributes.studyQueryAttributes.numberOfSeries);
            modalitiesInStudy = 
                    result.get(QStudyQueryAttributes.studyQueryAttributes.modalitiesInStudy);
            sopClassesInStudy = 
                    result.get(QStudyQueryAttributes.studyQueryAttributes.sopClassesInStudy);
        }
        byte[] seriesBytes =
                result.get(QueryBuilder.seriesAttributesBlob.encodedAttributes);
        byte[] studyBytes =
                result.get(QueryBuilder.studyAttributesBlob.encodedAttributes);
        byte[] patientBytes =
                result.get(QueryBuilder.patientAttributesBlob.encodedAttributes);

        Attributes patientAttrs = new Attributes();
        Attributes studyAttrs = new Attributes();
        Attributes seriesAttrs = new Attributes();
        Utils.decodeAttributes(patientAttrs, patientBytes);
        Utils.decodeAttributes(studyAttrs, studyBytes);
        Utils.decodeAttributes(seriesAttrs, seriesBytes);
        Attributes attrs = Utils.mergeAndNormalize(patientAttrs, studyAttrs, seriesAttrs);
        Utils.setStudyQueryAttributes(attrs, numberOfStudyRelatedSeries,
                numberOfStudyRelatedInstances, modalitiesInStudy,
                sopClassesInStudy);
        Utils.setSeriesQueryAttributes(attrs, numberOfSeriesRelatedInstances);
        return attrs;
    }

    Predicate createPredicate(Predicate initial, QueryParam queryParam) {
        BooleanBuilder builder = new BooleanBuilder(initial);
        builder.and(QueryBuilder.hideRejectedInstance(queryParam));
        builder.and(QueryBuilder.hideRejectionNote(queryParam));
        return builder;
    }

    public StudyQueryAttributes calculateStudyQueryAttributes(
            Long studyPk, QueryParam queryParam) {
        StudyQueryAttributesBuilder builder = createStudyQueryAttributesBuilder();
        builder.setViewID(queryParam.getQueryRetrieveView().getViewID());
        builder.setStudy(em.getReference(Study.class, studyPk));
        try (
            CloseableIterator<Tuple> results = queryFactory.query(
                    em.unwrap(Session.class))
                .from(QInstance.instance)
                .innerJoin(QInstance.instance.series, QSeries.series)
                .where(createPredicate(
                        QSeries.series.study.pk.eq(studyPk), queryParam))
                .iterate(CALC_STUDY_QUERY_ATTRS)) {

            while (results.hasNext()) {
                builder.addInstance(results.next());
            }
        }
        StudyQueryAttributes queryAttrs = builder.build();
        em.persist(queryAttrs);
        return queryAttrs;
    }

    public SeriesQueryAttributes calculateSeriesQueryAttributes(
            Long seriesPk, QueryParam queryParam) {
        SeriesQueryAttributesBuilder builder = createSeriesQueryAttributesBuilder();
        builder.setViewID(queryParam.getQueryRetrieveView().getViewID());
        builder.setSeries(em.getReference(Series.class, seriesPk));
        try (
            CloseableIterator<Tuple> results = queryFactory.query(
                    em.unwrap(Session.class))
                .from(QInstance.instance)
                .where(createPredicate(
                        QInstance.instance.series.pk.eq(seriesPk), queryParam))
                .iterate(CALC_SERIES_QUERY_ATTRS)) {

            while (results.hasNext()) {
                builder.addInstance(results.next());
            }
        }
        SeriesQueryAttributes queryAttrs = builder.build();
        em.persist(queryAttrs);
        return queryAttrs;
    }

    StudyQueryAttributesBuilder createStudyQueryAttributesBuilder() {
        return new StudyQueryAttributesBuilder();
    }

    SeriesQueryAttributesBuilder createSeriesQueryAttributesBuilder() {
        return new SeriesQueryAttributesBuilder();
    }

    static class CommonStudySeriesQueryAttributesBuilder {

        protected int numberOfInstances;
        protected String[] retrieveAETs;
        protected String externalRetrieveAET;
        protected Availability availability;
        protected String viewID;

        public void setViewID(String viewID) {
            this.viewID = viewID;
        }

        public void addInstance(Tuple result) {
            String[] retrieveAETs1 = StringUtils.split(
                    result.get(QInstance.instance.retrieveAETs),
                    '\\');
            String externalRetrieveAET1 =
                    result.get(QInstance.instance.externalRetrieveAET);
            Availability availability1 =
                    result.get(QInstance.instance.availability);
            if (numberOfInstances++ == 0) {
                retrieveAETs = retrieveAETs1;
                externalRetrieveAET = externalRetrieveAET1;
                availability = availability1;
            } else {
                retrieveAETs = Utils.intersection(
                        retrieveAETs, retrieveAETs1);
                if (externalRetrieveAET != null
                        && !externalRetrieveAET.equals(externalRetrieveAET1))
                    externalRetrieveAET = null;
                if (availability.compareTo(availability1) < 0)
                    availability = availability1;
            }
        }
    }

    static class SeriesQueryAttributesBuilder
            extends CommonStudySeriesQueryAttributesBuilder {

        protected Series series;

        public void setSeries(Series series) {
            this.series = series;
        }

        public SeriesQueryAttributes build() {
            SeriesQueryAttributes queryAttrs = new SeriesQueryAttributes();
            queryAttrs.setSeries(series);
            queryAttrs.setViewID(viewID);
            queryAttrs.setNumberOfInstances(numberOfInstances);
            if (numberOfInstances > 0) {
                queryAttrs.setRetrieveAETs(retrieveAETs);
                queryAttrs.setExternalRetrieveAET(externalRetrieveAET);
                queryAttrs.setAvailability(availability);
            }
            return queryAttrs;
        }
    }

    static class StudyQueryAttributesBuilder
            extends CommonStudySeriesQueryAttributesBuilder {
    
        protected Set<Long> seriesPKs = new HashSet<Long>();
        protected Set<String> mods = new HashSet<String>();
        protected Set<String> cuids = new HashSet<String>();
        protected Study study;

        public void setStudy(Study study) {
            this.study = study;
        }

        @Override
        public void addInstance(Tuple result) {
            super.addInstance(result);
            if (seriesPKs.add(result.get(QSeries.series.pk))) {
                String modality1 = result.get(QSeries.series.modality);
                if (modality1 != null)
                    mods.add(modality1);
            }
            cuids.add(result.get(QInstance.instance.sopClassUID));
        }
    
        public StudyQueryAttributes build() {
            StudyQueryAttributes queryAttrs = new StudyQueryAttributes();
            queryAttrs.setViewID(viewID);
            queryAttrs.setStudy(study);
            queryAttrs.setNumberOfInstances(numberOfInstances);
            if (numberOfInstances > 0) {
                queryAttrs.setNumberOfSeries(seriesPKs.size());
                queryAttrs.setModalitiesInStudy(mods.toArray(new String[mods.size()]));
                queryAttrs.setSOPClassesInStudy(cuids.toArray(new String[cuids.size()]));
                queryAttrs.setRetrieveAETs(retrieveAETs);
                queryAttrs.setExternalRetrieveAET(externalRetrieveAET);
                queryAttrs.setAvailability(availability);
            }
            return queryAttrs;
        }
    }
}
