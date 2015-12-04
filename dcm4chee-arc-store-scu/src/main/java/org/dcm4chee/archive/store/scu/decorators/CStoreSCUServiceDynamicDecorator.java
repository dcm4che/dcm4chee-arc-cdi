package org.dcm4chee.archive.store.scu.decorators;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.fetch.forward.FetchForwardService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.dcm4chee.conf.decorators.DynamicDecoratorWrapper;

@Decorator
public class CStoreSCUServiceDynamicDecorator extends DynamicDecoratorWrapper<CStoreSCUService> implements CStoreSCUService {
	@Inject
	@Delegate
	CStoreSCUService delegate;
	
	@Override
	public void cstore(String messageID, CStoreSCUContext context, List<ArchiveInstanceLocator> insts, int priority) throws DicomServiceException {
		wrapWithDynamicDecorators(delegate).cstore(messageID, context, insts, priority);
	}

	@Override
	public void scheduleStoreSCU(String messageID, CStoreSCUContext context, List<ArchiveInstanceLocator> insts, int retries, int priority,	long delay) {
		wrapWithDynamicDecorators(delegate).scheduleStoreSCU(messageID, context, insts, retries, priority, delay);
	}

	@Override
	public void coerceAttributes(Attributes attrs, CStoreSCUContext context) throws DicomServiceException {
		wrapWithDynamicDecorators(delegate).coerceAttributes(attrs, context);
	}

	@Override
	public void coerceFileBeforeMerge(ArchiveInstanceLocator inst, Attributes attrs, CStoreSCUContext context) throws DicomServiceException {
		wrapWithDynamicDecorators(delegate).coerceFileBeforeMerge(inst, attrs, context);
	}

	@Override
	public boolean isInstanceSuppressed(ArchiveInstanceLocator ref, Attributes attrs, String supressionCriteriaTemplateURI, CStoreSCUContext context) {
		return wrapWithDynamicDecorators(delegate).isInstanceSuppressed(ref, attrs, supressionCriteriaTemplateURI, context);
	}

	@Override
	public boolean isSOPClassUnsupported(ArchiveInstanceLocator ref, CStoreSCUContext context) {
		return wrapWithDynamicDecorators(delegate).isSOPClassUnsupported(ref, context);
	}

	@Override
	public Path getFile(ArchiveInstanceLocator inst) throws IOException {
		return wrapWithDynamicDecorators(delegate).getFile(inst);
	}

	@Override
	public FetchForwardService getFetchForwardService() {
		return wrapWithDynamicDecorators(delegate).getFetchForwardService();
	}

}
