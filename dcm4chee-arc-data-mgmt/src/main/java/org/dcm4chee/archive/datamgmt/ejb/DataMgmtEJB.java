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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4chee.archive.datamgmt.ejb;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.FileRef;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.entity.PersonName;
import org.dcm4chee.archive.entity.RequestAttributes;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.VerifyingObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@Stateless
public class DataMgmtEJB implements DataMgmtBean {

    private static final Logger LOG = LoggerFactory
            .getLogger(DataMgmtEJB.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    public Study deleteStudy(String studyInstanceUID) {
        TypedQuery<Study> query = em.createNamedQuery(
                Study.FIND_BY_STUDY_INSTANCE_UID, Study.class).setParameter(1,
                studyInstanceUID);
        Study study = query.getSingleResult();
        em.remove(study);
        LOG.info("Removed study entity - " + studyInstanceUID);
        return study;
    }

    @Override
    public Series deleteSeries(String seriesInstanceUID) {
        TypedQuery<Series> query = em.createNamedQuery(
                Series.FIND_BY_SERIES_INSTANCE_UID, Series.class).setParameter(
                1, seriesInstanceUID);
        Series series = query.getSingleResult();
        Study study = series.getStudy();
        int numInstancesNew = study.getNumberOfInstances()
                - series.getNumberOfInstances();
        em.remove(series);
        if (numInstancesNew > 0)
            study.setNumberOfInstances(numInstancesNew);
        else
            study.resetNumberOfInstances();

        study.setNumberOfSeries(study.getNumberOfSeries() - 1);
        LOG.info("Removed series entity - " + seriesInstanceUID);
        return series;
    }

    @Override
    public Instance deleteInstance(String sopInstanceUID) throws Exception {
        TypedQuery<Instance> query = em.createNamedQuery(
                Instance.FIND_BY_SOP_INSTANCE_UID, Instance.class)
                .setParameter(1, sopInstanceUID);
        Instance inst = query.getSingleResult();
        Series series = inst.getSeries();
        Study study = series.getStudy();

        int numInstancesNew = series.getNumberOfInstances() - 1;
        int numInstancesNewStudy = study.getNumberOfInstances() - 1;
        em.remove(inst);
        LOG.info("Removed instance entity - " + sopInstanceUID);
        if (numInstancesNew > 0)
            series.setNumberOfInstances(numInstancesNew);
        else
            series.resetNumberOfInstances();
        if (numInstancesNewStudy > 0)
            study.setNumberOfInstances(numInstancesNewStudy);
        else
            study.resetNumberOfInstances();
        List<FileRef> fileRef = (List<FileRef>) inst.getFileRefs();
        for (FileRef file : fileRef) {

            try {
                LOG.info("Deleted file: " + file.getFilePath());
                if (!new File(file.getFileSystem().getPath().toFile(),
                        file.getFilePath()).delete())
                    throw new Exception();
            } catch (NoSuchFileException e) {
                LOG.error("No such file or directory\n"
                        + e.getStackTrace().toString());
            } catch (IOException e1) {
                LOG.error("No sufficient permissions to delete file\n"
                        + e1.getStackTrace().toString());
            }
        }
        return inst;
    }

    @Override
    public boolean deleteSeriesIfEmpty(String seriesInstanceUID,
            String studyInstanceUID) {
        TypedQuery<Series> query = em.createNamedQuery(
                Series.FIND_BY_SERIES_INSTANCE_UID, Series.class).setParameter(
                1, seriesInstanceUID);
        Series series = query.getSingleResult();

        if (series.getNumberOfInstances() == -1) {
            em.remove(series);
            LOG.info("Removed series entity - " + seriesInstanceUID);
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteStudyIfEmpty(String studyInstanceUID) {
        TypedQuery<Study> query = em.createNamedQuery(
                Study.FIND_BY_STUDY_INSTANCE_UID, Study.class).setParameter(1,
                studyInstanceUID);
        Study study = query.getSingleResult();

        if (study.getNumberOfInstances() == -1) {
            em.remove(study);
            LOG.info("Removed study entity - " + studyInstanceUID);
            return true;
        }

        return false;
    }

    @Override
    public Study getStudy(String studyInstanceUID) {
        return em.find(Study.class, getStudyPK(studyInstanceUID));
    }

    @Override
    public void updatePatient(ArchiveDeviceExtension arcDevExt,
            IDWithIssuer patientID, Attributes attrs) throws EntityNotFoundException {
        Patient patient = getPatient(patientID);
        if (patient == null)
            throw new EntityNotFoundException("Unable to find patient "
                    + patientID);

        Attributes original = patient.getAttributes();

        // relations
        // patient if (add Only)
        if (attrs.contains(Tag.PatientID)
                || attrs.contains(Tag.OtherPatientIDs)
                || attrs.contains(Tag.OtherPatientIDsSequence)) {
            Collection<PatientID> patientIDs = getPatientIDs(patient, original,
                    attrs, arcDevExt);
            if (patientIDs != null && patientIDs.size() > 0) {
                patient.setPatientIDs(patientIDs);
            }
        }
        // name
        if (attrs.contains(Tag.PatientName)) {
            PersonName name = PersonName.valueOf(
                    attrs.getString(Tag.PatientName), arcDevExt.getFuzzyStr(),
                    null);
            if (name != null)
                em.persist(name);
            patient.setPatientName(name);
        }

        if (attrs.contains(Tag.SeriesInstanceUID)
                || attrs.contains(Tag.SeriesInstanceUID)
                || attrs.contains(Tag.StudyInstanceUID)) {
            // no function
            String ignoredAttr = attrs.contains(Tag.StudyInstanceUID) ? "StudyInstanceUID"
                    : attrs.contains(Tag.SeriesInstanceUID) ? "SeriesInstanceUID"
                            : "";
            if (LOG.isDebugEnabled()) {
                LOG.debug("Illegal attributes to modify, Ignoring illegal attributes "
                        + ignoredAttr);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attributes modified:\n" + attrs.toString());
        }
        original.update(attrs, original);
        patient.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Patient),
                arcDevExt.getFuzzyStr());
        em.flush();
    }

    @Override
    public void updateStudy(ArchiveDeviceExtension arcDevExt,
            String studyInstanceUID, Attributes attrs)
            throws EntityNotFoundException {
        Study study = getStudy(studyInstanceUID);
        if (study == null)
            throw new EntityNotFoundException("Unable to find study "
                    + studyInstanceUID);
        Attributes original = study.getAttributes();

        // relations
        if (attrs.contains(Tag.ProcedureCodeSequence)) {
            Collection<Code> procedureCodes = getProcedureCodes(attrs);
            if (procedureCodes != null && procedureCodes.size() > 0) {
                study.setProcedureCodes(procedureCodes);
            }
        }
        // one item only
        if (attrs.contains(Tag.IssuerOfAccessionNumberSequence)) {
            Issuer issuerOfAccessionNumber = getIssuerOfAccessionNumber(attrs);
            if (issuerOfAccessionNumber != null) {
                study.setIssuerOfAccessionNumber(issuerOfAccessionNumber);
            }
        }
        if (attrs.contains(Tag.PatientID)
                || attrs.contains(Tag.OtherPatientIDs)
                || attrs.contains(Tag.OtherPatientIDsSequence)
                || attrs.contains(Tag.SeriesInstanceUID)) {
            // no function
            String ignoredAttr = attrs.contains(Tag.PatientID) ? "PatientID"
                    : attrs.contains(Tag.OtherPatientIDs) ? "OtherPatientIDs"
                            : attrs.contains(Tag.OtherPatientIDsSequence) ? "OtherPatientIDsSequence"
                                    : attrs.contains(Tag.SeriesInstanceUID) ? "SeriesInstanceUID"
                                            : "";
            if (LOG.isDebugEnabled()) {
                LOG.debug("Illegal attributes to modify, Ignoring illegal attributes "
                        + ignoredAttr);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attributes modified:\n" + attrs.toString());
        }
        original.update(attrs, original);
        study.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Study),
                arcDevExt.getFuzzyStr());
        em.flush();
    }

    @Override
    public void updateSeries(ArchiveDeviceExtension arcDevExt,
            String studyInstanceUID, String seriesInstanceUID, Attributes attrs)
            throws EntityNotFoundException {

        Study study = getStudy(studyInstanceUID);
        if (study == null)
            throw new EntityNotFoundException("Unable to find study "
                    + studyInstanceUID);

        Series series = getSeries(study, seriesInstanceUID);

        if (series == null)
            throw new EntityNotFoundException("Unable to find series "
                    + seriesInstanceUID);
        Attributes original = series.getAttributes();
        // relations
        // institutionCode
        if (attrs.contains(Tag.InstitutionCodeSequence)) {
            if (original.getSequence(Tag.InstitutionCodeSequence) != null
                    && !attrs.getSequence(Tag.InstitutionCodeSequence).equals(
                            original.getSequence(Tag.InstitutionCodeSequence))) {
                Code institutionCode = getCode(attrs,
                        Tag.InstitutionCodeSequence);
                if (institutionCode != null) {
                    series.setInstitutionCode(institutionCode);
                }
            }
        }
        // Requested Procedure Step
        if (attrs.contains(Tag.RequestAttributesSequence)) {

            Collection<RequestAttributes> requestAttrs = getRequestAttributes(
                    series, attrs, arcDevExt, original);
            if (requestAttrs != null) {
                series.setRequestAttributes(requestAttrs);
            }
        }

        if (attrs.contains(Tag.PatientID)
                || attrs.contains(Tag.OtherPatientIDs)
                || attrs.contains(Tag.OtherPatientIDsSequence)
                || attrs.contains(Tag.SOPInstanceUID)
                || attrs.contains(Tag.StudyInstanceUID)) {
            // no function
            String ignoredAttr = attrs.contains(Tag.PatientID) ? "PatientID"
                    : attrs.contains(Tag.OtherPatientIDs) ? "OtherPatientIDs"
                            : attrs.contains(Tag.OtherPatientIDsSequence) ? "OtherPatientIDsSequence"
                                    : attrs.contains(Tag.StudyInstanceUID) ? "StudyInstanceUID"
                                            : attrs.contains(Tag.SOPInstanceUID) ? "SOPInstanceUID"
                                                    : "";
            if (LOG.isDebugEnabled()) {
                LOG.debug("Illegal attributes to modify, Ignoring illegal attributes "
                        + ignoredAttr);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attributes modified:\n" + attrs.toString());
        }

        original.update(attrs, original);
        series.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Series),
                arcDevExt.getFuzzyStr());

        em.flush();
    }

