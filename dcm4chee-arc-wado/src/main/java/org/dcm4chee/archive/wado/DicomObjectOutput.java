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
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback object used by the RESTful runtime when ready
 * to write the response (the method write is invoked).
 * 
 * The write method reads the referenced file in the file
 * system and eventually updates it with attributes than
 * in the meanwhile may have changed.
 * 
 * Bulk Data is not loaded in memory, but only an URI reference
 * to it. It is read only at stream time.
 * 
 * If the requested Transfer Syntax UID is different to 
 * the one used to store the file, the data is decompressed
 * and returned as is.
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
class DicomObjectOutput implements StreamingOutput {

    private static final Logger LOG = LoggerFactory.getLogger(DicomObjectOutput.class);

    private ArchiveInstanceLocator fileRef;
    private final Attributes attrs;
    private final String tsuid;
    private CStoreSCUContext context;
    private CStoreSCUService service;
    
    DicomObjectOutput(ArchiveInstanceLocator fileRef, Attributes attrs, String tsuid,
            CStoreSCUContext ctx, CStoreSCUService srv) {
        this.fileRef = fileRef;
        this.attrs = attrs;
        this.tsuid = tsuid;
        this.context = ctx;
        this.service = srv;
    }

    public void write(OutputStream out) throws IOException {
        ArchiveInstanceLocator inst = fileRef;
        Attributes attrs = null;
        do {
            try {
                attrs = readFrom(inst);
            } catch (IOException e) {
                LOG.info("Failed to read Data Set with iuid={} from {}@{}",
                        inst.iuid, inst.getFilePath(), inst.getStorageSystem(), e);
                inst = inst.getFallbackLocator();
                if (inst == null)
                    throw e;
                LOG.info("Try read Data Set from alternative location");
            }
        } while (attrs == null);

        if(context.getRemoteAE()!=null){
            service.coerceFileBeforeMerge(inst, attrs, context);
            service.coerceAttributes(attrs, context);
        }
        attrs.addAll(attrs);
        if (tsuid != inst.tsuid) {
            Decompressor.decompress(attrs, inst.tsuid);
        }
        Attributes fmi = attrs.createFileMetaInformation(tsuid);
        @SuppressWarnings("resource")
        DicomOutputStream dos = new DicomOutputStream(out,
                UID.ExplicitVRLittleEndian);
        dos.writeDataset(fmi, attrs);
    }

    private Attributes readFrom(ArchiveInstanceLocator inst) throws IOException {
        try (DicomInputStream din = new DicomInputStream(service.getFile(inst)
                .toFile())) {
            IncludeBulkData includeBulkData = IncludeBulkData.URI;
            din.setIncludeBulkData(IncludeBulkData.URI);
            return din.readDataset(-1, -1);
        }
    }
}
