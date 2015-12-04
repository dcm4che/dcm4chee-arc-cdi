package org.dcm4chee.archive.store.scu.decorators;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.fetch.forward.FetchForwardService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.dcm4chee.conf.decorators.DelegatingService;
import org.dcm4chee.conf.decorators.DelegatingServiceImpl;

@DelegatingService
public class DelegatingCStoreSCUService extends DelegatingServiceImpl<CStoreSCUService> implements CStoreSCUService {

	@Override
	public void cstore(String messageID, CStoreSCUContext context, List<ArchiveInstanceLocator> insts, int priority) throws DicomServiceException {
		getNextDecorator().cstore(messageID, context, insts, priority);
	}

	@Override
	public void scheduleStoreSCU(String messageID, CStoreSCUContext context, List<ArchiveInstanceLocator> insts, int retries, int priority,	long delay) {
		getNextDecorator().scheduleStoreSCU(messageID, context, insts, retries, priority, delay);
	}

	@Override
	public void coerceAttributes(Attributes attrs, CStoreSCUContext context) throws DicomServiceException {
		getNextDecorator().coerceAttributes(attrs, context);
	}

	@Override
	public void coerceFileBeforeMerge(ArchiveInstanceLocator inst, Attributes attrs, CStoreSCUContext context) throws DicomServiceException {
		getNextDecorator().coerceFileBeforeMerge(inst, attrs, context);
	}

	@Override
	public boolean isInstanceSuppressed(ArchiveInstanceLocator ref, Attributes attrs, String supressionCriteriaTemplateURI, CStoreSCUContext context) {
		return getNextDecorator().isInstanceSuppressed(ref, attrs, supressionCriteriaTemplateURI, context);
	}

	@Override
	public boolean isSOPClassUnsupported(ArchiveInstanceLocator ref, CStoreSCUContext context) {
		return getNextDecorator().isSOPClassUnsupported(ref, context);
	}

	@Override
	public Path getFile(ArchiveInstanceLocator inst) throws IOException {
		return getNextDecorator().getFile(inst);
	}

	@Override
	public FetchForwardService getFetchForwardService() {
		return getNextDecorator().getFetchForwardService();
	}

}
