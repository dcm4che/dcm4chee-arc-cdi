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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.MWLItem;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.entity.QIssuer;
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.QPatientID;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.issuer.IssuerService;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
import org.dcm4chee.archive.patient.PatientMergedException;
import org.dcm4chee.archive.patient.PatientSelector;
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

    private static Logger LOG = LoggerFactory.getLogger(PatientServiceEJB.class);

    @PersistenceContext(unitName="dcm4chee-arc")
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
        Collection<IDWithIssuer> pids = IDWithIssuer.pidsOf(attrs);
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
        List<Patient> patients;
        String pname;
        if (!pids.isEmpty())
            patients = findPatientByIDs(pids);
        else if ((pname = attrs.getString(Tag.PatientName)) != null) 
            patients = findPatientByName(pname);
        else
            throw new NonUniquePatientException("No Patient ID and no Patient Name");
        
        return selector.select(patients, attrs, pids);
    }

    private List<Patient> findPatientByName(String pn) {
        return em.createNamedQuery(
                Patient.FIND_BY_PATIENT_NAME, Patient.class)
                .setParameter(1, new PersonName(pn, true)
                .toString(PersonName.Group.Alphabetic, false))
                .getResultList();
    }

    private List<Patient> findPatientByIDs(Collection<IDWithIssuer> pids) {
        BooleanBuilder builder = new BooleanBuilder();
        Collection<BooleanExpression> eqIDs = new ArrayList<BooleanExpression>(pids.size());
        for (IDWithIssuer pid : pids) {
            BooleanExpression eqID = QPatientID.patientID.id.eq(pid.getID());
            if (pid.getIssuer() == null) {
                builder.or(eqID);
            } else {
                builder.or(ExpressionUtils.and(eqID, eqIssuer(pid.getIssuer())));
                eqIDs.add(eqID);
            }
        }
        BooleanExpression matchingIDs = new HibernateSubQuery()
            .from(QPatientID.patientID)
            .leftJoin(QPatientID.patientID.issuer, QIssuer.issuer)
            .where(ExpressionUtils.and(
                    QPatientID.patientID.patient.eq(QPatient.patient),
                    builder))
            .exists();
        Session session = em.unwrap(Session.class);
        List<Patient> result = new HibernateQuery(session)
                .from(QPatient.patient)
                .where(matchingIDs)
                .list(QPatient.patient);
        if (result.isEmpty() && !eqIDs.isEmpty()) {
            result = new HibernateQuery(session)
                    .from(QPatient.patient)
                    .where(matchingIDsWithoutIssuer(eqIDs))
                    .list(QPatient.patient);
        }
        return result;
    }

    private BooleanExpression matchingIDsWithoutIssuer(
            Collection<BooleanExpression> eqIDs) {
        BooleanExpression noIssuer = QPatientID.patientID.issuer.isNull();
        BooleanBuilder builder = new BooleanBuilder();
        for (BooleanExpression eqID : eqIDs) {
            builder.or(ExpressionUtils.and(eqID, noIssuer));
        }
        return new HibernateSubQuery()
            .from(QPatientID.patientID)
            .where(ExpressionUtils.and(
                    QPatientID.patientID.patient.eq(QPatient.patient),
                    builder))
            .exists();
    }

    private Predicate eqIssuer(org.dcm4che3.data.Issuer issuer) {
        String id = issuer.getLocalNamespaceEntityID();
        String uid = issuer.getUniversalEntityID();
        String uidType = issuer.getUniversalEntityIDType();
        if (id == null)
            return eqUniversalEntityID(uid, uidType);
        if (uid == null)
            return eqLocalNamespaceEntityID(id);

        Predicate eqID = eqLocalNamespaceEntityID(id);
        Predicate eqUID = eqUniversalEntityID(uid, uidType);
        Predicate noID = QIssuer.issuer.localNamespaceEntityID.isNull();
        Predicate noUID = QIssuer.issuer.universalEntityID.isNull();
        return ExpressionUtils.and(
                QIssuer.issuer.isNotNull(),
                ExpressionUtils.and(
                    ExpressionUtils.or(eqID, noID),
                    ExpressionUtils.or(eqUID, noUID)));
    }

    private Predicate eqUniversalEntityID(String uid, String uidType) {
        return ExpressionUtils.and(
                QIssuer.issuer.universalEntityID.eq(uid),
                QIssuer.issuer.universalEntityIDType.eq(uidType));
    }

    private BooleanExpression eqLocalNamespaceEntityID(String id) {
        return QIssuer.issuer.localNamespaceEntityID.eq(id);
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

    private Patient createPatient(Collection<IDWithIssuer> pids,
            Attributes attrs, StoreParam storeParam) {
        Patient patient = new Patient();
        patient.setAttributes(attrs,
                storeParam.getAttributeFilter(Entity.Patient),
                storeParam.getFuzzyStr());
        patient.setPatientIDs(createPatientIDs(pids, patient));
        em.persist(patient);
        LOG.info("Create {}", patient);
        return patient;
    }

    private Collection<PatientID> createPatientIDs(
            Collection<IDWithIssuer> pids, Patient patient) {
        ArrayList<PatientID> patientIDs = new ArrayList<PatientID>(pids.size());
        for (IDWithIssuer pid : pids)
            patientIDs.add(createPatientID(pid, patient));
        return patientIDs;
    }

    private PatientID createPatientID(IDWithIssuer pid, Patient patient) {
        PatientID patientID = new PatientID();
        patientID.setID(pid.getID());
        if (pid.getIssuer() != null)
            pid.setIssuer(findOrCreateIssuer(pid));
        patientID.setPatient(patient);
        return patientID;
    }

    private Issuer findOrCreateIssuer(IDWithIssuer pid) {
        return issuerService.findOrCreate(
            new Issuer(pid.getIssuer()));
    }

    @Override
    public void updatePatientByCStore(Patient patient, Attributes attrs,
            StoreParam storeParam) {
        updatePatientByDICOM(patient, attrs, storeParam, IDWithIssuer.pidsOf(attrs));
    }

    private void updatePatientByDICOM(Patient patient, Attributes attrs,
            StoreParam storeParam, Collection<IDWithIssuer> pids) {
        boolean patientIDsMerged = mergePatientIDs(patient, pids);
        Attributes patientAttrs = patient.getAttributes();
        if (patientIDsMerged) {
            updatePatientIDsAttributes(patientAttrs, patient);
        }
        AttributeFilter filter = storeParam.getAttributeFilter(Entity.Patient);
        if (patientAttrs.mergeSelected(attrs, filter.getSelection()) || patientIDsMerged) {
            patient.setAttributes(patientAttrs, filter, storeParam.getFuzzyStr());
        }
    }

    private void updatePatientIDsAttributes(Attributes attrs, Patient patient) {
        IDWithIssuer pid0 = IDWithIssuer.pidOf(attrs);
        attrs.remove(Tag.IssuerOfPatientID);
        attrs.remove(Tag.IssuerOfPatientIDQualifiersSequence);
        attrs.remove(Tag.OtherPatientIDsSequence);
        Collection<PatientID> patientIDs = patient.getPatientIDs();
        if (patientIDs.isEmpty()) {
            attrs.setNull(Tag.PatientID, VR.LO);
            return;
        }
        int numopids = patientIDs.size() - 1;
        if (numopids == 0) {
            patientIDs.iterator().next().toIDWithIssuer()
                .exportPatientIDWithIssuer(attrs);
            return;
        }
        Sequence opidsSeq = attrs.newSequence(Tag.OtherPatientIDsSequence, numopids);
        for (PatientID patientID : patientIDs) {
            IDWithIssuer opid = patientID.toIDWithIssuer();
            if (opid.matches(pid0)) {
                opid.exportPatientIDWithIssuer(attrs);
                pid0 = null;
            } else {
                opidsSeq.add(opid.exportPatientIDWithIssuer(null));
            }
        }
    }

    private boolean mergePatientIDs(Patient patient,
            Collection<IDWithIssuer> pids) {
        int modCount = 0;
        Collection<PatientID> patientIDs = patient.getPatientIDs();
        for (IDWithIssuer pid : pids) {
            PatientID patientID = selectFrom(patientIDs, pid);
            if (patientID != null) {
                if (pid.getIssuer() == null)
                    continue;
                
                Issuer issuer = patientID.getIssuer();
                if (issuer != null) {
                    if (!issuer.equals(pid.getIssuer()))
                        continue;

                    issuer.merge(pid.getIssuer());
                } else {
                    patientID.setIssuer(findOrCreateIssuer(pid));
                }
            } else {
                patientIDs.add(createPatientID(pid, patient));
            }
            modCount++;
        }
        return modCount > 0;
    }

    private PatientID selectFrom(Collection<PatientID> patientIDs,
            IDWithIssuer pid) {
        for (PatientID patientID : patientIDs) {
            if (pid.getID().equals(patientID.getID())
                    && pid.getIssuer() == null
                    || pid.getIssuer().matches(patientID.getIssuer()))
                return patientID;
        }
        return null;
    }

    private void updatePatient(Patient patient, Attributes attrs,
            Collection<IDWithIssuer> pids, StoreParam storeParam, boolean overwriteValues) {
        //TODO update patient IDs
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
        Collection<IDWithIssuer> pids = IDWithIssuer.pidsOf(attrs);
        Patient patient = selector.select(findPatientByIDs(pids), attrs, pids);
        if (patient == null)
            createPatient(pids, attrs, storeParam);

        if (patient.getMergedWith() != null)
            throw new PatientMergedException(patient);

        updatePatient(patient, attrs, pids, storeParam, true);
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

    @Override
    public Patient deletePatient(IDWithIssuer pid) throws NonUniquePatientException {
        List<Patient> results = findPatientByIDs(Collections.singleton(pid));
        switch (results.size()) {
        case 0:
            return null;
        case 1:
            em.remove(results.get(0));
            return results.get(0);
        }
        throw new NonUniquePatientException("id=" + pid);
    }

}
