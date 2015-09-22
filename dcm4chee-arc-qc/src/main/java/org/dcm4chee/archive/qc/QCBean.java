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
 * Portions created by the Initial Developer are Copyright (C) 2013
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
package org.dcm4chee.archive.qc;

import java.util.Collection;

import javax.persistence.EntityNotFoundException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;

import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope;
import org.dcm4chee.archive.qc.impl.QCPostProcessor;

/**
 * Quality Control Bean
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

public interface QCBean {

    /**
     * MergeStudies.
     * Merge each of the studies provided to the provided target study.
     * The method also takes care to enrich study and series attributes.
     * Throws an EJBException if the patient is different for any of the
     * source studies or the target study. Throws an EJB Exception if any
     * of the studies are not found. Utilizes the move method for performing
     * the suggested sub-operations.
     * 
     * @param sourceStudyUids
     *            the source study uids
     * @param targetStudyUID
     *            the target study uid
     * @param targetStudyattributes
     *            the target study attributes
     * @param targetSeriesattributes
     *            the target series attributess
     * @param qcRejectionCode
     *            the QC rejection code utilized by move
     * @return the QC event
     */
    public QCEvent mergeStudies(String[] sourceStudyUids, String targetStudyUID,
            Attributes targetStudyattributes, Attributes targetSeriesattributes,
            Code qcRejectionCode);

    /**
     * Merge.
     * Used to perform the merge operation per study. The method is used by the 
     * @link {@link #mergeStudies(String[], String, Attributes, Attributes, Code)}
     * to perform the suggested operation per source study.
     *  
     * @param sourceStudyUid
     *            the source study uid
     * @param targetStudyUid
     *            the target study uid
     * @param targetStudyattributes
     *            the target study attributes
     * @param targetSeriesattributes
     *            the target series attributes
     * @param qcRejectionCode
     *            the QC rejection code
     * @return the QC event
     */
    public QCEvent merge(String sourceStudyUid, String targetStudyUid,
            Attributes targetStudyattributes, Attributes targetSeriesattributes,
            Code qcRejectionCode);

    /**
     * Split.
     * Split study is a combined operation that uses the move basic operation.
     * Given a set of instances to move the method will create a study in case
     * the old study doesn't exist, using the provided createStudyAttributes
     * to enrich the created study. The method will also enrich the created
     * series with the targetSeriesAttributes (if provided).
     * Throws an EJBException if the patient is different for any of the
     * source study or the target study. Throws an EJB Exception if any
     * of the instances are not found or do not belong to the same study.
     * 
     * @param toMove
     *            the to move
     * @param pid
     *            the patient id
     * @param targetStudyUID
     *            the target study uid
     * @param createdStudyattributes
     *            the created study attributes
     * @param targetSeriesattributes
     *            the target series attributes
     * @param qcRejectionCode
     *            the QC rejection code
     * @return the QC event
     */
    public QCEvent split(Collection<String> toMove, IDWithIssuer pid,
            String targetStudyUID, Attributes createdStudyattributes,
            Attributes targetSeriesattributes, Code qcRejectionCode);

    /**
     * Segment.
     * The segment operation is used to group instances into different 
     * studies/series (virtually segments/groups)
     * The operation takes two collections to move and to clone and
     * performs the appropriate action accordingly.
     * The method will use the provided attributes for study and/or series
     * to enrich the objects created. The method will throw EJB Exception
     * if all instances to move or to clone don't have the same study.
     * Throws an EJBException if the patient is different for any of the
     * source study or the target study.
     * 
     * @param toMove
     *            the to move
     * @param toClone
     *            the to clone
     * @param pid
     *            the pid
     * @param targetStudyUID
     *            the target study uid
     * @param targetStudyattributes
     *            the target study attributes
     * @param targetSeriesattributes
     *            the target series attributes
     * @param qcRejectionCode
     *            the QC rejection code
     * @return the QC event
     */
    public QCEvent segment(Collection<String> toMove, Collection<String> toClone,
            IDWithIssuer pid, String targetStudyUID,
            Attributes targetStudyattributes, Attributes targetSeriesattributes,
            Code qcRejectionCode);

    /**
     * Segment frame.
     * Used to apply the same operation Segment
     * @link {@link #segment(Collection, Collection,
     *  IDWithIssuer, String, Attributes, Attributes, Code)}
     * however only extracting the provided frame in the frame number.
     * 
     * @param toMove
     *            the to move
     * @param toClone
     *            the to clone
     * @param frame
     *            the frame
     * @param pid
     *            the patient id
     * @param targetStudyUID
     *            the target study uid
     * @param targetStudyattributes
     *            the target study attributes
     * @param targetSeriesattributes
     *            the target series attributes
     */
    public void segmentFrame(Instance toMove, Instance toClone,
            int frame, PatientID pid, String targetStudyUID,
            Attributes targetStudyattributes, Attributes targetSeriesattributes);
