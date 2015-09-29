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

import java.util.*;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Tag;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.MetadataUpdateStrategy;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.issuer.IssuerService;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
import org.dcm4chee.archive.patient.PatientMergedException;
import org.dcm4chee.archive.patient.PatientSelector;
import org.dcm4chee.archive.patient.PatientSelectorFactory;
import org.dcm4chee.archive.patient.PatientService;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.jpa.hibernate.HibernateSubQuery;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
@Stateless
public class PatientServiceEJB implements PatientService {

    private static Logger LOG = LoggerFactory
            .getLogger(PatientServiceEJB.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    private IssuerService issuerService;

    @Override
    public Patient updateOrCreatePatientOnCStore(Attributes attrs,
            PatientSelector selector, StoreParam storeParam)
            throws PatientCircularMergedException {
        return updateOrCreatePatientByDICOM(attrs, selector, storeParam);
    }

    @Override
    public Patient updateOrCreatePatientOnMPPSNCreate(Attributes attrs,
            PatientSelector selector, StoreParam storeParam)
            throws PatientCircularMergedException {
        return updateOrCreatePatientByDICOM(attrs, selector, storeParam);
    }

    private Patient updateOrCreatePatientByDICOM(Attributes attrs,
            PatientSelector selector, StoreParam storeParam)
            throws PatientCircularMergedException {
        Set<IDWithIssuer> pids = IDWithIssuer.pidsOf(attrs);
        Patient patient = null;
        try {
            patient = findPatientByDICOM(pids, attrs, selector);
        } catch (NonUniquePatientException e) {
            LOG.info("Could not associate unique Patient Record to "
                    + "received DICOM object - create new Patient Record:", e);
        }
        if (patient == null)
            return createPatient(pids, attrs, storeParam);

        patient = followMergedWith(patient);
        updatePatientByDICOM(patient, attrs, storeParam, pids);
        return patient;
    }

    private Patient findPatientByDICOM(Collection<IDWithIssuer> pids,
            Attributes attrs, PatientSelector selector)
            throws NonUniquePatientException {
        List<Patient> candidates;
        if (!pids.isEmpty()) {
            candidates = findPatientByIDs(pids);
        } else {
            String familyName = new PersonName(attrs.getString(Tag.PatientName))
                    .get(PersonName.Component.FamilyName);
            if (familyName != null)
                candidates = findPatientByFamilyName(familyName);
            else
                throw new NonUniquePatientException(
                        "No Patient ID and no Patient Family Name");
        }
        return selector.select(candidates, attrs, pids);
    }

    private List<Patient> findPatientByFamilyName(String familyName) {
        return em
                .createNamedQuery(Patient.FIND_BY_PATIENT_FAMILY_NAME,
                        Patient.class).setParameter(1, familyName)
                .getResultList();
    }

    // private List<Patient> findPatientByIDs(Collection<IDWithIssuer> pids) {
    // BooleanBuilder builder = new BooleanBuilder();
    // Collection<BooleanExpression> eqIDs = new ArrayList<BooleanExpression>(
    // pids.size());
    // for (IDWithIssuer pid : pids) {
    // BooleanExpression eqID = QPatientID.patientID.id.eq(pid.getID());
    // if (pid.getIssuer() == null) {
    // builder.or(eqID);
    // } else {
    // builder.or(ExpressionUtils.and(eqID,
    // eqOrNoIssuer(pid.getIssuer())));
    // eqIDs.add(eqID);
    // }
    // }
    // BooleanExpression matchingIDs = new HibernateSubQuery()
    // .from(QPatientID.patientID)
    // .leftJoin(QPatientID.patientID.issuer, QIssuer.issuer)
    // .where(ExpressionUtils.and(
    // QPatientID.patientID.patient.eq(QPatient.patient),
    // builder)).exists();
    // Session session = em.unwrap(Session.class);
    // return new HibernateQuery(session).from(QPatient.patient)
    // .where(matchingIDs).list(QPatient.patient);
    // }

    private List<Patient> findPatientByIDs(Collection<IDWithIssuer> pids) {
        BooleanBuilder builder = new BooleanBuilder();
        Collection<BooleanExpression> eqIDs = new ArrayList<BooleanExpression>(
                pids.size());
        for (IDWithIssuer pid : pids) {
            BooleanExpression eqID = QPatientID.patientID.id.eq(pid.getID());
            if (pid.getIssuer() == null) {
                builder.or(eqID);
            } else {
                builder.or(ExpressionUtils.and(eqID,
                        eqOrNoIssuer(pid.getIssuer())));
                eqIDs.add(eqID);
            }
        }
        HibernateSubQuery matchingIDs = new HibernateSubQuery()
                .from(QPatientID.patientID)
                .leftJoin(QPatientID.patientID.issuer, QIssuer.issuer)
                .where(builder);
        Session session = em.unwrap(Session.class);
        return new HibernateQuery(session)
                .from(QPatient.patient)
                .leftJoin(QPatient.patient.attributesBlob,
                        QAttributesBlob.attributesBlob)
                .fetch()
                .where(QPatient.patient.pk.in(matchingIDs
                        .list(QPatientID.patientID.patient.pk)))
                .list(QPatient.patient);
    }

    private Predicate eqOrNoIssuer(org.dcm4che3.data.Issuer issuer) {
        String id = issuer.getLocalNamespaceEntityID();
        String uid = issuer.getUniversalEntityID();
        String uidType = issuer.getUniversalEntityIDType();

        BooleanBuilder builder = new BooleanBuilder();
        if (id != null)
            builder.and(eqOrNoLocalNamespaceEntityID(id));

        if (uid != null)
            builder.and(eqOrNoUniversalEntityID(uid, uidType));

        return builder;
    }

    private Predicate eqOrNoLocalNamespaceEntityID(String id) {
        return ExpressionUtils.or(QIssuer.issuer.localNamespaceEntityID.eq(id),
                QIssuer.issuer.localNamespaceEntityID.isNull());
    }

    private Predicate eqOrNoUniversalEntityID(String uid, String uidType) {
        return ExpressionUtils.or(ExpressionUtils.and(
                        QIssuer.issuer.universalEntityID.eq(uid),
                        QIssuer.issuer.universalEntityIDType.eq(uidType)),
                QIssuer.issuer.universalEntityID.isNull());
    }

    private Patient followMergedWith(Patient patient)
            throws PatientCircularMergedException {
        Patient mergedWith = patient.getMergedWith();
        if (mergedWith == null)
            return patient;

        HashSet<Long> pks = new HashSet<Long>();
        pks.add(patient.getPk());
        do {
            if (!pks.add(mergedWith.getPk())) {
                throw new PatientCircularMergedException(patient);
            }
            patient = mergedWith;
            mergedWith = patient.getMergedWith();
        } while (mergedWith != null);
        return patient;
    }

    private Patient createPatient(Set<IDWithIssuer> pids,
            Attributes attrs, StoreParam storeParam) {
        Patient patient = new Patient();
        patient.setAttributes(attrs,
                storeParam.getAttributeFilter(Entity.Patient),
                storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());

        if (pids.isEmpty() && storeParam.getIssuerOfPatientID()!=null) {
            pids= new HashSet<IDWithIssuer>();
            pids.add(new IDWithIssuer(UIDUtils.createUID(),storeParam.getIssuerOfPatientID()));
        }

        patient.setNoPatientID(pids.isEmpty());
        patient.setPatientIDs(createPatientIDs(pids, patient,
                storeParam.isDeIdentifyLogs()));
        em.persist(patient);
        LOG.info("Create {}", patient.toString(storeParam.isDeIdentifyLogs()));
        return patient;
    }

    private Collection<PatientID> createPatientIDs(
            Collection<IDWithIssuer> pids, Patient patient, boolean deidentify) {
        Collection<PatientID> patientIDs = new ArrayList<PatientID>(pids.size());
        for (IDWithIssuer pid : pids)
            patientIDs.add(createPatientID(pid, patient, deidentify));
        return patientIDs;
    }

    private PatientID createPatientID(IDWithIssuer pid, Patient patient,
            boolean deidentify) {
        PatientID patientID = new PatientID();
        patientID.setID(pid.getID());
        patientID.setIdentifierTypeCode(pid.getIdentifierTypeCode());
        patientID.setIssuer(findOrCreateIssuer(pid.getIssuer()));
        patientID.setPatient(patient);
        LOG.info("Add {} to {}", patientID, patient.toString(deidentify));
        return patientID;
    }

    private Issuer findOrCreateIssuer(org.dcm4che3.data.Issuer issuer) {
        if (issuer == null)
            return null;

        return issuerService.findOrCreate(new Issuer(issuer));
    }

    @Override
    public void updatePatientByCStore(Patient patient, Attributes attrs,
            StoreParam storeParam) {
        updatePatientByDICOM(patient, attrs, storeParam,
                IDWithIssuer.pidsOf(attrs));
    }

    private void updatePatientByDICOM(Patient patient, Attributes attrs,
            StoreParam storeParam, Collection<IDWithIssuer> pids) {
        if (mergePatientIDs(patient, pids, storeParam.isDeIdentifyLogs())) {
            patient.updateOtherPatientIDs();
        }
        Attributes patientAttrs = patient.getAttributes();
        AttributeFilter filter = storeParam.getAttributeFilter(Entity.Patient);
        if (Utils.updateAttributes(patientAttrs, attrs, null , filter, MetadataUpdateStrategy.COERCE_MERGE))
            patient.setAttributes(patientAttrs, filter,storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
    }

    private boolean mergePatientIDs(Patient patient,
            Collection<IDWithIssuer> pids, boolean deidentify) {
        boolean modified = false;
        Collection<PatientID> patientIDs = patient.getPatientIDs();
        Collection<IDWithIssuer> add = new ArrayList<IDWithIssuer>(pids);
        for (Iterator<IDWithIssuer> iter = add.iterator(); iter.hasNext();) {
            IDWithIssuer pid = iter.next();
            PatientID patientID = selectFrom(patientIDs, pid);
            if (patientID == null)
                continue;

            iter.remove();
            if (pid.getIssuer() == null)
                continue;

            Issuer issuer = patientID.getIssuer();
            if (issuer == null) {
                patientID.setIssuer(findOrCreateIssuer(pid.getIssuer()));
                modified = true;
                LOG.info("Set Issuer of {} of Patient {}", patientID,
                        patient.toString(deidentify));
            } else if (issuer.merge(pid.getIssuer())) {
                modified = true;
                LOG.info("Updated Issuer of {} of Patient {}", patientID,
                        patient.toString(deidentify));
            }
        }

        if (add.size() == pids.size()) { // no matching pid
            LOG.info("Patient IDs of exisiting {} does not match any Patient ID in received DICOM object - ignore Patient IDs in received object");
            return false;
        }

        for (IDWithIssuer pid : add)
            patientIDs.add(createPatientID(pid, patient, deidentify));

        patient.setNoPatientID(patientIDs.isEmpty());
        return modified || !add.isEmpty();
    }

    private PatientID selectFrom(Collection<PatientID> patientIDs,
            IDWithIssuer pid) {
        for (PatientID patientID : patientIDs) {
            if (pid.getID().equals(patientID.getID())
                    && (pid.getIssuer() == null

                    || pid.getIssuer().matches(patientID.getIssuer())))
                return patientID;
        }
        return null;
    }

    private void updatePatientByHL7(Patient patient, Attributes attrs,
            Collection<IDWithIssuer> pids, StoreParam storeParam) {
        if (mergePatientIDs(patient, pids, storeParam.isDeIdentifyLogs())) {
            patient.updateOtherPatientIDs();
        }
        Attributes patientAttrs = patient.getAttributes();
        AttributeFilter filter = storeParam.getAttributeFilter(Entity.Patient);
        if (Utils.updateAttributes(patientAttrs, attrs, null, filter, MetadataUpdateStrategy.COERCE_MERGE))
            patient.setAttributes(patientAttrs, filter, storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
    }

    @Override
    public Patient updateOrCreatePatientByHL7(Attributes attrs,
            StoreParam storeParam) throws NonUniquePatientException,
            PatientMergedException {
        // TODO make PatientSelector configurable
        PatientSelector selector = PatientSelectorFactory
                .createSelector(storeParam);
        Set<IDWithIssuer> pids = IDWithIssuer.pidsOf(attrs);
        return updateOrCreatePatientByHL7(attrs, storeParam, selector, pids);
    }

    private Patient updateOrCreatePatientByHL7(Attributes attrs,
            StoreParam storeParam, PatientSelector selector,
            Set<IDWithIssuer> pids) throws NonUniquePatientException,
            PatientMergedException {
        Patient patient = selector.select(findPatientByIDs(pids), attrs, pids);
        if (patient == null)
            return createPatient(pids, attrs, storeParam);

        if (patient.getMergedWith() != null)
            throw new PatientMergedException(patient);

        updatePatientByHL7(patient, attrs, pids, storeParam);
        return patient;
    }

    @Override
    public void mergePatientByHL7(Attributes attrs, Attributes priorAttrs,
            StoreParam storeParam) throws NonUniquePatientException,
            PatientMergedException {
        PatientSelector selector = PatientSelectorFactory
                .createSelector(storeParam);
        Set<IDWithIssuer> pids = IDWithIssuer.pidsOf(attrs);
        Set<IDWithIssuer>  priorPIDs = IDWithIssuer.pidsOf(priorAttrs);
        Patient prior = updateOrCreatePatientByHL7(priorAttrs, storeParam,
                selector, priorPIDs);
        Patient pat = updateOrCreatePatientByHL7(attrs, storeParam, selector,
                pids);
        mergePatient(pat, prior, priorPIDs, storeParam.isDeIdentifyLogs());
    }

    private void mergePatient(Patient pat, Patient prior,
            Collection<IDWithIssuer> priorPIDs, boolean deidentify) {
        if (pat == prior)
            throw new IllegalArgumentException("Cannot merge " + pat
                    + " with itself");

        LOG.info("Merge {} with {}", prior, pat);
        moveStudies(pat, prior);
        moveModalityWorklistItems(pat, prior);
        moveModalityPerformedProcedureSteps(pat, prior);

        boolean movePatientIDs = movePatientIDs(pat, prior, priorPIDs);
        Collection<Patient> linkedPatients = prior.getLinkedPatients();
        if (movePatientIDs || !linkedPatients.isEmpty()) {
            for (Patient linked : linkedPatients) {
                unlinkPatientIDs(prior, linked, deidentify);
                unlinkPatientIDs(linked, prior, deidentify);
                linkPatientIDs(pat, linked, deidentify);
                linkPatientIDs(linked, pat, deidentify);
                linked.updateOtherPatientIDs();
            }
            prior.updateOtherPatientIDs();
            pat.updateOtherPatientIDs();
        }
        prior.setMergedWith(pat);
    }

    @Override
    public void linkPatient(Attributes attrs, Attributes otherAttrs,
            StoreParam storeParam) throws NonUniquePatientException,
            PatientMergedException {
        Patient pat = updateOrCreatePatientByHL7(attrs, storeParam);
        Patient other = updateOrCreatePatientByHL7(otherAttrs, storeParam);
        linkPatient(pat, other, storeParam.isDeIdentifyLogs());
    }

    private void linkPatient(Patient pat, Patient other, boolean deidentify) {
        if (pat == other)
            throw new IllegalArgumentException("Cannot link " + pat
                    + " with itself");

        LOG.info("Link {} with {}", other, pat);
        linkPatientIDs(pat, other, deidentify);
        linkPatientIDs(other, pat, deidentify);
        pat.updateOtherPatientIDs();
        other.updateOtherPatientIDs();
    }

    private void linkPatientIDs(Patient pat, Patient other, boolean deidentify) {
        Collection<PatientID> linkedPatientIDs = pat.getLinkedPatientIDs();
        if (linkedPatientIDs == null) {
            linkedPatientIDs = new ArrayList<PatientID>();
            pat.setLinkedPatientIDs(linkedPatientIDs);
        }
        for (PatientID pid : other.getPatientIDs()) {
            if (!contains(linkedPatientIDs, pid)) {
                linkedPatientIDs.add(pid);
                LOG.info("Link {} of {} to {}", pid,
                        other.toString(deidentify), pat.toString(deidentify));
            }
        }
    }

    private boolean contains(Collection<PatientID> pids, PatientID other) {
        long pk = other.getPk();
        for (PatientID pid : pids)
            if (pid.getPk() == pk)
                return true;
        return false;
    }

    @Override
    public void unlinkPatient(Attributes attrs, Attributes otherAttrs,
            StoreParam storeParam) throws NonUniquePatientException,
            PatientMergedException {
        // TODO make PatientSelector configurable
        PatientSelector selector = PatientSelectorFactory
                .createSelector(storeParam);
        Collection<IDWithIssuer> pids = IDWithIssuer.pidsOf(attrs);
        Patient pat = selector.select(findPatientByIDs(pids), attrs, pids);
        if (pat == null)
            return;
        if (pat.getMergedWith() != null)
            throw new PatientMergedException(pat);

        Collection<IDWithIssuer> otherPIDs = IDWithIssuer.pidsOf(otherAttrs);
        Patient other = selector.select(findPatientByIDs(otherPIDs), attrs,
                otherPIDs);
        if (other == null)
            return;

        if (other.getMergedWith() != null)
            throw new PatientMergedException(other);

        if (pat == other)
            throw new IllegalArgumentException("Cannot link " + pat
                    + " with itself");

        LOG.info("Unlink {} from {}", other, pat);
        mergePatientIDs(pat, pids, storeParam.isDeIdentifyLogs());
        mergePatientIDs(other, otherPIDs, storeParam.isDeIdentifyLogs());
        unlinkPatientIDs(pat, other, storeParam.isDeIdentifyLogs());
        unlinkPatientIDs(other, pat, storeParam.isDeIdentifyLogs());
        pat.updateOtherPatientIDs();
        other.updateOtherPatientIDs();
    }

    private void unlinkPatientIDs(Patient pat, Patient other, boolean deidentify) {
        Collection<PatientID> linkedPatientIDs = pat.getLinkedPatientIDs();
        for (PatientID pid : other.getPatientIDs()) {
            if (removePatientID(linkedPatientIDs, pid)) {
                LOG.info("Unlink {} of {} from {}", pid,
                        other.toString(deidentify), pat.toString(deidentify));
            }
        }
    }

    private boolean removePatientID(Collection<PatientID> pids, PatientID other) {
        long pk = other.getPk();
        for (Iterator<PatientID> iter = pids.iterator(); iter.hasNext();) {
            PatientID pid = iter.next();
            if (pid.getPk() == pk) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    private void moveStudies(Patient pat, Patient prior) {
        Collection<Study> studies = (pat.getStudies() != null ? pat
                .getStudies() : new ArrayList<Study>());
        for (Iterator<Study> iter = (prior.getStudies() != null ? prior
                .getStudies().iterator() : new ArrayList<Study>().iterator()); iter
                .hasNext();) {
            Study study = iter.next();
            iter.remove();
            study.setPatient(pat);
            studies.add(study);
            LOG.info("Move {} from {} to {}", study, prior, pat);
        }
    }

    private void moveModalityWorklistItems(Patient pat, Patient prior) {
        Collection<MWLItem> mwlItems = (pat.getModalityWorklistItems() != null ? pat
                .getModalityWorklistItems() : new ArrayList<MWLItem>());
        for (Iterator<MWLItem> iter = (prior.getModalityWorklistItems() != null ? prior
                .getModalityWorklistItems().iterator()
                : new ArrayList<MWLItem>().iterator()); iter.hasNext();) {
            MWLItem mwlItem = iter.next();
            iter.remove();
            mwlItem.setPatient(pat);
            mwlItems.add(mwlItem);
            LOG.info("Move {} from {} to {}", mwlItem, prior, pat);
        }
    }

    private void moveModalityPerformedProcedureSteps(Patient pat, Patient prior) {
        Collection<MPPS> mppss = (pat.getModalityPerformedProcedureSteps() != null ? pat
                .getModalityPerformedProcedureSteps() : new ArrayList<MPPS>());
        for (Iterator<MPPS> iter = (prior.getModalityPerformedProcedureSteps() != null ? prior
                .getModalityPerformedProcedureSteps().iterator()
                : new ArrayList<MPPS>().iterator()); iter.hasNext();) {
            MPPS mpps = iter.next();
            iter.remove();
            mpps.setPatient(pat);
            mppss.add(mpps);
            LOG.info("Move {} from {} to {}", mpps, prior, pat);
        }
    }

    private boolean movePatientIDs(Patient pat, Patient prior,
            Collection<IDWithIssuer> priorPIDs) {
        int moved = 0;
        Collection<PatientID> patientIDs = pat.getPatientIDs();
        for (Iterator<PatientID> iter = prior.getPatientIDs().iterator(); iter
                .hasNext();) {
            PatientID patientID = iter.next();
            if (!contains(priorPIDs, patientID.toIDWithIssuer())) {
                iter.remove();
                patientID.setPatient(pat);
                patientIDs.add(patientID);
                LOG.info("Move {} from {} to {}", patientID, prior, pat);
                moved++;
            }
        }
        return moved > 0;
    }

    private boolean contains(Collection<IDWithIssuer> pids, IDWithIssuer pid) {
        for (IDWithIssuer pid1 : pids) {
            if (pid1.matches(pid))
                return true;
        }
        return false;
    }

    @Override
    public void updatePatientID(Attributes srcPatientAttrs,
            Attributes otherPatientAttrs, StoreParam storeParam)
            throws NonUniquePatientException {
        Collection<IDWithIssuer> pids = IDWithIssuer.pidsOf(srcPatientAttrs);
        Collection<IDWithIssuer> otherPids = IDWithIssuer
                .pidsOf(otherPatientAttrs);
        List<Patient> srcPatient;
        List<Patient> otherPatient;
        srcPatient = findPatientByIDs(pids);
        if (srcPatient.isEmpty())
            throw new IllegalArgumentException(
                    "No patient found for source attributes - expected 1 match");
        else if (srcPatient.size() > 1)
            throw new IllegalArgumentException(
                    "More than one patient found for source attributes - expected 1 match");

        Patient patient = srcPatient.get(0);
        otherPatient = findPatientByIDs(otherPids);
        if (!otherPatient.isEmpty())
            throw new IllegalArgumentException(
                    "One or more patients found for target update attributes - expected 0 match");
        // check if original ids have links
        ArrayList<IDWithIssuer> linkedPatientsIDs = new ArrayList<IDWithIssuer>();
        for (PatientID linkedID : patient.getLinkedPatientIDs()) {
            linkedPatientsIDs.add(linkedID.toIDWithIssuer());
        }
        Collection<Patient> linkedPatients = findPatientByIDs(linkedPatientsIDs);
        Iterator<Patient> linkedPatientsIterator = linkedPatients.iterator();
        try {
            while (linkedPatientsIterator.hasNext()) {
                Patient other = linkedPatientsIterator.next();
                unlinkPatientIDs(patient, other, storeParam.isDeIdentifyLogs());
                unlinkPatientIDs(other, patient, storeParam.isDeIdentifyLogs());
            }

            patient.getPatientIDs().clear();

            Collection<PatientID> newPatientIDs = createPatientIDs(otherPids,
                    patient, storeParam.isDeIdentifyLogs());
            for (PatientID id : newPatientIDs) {
                patient.getPatientIDs().add(id);
            }
        } catch (Exception e) {
            em.clear();
            LOG.info("failed to update ID {} with {} ", pids, otherPids);
        }

        // set blob data
        Attributes patientAttrs = patient.getAttributes();
        AttributeFilter filter = storeParam.getAttributeFilter(Entity.Patient);
        if (Utils.updateAttributes(patientAttrs, otherPatientAttrs, null, filter, MetadataUpdateStrategy.COERCE_MERGE)) {
            patient.setAttributes(patientAttrs, filter, storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
            em.flush();
            LOG.info("Update ID {} with {} ", pids, otherPids);
        }
    }
}
