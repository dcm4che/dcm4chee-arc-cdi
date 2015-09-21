package org.dcm4chee.archive.mpps;

import org.dcm4che3.net.Dimse;
import org.dcm4chee.archive.ServiceContext;

public class MPPSContext extends ServiceContext{

    private String sendingAET;
    private String receivingAET;
    private String mppsSopInstanceUID;
    private Dimse dimse;

    public MPPSContext(String sendingAET, String receivingAET, String mppsSopInstanceUID, Dimse dimse) {
        this.sendingAET = sendingAET;
        this.receivingAET = receivingAET;
        this.mppsSopInstanceUID = mppsSopInstanceUID;
        this.dimse = dimse;
    }

    public String getSendingAET() {
        return sendingAET;
    }

    public String getReceivingAET() {
        return receivingAET;
    }

    public String getMppsSopInstanceUID() {
        return mppsSopInstanceUID;
    }

    public Dimse getDimse() {
        return dimse;
    }
}
