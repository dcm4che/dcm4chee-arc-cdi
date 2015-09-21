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
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.conf.*;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.mpps.MPPSContext;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.store.session.StudyUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Umberto Cappellini
 * @author Roman K
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

    @Inject
    Device device;

    @Inject
    MPPSService mppsService;

    private static final Logger LOG = LoggerFactory.getLogger(MPPSEmulatorEJB.class);

    private static final int[] PATIENT_Selection = {
            Tag.SpecificCharacterSet,
            Tag.PatientName,
            Tag.PatientID,
            Tag.IssuerOfPatientID,
            Tag.PatientBirthDate,
            Tag.PatientSex };

    private static final int[] SERIES_Selection = {
            Tag.SeriesDescription,
            Tag.PerformingPhysicianName,
            Tag.ProtocolName,
            Tag.SeriesInstanceUID };

    private static final int[] STUDY_Selection = {
            Tag.ProcedureCodeSequence,
            Tag.StudyID };

    private static final int[] MPPS_SET_Selection = { Tag.SpecificCharacterSet,
            Tag.SOPInstanceUID, Tag.PerformedProcedureStepEndDate, Tag.PerformedProcedureStepEndTime,
            Tag.PerformedProcedureStepStatus, Tag.PerformedSeriesSequence };

    /**
     * Checks configured rule, finds the series, emulates MPPS
     * @param studyUpdatedEvent
     * @return
     * @throws DicomServiceException
     */
    public MPPS emulateMPPS(StudyUpdatedEvent studyUpdatedEvent) throws DicomServiceException {

        // find all series that are to be affected by this emulated MPPS
        List<Series> seriesList = findAffectedSeries(studyUpdatedEvent);
        if (seriesList == null) return null;

        // there could be multiple local AETs used - just get the first one for providing configuration for MPPS service
        String localAET = studyUpdatedEvent.getLocalAETs().iterator().next();

        // checks if emulated MPPS should be created, according to the configured rule
        MPPSCreationRule creationRule = device
                .getDeviceExtensionNotNull(ArchiveDeviceExtension.class)
                .getMppsEmulationRule(studyUpdatedEvent.getSourceAET())
                .getCreationRule();
        if (!checkCreationRule(creationRule, seriesList)) return null;

        LOG.info("Emulate MPPS for Study[iuid={}] received from {}", studyUpdatedEvent.getStudyInstanceUID(), studyUpdatedEvent.getSourceAET());
        String mppsIUID = UIDUtils.createUID();

        ApplicationEntity ae = device.getApplicationEntityNotNull(localAET);
        ArchiveAEExtension arcAE = ae.getAEExtensionNotNull(ArchiveAEExtension.class);

        updateMPPSReferences(mppsIUID, seriesList, arcAE.getStoreParam());

        // prepare attrs
        Attributes mppsCreateAttributes = makeMPPSCreateAttributes(seriesList, mppsIUID);
        Attributes completedAttributes = makeMPPSUpdateCompletedAttributes(mppsCreateAttributes);

        // create MPPS
        if (mppsCreateAttributes == null)
        	return null;
        MPPSContext mppsContext = new MPPSContext(studyUpdatedEvent.getSourceAET(), localAET, mppsIUID, Dimse.N_CREATE_RQ);
        mppsService.createPerformedProcedureStep(mppsCreateAttributes, mppsContext);

        // update MPPS with status COMPLETED
        mppsService.updatePerformedProcedureStep(completedAttributes,mppsContext);

        ArrayList<String> refMppsList = new ArrayList<>();
        refMppsList.add(mppsIUID);
        List<MPPS> mppsList = fetchMPPS(refMppsList);

        return mppsList.get(0);
    }

    public List<Series> findAffectedSeries(StudyUpdatedEvent studyUpdatedEvent) {
        List<Series> seriesList = em
                .createNamedQuery(
                        Series.FIND_BY_STUDY_INSTANCE_UID_AND_SOURCE_AET,
                        Series.class).setParameter(1, studyUpdatedEvent.getStudyInstanceUID())
                .setParameter(2, studyUpdatedEvent.getSourceAET()).getResultList();

        if (seriesList.isEmpty())
            return null;

        // filter series list - leave only ones affected by this StudyUpdateEvent
        Iterator<Series> seriesIterator = seriesList.iterator();
        while (seriesIterator.hasNext())
            if (!studyUpdatedEvent.getAffectedSeriesUIDs().contains(seriesIterator.next().getSeriesInstanceUID()))
                seriesIterator.remove();
        return seriesList;
    }

    public void updateMPPSReferences(String mppsIUID, List<Series> series, StoreParam storeParam) {
        for (Series ser : series) {
            Attributes serAttrs = ser.getAttributes();
            Attributes mppsRef = new Attributes(2);
            mppsRef.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.ModalityPerformedProcedureStepSOPClass);
            mppsRef.setString(Tag.ReferencedSOPInstanceUID, VR.UI, mppsIUID);
            serAttrs.newSequence(Tag.ReferencedPerformedProcedureStepSequence, 1).add(mppsRef);
            ser.setAttributes(serAttrs,
                    storeParam.getAttributeFilter(Entity.Series),
                    storeParam.getFuzzyStr(),
                    storeParam.getNullValueForQueryFields());
            em.merge(ser);
        }
    }

    private Attributes makeMPPSCreateAttributes(List<Series> seriesList, String mppsSOPInstanceUID) {
        Attributes mppsAttrs = new Attributes();

        Series firstSeries = seriesList.get(0);
        Study study = firstSeries.getStudy();
        Patient patient = study.getPatient();
        String modality = firstSeries.getModality() == null ? "OT" : firstSeries.getModality();

        // pps information
        mppsAttrs.setString(Tag.PerformedProcedureStepStatus, VR.CS, MPPS.IN_PROGRESS);
        mppsAttrs.addSelected(patient.getAttributes(), PATIENT_Selection);
        mppsAttrs.addSelected(study.getAttributes(), STUDY_Selection);
        mppsAttrs.setString(Tag.SOPInstanceUID, VR.UI, mppsSOPInstanceUID);
        mppsAttrs.setString(Tag.SOPClassUID, VR.UI, UID.ModalityPerformedProcedureStepSOPClass);
        mppsAttrs.setString(Tag.PerformedStationAETitle, VR.AE, firstSeries.getSourceAET());
        mppsAttrs.setString(Tag.PerformedStationName, VR.SH, firstSeries.getStationName());
        mppsAttrs.setNull(Tag.PerformedLocation, VR.SH);
        mppsAttrs.setString(Tag.Modality, VR.CS, modality);
        mppsAttrs.setString(Tag.PerformedProcedureStepID, VR.SH, makePPSID(modality, study.getStudyInstanceUID()));
        mppsAttrs.setString(Tag.PerformedProcedureStepDescription, VR.LO, study.getStudyDescription());

        // scheduled attribute sequence
        // TODO scheduled/unscheduled
        Sequence SchedStepAttSq = mppsAttrs.newSequence(Tag.ScheduledStepAttributesSequence, 1);
        Attributes ssasItem = new Attributes();
        ssasItem.setString(Tag.StudyInstanceUID, VR.UI, study.getStudyInstanceUID());
        ssasItem.setString(Tag.AccessionNumber, VR.SH, study.getAccessionNumber());
        ssasItem.setNull(Tag.RequestedProcedureID, VR.SH);
        ssasItem.setNull(Tag.RequestedProcedureDescription, VR.LO);
        ssasItem.setNull(Tag.ScheduledProcedureStepID, VR.SH);
        ssasItem.setNull(Tag.ScheduledProcedureStepDescription, VR.LO);
        ssasItem.newSequence(Tag.ScheduledProtocolCodeSequence, 0);
        ssasItem.newSequence(Tag.ReferencedStudySequence, 0);
        SchedStepAttSq.add(ssasItem);

        // performed series sequence
        Sequence perfSeriesSq = mppsAttrs.newSequence(Tag.PerformedSeriesSequence, seriesList.size());
        Date start_date = null, end_date = null;
        for (Series series : seriesList) {
        	if (series.getInstances().isEmpty())
        		continue;
            Attributes pssqItem = new Attributes();
            pssqItem.addSelected(series.getAttributes(), SERIES_Selection);
            Sequence refImgSq = pssqItem.newSequence(Tag.ReferencedImageSequence, series.getInstances().size());
            for (Instance inst : series.getInstances()) {
                start_date = choseDate(start_date, inst.getCreatedTime(), false);
                end_date = choseDate(end_date, inst.getCreatedTime(), true);
                Attributes refImg = new Attributes();
                refImg.setString(Tag.SOPClassUID, VR.UI, inst.getSopClassUID());
                refImg.setString(Tag.SOPInstanceUID, VR.UI, inst.getSopInstanceUID());
                refImgSq.add(refImg);
            }
            perfSeriesSq.add(pssqItem);
        }
        if (start_date == null) {
        	LOG.info("No instances available! Skip MPPS emulation.");
        	return null;
        }
        // pps datetime
        mppsAttrs.setString(Tag.PerformedProcedureStepStartDate, VR.DA, DateUtils.formatDA(null, start_date));
        mppsAttrs.setString(Tag.PerformedProcedureStepStartTime, VR.TM, DateUtils.formatTM(null, start_date));
        mppsAttrs.setString(Tag.PerformedProcedureStepEndDate, VR.DA, DateUtils.formatDA(null, end_date));
        mppsAttrs.setString(Tag.PerformedProcedureStepEndTime, VR.TM, DateUtils.formatTM(null, end_date));

        return mppsAttrs;
    }

    private Attributes makeMPPSUpdateCompletedAttributes(Attributes mppsCreateAttributes) {

        Attributes attributes = new Attributes();
        attributes.addSelected(mppsCreateAttributes, MPPS_SET_Selection);
        attributes.setString(Tag.PerformedProcedureStepStatus, VR.CS, MPPS.COMPLETED);

        return attributes;
    }

    private boolean checkCreationRule(MPPSCreationRule rule,
            List<Series> seriesList) {

        // if rule is NEVER, then just return
        if (rule.equals(MPPSCreationRule.NEVER)) {
            LOG.debug("MPPS creation rule is NEVER => NOT emulating MPPS.");
            return false;
        }

        // check if referenced mpps exists
        List<String> refMppsList = new ArrayList<String>();
        for (Series series : seriesList) {
            Sequence refPpsSeq = series.getAttributes().getSequence(Tag.ReferencedPerformedProcedureStepSequence);
            if (refPpsSeq != null
                    && refPpsSeq.size() > 0
                    && refPpsSeq.get(0).getString(Tag.ReferencedSOPInstanceUID) != null)
                refMppsList.add(refPpsSeq.get(0).getString(Tag.ReferencedSOPInstanceUID));
        }

        if (refMppsList.size() == 0) {
            LOG.debug("No reference to existing MPPS found => emulating MPPS...");
            return true;
        }

        // cases when there is an existing reference (refMppsList.size()>0)
        List<MPPS> mppsList = null;
        switch (rule) {
        case ALWAYS:
            LOG.debug("MPPS references found and rule is ALWAYS => emulating MPPS...");
            deleteMPPS(refMppsList);
            return true;
        case NEVER:
            // will never hit this line, see above
            return false;
        case NO_MPPS_CREATE:
            mppsList = fetchMPPS(refMppsList);
            if (mppsList == null || mppsList.size() == 0) {
                LOG.debug("MPPS references found, no mpps stored and rule NO_MPPS_CREATE => emulating MPPS...");
                return true;
            } else {
                LOG.debug("MPPS references found, mpps stored and rule NO_MPPS_CREATE => NOT emulating MPPS.");
                return false;
            }
        case NO_MPPS_FINAL:
            mppsList = fetchMPPS(refMppsList);
            for (MPPS mpps : mppsList) {
                if (mpps.getStatus() == MPPS.Status.IN_PROGRESS) {
                    LOG.debug("MPPS reference found, mpps stored, at least one MPPS not finalized and rule NO_MPPS_FINAL => emulating MPPS...");
                    deleteMPPS(refMppsList);
                    return true;
                }
            }
            LOG.debug("MPPS reference found, mpps stored, all MPPS finalized and rule NO_MPPS_FINAL => NOT emulating MPPS.");
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
