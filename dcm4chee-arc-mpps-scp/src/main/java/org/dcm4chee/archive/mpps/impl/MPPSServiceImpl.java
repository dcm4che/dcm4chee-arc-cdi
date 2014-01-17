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

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.net.Status;
import org.dcm4che.net.service.BasicMPPSSCP;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PerformedProcedureStep;
import org.dcm4chee.archive.entity.ScheduledProcedureStep;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.mpps.event.Create;
import org.dcm4chee.archive.mpps.event.MPPSEvent;
import org.dcm4chee.archive.mpps.event.Update;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.request.RequestService;
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
    private RequestService requestService;

    @Inject
    @Create
    Event<MPPSEvent> createMPPSEvent;

    @Inject
    @Update
    Event<MPPSEvent> updateMPPSEvent;    

    @Override
    public PerformedProcedureStep createPerformedProcedureStep(
            MPPSService service, String sopInstanceUID, Attributes attrs,
            StoreParam storeParam) throws DicomServiceException {
        try {
            find(sopInstanceUID);
            throw new DicomServiceException(Status.DuplicateSOPinstance)
                .setUID(Tag.AffectedSOPInstanceUID, sopInstanceUID);
        } catch (NoResultException e) {}
        Patient patient = service.findPatient(attrs);
        if (patient == null) {
            patient = service.createPatient(attrs, storeParam);
        } else {
            service.updatePatient(patient, attrs, storeParam);
        }
        PerformedProcedureStep mpps = new PerformedProcedureStep();
        mpps.setSopInstanceUID(sopInstanceUID);
        mpps.setAttributes(attrs,
                storeParam.getAttributeFilter(Entity.PerformedProcedureStep));
        mpps.setScheduledProcedureSteps(
                getScheduledProcedureSteps(
                        attrs.getSequence(Tag.ScheduledStepAttributesSequence),
                        patient,
                        storeParam));
        mpps.setPatient(patient);
        em.persist(mpps);
        
        //fire create event
        MPPSEvent event = new MPPSEvent(mpps);
        createMPPSEvent.fire(event);
        
        return mpps;
    }

    @Override
    public PerformedProcedureStep updatePerformedProcedureStep(
            MPPSService service, String sopInstanceUID, Attributes modified,
            StoreParam storeParam) throws DicomServiceException {
        PerformedProcedureStep pps;
        try {
            pps = find(sopInstanceUID);
        } catch (NoResultException e) {
            throw new DicomServiceException(Status.NoSuchObjectInstance)
                .setUID(Tag.AffectedSOPInstanceUID, sopInstanceUID);
        }
        if (pps.getStatus() != PerformedProcedureStep.Status.IN_PROGRESS)
            BasicMPPSSCP.mayNoLongerBeUpdated();

        Attributes attrs = pps.getAttributes();
        attrs.addAll(modified);
        pps.setAttributes(attrs,
                storeParam.getAttributeFilter(Entity.PerformedProcedureStep));
        if (pps.getStatus() != PerformedProcedureStep.Status.IN_PROGRESS) {
            if (!attrs.containsValue(Tag.PerformedSeriesSequence))
                throw new DicomServiceException(Status.MissingAttributeValue)
                        .setAttributeIdentifierList(Tag.PerformedSeriesSequence);
        }

        //fire update event
        MPPSEvent event = new MPPSEvent(pps);
        updateMPPSEvent.fire(event);

        return pps;
    }

    private PerformedProcedureStep find(String sopInstanceUID) {
        return em.createNamedQuery(
                PerformedProcedureStep.FIND_BY_SOP_INSTANCE_UID,
                PerformedProcedureStep.class)
             .setParameter(1, sopInstanceUID)
             .getSingleResult();
    }

    private Collection<ScheduledProcedureStep> getScheduledProcedureSteps(
            Sequence ssaSeq, Patient patient, StoreParam storeParam) {
        ArrayList<ScheduledProcedureStep> list =
                new ArrayList<ScheduledProcedureStep>(ssaSeq.size());
        for (Attributes ssa : ssaSeq) {
            if (ssa.containsValue(Tag.ScheduledProcedureStepID)
                    && ssa.containsValue(Tag.RequestedProcedureID)
                    && ssa.containsValue(Tag.AccessionNumber)) {
                ScheduledProcedureStep sps =
                        requestService.findOrCreateScheduledProcedureStep(
                                ssa, patient, storeParam);
                list.add(sps);
            }
        }
        return list;
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
        patientService.updatePatient(patient, attrs, storeParam);
    }

    @Override
    public Patient createPatient(Attributes attrs, StoreParam storeParam) {
        return patientService.createPatient(attrs, storeParam);
    }
}
