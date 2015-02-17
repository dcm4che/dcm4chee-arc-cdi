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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

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
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicRetrieveTask;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.dto.LocalAssociationParticipant;
import org.dcm4chee.archive.dto.RemoteAssociationParticipant;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.retrieve.impl.ArchiveInstanceLocator;
import org.dcm4chee.archive.retrieve.impl.RetrieveAfterSendEvent;
import org.dcm4chee.archive.retrieve.impl.UnsupportedRetrieveException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
class RetrieveTaskImpl extends BasicRetrieveTask<ArchiveInstanceLocator> {

    private final RetrieveContext retrieveContext;
    private final boolean withoutBulkData;
    private Event<RetrieveAfterSendEvent> retrieveEvent;

    public RetrieveTaskImpl(Dimse rq, Association rqas, PresentationContext pc,
            Attributes rqCmd, List<ArchiveInstanceLocator> matches,
            Association storeas, RetrieveContext retrieveContext,
            boolean withoutBulkData, Event<RetrieveAfterSendEvent> retrieveEvent) {
        super(rq, rqas, pc, rqCmd, matches, storeas);
        this.retrieveEvent = retrieveEvent;
        this.retrieveContext = retrieveContext;
        this.withoutBulkData = withoutBulkData;
    }

    @Override
    protected String selectTransferSyntaxFor(Association storeas,
            ArchiveInstanceLocator inst) throws UnsupportedRetrieveException {
        ArchiveAEExtension arcAEExt = retrieveContext.getArchiveAEExtension();
        RetrieveService retrieveService = retrieveContext.getRetrieveService();
        Set<String> acceptedTransferSyntax = storeas.getTransferSyntaxesFor(inst.cuid);
        // check for SOP classes elimination
        if (arcAEExt.getRetrieveSuppressionCriteria()
                .isCheckTransferCapabilities()){
            inst = retrieveService.eliminateUnSupportedSOPClasses(inst, retrieveContext);
        //check if eliminated then throw exception
        if(inst == null)
            throw new UnsupportedRetrieveException("Unable to retrieve instance, SOP class not configured");
        
        if(isConfiguredAndAccepted(inst, storeas.getTransferSyntaxesFor(inst.cuid)))
            return inst.tsuid;
        else
            return getDefaultConfiguredTransferSyntax(inst);
        }
        
            if (acceptedTransferSyntax.contains(inst.tsuid))
                return inst.tsuid;
            
            return storeas.getTransferSyntaxesFor(inst.cuid).contains(
                    UID.ExplicitVRLittleEndian) ? UID.ExplicitVRLittleEndian
                    : UID.ImplicitVRLittleEndian;
    }

    @Override
    protected DataWriter createDataWriter(ArchiveInstanceLocator inst,
            String tsuid) throws IOException, UnsupportedRetrieveException {
        ArchiveAEExtension arcAE = retrieveContext.getArchiveAEExtension();
        RetrieveService retrieveService = retrieveContext.getRetrieveService();
        TimeZone archiveTimeZone = arcAE.getApplicationEntity().getDevice()
                .getTimeZoneOfDevice();
        Attributes attrs = null;

        if(inst == null)
            throw new UnsupportedRetrieveException("Unable to retrieve instance, Suppressed by Attributes");
        
        attrs = getStreamOrRetryOtherLocators(inst, retrieveService, attrs,inst.getOtherLocators());
        
        // check for suppression criteria
        Map<String, String> suppressionCriteriaMap = arcAE
                .getRetrieveSuppressionCriteria().getSuppressionCriteriaMap();
        if (suppressionCriteriaMap.containsKey(retrieveContext.getDestinationAE()!=null?retrieveContext.getDestinationAE().getAETitle():null)) {
            String supressionCriteriaTemplateURI = suppressionCriteriaMap
                    .get(retrieveContext.getDestinationAE().getAETitle());
            if (supressionCriteriaTemplateURI != null) {
                inst = retrieveService.applySuppressionCriteria(inst, attrs,
                        supressionCriteriaTemplateURI, retrieveContext);
            }
        }

        if (archiveTimeZone != null) {
            ArchiveInstanceLocator archInst = (ArchiveInstanceLocator) inst;
            retrieveContext.getRetrieveService().coerceFileBeforeMerge(
                    archInst, retrieveContext, storeas.getRemoteAET(), attrs);
        }
        attrs = Utils.mergeAndNormalize(attrs,(Attributes) inst.getObject());
        if (!tsuid.equals(inst.tsuid))
            Decompressor.decompress(attrs, inst.tsuid);

        retrieveContext.getRetrieveService().coerceRetrievedObject(
                retrieveContext, storeas.getRemoteAET(), attrs);
        return new DataWriterAdapter(attrs);
    }

