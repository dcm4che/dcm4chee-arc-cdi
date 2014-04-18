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

package org.dcm4chee.archive.retrieve.scp;

import java.io.IOException;
import java.util.List;

import javax.enterprise.event.Event;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.DataWriterAdapter;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicRetrieveTask;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.dto.LocalAssociationParticipant;
import org.dcm4chee.archive.dto.RemoteAssociationParticipant;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.impl.RetrieveAfterSendEvent;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
class RetrieveTaskImpl extends BasicRetrieveTask<InstanceLocator> {


    private final RetrieveContext retrieveContext;
    private final boolean withoutBulkData;
    private Event<RetrieveAfterSendEvent> retrieveEvent;

    public RetrieveTaskImpl(Dimse rq, Association rqas,
            PresentationContext pc, Attributes rqCmd, List<InstanceLocator> matches,
            Association storeas, RetrieveContext retrieveContext, boolean withoutBulkData,
            Event<RetrieveAfterSendEvent> retrieveEvent) {
        super(rq, rqas, pc, rqCmd, matches, storeas);
        this.retrieveEvent = retrieveEvent;
        this.retrieveContext = retrieveContext;
        this.withoutBulkData = withoutBulkData;
    }

    @Override
    protected String selectTransferSyntaxFor(Association storeas, InstanceLocator inst) {
        if (storeas.getTransferSyntaxesFor(inst.cuid).contains(inst.tsuid))
            return inst.tsuid;
        
        return UID.ExplicitVRLittleEndian;
    }

    @Override
    protected DataWriter createDataWriter(InstanceLocator inst, String tsuid)
            throws IOException {
        Attributes attrs;
        DicomInputStream in = new DicomInputStream(inst.getFile());
        try {
            if (withoutBulkData) {
                in.setIncludeBulkData(IncludeBulkData.NO);
                attrs = in.readDataset(-1, Tag.PixelData);
            } else {
                in.setIncludeBulkData(IncludeBulkData.URI);
                attrs = in.readDataset(-1, -1);
            }
        } finally {
            SafeClose.close(in);
        }
        attrs.addAll((Attributes) inst.getObject());
        if (!tsuid.equals(inst.tsuid))
            Decompressor.decompress(attrs, inst.tsuid);

        retrieveContext.getRetrieveService().coerceRetrievedObject(
                retrieveContext, storeas.getRemoteAET(), attrs);
        return new DataWriterAdapter(attrs);
    }

    @Override
    protected void close() {
        super.close();
        
        retrieveEvent.fire(new RetrieveAfterSendEvent(
                new RemoteAssociationParticipant(rqas),
                new LocalAssociationParticipant(rqas), 
                new RemoteAssociationParticipant(storeas),
                rqas.getApplicationEntity().getDevice(),
                insts,
                completed,
                warning,
                failed));
    }

 }
