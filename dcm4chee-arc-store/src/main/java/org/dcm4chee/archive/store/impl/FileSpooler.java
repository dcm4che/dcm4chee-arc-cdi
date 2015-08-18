package org.dcm4chee.archive.store.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
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
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

/**
 * Created by Umberto Cappellini on 8/13/15.
 */
@ApplicationScoped
public class FileSpooler implements Spooler {

    static Logger LOG = LoggerFactory.getLogger(FileSpooler.class);

    @Inject
    private StorageService storageService;

    /**
     * Flushes to file the passed byte array, and then keeps spooling.
     */
    public void flushNspool(StoreContext context, byte[] toFlush) throws DicomServiceException {
        spool(context, toFlush);
    }

    public void spool(StoreContext context) throws DicomServiceException {
        spool(context, null);
    }

    private void spool(StoreContext context, byte[] toFlush) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        if (session.getSpoolStorageSystem() == null)
            throw new DicomServiceException(Status.UnableToProcess, "Missing Spool Storage");

        StorageSystem spoolingStorage = session.getSpoolStorageSystem();
        StorageContext spoolingContext = storageService.createStorageContext(spoolingStorage);
        int bufferLength = spoolingStorage.getBufferedOutputLength();
        MessageDigest digest = session.getMessageDigest();
        Attributes fmi = context.getFileMetainfo();
        InputStream in = context.getInputStream();
        Path spoolingPath = null;
        OutputStream out = null;

        try {

            String suffix = context.getSpoolFileSuffix() != null ? context.getSpoolFileSuffix() : ".dcm";
            spoolingPath = Files.createTempFile(session.getSpoolDirectory(), null, suffix);
            spoolingContext.setFilePath(spoolingPath);
            out = Files.newOutputStream(spoolingPath);

            if (digest != null) {
                digest.reset();
                out = new DigestOutputStream(out, digest);
            }

            out = new BufferedOutputStream(out);

            if (fmi != null) {

                @SuppressWarnings("resource")
                DicomOutputStream dout = new DicomOutputStream(out,
                        UID.ExplicitVRLittleEndian);

                //if the context is already containing the Attributes,
                //then those are stored
                if (context.getAttributes() == null)
                    dout.writeFileMetaInformation(fmi);
                else
                    dout.writeDataset(fmi, context.getAttributes());

                out = dout;
            }

            if (toFlush != null) {
                out.write(toFlush);
            }

            if (in != null) {
                if (in instanceof PDVInputStream)
                    ((PDVInputStream) in).copyTo(out);
                else StreamUtils.copy(in, out);

                DicomInputStream dis = null;

                try {
                    dis = new DicomInputStream(spoolingPath.toFile());
                    dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                    Attributes data = dis.readDataset(-1, -1);
                    context.setAttributes(data);
                } finally {
                    SafeClose.close(dis);
                }
            }

        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        } finally {
            try {
                SafeClose.close(out);
                spoolingContext.setFilePath(spoolingPath);
                spoolingContext.setFileSize(Files.size(spoolingContext.getFilePath()));
                spoolingContext.setFileDigest(digest == null ? null : TagUtils.toHexString(digest.digest()));
            } catch (IOException e) {
                throw new DicomServiceException(Status.UnableToProcess, e);
            }

            context.setSpoolingContext(spoolingContext);
        }
    }
}
