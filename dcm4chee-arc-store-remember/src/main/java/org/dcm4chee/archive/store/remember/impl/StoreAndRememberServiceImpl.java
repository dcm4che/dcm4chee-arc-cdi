package org.dcm4chee.archive.store.remember.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.qido.client.QidoClientService;
import org.dcm4chee.archive.store.remember.StoreAndRememberService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.dcm4chee.archive.stow.client.StowClientService;
import org.dcm4chee.archive.stow.client.StowContext;
import org.dcm4chee.archive.stow.client.StowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StoreAndRememberServiceImpl implements StoreAndRememberService{

    private static final Logger LOG = LoggerFactory.getLogger(
            StoreAndRememberServiceImpl.class);
    @Inject
    private QidoClientService qidoService;

    @Inject
    private StowClientService stowService;

    @Inject 
    private CStoreSCUService storeSCUService;

    @Override
    public void store(StowContext context,
            Collection<ArchiveInstanceLocator> insts) {
        stowService.scheduleStow(context, insts, 1, 1, 0);
    }

    public void verifyStorage(@Observes StowResponse storeResponse) {

        if(LOG.isDebugEnabled()) {
            LOG.debug("Stow Successful for the following instances:");
            for(String sopUID : storeResponse.getSuccessfulSopInstances())
                LOG.debug(sopUID);
            if(storeResponse.getFailedSopInstances().isEmpty())
                LOG.debug("No Instances Failed for Stow request to, {}");
        }
        //call qido verification

    }

    @Override
    public void store(CStoreSCUContext context,
            Collection<ArchiveInstanceLocator> insts) {
        // TODO Auto-generated method stub
        
    }

    public void verifyStorage(@Observes BasicCStoreSCUResp storeResponse) {
        //call storage commitment
    }
//    handles storage commitment event
//    public void handleSuccsessfulVerification(@Observes storagecommitmentevent storeResponse) {
//        //call storage commitment
//    }
    

}
