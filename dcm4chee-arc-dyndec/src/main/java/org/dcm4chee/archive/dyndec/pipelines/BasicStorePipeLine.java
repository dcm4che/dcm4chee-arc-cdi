package org.dcm4chee.archive.dyndec.pipelines;

import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.store.StoreContext;

public abstract interface BasicStorePipeLine {

    void coerceAttributes(StoreContext context) throws DicomServiceException;

}
