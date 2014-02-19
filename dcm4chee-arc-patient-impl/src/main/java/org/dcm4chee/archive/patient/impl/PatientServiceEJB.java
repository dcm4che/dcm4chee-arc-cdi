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

package org.dcm4chee.archive.patient.impl;

import java.util.Collection;
import java.util.HashSet;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.PersonName;
import org.dcm4che.data.Tag;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.MWLItem;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
import org.dcm4chee.archive.patient.PatientMergedException;
import org.dcm4chee.archive.patient.PatientSelector;
import org.dcm4chee.archive.patient.PatientService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Stateless
public class PatientServiceEJB implements PatientService {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    public Patient findPatient(Attributes attrs, PatientSelector selector)
            throws NonUniquePatientException, PatientMergedException {
        Patient patient = selector.select(createQuery(attrs).getResultList(), attrs);
        if (patient != null && patient.getMergedWith() != null)
            throw new PatientMergedException(patient);
        return patient;
    }

    @Override
    public Patient findPatientOnStore(Attributes attrs, PatientSelector selector)
            throws NonUniquePatientException, PatientCircularMergedException {
        Patient patient = selector.select(createQuery(attrs).getResultList(), attrs);
        if (patient != null && patient.getMergedWith() != null)
            followMergedWith(patient);
        return patient;
    }

    private TypedQuery<Patient> createQuery(Attributes attrs)
            throws NonUniquePatientException {
        String pid = attrs.getString(Tag.PatientID);
        if (pid != null)
            return em.createNamedQuery(
                    Patient.FIND_BY_PATIENT_ID, Patient.class)
                .setParameter(1, pid);

        String pn = attrs.getString(Tag.PatientName);
        if (pn != null)
            return em.createNamedQuery(
                    Patient.FIND_BY_PATIENT_NAME, Patient.class)
                    .setParameter(1, 
                            new PersonName(pn, true)
                    .toString(PersonName.Group.Alphabetic, false));

        throw new NonUniquePatientException("No Patient ID and no Patient Name");

    }

    private Patient followMergedWith(Patient patient)
            throws PatientCircularMergedException {
        HashSet<Long> pks = new HashSet<Long>();
        Patient mergedWith;
        pks.add(patient.getPk());
        while ((mergedWith = patient.getMergedWith()) != null) {
            if (!pks.add(mergedWith.getPk())) {
                throw new PatientCircularMergedException(patient);
            }
            patient = mergedWith;
        }
        return patient;
    }

    @Override
    public Patient createPatientOnStore(Attributes attrs, StoreParam storeParam) {
        Patient patient = new Patient();
        patient.setAttributes(attrs,
                storeParam.getAttributeFilter(Entity.Patient),
                storeParam.getFuzzyStr());
        em.persist(patient);
        return patient;
    }


    @Override
    public void updatePatientOnStore(Patient patient, Attributes attrs,
            StoreParam storeParam) {
        updatePatient(patient, attrs, storeParam, false);
    }

    private void updatePatient(Patient patient, Attributes attrs,
            StoreParam storeParam, boolean overwriteValues) {
        Attributes patientAttrs = patient.getAttributes();
        AttributeFilter filter = storeParam.getAttributeFilter(Entity.Patient);
        if (overwriteValues
                ? patientAttrs.updateSelected(attrs, null, filter.getSelection())
                : patientAttrs.mergeSelected(attrs, filter.getSelection())) {
            patient.setAttributes(patientAttrs, filter, storeParam.getFuzzyStr());
        }
    }

    @Override
    public Patient updateOrCreatePatientByHL7(Attributes attrs, StoreParam storeParam)
            throws NonUniquePatientException, PatientMergedException {
        //TODO make PatientSelector configurable
        PatientSelector selector = new IDPatientSelector();
        Patient patient = findPatient(attrs, selector);
        if (patient == null) {
            return createPatientOnStore(attrs, storeParam);
        }
        updatePatient(patient, attrs, storeParam, true);
        return patient;
    }

    @Override
    public void mergePatientByHL7(Attributes attrs, Attributes priorAttrs,
            StoreParam storeParam) throws NonUniquePatientException,
            PatientMergedException, PatientCircularMergedException {
        Patient prior = updateOrCreatePatientByHL7(priorAttrs, storeParam);
        Patient pat = updateOrCreatePatientByHL7(attrs, storeParam);
        mergePatient(pat, prior);
    }

    private void mergePatient(Patient pat, Patient prior)
            throws PatientCircularMergedException {
        if (pat == prior)
            throw new PatientCircularMergedException(pat);
        Collection<Study> studies = prior.getStudies();
        if (studies != null)
            for (Study study : studies)
                study.setPatient(pat);
        Collection<MWLItem> mwlItems = prior.getModalityWorklistItems();
        if (mwlItems != null)
            for (MWLItem mwlItem : mwlItems)
                mwlItem.setPatient(pat);
        Collection<MPPS> mpps = prior.getModalityPerformedProcedureSteps();
        if (mpps != null)
            for (MPPS pps : mpps)
                pps.setPatient(pat);
        prior.setMergedWith(pat);
    }
}
