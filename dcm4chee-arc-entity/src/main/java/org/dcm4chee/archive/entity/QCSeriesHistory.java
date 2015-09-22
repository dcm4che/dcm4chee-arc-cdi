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
@Table(name="qc_series_history")
public class QCSeriesHistory implements Serializable{

    private static final long serialVersionUID = -4611155198722940779L;
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval = true, optional=true)
    @JoinColumn(name = "dicomattrs_fk")
    private AttributesBlob updatedAttributesBlob;

    @Basic(optional = false)
    @Column(name = "old_series_uid", updatable = false)
    private String oldSeriesUID;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="qc_study_history_fk")
    private QCStudyHistory study;

    @Basic(optional = true)
    @Column(name = "none_iocm_src_aet", updatable = false)
    private String noneIOCMSourceAET;

    public QCSeriesHistory(){}

    public QCSeriesHistory(Attributes attrs , QCStudyHistory study) {
        this.study = study;
        if(attrs != null)
        this.updatedAttributesBlob = new AttributesBlob(attrs);
    }
    
    public AttributesBlob getUpdatedAttributesBlob() {
        return updatedAttributesBlob;
    }

    public void setUpdatedAttributesBlob(AttributesBlob updatedAttributesBlob) {
        this.updatedAttributesBlob = updatedAttributesBlob;
    }

    public QCStudyHistory getStudy() {
        return study;
    }

    public String getOldSeriesUID() {
        return oldSeriesUID;
    }

    public void setOldSeriesUID(String oldSeriesUID) {
        this.oldSeriesUID = oldSeriesUID;
    }

    public void setStudy(QCStudyHistory study) {
        this.study = study;
    }

    public String getNoneIOCMSourceAET() {
        return noneIOCMSourceAET;
    }

    public void setNoneIOCMSourceAET(String noneIOCMSourceAET) {
        this.noneIOCMSourceAET = noneIOCMSourceAET;
    }

    public long getPk() {
        return pk;
    }

    @Override
    public String toString() {
        return "QCSeriesHistory[pk=" + pk+ "]";
    }
}
