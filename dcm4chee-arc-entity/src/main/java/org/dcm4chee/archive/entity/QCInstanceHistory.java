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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

@Entity
@Table(name="qc_instance_history")
public class QCInstanceHistory implements Serializable{

    private static final long serialVersionUID = -8359497624548247954L;

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
    @Column(name = "old_series_uid", updatable = false)
    private String oldSeriesUID;

    @Basic(optional = false)
    @Column(name = "current_series_uid", updatable = true)
    private String currenSeriestUID;


    @Basic(optional = false)
    @Column(name = "old_study_uid", updatable = false)
    private String oldStudyUID;

    @Basic(optional = false)
    @Column(name = "current_study_uid", updatable = true)
    private String currentStudyUID;

    @Basic(optional = false)
    @Column(name = "cloned", updatable = false)
    private boolean cloned;

    @ManyToOne
    @JoinColumn(name="qc_series_history_fk")
    private QCSeriesHistory series;
    
    public long getPk() {
        return pk;
    }

    public QCInstanceHistory() {}

    public QCInstanceHistory(String oldStudyUID, String currentStudyUID,
            String oldSeriesUID, String currentSeriesUID,
            String oldUID, String currentUID, String nextUID, boolean cloned) {
        this.cloned=cloned;
        this.oldStudyUID = oldStudyUID;
        this.oldSeriesUID = oldSeriesUID;
        this.oldUID = oldUID;
        this.currenSeriestUID = currentSeriesUID;
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

    public String getOldSeriesUID() {
        return oldSeriesUID;
    }

    public void setOldSeriesUID(String oldSeriesUID) {
        this.oldSeriesUID = oldSeriesUID;
    }

    public String getCurrenSeriestUID() {
        return currenSeriestUID;
    }

    public void setCurrenSeriestUID(String currenSeriestUID) {
        this.currenSeriestUID = currenSeriestUID;
    }

    public String getOldStudyUID() {
        return oldStudyUID;
    }

    public void setOldStudyUID(String oldStudyUID) {
        this.oldStudyUID = oldStudyUID;
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

    public QCSeriesHistory getSeries() {
        return series;
    }

    public void setSeries(QCSeriesHistory series) {
        this.series = series;
    }


}
