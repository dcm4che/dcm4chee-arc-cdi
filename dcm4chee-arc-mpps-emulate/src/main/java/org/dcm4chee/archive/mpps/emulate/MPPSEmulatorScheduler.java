package org.dcm4chee.archive.mpps.emulate;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.MPPSEmulation;
import org.dcm4chee.archive.conf.MPPSEmulation.Rule;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;

public class MPPSEmulatorScheduler {

    @Inject MPPSEmulator mppsEmulator;
    
    public void onStoreInstance(@Observes StoreContext storeContext) {
        StoreSession storeSession = storeContext.getStoreSession();
        ArchiveAEExtension arcAE = storeSession.getArchiveAEExtension();
        MPPSEmulation mppsEmulation = arcAE.getMppsEmulation();
        if (mppsEmulation == null)
            return;
        
        String remoteAET = storeSession.getRemoteAET();
        MPPSEmulation.Rule mppsEmulationRule =
                mppsEmulation.getMPPSEmulationRule(remoteAET);
        if (mppsEmulationRule != null)
            scheduleMPPSEmulation(storeContext, mppsEmulationRule);
    }

    private void scheduleMPPSEmulation(StoreContext storeContext,
            Rule mppsEmulationRule) {
        // TODO Auto-generated method stub
        
    }
}
