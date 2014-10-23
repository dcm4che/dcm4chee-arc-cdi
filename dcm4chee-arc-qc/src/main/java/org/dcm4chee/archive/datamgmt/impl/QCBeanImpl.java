package org.dcm4chee.archive.datamgmt.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.datamgmt.QCEvent;
import org.dcm4chee.archive.datamgmt.QCEvent.DataMgmtOperation;
import org.dcm4chee.archive.datamgmt.QCNotification;
import org.dcm4chee.archive.datamgmt.PatientCommands;
import org.dcm4chee.archive.datamgmt.QCBean;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.entity.RequestAttributes;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.VerifyingObserver;
import org.dcm4chee.archive.iocm.RejectionService;
import org.dcm4chee.archive.issuer.IssuerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class QCBeanImpl  implements QCBean{

    private static Logger LOG = LoggerFactory.getLogger(QCBeanImpl.class);

    @Inject
    private PatientService patientService;

    @Inject
    private CodeService codeService;

    @Inject
    private IssuerService issuerService;

    @Inject
    private RejectionService rejectionService;
    @Inject
    @QCNotification
    Event<QCEvent> internalNotification;

    @PersistenceContext(name="dcm4chee-arc")
    EntityManager em;

    @Override
    public void mergeStudies(String[] sourceStudyUids, String targetStudyUid, org.dcm4che3.data.Code qcRejectionCode) {
        // TODO Auto-generated method stub
        LOG.info("Performing QC Merge Studies");
    }

    @Override
    public void merge(String sourceStudyUid, String targetStudyUid,
            boolean samePatient, org.dcm4che3.data.Code qcRejectionCode) {
        // TODO Auto-generated method stub
        LOG.info("Performing QC Merge");
    }

    @Override
    public void split(Collection<Instance> toMove, Attributes createdStudy, org.dcm4che3.data.Code qcRejectionCode) {
        // TODO Auto-generated method stub
        LOG.info("Performing QC Split");
    }

    @Override
    public void segment(Collection<Instance> toMove,
            Collection<Instance> toClone, Attributes targetStudy, org.dcm4che3.data.Code qcRejectionCode) {
        // TODO Auto-generated method stub
        LOG.info("Performing QC Segment");
    }

    @Override
    public void segmentFrame(Instance toMove, Instance toClone, int frame,
            Attributes targetStudy) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void move(Instance source, Series target, Attributes targetInstance, org.dcm4che3.data.Code qcRejectionCode) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clone(Instance source, Series target, Attributes targetInstance) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reject(Collection<Instance> instances, org.dcm4che3.data.Code qcRejectionCode) {
        rejectionService.reject(this, instances, findOrCreateCode(qcRejectionCode), null);
    }

    @Override
    public void reject(String[] sopInstanceUIDs, org.dcm4che3.data.Code qcRejectionCode) {
        rejectionService.reject(this, locateInstances(sopInstanceUIDs), findOrCreateCode(qcRejectionCode), null);
    }

    @Override
    public void restore(Collection<Instance> instances) {
        rejectionService.restore(this, instances, null);
    }

    @Override
    public void restore(String[] sopInstanceUIDs) {
        rejectionService.restore(this, locateInstances(sopInstanceUIDs), null);
    }

//    @Override
//    public void recordHistoryEntry(QCActionHistory action,
//            Collection<QCStudyHistory> study,
//            Collection<QCSeriesHistory> series,
//            Collection<QCInstanceHistory> instance) {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public QCInstanceHistory findUIDChangesFromHistory(Instance instance) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public void removeHistoryEntry(QCActionHistory action) {
//        // TODO Auto-generated method stub
//        
//    }

    @Override
    public boolean canApplyQC(Instance instance) {
        return instance.getRejectionNoteCode()!=null? false : true;
    }

    @Override
    public void notify(QCEvent event) {
        internalNotification.fire(event);
    }

    @Override
    public void updateDicomObject(ArchiveDeviceExtension arcDevExt,
            String scope, Attributes attrs)
            throws EntityNotFoundException {
        
        LOG.info("Performing QC update DICOM header on {} scope : ", scope);
        switch(scope.toLowerCase()) {
        case "patient":updatePatient(arcDevExt, attrs);
            break;
        case "study":updateStudy(arcDevExt, attrs.getString(Tag.StudyInstanceUID), attrs);
            break;
        case "series":updateSeries(arcDevExt, attrs.getString(Tag.SeriesInstanceUID), attrs);
            break;
        case "instance":updateInstance(arcDevExt, attrs.getString(Tag.SOPInstanceUID), attrs);
            break;
        }
        QCEvent updateEvent = new QCEvent(DataMgmtOperation.UPDATE,scope,attrs, null, null);
        notify(updateEvent);
    }

    public boolean patientOperation(Attributes srcPatientAttrs, Attributes targetPatientAttrs, ArchiveAEExtension arcAEExt, PatientCommands command)
    {
        try {
            
            if(command == PatientCommands.PATIENT_UPDATE_ID)
                patientService.updatePatientID(srcPatientAttrs,targetPatientAttrs,arcAEExt.getStoreParam());
            if (command == PatientCommands.PATIENT_LINK)
                patientService.linkPatient(targetPatientAttrs,
                        srcPatientAttrs, arcAEExt.getStoreParam());
            else if (command == PatientCommands.PATIENT_UNLINK)
                patientService.unlinkPatient(targetPatientAttrs,
                        srcPatientAttrs, arcAEExt.getStoreParam());
            else if (command == PatientCommands.PATIENT_MERGE)
            {
                patientService.mergePatientByHL7(
                        targetPatientAttrs,
                        srcPatientAttrs, arcAEExt.getStoreParam());
            } 
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
//    @Override
//    public void undoLastAction(QCStudyHistory study) {
//        // TODO Auto-generated method stub
//        
//    }

    @Override
    public Collection<Instance> locateInstances(String[] sopInstanceUIDs) {
        ArrayList<Instance> list = new ArrayList<Instance>();
        for(String sopInstanceUID : sopInstanceUIDs) {
        Query query  = em.createNamedQuery(Instance.FIND_BY_SOP_INSTANCE_UID_EAGER);
        query.setParameter(1, sopInstanceUID);
        list.add((Instance)query.getSingleResult());
        }
        return list;
    }

    private void updatePatient(ArchiveDeviceExtension arcDevExt, Attributes attrs)
            throws EntityNotFoundException {
        Patient patient = getPatient(attrs);

        if (patient == null)
            throw new EntityNotFoundException("Unable to find patient or multiple patients found");

        Attributes original = patient.getAttributes();

        original.update(attrs, original);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attributes modified:\n" + attrs.toString());
        }
        patient.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Patient),
                arcDevExt.getFuzzyStr());
    }
    private void updateStudy(ArchiveDeviceExtension arcDevExt,
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
            if (!procedureCodes.isEmpty()) {
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("Attributes modified:\n" + attrs.toString());
        }
        original.update(attrs, original);
        study.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Study),
                arcDevExt.getFuzzyStr());
    }

    private void updateSeries(ArchiveDeviceExtension arcDevExt, 
            String seriesInstanceUID, Attributes attrs)
            throws EntityNotFoundException {

        Series series = getSeries(seriesInstanceUID);

        if (series == null)
            throw new EntityNotFoundException("Unable to find series "
                    + seriesInstanceUID);
        Attributes original = series.getAttributes();
        // relations
        // institutionCode
        if (attrs.contains(Tag.InstitutionCodeSequence) && (
                attrs.contains(Tag.InstitutionName) ||
                original.contains(Tag.InstitutionName) )) {
                Code institutionCode = getInstitutionalCode(attrs);
                if (institutionCode != null) {
                    series.setInstitutionCode(institutionCode);
                }
        }
        // Requested Procedure Step
        if (attrs.contains(Tag.RequestAttributesSequence)) {

            Collection<RequestAttributes> requestAttrs = getRequestAttributes(
                    series, attrs, arcDevExt, original);
                series.setRequestAttributes(requestAttrs);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Attributes modified:\n" + attrs.toString());
        }

        original.update(attrs, original);
        series.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Series),
                arcDevExt.getFuzzyStr());
    }

    private void updateInstance(ArchiveDeviceExtension arcDevExt,
            String sopInstanceUID, Attributes attrs)
            throws EntityNotFoundException {

        Instance instance = getInstance(sopInstanceUID);

        if (instance == null)
            throw new EntityNotFoundException("Unable to find instance "
                    + sopInstanceUID);

        Attributes original = instance.getAttributes();

        // relations
        // Concept name Code Sequence on root level (SR)
        if (attrs.contains(Tag.ConceptNameCodeSequence)) {
            Code conceptNameCode = getConceptNameCode(attrs);
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("Attributes modified:\n" + attrs.toString());
        }

        original.update(attrs, original);
        instance.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Instance),
                arcDevExt.getFuzzyStr());
    }

    private Patient getPatient(Attributes attrs) {
        Collection<IDWithIssuer> pids = IDWithIssuer.pidsOf(attrs);
        Collection<PatientID> queryIDs = findOrCreatePatientIDs(pids);

        if(queryIDs.isEmpty())
        return null;

        Iterator<PatientID> idIterator = queryIDs.iterator();
        Patient pat = idIterator.next().getPatient();
        while(idIterator.hasNext()) {
            if(idIterator.next().getPatient().getPk() != pat.getPk())
            {
                return null;
            }
        }
        
        return pat;
    }

    private Study getStudy(String studyInstanceUID) {
        Query query = em.createNamedQuery(Study.FIND_BY_STUDY_INSTANCE_UID);
        Study study = (Study) query.getSingleResult();
        return study;
    }

    private Series getSeries(String seriesInstanceUID) {
        Query query = em.createNamedQuery(Series.FIND_BY_SERIES_INSTANCE_UID);
        Series series = (Series) query.getSingleResult();
        return series;
    }

    private Instance getInstance(String sopInstanceUID) {
        Query query = em.createNamedQuery(Instance.FIND_BY_SOP_INSTANCE_UID);
        Instance instance = (Instance) query.getSingleResult();
        return instance;
    }

    private Collection<Code> getProcedureCodes(Attributes attrs) {
        ArrayList<Code> resultCode = new ArrayList<Code>();
        for (Attributes codeAttrs : attrs
                .getSequence(Tag.ProcedureCodeSequence)) {
            Code code = codeService.findOrCreate(new Code(codeAttrs));
            resultCode.add(code);
        }
        return resultCode;
    }

    private Code getConceptNameCode(Attributes attrs) {
        Attributes codeAttrs  = attrs.getSequence(Tag.ConceptNameCodeSequence).get(0);
        Code code = codeService.findOrCreate(new Code(codeAttrs));
        return code;
    }

    private Code getInstitutionalCode(Attributes attrs) {
        Attributes codeAttrs  = attrs.getSequence(Tag.InstitutionCodeSequence).get(0);
        Code code = codeService.findOrCreate(new Code(codeAttrs));
        return code;
    }
    private Issuer getIssuerOfAccessionNumber(Attributes attrs) {
        Attributes issuerAttrs = attrs.getSequence(Tag.IssuerOfAccessionNumberSequence).get(0);
        Issuer issuer = issuerService.findOrCreate(new Issuer(issuerAttrs));

        return issuer;
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
                VerifyingObserver newObserver = new VerifyingObserver(observer,
                        arcDevExt.getFuzzyStr());
                oldObservers.add(newObserver);
            }
        }
        return oldObservers;
    }

    private VerifyingObserver getVerifyingObserver(Attributes observerAttrs,
            Instance instance, ArchiveDeviceExtension arcDevExt) {
        Query query = em.createQuery("Select o from VerifyingObserver o "
                + "where o.instance = ?1");
        query.setParameter(1, instance);
        VerifyingObserver foundObserver = (VerifyingObserver) query.getSingleResult(); 
        return foundObserver;
    }

    private Collection<PatientID> findOrCreatePatientIDs(Collection<IDWithIssuer> ids) {
        ArrayList<PatientID> foundIDs = new ArrayList<PatientID>();
        for(IDWithIssuer id : ids){
        Issuer issuer =  issuerService.findOrCreate(new Issuer(id.getIssuer()));
         Query query = em.createQuery(
                 "Select pid from PatientID pid where pid.id = ?1 AND pid.issuer = ?2");
         query.setParameter(1, id.getID());
         query.setParameter(2, issuer);
         PatientID foundID = (PatientID) query.getSingleResult();
         
         foundID.setIssuer(issuer);
         foundIDs.add(foundID);
        }
         return foundIDs;
     }

    @Override
    public Study deleteStudy(String studyInstanceUID) throws Exception {
        TypedQuery<Study> query = em.createNamedQuery(
                Study.FIND_BY_STUDY_INSTANCE_UID, Study.class).setParameter(1,
                studyInstanceUID);
        Study study = query.getSingleResult();

        Collection<Series> allSeries = study.getSeries();
        for(Series series: allSeries)
        for(Instance inst : series.getInstances())
            deleteInstance(inst.getSopInstanceUID());

        em.remove(study);
        LOG.info("Removed study entity - " + studyInstanceUID);
        return study;
    }

    @Override
    public Series deleteSeries(String seriesInstanceUID) throws Exception {
        TypedQuery<Series> query = em.createNamedQuery(
                Series.FIND_BY_SERIES_INSTANCE_UID, Series.class)
                .setParameter(1, seriesInstanceUID);
        Series series = query.getSingleResult();

        Collection<Instance> insts = series.getInstances();
        for(Instance inst : insts)
            deleteInstance(inst.getSopInstanceUID());

        Study study = series.getStudy();
        em.remove(series);
        study.clearQueryAttributes();
        LOG.info("Removed series entity - " + seriesInstanceUID);
        return series;
    }

    @Override
    public Instance deleteInstance(String sopInstanceUID) throws Exception {
        TypedQuery<Instance> query = em.createNamedQuery(
                Instance.FIND_BY_SOP_INSTANCE_UID_FETCH_FILE_REFS_AND_FS, Instance.class)
                .setParameter(1, sopInstanceUID);
        Instance inst = query.getSingleResult();
        String[] sopUID = new String[1];
        sopUID[0]=sopInstanceUID;
        Collection<Instance> tmpList = locateInstances(sopUID);
        //reject to make sure no modality retrieves the item after it has been scheduled for delete and before it's actually deleted
        rejectionService.reject(this, tmpList, codeService.findOrCreate(new Code("113037","DCM",null,"Rejected for Patient Safety Reasons")), null);
        //schedule for deletion 
        rejectionService.deleteRejected(this, tmpList);
        
        Series series = inst.getSeries();
        Study study = series.getStudy();
        em.remove(inst);
        LOG.info("Removed instance entity - " + sopInstanceUID);
        series.clearQueryAttributes();
        study.clearQueryAttributes();

        return inst;
    }

    @Override
    public boolean deleteSeriesIfEmpty(String seriesInstanceUID,
            String studyInstanceUID) {
        TypedQuery<Series> query = em.createNamedQuery(
                Series.FIND_BY_SERIES_INSTANCE_UID, Series.class).setParameter(
                1, seriesInstanceUID);
        Series series = query.getSingleResult();
        if(series.getInstances().isEmpty()) {
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

        if (study.getSeries().isEmpty()) {
            em.remove(study);
            LOG.info("Removed study entity - " + studyInstanceUID);
            return true;
        }

        return false;
    }

    public Code findOrCreateCode(org.dcm4che3.data.Code code) {
        return codeService.findOrCreate(new Code(code));
    }
}
