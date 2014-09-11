/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.archive.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.AttributeFilter;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
@NamedQueries({
@NamedQuery(
    name="Series.findBySeriesInstanceUID",
    query="SELECT s FROM Series s WHERE s.seriesInstanceUID = ?1"),
@NamedQuery(
        name="Series.findBySeriesInstanceUID.eager",
        query="SELECT se FROM Series se "
                + "JOIN FETCH se.study st "
                + "JOIN FETCH st.patient p "
                + "JOIN FETCH se.attributesBlob "
                + "JOIN FETCH st.attributesBlob "                
                + "JOIN FETCH p.attributesBlob "
                + "WHERE se.seriesInstanceUID = ?1"),    
@NamedQuery(
    name="Series.patientStudySeriesAttributes",
    query="SELECT NEW org.dcm4chee.archive.entity.PatientStudySeriesAttributes("
            + "s.attributesBlob.encodedAttributes, "
            + "s.study.attributesBlob.encodedAttributes, "
            + "s.study.patient.attributesBlob.encodedAttributes) "
            + "FROM Series s WHERE s.pk = ?1"),
@NamedQuery(
    name="Series.queryPatientStudySeriesAttributes",
    query="SELECT NEW org.dcm4chee.archive.entity.QueryPatientStudySeriesAttributes("
            + "s.study.pk, "
            + "s.study.numberOfSeries1, "
            + "s.study.numberOfSeries2, "
            + "s.study.numberOfSeries3, "
            + "s.study.numberOfInstances1, "
            + "s.study.numberOfInstances2, "
            + "s.study.numberOfInstances3, "
            + "s.numberOfInstances1, "
            + "s.numberOfInstances2, "
            + "s.numberOfInstances3, "
            + "s.study.modalitiesInStudy, "
            + "s.study.sopClassesInStudy, "
            + "s.attributesBlob.encodedAttributes, "
            + "s.study.attributesBlob.encodedAttributes, "
            + "s.study.patient.attributesBlob.encodedAttributes) "
            + "FROM Series s WHERE s.pk = ?1"),
@NamedQuery(
    name="Series.updateNumberOfInstances1",
    query="UPDATE Series s "
            + "SET s.numberOfInstances1 = ?1 "
            + "WHERE s.pk = ?2"),
@NamedQuery(
    name="Series.updateNumberOfInstances2",
    query="UPDATE Series s "
            + "SET s.numberOfInstances2 = ?1 "
            + "WHERE s.pk = ?2"),
@NamedQuery(
    name="Series.updateNumberOfInstances3",
    query="UPDATE Series s "
            + "SET s.numberOfInstances3 = ?1 "
            + "WHERE s.pk = ?2")
})
@Entity
@Table(name = "series")
public class Series implements Serializable {

    private static final long serialVersionUID = -8317105475421750944L;

    public static final String FIND_BY_SERIES_INSTANCE_UID = "Series.findBySeriesInstanceUID";
    public static final String FIND_BY_SERIES_INSTANCE_UID_EAGER = "Series.findBySeriesInstanceUID.eager";    
    public static final String PATIENT_STUDY_SERIES_ATTRIBUTES = "Series.patientStudySeriesAttributes";
    public static final String QUERY_PATIENT_STUDY_SERIES_ATTRIBUTES = "Series.queryPatientStudySeriesAttributes";
    public static final String [] UPDATE_NUMBER_OF_INSTANCES = {
            "Series.updateNumberOfInstances1",
            "Series.updateNumberOfInstances2",
            "Series.updateNumberOfInstances3"
    };

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private Date createdTime;

    @Basic(optional = false)
    @Column(name = "updated_time")
    private Date updatedTime;

    @Basic(optional = false)
    @Column(name = "series_iuid", updatable = false)
    private String seriesInstanceUID;

    @Basic(optional = false)
    @Column(name = "series_no")
    private String seriesNumber;

    @Basic(optional = false)
    @Column(name = "series_desc")
    private String seriesDescription;

    @Basic(optional = false)
    @Column(name = "modality")
    private String modality;

    @Basic(optional = false)
    @Column(name = "department")
    private String institutionalDepartmentName;

    @Basic(optional = false)
    @Column(name = "institution")
    private String institutionName;

    @Basic(optional = false)
    @Column(name = "station_name")
    private String stationName;

    @Basic(optional = false)
    @Column(name = "body_part")
    private String bodyPartExamined;

    @Basic(optional = false)
    @Column(name = "laterality")
    private String laterality;

    @Basic(optional = false)
    @Column(name = "pps_start_date")
    private String performedProcedureStepStartDate;

