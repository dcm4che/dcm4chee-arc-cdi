package org.dcm4chee.archive.mpps.emulate;

import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.MPPSEmulationRule;
import org.dcm4chee.archive.entity.MPPS;

public interface MPPSEmulator {

    MPPS emulatePerformedProcedureStep(String emulatorAET, String sourceAET,
            String studyInstanceUID) throws DicomServiceException;

    void scheduleMPPSEmulation(String sourceAET, String studyInstanceUID,
            MPPSEmulationRule mppsEmulationRule);

    MPPS emulateNextScheduled() throws DicomServiceException;

    int emulateAllScheduled() throws DicomServiceException;

}
