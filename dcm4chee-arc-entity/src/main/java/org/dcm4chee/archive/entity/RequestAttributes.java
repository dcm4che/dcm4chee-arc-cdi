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
import org.dcm4che3.data.Tag;
import org.dcm4che3.soundex.FuzzyStr;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 29, 2008
 */
@Entity
@Table(name = "series_req")
public class RequestAttributes implements Serializable {

    private static final long serialVersionUID = -5693026277386978780L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    //@Basic(optional = false)
    @Column(name = "accession_no")
    private String accessionNumber;

    //@Basic(optional = false)
    @Column(name = "study_iuid")
    private String studyInstanceUID;

    //@Basic(optional = false)
    @Column(name = "req_proc_id")
    private String requestedProcedureID;

    //@Basic(optional = false)
    @Column(name = "sps_id")
    private String scheduledProcedureStepID;

    //@Basic(optional = false)
    @Column(name = "req_service")
    private String requestingService;

    @OneToOne(cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "req_phys_name_fk")
    private PersonName requestingPhysician;
    
    @ManyToOne
    @JoinColumn(name = "accno_issuer_fk")
    private Issuer issuerOfAccessionNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "series_fk")
    private Series series;
    
    public RequestAttributes() {}

    public RequestAttributes(Attributes attrs, Issuer issuerOfAccessionNumber,FuzzyStr fuzzyStr, String nullValue) {
        studyInstanceUID = attrs.getString(Tag.StudyInstanceUID, nullValue);
        accessionNumber = attrs.getString(Tag.AccessionNumber, nullValue);
        this.issuerOfAccessionNumber = issuerOfAccessionNumber;
        requestedProcedureID = attrs.getString(Tag.RequestedProcedureID, nullValue);
        scheduledProcedureStepID = attrs.getString(Tag.ScheduledProcedureStepID, nullValue);
        requestingService = attrs.getString(Tag.RequestingService, nullValue);
    }

    public long getPk() {
        return pk;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public String getRequestedProcedureID() {
        return requestedProcedureID;
    }

    public String getScheduledProcedureStepID() {
        return scheduledProcedureStepID;
    }

    public String getRequestingService() {
        return requestingService;
    }

    public PersonName getRequestingPhysician() {
        return requestingPhysician;
    }

    public void setRequestingPhysician(PersonName requestingPhysician) {
        this.requestingPhysician = requestingPhysician;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public Issuer getIssuerOfAccessionNumber() {
        return issuerOfAccessionNumber;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    @Override
    public String toString() {
        return "RequestAttributes[pk=" + pk
                + ", suid=" + studyInstanceUID
                + ", rpid=" + requestedProcedureID
                + ", spsid=" + scheduledProcedureStepID
                + "]";
    }

}
