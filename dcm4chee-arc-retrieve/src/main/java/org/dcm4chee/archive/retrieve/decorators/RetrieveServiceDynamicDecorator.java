package org.dcm4chee.archive.retrieve.decorators;

import java.util.List;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.conf.decorators.DynamicDecoratorWrapper;

@Decorator
public class RetrieveServiceDynamicDecorator extends DynamicDecoratorWrapper<RetrieveService> implements RetrieveService {
    @Inject
    @Delegate
    RetrieveService delegate;

	@Override
	public RetrieveContext createRetrieveContext(RetrieveService service,
			String sourceAET, ArchiveAEExtension arcAE) {
		return wrapWithDynamicDecorators(delegate).createRetrieveContext(service, sourceAET, arcAE);
	}

	@Override
	public IDWithIssuer[] queryPatientIDs(RetrieveContext context, Attributes keys) {
		return wrapWithDynamicDecorators(delegate).queryPatientIDs(context, keys);
	}

	@Override
	public List<ArchiveInstanceLocator> calculateMatches(IDWithIssuer[] pids,
			Attributes keys, QueryParam queryParam, boolean withoutBulkData) {
		return wrapWithDynamicDecorators(delegate).calculateMatches(pids, keys, queryParam, withoutBulkData);
	}

	@Override
	public List<ArchiveInstanceLocator> calculateMatches(String studyUID, String seriesUID,
			String objectUID, QueryParam queryParam, boolean withoutBulkData) {
		return wrapWithDynamicDecorators(delegate).calculateMatches(studyUID, seriesUID, objectUID, queryParam, withoutBulkData);
	}
}
