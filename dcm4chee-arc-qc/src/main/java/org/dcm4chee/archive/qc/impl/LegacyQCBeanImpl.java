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
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.dto.ServiceQualifier;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope;
import org.dcm4chee.archive.qc.PatientCommands;
import org.dcm4chee.archive.qc.QCBean;
import org.dcm4chee.archive.qc.QCEvent;
import org.dcm4chee.archive.qc.QCOperationContext;
import org.dcm4chee.archive.qc.QCOperationNotPermittedException;
import org.dcm4chee.archive.qc.StructuralChangeService;
import org.dcm4chee.archive.sc.STRUCTURAL_CHANGE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Legacy interface for QC.
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 */
@Stateless
@Deprecated
public class LegacyQCBeanImpl implements QCBean {
    private static final Logger LOG = LoggerFactory.getLogger(LegacyQCBeanImpl.class);
    
    @Inject
    private StructuralChangeService structuralChangeService;
    
    @Inject
    @Any
    private Event<QCEvent> internalNotification;
    
    @Override
    public QCEvent mergeStudies(String[] sourceStudyUids, String targetStudyUID,
            Attributes targetStudyattributes, Attributes targetSeriesattributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.mergeStudies(STRUCTURAL_CHANGE.QC, sourceStudyUids, targetStudyUID, targetStudyattributes, targetSeriesattributes, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

   
    @Override
    public QCEvent merge(String sourceStudyUID, String targetStudyUID,
            Attributes targetStudyAttributes, Attributes targetSeriesAttributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.merge(STRUCTURAL_CHANGE.QC, sourceStudyUID, targetStudyUID, targetStudyAttributes, targetSeriesAttributes, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public QCEvent split(Collection<String> toMove, IDWithIssuer pid, String targetStudyUID,
            Attributes createdStudyAttributes, Attributes targetSeriesAttributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.split(STRUCTURAL_CHANGE.QC, toMove, pid, targetStudyUID, createdStudyAttributes, targetSeriesAttributes, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public QCEvent segment(Collection<String> toMove, Collection<String> toClone, IDWithIssuer pid,
            String targetStudyUID, Attributes targetStudyAttributes,
            Attributes targetSeriesAttributes, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.segment(STRUCTURAL_CHANGE.QC, toMove, toClone, pid, targetStudyUID, targetStudyAttributes, targetSeriesAttributes, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public void segmentFrame(Instance toMove, Instance toClone, int frame, PatientID pid,
            String targetStudyUID, Attributes targetStudyattributes,
            Attributes targetSeriesattributes) throws QCOperationNotPermittedException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public boolean canApplyQC(Instance sopInstanceUID) {
        return structuralChangeService.canApplyQC(sopInstanceUID);
    }

    @Override
    public void notify(QCEvent event) {
        LOG.debug("QC info[Notify] - Operation successfull,"
                + " notification triggered with event {}",event.toString());
        internalNotification.select(new ServiceQualifier(ServiceType.QCPOSTPROCESSING)).fire(event);
    }

    @Override
    public QCEvent updateDicomObject(ArchiveDeviceExtension arcDevExt, QCUpdateScope scope,
            Attributes attributes) throws QCOperationNotPermittedException, EntityNotFoundException {
        QCOperationContext qcContext = structuralChangeService.updateDicomObject(STRUCTURAL_CHANGE.QC, arcDevExt, scope, attributes);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public boolean patientOperation(Attributes sourcePatientAttributes,
            Attributes targetPatientAttributes, ArchiveAEExtension arcAEExt, PatientCommands command)
            throws QCOperationNotPermittedException {
        return structuralChangeService.patientOperation(sourcePatientAttributes, targetPatientAttributes, arcAEExt, command);
    }

    @Override
    public Collection<Instance> locateInstances(String... strings) {
        return structuralChangeService.locateInstances(strings);
    }

    @Override
    public QCEvent deletePatient(IDWithIssuer pid, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.deletePatient(STRUCTURAL_CHANGE.QC, pid, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public QCEvent deleteStudy(String studyInstanceUID, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.deleteStudy(STRUCTURAL_CHANGE.QC, studyInstanceUID, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public QCEvent deleteSeries(String seriesInstanceUID, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.deleteSeries(STRUCTURAL_CHANGE.QC, seriesInstanceUID, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public QCEvent deleteInstance(String sopInstanceUID, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.deleteInstance(STRUCTURAL_CHANGE.QC, sopInstanceUID, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public boolean deletePatientIfEmpty(IDWithIssuer pid) {
        return structuralChangeService.deletePatientIfEmpty(pid);
    }

    @Override
    public boolean deleteStudyIfEmpty(String studyInstanceUID) {
        return deleteStudyIfEmpty(studyInstanceUID);
    }

    @Override
    public boolean deleteSeriesIfEmpty(String seriesInstanceUID, String studyInstanceUID) {
        return deleteSeriesIfEmpty(seriesInstanceUID, studyInstanceUID);
    }

    @Override
    public QCEvent reject(String[] sopInstanceUIDs, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.reject(STRUCTURAL_CHANGE.QC, sopInstanceUIDs, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public QCEvent restore(String[] sopInstanceUIDs) throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.restore(ServiceType.QCDURINGTRANSACTION, sopInstanceUIDs);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

    @Override
    public Patient findPatient(Attributes attrs) {
        return structuralChangeService.findPatient(attrs);
    }

    @Override
    public QCEvent replaced(Map<String, String> oldToNewIUIDs, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        QCOperationContext qcContext = structuralChangeService.replaced(STRUCTURAL_CHANGE.QC, oldToNewIUIDs, qcRejectionCode);
        QCEvent qcEvent = QCContextImpl.toQCEvent(qcContext);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(qcEvent);
        return qcEvent;
    }

}
