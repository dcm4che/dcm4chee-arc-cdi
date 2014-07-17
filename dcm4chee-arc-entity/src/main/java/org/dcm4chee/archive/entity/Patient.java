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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.entity.ext.PatientExtension;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
@NamedQueries({
@NamedQuery(
    name="Patient.findByPatientFamilyName",
    query="SELECT p FROM Patient p "
            + "WHERE p.patientName.familyName = ?1")
})
@Entity
@Table(name = "patient")
public class Patient implements Serializable {

    private static final long serialVersionUID = 6430339764844147679L;

    public static final String FIND_BY_PATIENT_FAMILY_NAME =
            "Patient.findByPatientFamilyName";

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
    @Column(name = "no_pat_id")
    private boolean noPatientID;

    @Basic(optional = false)
    @Column(name = "pat_birthdate")
    private String patientBirthDate;

    @Basic(optional = false)
    @Column(name = "pat_sex")
    private String patientSex;

    @Basic(optional = false)
    @Column(name = "pat_custom1")
    private String patientCustomAttribute1;

    @Basic(optional = false)
    @Column(name = "pat_custom2")
    private String patientCustomAttribute2;

    @Basic(optional = false)
    @Column(name = "pat_custom3")
    private String patientCustomAttribute3;

    @Basic(optional = false)
    @Column(name = "pat_attrs")
    private byte[] encodedAttributes;

    @Embedded
    private PatientExtension extension;

