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
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.entity.QCActionHistory;
import org.dcm4chee.archive.entity.Series;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

public interface QCBean {

    public QCEvent mergeStudies(String[] sourceStudyUids, String targetStudyUID, Attributes targetStudyAttrs, Attributes targetSeriesAttrs, Code qcRejectionCode);

    public QCEvent merge(String sourceStudyUid, String targetStudyUid,Attributes targetStudyAttrs, Attributes targetSeriesAttrs, boolean samePatient, Code qcRejectionCode);

    public QCEvent split(Collection<Instance> toMove, IDWithIssuer pid, String targetStudyUID, Attributes createdStudyAttrs, Attributes targetSeriesAttrs, Code qcRejectionCode);

    public QCEvent segment(Collection<Instance> toMove, Collection<Instance> toClone, IDWithIssuer pid, String targetStudyUID, Attributes targetStudyAttrs, Attributes targetSeriesAttrs, Code qcRejectionCode);

    public void segmentFrame(Instance toMove, Instance toClone, int frame,
            PatientID pid, String targetStudyUID, Attributes targetStudyAttrs, Attributes targetSeriesAttrs);

    public Instance move(Instance source, Series target, Code qcRejectionCode);

    public Instance clone(Instance source, Series target);


//    public QCInstanceHistory findUIDChangesFromHistory(Instance instance);

//    public void removeHistoryEntry(QCActionHistory action);

    public boolean canApplyQC(Instance sopInstanceUID);

    public void notify(QCEvent event);

    QCEvent updateDicomObject(ArchiveDeviceExtension arcDevExt, String scope, Attributes attrs) throws EntityNotFoundException;
    
    boolean patientOperation(Attributes sourcePatientAttributes, Attributes targetPatientAttributes, ArchiveAEExtension arcAEExt, PatientCommands command);
    
//    public void undoLastAction(QCStudyHistory study);

    public Collection<Instance> locateInstances(String[] strings);

    public QCEvent deleteStudy(String studyInstanceUID) throws Exception;

    public QCEvent deleteSeries(String seriesInstanceUID) throws Exception;

    public QCEvent deleteInstance(String sopInstanceUID) throws Exception;

    boolean deleteStudyIfEmpty(String studyInstanceUID);

    boolean deleteSeriesIfEmpty(String seriesInstanceUID,
            String studyInstanceUID);

    QCEvent reject(String[] sopInstanceUIDs, Code qcRejectionCode);

    QCEvent reject(Collection<Instance> instances, Code qcRejectionCode);

    QCEvent restore(String[] sopInstanceUIDs);

    QCEvent restore(Collection<Instance> instances);
}