    @Override
    public void updateInstance(ArchiveDeviceExtension arcDevExt,
            String studyInstanceUID, String seriesInstanceUID,
            String sopInstanceUID, Attributes attrs)
            throws EntityNotFoundException {

        Study study = getStudy(studyInstanceUID);
        if (study == null)
            throw new EntityNotFoundException("Unable to find study "
                    + studyInstanceUID);

        Series series = getSeries(study, seriesInstanceUID);

        if (series == null)
            throw new EntityNotFoundException("Unable to find series "
                    + seriesInstanceUID);

        Instance instance = getInstance(series, sopInstanceUID);

        if (instance == null)
            throw new EntityNotFoundException("Unable to find instance "
                    + sopInstanceUID);

        Attributes original = instance.getAttributes();

        // relations
        // Concept name Code Sequence on root level (SR)
        if (attrs.contains(Tag.ConceptNameCodeSequence)) {
            Code conceptNameCode = getCode(attrs, Tag.ConceptNameCodeSequence);
            if (conceptNameCode != null) {
                instance.setConceptNameCode(conceptNameCode);
            }

        }

        // verifying observers
        if (attrs.contains(Tag.VerifyingObserverSequence)) {
            Collection<VerifyingObserver> newObservers = getVerifyingObservers(
                    instance, attrs, original, arcDevExt);
            if (newObservers != null) {
                instance.setVerifyingObservers(newObservers);
            }
        }

        // ignored
        if (attrs.contains(Tag.PatientID)
                || attrs.contains(Tag.OtherPatientIDs)
                || attrs.contains(Tag.OtherPatientIDsSequence)
                || attrs.contains(Tag.SOPInstanceUID)
                || attrs.contains(Tag.StudyInstanceUID)
                || attrs.contains(Tag.SeriesInstanceUID)) {
            // no function
            String ignoredAttr = attrs.contains(Tag.PatientID) ? "PatientID"
                    : attrs.contains(Tag.OtherPatientIDs) ? "OtherPatientIDs"
                            : attrs.contains(Tag.OtherPatientIDsSequence) ? "OtherPatientIDsSequence"
                                    : attrs.contains(Tag.StudyInstanceUID) ? "StudyInstanceUID"
                                            : attrs.contains(Tag.SeriesInstanceUID) ? "SeriesInstanceUID"
                                                    : attrs.contains(Tag.SOPInstanceUID) ? "SOPInstanceUID"
                                                            : "";
            if (LOG.isDebugEnabled()) {
                LOG.debug("Illegal attributes to modify, Ignoring illegal attributes "
                        + ignoredAttr);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attributes modified:\n" + attrs.toString());
        }

        original.update(attrs, original);
        instance.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Instance),
                arcDevExt.getFuzzyStr());

