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
package org.dcm4chee.archive.store.remember.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.Service;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.StoreVerifyStatus;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.store.remember.StoreAndRememberContext;
import org.dcm4chee.archive.store.remember.StoreAndRememberResponse;
import org.dcm4chee.archive.store.remember.StoreAndRememberService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.verify.StoreVerifyResponse;
import org.dcm4chee.archive.store.verify.StoreVerifyResponse.VerifiedInstanceStatus;
import org.dcm4chee.archive.store.verify.StoreVerifyService.STORE_VERIFY_PROTOCOL;
import org.dcm4chee.archive.store.verify.StoreVerifyService;
import org.dcm4chee.archive.stow.client.StowContext;
import org.dcm4chee.storage.conf.Availability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
@ApplicationScoped
public class StoreAndRememberServiceImpl implements StoreAndRememberService {
    private static final Logger LOG = LoggerFactory.getLogger(StoreAndRememberServiceImpl.class);

    @Inject
    private IApplicationEntityCache aeCache;
    
    @Inject
    private DicomConfiguration conf;
    
    @Inject
    private StoreVerifyService storeVerifyService;
    
    @Inject
    private RetrieveService retrieveService;
    
    @Inject
    private Device device;
    
    @Inject
    private StoreAndRememberEJB storeRememberEJB;
    
    @Inject
    private Event<StoreAndRememberResponse> storeRememberResponse;
   
    public void storeAndRemember(StoreAndRememberContext ctx) {
        ApplicationEntity remoteAE = getRemoteAE(ctx);
        ApplicationEntity localAE = getLocalAE();
        if (localAE == null || remoteAE == null) {
            return;
        }

        List<ArchiveInstanceLocator> insts = locate(ctx.getInstances());
        
        boolean isDimseStoreVerify = STORE_VERIFY_PROTOCOL.CSTORE_PLUS_STGCMT.equals(ctx.getStoreVerifyProtocol());
        String storeVerifyTxUID = storeVerifyService.generateTransactionUID(isDimseStoreVerify);
        if(ctx.getTransactionUID() == null) {
            createStoreRememberTransaction(ctx, storeVerifyTxUID);
        } else {
            updateStoreRememberTransaction(ctx, storeVerifyTxUID);
        }
        
        switch(ctx.getStoreVerifyProtocol()) {
        case CSTORE_PLUS_STGCMT:
            CStoreSCUContext cxt = new CStoreSCUContext(localAE, remoteAE, ServiceType.STOREREMEMBER);
            storeVerifyService.store(storeVerifyTxUID, cxt, insts);
            break;
        case STOW_PLUS_QIDO:
            WebServiceAEExtension wsExt = remoteAE.getAEExtension(WebServiceAEExtension.class);
            StowContext stowCtx = new StowContext(localAE, remoteAE, ServiceType.STOREREMEMBER);
            stowCtx.setStowRemoteBaseURL(wsExt.getStowRSBaseURL());
            stowCtx.setQidoRemoteBaseURL(wsExt.getQidoRSBaseURL());
            storeVerifyService.store(storeVerifyTxUID, stowCtx, insts);
            break;
        default:
            throw new RuntimeException("Unknown store-verify protocol " + ctx.getStoreVerifyProtocol());
        }
    }
    
    private void createStoreRememberTransaction(StoreAndRememberContext ctx, String storeVerifyTxUID) {
        String storeRememberTransactionUID = generateTransactionUID();
        // create db entries
        storeRememberEJB.addStoreRememberTx(storeRememberTransactionUID, storeVerifyTxUID, ctx.getExternalDeviceName(), 
                ctx.getStoreVerifyProtocol(), ctx.getInstances(), ctx.getRetries(), ctx.getDelay());
    }
    
    private void updateStoreRememberTransaction(StoreAndRememberContext ctx, String storeVerifyTxUID) {
        storeRememberEJB.updateStoreVerifyUIDOfStoreRemembers(ctx.getTransactionUID(), storeVerifyTxUID);
    }
    
    private List<ArchiveInstanceLocator> locate(String... iuids) {
        ArchiveDeviceExtension arcDev = device.getDeviceExtension(ArchiveDeviceExtension.class);
        Attributes keys = new Attributes();
        keys.setString(Tag.SOPInstanceUID, VR.UI, iuids);
        QueryParam queryParam = arcDev.getQueryParam();
        QueryRetrieveView view = new QueryRetrieveView();
        view.setViewID("IOCM");
        view.setHideNotRejectedInstances(false);
        queryParam.setQueryRetrieveView(view);
        return retrieveService.calculateMatches(null, keys, queryParam, true);
    }
    
