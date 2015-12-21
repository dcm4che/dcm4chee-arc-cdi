package org.dcm4chee.archive.entity.history;

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
import org.dcm4chee.archive.entity.AttributesBlob;

@Entity
@Table(name="series_history")
public class SeriesHistory implements Serializable{

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
    @JoinColumn(name="study_history_fk")
    private StudyHistory study;

    @Basic(optional = true)
    @Column(name = "none_iocm_src_aet", updatable = false)
    private String noneIOCMSourceAET;

    public SeriesHistory(){}

    public SeriesHistory(Attributes attrs, StudyHistory study) {
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

    public StudyHistory getStudy() {
        return study;
    }

    public String getOldSeriesUID() {
        return oldSeriesUID;
    }

    public void setOldSeriesUID(String oldSeriesUID) {
        this.oldSeriesUID = oldSeriesUID;
    }

    public void setStudy(StudyHistory study) {
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
        return "SeriesHistory[pk=" + pk+ "]";
    }
}
