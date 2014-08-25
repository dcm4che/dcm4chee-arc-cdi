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

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.same;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.jpa.hibernate.HibernateQueryFactory;
import com.mysema.query.jpa.hibernate.HibernateSubQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BooleanBuilder.class, HibernateQuery.class,
        HibernateSubQuery.class, ExpressionUtils.class })
public class QueryServiceEJBTest {
    private static final Long STUDY_PK = 3L;

    private static final int UPDATED_ROW_COUNT = 5;

    private static final int NUMBER_OF_STUDY_RELATED_SERIES = 7;

    private static final int NUMBER_OF_STUDY_RELATED_INSTANCES = 11;

    private static final int NUMBER_OF_SERIES_RELATED_INSTANCES = 13;

    private static final Long SERIES_PK = 17L;

    EasyMockSupport easyMockSupport;

    EntityManager mockEntityManager;

    BooleanBuilder mockBooleanBuilder;

    HibernateQueryFactory mockHibernateQueryFactory;

    HibernateQuery mockHibernateQuery;

    QueryServiceEJB cut;

    @Before
    public void before() {
        easyMockSupport = new EasyMockSupport();

        mockEntityManager = easyMockSupport.createMock(EntityManager.class);
        mockBooleanBuilder = PowerMock.createMock(BooleanBuilder.class);
        mockHibernateQueryFactory = easyMockSupport
                .createMock(HibernateQueryFactory.class);
        mockHibernateQuery = PowerMock.createMock(HibernateQuery.class);

        cut = easyMockSupport.createMockBuilder(QueryServiceEJB.class)
                .addMockedMethod("createBooleanBuilder").createMock();
        cut.em = mockEntityManager;
        cut.hibernateQueryFactory = mockHibernateQueryFactory;
    }

    @Test
    public void calculateNumberOfStudyRelatedSeries_shouldUpdateSeriesA_whenIsShowRejectedForQualityReasonsIsTrue() {
        QueryParam mockQueryParam = easyMockSupport
                .createMock(QueryParam.class);
        Query mockQuery = easyMockSupport.createMock(Query.class);

        expect(mockQueryParam.isShowRejectedForQualityReasons())
                .andReturn(true);
        expect(
                mockEntityManager
                        .createNamedQuery(Study.UPDATE_NUMBER_OF_SERIES_A))
                .andReturn(mockQuery);

        calculateNumbeOfStudyRelatedSeries(mockQueryParam, mockQuery);
    }

    @Test
    public void calculateNumberOfStudyRelatedSeries_shouldUpdateSeries_whenIsShowRejectedForQualityReasonsIsFalse() {
        QueryParam mockQueryParam = easyMockSupport
                .createMock(QueryParam.class);
        Query mockQuery = easyMockSupport.createMock(Query.class);

        expect(mockQueryParam.isShowRejectedForQualityReasons()).andReturn(
                false);
        expect(
                mockEntityManager
                        .createNamedQuery(Study.UPDATE_NUMBER_OF_SERIES))
                .andReturn(mockQuery);

        calculateNumbeOfStudyRelatedSeries(mockQueryParam, mockQuery);
    }

    @Test
    public void calculateNumberOfStudyRelatedInstance_shouldUpdateInstancesA_whenIsShowRejectedForQualityReasonsIsTrue() {
        QueryParam mockQueryParam = easyMockSupport
                .createMock(QueryParam.class);
        Query mockQuery = easyMockSupport.createMock(Query.class);

        expect(mockQueryParam.isShowRejectedForQualityReasons())
                .andReturn(true);
        expect(
                mockEntityManager
                        .createNamedQuery(Study.UPDATE_NUMBER_OF_INSTANCES_A))
                .andReturn(mockQuery);

        calculateNumberOfStudyRelatedInstance(mockQueryParam, mockQuery);
    }

    @Test
    public void calculateNumberOfStudyRelatedInstance_shouldUpdateInstances_whenIsShowRejectedForQualityReasonsIsFalse() {
        QueryParam mockQueryParam = easyMockSupport
                .createMock(QueryParam.class);
        Query mockQuery = easyMockSupport.createMock(Query.class);

        expect(mockQueryParam.isShowRejectedForQualityReasons()).andReturn(
                false);
        expect(
                mockEntityManager
                        .createNamedQuery(Study.UPDATE_NUMBER_OF_INSTANCES))
                .andReturn(mockQuery);

        calculateNumberOfStudyRelatedInstance(mockQueryParam, mockQuery);
    }

    @Test
    public void calculateNumberOfSeriesRelatedInstance_shouldUpdateInstancesA_whenIsShowRejectedForQualityReasonsIsTrue() {
        QueryParam mockQueryParam = easyMockSupport
                .createMock(QueryParam.class);
        Query mockQuery = easyMockSupport.createMock(Query.class);

        expect(mockQueryParam.isShowRejectedForQualityReasons())
                .andReturn(true);
        expect(
                mockEntityManager
                        .createNamedQuery(Series.UPDATE_NUMBER_OF_INSTANCES_A))
                .andReturn(mockQuery);

        calculateNumberOfSeriesRelatedInstance(mockQueryParam, mockQuery);
    }

