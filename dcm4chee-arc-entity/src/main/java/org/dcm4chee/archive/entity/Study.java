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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

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
    name="Study.findByStudyInstanceUID",
    query="SELECT s FROM Study s WHERE s.studyInstanceUID = ?1"),
@NamedQuery(
    name="Study.updateNumberOfSeries1",
    query="UPDATE Study s "
        + "SET s.numberOfSeries1 = ?1 "
        + "WHERE s.pk = ?2"),
@NamedQuery(
    name="Study.updateNumberOfSeries2",
    query="UPDATE Study s "
        + "SET s.numberOfSeries2 = ?1 "
        + "WHERE s.pk = ?2"),
@NamedQuery(
    name="Study.updateNumberOfSeries3",
    query="UPDATE Study s "
        + "SET s.numberOfSeries3 = ?1 "
        + "WHERE s.pk = ?2"),
@NamedQuery(
    name="Study.updateNumberOfInstances1",
    query="UPDATE Study s "
        + "SET s.numberOfInstances1 = ?1 "
        + "WHERE s.pk = ?2"),
@NamedQuery(
        name="Study.updateNumberOfInstances2",
        query="UPDATE Study s "
        + "SET s.numberOfInstances2 = ?1 "
        + "WHERE s.pk = ?2"),
@NamedQuery(
        name="Study.updateNumberOfInstances3",
        query="UPDATE Study s "
        + "SET s.numberOfInstances3 = ?1 "
        + "WHERE s.pk = ?2")
})
@Entity
@Table(name = "study")
public class Study implements Serializable {

    private static final long serialVersionUID = -6358525535057418771L;

