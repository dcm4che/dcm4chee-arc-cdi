package org.dcm4chee.archive.mpps;

public class MPPSContext {

    private String sendingAET;
    private String receivingAET;

    public MPPSContext(String sendingAET, String receivingAET) {
        this.sendingAET = sendingAET;
        this.receivingAET = receivingAET;
    }

    public String getSendingAET() {
        return sendingAET;
    }

    public String getReceivingAET() {
        return receivingAET;
    }
}
