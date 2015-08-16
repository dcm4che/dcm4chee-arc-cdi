package org.dcm4chee.archive.mpps.decorators;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.mpps.MPPSContext;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.conf.decorators.DynamicDecoratorWrapper;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

@Decorator
public abstract class MPPSServiceDynamicDecorator extends DynamicDecoratorWrapper<MPPSService> implements MPPSService {
	
    @Inject
    @Delegate
    MPPSService delegate;

    @Override
    public void coerceAttributes(MPPSContext context, Dimse dimse, Attributes attrs) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).coerceAttributes(context, dimse, attrs);
    }

    @Override
    public void createPerformedProcedureStep(String mppsSopInstanceUID, Attributes attrs, MPPSContext mppsContext) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).createPerformedProcedureStep(mppsSopInstanceUID, attrs, mppsContext);
    }

    @Override
    public void updatePerformedProcedureStep(String mppsSopInstanceUID, Attributes attrs, MPPSContext mppsContext) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).updatePerformedProcedureStep(mppsSopInstanceUID, attrs, mppsContext);
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

}
