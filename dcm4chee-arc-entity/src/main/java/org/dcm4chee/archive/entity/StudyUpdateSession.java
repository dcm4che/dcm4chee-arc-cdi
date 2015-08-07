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

import org.dcm4chee.archive.store.session.StudyUpdatedEvent;

import javax.persistence.*;
import java.io.*;
import java.util.Date;

/**
 * Store Study Session denotes a unit of work related to a number of instances of a certain study
 * stored one after another in a batch (possibly on multiple associations).
 *
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Roman K
 */
@NamedQueries({
@NamedQuery(
    name= StudyUpdateSession.FIND_BY_STUDY_INSTANCE_UID_AND_SOURCE_AET,
    query="SELECT e FROM StudyUpdateSession e "
        + "WHERE e.studyInstanceUID = ?1 "
        + "AND e.sourceAET = ?2"),
@NamedQuery(
    name= StudyUpdateSession.FIND_READY_TO_FINISH,
    query="SELECT e FROM StudyUpdateSession e "
        + "WHERE e.emulationTime <= CURRENT_TIMESTAMP "
        + "ORDER BY e.emulationTime")
})
@Entity
@Table(name = "study_update_session",
        uniqueConstraints = {
                // A study being received from a certain AE corresponds to a single store study session
                @UniqueConstraint(columnNames = {"src_aet", "study_iuid"})
        })

public class StudyUpdateSession implements Serializable {

    private static final long serialVersionUID = -4965589892596116293L;

    public static final String FIND_READY_TO_FINISH =
            "StudyUpdateSession.findReadyToFinish";

    public static final String FIND_BY_STUDY_INSTANCE_UID_AND_SOURCE_AET =
            "StudyUpdateSession.findByStudyInstanceUIDAndSourceAET";

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

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


    @Transient
    private StudyUpdatedEvent pendingStudyUpdatedEvent = new StudyUpdatedEvent();

    public StudyUpdatedEvent getPendingStudyUpdatedEvent() {
        return pendingStudyUpdatedEvent;
    }

    @Basic
    @Column(name = "event_blob")
    @Access(AccessType.PROPERTY)
    @Lob
    public byte[] getEventBlob() {
        return serialize(pendingStudyUpdatedEvent);
    }

    @SuppressWarnings("unchecked")
    public void setEventBlob(byte[] eventBlob) {
        try {
            this.pendingStudyUpdatedEvent = (StudyUpdatedEvent) deserialize(eventBlob);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error - event_blob field is corrupted",e);
        }
    }

    public final long getPk() {
        return pk;
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


    private Object deserialize(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("The byte[] must not be null");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream in = null;
        try {
            // stream closed in the finally
            in = new ObjectInputStream(inputStream);
            return in.readObject();

        } catch (ClassNotFoundException | IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }

    }

    private byte[] serialize(Object pendingStudyUpdatedEvent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        ObjectOutputStream out = null;
        try {
            // stream closed in the finally
            out = new ObjectOutputStream(baos);
            out.writeObject(pendingStudyUpdatedEvent);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return baos.toByteArray();
    }

}
