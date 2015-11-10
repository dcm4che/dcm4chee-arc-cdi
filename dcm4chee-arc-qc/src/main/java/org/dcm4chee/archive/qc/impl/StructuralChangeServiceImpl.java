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

package org.dcm4chee.archive.qc.impl;

import java.util.Collection;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope;
import org.dcm4chee.archive.qc.PatientCommands;
import org.dcm4chee.archive.qc.QCBean;
import org.dcm4chee.archive.qc.QCEvent;
import org.dcm4chee.archive.qc.QCOperationContext;
import org.dcm4chee.archive.qc.QCOperationNotPermittedException;
import org.dcm4chee.archive.qc.StructuralChangeService;
import org.dcm4chee.archive.sc.impl.StructuralChangeTransactionAggregator;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
@Stateless
public class StructuralChangeServiceImpl implements StructuralChangeService {
    @Inject
    private QCBean qcBean;
    
    @Inject
    private StructuralChangeTransactionAggregator structuralChangeAggregator;
   
    @Override
    public QCOperationContext mergeStudies(Enum<?> structuralChangeType, String[] sourceStudyUids, String targetStudyUID,
            Attributes targetStudyattributes, Attributes targetSeriesattributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.mergeStudies(sourceStudyUids, targetStudyUID, targetStudyattributes, targetSeriesattributes, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public QCOperationContext merge(Enum<?> structuralChangeType, String sourceStudyUid, String targetStudyUid,
            Attributes targetStudyattributes, Attributes targetSeriesattributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.merge(sourceStudyUid, targetStudyUid, targetStudyattributes, targetSeriesattributes, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public QCOperationContext split(Enum<?> structuralChangeType, Collection<String> toMove, IDWithIssuer pid, String targetStudyUID,
            Attributes createdStudyattributes, Attributes targetSeriesattributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.split(toMove, pid, targetStudyUID, createdStudyattributes, targetSeriesattributes, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public QCOperationContext segment(Enum<?> structuralChangeType, Collection<String> toMove, Collection<String> toClone, IDWithIssuer pid,
            String targetStudyUID, Attributes targetStudyattributes,
            Attributes targetSeriesattributes, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.segment(toMove, toClone, pid, targetStudyUID, targetStudyattributes, targetSeriesattributes, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }
    
    @Override
    public boolean canApplyQC(Instance sopInstanceUID) {
        return qcBean.canApplyQC(sopInstanceUID);
    }

    @Override
    public QCOperationContext updateDicomObject(Enum<?> structuralChangeType, ArchiveDeviceExtension arcDevExt, QCUpdateScope scope,
            Attributes attributes) throws QCOperationNotPermittedException, EntityNotFoundException {
        QCEvent qcEvent = qcBean.updateDicomObject(arcDevExt, scope, attributes);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public boolean patientOperation(Attributes sourcePatientAttributes,
            Attributes targetPatientAttributes, ArchiveAEExtension arcAEExt, PatientCommands command)
            throws QCOperationNotPermittedException {
        return qcBean.patientOperation(sourcePatientAttributes, targetPatientAttributes, arcAEExt, command);
    }

    @Override
    public Collection<Instance> locateInstances(String... sopInstanceUIDs) {
        return qcBean.locateInstances(sopInstanceUIDs);
    }

    @Override
    public QCOperationContext deletePatient(Enum<?> structuralChangeType, IDWithIssuer pid, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.deletePatient(pid, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public QCOperationContext deleteStudy(Enum<?> structuralChangeType, String studyInstanceUID, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.deleteStudy(studyInstanceUID, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public QCOperationContext deleteSeries(Enum<?> structuralChangeType, String seriesInstanceUID, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.deleteSeries(seriesInstanceUID, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public QCOperationContext deleteInstance(Enum<?> structuralChangeType, String sopInstanceUID, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.deleteInstance(sopInstanceUID, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public boolean deletePatientIfEmpty(IDWithIssuer pid) {
        return qcBean.deletePatientIfEmpty(pid);
    }
    
    @Override
    public boolean deleteStudyIfEmpty(String studyInstanceUID) {
        return qcBean.deleteStudyIfEmpty(studyInstanceUID);
    }

    @Override
    public boolean deleteSeriesIfEmpty(String seriesInstanceUID, String studyInstanceUID) {
        return qcBean.deleteSeriesIfEmpty(seriesInstanceUID, studyInstanceUID);
    }

    @Override
    public QCOperationContext reject(Enum<?> structuralChangeType, String[] sopInstanceUIDs, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.reject(sopInstanceUIDs, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public QCOperationContext restore(Enum<?> structuralChangeType, String[] sopInstanceUIDs) throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.restore(sopInstanceUIDs);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }

    @Override
    public Patient findPatient(Attributes attrs) {
        return qcBean.findPatient(attrs);
    }

    @Override
    public QCOperationContext replaced(Enum<?> structuralChangeType, Map<String, String> oldToNewIUIDs, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCEvent qcEvent = qcBean.replaced(oldToNewIUIDs, qcRejectionCode);
        return aggregateStructuralChange(structuralChangeType, qcEvent);
    }
    
    private QCContextImpl aggregateStructuralChange(Enum<?> structuralChangeType, QCEvent qcEvent) {
        QCContextImpl changeContext = QCContextImpl.createInstance(structuralChangeType, qcEvent);
        structuralChangeAggregator.aggregate(changeContext);
        return changeContext;
    }

}