    private Attributes getStreamOrRetryOtherLocators(ArchiveInstanceLocator inst,
            RetrieveService retrieveService, Attributes attrs, Collection<ArchiveInstanceLocator> fallBackLocations) {
        DicomInputStream in=null;
        try{
        in = new DicomInputStream(
                retrieveService.getFile(inst).toFile());
        
            if (withoutBulkData) {
                in.setIncludeBulkData(IncludeBulkData.NO);
                attrs = in.readDataset(-1, Tag.PixelData);
            } else {
                in.setIncludeBulkData(IncludeBulkData.URI);
                attrs = in.readDataset(-1, -1);
            }
        }
        catch(IOException e) {
            //fallback
            if(inst.getOtherLocators()!=null) {
            
            Collections.sort((List<ArchiveInstanceLocator>)fallBackLocations,
                    new Comparator<ArchiveInstanceLocator>() {
                        @Override
                        public int compare(ArchiveInstanceLocator a,
                                ArchiveInstanceLocator b) {
                            return a.getStorageSystem().getStorageAccessTime() < b
                                    .getStorageSystem().getStorageAccessTime() ? -1
                                    : a.getStorageSystem()
                                            .getStorageAccessTime() == b
                                            .getStorageSystem()
                                            .getStorageAccessTime() ? 0 : 1;
                        }
                    });
            for(Iterator<ArchiveInstanceLocator> iter = fallBackLocations.iterator();iter.hasNext();) {
                ArchiveInstanceLocator fallBackLocation = iter.next();
                iter.remove();
                return getStreamOrRetryOtherLocators(fallBackLocation, retrieveService, attrs,fallBackLocations);
            }
        }
        }
        finally {
            SafeClose.close(in);
        }
        return attrs;
    }

    @Override
    protected void close() {
        super.close();

        retrieveEvent.fire(new RetrieveAfterSendEvent(
                new RemoteAssociationParticipant(rqas),
                new LocalAssociationParticipant(rqas),
                new RemoteAssociationParticipant(storeas), rqas
                        .getApplicationEntity().getDevice(), insts, completed,
                warning, failed));
    }

    private boolean isConfiguredAndAccepted(InstanceLocator ref, Set<String> negotiated) {
        ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                retrieveContext.getDestinationAE().getTransferCapabilitiesWithRole(
                        Role.SCU));
            for (TransferCapability supportedTC : aeTCs){
                if (ref.cuid.compareTo(supportedTC.getSopClass()) == 0 && 
                        supportedTC.containsTransferSyntax(ref.tsuid) && negotiated.contains(ref.tsuid)) {
                    return true;
                }
            }
            return false;
    }

    private String getDefaultConfiguredTransferSyntax(InstanceLocator ref)
    {
        ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                retrieveContext.getDestinationAE().getTransferCapabilitiesWithRole(
                        Role.SCU));
        for (TransferCapability supportedTC : aeTCs){
            if (ref.cuid.compareTo(supportedTC.getSopClass()) == 0) {
                return supportedTC.containsTransferSyntax(UID.ExplicitVRLittleEndian)?
                        UID.ExplicitVRLittleEndian:UID.ImplicitVRLittleEndian;
            }
        }
        return UID.ImplicitVRLittleEndian;
    }
}
