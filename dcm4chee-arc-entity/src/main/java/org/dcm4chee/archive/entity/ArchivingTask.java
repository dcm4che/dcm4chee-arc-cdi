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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@NamedQueries({
@NamedQuery(
    name=ArchivingTask.FIND_BY_SERIES_INSTANCE_UID,
    query="SELECT a FROM ArchivingTask a "
        + "WHERE a.seriesInstanceUID = ?1 "),
@NamedQuery(
    name=ArchivingTask.FIND_READY_TO_ARCHIVE,
    query="SELECT a FROM ArchivingTask a "
        + "WHERE a.archivingTime <= CURRENT_TIMESTAMP AND a.delayReasonCode IS NULL "
        + "ORDER BY a.archivingTime")
})
@Entity
@Table(name = "archiving_task")
public class ArchivingTask implements Serializable {

    private static final long serialVersionUID = -5144838704218060188L;

    public static final String FIND_BY_SERIES_INSTANCE_UID =
            "ArchivingTask.findBySeriesInstanceUID";

    public static final String FIND_READY_TO_ARCHIVE =
            "ArchivingTask.findReadyToArchive";

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "series_iuid", updatable = false)
    private String seriesInstanceUID;

    @Basic(optional = false)
    @Column(name = "target_stg_group_ids", updatable = false)
    private String targetStorageSystemGroupIDs;

    @Basic(optional = false)
    @Column(name = "source_stg_group_id", updatable = false)
    private String sourceStorageSystemGroupID;

    @Basic(optional = false)
    @Column(name = "target_name", updatable = false)
    private String targetName;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "archiving_time")
    private Date archivingTime;

    @ManyToOne
    @JoinColumn(name = "delay_reason_code_fk")
    private Code delayReasonCode;

    public final long getPk() {
        return pk;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public void setSeriesInstanceUID(String seriesInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public Date getArchivingTime() {
        return archivingTime;
    }

    public void setArchivingTime(Date archivingTime) {
        this.archivingTime = archivingTime;
    }

    public Code getDelayReasonCode() {
        return delayReasonCode;
    }

    public void setDelayReasonCode(Code delayReasonCode) {
        this.delayReasonCode = delayReasonCode;
    }

    public String[] getTargetStorageSystemGroupIDs() {
        return StringUtils.split(targetStorageSystemGroupIDs, '\\');
    }

    public void setTargetStorageSystemGroupIDs(
            String... targetStorageSystemGroupIDs) {
        this.targetStorageSystemGroupIDs = StringUtils.concat(
                targetStorageSystemGroupIDs, '\\');
    }

    public String getSourceStorageSystemGroupID() {
        return sourceStorageSystemGroupID;
    }

    public void setSourceStorageSystemGroupID(String sourceStorageSystemGroupID) {
        this.sourceStorageSystemGroupID = sourceStorageSystemGroupID;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @Override
    public String toString() {
        return "ArchivingTask[pk=" + pk 
                + ", series=" + seriesInstanceUID
                + ", sourceStorageGroupID=" + sourceStorageSystemGroupID
                + ", targetStorageGroupIDs=" + targetStorageSystemGroupIDs
                + "]";
    }
}
