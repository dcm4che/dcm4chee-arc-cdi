/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.archive.compress.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.CompressionRule;
import org.dcm4che3.imageio.codec.Compressor;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.ObjectAlreadyExistsException;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.service.StorageService;
import org.dcm4chee.task.ImageProcessingTaskTypes;
import org.dcm4chee.task.MemoryConsumingTask;
import org.dcm4chee.task.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compresses and stores DICOM instance.
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
class StoreCompressedTask implements MemoryConsumingTask<StorageContext> {

    private static Logger LOG = LoggerFactory.getLogger(StoreServiceCompressDecorator.class);

    private final StorageService storageService;
    private final StoreContext context;
    private final CompressionRule rule;
    private Compressor compressor;

    public StoreCompressedTask(StorageService storageService, StoreContext context, CompressionRule rule) {
        this.storageService = storageService;
        this.context = context;
        this.rule = rule;

        Attributes fmi = context.getFileMetainfo();
        Attributes attributes = context.getAttributes();

        compressor = new Compressor(attributes, fmi.getString(Tag.TransferSyntaxUID),
                rule.getTransferSyntax(), rule.getImageWriteParams());
    }

    @Override
    public TaskType getTaskType() {
        return ImageProcessingTaskTypes.TRANSCODE_INCOMING;
    }

    @Override
    public long getEstimatedWeight() {
        return compressor.getEstimatedNeededMemory();
    }

    @Override
    public StorageContext call() throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        Attributes attributes = context.getAttributes();

        StorageSystem bulkdataStorage = session.getStorageSystem();
        StorageContext bulkdataContext = storageService.createStorageContext(bulkdataStorage);
        String bulkdataRoot = calculatePath(bulkdataStorage, attributes);
        String bulkdataPath = bulkdataRoot;
        int copies = 1;

        int bufferLength = bulkdataStorage.getBufferedOutputLength();
        OutputStream out = null;

        while (out == null) {
            try {
                out = storageService.openOutputStream(bulkdataContext, bulkdataPath);
            } catch (ObjectAlreadyExistsException e) {
                bulkdataPath = bulkdataRoot + '.' + copies++;
            } catch (Exception e) {
                throw new DicomServiceException(Status.UnableToProcess, e);
            }
        }

        try {
            String transferSyntax = rule.getTransferSyntax();
            compressor.compress();
            Attributes newfmi = attributes.createFileMetaInformation(transferSyntax);

            out = new BufferedOutputStream(out, bufferLength);
            out = new DicomOutputStream(out, UID.ExplicitVRLittleEndian);
            ((DicomOutputStream) out).writeDataset(newfmi, attributes);
            context.setTransferSyntax(transferSyntax);

        } catch (Exception e) {
            LOG.info("{} : compression failed", session, e);
            StorageContext storageContext = bulkdataContext;
            bulkdataContext = null;

            try {
                storageService.deleteObject(storageContext, bulkdataPath);
            } catch (IOException e1) {
                LOG.warn("{} : failed to delete compressed file : {}",
                        context.getStoreSession(), bulkdataPath, e);
            }

        } finally {
            try {
                SafeClose.close(out);
                SafeClose.close(compressor);
                if (bulkdataContext != null) {
                    bulkdataContext.setFilePath(Paths.get(bulkdataPath));
                    bulkdataContext.setFileSize(Files.size(Paths.get(bulkdataStorage.getStorageSystemPath(), bulkdataPath)));
                }

                // make sure to remove the reference to the compressed pixel data,
                // so that the memory can be freed, after the compression task has finished
                attributes.setNull(Tag.PixelData, VR.OB);

                // also the reference to the decompressor needs to be removed, so that it's memory can be freed
                compressor = null;

            } catch (IOException e) {
                throw new DicomServiceException(Status.UnableToProcess, e);
            }
        }
        return bulkdataContext;
    }

    private static String calculatePath(StorageSystem system, Attributes attributes) {
        String pattern = system.getStorageSystemGroup().getStorageFilePathFormat();
        AttributesFormat format = AttributesFormat.valueOf(pattern);
        return format.format(attributes);
    }
}
