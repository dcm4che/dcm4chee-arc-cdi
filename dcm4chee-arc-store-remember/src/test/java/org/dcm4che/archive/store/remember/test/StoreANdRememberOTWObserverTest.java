package org.dcm4che.archive.store.remember.test;

import static org.junit.Assert.assertTrue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.dcm4chee.archive.stow.client.StowResponse;


@ApplicationScoped
public class StoreANdRememberOTWObserverTest {

    StowResponse response;

    public void observeSToreAndRememberOTW(@Observes StowResponse rsp) {
        response = rsp;
        assertTrue(response.getFailedSopInstances().isEmpty());
        assertTrue(response.getSuccessfulSopInstances().contains("1.1.1.2"));
    }

}
