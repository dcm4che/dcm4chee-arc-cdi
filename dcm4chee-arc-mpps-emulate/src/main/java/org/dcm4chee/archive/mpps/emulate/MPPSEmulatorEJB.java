/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4chee.archive.mpps.emulate;

import org.dcm4che3.data.*;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.MPPSCreationRule;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.mpps.MPPSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Umberto Cappellini
 */
@Stateless
public class MPPSEmulatorEJB {

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

    private static final Logger LOG = LoggerFactory.getLogger(MPPSEmulatorEJB.class);
    private static final int[] PATIENT_Selection = { Tag.SpecificCharacterSet,
            Tag.PatientName, Tag.PatientID, Tag.IssuerOfPatientID,
            Tag.PatientBirthDate, Tag.PatientSex };
    private static final int[] SERIES_Selection = { Tag.SeriesDescription,
            Tag.PerformingPhysicianName, Tag.ProtocolName,
            Tag.SeriesInstanceUID };
    private static final int[] STUDY_Selection = { Tag.ProcedureCodeSequence,
            Tag.StudyID };



    public MPPS emulatePerformedProcedureStep(ApplicationEntity ae,
            String sourceAET, String studyInstanceUID, MPPSService mppsService)
            throws DicomServiceException {

        List<Series> seriesList = em
                .createNamedQuery(
                        Series.FIND_BY_STUDY_INSTANCE_UID_AND_SOURCE_AET,
                        Series.class).setParameter(1, studyInstanceUID)
                .setParameter(2, sourceAET).getResultList();
        if (seriesList.isEmpty())
            return null;

        ArchiveAEExtension arcAE = ae.getAEExtension(ArchiveAEExtension.class);
        MPPSCreationRule creationRule = arcAE.getMppsEmulationRule(sourceAET)
                .getCreationRule();

        // checks if emulated MPPS should be created, according to configured
        // rule
        if (!checkCreationRule(creationRule, seriesList))
            return null;

        LOG.info("Emulate MPPS for Study[iuid={}] received from {}",
                studyInstanceUID, sourceAET);
        String mppsIUID = UIDUtils.createUID();
        MPPS mpps = mppsService.createPerformedProcedureStep(arcAE, mppsIUID,
                createMPPS(seriesList), seriesList.get(0).getStudy()
                        .getPatient(), mppsService);
        updateMPPSReferences(mppsIUID, seriesList, arcAE.getStoreParam());
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
                    storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
        }
    }

    private Attributes createMPPS(List<Series> seriesList) {
        Attributes mppsAttrs = new Attributes();

        Series firstSeries = seriesList.get(0);
        Study study = firstSeries.getStudy();
        Patient patient = study.getPatient();
        String modality = firstSeries.getModality() == null ? "OT"
                : firstSeries.getModality();

        // pps information
        mppsAttrs.setString(Tag.PerformedProcedureStepStatus, VR.CS,
                MPPS.COMPLETED);
        mppsAttrs.addSelected(patient.getAttributes(), PATIENT_Selection);
        mppsAttrs.addSelected(study.getAttributes(), STUDY_Selection);
        mppsAttrs.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        mppsAttrs.setString(Tag.SOPClassUID, VR.UI,
                UID.ModalityPerformedProcedureStepSOPClass);
        mppsAttrs.setString(Tag.PerformedStationAETitle, VR.AE,
                firstSeries.getSourceAET());
        mppsAttrs.setString(Tag.PerformedStationName, VR.SH,
                firstSeries.getStationName());
        mppsAttrs.setNull(Tag.PerformedLocation, VR.SH);
        mppsAttrs.setString(Tag.Modality, VR.CS, modality);
        mppsAttrs.setString(Tag.PerformedProcedureStepID, VR.SH,
                makePPSID(modality, study.getStudyInstanceUID()));
        mppsAttrs.setString(Tag.PerformedProcedureStepDescription, VR.LO,
                study.getStudyDescription());

        // scheduled attribute sequence
        // TODO scheduled/unscheduled
        Sequence SchedStepAttSq = mppsAttrs.newSequence(
                Tag.ScheduledStepAttributesSequence, 1);
        Attributes ssasItem = new Attributes();
        ssasItem.setString(Tag.StudyInstanceUID, VR.UI,
                study.getStudyInstanceUID());
        ssasItem.setString(Tag.AccessionNumber, VR.SH,
                study.getAccessionNumber());
        ssasItem.setNull(Tag.RequestedProcedureID, VR.SH);
        ssasItem.setNull(Tag.RequestedProcedureDescription, VR.LO);
        ssasItem.setNull(Tag.ScheduledProcedureStepID, VR.SH);
        ssasItem.setNull(Tag.ScheduledProcedureStepDescription, VR.LO);
        ssasItem.newSequence(Tag.ScheduledProtocolCodeSequence, 0);
        ssasItem.newSequence(Tag.ReferencedStudySequence, 0);
        SchedStepAttSq.add(ssasItem);

        // performed series sequence
        Sequence perfSeriesSq = mppsAttrs.newSequence(
                Tag.PerformedSeriesSequence, seriesList.size());

        Date start_date = null, end_date = null;
        for (Series series : seriesList) {
            Attributes pssqItem = new Attributes();
            pssqItem.addSelected(series.getAttributes(), SERIES_Selection);
            Sequence refImgSq = pssqItem.newSequence(
                    Tag.ReferencedImageSequence, series.getInstances().size());
            for (Instance inst : series.getInstances()) {
                start_date = choseDate(start_date, inst.getCreatedTime(), false);
                end_date = choseDate(end_date, inst.getCreatedTime(), true);
                Attributes refImg = new Attributes();
                refImg.setString(Tag.SOPClassUID, VR.UI, inst.getSopClassUID());
                refImg.setString(Tag.SOPInstanceUID, VR.UI,
                        inst.getSopInstanceUID());
                refImgSq.add(refImg);
            }
            perfSeriesSq.add(pssqItem);
        }

        mppsAttrs.setString(Tag.PerformedProcedureStepStartDate, VR.DA,
                DateUtils.formatDA(null, start_date));
        mppsAttrs.setString(Tag.PerformedProcedureStepStartTime, VR.TM,
                DateUtils.formatTM(null, start_date));
        mppsAttrs.setString(Tag.PerformedProcedureStepEndDate, VR.DA,
                DateUtils.formatDA(null, end_date));
        mppsAttrs.setString(Tag.PerformedProcedureStepEndTime, VR.TM,
                DateUtils.formatTM(null, end_date));

        return mppsAttrs;
    }

    private boolean checkCreationRule(MPPSCreationRule rule,
            List<Series> seriesList) {

        // check if referenced mpps exists
        List<String> refMppsList = new ArrayList<String>();
        for (Series series : seriesList) {
            Sequence refPpsSeq = series.getAttributes().getSequence(
                    Tag.ReferencedPerformedProcedureStepSequence);
            if (refPpsSeq != null
                    && refPpsSeq.size() > 0
                    && refPpsSeq.get(0).getString(Tag.ReferencedSOPInstanceUID) != null)
                refMppsList.add(refPpsSeq.get(0).getString(
                        Tag.ReferencedSOPInstanceUID));
        }

        if (refMppsList.size() == 0) {
            LOG.debug("Emulate MPPS (No Reference found always emulate)");
            return true;
        }

        // cases when there is an existing reference (refMppsList.size()>0)
        List<MPPS> mppsList = null;
        switch (rule) {
        case ALWAYS:
            LOG.debug("Emulate MPPS (reference found and rule ALWAYS)");
            deleteMPPS(refMppsList);
            return true;
        case NEVER:
            LOG.debug("NO MPPS Emulation (reference found and rule NEVER)");
            return false;
        case NO_MPPS_CREATE:
            mppsList = fetchMPPS(refMppsList);
            if (mppsList == null || mppsList.size() == 0) {
                LOG.debug("Emulate MPPS (reference found, no mpps stored and rule NO_MPPS_CREATE)");
                return true;
            } else {
                LOG.debug("NO MPPS Emulation (reference found, mpps stored and rule NO_MPPS_CREATE)");
                return false;
            }
        case NO_MPPS_FINAL:
            mppsList = fetchMPPS(refMppsList);
            for (MPPS mpps : mppsList) {
                if (mpps.getStatus() == MPPS.Status.IN_PROGRESS) {
                    LOG.debug("Emulate MPPS (reference found, mpps stored, at least one MPPS not finalized and rule NO_MPPS_FINAL)");
                    deleteMPPS(refMppsList);
                    return true;
                }
            }
            LOG.debug("NO MPPS Emulation (reference found, mpps stored, all MPPS finalized and rule NO_MPPS_FINAL)");
            return false;
        default:
            return true; // default = emulate MPPS
        }
    }

    private List<MPPS> fetchMPPS(List<String> refMppsList) {
        return (refMppsList.size() > 0) ? em
                .createNamedQuery(MPPS.FIND_BY_SOP_INSTANCE_UIDs, MPPS.class)
                .setParameter("idList", refMppsList).getResultList() : null;
    }

    private void deleteMPPS(List<String> refMppsList) {
        if (refMppsList.size() > 0)
            em.createNamedQuery(MPPS.DELETE_BY_SOP_INSTANCE_UIDs)
                    .setParameter("idList", refMppsList).executeUpdate();
    }

    private String makePPSID(String modality, String studyInstanceUID) {
        return modality.substring(0, 2)
                + studyInstanceUID.substring(Math.max(0,
                        studyInstanceUID.length() - 14));
    }

    private Date choseDate(Date date1, Date date2, boolean returnMostRecent) {
        if (date1 == null)
            return date2;
        if (date2 == null)
            return date1;
        if (date1.compareTo(date2) > 0)
            return returnMostRecent ? date1 : date2;
        else
            return returnMostRecent ? date2 : date1;
    }
}
