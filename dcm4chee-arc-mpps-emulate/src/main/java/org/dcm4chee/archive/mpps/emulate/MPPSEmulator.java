package org.dcm4chee.archive.mpps.emulate;

import java.util.Collection;

import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Series;

public interface MPPSEmulator {

    MPPS emulatePerformedProcedureStep(ArchiveAEExtension aeExt,
            Collection<Series> series) throws DicomServiceException;
}
