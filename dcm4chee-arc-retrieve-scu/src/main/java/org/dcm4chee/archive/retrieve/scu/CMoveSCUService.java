package org.dcm4chee.archive.retrieve.scu;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.DimseRSPHandler;

public interface CMoveSCUService {

    public boolean cmove(Attributes keys, DimseRSPHandler handler,
            ApplicationEntity ae,ApplicationEntity remoteAE
            , String destinationAET);

}
