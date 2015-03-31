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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.stream.StreamResult;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DicomXMLOutput implements StreamingOutput {

    private final ArchiveInstanceLocator fileRef;
    private final Attributes attrs;
    private final String bulkDataURI;
    private CStoreSCUContext context;
    private CStoreSCUService service;
    
    public DicomXMLOutput(ArchiveInstanceLocator fileRef, String bulkDataURI,
            Attributes attrs, CStoreSCUContext ctx,
            CStoreSCUService srv) {
        this.fileRef = fileRef;
        this.bulkDataURI = bulkDataURI;
        this.attrs = attrs;
        this.context = ctx;
        this.service = srv;
    }

    @Override
    public void write(OutputStream out) throws IOException,
            WebApplicationException {
        DicomInputStream dis = new DicomInputStream(
                service.getFile(fileRef).toFile());
        dis.setURI(bulkDataURI);
        try {
            dis.setIncludeBulkData(IncludeBulkData.URI);
            Attributes dataset = dis.readDataset(-1, -1);
            
            if (context.getRemoteAE() != null) {
                service.coerceFileBeforeMerge((ArchiveInstanceLocator) fileRef,
                        dataset, context);

                service.coerceAttributes(dataset, context);
            }
            
            dataset.addAll(attrs);
            Object pixelData = dataset.getValue(Tag.PixelData);
            if (pixelData instanceof Fragments) {
                Fragments frags = (Fragments) pixelData;
                dataset.setValue(Tag.PixelData, VR.OB,
                        new BulkData(((BulkData) frags.get(1))
                                .uriWithoutQuery(), 0, -1,
                                dataset.bigEndian()));
            }
            SAXTransformer.getSAXWriter(new StreamResult(out)).write(dataset);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        } finally {
            SafeClose.close(dis);
        }
    }

}
