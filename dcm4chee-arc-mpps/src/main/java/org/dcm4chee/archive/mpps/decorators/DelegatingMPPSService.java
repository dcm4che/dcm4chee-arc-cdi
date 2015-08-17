package org.dcm4chee.archive.mpps.decorators;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.mpps.MPPSContext;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.conf.decorators.DelegatingService;
import org.dcm4chee.conf.decorators.DelegatingServiceImpl;

@DelegatingService
public class DelegatingMPPSService extends DelegatingServiceImpl<MPPSService> implements MPPSService {

	@Override
	public void createPerformedProcedureStep(String mppsSopInstanceUID, Attributes attrs, MPPSContext mppsContext) throws DicomServiceException {
		getNextDecorator().createPerformedProcedureStep(mppsSopInstanceUID, attrs, mppsContext);
	}

	@Override
	public void updatePerformedProcedureStep(String mppsSopInstanceUID, Attributes attrs, MPPSContext mppsContext) throws DicomServiceException {
		getNextDecorator().updatePerformedProcedureStep(mppsSopInstanceUID, attrs, mppsContext);
	}

	@Override
	public void coerceAttributes(MPPSContext context, Dimse dimse, Attributes attrs) throws DicomServiceException {
		getNextDecorator().coerceAttributes(context, dimse, attrs);
	}

	@Deprecated
	@Override
	public MPPS createPerformedProcedureStep(ArchiveAEExtension arcAE, String sopInstanceUID, Attributes attrs,
											 Patient patient, MPPSService service) throws DicomServiceException {
		return null;
	}


	@Deprecated
	@Override
	public MPPS updatePerformedProcedureStep(ArchiveAEExtension arcAE, String iuid, Attributes attrs, MPPSService service)	throws DicomServiceException {
		return null;
	}

	@Deprecated
	@Override
	public void coerceAttributes(Association as, Dimse dimse, Attributes attrs) throws DicomServiceException {

	}


}