    @Test
    public void calculateNumberOfSeriesRelatedInstance_shouldUpdateInstances_whenIsShowRejectedForQualityReasonsIsFalse() {
        QueryParam mockQueryParam = easyMockSupport
                .createMock(QueryParam.class);
        Query mockQuery = easyMockSupport.createMock(Query.class);

        expect(mockQueryParam.isShowRejectedForQualityReasons()).andReturn(
                false);
        expect(
                mockEntityManager
                        .createNamedQuery(Series.UPDATE_NUMBER_OF_INSTANCES))
                .andReturn(mockQuery);

        calculateNumberOfSeriesRelatedInstance(mockQueryParam, mockQuery);
    }

    void calculateNumbeOfStudyRelatedSeries(QueryParam mockQueryParam,
            Query mockQuery) {
        HibernateSubQuery mockHibernateSubQuery = PowerMock
                .createMock(HibernateSubQuery.class);
        Predicate mockPredicate = easyMockSupport
                .createNiceMock(Predicate.class);
        BooleanExpression mockBooleanExpression = easyMockSupport
                .createNiceMock(BooleanExpression.class);

        PowerMock.mockStatic(ExpressionUtils.class);

        expect(
                cut.createBooleanBuilder(isA(BooleanExpression.class),
                        same(mockQueryParam))).andReturn(mockBooleanBuilder);

        expect(mockHibernateQueryFactory.query()).andReturn(mockHibernateQuery);
        expect(mockHibernateQuery.from(QSeries.series)).andReturn(
                mockHibernateQuery);
        expect(mockHibernateQuery.where(mockPredicate)).andReturn(
                mockHibernateQuery);
        expect(mockHibernateQuery.count()).andReturn(
                (long) NUMBER_OF_STUDY_RELATED_SERIES);

        expect(mockHibernateQueryFactory.subQuery()).andReturn(
                mockHibernateSubQuery);
        expect(mockHibernateSubQuery.from(QInstance.instance)).andReturn(
                mockHibernateSubQuery);
        expect(mockHibernateSubQuery.where(mockBooleanBuilder)).andReturn(
                mockHibernateSubQuery);
        expect(mockHibernateSubQuery.exists()).andReturn(mockBooleanExpression);

        expect(
                ExpressionUtils.and(isA(Predicate.class),
                        same(mockBooleanExpression))).andReturn(mockPredicate);

        expect(
                mockQuery.setParameter(eq(1),
                        eq(NUMBER_OF_STUDY_RELATED_SERIES))).andReturn(
                mockQuery);
        expect(mockQuery.setParameter(2, STUDY_PK)).andReturn(mockQuery);
        expect(mockQuery.executeUpdate()).andReturn(UPDATED_ROW_COUNT);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        assertThat(cut.calculateNumberOfStudyRelatedSeries(STUDY_PK,
                mockQueryParam), is(NUMBER_OF_STUDY_RELATED_SERIES));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @SuppressWarnings("unchecked")
    void calculateNumberOfStudyRelatedInstance(QueryParam mockQueryParam,
            Query mockQuery) {
        expect(
                cut.createBooleanBuilder(isA(BooleanExpression.class),
                        same(mockQueryParam))).andReturn(mockBooleanBuilder);

        expect(mockHibernateQueryFactory.query()).andReturn(mockHibernateQuery);
        expect(mockHibernateQuery.from(QInstance.instance)).andReturn(
                mockHibernateQuery);
        expect(
                mockHibernateQuery.innerJoin(isA(EntityPath.class),
                        isA(Path.class))).andReturn(mockHibernateQuery);
        expect(mockHibernateQuery.where(mockBooleanBuilder)).andReturn(
                mockHibernateQuery);
        expect(mockHibernateQuery.count()).andReturn(
                (long) NUMBER_OF_STUDY_RELATED_INSTANCES);

        expect(
                mockQuery.setParameter(eq(1),
                        eq(NUMBER_OF_STUDY_RELATED_INSTANCES))).andReturn(
                mockQuery);
        expect(mockQuery.setParameter(2, STUDY_PK)).andReturn(mockQuery);
        expect(mockQuery.executeUpdate()).andReturn(UPDATED_ROW_COUNT);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        assertThat(cut.calculateNumberOfStudyRelatedInstance(STUDY_PK,
                mockQueryParam), is(NUMBER_OF_STUDY_RELATED_INSTANCES));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    void calculateNumberOfSeriesRelatedInstance(QueryParam mockQueryParam,
            Query mockQuery) {
        expect(
                cut.createBooleanBuilder(isA(BooleanExpression.class),
                        same(mockQueryParam))).andReturn(mockBooleanBuilder);

        expect(mockHibernateQueryFactory.query()).andReturn(mockHibernateQuery);
        expect(mockHibernateQuery.from(QInstance.instance)).andReturn(
                mockHibernateQuery);
        expect(mockHibernateQuery.where(mockBooleanBuilder)).andReturn(
                mockHibernateQuery);
        expect(mockHibernateQuery.count()).andReturn(
                (long) NUMBER_OF_SERIES_RELATED_INSTANCES);

        expect(
                mockQuery.setParameter(eq(1),
                        eq(NUMBER_OF_SERIES_RELATED_INSTANCES))).andReturn(
                mockQuery);
        expect(mockQuery.setParameter(2, SERIES_PK)).andReturn(mockQuery);
        expect(mockQuery.executeUpdate()).andReturn(UPDATED_ROW_COUNT);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        assertThat(cut.calculateNumberOfSeriesRelatedInstance(SERIES_PK,
                mockQueryParam), is(NUMBER_OF_SERIES_RELATED_INSTANCES));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }
}