    public static final String FIND_BY_STUDY_INSTANCE_UID = "Study.findByStudyInstanceUID";
    public static final String[] UPDATE_NUMBER_OF_SERIES = {
        "Study.updateNumberOfSeries1",
        "Study.updateNumberOfSeries2",
        "Study.updateNumberOfSeries3"
    };
    public static final String[] UPDATE_NUMBER_OF_INSTANCES = {
        "Study.updateNumberOfInstances1",
        "Study.updateNumberOfInstances2",
        "Study.updateNumberOfInstances3"
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
    @Column(name = "study_iuid", updatable = false)
    private String studyInstanceUID;

    @Basic(optional = false)
    @Column(name = "study_id")
    private String studyID;

    @Basic(optional = false)
    @Column(name = "study_date")
    private String studyDate;

    @Basic(optional = false)
    @Column(name = "study_time")
    private String studyTime;

    @Basic(optional = false)
    @Column(name = "accession_no")
    private String accessionNumber;

    @Basic(optional = false)
    @Column(name = "study_desc")
    private String studyDescription;

    @Basic(optional = false)
    @Column(name = "study_custom1")
    private String studyCustomAttribute1;

    @Basic(optional = false)
    @Column(name = "study_custom2")
    private String studyCustomAttribute2;

    @Basic(optional = false)
    @Column(name = "study_custom3")
    private String studyCustomAttribute3;

    @Column(name = "access_control_id")
    private String accessControlID;

    @Basic(optional = false)
    @Column(name = "num_series1")
    private int numberOfSeries1 = -1;

    @Basic(optional = false)
    @Column(name = "num_series2")
    private int numberOfSeries2 = -1;

    @Basic(optional = false)
    @Column(name = "num_series3")
    private int numberOfSeries3 = -1;

    @Basic(optional = false)
    @Column(name = "num_instances1")
    private int numberOfInstances1 = -1;

    @Basic(optional = false)
    @Column(name = "num_instances2")
    private int numberOfInstances2 = -1;

    @Basic(optional = false)
    @Column(name = "num_instances3")
    private int numberOfInstances3 = -1;

    @Column(name = "mods_in_study")
    private String modalitiesInStudy;

    @Column(name = "cuids_in_study")
    private String sopClassesInStudy;

    @Column(name = "retrieve_aets")
    private String retrieveAETs;

    @Column(name = "ext_retr_aet")
    private String externalRetrieveAET;

    @Basic(optional = false)
    @Column(name = "availability")
    private Availability availability;

    @Basic(optional = false)
    @Column(name = "study_attrs")
    private byte[] encodedAttributes;

    @Transient
    private Attributes cachedAttributes;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ref_phys_name_fk")
    private PersonName referringPhysicianName;

    @ManyToOne
    @JoinColumn(name = "accno_issuer_fk")
    private Issuer issuerOfAccessionNumber;

    @ManyToMany
    @JoinTable(name = "rel_study_pcode", 
        joinColumns = @JoinColumn(name = "study_fk", referencedColumnName = "pk"),
        inverseJoinColumns = @JoinColumn(name = "pcode_fk", referencedColumnName = "pk"))
    private Collection<Code> procedureCodes;

    @ManyToOne
    @JoinColumn(name = "patient_fk")
    private Patient patient;

    @OneToMany(mappedBy = "study", orphanRemoval = true)
    private Collection<Series> series;

    @Override
    public String toString() {
        return "Study[pk=" + pk
                + ", uid=" + studyInstanceUID
                + ", id=" + studyID
                + ", mods=" + modalitiesInStudy
                + ", numS=" + numberOfSeries1
                + "/" + numberOfSeries2
                + "/" + numberOfSeries3
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

    public Attributes getAttributes() throws BlobCorruptedException {
        if (cachedAttributes == null)
            cachedAttributes = Utils.decodeAttributes(encodedAttributes);
        return cachedAttributes;
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

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public String getStudyID() {
        return studyID;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public Issuer getIssuerOfAccessionNumber() {
        return issuerOfAccessionNumber;
    }

    public void setIssuerOfAccessionNumber(Issuer issuerOfAccessionNumber) {
        this.issuerOfAccessionNumber = issuerOfAccessionNumber;
    }

    public PersonName getReferringPhysicianName() {
        return referringPhysicianName;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public String getStudyCustomAttribute1() {
        return studyCustomAttribute1;
    }

    public String getStudyCustomAttribute2() {
        return studyCustomAttribute2;
    }

    public String getStudyCustomAttribute3() {
        return studyCustomAttribute3;
    }

    public int getNumberOfSeries(int slot) {
        switch(slot) {
        case 1:
            return numberOfSeries1;
        case 2:
            return numberOfSeries2;
        }
        throw new IllegalArgumentException("slot:" + slot);
    }

    public void setNumberOfSeries(int slot, int num) {
        switch(slot) {
        case 1:
            numberOfSeries1 = num;
            break;
        case 2:
            numberOfSeries2 = num;
            break;
        default:
            throw new IllegalArgumentException("slot:" + slot);
        }
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
        this.numberOfSeries1 = -1;
        this.numberOfSeries2 = -1;
        this.numberOfSeries3 = -1;
        this.numberOfInstances1 = -1;
        this.numberOfInstances2 = -1;
        this.numberOfInstances3 = -1;
   }

    public String[] getModalitiesInStudy() {
        return StringUtils.split(modalitiesInStudy, '\\');
    }

    public void setModalitiesInStudy(String... modalitiesInStudy) {
        this.modalitiesInStudy = StringUtils.concat(modalitiesInStudy, '\\');
    }

    public void addModalityInStudy(String modality) {
        if (modality != null && !Utils.contains(getModalitiesInStudy(), modality))
            this.modalitiesInStudy = this.modalitiesInStudy + '\\' + modality;
    }

    public String[] getSOPClassesInStudy() {
        return StringUtils.split(sopClassesInStudy, '\\');
    }

    public void setSOPClassesInStudy(String... sopClassesInStudy) {
        this.sopClassesInStudy = StringUtils.concat(sopClassesInStudy, '\\');
    }

    public void addSOPClassInStudy(String sopClass) {
        if (!Utils.contains(getSOPClassesInStudy(), sopClass))
            this.sopClassesInStudy = this.sopClassesInStudy + '\\' + sopClass;
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
        if (this.externalRetrieveAET!= null
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

    public String getAccessControlID() {
        return accessControlID;
    }

    public void setAccessControlID(String accessControlID) {
        this.accessControlID = accessControlID;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public Collection<Code> getProcedureCodes() {
        return procedureCodes;
    }

    public void setProcedureCodes(Collection<Code> procedureCodes) {
        this.procedureCodes = procedureCodes;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Collection<Series> getSeries() {
        return series;
    }

    public void setAttributes(Attributes attrs, AttributeFilter filter, FuzzyStr fuzzyStr) {
        studyInstanceUID = attrs.getString(Tag.StudyInstanceUID);
        studyID = attrs.getString(Tag.StudyID, "*");
        studyDescription = attrs.getString(Tag.StudyDescription, "*");
        Date dt = attrs.getDate(Tag.StudyDateAndTime);
        if (dt != null) {
            studyDate = DateUtils.formatDA(null, dt);
            studyTime = attrs.containsValue(Tag.StudyTime)
                    ? DateUtils.formatTM(null, dt)
                    : "*";
        } else {
            studyDate = "*";
            studyTime = "*";
        }
        accessionNumber = attrs.getString(Tag.AccessionNumber, "*");
        referringPhysicianName = PersonName.valueOf(
                attrs.getString(Tag.ReferringPhysicianName), fuzzyStr,
                referringPhysicianName);
        studyCustomAttribute1 = 
            AttributeFilter.selectStringValue(attrs, filter.getCustomAttribute1(), "*");
        studyCustomAttribute2 =
            AttributeFilter.selectStringValue(attrs, filter.getCustomAttribute2(), "*");
        studyCustomAttribute3 =
            AttributeFilter.selectStringValue(attrs, filter.getCustomAttribute3(), "*");

        encodedAttributes = Utils.encodeAttributes(
                cachedAttributes = new Attributes(attrs, filter.getSelection()));
    }
}
