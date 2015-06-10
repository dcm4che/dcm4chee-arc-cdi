package org.dcm4chee.archive.query.decorators;

import java.util.EnumSet;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.entity.SeriesQueryAttributes;
import org.dcm4chee.archive.entity.StudyQueryAttributes;
import org.dcm4chee.archive.query.Query;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;
import org.dcm4chee.conf.decorators.DynamicDecoratorWrapper;

@Decorator
public class QueryServiceDynamicDecorator extends DynamicDecoratorWrapper<QueryService> implements QueryService {
	@Inject
	@Delegate
	QueryService delegate;
	
	@Override
	public QueryContext createQueryContext(QueryService queryService) {
		return wrapWithDynamicDecorators(delegate).createQueryContext(queryService);
	}

	@Override
	public Query createPatientQuery(QueryContext ctx) {
		return wrapWithDynamicDecorators(delegate).createPatientQuery(ctx);
	}

	@Override
	public Query createStudyQuery(QueryContext ctx) {
		return wrapWithDynamicDecorators(delegate).createStudyQuery(ctx);
	}

	@Override
	public Query createSeriesQuery(QueryContext ctx) {
		return wrapWithDynamicDecorators(delegate).createSeriesQuery(ctx);
	}

	@Override
	public Query createInstanceQuery(QueryContext ctx) {
		return wrapWithDynamicDecorators(delegate).createInstanceQuery(ctx);
	}

    @Override
    public Query createMWLItemQuery(QueryContext ctx) {
        return wrapWithDynamicDecorators(delegate).createMWLItemQuery(ctx);
    }

	@Override
	public Attributes getSeriesAttributes(Long seriesPk, QueryContext context) {
		return wrapWithDynamicDecorators(delegate).getSeriesAttributes
				(seriesPk, context);
	}

	@Override
	public QueryParam getQueryParam(Object source, String sourceAET, ArchiveAEExtension aeExt,
			EnumSet<QueryOption> queryOpts,	String[] accessControlIDs) {
		return wrapWithDynamicDecorators(delegate).getQueryParam(source, sourceAET, aeExt, queryOpts, accessControlIDs);
	}

	@Override
	public void initPatientIDs(QueryContext queryContext) {
		wrapWithDynamicDecorators(delegate).initPatientIDs(queryContext);		
	}

	@Override
	public void coerceRequestAttributes(QueryContext context) throws DicomServiceException {
		wrapWithDynamicDecorators(delegate).coerceRequestAttributes(context);		
	}

	@Override
	public void coerceResponseAttributes(QueryContext context, Attributes match) throws DicomServiceException {
		wrapWithDynamicDecorators(delegate).coerceResponseAttributes(context, match);		
	}

	@Override
	public StudyQueryAttributes createStudyView(Long studyPk, QueryParam queryParam) {
		return wrapWithDynamicDecorators(delegate).createStudyView(studyPk, queryParam);
	}

	@Override
	public SeriesQueryAttributes createSeriesView(Long seriesPk, QueryParam queryParam) {
		return wrapWithDynamicDecorators(delegate).createSeriesView(seriesPk, queryParam);
	}
}
