package org.dcm4chee.archive.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name="store_remember_dimse")
@Table(name="store_remember_dimse")
public class StoreRememberDimse {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;
    
    @Column(name="transaction_id")
    private String transactionID;

    @Column(name="remote_aet")
    private String remoteAET;

    @Column(name="local_aet")
    private String localAET;

    @Column(name="status")
    private StoreRememberStatus status;

    public String getTransactionID() {
        return transactionID;
    }

    public String getRemoteAET() {
        return remoteAET;
    }

    public String getLocalAET() {
        return localAET;
    }

    public StoreRememberStatus getStatus() {
        return status;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public void setRemoteAET(String remoteAET) {
        this.remoteAET = remoteAET;
    }

    public void setLocalAET(String localAET) {
        this.localAET = localAET;
    }

    public void setStatus(StoreRememberStatus status) {
        this.status = status;
    }

    
}
