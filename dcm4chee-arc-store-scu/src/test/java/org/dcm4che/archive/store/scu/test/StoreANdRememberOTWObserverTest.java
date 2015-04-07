package org.dcm4che.archive.store.scu.test;

import static org.junit.Assert.assertTrue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.dcm4chee.archive.store.scu.StoreAndRememberResponse;

@ApplicationScoped
public class StoreANdRememberOTWObserverTest {

    StoreAndRememberResponse response;

    public void observeSToreAndRememberOTW(@Observes StoreAndRememberResponse rsp) {
        response = rsp;
        assertTrue(response.getFailedSopInstances().isEmpty());
        assertTrue(response.getSuccessfulSopInstances().contains("1.1.1.2"));

        if(response.getVerifiedStoredInstances() != null)
        assertTrue(response.getVerifiedStoredInstances().contains("1.1.1.2"));
    }

}
