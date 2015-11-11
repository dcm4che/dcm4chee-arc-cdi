//
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

package org.dcm4chee.archive.qc;

import java.util.Collection;

import javax.persistence.EntityNotFoundException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public interface QCService {
    
    /**
     * MergeStudies.
     * Merge each of the studies provided to the provided target study.
     * The method also takes care to enrich study and series attributes.
     * Throws an EJBException if the patient is different for any of the
     * source studies or the target study. Throws an EJB Exception if any
     * of the studies are not found. Utilizes the move method for performing
     * the suggested sub-operations.
     * 
     * @param sourceStudyUIDs
     * @param targetStudyUID
     * @param targetStudyAttributes
     * @param targetSeriesAttributes
     * @param qcRejectionCode
     * @return Returns the context of the operation performed
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext mergeStudies(String[] sourceStudyUIDs, String targetStudyUID,
            Attributes targetStudyAttributes, Attributes targetSeriesAttributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException;

    /**
     * Merge.
     * Used to perform the merge operation per study. The method is used by the 
     * @link {@link #mergeStudies(String[], String, Attributes, Attributes, Code)}
     * to perform the suggested operation per source study.
     *  
     * @param sourceStudyUID
     * @param targetStudyUID
     * @param targetStudyAttributes
     * @param targetSeriesAttributes
     * @param qcRejectionCode
     * @return Returns the context of the operation performed
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext merge(String sourceStudyUID, String targetStudyUID,
            Attributes targetStudyAttributes, Attributes targetSeriesAttributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException ;

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
     * @param pid
     * @param targetStudyUID
     * @param createdStudyattributes
     * @param targetSeriesAttributes
     * @param qcRejectionCode
     * @return Returns the context of the operation performed
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext split(Collection<String> toMove, IDWithIssuer pid,
            String targetStudyUID, Attributes createdStudyAttributes,
            Attributes targetSeriesAttributes, Code qcRejectionCode) throws QCOperationNotPermittedException;

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
     * @param toClone
     * @param pid
     * @param targetStudyUID
     * @param targetStudyAttributes
     * @param targetSeriesAttributes
     * @param qcRejectionCode
     * @return Returns the context of the operation performed
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext segment(Collection<String> toMove, Collection<String> toClone,
            IDWithIssuer pid, String targetStudyUID,
            Attributes targetStudyAttributes, Attributes targetSeriesAttributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException;

    /**
     * Update dicom object.
     * Used to update Patient/Study/Series/Instance with the provided attributes.
     * The method will create any entities required aand associate them with the
     * object being updated, the operation is reversible.
     * @param arcDevExt
     * @param scope
     * @param attributes
     * @return Returns the context of the operation performed
     * @throws EntityNotFoundException
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext updateDicomObject(ArchiveDeviceExtension arcDevExt, QCUpdateScope scope,
            Attributes attributes) throws QCOperationNotPermittedException, EntityNotFoundException;

    /**
     * 
     * @param pid PID of the patient to be deleted
     * @return Returns the context of the operation performed
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext deletePatient(IDWithIssuer pid, Code qcRejectionCode) throws QCOperationNotPermittedException;

    /**
     * Delete study.
     * Deletes a study permanently
     * The method will use the archives delete queue to perform asynchronously
     * 
     * @param studyInstanceUID instance UID of study to be deleted
     *    
     * @return Returns the context of the operation performed
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext deleteStudy(String studyInstanceUID, Code qcRejectionCode) throws QCOperationNotPermittedException;

    /**
     * Delete series.
     * Deletes a series permanently
     * The method will use the archives delete queue to perform asynchronously
     * 
     * @param seriesInstanceUID the instance UID of the series to be delted
     * @return Returns the context of the operation performed
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext deleteSeries(String seriesInstanceUID, Code qcRejectionCode) throws QCOperationNotPermittedException;

    /**
     * Delete instance.
     * Deletes an instance permanently
     * The method will use the archives delete queue to perform asynchronously
     * 
     * @param sopInstanceUID SOP instance UID of instance to be deleted
     * @param qcRejectionCode
     * @return Returns the context of the operation performed
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext deleteInstance(String sopInstanceUID, Code qcRejectionCode) throws QCOperationNotPermittedException;

    /**
     * Restore.
     * Used to remove the previously applied rejection code on an instance.
     * 
     * @param sopInstanceUIDs
     * @return Returns the context of the operation performed
     * @throws QCOperationNotPermittedException
     */
    QCOperationContext restore(String[] sopInstanceUIDs) throws QCOperationNotPermittedException;
    
}
