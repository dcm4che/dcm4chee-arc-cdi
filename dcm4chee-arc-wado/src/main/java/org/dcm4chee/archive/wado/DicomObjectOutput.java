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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
package org.dcm4chee.archive.wado;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.dcm4chee.archive.task.ImageProcessingTaskTypes;
import org.dcm4chee.archive.task.MemoryConsumingTask;
import org.dcm4chee.archive.task.TaskType;
import org.dcm4chee.archive.task.WeightWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Callback object used by the RESTful runtime when ready
 * to write the response (the method write is invoked).
 * <p>
 * The write method reads the referenced file in the file
 * system and eventually updates it with attributes than
 * in the meanwhile may have changed.
 * <p>
 * Bulk Data is not loaded in memory, but only an URI reference
 * to it. It is read only at stream time.
 * <p>
 * If the requested Transfer Syntax UID is different to
 * the one used to store the file, the data is returned
 * decompressed.
 *
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
class DicomObjectOutput implements StreamingOutput {

    private static final Logger LOG = LoggerFactory.getLogger(DicomObjectOutput.class);

    private ArchiveInstanceLocator fileRef;
    private final Attributes attrs;
    private final String tsuid;
    private CStoreSCUContext context;
    private CStoreSCUService service;
    private final WeightWatcher weightWatcher;

    DicomObjectOutput(ArchiveInstanceLocator fileRef, Attributes attrs, String tsuid,
                      CStoreSCUContext ctx, CStoreSCUService srv, WeightWatcher weightWatcher) {
        this.fileRef = fileRef;
        this.attrs = attrs;
        this.tsuid = tsuid;
        this.context = ctx;
        this.service = srv;
        this.weightWatcher = weightWatcher;
    }

    public void write(OutputStream out) throws IOException {
        ArchiveInstanceLocator inst = fileRef;
        Attributes dataset = null;
        do {
            try {
                dataset = readFrom(inst);
            } catch (IOException e) {
                LOG.info("Failed to read Data Set with iuid={} from {}@{}",
                        inst.iuid, inst.getFilePath(), inst.getStorageSystem(), e);
                inst = inst.getFallbackLocator();
                if (inst == null)
                    throw e;
                LOG.info("Try read Data Set from alternative location");
            }
        } while (dataset == null);

        if (context.getRemoteAE() != null) {
            service.coerceFileBeforeMerge(inst, dataset, context);
        }
        dataset = Utils.mergeAndNormalize(dataset, attrs);
        if (context.getRemoteAE() != null) {
            service.coerceAttributes(dataset, context);
        }

        try {
            weightWatcher.execute(new WriteDicomObjectTask(dataset, inst.tsuid, tsuid, out));
        } catch (Exception e) {
            if (e instanceof IOException)
                throw (IOException) e;
            else if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw new RuntimeException(e); // should not happen
        }
    }

    private Attributes readFrom(ArchiveInstanceLocator inst) throws IOException {
        try (DicomInputStream din = new DicomInputStream(service.getFile(inst)
                .toFile())) {
            din.setIncludeBulkData(IncludeBulkData.URI);
            return din.readDataset(-1, -1);
        }
    }

    private static class WriteDicomObjectTask implements MemoryConsumingTask<Void> {

        private Attributes dataset;
        private String targetTransferSyntaxUID;
        private OutputStream out;
        private Decompressor decompressor;

        public WriteDicomObjectTask(Attributes dataset, String sourceTransferSyntaxUID,
                                    String targetTransferSyntaxUID, OutputStream out) {

            if (!sourceTransferSyntaxUID.equals(targetTransferSyntaxUID) &&
                    !TransferSyntaxType.NATIVE.equals(TransferSyntaxType.forUID(targetTransferSyntaxUID))) {
                throw new IllegalArgumentException("Only same or uncompressed target TransferSyntaxUID is supported");
            }

            this.dataset = dataset;
            this.targetTransferSyntaxUID = targetTransferSyntaxUID;
            this.out = out;

            if (!targetTransferSyntaxUID.equals(sourceTransferSyntaxUID))
                decompressor = new Decompressor(dataset, sourceTransferSyntaxUID);
            else
                decompressor = null;
        }

        @Override
        public TaskType getTaskType() {
            return ImageProcessingTaskTypes.TRANSCODE_OUTGOING;
        }

        @Override
        public long getEstimatedWeight() {
            if (decompressor != null)
                return decompressor.getEstimatedNeededMemory();
            else
                return 0;
        }

        @Override
        public Void call() throws IOException {
            try {
                if (decompressor != null) {
                    decompressor.decompress();
                }

                Attributes fmi = dataset.createFileMetaInformation(targetTransferSyntaxUID);
                @SuppressWarnings("resource")
                DicomOutputStream dos = new DicomOutputStream(out, UID.ExplicitVRLittleEndian);
                dos.writeDataset(fmi, dataset);

                // nullify pixeldata so that memory can be freed before the task ends
                dataset.setNull(Tag.PixelData, VR.OW);
            } finally {
                if (decompressor != null) {
                    decompressor.dispose();

                    // also remove reference to decompressor, to be able to free the memory
                    decompressor = null;
                }
            }

            return null;
        }
    }
}
