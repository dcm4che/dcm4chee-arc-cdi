package org.dcm4chee.archive.query.impl;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.same;

import javax.enterprise.inject.Instance;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.Study;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
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
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BooleanBuilder.class, HibernateQuery.class,
		HibernateSubQuery.class, ExpressionUtils.class })
public class QueryServiceEJBTest {
	private static final Long STUDY_PK = 5L;
	private static final int UPDATED_ROW_COUNT = 7;
	private static final int NUMBER_OF_STUDY_RELATED_SERIES = 3;

	EasyMockSupport easyMockSupport;
	EntityManager mockEntityManager;
	BooleanBuilder mockBooleanBuilder;
	Instance<HibernateQueryFactory> mockHibernateQueryFactoryInstance;
	HibernateQueryFactory mockHibernateQueryFactory;
	HibernateQuery mockHibernateQuery;
	HibernateSubQuery mockHibernateSubQuery;
	QueryServiceEJB cut;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		easyMockSupport = new EasyMockSupport();

		mockEntityManager = easyMockSupport.createMock(EntityManager.class);
		mockBooleanBuilder = PowerMock.createMock(BooleanBuilder.class);
		mockHibernateQueryFactoryInstance = easyMockSupport
				.createMock(Instance.class);
		mockHibernateQueryFactory = easyMockSupport
				.createMock(HibernateQueryFactory.class);
		mockHibernateQuery = PowerMock.createMock(HibernateQuery.class);
		mockHibernateSubQuery = PowerMock.createMock(HibernateSubQuery.class);

		cut = easyMockSupport.createMockBuilder(QueryServiceEJB.class)
				.addMockedMethod("createBooleanBuilder").createMock();
		cut.em = mockEntityManager;
		cut.hibernateQueryFactoryInstance = mockHibernateQueryFactoryInstance;
	}

	@Test
	public void test() {
		QueryParam mockQueryParam = easyMockSupport
				.createMock(QueryParam.class);
		Predicate mockPredicate = easyMockSupport
				.createNiceMock(Predicate.class);
		BooleanExpression mockBooleanExpression = easyMockSupport
				.createNiceMock(BooleanExpression.class);
		Query mockQuery = easyMockSupport.createMock(Query.class);

		PowerMock.mockStatic(ExpressionUtils.class);

		expect(
				cut.createBooleanBuilder(isA(BooleanExpression.class),
						same(mockQueryParam))).andReturn(mockBooleanBuilder);
		expect(mockHibernateQueryFactoryInstance.get()).andReturn(
				mockHibernateQueryFactory);

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

		expect(mockQueryParam.isShowRejectedForQualityReasons())
				.andReturn(true);

		expect(
				ExpressionUtils.and(isA(Predicate.class),
						same(mockBooleanExpression))).andReturn(mockPredicate);

		expect(
				mockEntityManager
						.createNamedQuery(Study.UPDATE_NUMBER_OF_SERIES_A))
				.andReturn(mockQuery);
		expect(
				mockQuery.setParameter(eq(1),
						eq(NUMBER_OF_STUDY_RELATED_SERIES))).andReturn(
				mockQuery);
		expect(mockQuery.setParameter(2, STUDY_PK)).andReturn(mockQuery);
		expect(mockQuery.executeUpdate()).andReturn(UPDATED_ROW_COUNT);

		easyMockSupport.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals(NUMBER_OF_STUDY_RELATED_SERIES, cut
				.calculateNumberOfStudyRelatedSeries(STUDY_PK, mockQueryParam));

		PowerMock.verifyAll();
		easyMockSupport.verifyAll();
	}

}