        em.flush();

    }

    private Collection<PatientID> getPatientIDs(Patient patient,
            Attributes original, Attributes attrs,
            ArchiveDeviceExtension arcDevExt) {
        Collection<PatientID> oldIDs = patient.getPatientIDs();
        Sequence newOtherPatientIDsSeq = attrs
                .getSequence(Tag.OtherPatientIDsSequence);
        // used in case the item in the OtherPatientIDsSequence has no issuer
        Issuer fallbackIssuer = null;
        // add missing ones
        if (attrs.contains(Tag.PatientID)) {
            Issuer issuer = getIssuer(attrs, original);
            fallbackIssuer = issuer;
            PatientID id = getPatientID(attrs.getString(Tag.PatientID), issuer);
            id.setPatient(patient);
            if (!oldIDs.contains(id))
                oldIDs.add(id);
        }
        if (attrs.contains(Tag.OtherPatientIDs)) {
            Issuer issuer = getIssuer(attrs, original);
            fallbackIssuer = issuer;
            PatientID id = getPatientID(attrs.getString(Tag.OtherPatientIDs),
                    issuer);
            if (!oldIDs.contains(id))
                oldIDs.add(id);
        }
        if(newOtherPatientIDsSeq!=null)
        for (Attributes otherPatientIDAttrs : newOtherPatientIDsSeq) {

            Issuer issuer = null;
            if (otherPatientIDAttrs.contains(Tag.IssuerOfPatientID)
                    || otherPatientIDAttrs
                            .contains(Tag.IssuerOfPatientIDQualifiersSequence)) {
                String local, universal = null, universalType = null;
                if (otherPatientIDAttrs
                        .getSequence(Tag.IssuerOfPatientIDQualifiersSequence) != null
                        && otherPatientIDAttrs.getSequence(
                                Tag.IssuerOfPatientIDQualifiersSequence).size() > 0) {
                    Sequence tmp = otherPatientIDAttrs
                            .getSequence(Tag.IssuerOfPatientIDQualifiersSequence);
                    universal = tmp.get(0).getString(Tag.UniversalEntityID);
                    universalType = tmp.get(0).getString(
                            Tag.UniversalEntityIDType);
                }
                local = otherPatientIDAttrs.getString(Tag.IssuerOfPatientID);
                issuer = getIssuer(local, universal, universalType);

            }
            if (issuer == null)
                issuer = fallbackIssuer;
            PatientID id = getPatientID(
                    otherPatientIDAttrs.getString(Tag.PatientID), issuer);
            if (!oldIDs.contains(id))
                oldIDs.add(id);
        }

        return oldIDs;
    }

    private Issuer getIssuer(Attributes attrs, Attributes original) {
        Issuer issuer = null;
        if (attrs.contains(Tag.IssuerOfPatientID)
                || attrs.contains(Tag.IssuerOfPatientIDQualifiersSequence)) {
            String local, universal = null, universalType = null;
            if (attrs.getSequence(Tag.IssuerOfPatientIDQualifiersSequence) != null
                    && attrs.getSequence(
                            Tag.IssuerOfPatientIDQualifiersSequence).size() > 0) {
                Sequence tmp = attrs
                        .getSequence(Tag.IssuerOfPatientIDQualifiersSequence);
                universal = tmp.get(0).getString(Tag.UniversalEntityID);
                universalType = tmp.get(0).getString(Tag.UniversalEntityIDType);
            }
            local = attrs.getString(Tag.IssuerOfPatientID);
            issuer = getIssuer(local, universal, universalType);

        } else if (original.contains(Tag.IssuerOfPatientID)) {
            String local, universal = null, universalType = null;
            if (original.getSequence(Tag.IssuerOfPatientIDQualifiersSequence) != null
                    && original.getSequence(
                            Tag.IssuerOfPatientIDQualifiersSequence).size() > 0) {
                Sequence tmp = original
                        .getSequence(Tag.IssuerOfPatientIDQualifiersSequence);
                universal = tmp.get(0).getString(Tag.UniversalEntityID);
                universalType = tmp.get(0).getString(Tag.UniversalEntityIDType);
            }
            local = original.getString(Tag.IssuerOfPatientID);
            issuer = getIssuer(local, universal, universalType);

        }
        return issuer;
    }

    private PatientID getPatientID(String string, Issuer issuer) {
        Query query = em
                .createQuery("Select pid from PatientID pid where (pid.id = ?1 AND pid.issuer = ?2) OR pid.id=?1");
        query.setParameter(1, string);
        query.setParameter(2, issuer);
        PatientID foundID = null;
        try {
            foundID = (PatientID) query.getSingleResult();
        } catch (Exception e) {
        }
        if (foundID == null) {
            foundID = new PatientID();
            foundID.setID(string);
            foundID.setIssuer(issuer);
            em.persist(foundID);
        }
        foundID.setIssuer(issuer);
        return foundID;
    }

    private Patient getPatient(IDWithIssuer patientID) {
        return em.find(Patient.class, getPatientPK(patientID));
    }

    private Collection<VerifyingObserver> getVerifyingObservers(
            Instance instance, Attributes attrs, Attributes original,
            ArchiveDeviceExtension arcDevExt) {
        Collection<VerifyingObserver> oldObservers = instance
                .getVerifyingObservers();
        Sequence verifyingObserversOld = original
                .getSequence(Tag.VerifyingObserverSequence);
        Sequence verifyingObserversNew = attrs
                .getSequence(Tag.VerifyingObserverSequence);
        // remove deprecated observers
        if (verifyingObserversOld != null)
            for (Attributes observer : verifyingObserversOld) {
                if (!verifyingObserversNew.contains(observer)) {
                    VerifyingObserver tmp = getVerifyingObserver(observer,
                            instance, arcDevExt);
                    oldObservers.remove(tmp);
                }
            }
        // add missing ones
        for (Attributes observer : verifyingObserversNew) {
            if (verifyingObserversOld == null
                    || (verifyingObserversOld != null && !verifyingObserversOld
                            .contains(observer))) {
                PersonName verifyingObserverName = PersonName.valueOf(
                        observer.getString(Tag.VerifyingObserverName),
                        arcDevExt.getFuzzyStr(), null);
                if (verifyingObserverName != null)
                    em.persist(verifyingObserverName);
                VerifyingObserver newObserver = new VerifyingObserver(observer,
                        arcDevExt.getFuzzyStr());
                oldObservers.add(newObserver);
            }
        }
        return oldObservers;
    }

    private VerifyingObserver getVerifyingObserver(Attributes observerAttrs,
            Instance instance, ArchiveDeviceExtension arcDevExt) {

        VerifyingObserver tmp = em.find(VerifyingObserver.class,
                getObserverPK(observerAttrs, arcDevExt.getFuzzyStr()));
        if (tmp == null) {
            tmp = new VerifyingObserver(observerAttrs, arcDevExt.getFuzzyStr());
        }

        return tmp;
    }

    private long getObserverPK(Attributes observerAttrs, FuzzyStr str) {
        Query query = em.createQuery("Select o from VerifyingObserver o "
                + "where o.verifyingObserverName = ?1");
        query.setParameter(1, PersonName.valueOf(
                observerAttrs.getString(Tag.VerifyingObserverName), str, null));
        VerifyingObserver foundObserver = null;
        try {
            foundObserver = (VerifyingObserver) query.getSingleResult();
        } catch (Exception e) {
            // do nothing
        }
        return foundObserver != null ? foundObserver.getPk() : -1l;
    }

    private Collection<RequestAttributes> getRequestAttributes(Series series,
            Attributes attrs, ArchiveDeviceExtension arcDevExt,
            Attributes original) {

        Collection<RequestAttributes> oldRequests = series
                .getRequestAttributes();
        Sequence oldSequence = original
                .getSequence(Tag.RequestAttributesSequence);
        Sequence updateSequence = attrs
                .getSequence(Tag.RequestAttributesSequence);
        // remove deprecated items
        if (oldSequence != null)
            for (Attributes oldItem : oldSequence) {
                if (!updateSequence.contains(oldItem)) {
                    RequestAttributes tmp = getRequestAttr(oldItem, series);
                    oldRequests.remove(tmp);
                }

            }
        // add missing ones
        for (Attributes request : updateSequence) {
            if (oldSequence == null
                    || (oldSequence != null && !oldSequence.contains(request))) {
                Issuer issuerOfAccessionNumber = getIssuerOfAccessionNumber(request);
                PersonName requestingPhysician = PersonName.valueOf(
                        request.getString(Tag.RequestingPhysician),
                        arcDevExt.getFuzzyStr(), null);
                if (requestingPhysician != null)
                    em.persist(requestingPhysician);
                RequestAttributes newRequest = new RequestAttributes(request,
                        issuerOfAccessionNumber, arcDevExt.getFuzzyStr());
                oldRequests.add(newRequest);
            }
        }
        return oldRequests;
    }

    private RequestAttributes getRequestAttr(Attributes attrs, Series series) {
        Query query = em.createQuery("SELECT r FROM RequestAttributes r "
                + "WHERE r.studyInstanceUID = ?1  and "
                + "r.scheduledProcedureStepID = ?2 and "
                + "r.requestedProcedureID = ?3 and " + "r.series = ?4");
        query.setParameter(1, attrs.getString(Tag.StudyInstanceUID));
        query.setParameter(2, attrs.getString(Tag.ScheduledProcedureStepID));
        query.setParameter(3, attrs.getString(Tag.RequestedProcedureID));
        query.setParameter(4, series);
        RequestAttributes request = (RequestAttributes) query.getSingleResult();
        return request;
    }

    private Code getCode(Attributes attrs, int codeSeqTag) {
        Sequence seq = attrs.getSequence(codeSeqTag);
        Attributes codeAttrs = seq.get(0);
        String meaning = codeAttrs.getString(Tag.CodeMeaning);
        String value = codeAttrs.getString(Tag.CodeValue);
        String designator = codeAttrs.getString(Tag.CodingSchemeDesignator);
        String version = codeAttrs.getString(Tag.CodingSchemeVersion);
        Code code = em.find(Code.class, getCodePK(value, designator));
        if (code == null) {
            code = new Code(value, designator, version, meaning);
            em.persist(code);
        }
        return code;
    }

    private long getCodePK(String value, String designator) {
        Query query = em
                .createNamedQuery(Code.FIND_BY_CODE_VALUE_WITHOUT_SCHEME_VERSION);
        query.setParameter(1, value);
        query.setParameter(2, designator);
        Code foundCode = null;
        try {
            foundCode = (Code) query.getSingleResult();
        } catch (Exception e) {
            // do nothing
        }

        return foundCode != null ? foundCode.getPk() : -1l;
    }

    private Object getIssuerPK(String local, String universal,
            String universalType) {
        Query query = em.createNamedQuery(Issuer.FIND_BY_ENTITY_ID_OR_UID);
        query.setParameter(1, local);
        query.setParameter(2, universal);
        query.setParameter(3, universalType);
        Issuer foundIssuer = null;
        try {
            foundIssuer = (Issuer) query.getSingleResult();
        } catch (Exception e) {
        }
        return foundIssuer != null ? foundIssuer.getPk() : -1l;
    }

    private Issuer getIssuerOfAccessionNumber(Attributes attrs) {

        if (!attrs.contains(Tag.IssuerOfAccessionNumberSequence))
            return null;
        Attributes issuerAttrs = attrs.getSequence(
                Tag.IssuerOfAccessionNumberSequence).get(0);
        String local = issuerAttrs.getString(Tag.LocalNamespaceEntityID);
        String universal = issuerAttrs.getString(Tag.UniversalEntityID);
        String universalType = issuerAttrs.getString(Tag.UniversalEntityIDType);
        Issuer issuer = em.find(Issuer.class,
                getIssuerPK(local, universal, universalType));
        if (issuer == null) {
            issuer = new Issuer(local, universal, universalType);
            em.persist(issuer);
        }

        return issuer;
    }

    public Issuer getIssuer(String local, String universal,
            String universalType) {
        Issuer issuer = em.find(Issuer.class,
                getIssuerPK(local, universal, universalType));
        if (issuer == null) {
            issuer = new Issuer(local, universal, universalType);
            em.persist(issuer);
        }

        return issuer;
    }

    private Collection<Code> getProcedureCodes(Attributes attrs) {

        ArrayList<Code> resultCode = new ArrayList<Code>();
        for (Attributes codeAttrs : attrs
                .getSequence(Tag.ProcedureCodeSequence)) {
            String meaning = codeAttrs.getString(Tag.CodeMeaning);
            String value = codeAttrs.getString(Tag.CodeValue);
            String designator = codeAttrs.getString(Tag.CodingSchemeDesignator);
            String version = codeAttrs.getString(Tag.CodingSchemeVersion);
            Code code = em.find(Code.class, getCodePK(value, designator));
            if (code == null) {
                code = new Code(value, designator, version, meaning);
                em.persist(code);
            }
            resultCode.add(code);
        }
        return resultCode;

    }

    private Series getSeries(Study study, String seriesInstanceUID) {
        for (Series series : study.getSeries()) {
            if (series.getSeriesInstanceUID().compareTo(seriesInstanceUID) == 0)
                return series;
        }
        return null;
    }

    private Instance getInstance(Series series, String sopInstanceUID) {
        for (Instance instance : series.getInstances()) {
            if (instance.getSopInstanceUID().compareTo(sopInstanceUID) == 0)
                return instance;
        }
        return null;
    }

    private long getInstancePK(String sopInstanceUID) {
        Instance inst = null;
        try {
            inst = (Instance) em
                    .createQuery(
                            "SELECT i FROM Instance i "
                                    + "WHERE i.sopInstanceUID = ?1 ")
                    .setParameter(1, sopInstanceUID).getSingleResult();
        } catch (Exception e) {
        }
        return inst != null ? inst.getPk() : -1l;
    }

    private long getStudyPK(String studyInstanceUID) {
        Study study = null;
        try {
            study = (Study) em
                    .createQuery(
                            "SELECT i FROM Study i "
                                    + "WHERE i.studyInstanceUID = ?1 ")
                    .setParameter(1, studyInstanceUID).getSingleResult();
        } catch (Exception e) {
        }

        return study != null ? study.getPk() : -1l;
    }

    private long getSeriesPK(String seriesInstanceUID) {
        Series series = null;
        try {
            series = (Series) em
                    .createQuery(
                            "SELECT i FROM Series i "
                                    + "WHERE i.seriesInstanceUID = ?1 ")
                    .setParameter(1, seriesInstanceUID).getSingleResult();
        } catch (Exception e) {
        }
        return series != null ? series.getPk() : -1l;
    }

    private long getPatientPK(IDWithIssuer patientID) {
        Patient patient = null;
        if(patientID.getIssuer()!=null){
        try {
             PatientID id = getPatientID(patientID.getID(),(Issuer)patientID.getIssuer());
             patient = id.getPatient();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        }
        return patient != null ? patient.getPk() : -1l;
    }

    @Override
    public boolean moveStudy(String studyInstanceUID, IDWithIssuer id) {

        boolean moved=false;
        Study study = getStudy(studyInstanceUID);

        if(study == null)
            return false;

        Patient previous = study.getPatient();
        Patient current = getPatient(id);
        if(previous == current)
        return false;
        
        Collection<Study> currentPatientStudies = current.getStudies();
        Iterator<Study> previousPatientStudies = previous.getStudies().iterator();
        while(previousPatientStudies.hasNext())
        {
            Study currentStudy = previousPatientStudies.next();
            if(currentStudy.equals(study))
            {
                previousPatientStudies.remove();
                currentStudy.setPatient(current);
                currentPatientStudies.add(currentStudy);
                moved = true;
            }
            
        }

        em.flush();
        return moved;
    }

    @Override
    public boolean splitStudy(String studyInstanceUID,
            String seriesInstanceUID, String targetStudyInstanceUID) {
        boolean split = false;
        Study study = getStudy(studyInstanceUID);
        if(study == null)
            return false;
        
        Study targetStudy = getStudy(targetStudyInstanceUID);
        if(targetStudy == null)
            return false;
        
        if(targetStudy == study)
            return false;
        
        Series series = getSeries(study, seriesInstanceUID);
        if(series == null)
            return false;
        
        Collection<Series> targetStudySeries = targetStudy.getSeries();
        
        Iterator<Series> currentStudySeries = study.getSeries().iterator();
        
        while(currentStudySeries.hasNext())
        {
            Series current = currentStudySeries.next();
            if(current == series)
            {
                currentStudySeries.remove();
                series.setStudy(targetStudy);
                targetStudySeries.add(series);
                split = true;
            }
        }
        return split;
    }

    @Override
    public boolean splitSeries(String studyInstanceUID,
            String seriesInstanceUID, String sopInstanceUID,
            String targetStudyInstanceUID, String targetSeriesInstanceUID) {
        boolean split = false;
        Study study = getStudy(studyInstanceUID);
        if(study == null)
            return false;
        
        Study targetStudy = getStudy(targetStudyInstanceUID);
        if(targetStudy == null)
            return false;
        
        if(targetStudy == study)
            return false;
        
        Series series = getSeries(study, seriesInstanceUID);
        if(series == null)
            return false;
        
        Series targetSeries = getSeries(targetStudy, targetSeriesInstanceUID);
        if(targetSeries == null)
            return false;
        
        if(targetSeries == series)
            return false;
        
        Instance instance = getInstance(series, sopInstanceUID);
        
        if(instance == null)
            return false;
        
        
        Collection<Instance> targetSeriesInstances = targetSeries.getInstances();
        
        Iterator<Instance> currentSeriesInstances = series.getInstances().iterator();
        
        while(currentSeriesInstances.hasNext())
        {
            Instance current = currentSeriesInstances.next();
            if(current == instance)
            {
                currentSeriesInstances.remove();
                instance.setSeries(targetSeries);
                targetSeriesInstances.add(instance);
                split = true;
            }
        }
        return split;
    }

}
/*
 * 
 * private Collection<ContentItem> getContentItems(Instance instance, Attributes
 * attrs, ArchiveDeviceExtension arcDevExt, Attributes original) {
 * 
 * Collection<ContentItem> oldContentItems = instance.getContentItems();
 * 
 * Sequence oldContentSequence = original.getSequence(Tag.ContentSequence);
 * 
 * Sequence newContentSequence = attrs.getSequence(Tag.ContentSequence);
 * 
 * //remove deprecated items if(oldContentSequence!=null) { for(Attributes
 * oldcontentItemAttr: oldContentSequence) {
 * if(!newContentSequence.contains(oldcontentItemAttr)) { ContentItem tmp =
 * getContentItem(oldContentItems,oldcontentItemAttr); if(tmp!=null)
 * oldContentItems.remove(tmp); } } } for(Attributes newItem:
 * newContentSequence){ if(oldContentSequence==null || (oldContentSequence!=null
 * && !oldContentSequence.contains(newItem))) { String relationShipType =
 * newItem.getString(Tag.RelationshipType); String valueType =
 * newItem.getString(Tag.ValueType); Code conceptNameCode = getCode(newItem,
 * Tag.ConceptNameCodeSequence); Code conceptCode = getCode(newItem,
 * Tag.ConceptCodeSequence);
 * 
 * 
 * } } }
 * 
 * private ContentItem getContentItem(Collection<ContentItem> oldContentItems,
 * Attributes oldcontentItemAttr) { for(ContentItem item:oldContentItems) {
 * if(item.getRelationshipType().compareTo(oldcontentItemAttr.getString(Tag.
 * RelationshipType))==0) { Sequence oldConceptNameCode =
 * oldcontentItemAttr.getSequence(Tag.ConceptNameCodeSequence); String
 * oldConceptNameCodeValue = oldConceptNameCode.get(0).getString(Tag.CodeValue);
 * String oldConceptNameCodeDesignator =
 * oldConceptNameCode.get(0).getString(Tag.CodingSchemeDesignator);
 * if(item.getConceptName().getCodeValue().compareTo(oldConceptNameCodeValue)==0
 * && item.getConceptName().getCodingSchemeDesignator().compareTo(
 * oldConceptNameCodeDesignator)==0) { Sequence oldConceptCode =
 * oldcontentItemAttr.getSequence(Tag.ConceptCodeSequence); String
 * oldConceptCodeValue = oldConceptCode.get(0).getString(Tag.CodeValue); String
 * oldConceptCodeDesignator =
 * oldConceptCode.get(0).getString(Tag.CodingSchemeDesignator);
 * if(item.getConceptCode().getCodeValue().compareTo(oldConceptCodeValue)==0 &&
 * item
 * .getConceptCode().getCodingSchemeDesignator().compareTo(oldConceptCodeDesignator
 * )==0) { return item; } } } } return null; }
 */