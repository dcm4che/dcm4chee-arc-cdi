package org.dcm4chee.archive.store.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.store.Spooler;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

/**
 * Created by umberto on 8/13/15.
 */
@ApplicationScoped
public class MemoryOrFileSpooler implements Spooler {

    static Logger LOG = LoggerFactory.getLogger(MemoryOrFileSpooler.class);

    @Inject
    private StorageService storageService;

    @Inject
    private FileSpooler fileSpooler;

    private static final int DEFAULT_READ_SIZE = 1024 * 2;

    public void spool(StoreContext context, boolean parse) throws DicomServiceException {

        StoreSession session = context.getStoreSession();

        StorageSystem spoolingStorage = session.getSpoolStorageSystem();
        StorageContext spoolingContext = storageService.createStorageContext(spoolingStorage);
        Attributes fmi = context.getFileMetainfo();
        InputStream in = context.getInputStream();

        int cutoffLength = spoolingStorage.getSpoolingCutoffLength();
        ByteArrayOutputStream bufferOS = new ByteArrayOutputStream(cutoffLength);
        boolean spooToFile = false;

        try {
            int nRead;
            long readBytes = 0;
            byte[] data = new byte[DEFAULT_READ_SIZE];

            while ((nRead = in.read(data, 0, data.length)) != -1) {

                bufferOS.write(data, 0, nRead);
                readBytes+=nRead;

                if ((readBytes+DEFAULT_READ_SIZE)>cutoffLength) {
                    spooToFile = true;
                    break;
                }
            }

        } catch (IOException e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }

        if (spooToFile) {
            fileSpooler.flushNspool(context, bufferOS.toByteArray(),parse);
            SafeClose.close(bufferOS);
        }
        else {
            if (parse) {
                try {
                    DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(bufferOS.toByteArray()));
                    dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES);
                    Attributes data = dis.readDataset(-1, -1);
                    context.setAttributes(data);
                    Attributes dsFMI = dis.readFileMetaInformation();
                    context.setTransferSyntax(dsFMI != null ? dsFMI.getString(Tag.TransferSyntaxUID) : 
                            fmi != null ? fmi.getString(Tag.TransferSyntaxUID) : UID.ImplicitVRLittleEndian);
                } catch (IOException e) {
                    throw new DicomServiceException(StoreService.DATA_SET_NOT_PARSEABLE);
                } finally {
                    context.setSpoolingContext(spoolingContext);
                }
            }
        }
    }

}
