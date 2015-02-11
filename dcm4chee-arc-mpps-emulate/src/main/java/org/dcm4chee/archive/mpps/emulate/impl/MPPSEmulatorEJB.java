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

package org.dcm4chee.archive.mpps.emulate.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.faces.component.UIGraphic;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.DatePrecision;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveConfigurationException;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.MPPSEmulationRule;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.MPPSEmulate;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.mpps.MPPSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */

@Stateless
public class MPPSEmulatorEJB {

    private static final Logger LOG = LoggerFactory
            .getLogger(MPPSEmulatorEJB.class);

    private static final int[] PATIENT_Selection = { Tag.SpecificCharacterSet,
            Tag.PatientName, Tag.PatientID, Tag.IssuerOfPatientID,
            Tag.PatientBirthDate, Tag.PatientSex };

    private static final int[] SERIES_Selection = { Tag.SeriesDescription,
            Tag.PerformingPhysicianName, Tag.ProtocolName,
            Tag.SeriesInstanceUID };

    private static final int[] STUDY_Selection = { Tag.ProcedureCodeSequence,
            Tag.StudyID };

    private static final int[] SERIES_PPS_Selection = {
            Tag.PerformedProcedureStepStartDate,
            Tag.PerformedProcedureStepStartTime, Tag.PerformedProcedureStepID };

    public static final class EmulationResult {
        public final ApplicationEntity ae;
        public final MPPS mpps;

        public EmulationResult(ApplicationEntity ae, MPPS mpps) {
            this.ae = ae;
            this.mpps = mpps;
        }
    }

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    public void scheduleMPPSEmulation(String sourceAET,
            String studyInstanceUID, MPPSEmulationRule mppsEmulationRule) {
        Date emulationTime = new Date(System.currentTimeMillis()
                + mppsEmulationRule.getEmulationDelay() * 1000L);
        MPPSEmulate entity;
        try {
            entity = em
                    .createNamedQuery(
                            MPPSEmulate.FIND_BY_STUDY_INSTANCE_UID_AND_SOURCE_AET,
                            MPPSEmulate.class)
                    .setParameter(1, studyInstanceUID)
                    .setParameter(2, sourceAET).getSingleResult();
            entity.setEmulationTime(emulationTime);
            LOG.debug(
                    "Update scheduled MPPS Emulation for Study[iuid={}] received from {}",
                    studyInstanceUID, sourceAET);
        } catch (NoResultException nre) {
            entity = new MPPSEmulate();
            entity.setEmulatorAET(mppsEmulationRule.getEmulatorAET());
            entity.setSourceAET(sourceAET);
            entity.setStudyInstanceUID(studyInstanceUID);
            entity.setEmulationTime(emulationTime);
            em.persist(entity);
            LOG.info(
                    "Schedule MPPS Emulation for Study[iuid={}] received from {}",
                    studyInstanceUID, sourceAET);
        }
    }

    public EmulationResult emulateNextPerformedProcedureStep(Device device,
            MPPSService mppsService) throws DicomServiceException {
        List<MPPSEmulate> resultList = em
                .createNamedQuery(MPPSEmulate.FIND_READY_TO_EMULATE,
                        MPPSEmulate.class).setMaxResults(1).getResultList();
        if (resultList.isEmpty())
            return null;

        MPPSEmulate emulate = resultList.get(0);
        ApplicationEntity ae = device.getApplicationEntity(emulate
                .getEmulatorAET());
        if (ae == null) {
            throw new ArchiveConfigurationException(
                    "Unknown MPPS Emulator AET: " + emulate.getEmulatorAET());
        }
        MPPS mpps = emulatePerformedProcedureStep(ae, emulate.getSourceAET(),
                emulate.getStudyInstanceUID(), mppsService);
        em.remove(emulate);
        return new EmulationResult(ae, mpps);
    }

    public MPPS emulatePerformedProcedureStep(ApplicationEntity ae,
            String sourceAET, String studyInstanceUID, MPPSService mppsService)
            throws DicomServiceException {
        List<Series> series = em
                .createNamedQuery(
                        Series.FIND_BY_STUDY_INSTANCE_UID_AND_SOURCE_AET,
                        Series.class).setParameter(1, studyInstanceUID)
                .setParameter(2, sourceAET).getResultList();
        if (series.isEmpty())
            return null;

        LOG.info("Emulate MPPS for Study[iuid={}] received from {}",
                studyInstanceUID, sourceAET);
        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        String mppsIUID = UIDUtils.createUID();
        MPPS mpps = mppsService.createPerformedProcedureStep(arcAE, mppsIUID,
                createMPPS(series), series.get(0).getStudy().getPatient(),
                mppsService);
        updateMPPSReferences(mppsIUID, series, arcAE.getStoreParam());
        return mpps;
    }

    private void updateMPPSReferences(String mppsIUID, List<Series> series,
            StoreParam storeParam) {
        for (Series ser : series) {
            Attributes serAttrs = ser.getAttributes();
            Attributes mppsRef = new Attributes(2);
            mppsRef.setString(Tag.ReferencedSOPClassUID, VR.UI,
                    UID.ModalityPerformedProcedureStepSOPClass);
            mppsRef.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mppsIUID);
            serAttrs.newSequence(Tag.ReferencedPerformedProcedureStepSequence,
                    1).add(mppsRef);
            ser.setAttributes(serAttrs,
                    storeParam.getAttributeFilter(Entity.Series),
                    storeParam.getFuzzyStr());
        }
    }

    private Attributes createMPPS(List<Series> seriesList) {
        Attributes mppsAttrs = new Attributes();

        Series firstSeries = seriesList.get(0);
        Study study = firstSeries.getStudy();
        Patient patient = study.getPatient();
        String modality = firstSeries.getModality() == null ? "OT"
                : firstSeries.getModality();

        mppsAttrs.addSelected(patient.getAttributes(), PATIENT_Selection);
        mppsAttrs.addSelected(study.getAttributes(), STUDY_Selection);
        mppsAttrs.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        mppsAttrs.setString(Tag.SOPClassUID, VR.UI,
                UID.ModalityPerformedProcedureStepSOPClass);
        mppsAttrs.setString(Tag.PerformedStationAETitle, VR.AE,
                firstSeries.getSourceAET());
        mppsAttrs.setString(Tag.PerformedStationName, VR.SH,
                firstSeries.getStationName());
        mppsAttrs.setString(Tag.Modality, VR.CS, modality);
        mppsAttrs.setString(Tag.PerformedProcedureStepID, VR.SH,
                makePPSID(modality, study.getStudyInstanceUID()));
        mppsAttrs.setString(Tag.PerformedProcedureStepDescription, VR.LO,
                study.getStudyDescription());

        Sequence ssa = mppsAttrs.newSequence(
                Tag.ScheduledStepAttributesSequence, seriesList.size());

        Date date = null;

        for (Series series : seriesList) {

            Date ppsDate = DateUtils.parseDT(
                    null,
                    series.getPerformedProcedureStepStartDate()
                            + series.getPerformedProcedureStepStartTime(),
                    new DatePrecision());
            if (date == null
                    || (ppsDate != null && ppsDate.compareTo(date) > 0))
                date = ppsDate;
        }

        if (date != null)
            mppsAttrs.setDate(Tag.PerformedProcedureStepStartDateAndTime, date);

        return mppsAttrs;
    }

    private String makePPSID(String modality, String studyInstanceUID) {
        return modality.substring(0, 2)
                + studyInstanceUID.substring(Math.max(0,
                        studyInstanceUID.length() - 14));
    }

}
