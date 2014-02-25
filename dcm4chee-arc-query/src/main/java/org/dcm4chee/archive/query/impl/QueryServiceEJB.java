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
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.QueryPatientStudySeriesAttributes;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.hibernate.Session;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.jpa.hibernate.HibernateSubQuery;
import com.mysema.query.types.ExpressionUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Stateless
public class QueryServiceEJB {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;
    public int calculateNumberOfStudyRelatedSeries(Long studyPk,
            QueryParam queryParam) {
        BooleanBuilder builder = new BooleanBuilder(
                QInstance.instance.series.eq(QSeries.series));
        builder.and(QInstance.instance.replaced.isFalse());
        builder.and(QueryBuilder.hideRejectedInstance(queryParam));
        builder.and(QueryBuilder.hideRejectionNotes(queryParam));
        int num = (int) new HibernateQuery(em.unwrap(Session.class))
            .from(QSeries.series)
            .where(ExpressionUtils.and(
                QSeries.series.study.pk.eq(studyPk),
                new HibernateSubQuery()
                    .from(QInstance.instance)
                    .where(builder)
                    .exists()))
            .count();
        em.createNamedQuery(queryParam.isShowRejectedForQualityReasons()
                ? Study.UPDATE_NUMBER_OF_SERIES_A
                : Study.UPDATE_NUMBER_OF_SERIES)
            .setParameter(1, num)
            .setParameter(2, studyPk)
            .executeUpdate();
        return num;
    }

    public int calculateNumberOfStudyRelatedInstance(Long studyPk,
            QueryParam queryParam) {
        BooleanBuilder builder =
                new BooleanBuilder(QSeries.series.study.pk.eq(studyPk));
        builder.and(QInstance.instance.replaced.isFalse());
        builder.and(QueryBuilder.hideRejectedInstance(queryParam));
        builder.and(QueryBuilder.hideRejectionNotes(queryParam));
        int num = (int) new HibernateQuery(em.unwrap(Session.class))
            .from(QInstance.instance)
            .innerJoin(QInstance.instance.series, QSeries.series)
            .where(builder)
            .count();
        em.createNamedQuery(queryParam.isShowRejectedForQualityReasons()
                ? Study.UPDATE_NUMBER_OF_INSTANCES_A
                : Study.UPDATE_NUMBER_OF_INSTANCES)
            .setParameter(1, num)
            .setParameter(2, studyPk)
            .executeUpdate();
        return num;
    }

    public int calculateNumberOfSeriesRelatedInstance(Long seriesPk,
            QueryParam queryParam) {
        BooleanBuilder builder =
                new BooleanBuilder(QInstance.instance.series.pk.eq(seriesPk));
        builder.and(QInstance.instance.replaced.isFalse());
        builder.and(QueryBuilder.hideRejectedInstance(queryParam));
        builder.and(QueryBuilder.hideRejectionNotes(queryParam));
        int num = (int) new HibernateQuery(em.unwrap(Session.class))
            .from(QInstance.instance)
            .where(builder)
            .count();
        em.createNamedQuery(queryParam.isShowRejectedForQualityReasons()
                ? Series.UPDATE_NUMBER_OF_INSTANCES_A
                : Series.UPDATE_NUMBER_OF_INSTANCES)
            .setParameter(1, num)
            .setParameter(2, seriesPk)
            .executeUpdate();
        return num;
    }

    public Attributes getSeriesAttributes(Long seriesPk, QueryParam queryParam) {
        QueryPatientStudySeriesAttributes result = (QueryPatientStudySeriesAttributes)
                 em.createNamedQuery(queryParam.isShowRejectedForQualityReasons()
                        ? Series.QUERY_PATIENT_STUDY_SERIES_ATTRIBUTES_A
                        : Series.QUERY_PATIENT_STUDY_SERIES_ATTRIBUTES)
                  .setParameter(1, seriesPk)
                  .getSingleResult();
        Attributes attrs = result.getAttributes();
        Long studyPk = result.getStudyPk();
        int numberOfStudyRelatedSeries = result.getNumberOfStudyRelatedSeries();
        if (numberOfStudyRelatedSeries < 0)
            numberOfStudyRelatedSeries =
                calculateNumberOfStudyRelatedSeries(studyPk, queryParam);

        int numberOfStudyRelatedInstances = result.getNumberOfStudyRelatedInstances();
        if (numberOfStudyRelatedInstances < 0)
            numberOfStudyRelatedInstances = 
                calculateNumberOfStudyRelatedInstance(studyPk, queryParam);

        int numberOfSeriesRelatedInstances = result.getNumberOfSeriesRelatedInstances();
        if (numberOfSeriesRelatedInstances < 0)
            numberOfSeriesRelatedInstances =
                calculateNumberOfSeriesRelatedInstance(seriesPk, queryParam);

        Utils.setStudyQueryAttributes(attrs,
                numberOfStudyRelatedSeries,
                numberOfStudyRelatedInstances,
                result.getModalitiesInStudy(),
                result.getSopClassesInStudy());
        Utils.setSeriesQueryAttributes(attrs, numberOfSeriesRelatedInstances);
        return attrs;
    }

}
