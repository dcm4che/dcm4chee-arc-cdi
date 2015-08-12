package org.dcm4chee.archive.mpps.decorators;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.conf.decorators.DynamicDecoratorWrapper;

@Decorator
public abstract class MPPSServiceDynamicDecorator extends DynamicDecoratorWrapper<MPPSService> implements MPPSService {
	
    @Inject
    @Delegate
    MPPSService delegate;

    @Override
    public void coerceAttributes(Association as, Dimse dimse, Attributes attrs) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).coerceAttributes(as, dimse, attrs);
    }

    @Override
    public MPPS createPerformedProcedureStep(ApplicationEntity ae, String mppsSopInstanceUID, Attributes attrs) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).createPerformedProcedureStep(ae, mppsSopInstanceUID, attrs);
    }

    @Override
    public MPPS updatePerformedProcedureStep(ApplicationEntity ae, String mppsSopInstanceUID, Attributes attrs) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).updatePerformedProcedureStep(ae, mppsSopInstanceUID, attrs);
    }

    @Override
	public MPPS createPerformedProcedureStep(ArchiveAEExtension arcAE, String sopInstanceUID, Attributes attrs, Patient patient,
			MPPSService service) throws DicomServiceException {
		return wrapWithDynamicDecorators(delegate).createPerformedProcedureStep(arcAE, sopInstanceUID, attrs, patient, service);
	}

	@Override
	public MPPS updatePerformedProcedureStep(ArchiveAEExtension arcAE, String iuid, Attributes attrs, MPPSService service) throws DicomServiceException {
		return wrapWithDynamicDecorators(delegate).updatePerformedProcedureStep(arcAE, iuid, attrs, service);
	}

	@Override
	public void fireCreateMPPSEvent(ApplicationEntity ae, Attributes data, MPPS mpps) {
		wrapWithDynamicDecorators(delegate).fireCreateMPPSEvent(ae, data, mpps);
	}

	@Override
	public void fireUpdateMPPSEvent(ApplicationEntity ae, Attributes data, MPPS mpps) {
		wrapWithDynamicDecorators(delegate).fireUpdateMPPSEvent(ae, data, mpps);
	}

	@Override
	public void fireFinalMPPSEvent(ApplicationEntity ae, Attributes data, MPPS mpps) {
		wrapWithDynamicDecorators(delegate).fireFinalMPPSEvent(ae, data, mpps);
	}

}
