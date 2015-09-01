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
package org.dcm4chee.archive.store.verify.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.Service;
import org.dcm4chee.archive.dto.ServiceQualifier;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.StoreVerifyDimse;
import org.dcm4chee.archive.entity.StoreVerifyStatus;
import org.dcm4chee.archive.entity.StoreVerifyWeb;
import org.dcm4chee.archive.qido.client.QidoClientService;
import org.dcm4chee.archive.qido.client.QidoContext;
import org.dcm4chee.archive.qido.client.QidoResponse;
import org.dcm4chee.archive.stgcmt.scp.CommitEvent;
import org.dcm4chee.archive.stgcmt.scp.StgCmtService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUResponse;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.dcm4chee.archive.store.verify.StoreVerifyEJB;
import org.dcm4chee.archive.store.verify.StoreVerifyResponse;
import org.dcm4chee.archive.store.verify.StoreVerifyResponse.VerifiedInstanceStatus;
import org.dcm4chee.archive.store.verify.StoreVerifyService;
import org.dcm4chee.archive.stow.client.StowClientService;
import org.dcm4chee.archive.stow.client.StowContext;
import org.dcm4chee.archive.stow.client.StowResponse;
import org.dcm4chee.storage.conf.Availability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
@ApplicationScoped
public class StoreVerifyServiceImpl implements StoreVerifyService {
    private static final Logger LOG = LoggerFactory.getLogger(StoreVerifyServiceImpl.class);
    
    @Inject
    private QidoClientService qidoService;

    @Inject
    private StowClientService stowService;

    @Inject
    private CStoreSCUService storeSCUService;

    @Inject 
    private StgCmtService stgCmtService;

    @Inject
    private StoreVerifyEJB ejb;

    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    @Any
    private Event<StoreVerifyResponse> storeVerifyResponseEvent;

    @Override
    public void scheduleStore(String transactionUID, StowContext context, List<ArchiveInstanceLocator> insts) {
        // create entity and set to pending
        if(transactionUID == null) {
            transactionUID = generateTransactionUID(false);
        }
        ejb.addWebEntry(transactionUID, context.getQidoRemoteBaseURL(), 
                context.getRemoteAE().getAETitle()
                , context.getLocalAE().getAETitle()
                , context.getService());
        context.setService(ServiceType.STOREVERIFY);
        stowService.scheduleStow(transactionUID, context, insts, 1, 1, 0);
    }
    
    @Override
    public void store(String transactionUID, StowContext context, List<ArchiveInstanceLocator> insts) {
        if(transactionUID == null) {
            transactionUID = generateTransactionUID(false);
        }
        // create entity and set to pending
        ejb.addWebEntry(transactionUID, context.getQidoRemoteBaseURL(), 
                context.getRemoteAE().getAETitle()
                , context.getLocalAE().getAETitle()
                , context.getService());
        context.setService(ServiceType.STOREVERIFY);
        StowResponse stowResp = stowService.createStowRSClient(stowService,context).storeOverWebService(transactionUID, insts);
        // we are notifying ourself
        stowService.notify(context, stowResp);
    }

    @Override
    public void scheduleStore(String transactionUID, CStoreSCUContext context, List<ArchiveInstanceLocator> insts) {
        String localAET = context.getLocalAE().getAETitle();
        String remoteAET = context.getRemoteAE().getAETitle();
        ServiceType service = context.getService();
        
        if(transactionUID == null) {
            transactionUID = generateTransactionUID(true);
        }
        ejb.addDimseEntry(transactionUID, remoteAET, localAET, service);
        context.setService(ServiceType.STOREVERIFY);
        storeSCUService.scheduleStoreSCU(transactionUID, context,
                insts, 1, 1, 0);
    }
    
