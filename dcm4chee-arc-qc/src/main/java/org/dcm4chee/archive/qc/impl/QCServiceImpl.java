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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope;
import org.dcm4chee.archive.qc.QCOperationContext;
import org.dcm4chee.archive.qc.QCOperationNotPermittedException;
import org.dcm4chee.archive.qc.QCService;
import org.dcm4chee.archive.qc.StructuralChangeService;
import org.dcm4chee.archive.sc.STRUCTURAL_CHANGE;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
@Stateless
public class QCServiceImpl implements QCService {
    @Inject
    private StructuralChangeService scService;
  
    @Override
    public QCOperationContext mergeStudies(String[] sourceStudyUIDs, String targetStudyUID,
            Attributes targetStudyAttributes, Attributes targetSeriesAttributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException {
        return scService.mergeStudies(STRUCTURAL_CHANGE.QC, sourceStudyUIDs, targetStudyUID, targetStudyAttributes, targetSeriesAttributes, qcRejectionCode);
    }

    @Override
    public QCOperationContext merge(String sourceStudyUID, String targetStudyUID,
            Attributes targetStudyAttributes, Attributes targetSeriesAttributes,
            Code qcRejectionCode) throws QCOperationNotPermittedException {
        return scService.merge(STRUCTURAL_CHANGE.QC, sourceStudyUID, targetStudyUID, targetStudyAttributes, targetSeriesAttributes, qcRejectionCode);
    }

    @Override
    public QCOperationContext split(Collection<String> toMove, IDWithIssuer pid,
            String targetStudyUID, Attributes createdStudyAttributes,
            Attributes targetSeriesAttributes, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        return scService.split(STRUCTURAL_CHANGE.QC, toMove, pid, targetStudyUID, createdStudyAttributes, targetSeriesAttributes, qcRejectionCode);
    }

    @Override
    public QCOperationContext segment(Collection<String> toMove, Collection<String> toClone,
            IDWithIssuer pid, String targetStudyUID, Attributes targetStudyAttributes,
            Attributes targetSeriesAttributes, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        return scService.segment(STRUCTURAL_CHANGE.QC, toMove, toClone, pid, targetStudyUID, targetStudyAttributes, targetSeriesAttributes, qcRejectionCode);
    }

    @Override
    public QCOperationContext updateDicomObject(ArchiveDeviceExtension arcDevExt,
            QCUpdateScope scope, Attributes attributes) throws QCOperationNotPermittedException,
            EntityNotFoundException {
        return scService.updateDicomObject(STRUCTURAL_CHANGE.QC, arcDevExt, scope, attributes);
    }

    @Override
    public QCOperationContext deletePatient(IDWithIssuer pid, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        return scService.deletePatient(STRUCTURAL_CHANGE.QC, pid, qcRejectionCode);
    }

    @Override
    public QCOperationContext deleteStudy(String studyInstanceUID, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        return scService.deleteStudy(STRUCTURAL_CHANGE.QC, studyInstanceUID, qcRejectionCode);
    }

    @Override
    public QCOperationContext deleteSeries(String seriesInstanceUID, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        return scService.deleteSeries(STRUCTURAL_CHANGE.QC, seriesInstanceUID, qcRejectionCode);
    }

    @Override
    public QCOperationContext deleteInstance(String sopInstanceUID, Code qcRejectionCode)
            throws QCOperationNotPermittedException {
        return scService.deleteInstance(STRUCTURAL_CHANGE.QC, sopInstanceUID, qcRejectionCode);
    }

    @Override
    public QCOperationContext restore(String[] sopInstanceUIDs)
            throws QCOperationNotPermittedException {
        return scService.restore(STRUCTURAL_CHANGE.QC, sopInstanceUIDs);
    }

}
