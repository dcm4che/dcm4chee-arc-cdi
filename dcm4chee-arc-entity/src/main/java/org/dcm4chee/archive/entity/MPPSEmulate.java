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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@NamedQueries({
@NamedQuery(
    name=MPPSEmulate.FIND_BY_STUDY_INSTANCE_UID_AND_SOURCE_AET,
    query="SELECT e FROM MPPSEmulate e "
        + "WHERE e.studyInstanceUID = ?1 "
        + "AND e.sourceAET = ?2"),
@NamedQuery(
    name=MPPSEmulate.FIND_READY_TO_EMULATE,
    query="SELECT e FROM MPPSEmulate e "
        + "WHERE e.emulationTime <= CURRENT_TIMESTAMP "
        + "ORDER BY e.emulationTime")
})
@Entity
@Table(name = "mpps_emulate")
public class MPPSEmulate implements Serializable {

    private static final long serialVersionUID = -4965589892596116293L;

    public static final String FIND_READY_TO_EMULATE =
            "MPPSEmulaton.findReadyToEmulate";

    public static final String FIND_BY_STUDY_INSTANCE_UID_AND_SOURCE_AET =
            "MPPSEmulaton.findByStudyInstanceUIDAndSourceAET";

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "emulator_aet", updatable = false)
    private String emulatorAET;

    @Basic(optional = false)
    @Column(name = "src_aet", updatable = false)
    private String sourceAET;

    @Basic(optional = false)
    @Column(name = "study_iuid", updatable = false)
    private String studyInstanceUID;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "emulation_time")
    private Date emulationTime;

    public final long getPk() {
        return pk;
    }

    public final String getEmulatorAET() {
        return emulatorAET;
    }

    public final void setEmulatorAET(String emulatorAET) {
        this.emulatorAET = emulatorAET;
    }

    public final String getSourceAET() {
        return sourceAET;
    }

    public final void setSourceAET(String sourceAET) {
        this.sourceAET = sourceAET;
    }

    public final String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public final void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public final Date getEmulationTime() {
        return emulationTime;
    }

    public final void setEmulationTime(Date emulationTime) {
        this.emulationTime = emulationTime;
    }

}
