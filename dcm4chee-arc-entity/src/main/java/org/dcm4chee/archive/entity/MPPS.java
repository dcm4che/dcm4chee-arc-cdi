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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 29, 2008
 */
@NamedQueries({
    @NamedQuery(
            name="MPPS.findBySOPInstanceUID",
            query="SELECT mpps FROM MPPS mpps WHERE mpps.sopInstanceUID = ?1)")
})
@Entity
@Table(name = "mpps4")
public class MPPS implements Serializable {

    public enum Status {
        IN_PROGRESS, COMPLETED, DISCONTINUED;
    }

    public static final String FIND_BY_SOP_INSTANCE_UID =
            "MPPS.findBySOPInstanceUID";

    public static final String IN_PROGRESS = "IN PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String DISCONTINUED = "DISCONTINUED";

    private static final long serialVersionUID = -599495313070741738L;

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
    @Column(name = "mpps_iuid", unique = true)
    private String sopInstanceUID;

    @Basic(optional = false)
    @Column(name = "pps_start_date")
    private String startDate;

    @Basic(optional = false)
    @Column(name = "pps_start_time")
    private String startTime;

    @Basic(optional = false)
    @Column(name = "station_aet")
    private String performedStationAET;

    @Basic(optional = false)
    @Column(name = "modality")
    private String modality;

    @Column(name = "accession_no")
    private String accessionNumber;

    @Basic(optional = false)
    @Column(name = "mpps_status")
    private Status status;

    @Basic(optional = false)
    @Column(name = "mpps_attrs")
    private byte[] encodedAttributes;

    @ManyToOne
    @JoinColumn(name = "drcode_fk")
    private Code discontinuationReasonCode;

    @ManyToOne
    @JoinColumn(name = "patient_fk")
    private Patient patient;

    @Transient
    private Attributes cachedAttributes;

    public long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setSopInstanceUID(String sopInstanceUID) {
        this.sopInstanceUID = sopInstanceUID;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getPerformedStationAET() {
        return performedStationAET;
    }

    public String getModality() {
        return modality;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public Status getStatus() {
        return status;
    }

    public byte[] getEncodedAttributes() {
        return encodedAttributes;
    }

    public Code getDiscontinuationReasonCode() {
        return discontinuationReasonCode;
    }

    public void setDiscontinuationReasonCode(Code discontinuationReasonCode) {
        this.discontinuationReasonCode = discontinuationReasonCode;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @Override
    public String toString() {
        return "MPPS[pk=" + pk
                + ", iuid=" + sopInstanceUID
                + ", status=" + status
                + ", accno=" + accessionNumber
                + ", startDate=" + startDate
                + ", startTime=" + startTime
                 + ", mod=" + modality
                + ", aet=" + performedStationAET
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

    public Attributes getAttributes() {
        if (cachedAttributes == null)
            cachedAttributes = Utils.decodeAttributes(encodedAttributes);
        return cachedAttributes;
    }

    public void setAttributes(Attributes attrs) {
        this.startDate = attrs.getString(Tag.PerformedProcedureStepStartDate);
        this.startTime = attrs.getString(Tag.PerformedProcedureStepStartTime);
        this.performedStationAET = attrs.getString(Tag.PerformedStationAETitle);
        this.modality = attrs.getString(Tag.Modality);
        Attributes ssa = attrs.getNestedDataset(
                Tag.ScheduledStepAttributesSequence);
        if (ssa != null)
            this.accessionNumber = ssa.getString(Tag.AccessionNumber);
        String s = attrs.getString(Tag.PerformedProcedureStepStatus);
        if (s != null)
            status = Status.valueOf(s.replace(' ', '_'));
        encodedAttributes = Utils.encodeAttributes(cachedAttributes = attrs);
    }
}
