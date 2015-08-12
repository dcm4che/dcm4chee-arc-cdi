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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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
package org.dcm4chee.archive.mpps;

import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.BasicMPPSSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.patient.PatientSelectorFactory;
import org.dcm4chee.archive.patient.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Roman K
 */
@Stateless
public class MPPSServiceEJB {
    private static final Logger LOG = LoggerFactory.getLogger(MPPSServiceEJB.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    private PatientService patientService;

    @Inject
    private CodeService codeService;

    @Inject
    Device device;

    public MPPS createPerformedProcedureStep(
            ApplicationEntity ae,
            String mppsIuid,
            Attributes attrs) throws DicomServiceException {

        // mpps with such iuid must not exist
        try {
            findPPS(mppsIuid);
            throw new DicomServiceException(Status.DuplicateSOPinstance, "PPS with iuid " + mppsIuid + " already exists")
                    .setUID(Tag.AffectedSOPInstanceUID, mppsIuid);
        } catch (NoResultException ignore) {
        }

        StoreParam storeParam = ae.getAEExtensionNotNull(ArchiveAEExtension.class).getStoreParam();
        Patient patient;
        try {
            patient = patientService.updateOrCreatePatientOnMPPSNCreate(attrs, PatientSelectorFactory.createSelector(storeParam), storeParam);
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }

        Attributes mppsAttrs = new Attributes(attrs.size());

        /// TODO: not selected?
        mppsAttrs.addNotSelected(attrs,storeParam.getAttributeFilter(Entity.Patient).getCompleteSelection(attrs));
        MPPS mpps = new MPPS();
        mpps.setSopInstanceUID(mppsIuid);
        mpps.setAttributes(mppsAttrs, storeParam.getNullValueForQueryFields());
        mpps.setPatient(patient);

        em.persist(mpps);
        return mpps;
    }

    public MPPS updatePerformedProcedureStep(ApplicationEntity ae,
                                             String iuid,
                                             Attributes modified) throws DicomServiceException {
        MPPS pps;
        try {
            pps = findPPS(iuid);
        } catch (NoResultException e) {
            throw new DicomServiceException(Status.NoSuchObjectInstance).setUID(Tag.AffectedSOPInstanceUID, iuid);
        }

        if (pps.getStatus() != MPPS.Status.IN_PROGRESS)
            BasicMPPSSCP.mayNoLongerBeUpdated();

        // overwrite attributes
        Attributes attrs = pps.getAttributes();
        attrs.addAll(modified);
        StoreParam storeParam = ae.getAEExtensionNotNull(ArchiveAEExtension.class).getStoreParam();
        pps.setAttributes(attrs, storeParam.getNullValueForQueryFields());

        // check if allowed to change
        if (pps.getStatus() != MPPS.Status.IN_PROGRESS) {
            if (!attrs.containsValue(Tag.PerformedSeriesSequence))
                throw new DicomServiceException(Status.MissingAttributeValue)
                        .setAttributeIdentifierList(Tag.PerformedSeriesSequence);
        }

        // What happens if we receive this before all the instances are stored? - See the StoreServiceMPPSDecorator
        // reject stored instances that are a result of an incorrectly chosen worklist entry
        if (pps.getStatus() == MPPS.Status.DISCONTINUED) {

            // set discontinuation code
            Attributes codeItem = attrs.getNestedDataset(Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence);
            if (codeItem != null) {
                Code code = codeService.findOrCreate(new Code(codeItem));
                pps.setDiscontinuationReasonCode(code);
            }

            if (pps.discontinuedForReason(incorrectWorklistEntrySelectedCode()))
                rejectReferencedInstancesDueToIncorrectlySelectedWorklistEntry(pps);
        }
        em.merge(pps);
        return pps;
    }

    private Code incorrectWorklistEntrySelectedCode() {
        return (Code) device
                .getDeviceExtension(ArchiveDeviceExtension.class)
                .getIncorrectWorklistEntrySelectedCode();
    }

    private void rejectReferencedInstancesDueToIncorrectlySelectedWorklistEntry(MPPS pps) {
        HashMap<String, Attributes> referencedInstancesByIuid = new HashMap<>();

        // inited by first local instance if found
        Study study = null;

        for (Attributes seriesRef : pps.getAttributes().getSequence(Tag.PerformedSeriesSequence)) {

            // put all mpps- referenced instances into a map by iuid
            for (Attributes ref : seriesRef.getSequence(Tag.ReferencedImageSequence))
                referencedInstancesByIuid.put(ref.getString(Tag.ReferencedSOPInstanceUID), ref);
            for (Attributes ref : seriesRef.getSequence(Tag.ReferencedNonImageCompositeSOPInstanceSequence))
                referencedInstancesByIuid.put(ref.getString(Tag.ReferencedSOPInstanceUID), ref);

            // inited by first local instance if found
            Series series = null;

            // iterate over the instances we have locally for this series
            for (Instance localInstance : findBySeriesInstanceUID(seriesRef)) {
                String iuid = localInstance.getSopInstanceUID();
                Attributes referencedInstance = referencedInstancesByIuid.get(iuid);
                if (referencedInstance != null) {
                    String cuid = localInstance.getSopClassUID();
                    String cuidInPPS = referencedInstance.getString(Tag.ReferencedSOPClassUID);

                    series = localInstance.getSeries();
                    study = series.getStudy();

                    if (!cuid.equals(cuidInPPS)) {
                        LOG.warn("SOP Class of received Instance[iuid={}, cuid={}] "
                                        + "of Series[iuid={}] of Study[iuid={}] differs from "
                                        + "SOP Class[cuid={}] referenced by MPPS[iuid={}]",
                                iuid, cuid, series.getSeriesInstanceUID(),
                                study.getStudyInstanceUID(), cuidInPPS,
                                pps.getSopInstanceUID());
                    }

                    LOG.info("Reject Instance[pk={},iuid={}] by MPPS Discontinuation Reason - {}",
                            localInstance.getPk(), iuid,
                            incorrectWorklistEntrySelectedCode());

                    localInstance.setRejectionNoteCode(incorrectWorklistEntrySelectedCode());
                    em.merge(localInstance);
                }
            }
            referencedInstancesByIuid.clear();

            // TODO: Derived fields: this should be decoupled...
            // cleanup
            if (series != null) {
                series.clearQueryAttributes();
                em.merge(series);
            }

        }
        // TODO: Derived fields: this should be decoupled...
        // cleanup
        if (study != null) {
            study.clearQueryAttributes();
            em.merge(study);
        }
    }

    public List<Instance> findBySeriesInstanceUID(Attributes seriesRef) {
        return em.createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID, Instance.class)
                .setParameter(1, seriesRef.getString(Tag.SeriesInstanceUID))
                .getResultList();
    }

    public MPPS findPPS(String sopInstanceUID) {
        return em.createNamedQuery(MPPS.FIND_BY_SOP_INSTANCE_UID_EAGER, MPPS.class)
                .setParameter(1, sopInstanceUID)
                .getSingleResult();
    }

    public Study findStudyByUID(String studyUID) {
        String queryStr = "SELECT s FROM Study s JOIN FETCH s.series se WHERE s.studyInstanceUID = ?1";
        Query query = em.createQuery(queryStr);
        Study study = null;
        try {
            query.setParameter(1, studyUID);
            study = (Study) query.getSingleResult();
        } catch (NoResultException e) {
            LOG.error("Unable to find study {}, related to an already performed procedure", studyUID);
        }
        return study;
    }
}
