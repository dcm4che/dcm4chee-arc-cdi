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
import java.net.URI;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.json.JSONWriter;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class DicomJSONOutput implements StreamingOutput {

    private static final Logger LOG = LoggerFactory.getLogger(DicomObjectOutput.class);

    private final List<ArchiveInstanceLocator> refs;
    private String bulkDataURI;
    private CStoreSCUContext context;
    private CStoreSCUService service;
    private UriInfo uriInfo;
    private String aeTitle;

    public DicomJSONOutput(String aeTitle, UriInfo uriInfo,
            List<ArchiveInstanceLocator> refs, CStoreSCUContext ctx,
            CStoreSCUService srv) {
        this.refs = refs;
        this.context = ctx;
        this.service = srv;
        this.aeTitle = aeTitle;
        this.uriInfo = uriInfo;
    }

    @Override
    public void write(OutputStream out) throws IOException,
            WebApplicationException {
        JsonGenerator gen = Json.createGenerator(out);
        JSONWriter writer = new JSONWriter(gen);
        gen.writeStartArray();

        for (ArchiveInstanceLocator ref : refs) {
            Attributes dataset = null;
            do {
                try {
                    dataset = readFrom(ref);
                } catch (IOException e) {
                    LOG.info("Failed to read Data Set with iuid={} from {}@{}",
                            ref.iuid, ref.getFilePath(), ref.getStorageSystem(), e);
                    ref = ref.getFallbackLocator();
                    if (ref == null)
                        throw e;
                    LOG.info("Try read Data Set from alternative location");
                }
            } while (dataset == null);

            if (context.getRemoteAE() != null) {
                service.coerceFileBeforeMerge(ref, dataset, context);
            }
            dataset = Utils.mergeAndNormalize(dataset, (Attributes) ref.getObject());
            if (context.getRemoteAE() != null) {
                service.coerceAttributes(dataset, context);
            }

            DefaultWadoRS.replacePixelDataBulkDataURI(ref, dataset);

            writer.write(dataset);
        }
        gen.writeEnd();
        gen.flush();
    }

    private String toBulkDataURI(String uri) {
        return uriInfo.getBaseUri() + aeTitle + "/bulkdata/"
                + URI.create(uri).getPath();
    }

    private Attributes readFrom(ArchiveInstanceLocator inst) throws IOException {
        try (DicomInputStream din = new DicomInputStream(service.getFile(inst)
                .toFile())) {
            bulkDataURI = toBulkDataURI(inst.uri);
            din.setURI(bulkDataURI);
            din.setIncludeBulkData(IncludeBulkData.URI);
            return din.readDataset(-1, -1);
        }
    }
}
