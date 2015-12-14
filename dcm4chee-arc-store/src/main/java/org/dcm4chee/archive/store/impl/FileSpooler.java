package org.dcm4chee.archive.store.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.store.Spooler;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.service.StorageService;

/**
 * Created by Umberto Cappellini on 8/13/15.
 */
@ApplicationScoped
public class FileSpooler implements Spooler {

    @Inject
    private StorageService storageService;

    /**
     * Flushes to file the passed byte array, and then keeps spooling.
     */
    public void flushNspool(StoreContext context, byte[] toFlush, boolean parse) throws DicomServiceException {
        spool(context, toFlush, parse);
    }

    public void spool(StoreContext context, boolean parse) throws DicomServiceException {
        spool(context, null, parse);
    }

    private void spool(StoreContext context, byte[] toFlush, boolean parse) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StorageSystem spoolingStorage = session.getSpoolStorageSystem();
        if (spoolingStorage == null)
            throw new DicomServiceException(Status.UnableToProcess, "Missing Spool Storage");

        StorageContext spoolingContext = storageService.createStorageContext(spoolingStorage);
        int bufferLength = spoolingStorage.getBufferedOutputLength();
        MessageDigest digest = session.getMessageDigest();
        Attributes fmi = context.getFileMetainfo();
        InputStream in = context.getInputStream();

        OutputStream out = null;
        Path spoolingPath;
        try {
            String suffix = context.getSpoolFileSuffix() != null ? context.getSpoolFileSuffix() : ".dcm";
            spoolingPath = Files.createTempFile(session.getSpoolDirectory(), null, suffix);
            out = Files.newOutputStream(spoolingPath);

            // normally the StorageService is responsible for digest calculation, but here we have to do it ourselves
            if (digest != null) {
                digest.reset();
                out = new DigestOutputStream(out, digest);
            }

            out = new BufferedOutputStream(out, bufferLength);

            if (fmi != null) {

                @SuppressWarnings("resource")
                DicomOutputStream dout = new DicomOutputStream(out,
                        UID.ExplicitVRLittleEndian);

                //if the context is already containing the Attributes,
                //then those are stored
                if (context.getOriginalAttributes() == null)
                    dout.writeFileMetaInformation(fmi);
                else
                    dout.writeDataset(fmi, context.getOriginalAttributes());

                out = dout;
            }

            if (toFlush != null) {
                out.write(toFlush);
            }

            if (in != null) {
                if (in instanceof PDVInputStream)
                    ((PDVInputStream) in).copyTo(out);
                else StreamUtils.copy(in, out);

                if (parse) {
                	out.flush();
                    try (DicomInputStream dis = new DicomInputStream(spoolingPath.toFile())) {
                        dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
                        Attributes data = dis.readDataset(-1, -1);
                        context.setOriginalAttributes(data);
                        Attributes dsFMI = dis.readFileMetaInformation();
                        context.setTransferSyntax(dsFMI != null ? dsFMI.getString(Tag.TransferSyntaxUID) : 
                                fmi != null ? fmi.getString(Tag.TransferSyntaxUID) : UID.ImplicitVRLittleEndian);
                    }
                }
            }

        } catch (IOException e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                throw new DicomServiceException(Status.UnableToProcess, e);
            }
        }

        spoolingContext.setFilePath(spoolingPath);
        try {
            spoolingContext.setFileSize(Files.size(spoolingPath));
        } catch (IOException e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
        spoolingContext.setFileDigest(digest == null ? null : TagUtils.toHexString(digest.digest()));

        context.setSpoolingContext(spoolingContext);
    }
}
