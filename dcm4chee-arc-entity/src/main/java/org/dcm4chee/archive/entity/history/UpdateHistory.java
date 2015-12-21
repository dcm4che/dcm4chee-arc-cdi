package org.dcm4chee.archive.entity.history;

import org.dcm4chee.archive.entity.AttributesBlob;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
@NamedQueries({
@NamedQuery(
    name="UpdateHistory.findFirstUpdateEntry",
    query="SELECT uh FROM UpdateHistory uh where uh.objectUID = ?1 "
            + "AND uh.next IS NULL"
)
})
@Entity
@Table(name="update_history")
public class UpdateHistory implements Serializable{

    private static final long serialVersionUID = -4407564849668358911L;

    public static final String FIND_FIRST_UPDATE_ENTRY = "UpdateHistory.findFirstUpdateEntry";
    
    public enum UpdateScope {NONE,PATIENT, STUDY, SERIES, INSTANCE}

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private Date createdTime;

    @Basic(optional = false)
    @Column(name = "object_uid")
    private String objectUID;

    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval = true, optional=true)
    @JoinColumn(name = "dicomattrs_fk")
    private AttributesBlob updatedAttributesBlob;

    @Basic(optional = false) @Enumerated(EnumType.STRING)
    @Column(name= "scope", updatable=false)
    private UpdateScope scope;

    @OneToOne(optional = true)
    @JoinColumn(name = "update_history_fk")
    private UpdateHistory next;

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public AttributesBlob getUpdatedAttributesBlob() {
        return updatedAttributesBlob;
    }

    public void setUpdatedAttributesBlob(AttributesBlob updatedAttributesBlob) {
        this.updatedAttributesBlob = updatedAttributesBlob;
    }

    public UpdateScope getScope() {
        return scope;
    }

    public void setScope(UpdateScope scope) {
        this.scope = scope;
    }

    public UpdateHistory getNext() {
        return next;
    }

    public void setNext(UpdateHistory next) {
        this.next = next;
    }

    public long getPk() {
        return pk;
    }

    public String getObjectUID() {
        return objectUID;
    }

    public void setObjectUID(String objectUID) {
        this.objectUID = objectUID;
    }

}