    @Basic(optional = false)
    @Column(name = "pps_start_time")
    private String performedProcedureStepStartTime;

    @Basic(optional = false)
    @Column(name = "pps_iuid")
    private String performedProcedureStepInstanceUID;

    @Basic(optional = false)
    @Column(name = "pps_cuid")
    private String performedProcedureStepClassUID;

    @Basic(optional = false)
    @Column(name = "series_custom1")
    private String seriesCustomAttribute1;

    @Basic(optional = false)
    @Column(name = "series_custom2")
    private String seriesCustomAttribute2;

    @Basic(optional = false)
    @Column(name = "series_custom3")
    private String seriesCustomAttribute3;

    @Basic(optional = false)
    @Column(name = "num_instances1")
    private int numberOfInstances1 = -1;

    @Basic(optional = false)
    @Column(name = "num_instances2")
    private int numberOfInstances2 = -1;

    @Basic(optional = false)
    @Column(name = "num_instances3")
    private int numberOfInstances3 = -1;

    @Column(name = "src_aet")
    private String sourceAET;

    @Column(name = "retrieve_aets")
    private String retrieveAETs;

    @Column(name = "ext_retr_aet")
    private String externalRetrieveAET;

    @Basic(optional = false)
    @Column(name = "availability")
    private Availability availability;

    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "attrs_fk")
    private AttributesBlob attributesBlob;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "perf_phys_name_fk")
    private PersonName performingPhysicianName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inst_code_fk")
    private Code institutionCode;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "series_fk")
    private Collection<RequestAttributes> requestAttributes;

