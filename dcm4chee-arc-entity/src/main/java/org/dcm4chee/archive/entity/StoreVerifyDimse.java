package org.dcm4chee.archive.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
    @NamedQuery(
            name=StoreVerifyDimse.GET_STORE_VERIFY_DIMSE_ENTRY,
            query="select e from StoreVerifyDimse e where "
                    + "e.transactionID = ?1"),
    @NamedQuery(
            name=StoreVerifyDimse.STORE_VERIFY_DIMSE_ENTRY_EXISTS,
            query="select count(e.pk) from StoreVerifyDimse e"
                    + " where e.transactionID = ?1")
})
@Entity
@Table(name="store_verify_dimse")
public class StoreVerifyDimse {

    public static final String GET_STORE_VERIFY_DIMSE_ENTRY 
        = "StoreVerifyDimse.getStoreVerifyDimseEntry";
    public static final String STORE_VERIFY_DIMSE_ENTRY_EXISTS
        = "StoreVerifyDimse.storeVerifyDimseEntryExists";
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

    @Column(name="intended_service")
    private String Service;
    
    @Column(name="status")
    private StoreVerifyStatus status;

    public String getTransactionID() {
        return transactionID;
    }

    public String getRemoteAET() {
        return remoteAET;
    }

    public String getLocalAET() {
        return localAET;
    }

    public StoreVerifyStatus getStatus() {
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

    public void setStatus(StoreVerifyStatus status) {
        this.status = status;
    }

    public String getService() {
        return Service;
    }

    public void setService(String service) {
        Service = service;
    }

    
}
