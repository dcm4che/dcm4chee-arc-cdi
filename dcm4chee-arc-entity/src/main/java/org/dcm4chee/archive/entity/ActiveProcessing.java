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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.dto.ActiveService;
/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
@NamedQueries({
    @NamedQuery(
            name=ActiveProcessing.IS_STUDY_BEING_PROCESSED,
            query="SELECT count(ap) FROM ActiveProcessing ap"
                    + " WHERE ap.studyInstanceUID = :uid AND ap.activeService IN (:serviceList)"
            ),
    @NamedQuery(
            name=ActiveProcessing.FIND_BY_SOP_IUID,
            query="SELECT ap FROM ActiveProcessing ap WHERE ap.sopInstanceUID = :uid"),
    @NamedQuery(
            name=ActiveProcessing.FIND_BY_SOP_IUID_AND_SERVICE,
            query="SELECT ap FROM ActiveProcessing ap WHERE ap.sopInstanceUID = :uid AND ap.activeService = :service"),
    @NamedQuery(
            name=ActiveProcessing.FIND_BY_SOP_IUIDs,
            query="SELECT ap FROM ActiveProcessing ap WHERE ap.sopInstanceUID IN (:uidList)"),
    @NamedQuery(
            name=ActiveProcessing.FIND_BY_SOP_IUIDs_AND_SERVICE,
            query="SELECT ap FROM ActiveProcessing ap WHERE ap.activeService = :service AND ap.sopInstanceUID IN (:uidList)"),
    @NamedQuery(
            name=ActiveProcessing.FIND_BY_SERIES_IUID,
            query="SELECT ap FROM ActiveProcessing ap WHERE ap.seriesInstanceUID = :uid"),
    @NamedQuery(
            name=ActiveProcessing.FIND_BY_SERIES_IUID_AND_SERVICE,
            query="SELECT ap FROM ActiveProcessing ap WHERE ap.seriesInstanceUID = :uid AND ap.activeService = :service"),
    @NamedQuery(
            name=ActiveProcessing.FIND_BY_STUDY_IUID,
            query="SELECT ap FROM ActiveProcessing ap WHERE ap.studyInstanceUID = :uid"),
    @NamedQuery(
            name=ActiveProcessing.FIND_BY_STUDY_IUID_AND_SERVICE,
            query="SELECT ap FROM ActiveProcessing ap WHERE ap.studyInstanceUID = :uid AND ap.activeService = :service"),
    @NamedQuery(
            name=ActiveProcessing.DELETE_BY_SOP_IUID_AND_SERVICE,
            query="DELETE FROM ActiveProcessing ap WHERE ap.sopInstanceUID = :uid AND ap.activeService = :service"),
    @NamedQuery(
            name=ActiveProcessing.DELETE_BY_SOP_IUIDs_AND_SERVICE,
            query="DELETE FROM ActiveProcessing ap WHERE ap.sopInstanceUID IN (:uidList) AND ap.activeService = :service"),
})
@Entity
@Table(name = "active_processing", uniqueConstraints = 
@UniqueConstraint(
        columnNames={"sop_iuid","active_service"}))
public class ActiveProcessing implements Serializable {

    private static final long serialVersionUID = 8938116804951734177L;

    public static final String FIND_BY_SOP_IUID = "ActiveProcessing.findBySOPInstanceUID";
    public static final String FIND_BY_SOP_IUID_AND_SERVICE = "ActiveProcessing.findBySOPInstanceUIDAndService";

    public static final String FIND_BY_SOP_IUIDs = "ActiveProcessing.findBySOPInstanceUIDs";
    public static final String FIND_BY_SOP_IUIDs_AND_SERVICE = "ActiveProcessing.findBySOPInstanceUIDsAndServcice";

    public static final String FIND_BY_SERIES_IUID = "ActiveProcessing.findBySeriesInstanceUID";
    public static final String FIND_BY_SERIES_IUID_AND_SERVICE = "ActiveProcessing.findBySeriesInstanceUIDAndService";

    public static final String FIND_BY_STUDY_IUID = "ActiveProcessing.findByStudyInstanceUID";
    public static final String FIND_BY_STUDY_IUID_AND_SERVICE = "ActiveProcessing.findByStudyInstanceUIDAndService";

    public static final String DELETE_BY_SOP_IUID_AND_SERVICE = "ActiveProcessing.deleteBySOPInstanceUIDAndService";
    public static final String DELETE_BY_SOP_IUIDs_AND_SERVICE = "ActiveProcessing.deleteBySOPInstanceUIDsAndService";

    public static final String IS_STUDY_BEING_PROCESSED = "ActiveProcessing.isStudyBeingProcessed";
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "sop_iuid", updatable = false)
    private String sopInstanceUID;

    @Basic(optional = false)
    @Column(name = "series_iuid", updatable = false)
    private String seriesInstanceUID;

    @Basic(optional = false)
    @Column(name = "study_iuid", updatable = false)
    private String studyInstanceUID;

    @Basic(optional = true)
    @Column(name = "active_service", updatable = false)
    private ActiveService activeService;

    @Column(name = "created_time", updatable = false)
    private Date createdTime;

    @Column(name = "updated_time")
    private Date updatedTime;

    @OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval = true, optional = true)
    @JoinColumn(name = "dicomattrs_fk")
    private AttributesBlob attributesBlob;

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

    public final long getPk() {
        return pk;
    }

    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setSopInstanceUID(String sopInstanceUID) {
        this.sopInstanceUID = sopInstanceUID;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public void setSeriesInstanceUID(String seriesInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public ActiveService getActiveService() {
        return activeService;
    }

    public void setActiveService(ActiveService activeService) {
        this.activeService = activeService;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public AttributesBlob getAttributesBlob() {
        return attributesBlob;
    }
    
    public Attributes getAttributes() throws BlobCorruptedException {
        return attributesBlob == null ? null : attributesBlob.getAttributes();
    }

    public void setAttributes(Attributes attrs) {
        if (attrs == null) 
            attributesBlob = null;
        else if (attributesBlob == null)
            attributesBlob = new AttributesBlob(attrs);
        else
            attributesBlob.setAttributes(attrs);
    }
    
    @Override
    public String toString() {
        return "ActiveProcessing Entry [pk=" + pk 
                + ", study=" + studyInstanceUID
                + ", series=" + seriesInstanceUID
                + ", instance=" + sopInstanceUID
                + ", process=" + activeService
                + "]";
    }
}