    @Override
    public void store(String transactionUID, CStoreSCUContext context, List<ArchiveInstanceLocator> insts) {
        String localAET = context.getLocalAE().getAETitle();
        String remoteAET = context.getRemoteAE().getAETitle();
        ServiceType service = context.getService();
        
        if(transactionUID == null) {
            transactionUID = generateTransactionUID(true);
        }
        
        ejb.addDimseEntry(transactionUID, remoteAET, localAET, service);
        context.setService(ServiceType.STOREVERIFY);
        try {
            storeSCUService.cstore(transactionUID, context, insts, 1);
        } catch (DicomServiceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * CDI Event receiver method for response of STOW.
     * 
     * @param storeResponse
     */
    public void doQidoVerify(@Observes @Service(ServiceType.STOREVERIFY) StowResponse storeResponse) {
        if (LOG.isDebugEnabled()) {
            if (!storeResponse.getSuccessfulSopInstances().isEmpty()) {
                LOG.debug("STOW SUCCESSFUL for the following instances:");
                for (String sopUID : storeResponse.getSuccessfulSopInstances()) {
                    LOG.debug(sopUID);
                }
            }
            if (!storeResponse.getFailedSopInstances().isEmpty()) {
                LOG.debug("STOW FAILED for the following instances:");
                for (String sopUID : storeResponse.getFailedSopInstances()) {
                    LOG.debug(sopUID);
                }
            }
        }
        
        String transactionUID = storeResponse.getTransactionID();
        List<String> toVerify = new ArrayList<String>(storeResponse.getSuccessfulSopInstances());
        /*
         * also add instance whose storage failed to verify -> verification will of course fail for this instances
         * -> easier to manage
         */
        toVerify.addAll(storeResponse.getFailedSopInstances());
        StoreVerifyWeb webEntry = ejb.getWebEntry(transactionUID);
        String localAET = webEntry.getLocalAET();
        String remoteAET = webEntry.getRemoteAET();
        
        QidoContext ctx = null;
        try {
            ApplicationEntity archiveAE = aeCache.findApplicationEntity(localAET);
            ApplicationEntity remoteAE = aeCache.findApplicationEntity(remoteAET);
            ctx = new QidoContext(archiveAE, remoteAE);
        } catch (ConfigurationException e) {
            LOG.error("Unable to find Application"
                    + " Entity for {} or {} verification failure for "
                    + "store and remember trabnsaction {}",
                    localAET, remoteAET,
                    transactionUID);
            ejb.removeWebEntry(transactionUID);
            return;
        }
        // set the qido url
        ctx.setRemoteBaseURL(webEntry.getQidoBaseURL());
        //set transactionid
        ctx.setTransactionID(transactionUID);
        QidoResponse qidoResponse = qidoService.verifyStorage(qidoService.createQidoClient(ctx), toVerify);
        
        Map<String,VerifiedInstanceStatus> verifiedInstances = new HashMap<>();
        for(Entry<String,Availability> inst : qidoResponse.getVerifiedSopInstances().entrySet()) {
            verifiedInstances.put(inst.getKey(), new VerifiedInstanceStatus(inst.getValue(), null));
        }
        
        String retrieveAET = remoteAET;

        StoreVerifyStatus status = determineAndPersistStoreVerifyStatus(transactionUID, verifiedInstances);
        StoreVerifyResponse storeVerifyResponse = new StoreVerifyResponse(status, STORE_VERIFY_PROTOCOL.STOW_PLUS_QIDO, transactionUID, localAET, 
                remoteAET, retrieveAET, verifiedInstances);
        
        ejb.removeWebEntry(transactionUID);
        
        storeVerifyResponseEvent.select(new ServiceQualifier(ServiceType.valueOf(webEntry.getService()))).fire(storeVerifyResponse);
    }
    
    private StoreVerifyStatus determineAndPersistStoreVerifyStatus(String transactionUID, Map<String,VerifiedInstanceStatus> verifiedSopInstances) {
        int numVerified = 0;
        for(VerifiedInstanceStatus instStatus : verifiedSopInstances.values()) {
            Availability extAvailability = instStatus.getAvailability();
            if (Availability.ONLINE.equals(extAvailability) || Availability.NEARLINE.equals(extAvailability)) {
                numVerified++;
            }
        }
        
        StoreVerifyStatus status = null;
        if(numVerified < verifiedSopInstances.size()) {
            status = (numVerified == 0) ? StoreVerifyStatus.FAILED: StoreVerifyStatus.INCOMPLETE;
        } else {
            status = StoreVerifyStatus.VERIFIED;
        }
        
        ejb.updateStatus(transactionUID, status);
        
        return status;
    }

    /**
     * CDI Event receiver method for response of CStoreSCU
     * @param storeResponse
     */
    public void doStorageCmtVerify(@Observes @Service(ServiceType.STOREVERIFY) CStoreSCUResponse storeResponse) {
        String transactionUID = storeResponse.getMessageID();
        String localAET = storeResponse.getLocalAET();
        String remoteAET = storeResponse.getRemoteAET();

        /*
         * Contains also the instances whose storage has failed
         */
        List<ArchiveInstanceLocator> insts = storeResponse.getInstances();

        StgCmtService.N_ACTION_REQ_STATE stgCmtReqState = stgCmtService.sendNActionRequest(
                localAET, remoteAET, insts, transactionUID);
        
        // sending of the N-Action request has failed 
        if(!StgCmtService.N_ACTION_REQ_STATE.SEND_REQ_OK.equals(stgCmtReqState)) {
            Map<String,VerifiedInstanceStatus> verifiedInstances = new HashMap<>();
            for(ArchiveInstanceLocator inst : insts) {
                verifiedInstances.put(inst.iuid, new VerifiedInstanceStatus(Availability.UNAVAILABLE, null));
            }
            
            StoreVerifyStatus status = determineAndPersistStoreVerifyStatus(transactionUID, verifiedInstances);
            StoreVerifyResponse storeVerifyResponse = new StoreVerifyResponse(status, 
                    STORE_VERIFY_PROTOCOL.CSTORE_PLUS_STGCMT, 
                    transactionUID, localAET, 
                    remoteAET, null, verifiedInstances);
            
            String service = ejb.getDimseEntry(transactionUID).getService();
            ejb.removeDimseEntry(transactionUID);
            
            storeVerifyResponseEvent.select(new ServiceQualifier(ServiceType.valueOf(service))).fire(storeVerifyResponse);
        }
        
    }
    
    /**
     *  CDI event receiver method for response from StgCmt
     * @param commitEvent
     */
    public void onStorageCommitmentResponse(@Observes CommitEvent commitEvent) {
        String transactionUID = commitEvent.getTransactionUID();
        String localAET = commitEvent.getLocalAET();
        String remoteAET = commitEvent.getRemoteAET();
        
        StoreVerifyDimse dimseEntry = ejb.getDimseEntry(transactionUID);
        if(dimseEntry == null) {
            LOG.error("Received storage commitment response for unknown transaction, transaction UID: {}", transactionUID);
            return;
        }
        
        Attributes eventInfo = commitEvent.getEventInfo();
        String retrieveAET = eventInfo.getString(Tag.RetrieveAETitle);
       
        Map<String,VerifiedInstanceStatus> verifiedInstances = new HashMap<>();
        
        Sequence refSops = eventInfo.getSequence(Tag.ReferencedSOPSequence);
        if (refSops != null) {
            for (int i = 0; i < refSops.size(); i++) {
                Attributes item = refSops.get(i);
                String sopInstanceUID = item.getString(Tag.ReferencedSOPInstanceUID);
                String instanceRetrieveAET = (retrieveAET != null) ? retrieveAET : item.getString(Tag.RetrieveAETitle);
                verifiedInstances.put(sopInstanceUID, new VerifiedInstanceStatus(Availability.NEARLINE, instanceRetrieveAET));
            }
        }
        
        Sequence failSops = eventInfo.getSequence(Tag.FailedSOPSequence);
        if (failSops != null) {
            for (int i = 0; i < failSops.size(); i++) {
                Attributes item = failSops.get(i);
                String sopInstanceUID = item.getString(Tag.ReferencedSOPInstanceUID);
                verifiedInstances.put(sopInstanceUID, new VerifiedInstanceStatus(Availability.UNAVAILABLE, null));
            }
        }
        
        StoreVerifyStatus status = determineAndPersistStoreVerifyStatus(transactionUID, verifiedInstances);
        StoreVerifyResponse storeVerifyResponse = new StoreVerifyResponse(status, STORE_VERIFY_PROTOCOL.CSTORE_PLUS_STGCMT, transactionUID, localAET, 
                remoteAET, retrieveAET, verifiedInstances);
        
        ejb.removeDimseEntry(transactionUID);
        
        storeVerifyResponseEvent.select(new ServiceQualifier(ServiceType.valueOf(dimseEntry.getService()))).fire(storeVerifyResponse);
    }
    
    @Override
    public String generateTransactionUID(boolean dimse) {
        return dimse ? "dimse-" + UUID.randomUUID().toString() : "web-"
                + UUID.randomUUID().toString();
    }

}
