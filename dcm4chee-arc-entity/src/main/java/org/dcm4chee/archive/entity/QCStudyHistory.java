package org.dcm4chee.archive.entity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.dcm4che3.data.Attributes;

@Entity
@Table(name="qc_study_history")
public class QCStudyHistory implements Serializable{

    private static final long serialVersionUID = 7403364046696013058L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval = true, optional=true)
    @JoinColumn(name = "dicomattrs_fk")
    private AttributesBlob updatedAttributesBlob;
    

    @Basic(optional = false)
    @Column(name = "old_study_uid", updatable = false)
    private String oldStudyUID;

    @Basic(optional = true)
    @Column(name = "next_study_uid", updatable = false)
    private String nextStudyUID;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "qc_action_history_fk")
    private QCActionHistory action;
    

    public QCStudyHistory(){}
    public QCStudyHistory(Attributes attrs , QCActionHistory action) {
        this.action = action;
        if(attrs != null)
        this.updatedAttributesBlob = new AttributesBlob(attrs);
    }

    public QCActionHistory getAction() {
        return action;
    }

    public void setAction(QCActionHistory action) {
        this.action = action;
    }

    public AttributesBlob getUpdatedAttributesBlob() {
        return updatedAttributesBlob;
    }

    public void setUpdatedAttributesBlob(AttributesBlob updatedAttributesBlob) {
        this.updatedAttributesBlob = updatedAttributesBlob;
    }

    public long getPk() {
        return pk;
    }

    @Override
    public String toString() {
        return "QCStudyHistory[pk=" + pk+ "]";
    }


    public String getOldStudyUID() {
        return oldStudyUID;
    }

    public void setOldStudyUID(String oldStudyUID) {
        this.oldStudyUID = oldStudyUID;
    }
    public String getNextStudyUID() {
        return nextStudyUID;
    }
    public void setNextStudyUID(String nextStudyUID) {
        this.nextStudyUID = nextStudyUID;
    }

}
