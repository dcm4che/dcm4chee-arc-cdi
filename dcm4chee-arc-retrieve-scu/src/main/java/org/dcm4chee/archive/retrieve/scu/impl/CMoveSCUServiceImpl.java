package org.dcm4chee.archive.retrieve.scu.impl;

import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4chee.archive.retrieve.scu.CMoveSCU;
import org.dcm4chee.archive.retrieve.scu.CMoveSCUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMoveSCUServiceImpl implements CMoveSCUService{
    
    private static final Logger LOG = LoggerFactory.getLogger(CMoveSCU.class);
    
    @Override
    public boolean cmove(Attributes keys, DimseRSPHandler handler,
            ApplicationEntity ae,ApplicationEntity remoteAE
            , String destinationAET) {
        CMoveSCU scu = new CMoveSCU(ae, remoteAE, destinationAET);
        
        try {
            scu.cmove(keys, handler);
        } catch (IOException e) {
            LOG.error("");
            return false;
        } catch (InterruptedException e) {
            LOG.error("");
            return false;
        }
        return true;
    }

}