    private ApplicationEntity getRemoteAE(StoreAndRememberContext cxt) {
        try {
            Device extDeviceTarget = conf.findDevice(cxt.getExternalDeviceName());
            Collection<ApplicationEntity> aes = extDeviceTarget.getApplicationEntities();
            if(aes.isEmpty()) {
                LOG.warn("Did not find suitable AE for archiving on device {}", extDeviceTarget);
                return null;
            }
            
            return aes.iterator().next();
        } catch(Exception e) {
            return null;
        }
    }
    
    private ApplicationEntity getLocalAE() {
       //TODO: make used AE for store-verify configurable
       ApplicationEntity localAE = device.getApplicationEntity("DCM4CHEE");
       return localAE;
    }
  
    public void onStoreVerifyResponse(@Observes @Service(ServiceType.STOREREMEMBER) StoreVerifyResponse storeVerifyResponse) {
        String storeVerifyTxUID = storeVerifyResponse.getTransactionUID();
        String storeAndRememberTxUID = getStoreAndRememberTransactionUID(storeVerifyTxUID);
        if(storeAndRememberTxUID == null) {
            LOG.error("Received store verify response for non existing transaction, store-verify-transaction UID: {}", storeVerifyTxUID);
            return;
        }
        
        String localAET = storeVerifyResponse.getLocalAET();
        String remoteAET = storeVerifyResponse.getRemoteAET();
        
        Availability defaultAvailability = null;
        String remoteDeviceName = null;
        try {
            ApplicationEntity archiveAE = aeCache.findApplicationEntity(localAET);
            ApplicationEntity remoteAE = aeCache.findApplicationEntity(remoteAET);
            remoteDeviceName= remoteAE.getDevice().getDeviceName();
            ArchiveAEExtension archAEExt = archiveAE.getAEExtension(ArchiveAEExtension.class);
            defaultAvailability = archAEExt.getDefaultExternalRetrieveAETAvailability();
        } catch (ConfigurationException e) {
            //failure attempt
            LOG.error("Unable to find Application"
                    + " Entity for {} or {} verification failure for "
                    + "store and remember transaction {}",
                    localAET, remoteAET,
                    storeAndRememberTxUID);
            return;
        }
        
        String retrieveAET = remoteAET;
        Map<String, VerifiedInstanceStatus> verifiedSopInstances = storeVerifyResponse.getVerifiedInstances();
        int numToVerify = verifiedSopInstances.size();
        int numVerified = 0;
        for(Entry<String, VerifiedInstanceStatus> inst : verifiedSopInstances.entrySet()) {
            Availability externalAvailability = inst.getValue().getAvailability();
            String sopInstanceUID = inst.getKey();
            if (Availability.ONLINE.equals(externalAvailability) || Availability.NEARLINE.equals(externalAvailability)) {
                Availability instAvailability = (defaultAvailability == null) ? externalAvailability
                        : (externalAvailability.compareTo(defaultAvailability) <= 0 ? externalAvailability : defaultAvailability);
                storeRememberEJB.addExternalLocation(sopInstanceUID, retrieveAET, remoteDeviceName, instAvailability);
                storeRememberEJB.updateStoreRemember(storeAndRememberTxUID, sopInstanceUID, StoreVerifyStatus.VERIFIED);
                numVerified++;
            } else {
                storeRememberEJB.updateStoreRemember(storeAndRememberTxUID, sopInstanceUID, StoreVerifyStatus.FAILED);
            }
        }
        
        if(numVerified < numToVerify) {
            int retriesLeft = storeRememberEJB.updatePartialStoreRemembersAndCheckForRetry(storeAndRememberTxUID);
            if(retriesLeft > 0) {
                // schedule retry
                StoreAndRememberContext retryContext = storeRememberEJB.createStoreAndRememberContext(storeAndRememberTxUID, retriesLeft - 1);
                storeRememberEJB.scheduleStoreAndRemember(retryContext);
            } else {
                // send 'failed' response
                storeRememberResponse.fire(new StoreAndRememberResponse(storeAndRememberTxUID, verifiedSopInstances));
            }
        } else {
            // send 'success' response
            storeRememberEJB.removeStoreRemembers(storeAndRememberTxUID);
            storeRememberResponse.fire(new StoreAndRememberResponse(storeAndRememberTxUID, verifiedSopInstances));
        }
    }
    
    private String getStoreAndRememberTransactionUID(String storeVerifyTxUID) {
        return storeRememberEJB.getStoreRememberUIDByStoreVerifyUID(storeVerifyTxUID);
    }
    
    private String generateTransactionUID() {
        return UUID.randomUUID().toString();
    }

}
