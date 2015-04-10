package org.dcm4chee.archive.qido.client;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4chee.archive.conf.ArchiveAEExtension;

public class QidoContext {
    private ApplicationEntity localAE, remoteAE;

    private String remoteBaseURL;

    private boolean fuzzyMatching;

    private boolean timeZoneAdjustment;

    public QidoContext(ApplicationEntity localAE,
            ApplicationEntity remoteAE) {
        super();
        this.localAE = localAE;
        this.remoteAE = remoteAE;
    }

    public ApplicationEntity getLocalAE() {
        return localAE;
    }

    public ArchiveAEExtension getArchiveAEExtension() {
        return localAE.getAEExtension(ArchiveAEExtension.class);
    }

    public void setLocalAE(ApplicationEntity localAE) {
        this.localAE = localAE;
    }

    public ApplicationEntity getRemoteAE() {
        return remoteAE;
    }

    public void setRemoteAE(ApplicationEntity remoteAE) {
        this.remoteAE = remoteAE;
    }

    public String getRemoteBaseURL() {
        return remoteBaseURL;
    }

    public void setRemoteBaseURL(String remoteBaseURL) {
        this.remoteBaseURL = remoteBaseURL;
    }

    public boolean isFuzzyMatching() {
        return fuzzyMatching;
    }

    public boolean isTimeZoneAdjustment() {
        return timeZoneAdjustment;
    }

    public void setFuzzyMatching(boolean fuzzyMatching) {
        this.fuzzyMatching = fuzzyMatching;
    }

    public void setTimeZoneAdjustment(boolean timeZoneAdjustment) {
        this.timeZoneAdjustment = timeZoneAdjustment;
    }

}
