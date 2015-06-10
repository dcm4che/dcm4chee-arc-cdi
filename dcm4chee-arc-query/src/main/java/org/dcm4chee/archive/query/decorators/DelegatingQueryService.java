package org.dcm4chee.archive.query.decorators;

import java.util.EnumSet;

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
import org.dcm4chee.conf.decorators.DelegatingService;
import org.dcm4chee.conf.decorators.DelegatingServiceImpl;

@DelegatingService
public class DelegatingQueryService extends DelegatingServiceImpl<QueryService> implements QueryService {

	@Override
	public QueryContext createQueryContext(QueryService queryService) {
		return getNextDecorator().createQueryContext(queryService);
	}

	@Override
	public Query createPatientQuery(QueryContext ctx) {
		return getNextDecorator().createPatientQuery(ctx);
	}

	@Override
	public Query createStudyQuery(QueryContext ctx) {
		return getNextDecorator().createStudyQuery(ctx);
	}

	@Override
	public Query createSeriesQuery(QueryContext ctx) {
		return getNextDecorator().createSeriesQuery(ctx);
	}

	@Override
	public Query createInstanceQuery(QueryContext ctx) {
		return getNextDecorator().createInstanceQuery(ctx);
	}

    @Override
    public Query createMWLItemQuery(QueryContext ctx) {
        return getNextDecorator().createMWLItemQuery(ctx);
    }

	@Override
	public Attributes getSeriesAttributes(Long seriesPk, QueryContext context) {
		return getNextDecorator().getSeriesAttributes(seriesPk, context);
	}

	@Override
	public QueryParam getQueryParam(Object source, String sourceAET, ArchiveAEExtension aeExt,
			EnumSet<QueryOption> queryOpts,	String[] accessControlIDs) {
		return getNextDecorator().getQueryParam(source, sourceAET, aeExt, queryOpts, accessControlIDs);
	}

	@Override
	public void initPatientIDs(QueryContext queryContext) {
		getNextDecorator().initPatientIDs(queryContext);		
	}

	@Override
	public void coerceRequestAttributes(QueryContext context) throws DicomServiceException {
		getNextDecorator().coerceRequestAttributes(context);		
	}

	@Override
	public void coerceResponseAttributes(QueryContext context, Attributes match) throws DicomServiceException {
		getNextDecorator().coerceResponseAttributes(context, match);		
	}

	@Override
	public StudyQueryAttributes createStudyView(Long studyPk, QueryParam queryParam) {
		return getNextDecorator().createStudyView(studyPk, queryParam);
	}

	@Override
	public SeriesQueryAttributes createSeriesView(Long seriesPk, QueryParam queryParam) {
		return getNextDecorator().createSeriesView(seriesPk, queryParam);
	}

}
