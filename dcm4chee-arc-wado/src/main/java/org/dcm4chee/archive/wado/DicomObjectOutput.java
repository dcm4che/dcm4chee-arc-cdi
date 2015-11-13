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

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.StreamingOutput;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4chee.task.ImageProcessingTaskTypes;
import org.dcm4chee.task.MemoryConsumingTask;
import org.dcm4chee.task.TaskType;
import org.dcm4chee.task.WeightWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback object used by the RESTful runtime when ready
 * to write the response (the method write is invoked).
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

    private final Attributes dataset;
    private String sourceTransferSyntaxUID;
    private final String targetTransferSyntaxUID;
    private final WeightWatcher weightWatcher;

    DicomObjectOutput(Attributes dataset, String sourceTransferSyntaxUID, String targetTransferSyntaxUID, WeightWatcher weightWatcher) {
        this.sourceTransferSyntaxUID = sourceTransferSyntaxUID;
        this.dataset = dataset;
        this.targetTransferSyntaxUID = targetTransferSyntaxUID;
        this.weightWatcher = weightWatcher;
    }

    public void write(OutputStream out) throws IOException {
        try {
            weightWatcher.execute(new WriteDicomObjectTask(dataset, sourceTransferSyntaxUID, targetTransferSyntaxUID, out));
        } catch (Exception e) {
            if (e instanceof IOException)
                throw (IOException) e;
            else if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw new RuntimeException(e); // should not happen
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
