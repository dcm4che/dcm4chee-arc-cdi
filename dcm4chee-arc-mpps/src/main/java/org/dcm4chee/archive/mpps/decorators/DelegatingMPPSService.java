package org.dcm4chee.archive.mpps.decorators;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
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
        return getNextDecorator().createPerformedProcedureStep(arcAE.getApplicationEntity(), sopInstanceUID, attrs);
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
        return getNextDecorator().updatePerformedProcedureStep(arcAE.getApplicationEntity(), iuid, attrs);
    }

	@Override
	public MPPS createPerformedProcedureStep(ApplicationEntity ae, String mppsSopInstanceUID, Attributes attrs)
			throws DicomServiceException {
		return getNextDecorator().createPerformedProcedureStep(ae, mppsSopInstanceUID, attrs);
	}

	@Override
	public MPPS updatePerformedProcedureStep(ApplicationEntity ae, String mppsSopInstanceUID, Attributes attrs)
			throws DicomServiceException {
		return getNextDecorator().updatePerformedProcedureStep(ae, mppsSopInstanceUID, attrs);
	}

	@Override
	public void coerceAttributes(Association as, Dimse dimse, Attributes attrs)	throws DicomServiceException {
		getNextDecorator().coerceAttributes(as, dimse, attrs);
	}

	@Override
	public void fireCreateMPPSEvent(ApplicationEntity ae, Attributes data, MPPS mpps) {
		getNextDecorator().fireCreateMPPSEvent(ae, data, mpps);
	}

	@Override
	public void fireUpdateMPPSEvent(ApplicationEntity ae, Attributes data, MPPS mpps) {
        getNextDecorator().fireUpdateMPPSEvent(ae, data, mpps);
	}

	@Override
	public void fireFinalMPPSEvent(ApplicationEntity ae, Attributes data, MPPS mpps) {
		getNextDecorator().fireFinalMPPSEvent(ae, data, mpps);
	}

}
