package org.dcm4chee.archive.mpps.decorators;

import org.dcm4che3.data.Attributes;
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

    /**
     * will be removed soon, redirected to createPerformedProcedureStep(ApplicationEntity ae, String mppsSopInstanceUID, Attributes attrs)
     * @param arcAE
     * @param sopInstanceUID
     * @param attrs
     * @param patient ignored
     * @param service ignored
     * @return
     * @throws DicomServiceException
     */
    @Deprecated
    @Override
	public MPPS createPerformedProcedureStep(ArchiveAEExtension arcAE, String sopInstanceUID, Attributes attrs,
			Patient patient, MPPSService service) throws DicomServiceException {
        getNextDecorator().createPerformedProcedureStep(sopInstanceUID, attrs, new MPPSContext(null, arcAE.getApplicationEntity().getAETitle()));
		return null;
	}

    /**
     * will be removed soon, redirected to updatePerformedProcedureStep(ApplicationEntity ae, String mppsSopInstanceUID, Attributes attrs)
     * @param arcAE
     * @param iuid
     * @param attrs
     * @param service ignored
     * @return
     * @throws DicomServiceException
     */
    @Deprecated
	@Override
	public MPPS updatePerformedProcedureStep(ArchiveAEExtension arcAE,
			String iuid, Attributes attrs, MPPSService service)	throws DicomServiceException {
        getNextDecorator().updatePerformedProcedureStep(iuid, attrs, new MPPSContext(null, arcAE.getApplicationEntity().getAETitle()));
		return null;
	}

	@Override
	public void createPerformedProcedureStep(String mppsSopInstanceUID, Attributes attrs, MPPSContext mppsContext)
			throws DicomServiceException {
		getNextDecorator().createPerformedProcedureStep(mppsSopInstanceUID, attrs, mppsContext);
	}

	@Override
	public void updatePerformedProcedureStep(String mppsSopInstanceUID, Attributes attrs, MPPSContext mppsContext)
			throws DicomServiceException {
		getNextDecorator().updatePerformedProcedureStep(mppsSopInstanceUID, attrs, mppsContext);
	}

	@Override
	public void coerceAttributes(MPPSContext context, Dimse dimse, Attributes attrs)	throws DicomServiceException {
		getNextDecorator().coerceAttributes(context, dimse, attrs);
	}


}
