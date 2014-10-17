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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.same;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManager;

import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.SeriesQueryAttributes;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyQueryAttributes;
import org.dcm4chee.mysema.query.jpa.hibernate.DetachedHibernateQueryFactory;
import org.easymock.EasyMockSupport;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.Tuple;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.jpa.hibernate.HibernateSubQuery;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.expr.BooleanExpression;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BooleanBuilder.class, HibernateQuery.class,
        HibernateSubQuery.class, ExpressionUtils.class, QueryRetrieveView.class })
public class QueryServiceEJBTest {
    private static final Long STUDY_PK = 3L;

    private static final Long SERIES_PK = 17L;

    private static final String VIEW_ID = "view id";

    EasyMockSupport easyMockSupport;

    EntityManager mockEntityManager;

    BooleanBuilder mockBooleanBuilder;

    DetachedHibernateQueryFactory mockDetachedHibernateQueryFactory;

    HibernateQuery mockHibernateQuery;

    CloseableIterator<Tuple> mockTuples;

    QueryServiceEJB cut;

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        easyMockSupport = new EasyMockSupport();

        mockEntityManager = easyMockSupport.createMock(EntityManager.class);
        mockBooleanBuilder = PowerMock.createMock(BooleanBuilder.class);
        mockDetachedHibernateQueryFactory = easyMockSupport
                .createMock(DetachedHibernateQueryFactory.class);
        mockHibernateQuery = PowerMock.createMock(HibernateQuery.class);
        mockTuples = easyMockSupport.createMock(CloseableIterator.class);

