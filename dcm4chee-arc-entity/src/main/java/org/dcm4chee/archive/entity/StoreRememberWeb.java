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
            name=StoreRememberWeb.GET_STORE_REMEMBER_WEB_ENTRY,
            query="select e from StoreRememberWeb e where "
                    + "e.transactionID = ?1")
})
@Entity(name="store_remember_web")
@Table(name="store_remember_web")
public class StoreRememberWeb {

    public static final String GET_STORE_REMEMBER_WEB_ENTRY
        = "StoreRememberWeb.getStoreRememberWebEntry";
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

    @Column(name="status")
    private StoreRememberStatus status;

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

    public StoreRememberStatus getStatus() {
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

    public void setStatus(StoreRememberStatus status) {
        this.status = status;
    }

    
}
