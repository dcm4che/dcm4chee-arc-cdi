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
            name=StoreVerifyWeb.GET_STORE_VERIFY_WEB_ENTRY,
            query="select e from StoreVerifyWeb e where "
                    + "e.transactionID = ?1")
})
@Entity
@Table(name="store_verify_web")
public class StoreVerifyWeb {

    public static final String GET_STORE_VERIFY_WEB_ENTRY
        = "StoreVerifyWeb.getStoreVerifyWebEntry";
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Column(name="transaction_id")
    private String transactionID;

    @Column(name="qido_base_url")
    private String qidoBaseURL;

    @Column(name="remote_aet")
    private String remoteAET;

    @Column(name="local_aet")
    private String localAET;

    @Column(name="intended_service")
    private String Service;

    @Column(name="status")
    private StoreVerifyStatus status;
    
    public long getPk() {
        return pk;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public String getQidoBaseURL() {
        return qidoBaseURL;
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

    public void setQidoBaseURL(String qidoBaseURL) {
        this.qidoBaseURL = qidoBaseURL;
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
