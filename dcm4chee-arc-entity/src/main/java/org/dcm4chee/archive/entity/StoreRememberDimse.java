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
            name=StoreRememberDimse.GET_STORE_REMEMBER_DIMSE_ENTRY,
            query="select e from StoreRememberDimse e where "
                    + "e.transactionID = ?1")
})
@Entity
@Table(name="store_remember_dimse")
public class StoreRememberDimse {

    public static final String GET_STORE_REMEMBER_DIMSE_ENTRY 
        = "StoreRememberDimse.getStoreRememberDimseEntry";
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
