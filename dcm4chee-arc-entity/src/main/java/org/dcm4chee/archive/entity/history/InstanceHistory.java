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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.dcm4chee.archive.entity.AttributesBlob;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

@NamedQueries({
@NamedQuery(
    name="InstanceHistory.findByCurrentUID",
    query="SELECT ih FROM InstanceHistory ih "
            + "JOIN ih.series seh "
            + "JOIN seh.study sth "
            + "JOIN sth.action qca "
            + "where ih.currentUID = ?1 "),

@NamedQuery(
    name="InstanceHistory.findByCurrentUIDForAction",
    query="SELECT ih FROM InstanceHistory ih "
            + "JOIN ih.series seh "
            + "JOIN seh.study sth "
            + "JOIN sth.action qca "
            + "where ih.currentUID = ?1 AND qca.action = ?2 "),

@NamedQuery(
    name="InstanceHistory.findByOldUID",
    query="SELECT ih FROM InstanceHistory ih "
            + "LEFT JOIN FETCH ih.series seh "
            + "LEFT JOIN FETCH seh.updatedAttributesBlob "
            + "LEFT JOIN FETCH seh.study sth "
            + "LEFT JOIN FETCH sth.updatedAttributesBlob "
            + "LEFT JOIN FETCH sth.action qca "
            + "where ih.oldUID = ?1"),
@NamedQuery(
    name="InstanceHistory.studyExistsInQCHistoryAsOldOrNext",
    query="SELECT sth.oldStudyUID FROM StudyHistory sth "
            + " where exists "
            + " (select sth2.oldStudyUID from StudyHistory sth2"
            + " where sth2.oldStudyUID = ?1 OR sth2.nextStudyUID = ?1)"
    ),
@NamedQuery(
    name="InstanceHistory.studiesExistsInQCHistoryAsOldOrNext",
    query="SELECT sth.oldStudyUID FROM StudyHistory sth "
            + " where exists "
            + " (select sth2.oldStudyUID from StudyHistory sth2"
            + " where sth2.oldStudyUID IN (:uids) OR sth2.nextStudyUID IN (:uids))"
    ),
@NamedQuery(
    name="InstanceHistory.findDistinctInstancesWhereStudyOldOrCurrentInList",
    query="SELECT DISTINCT ih , ih.series.study.action.createdTime from InstanceHistory ih  "
            + "LEFT JOIN ih.series.study sth "
            + "where sth.oldStudyUID IN (:uids) "
            + "OR ih.currentStudyUID IN (:uids) order by ih.series.study.action.createdTime DESC"
    ),
@NamedQuery(
    name="InstanceHistory.findByOldStudyUIDs",
    query="SELECT ih from InstanceHistory ih LEFT JOIN ih.series.study qcs WHERE qcs.oldStudyUID IN (:uids)"
    ),
@NamedQuery(
    name="InstanceHistory.findByOldSeriesUIDs",
    query="SELECT ih from InstanceHistory ih LEFT JOIN ih.series qcs WHERE qcs.oldSeriesUID IN (:uids)"
    ),
@NamedQuery(
    name="InstanceHistory.findByOldSopUIDs",
    query="SELECT ih from InstanceHistory ih WHERE ih.currentUID IN (SELECT DISTINCT sub.currentUID from InstanceHistory sub WHERE sub.oldUID IN (:uids))"
    ),
@NamedQuery(
    name="InstanceHistory.findByNewStudyUIDs",
    query="SELECT ih from InstanceHistory ih LEFT JOIN ih.series.study qcs WHERE ih.currentStudyUID IN (:uids)"
    ),
@NamedQuery(
    name="InstanceHistory.findByNewSeriesUIDs",
    query="SELECT ih from InstanceHistory ih LEFT JOIN ih.series qcs WHERE ih.currentSeriesUID IN (:uids)"
    ),
@NamedQuery(
    name="InstanceHistory.findByNewSopUIDs",
    query="SELECT ih from InstanceHistory ih WHERE ih.currentUID IN (:uids)"
    )

})

@Entity
@Table(name="instance_history")
public class InstanceHistory implements Serializable{

    private static final long serialVersionUID = -8359497624548247954L;

