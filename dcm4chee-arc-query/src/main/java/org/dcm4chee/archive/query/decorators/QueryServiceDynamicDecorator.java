package org.dcm4chee.archive.query.decorators;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.SeriesQueryAttributes;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyQueryAttributes;
import org.dcm4chee.archive.query.Query;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.QueryService;
import org.dcm4chee.conf.decorators.DynamicDecoratorWrapper;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import java.util.EnumSet;

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

	@Override
	public void recalculateDerivedFields(Study study, ApplicationEntity ae) {
        StudyUpdateSessionManager.LOG.info("Calculating derived fields");
        ArchiveDeviceExtension arcDevExt = studyUpdateSessionManager.device.getDeviceExtension(ArchiveDeviceExtension.class);

        ArchiveAEExtension arcAEExt = ae.getAEExtension(ArchiveAEExtension.class);
        QueryRetrieveView view = arcDevExt.getQueryRetrieveView(arcAEExt.getQueryRetrieveViewID());
        if (view == null) {
            StudyUpdateSessionManager.LOG.warn("Cannot re-calculate derived fields - query retrieve view ID is not specified for AE {}", ae.getAETitle());
            return;
        }

        QueryParam param = new QueryParam();
        param.setQueryRetrieveView(view);

        try {
            //create study view
            createStudyView(study.getPk(), param);

            //create series view
            for (Series series : study.getSeries())
                createSeriesView(series.getPk(), param);

        } catch (Exception e) {
            StudyUpdateSessionManager.LOG.error("Error while calculating derived fields on MPPS COMPLETE", e);
        }
    }
}
