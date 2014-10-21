package org.dcm4chee.archive.mpps.emulate;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;

public class MPPSEmulatorScheduler {

    @Inject MPPSEmulator mppsEmulator;
    
    public void onStoreInstance(@Observes StoreContext storeContext) {
        StoreSession storeSession = storeContext.getStoreSession();
        ArchiveAEExtension arcAE = storeSession.getArchiveAEExtension();
        String remoteAET = storeSession.getRemoteAET();
        
        //TODO
    }
}