        cut = easyMockSupport.createMockBuilder(QueryServiceEJB.class)
                .addMockedMethod("createBooleanBuilder")
                .addMockedMethod("createSeriesAttributes")
                .addMockedMethod("createSeriesQueryAttributes")
                .addMockedMethod("createStudyAttributes")
                .addMockedMethod("createStudyQueryAttributes").createMock();
        cut.em = mockEntityManager;
        cut.queryFactory = mockDetachedHibernateQueryFactory;
    }

    @Test
    public void calculateSeriesQueryAttributes_doesNotUpdateSeriesAttributes_whenSeriesHasZeroInstances() {
        QueryServiceEJB.SeriesAttributes mockSeriesAttributes = easyMockSupport
                .createMock(QueryServiceEJB.SeriesAttributes.class);
        SeriesQueryAttributes mockSeriesQueryAttributes = easyMockSupport
                .createMock(SeriesQueryAttributes.class);

        expect(mockTuples.hasNext()).andReturn(FALSE);

        mockSeriesQueryAttributes.setNumberOfInstances(0);
        mockSeriesAttributes.populateSeriesQueryAttributes(0,
                mockSeriesQueryAttributes);

        calculateSeriesQueryAttributes(mockSeriesAttributes,
                mockSeriesQueryAttributes);
    }

    @Test
    public void calculateSeriesQueryAttributes_doesUpdateSeriesAttributes_whenSeriesHasTwoInstances() {
        QueryServiceEJB.SeriesAttributes mockSeriesAttributes = easyMockSupport
                .createMock(QueryServiceEJB.SeriesAttributes.class);
        SeriesQueryAttributes mockSeriesQueryAttributes = easyMockSupport
                .createMock(SeriesQueryAttributes.class);
        Tuple mockTuple = easyMockSupport.createMock(Tuple.class);

        expect(mockTuples.hasNext()).andReturn(TRUE);
        expect(mockTuples.next()).andReturn(mockTuple);
        mockSeriesAttributes.updateAttributes(0, mockTuple);

        expect(mockTuples.hasNext()).andReturn(TRUE);
        expect(mockTuples.next()).andReturn(mockTuple);
        mockSeriesAttributes.updateAttributes(1, mockTuple);

        expect(mockTuples.hasNext()).andReturn(FALSE);

        mockSeriesQueryAttributes.setNumberOfInstances(2);
        mockSeriesAttributes.populateSeriesQueryAttributes(2,
                mockSeriesQueryAttributes);

        calculateSeriesQueryAttributes(mockSeriesAttributes,
                mockSeriesQueryAttributes);
    }

    @Test
    public void calculateStudyQueryAttributes_doesNotUpdateStudyAttributes_whenStudyHasZeroInstances() {
        QueryServiceEJB.StudyAttributes mockStudyAttributes = easyMockSupport
                .createMock(QueryServiceEJB.StudyAttributes.class);
        StudyQueryAttributes mockStudyQueryAttributes = easyMockSupport
                .createMock(StudyQueryAttributes.class);

        expect(mockTuples.hasNext()).andReturn(FALSE);

        mockStudyQueryAttributes.setNumberOfInstances(0);
        mockStudyAttributes.populateStudyQueryAttributes(0,
                mockStudyQueryAttributes);

        calculateStudyQueryAttributes(mockStudyAttributes,
                mockStudyQueryAttributes);
    }

    @Test
    public void calculateStudyQueryAttributes_doesUpdateStudyAttributes_whenStudyHasOneInstance() {
        QueryServiceEJB.StudyAttributes mockStudyAttributes = easyMockSupport
                .createMock(QueryServiceEJB.StudyAttributes.class);
        StudyQueryAttributes mockStudyQueryAttributes = easyMockSupport
                .createMock(StudyQueryAttributes.class);
        Tuple mockTuple = easyMockSupport.createMock(Tuple.class);

        expect(mockTuples.hasNext()).andReturn(TRUE);
        expect(mockTuples.next()).andReturn(mockTuple);
        mockStudyAttributes.updateAttributes(0, mockTuple);

        expect(mockTuples.hasNext()).andReturn(FALSE);

        mockStudyQueryAttributes.setNumberOfInstances(1);
        mockStudyAttributes.populateStudyQueryAttributes(1,
                mockStudyQueryAttributes);

        calculateStudyQueryAttributes(mockStudyAttributes,
                mockStudyQueryAttributes);
    }

    private void calculateSeriesQueryAttributes(
            QueryServiceEJB.SeriesAttributes mockSeriesAttributes,
            SeriesQueryAttributes mockSeriesQueryAttributes) {
        QueryParam mockQueryParam = easyMockSupport
                .createMock(QueryParam.class);
        Series mockSeries = easyMockSupport.createNiceMock(Series.class);
        QueryRetrieveView mockQueryRetrieveView = PowerMock
                .createMock(QueryRetrieveView.class);

        Session mockSession = easyMockSupport.createNiceMock(Session.class);

        expect(cut.createSeriesAttributes()).andReturn(mockSeriesAttributes);
        expect(mockEntityManager.unwrap(Session.class)).andReturn(mockSession);

        expect(
                cut.createBooleanBuilder(isA(BooleanExpression.class),
                        same(mockQueryParam))).andReturn(mockBooleanBuilder);

        expect(mockDetachedHibernateQueryFactory.query(mockSession)).andReturn(
                mockHibernateQuery);
        expect(mockHibernateQuery.from(QInstance.instance)).andReturn(
                mockHibernateQuery);
        expect(mockHibernateQuery.where(mockBooleanBuilder)).andReturn(
                mockHibernateQuery);

        expect(
                mockHibernateQuery
                        .iterate(QueryServiceEJB.CALC_SERIES_QUERY_ATTRS))
                .andReturn(mockTuples);

        mockTuples.close();

        expect(cut.createSeriesQueryAttributes()).andReturn(
                mockSeriesQueryAttributes);

        expect(mockEntityManager.getReference(Series.class, SERIES_PK))
                .andReturn(mockSeries);
        mockSeriesQueryAttributes.setSeries(mockSeries);

        expect(mockQueryParam.getQueryRetrieveView()).andReturn(
                mockQueryRetrieveView);
        expect(mockQueryRetrieveView.getViewID()).andReturn(VIEW_ID);

        mockSeriesQueryAttributes.setViewID(VIEW_ID);
        mockEntityManager.persist(mockSeriesQueryAttributes);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        assertThat(
                cut.calculateSeriesQueryAttributes(SERIES_PK, mockQueryParam),
                is(mockSeriesQueryAttributes));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    private void calculateStudyQueryAttributes(
            QueryServiceEJB.StudyAttributes mockStudyAttributes,
            StudyQueryAttributes mockStudyQueryAttributes) {
        QueryParam mockQueryParam = easyMockSupport
                .createMock(QueryParam.class);
        Study mockStudy = easyMockSupport.createNiceMock(Study.class);
        QueryRetrieveView mockQueryRetrieveView = PowerMock
                .createMock(QueryRetrieveView.class);

        Session mockSession = easyMockSupport.createNiceMock(Session.class);

        expect(cut.createStudyAttributes()).andReturn(mockStudyAttributes);
        expect(mockEntityManager.unwrap(Session.class)).andReturn(mockSession);

        expect(
                cut.createBooleanBuilder(isA(BooleanExpression.class),
                        same(mockQueryParam))).andReturn(mockBooleanBuilder);

        expect(mockDetachedHibernateQueryFactory.query(mockSession)).andReturn(
                mockHibernateQuery);
        expect(mockHibernateQuery.from(QInstance.instance)).andReturn(
                mockHibernateQuery);
        expect(
                mockHibernateQuery.innerJoin(QInstance.instance.series,
                        QSeries.series)).andReturn(mockHibernateQuery);
        expect(mockHibernateQuery.where(mockBooleanBuilder)).andReturn(
                mockHibernateQuery);

        expect(
                mockHibernateQuery
                        .iterate(QueryServiceEJB.CALC_STUDY_QUERY_ATTRS))
                .andReturn(mockTuples);

        mockTuples.close();

        expect(cut.createStudyQueryAttributes()).andReturn(
                mockStudyQueryAttributes);

        expect(mockEntityManager.getReference(Study.class, STUDY_PK))
                .andReturn(mockStudy);
        mockStudyQueryAttributes.setStudy(mockStudy);

        expect(mockQueryParam.getQueryRetrieveView()).andReturn(
                mockQueryRetrieveView);
        expect(mockQueryRetrieveView.getViewID()).andReturn(VIEW_ID);

        mockStudyQueryAttributes.setViewID(VIEW_ID);

        mockEntityManager.persist(mockStudyQueryAttributes);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        assertThat(cut.calculateStudyQueryAttributes(STUDY_PK, mockQueryParam),
                is(mockStudyQueryAttributes));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }
}