//    @ManyToMany
//    @JoinTable(name = "rel_series_sps", 
//        joinColumns = @JoinColumn(name = "series_fk", referencedColumnName = "pk"),
//        inverseJoinColumns = @JoinColumn(name = "sps_fk", referencedColumnName = "pk"))
//    private Collection<ScheduledProcedureStep> scheduledProcedureSteps;

    @ManyToOne
    @JoinColumn(name = "study_fk")
    private Study study;

    @OneToMany(mappedBy = "series", orphanRemoval = true)
    private Collection<Instance> instances;

    @Override
    public String toString() {
        return "Series[pk=" + pk
                + ", uid=" + seriesInstanceUID
                + ", no=" + seriesNumber
                + ", mod=" + modality
                + ", numI=" + numberOfInstances1
                + "/" + numberOfInstances2
                + "/" + numberOfInstances3
                + "]";
    }

    @PrePersist
    public void onPrePersist() {
        Date now = new Date();
        createdTime = now;
        updatedTime = now;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedTime = new Date();
    }

    public AttributesBlob getAttributesBlob() {
        return attributesBlob;
    }
    
    public Attributes getAttributes() throws BlobCorruptedException {
        return attributesBlob.getAttributes();
    }

    public long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public String getModality() {
        return modality;
    }

    public String getInstitutionalDepartmentName() {
        return institutionalDepartmentName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public String getStationName() {
        return stationName;
    }

    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    public String getLaterality() {
        return laterality;
    }

    public PersonName getPerformingPhysicianName() {
        return performingPhysicianName;
    }

    public String getPerformedProcedureStepStartDate() {
        return performedProcedureStepStartDate;
    }

    public String getPerformedProcedureStepStartTime() {
        return performedProcedureStepStartTime;
    }

    public String getPerformedProcedureStepInstanceUID() {
        return performedProcedureStepInstanceUID;
    }

    public String getPerformedProcedureStepClassUID() {
        return performedProcedureStepClassUID;
    }

    public String getSeriesCustomAttribute1() {
        return seriesCustomAttribute1;
    }

    public String getSeriesCustomAttribute2() {
        return seriesCustomAttribute2;
    }

    public String getSeriesCustomAttribute3() {
        return seriesCustomAttribute3;
    }

    public int getNumberOfInstances(int slot) {
        switch(slot) {
        case 1:
            return numberOfInstances1;
        case 2:
            return numberOfInstances2;
        case 3:
            return numberOfInstances3;
        }
        throw new IllegalArgumentException("slot:" + slot);
    }

    public void setNumberOfInstances(int slot, int num) {
        switch(slot) {
        case 1:
            numberOfInstances1 = num;
            break;
        case 2:
            numberOfInstances2 = num;
            break;
        case 3:
            numberOfInstances3 = num;
            break;
        default:
            throw new IllegalArgumentException("slot:" + slot);
        }
    }

    public void resetNumberOfInstances() {
        this.numberOfInstances1 = -1;
        this.numberOfInstances2 = -1;
        this.numberOfInstances3 = -1;
    }

    public String getSourceAET() {
        return sourceAET;
    }

    public void setSourceAET(String sourceAET) {
        this.sourceAET = sourceAET;
    }

    public String[] getRetrieveAETs() {
        return StringUtils.split(retrieveAETs, '\\');
    }

    public void setRetrieveAETs(String... retrieveAETs) {
        this.retrieveAETs = StringUtils.concat(retrieveAETs, '\\');
    }

    public void retainRetrieveAETs(String[] retrieveAETs) {
        String[] aets = getRetrieveAETs();
        if (!Arrays.equals(aets, retrieveAETs))
            setRetrieveAETs(Utils.intersection(aets, retrieveAETs));
    }

    public String getExternalRetrieveAET() {
        return externalRetrieveAET;
    }

    public void setExternalRetrieveAET(String externalRetrieveAET) {
        this.externalRetrieveAET = externalRetrieveAET;
    }

    public void retainExternalRetrieveAET(String retrieveAET) {
        if (this.externalRetrieveAET != null
                && !this.externalRetrieveAET.equals(retrieveAET))
            setExternalRetrieveAET(null);
    }

    public String[] getAllRetrieveAETs() {
        return Utils.decodeAETs(retrieveAETs, externalRetrieveAET);
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public void floorAvailability(Availability availability) {
        if (this.availability.compareTo(availability) < 0)
            this.availability = availability;
    }

    public Code getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(Code institutionCode) {
        this.institutionCode = institutionCode;
    }

    public Collection<RequestAttributes> getRequestAttributes() {
        return requestAttributes;
    }

    public void setRequestAttributes(Collection<RequestAttributes> requestAttributes) {
        this.requestAttributes = requestAttributes;
    }

//    public Collection<ScheduledProcedureStep> getScheduledProcedureSteps() {
//        return scheduledProcedureSteps;
//    }
//
//    public void setScheduledProcedureSteps(
//            Collection<ScheduledProcedureStep> scheduledProcedureSteps) {
//        this.scheduledProcedureSteps = scheduledProcedureSteps;
//    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Collection<Instance> getInstances() {
        return instances;
    }

    public void setAttributes(Attributes attrs, AttributeFilter filter, FuzzyStr fuzzyStr) {
        seriesInstanceUID = attrs.getString(Tag.SeriesInstanceUID);
        seriesNumber = attrs.getString(Tag.SeriesNumber, "*");
        seriesDescription = attrs.getString(Tag.SeriesDescription, "*");
        institutionName = attrs.getString(Tag.InstitutionName, "*");
        institutionalDepartmentName = attrs.getString(Tag.InstitutionalDepartmentName, "*");
        modality = attrs.getString(Tag.Modality, "*").toUpperCase();
        stationName = attrs.getString(Tag.StationName, "*");
        bodyPartExamined = attrs.getString(Tag.BodyPartExamined, "*").toUpperCase();
        laterality = attrs.getString(Tag.Laterality, "*").toUpperCase();
        Attributes refPPS = attrs.getNestedDataset(Tag.ReferencedPerformedProcedureStepSequence);
        if (refPPS != null) {
            performedProcedureStepInstanceUID = refPPS.getString(Tag.ReferencedSOPInstanceUID, "*");
            performedProcedureStepClassUID = refPPS.getString(Tag.ReferencedSOPClassUID, "*");
        } else {
            performedProcedureStepInstanceUID = "*";
            performedProcedureStepClassUID = "*";
        }
        Date dt = attrs.getDate(Tag.PerformedProcedureStepStartDateAndTime);
        if (dt != null) {
            performedProcedureStepStartDate = DateUtils.formatDA(null, dt);
            performedProcedureStepStartTime = 
                attrs.containsValue(Tag.PerformedProcedureStepStartDate)
                    ? DateUtils.formatTM(null, dt)
                    : "*";
        } else {
            performedProcedureStepStartDate = "*";
            performedProcedureStepStartTime = "*";
        }
        performingPhysicianName = PersonName.valueOf(
                attrs.getString(Tag.PerformingPhysicianName), fuzzyStr,
                performingPhysicianName);
        seriesCustomAttribute1 = 
            AttributeFilter.selectStringValue(attrs, filter.getCustomAttribute1(), "*");
        seriesCustomAttribute2 =
            AttributeFilter.selectStringValue(attrs, filter.getCustomAttribute2(), "*");
        seriesCustomAttribute3 =
            AttributeFilter.selectStringValue(attrs, filter.getCustomAttribute3(), "*");

        attributesBlob = new AttributesBlob(new Attributes(attrs, filter.getSelection()));
        
    }
}
