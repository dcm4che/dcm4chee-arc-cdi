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

import java.util.EnumSet;
import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCMoveSCP;
import org.dcm4che3.net.service.BasicRetrieveTask;
import org.dcm4che3.net.service.CStoreSCU;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.net.service.QueryRetrieveLevel;
import org.dcm4che3.net.service.RetrieveTask;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.dto.LocalAssociationParticipant;
import org.dcm4chee.archive.dto.Participant;
import org.dcm4chee.archive.dto.RemoteAssociationParticipant;
import org.dcm4chee.archive.retrieve.RetrieveContext;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.retrieve.impl.RetrieveAfterSendEvent;
import org.dcm4chee.archive.retrieve.impl.RetrieveBeforeSendEvent;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.dcm4chee.archive.store.scu.impl.CStoreSCUImpl;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class CMoveSCP extends BasicCMoveSCP {

    private final String[] qrLevels;
    private final QueryRetrieveLevel rootLevel;

    @Inject
    private RetrieveService retrieveService;
    
    @Inject
    private CStoreSCUService storescuService;

    @Inject
    private IApplicationEntityCache aeCache;
    
    @Inject
    private Event<RetrieveBeforeSendEvent> retrieveBeforeEvent;
    
    @Inject
    private Event<RetrieveAfterSendEvent> retrieveAfterEvent;
    
    public CMoveSCP(String sopClass, String... qrLevels) {
        super(sopClass);
        this.qrLevels = qrLevels;
        this.rootLevel = QueryRetrieveLevel.valueOf(qrLevels[0]);
    }

    @Override
    protected RetrieveTask calculateMatches(Association as, PresentationContext pc,
            final Attributes rq, Attributes keys) throws DicomServiceException {
        QueryRetrieveLevel level = QueryRetrieveLevel.valueOf(keys, qrLevels);
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        ExtendedNegotiation extNeg = as.getAAssociateAC().getExtNegotiationFor(cuid);
        EnumSet<QueryOption> queryOpts = QueryOption.toOptions(extNeg);
        boolean relational = queryOpts.contains(QueryOption.RELATIONAL);
        level.validateRetrieveKeys(keys, rootLevel, relational);
        String dest = rq.getString(Tag.MoveDestination);
        try {
            final ApplicationEntity destAE =
                    aeCache.findApplicationEntity(dest);
            ApplicationEntity ae = as.getApplicationEntity();
            ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
            QueryParam queryParam = arcAE.getQueryParam(queryOpts,
                    accessControlIDs());
//            ApplicationEntity sourceAE = aeCache.get(as.getRemoteAET());
//            if (sourceAE != null)
//                queryParam.setDefaultIssuer(sourceAE.getDevice());
//            IDWithIssuer pid = IDWithIssuer.fromPatientIDWithIssuer(keys);
//            if (pid != null && pid.getIssuer() == null)
//                pid.setIssuer(queryParam.getDefaultIssuerOfPatientID());
//            IDWithIssuer[] pids = Archive.getInstance().pixQuery(ae, pid);
//            IDWithIssuer[] pids = pid != null 
//                    ? new IDWithIssuer[]{ pid }
//                    : IDWithIssuer.EMPTY;
            RetrieveContext context = retrieveService.createRetrieveContext(
                    retrieveService, as.getRemoteAET(), arcAE);
            context.setDestinationAE(destAE);
            IDWithIssuer[] pids = retrieveService.queryPatientIDs(context, keys);
            List<ArchiveInstanceLocator> matches =
                     retrieveService.calculateMatches(pids, keys, queryParam, false);
            if (matches.isEmpty())
                return null;
            
            AAssociateRQ aarq = makeAAssociateRQ(as.getLocalAET(), dest, matches);
            Association storeas = openStoreAssociation(as, destAE, aarq);
            CStoreSCU<ArchiveInstanceLocator> cstorescu = new CStoreSCUImpl (
                    ae, destAE, storescuService);
            BasicRetrieveTask<ArchiveInstanceLocator> retrieveTask = new BasicRetrieveTask<ArchiveInstanceLocator>(
                    Dimse.C_MOVE_RQ, as, pc, rq, matches, storeas, cstorescu);
//            retrieveTask.setDestinationDevice(destAE.getDevice());
            retrieveTask.setSendPendingRSPInterval(arcAE.getSendPendingCMoveInterval());
//            retrieveTask.setReturnOtherPatientIDs(aeExt.isReturnOtherPatientIDs());
//            retrieveTask.setReturnOtherPatientNames(aeExt.isReturnOtherPatientNames());
            
            retrieveBeforeEvent.fire(new RetrieveBeforeSendEvent(
                    new RemoteAssociationParticipant(as),
                    new LocalAssociationParticipant(as), 
                    new GenericParticipant(Participant.UNKNOWN,destAE.getAETitle()),
                    ae.getDevice(),
                    matches));
            
            return retrieveTask;
        } catch (ConfigurationNotFoundException e) {
            throw new DicomServiceException(Status.MoveDestinationUnknown,
                    "Unknown Move Destination: " + dest);
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToCalculateNumberOfMatches, e);
        }
    }

    private Association openStoreAssociation(Association as,
            ApplicationEntity destAE, AAssociateRQ aarq) throws DicomServiceException {
        try {
            return as.getApplicationEntity().connect(destAE, aarq);
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToPerformSubOperations, e);
        }

    }

    private AAssociateRQ makeAAssociateRQ(String callingAET, String calledAET,
            List<ArchiveInstanceLocator> matches) {
        AAssociateRQ aarq = new AAssociateRQ();
        aarq.setCalledAET(calledAET);
        aarq.setCallingAET(callingAET);
        for (InstanceLocator match : matches) {
            if (aarq.addPresentationContextFor(match.cuid, match.tsuid)) {
                if (!UID.ExplicitVRLittleEndian.equals(match.tsuid))
                    aarq.addPresentationContextFor(match.cuid, UID.ExplicitVRLittleEndian);
                if (!UID.ImplicitVRLittleEndian.equals(match.tsuid))
                    aarq.addPresentationContextFor(match.cuid, UID.ImplicitVRLittleEndian);
            }
        }
        return aarq;
    }

    private String[] accessControlIDs() {
        // TODO Auto-generated method stub
        return null;
    }
}