    public static final String FIND_BY_CURRENT_UID="InstanceHistory.findByCurrentUID";
    public static final String FIND_BY_OLD_UID="InstanceHistory.findByOldUID";
    public static final String FIND_BY_CURRENT_UID_FOR_ACTION="InstanceHistory.findByCurrentUIDForAction";
    public static final String STUDY_EXISTS_IN_QC_HISTORY_AS_OLD_OR_NEXT= "InstanceHistory.studyExistsInQCHistoryAsOldOrNext";
    public static final String STUDIES_EXISTS_IN_QC_HISTORY_AS_OLD_OR_NEXT= "InstanceHistory.studiesExistsInQCHistoryAsOldOrNext";
    public static final String FIND_DISTINCT_INSTANCES_WHERE_STUDY_OLD_OR_CURRENT_IN_LIST = "InstanceHistory.findDistinctInstancesWhereStudyOldOrCurrentInList";
    public static final String FIND_BY_OLD_STUDY_UIDS = "InstanceHistory.findByOldStudyUIDs";
    public static final String FIND_BY_OLD_SERIES_UIDS = "InstanceHistory.findByOldSeriesUIDs";
    public static final String FIND_BY_OLD_SOP_UIDS = "InstanceHistory.findByOldSopUIDs";
    public static final String FIND_BY_NEW_STUDY_UIDS = "InstanceHistory.findByNewStudyUIDs";
    public static final String FIND_BY_NEW_SERIES_UIDS = "InstanceHistory.findByNewSeriesUIDs";
    public static final String FIND_BY_NEW_SOP_UIDS = "InstanceHistory.findByNewSopUIDs";
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "old_uid", updatable = false)
    private String oldUID;

    @Basic(optional = false)
    @Column(name = "current_uid", updatable = true)
    private String currentUID;

    @Basic(optional = false)
    @Column(name = "next_uid", updatable = false)
    private String nextUID;

    @Basic(optional = false)
    @Column(name = "current_series_uid", updatable = true)
    private String currentSeriesUID;

    @Basic(optional = false)
    @Column(name = "current_study_uid", updatable = true)
    private String currentStudyUID;

    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval = true, optional=true)
    @JoinColumn(name = "dicomattrs_fk")
    private AttributesBlob previousAttributesBlob;

    @Basic(optional = false)
    @Column(name = "cloned", updatable = false)
    private boolean cloned;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="series_history_fk")
    private SeriesHistory series;
    
    public long getPk() {
        return pk;
    }

    public InstanceHistory() {}

    public InstanceHistory(String currentStudyUID,
            String currentSeriesUID, String oldUID,
            String currentUID, String nextUID, boolean cloned) {
        this.cloned=cloned;
        this.oldUID = oldUID;
        this.currentSeriesUID = currentSeriesUID;
        this.currentStudyUID = currentStudyUID;
        this.currentUID = currentUID;
        this.nextUID = nextUID;
    }
    public String getOldUID() {
        return oldUID;
    }

    public void setOldUID(String oldUID) {
        this.oldUID = oldUID;
    }

    public String getCurrentUID() {
        return currentUID;
    }

    public void setCurrentUID(String currentUID) {
        this.currentUID = currentUID;
    }

    public String getNextUID() {
        return nextUID;
    }

    public void setNextUID(String nextUID) {
        this.nextUID = nextUID;
    }

    public String getCurrentSeriesUID() {
        return currentSeriesUID;
    }

    public void setCurrentSeriesUID(String currenSeriestUID) {
        this.currentSeriesUID = currenSeriestUID;
    }

    public String getCurrentStudyUID() {
        return currentStudyUID;
    }

    public void setCurrentStudyUID(String currentStudyUID) {
        this.currentStudyUID = currentStudyUID;
    }

    public boolean isCloned() {
        return cloned;
    }

    public void setCloned(boolean cloned) {
        this.cloned = cloned;
    }

    public SeriesHistory getSeries() {
        return series;
    }

    public void setSeries(SeriesHistory series) {
        this.series = series;
    }

    public AttributesBlob getPreviousAtributesBlob() {
        return previousAttributesBlob;
    }

    public void setPreviousAtributesBlob(AttributesBlob previousAtributesBlob) {
        this.previousAttributesBlob = new AttributesBlob(previousAtributesBlob.getAttributes());
    }

    @Override
    public String toString() {
        return "InstanceHistory[pk=" + pk
                + ", oldUID= " + oldUID
                + ", nextUID= " + nextUID
                + ", currentUID= " + currentUID
                + ", currentSeriesUID= " + currentSeriesUID
                + ", currentStudyUID= " + currentStudyUID
                + ", cloned= "+ cloned
                + "]";
    }
}
