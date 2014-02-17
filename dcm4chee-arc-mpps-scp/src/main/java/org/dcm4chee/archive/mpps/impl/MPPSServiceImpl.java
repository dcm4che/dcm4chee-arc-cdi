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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4chee.archive.mpps.impl;

import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Device;
import org.dcm4che.net.Status;
import org.dcm4che.net.service.BasicMPPSSCP;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.Availability;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@Stateless
public class MPPSServiceImpl implements MPPSService {

    private static Logger LOG = LoggerFactory
            .getLogger(MPPSServiceImpl.class);

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @Inject
    private PatientService patientService;

    @Inject
    private CodeService codeService;

    @Override
    public MPPS createPerformedProcedureStep(String prompt,
            ApplicationEntity ae, String iuid, Attributes attrs,
            MPPSService service) throws DicomServiceException {
        ArchiveAEExtension arcAE =
                ae.getAEExtensionNotNull(ArchiveAEExtension.class);
        try {
            find(iuid);
            throw new DicomServiceException(Status.DuplicateSOPinstance)
                .setUID(Tag.AffectedSOPInstanceUID, iuid);
        } catch (NoResultException e) {}
        Patient patient = service.findPatient(attrs);
        StoreParam storeParam = arcAE.getStoreParam();
        if (patient == null) {
            patient = service.createPatient(attrs, storeParam);
        } else {
            service.updatePatient(patient, attrs, storeParam);
        }
        MPPS mpps = new MPPS();
        mpps.setSopInstanceUID(iuid);
        mpps.setAttributes(attrs);
        mpps.setPatient(patient);
        em.persist(mpps);
        return mpps;
    }

    @Override
    public MPPS updatePerformedProcedureStep(String prompt,
            ApplicationEntity ae, String iuid, Attributes modified,
            MPPSService service) throws DicomServiceException {
        MPPS pps;
        try {
            pps = find(iuid);
        } catch (NoResultException e) {
            throw new DicomServiceException(Status.NoSuchObjectInstance)
                .setUID(Tag.AffectedSOPInstanceUID, iuid);
        }
        if (pps.getStatus() != MPPS.Status.IN_PROGRESS)
            BasicMPPSSCP.mayNoLongerBeUpdated();

        Attributes attrs = pps.getAttributes();
        attrs.addAll(modified);
        pps.setAttributes(attrs);
        if (pps.getStatus() != MPPS.Status.IN_PROGRESS) {
            if (!attrs.containsValue(Tag.PerformedSeriesSequence))
                throw new DicomServiceException(Status.MissingAttributeValue)
                        .setAttributeIdentifierList(Tag.PerformedSeriesSequence);
        }

        if (pps.getStatus() == MPPS.Status.DISCONTINUED) {
            Attributes codeItem = attrs.getNestedDataset(
                    Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence);
            if (codeItem != null) {
                Code code = codeService.findOrCreate(new Code(codeItem));
                pps.setDiscontinuationReasonCode(code);
                checkIncorrectWorklistEntrySelected(prompt, pps, ae.getDevice());
            }
        }
        return pps;
    }

    private void checkIncorrectWorklistEntrySelected(String prompt, 
            MPPS pps, Device device) {
        ArchiveDeviceExtension arcDev = 
                device.getDeviceExtension(ArchiveDeviceExtension.class);
        Code code = arcDev != null
                ? (Code) arcDev.getIncorrectWorklistEntrySelectedCode()
                : null;
        if (code == null || !code.equals(pps.getDiscontinuationReasonCode()))
            return;

        HashMap<String,Attributes> map = new HashMap<String,Attributes>();
        for (Attributes seriesRef : pps.getAttributes()
                .getSequence(Tag.PerformedSeriesSequence)) {
            for (Attributes ref : seriesRef
                    .getSequence(Tag.ReferencedImageSequence)) {
                map.put(ref.getString(Tag.ReferencedSOPInstanceUID), ref);
            }
            for (Attributes ref : seriesRef
                    .getSequence(Tag.ReferencedNonImageCompositeSOPInstanceSequence)) {
                map.put(ref.getString(Tag.ReferencedSOPInstanceUID), ref);
            }
            List<Instance> insts = em
                    .createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID,
                            Instance.class)
                    .setParameter(1, seriesRef.getString(Tag.SeriesInstanceUID))
                    .getResultList();
            for (Instance inst : insts) {
                String iuid = inst.getSopInstanceUID();
                Attributes ref = map.get(iuid);
                if (ref != null) {
                    String cuid = inst.getSopClassUID();
                    String cuidInPPS = ref.getString(Tag.ReferencedSOPClassUID);
                    Series series = inst.getSeries();
                    Study study = series.getStudy();
                    if (!cuid.equals(cuidInPPS)) {
                        LOG.warn("{}: SOP Class of received Instance[iuid={}, cuid={}] "
                                + "of Series[iuid={}] of Study[iuid={}] differs from"
                                + "SOP Class[cuid={}] referenced by MPPS[iuid={}]",
                                prompt, iuid, cuid, series.getSeriesInstanceUID(), 
                                study.getStudyInstanceUID(), cuidInPPS,
                                pps.getSopInstanceUID());
                    }
                    inst.setRejectionNoteCode(code);
                    inst.setAvailability(Availability.UNAVAILABLE);
                    series.resetNumberOfInstances();
                    study.resetNumberOfInstances();
                    LOG.info("{}: Reject Instance[pk={},iuid={}] by MPPS Discontinuation Reason - {}",
                            prompt, inst.getPk(), iuid,
                            code);
                }
            }
            map.clear();
        }
    }

    private MPPS find(String sopInstanceUID) {
        return em.createNamedQuery(
                MPPS.FIND_BY_SOP_INSTANCE_UID,
                MPPS.class)
             .setParameter(1, sopInstanceUID)
             .getSingleResult();
    }

    @Override
    public Patient findPatient(Attributes attrs)
            throws DicomServiceException {
        try {
            return patientService.findPatientFollowMerged(attrs, new IDPatientSelector());
        } catch (NonUniquePatientException e) {
            LOG.info("Could not find unique Patient Record for received MPPS - create new Patient Record", e);
            return null;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    @Override
    public void updatePatient(Patient patient, Attributes attrs, StoreParam storeParam) {
        patientService.updatePatient(patient, attrs, storeParam, false);
    }

    @Override
    public Patient createPatient(Attributes attrs, StoreParam storeParam) {
        return patientService.createPatient(attrs, storeParam);
    }
}
