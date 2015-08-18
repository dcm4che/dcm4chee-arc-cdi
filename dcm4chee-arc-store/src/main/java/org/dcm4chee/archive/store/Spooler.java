package org.dcm4chee.archive.store;

import org.dcm4che3.net.service.DicomServiceException;

/**
 * Created by umberto on 8/13/15.
 */
public interface Spooler {

    /**
     * Spools a DICOM-store InputStream or Dataset.
     *
     * @param context StoreContext containing all the actual information about
     *                what and how to spool. It may contain already a Dataset
     *                (Attributes) or just an InputStream.
     * @throws DicomServiceException
     */
    public void spool(StoreContext context) throws DicomServiceException;
}