    @Transient
    private Attributes cachedAttributes;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pat_name_fk")
    private PersonName patientName;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "merge_fk")
    private Patient mergedWith;

    @OneToMany(mappedBy = "mergedWith", orphanRemoval = true)
    private Collection<Patient> previous;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<PatientID> patientIDs;

    @ManyToMany
    @JoinTable(name = "rel_linked_patient_id", 
        joinColumns = @JoinColumn(name = "patient_fk", referencedColumnName = "pk"),
        inverseJoinColumns = @JoinColumn(name = "patient_id_fk", referencedColumnName = "pk"))
    private Collection<PatientID> linkedPatientIDs;

    @OneToMany(mappedBy = "patient", orphanRemoval = true)
    private Collection<Study> studies;

    @OneToMany(mappedBy = "patient", orphanRemoval = true)
    private Collection<MPPS> modalityPerformedProcedureSteps;

    @OneToMany(mappedBy = "patient", orphanRemoval = true)
    private Collection<MWLItem> modalityWorklistItems;

    @Override
    public String toString() {
        return "Patient[pk=" + pk
                + ", name=" + patientName
                + ", dob=" + patientBirthDate
                + ", sex=" + patientSex
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

    public long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public boolean isNoPatientID() {
        return noPatientID;
    }

    public void setNoPatientID(boolean noPatientID) {
        this.noPatientID = noPatientID;
    }

    public String getPatientBirthDate() {
        return patientBirthDate;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public String getPatientCustomAttribute1() {
        return patientCustomAttribute1;
    }

    public String getPatientCustomAttribute2() {
        return patientCustomAttribute2;
    }

    public String getPatientCustomAttribute3() {
        return patientCustomAttribute3;
    }

    public PatientExtension getExtension() {
        return extension;
    }

    public void setExtension(PatientExtension extension) {
        this.extension = extension;
    }

    public Patient getMergedWith() {
        return mergedWith;
    }

    public PersonName getPatientName() {
        return patientName;
    }

    public void setPatientName(PersonName patientName) {
        this.patientName = patientName;
    }

    public void setMergedWith(Patient mergedWith) {
        this.mergedWith = mergedWith;
    }

    public Collection<Patient> getPrevious() {
        return previous;
    }

    public Collection<PatientID> getPatientIDs() {
        return patientIDs;
    }

    public void setPatientIDs(Collection<PatientID> patientIDs) {
        this.patientIDs = patientIDs;
    }

    public Collection<PatientID> getLinkedPatientIDs() {
        return linkedPatientIDs;
    }

    public void setLinkedPatientIDs(Collection<PatientID> linkedPatientIDs) {
        this.linkedPatientIDs = linkedPatientIDs;
    }

    public Collection<Study> getStudies() {
        return studies;
    }

    public Collection<MPPS> getModalityPerformedProcedureSteps() {
        return modalityPerformedProcedureSteps;
    }

    public Collection<MWLItem> getModalityWorklistItems() {
        return modalityWorklistItems;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public Attributes getAttributes() throws BlobCorruptedException {
        if (cachedAttributes == null)
            cachedAttributes = Utils.decodeAttributes(encodedAttributes);
        return cachedAttributes;
    }

    public void setAttributes(Attributes attrs, AttributeFilter filter, FuzzyStr fuzzyStr) {
        patientName = PersonName.valueOf(
                attrs.getString(Tag.PatientName), fuzzyStr, patientName);
        patientBirthDate = attrs.getString(Tag.PatientBirthDate, "*");
        patientSex = attrs.getString(Tag.PatientSex, "*").toUpperCase();

        patientCustomAttribute1 = 
            AttributeFilter.selectStringValue(attrs, filter.getCustomAttribute1(), "*");
        patientCustomAttribute2 =
            AttributeFilter.selectStringValue(attrs, filter.getCustomAttribute2(), "*");
        patientCustomAttribute3 =
            AttributeFilter.selectStringValue(attrs, filter.getCustomAttribute3(), "*");

        encodedAttributes = Utils.encodeAttributes(
                cachedAttributes = new Attributes(attrs, filter.getSelection()));
    }

    public void updateOtherPatientIDs() {
        Attributes attrs = getAttributes();
        IDWithIssuer pid0 = IDWithIssuer.pidOf(attrs);
        attrs.remove(Tag.IssuerOfPatientID);
        attrs.remove(Tag.IssuerOfPatientIDQualifiersSequence);
        attrs.remove(Tag.OtherPatientIDsSequence);
        Collection<PatientID> allPatientIDs = getAllPatientIDs();
        if (allPatientIDs.isEmpty()) {
            attrs.setNull(Tag.PatientID, VR.LO);
            return;
        } else {
            int numopids = allPatientIDs.size() - 1;
            if (numopids == 0) {
                allPatientIDs.iterator().next().toIDWithIssuer()
                    .exportPatientIDWithIssuer(attrs);
            } else {
                Sequence opidsSeq = attrs.newSequence(
                        Tag.OtherPatientIDsSequence, numopids);
                for (PatientID patientID : allPatientIDs) {
                    IDWithIssuer opid = patientID.toIDWithIssuer();
                    if (pid0 != null && pid0.matches(opid)) {
                        opid.exportPatientIDWithIssuer(attrs);
                        pid0 = null;
                    } else {
                        opidsSeq.add(opid.exportPatientIDWithIssuer(null));
                    }
                }
            }
        }
        encodedAttributes = Utils.encodeAttributes(attrs);
    }

    private Collection<PatientID> getAllPatientIDs() {
        if (linkedPatientIDs == null || linkedPatientIDs.isEmpty())
            if (patientIDs == null)
                return Collections.emptyList();
            else
                return patientIDs;

        if (patientIDs == null || patientIDs.isEmpty())
            return linkedPatientIDs;

        ArrayList<PatientID> all = new ArrayList<PatientID>(
                patientIDs.size() + linkedPatientIDs.size());
        all.addAll(patientIDs);
        all.addAll(linkedPatientIDs);
        return all;
    }

    public Collection<Patient> getLinkedPatients() {
        if (linkedPatientIDs == null || linkedPatientIDs.isEmpty())
            return Collections.emptyList();

        List<Patient> list = new ArrayList<Patient>(linkedPatientIDs.size());
        for (PatientID pid : linkedPatientIDs) {
            Patient pat = pid.getPatient();
            if (!contains(list, pat))
                list.add(pat);
        }
        return list;
    }

    private static boolean contains(Collection<Patient> pats, Patient other) {
        long pk = other.getPk();
        for (Patient pat  : pats)
            if (pat.getPk() == pk)
                return true;
        return false;
    }
}