/*
    *//**
     * Move.
     * 
     * A basic operation that once called on an instance will make a copy
     * of the instance without actually doing file operations by rather by
     * linking the new instance to the file of the old one.
     * The method also rejects the old instance with the provided code.
     * 
     * @param source
     *            the source
     * @param target
     *            the target
     * @param qcRejectionCode
     *            the QC rejection code
     * @return the new instance
     *//*
     Instance move(Instance source, Series target, Code qcRejectionCode);

    *//**
     * Clone.
     * The clone operations is a basic operation that once called on an instance
     * will make a copy of the instance without actually doing any file operations
     * but rather linking the new instance to the file of the old one.
     * Unlike @link {@link #move(Instance, Series, Code)} no rejection applies.
     * 
     * @param source
     *            the source
     * @param target
     *            the target
     * @return the new instance
     *//*
    public Instance clone(Instance source, Series target);*/

    /**
     * Can apply QC.
     * Checks if the method provided by the sopInstanceUID is rejected.
     * @param sopInstanceUID
     *            the sop instance uid
     * @return true, if the object is not rejected
     */
    public boolean canApplyQC(Instance sopInstanceUID);

    /**
     * Notify.
     * 
     * Used to trigger the QC post processing work flow after each QC is successful.
     * fires the CDI event observer by @see {@link QCPostProcessor#observeQC(QCEvent)}
     * 
     * @param event
     *            the event
     */
    public void notify(QCEvent event);

    /**
     * Update dicom object.
     * Used to update Patient/Study/Series/Instance with the provided attributes.
     * The method will create any entities required aand associate them with the
     * object being updated, the operation is reversible.
     * @param arcDevExt
     *            the arc dev ext
     * @param scope
     *            the scope
     * @param attributes
     *            the attributes
     * @return the QC event
     * @throws EntityNotFoundException
     *             the entity not found exception
     */
    QCEvent updateDicomObject(ArchiveDeviceExtension arcDevExt, QCUpdateScope scope,
            Attributes attributes) throws EntityNotFoundException;
    
    /**
     * Patient operation.
     * Patient operations are those operations supported by the patientservice
     * This interface provides a way to trigger them without having to use HL7
     * operations performed include link/unlink/merge/updateid
     * 
     * @param sourcePatientAttributes
     *            the source patient attributes
     * @param targetPatientAttributes
     *            the target patient attributes
     * @param arcAEExt
     *            the arc ae ext
     * @param command
     *            the command
     * @return true, if successful
     */
    boolean patientOperation(Attributes sourcePatientAttributes,
            Attributes targetPatientAttributes, ArchiveAEExtension arcAEExt,
            PatientCommands command);

    /**
     * Locate instances.
     * Used to locate the instances entities identified by the string array of 
     * sopInstanceUIDs from the archive.

     * @param strings
     *            the strings
     * @return the collection
     */
    public Collection<Instance> locateInstances(String[] strings);

    /**
     * Delete study.
     * Deletes a study permanently
     * The method will use the archives delete queue to perform asynchronously
     * 
     * @param studyInstanceUID
     *            the study instance uid
     * @return the QC event
     * @throws Exception
     *             the exception
     */
    public QCEvent deletePatient(IDWithIssuer pid, Code qcRejectionCode) throws Exception;

    /**
     * Delete study.
     * Deletes a study permanently
     * The method will use the archives delete queue to perform asynchronously
     * 
     * @param studyInstanceUID
     *            the study instance uid
     * @return the QC event
     * @throws Exception
     *             the exception
     */
    public QCEvent deleteStudy(String studyInstanceUID, Code qcRejectionCode) throws Exception;

    /**
     * Delete series.
     * Deletes a series permanently
     * The method will use the archives delete queue to perform asynchronously
     * 
     * @param seriesInstanceUID
     *            the series instance uid
     * @return the QC event
     * @throws Exception
     *             the exception
     */
    public QCEvent deleteSeries(String seriesInstanceUID, Code qcRejectionCode) throws Exception;

    /**
     * Delete instance.
     * Deletes an instance permanently
     * The method will use the archives delete queue to perform asynchronously
     * 
     * @param sopInstanceUID
     *            the sop instance uid
     * @return the QC event
     * @throws Exception
     *             the exception
     */
    public QCEvent deleteInstance(String sopInstanceUID, Code qcRejectionCode) throws Exception;

    /**
     * Delete patient if empty.
     * Purges an empty patient.
     * 
     * @param pid
     *            the patient id
     * @return true, if successful
     */
    boolean deletePatientIfEmpty(IDWithIssuer pid);

    /**
     * Delete study if empty.
     * Purges an empty study.
     * 
     * @param studyInstanceUID
     *            the study instance uid
     * @return true, if successful
     */
    boolean deleteStudyIfEmpty(String studyInstanceUID);

    /**
     * Delete series if empty.
     * Purges an empty series.
     * 
     * @param seriesInstanceUID
     *            the series instance uid
     * @param studyInstanceUID
     *            the study instance uid
     * @return true, if successful
     */
    boolean deleteSeriesIfEmpty(String seriesInstanceUID,
            String studyInstanceUID);

    /**
     * Reject.
     * Used to call the rejection service on some instance give a code.
     * This interface is used by the restful service.
     * 
     * @param sopInstanceUIDs
     *            the sop instance ui ds
     * @param qcRejectionCode
     *            the QC rejection code
     * @return the QC event
     */
    QCEvent reject(String[] sopInstanceUIDs, Code qcRejectionCode);

    /**
     * Restore.
     * Used to remove the previously applied rejection code on an instance.
     * 
     * @param sopInstanceUIDs
     *            the sop instance ui ds
     * @return the QC event
     */
    QCEvent restore(String[] sopInstanceUIDs);

    /**
     * Gets the patient from the archive.
     * 
     * @param attrs
     *            the attributes with the patient id and issuer
     * @return the patient
     */
    public Patient findPatient(Attributes attrs);

    /**
     * Split.
     * Split study is a combined operation that uses the move basic operation.
     * Given a set of instances to move the method will create a study in case
     * the old study doesn't exist, using the provided createStudyAttributes
     * to enrich the created study. The method will also enrich the created
     * series with the targetSeriesAttributes (if provided).
     * Throws an EJBException if the patient is different for any of the
     * source study or the target study. Throws an EJB Exception if any
     * of the instances are not found or do not belong to the same study.
     * 
     * @param toMove
     *            the to move
     * @param pid
     *            the patient id
     * @param targetStudyUID
     *            the target study uid
     * @param createdStudyattributes
     *            the created study attributes
     * @param targetSeriesattributes
     *            the target series attributes
     * @param qcRejectionCode
     *            the QC rejection code
     * @param noneIOCMAET
     *            the none iocm compliant AET
     * @return the QC event
     */
/*    public QCEvent splitNoneIOCM(Collection<String> toMove, IDWithIssuer pid,
            String targetStudyUID, Attributes createdStudyattributes,
            Attributes targetSeriesattributes, Code qcRejectionCode, String noneIOCMAET);*/

}
