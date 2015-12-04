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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.data.Tag;
import org.dcm4che3.util.DateUtils;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 29, 2008
 */
@NamedQueries({
    @NamedQuery(
            name=MPPS.FIND_BY_SOP_INSTANCE_UID_EAGER,
            query="SELECT mpps FROM MPPS mpps JOIN FETCH mpps.attributesBlob  WHERE mpps.sopInstanceUID = ?1"),
    @NamedQuery(
            name="MPPS.findBySOPInstanceUID",
            query="SELECT mpps FROM MPPS mpps WHERE mpps.sopInstanceUID = ?1"),
    @NamedQuery(
            name="MPPS.findBySOPInstanceUIDs",
            query="SELECT mpps FROM MPPS mpps WHERE mpps.sopInstanceUID IN :idList"),
    @NamedQuery(
            name="MPPS.deleteBySOPInstanceUIDs",
            query="DELETE FROM MPPS mpps WHERE mpps.sopInstanceUID IN :idList"),
})
@Entity
@Table(name = "mpps")
public class MPPS implements Serializable {

    public boolean discontinuedForReason(Code reasonCode) {
        return getStatus() == Status.DISCONTINUED
                && getDiscontinuationReasonCode() != null
                && getDiscontinuationReasonCode().equals(reasonCode);
    }

    public enum Status {
        IN_PROGRESS, COMPLETED, DISCONTINUED;
    }

    public static final String FIND_BY_SOP_INSTANCE_UID =
            "MPPS.findBySOPInstanceUID";
    public static final String FIND_BY_SOP_INSTANCE_UID_EAGER =
            "MPPS.findBySOPInstanceUIDEager";
    public static final String FIND_BY_SOP_INSTANCE_UIDs =
            "MPPS.findBySOPInstanceUIDs";
    public static final String DELETE_BY_SOP_INSTANCE_UIDs =
            "MPPS.deleteBySOPInstanceUIDs";

    public static final String IN_PROGRESS = "IN PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String DISCONTINUED = "DISCONTINUED";

    private static final long serialVersionUID = -599495313070741738L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;
    
    @Version
    @Column(name = "version")
    private long version;  

    //@Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private Date createdTime;

    //@Basic(optional = false)
    @Column(name = "updated_time")
    private Date updatedTime;

    //@Basic(optional = false)
    @Column(name = "mpps_iuid", updatable = true, unique = true)
    private String sopInstanceUID;

    //@Basic(optional = false)
    @Column(name = "pps_start", nullable=true)
    private Date startDateTime;

    //@Basic(optional = false)
    @Column(name = "station_aet")
    private String performedStationAET;

    //@Basic(optional = false)
    @Column(name = "modality")
    private String modality;

    @Column(name = "accession_no")
    private String accessionNumber;

    //@Basic(optional = false)
    @Column(name = "mpps_status")
    private Status status;

    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "dicomattrs_fk")
    private AttributesBlob attributesBlob;

    @ManyToOne
    @JoinColumn(name = "drcode_fk")
    private Code discontinuationReasonCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_fk")
    private Patient patient;

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

    public Date getStartDateTime() {
        return startDateTime;
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "MPPS[pk=" + pk
                + ", iuid=" + sopInstanceUID
                + ", status=" + status
                + ", accno=" + accessionNumber
                + ", startDateTime=" + startDateTime.toString()
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

    public AttributesBlob getAttributesBlob() {
        return attributesBlob;
    }
    
    public Attributes getAttributes() throws BlobCorruptedException {
        return attributesBlob.getAttributes();
    }

    public void setAttributes(Attributes attrs, String nullValue) {
        
        Date dt = attrs.getDate(Tag.PerformedProcedureStepStartDateAndTime, new DatePrecision(Calendar.SECOND));
        if (dt != null) {
            Calendar adjustedDateTimeCal = new GregorianCalendar();
            adjustedDateTimeCal.setTime(dt);
            adjustedDateTimeCal.set(Calendar.MILLISECOND, 0);
            startDateTime = adjustedDateTimeCal.getTime();
        }
        this.performedStationAET = attrs.getString(Tag.PerformedStationAETitle);
        this.modality = attrs.getString(Tag.Modality);
        Attributes ssa = attrs.getNestedDataset(
                Tag.ScheduledStepAttributesSequence);
        if (ssa != null)
            this.accessionNumber = ssa.getString(Tag.AccessionNumber);

        Status status = getMPPSStatus(attrs);

        if (status != null)
            this.status = status;

        if (attributesBlob == null)
            attributesBlob = new AttributesBlob(attrs);
        else
            attributesBlob.setAttributes(attrs);
    }

    public static Status getMPPSStatus(Attributes attrs) {
        String s = attrs.getString(Tag.PerformedProcedureStepStatus);
        Status status = null;
        if (s != null) status = Status.valueOf(s.replace(' ', '_'));
        return status;
    }
}
