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
package org.dcm4chee.archive.qc.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.dto.QCEventInstance;
import org.dcm4chee.archive.dto.ServiceQualifier;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.AttributesBlob;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.ContentItem;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.entity.QCActionHistory;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.entity.QCSeriesHistory;
import org.dcm4chee.archive.entity.QCStudyHistory;
import org.dcm4chee.archive.entity.QCUpdateHistory;
import org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope;
import org.dcm4chee.archive.entity.RequestAttributes;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.VerifyingObserver;
import org.dcm4chee.archive.iocm.RejectionDeleteService;
import org.dcm4chee.archive.iocm.RejectionService;
import org.dcm4chee.archive.iocm.client.ChangeRequesterService;
import org.dcm4chee.archive.issuer.IssuerService;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.qc.PatientCommands;
import org.dcm4chee.archive.qc.QCBean;
import org.dcm4chee.archive.qc.QCEvent;
import org.dcm4chee.archive.qc.QCEvent.QCOperation;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The stateless bean implementing the QCBean interface
 * Used by clients to perform QC operations.
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

@Stateless
public class QCBeanImpl  implements QCBean{

    private static final int[] PATIENT_AND_STUDY_ATTRS = {Tag.SpecificCharacterSet, Tag.StudyDate, Tag.StudyTime, Tag.AccessionNumber,
        Tag.IssuerOfAccessionNumberSequence, Tag.ReferringPhysicianName, Tag.PatientName, Tag.PatientID, Tag.IssuerOfPatientID,
        Tag.PatientBirthDate, Tag.PatientSex, Tag.StudyInstanceUID, Tag.StudyID };
    
    private static RecordFactory recordFactory = new RecordFactory();

    private static Logger LOG = LoggerFactory.getLogger(QCBeanImpl.class);

    /**
     * Enum ReferenceState. Signifies if an object's references were fully moved
     * , partially moved or none of the references were moved due to a QC
     * operation used by the methods for handling invisible objects (KO/PR/SR)
     * 
     */

    private enum ReferenceState {
    ALLMOVED,SOMEMOVED,NONEMOVED
    }

    @Inject
    private PatientService patientService;

    @Inject
    private CodeService codeService;

    @Inject
    private IssuerService issuerService;

    @Inject
    private RejectionService rejectionService;

    @Inject
    private Device device;

    @Inject 
    private RejectionDeleteService rejectionServiceDeleter;

    @Inject
    private ChangeRequesterService changeRequester;

    @Inject
    protected StoreService storeService; 

    @Inject
    @Any
    Event<QCEvent> internalNotification;

    @PersistenceContext(name="dcm4chee-arc")
    EntityManager em;

    private String qcSource="Quality Control";

    private ArchiveDeviceExtension archiveDeviceExtension;

    /**
     * Triggered by the container to set the value for the archive extension.
     */
    @PostConstruct
    public void init() {
        archiveDeviceExtension = device.getDeviceExtension(ArchiveDeviceExtension.class);
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#mergeStudies(java.lang.String[], 
     * java.lang.String, org.dcm4che3.data.Attributes,
     *  org.dcm4che3.data.Attributes, org.dcm4che3.data.Code)
     */
    @Override
    public QCEvent mergeStudies(String[] sourceStudyUids, String targetStudyUId, 
            Attributes targetStudyAttrs, Attributes targetSeriesAttrs, 
            org.dcm4che3.data.Code qcRejectionCode) {

        Collection<QCEventInstance> sourceUIDs = new ArrayList<QCEventInstance>();
        Collection<QCEventInstance> targetUIDs = new ArrayList<QCEventInstance>();
        QCEvent mergeEvent = new QCEvent(QCOperation.MERGE, null,
                null, sourceUIDs, targetUIDs);

        try{
            
        for(String sourceStudyUID : sourceStudyUids) {
            QCEvent singleMergeEvent = merge(sourceStudyUID, targetStudyUId,
                    targetStudyAttrs, targetSeriesAttrs,qcRejectionCode);
            sourceUIDs.addAll(singleMergeEvent.getSource());
            targetUIDs.addAll(singleMergeEvent.getTarget());
            mergeEvent.addRejectionNote(singleMergeEvent.getRejectionNotes().iterator().next());
        }
        
        }
        catch(Exception e) {
            LOG.error("{}: QC info[Merge] - Failure, reason {}",e);
            throw new EJBException();
        }
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(mergeEvent);
        return mergeEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#merge(java.lang.String, 
     * java.lang.String, org.dcm4che3.data.Attributes, 
     * org.dcm4che3.data.Attributes, boolean, org.dcm4che3.data.Code)
     */
    @Override
    public QCEvent merge(String sourceStudyUid, String targetStudyUID, 
            Attributes targetStudyAttrs, Attributes targetSeriesAttrs,
            org.dcm4che3.data.Code qcRejectionCode) {
        
        QCActionHistory mergeAction = generateQCAction(QCOperation.MERGE);
        List<QCEventInstance> sourceUIDs = new ArrayList<QCEventInstance>();
        List<QCEventInstance> targetUIDs = new ArrayList<QCEventInstance>();
        List<Instance> rejectedInstances = new ArrayList<Instance>();

        List<QCInstanceHistory> instancesHistory = new ArrayList<QCInstanceHistory>();
        Study source = findStudy(sourceStudyUid);
        Study target = findStudy(targetStudyUID);
        boolean samePatient = source.getPatient().getPk()==target.getPatient().getPk()?
                true:false;
        if(source==null || target==null) {
            LOG.error("{} : QC info[Merge] - Failure, Source Study {} or Target Study {}"
                    + " not Found",qcSource,sourceStudyUid, targetStudyUID);
            throw new EJBException();
        }

        if(!samePatient) {
            LOG.error("{} : QC info[Merge] - Failure, Source Study {} or Target Study {}"
                    + " do not belong to the same patient",qcSource,sourceStudyUid,
                    targetStudyUID);
            throw new EJBException();
        }
        
        QCStudyHistory studyHistory = createQCStudyHistory(source.getStudyInstanceUID(),
                targetStudyUID, target.getAttributes(), mergeAction);

        if(!targetStudyAttrs.isEmpty())
            updateStudy(archiveDeviceExtension, targetStudyUID, targetStudyAttrs);

        for(Series series: source.getSeries()) {
            rejectedInstances.addAll(series.getInstances());
            Series newSeries = createSeries(series, target, targetSeriesAttrs);

            QCSeriesHistory seriesHistory = createQCSeriesHistory(
                    series.getSeriesInstanceUID(), series.getAttributes(), studyHistory);

            for(Instance inst: series.getInstances()) {
                Instance newInstance = move(inst, newSeries, qcRejectionCode);
                QCInstanceHistory instanceHistory = new QCInstanceHistory( targetStudyUID, 
                        newSeries.getSeriesInstanceUID(), inst.getSopInstanceUID(),
                        newInstance.getSopInstanceUID(), newInstance.getSopInstanceUID(), false);
                instanceHistory.setSeries(seriesHistory);
                instancesHistory.add(instanceHistory);
                targetUIDs.add(new QCEventInstance(newInstance.getSopInstanceUID(),newSeries.getSeriesInstanceUID(), targetStudyUID));
                sourceUIDs.add(new QCEventInstance(inst.getSopInstanceUID(), series.getSeriesInstanceUID(), sourceStudyUid));
                //update identical document sequence
                if(series.getModality().equalsIgnoreCase("KO")
                        || series.getModality().equalsIgnoreCase("SR"))
                for(Instance ident: findIdenticalDocumentReferences(newInstance)) {
                    removeIdenticalDocumentSequence(ident, inst);
                    removeIdenticalDocumentSequence(inst,ident);
                    addIdenticalDocumentSequence(ident, newInstance);
                    addIdenticalDocumentSequence(newInstance, ident);
                    if(!identicalDocumentSequenceHistoryExists(ident, mergeAction) 
                            && !ident.getSeries().getStudy().getStudyInstanceUID()
                            .equalsIgnoreCase(sourceStudyUid))
                    addIdenticalDocumentSequenceHistory(ident, mergeAction);
                }
            }
        }
        recordHistoryEntry(instancesHistory);
        Instance rejNote = createAndStoreRejectionNote(
                codeService.findOrCreate(new Code(qcRejectionCode)), rejectedInstances);
        QCEvent mergeEvent = new QCEvent(QCOperation.MERGE, null,null,sourceUIDs,targetUIDs);
        mergeEvent.addRejectionNote(rejNote);
        changeRequester.scheduleChangeRequest(sourceUIDs, targetUIDs, rejNote);
        return mergeEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#split(java.util.Collection,
     *  org.dcm4che3.data.IDWithIssuer, java.lang.String,
     *   org.dcm4che3.data.Attributes, org.dcm4che3.data.Attributes, org.dcm4che3.data.Code)
     */
    @Override
    public QCEvent split(Collection<String> toMoveUIDs, IDWithIssuer pid,
            String targetStudyUID, Attributes createdStudyAttrs,
            Attributes targetSeriesAttrs, org.dcm4che3.data.Code qcRejectionCode) {
        
        QCActionHistory splitAction = generateQCAction(QCOperation.SPLIT);
        List<QCEventInstance> sourceUIDs = new ArrayList<QCEventInstance>();
        List<QCEventInstance> targetUIDs = new ArrayList<QCEventInstance>();
        List<QCInstanceHistory> instancesHistory = new ArrayList<QCInstanceHistory>();
        Collection<Instance> toMove = locateInstances( toMoveUIDs.toArray(new String[toMoveUIDs.size()]));
        Study sourceStudy= toMove.iterator().next().getSeries().getStudy();

        if(!allInstancesFromSameStudy(toMove)) {
            LOG.error("{} : QC info[Split] - Failure, Different studie used as source", qcSource);
            throw new EJBException();
        }
        Study targetStudy = findStudy(targetStudyUID);

        if(targetStudy == null) {
            LOG.debug("{} : QC info[Split] - Target study"
                    + " didn't exist, creating target study",qcSource);
            targetStudy = createStudy(pid, targetStudyUID, createdStudyAttrs);
        }
        else {
            if(!createdStudyAttrs.isEmpty())
            updateStudy(archiveDeviceExtension, targetStudyUID, createdStudyAttrs);
        }

        QCStudyHistory studyHistory = createQCStudyHistory(
                sourceStudy.getStudyInstanceUID(), targetStudy.getStudyInstanceUID(),
                sourceStudy.getAttributes(), splitAction);

        HashMap<String,NewSeriesTuple> oldToNewSeries = new HashMap<String, NewSeriesTuple>();
        ArrayList<String> sopUIDs = new ArrayList<String>();
        
        for(Instance inst : toMove)
            sopUIDs.add(inst.getSopInstanceUID());
        
        toMove = locateInstances(sopUIDs.toArray(new String[sopUIDs.size()]));
        Series newSeries;
        for(Instance instance: toMove) {
            if(!oldToNewSeries.keySet().contains(
                    instance.getSeries().getSeriesInstanceUID())) {
                newSeries= createSeries(instance.getSeries(), targetStudy, targetSeriesAttrs);
                Series series = instance.getSeries();
                QCSeriesHistory seriesHistory = createQCSeriesHistory(series.getSeriesInstanceUID(), 
                        series.getAttributes(), studyHistory);
                oldToNewSeries.put(instance.getSeries().getSeriesInstanceUID(), 
                        new NewSeriesTuple(newSeries.getPk(),seriesHistory));
                
            }
            else {
                long newSeriesPK = oldToNewSeries.get(instance.getSeries().getSeriesInstanceUID()).getPK();
                newSeries = em.find(Series.class, newSeriesPK);
            }
            Instance newInstance = move(instance,newSeries,qcRejectionCode);
            QCInstanceHistory instanceHistory = new QCInstanceHistory(targetStudyUID,
                    newSeries.getSeriesInstanceUID(), instance.getSopInstanceUID(),
                    newInstance.getSopInstanceUID(), newInstance.getSopInstanceUID(), false);
            instanceHistory.setSeries(oldToNewSeries.get(
                    instance.getSeries().getSeriesInstanceUID()).getSeriesHistory());
            instancesHistory.add(instanceHistory);
            targetUIDs.add(new QCEventInstance(newInstance.getSopInstanceUID(), newSeries.getSeriesInstanceUID(), targetStudyUID));
            sourceUIDs.add(new QCEventInstance(instance.getSopInstanceUID(),instance.getSeries().getSeriesInstanceUID(), sourceStudy.getStudyInstanceUID()));
        }

        instancesHistory.addAll(handleKOPRSR(targetSeriesAttrs, qcRejectionCode, 
                studyHistory, sourceUIDs,
                targetUIDs, sourceStudy, 
                targetStudy, oldToNewSeries));

        recordHistoryEntry(instancesHistory);
        Instance rejNote = createAndStoreRejectionNote(qcRejectionCode, toMove);
        QCEvent splitEvent = new QCEvent(QCOperation.SPLIT,null,null,sourceUIDs,targetUIDs);
        splitEvent.addRejectionNote(rejNote);
        changeRequester.scheduleChangeRequest(sourceUIDs, targetUIDs, rejNote);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(splitEvent);
        return splitEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#segment(java.util.Collection, 
     * java.util.Collection, org.dcm4che3.data.IDWithIssuer, java.lang.String, 
     * org.dcm4che3.data.Attributes, org.dcm4che3.data.Attributes, org.dcm4che3.data.Code)
     */
    @Override
    public QCEvent segment(Collection<String> toMoveUIDs, Collection<String> toCloneUIDs,
            IDWithIssuer pid, String targetStudyUID, 
            Attributes createdStudyAttrs,Attributes targetSeriesAttrs,
            org.dcm4che3.data.Code qcRejectionCode) {
        QCActionHistory segmentAction = generateQCAction(QCOperation.SEGMENT);
        List<QCEventInstance> movedSourceUIDs = new ArrayList<QCEventInstance>();
        List<QCEventInstance> movedTargetUIDs = new ArrayList<QCEventInstance>();
        List<QCEventInstance> clonedSourceUIDs = new ArrayList<QCEventInstance>();
        List<QCEventInstance> clonedTargetUIDs = new ArrayList<QCEventInstance>();
        List<QCInstanceHistory> instancesHistory = new ArrayList<QCInstanceHistory>();
        //check move and clone belong to same study
        ArrayList<Instance> tmpAllInstancesInvolved = new ArrayList<Instance>();
        Collection<Instance> toMove = locateInstances(toMoveUIDs.isEmpty()?new String[]{}:toMoveUIDs.toArray(new String[toMoveUIDs.size()]));
        Collection<Instance> toClone = locateInstances(toCloneUIDs.isEmpty()?new String[]{}:toCloneUIDs.toArray(new String[toCloneUIDs.size()]));
        tmpAllInstancesInvolved.addAll(toMove);
        tmpAllInstancesInvolved.addAll(toClone);
        Study sourceStudy = tmpAllInstancesInvolved.get(0).getSeries().getStudy();
        if(!allInstancesFromSameStudy(tmpAllInstancesInvolved)) {
            LOG.error("{} : QC info[Segment] Failure, Different studies used as source", qcSource);
            throw new EJBException();
        }
        Study targetStudy = findStudy(targetStudyUID);
        if(targetStudy == null) {
            LOG.debug("{} :  QC info[Segment] info - Target study didn't exist, creating target study",qcSource);
            targetStudy = createStudy(pid, targetStudyUID, createdStudyAttrs);
        }
        else {
            if(!createdStudyAttrs.isEmpty())
            updateStudy(archiveDeviceExtension, targetStudyUID, createdStudyAttrs);
        }
        if(sourceStudy.getPatient().getPk()!=targetStudy.getPatient().getPk()) {
            LOG.error("{} :  QC info[Segment] Failure, Source Study {} or Target Study {}"
                    + " do not belong to the same patient",
                    qcSource,sourceStudy.getStudyInstanceUID(), targetStudyUID);
            throw new EJBException();
        }
        
        QCStudyHistory studyHistory = createQCStudyHistory(sourceStudy.getStudyInstanceUID(), 
                targetStudyUID, sourceStudy.getAttributes(), segmentAction);
        HashMap<String,NewSeriesTuple> oldToNewSeries = new HashMap<String, NewSeriesTuple>();
        Series newSeries;
        //move
        for(Instance instance: toMove) {
            if(!oldToNewSeries.keySet().contains(
                    instance.getSeries().getSeriesInstanceUID())) {
                newSeries= createSeries(instance.getSeries(),
                        targetStudy, targetSeriesAttrs);
                Series series = instance.getSeries();
                QCSeriesHistory seriesHistory = createQCSeriesHistory(series.getSeriesInstanceUID(),
                        series.getAttributes(), studyHistory);
                oldToNewSeries.put(instance.getSeries().getSeriesInstanceUID(), 
                        new NewSeriesTuple(newSeries.getPk(),seriesHistory));
            }
            else {
                newSeries = em.find(Series.class, oldToNewSeries.get(
                        instance.getSeries().getSeriesInstanceUID()).getPK());
            }
            Instance newInstance = move(instance,newSeries,qcRejectionCode);
            QCInstanceHistory instanceHistory = new QCInstanceHistory(targetStudyUID,
                    newSeries.getSeriesInstanceUID(), instance.getSopInstanceUID(),
                    newInstance.getSopInstanceUID(), newInstance.getSopInstanceUID(), false);

            instanceHistory.setSeries(oldToNewSeries.get(instance.getSeries()
                    .getSeriesInstanceUID()).getSeriesHistory());
            instancesHistory.add(instanceHistory);
            movedTargetUIDs.add(new QCEventInstance(newInstance.getSopInstanceUID(), newSeries.getSeriesInstanceUID(), targetStudy.getStudyInstanceUID()));
            movedSourceUIDs.add(new QCEventInstance(instance.getSopInstanceUID(), instance.getSeries().getSeriesInstanceUID(), sourceStudy.getStudyInstanceUID()));
            }
        
        //clone
        for(Instance instance: toClone) {
            if(!oldToNewSeries.keySet().contains(
                    instance.getSeries().getSeriesInstanceUID())) {
                newSeries= createSeries(instance.getSeries(),
                        targetStudy, targetSeriesAttrs);
                Series series = instance.getSeries();
                QCSeriesHistory seriesHistory = createQCSeriesHistory(series.getSeriesInstanceUID(),
                        series.getAttributes(), studyHistory);
                oldToNewSeries.put(instance.getSeries().getSeriesInstanceUID(), 
                        new NewSeriesTuple(newSeries.getPk(),seriesHistory));
            }
            else {
                newSeries = em.find(Series.class, oldToNewSeries.get(
                        instance.getSeries().getSeriesInstanceUID()).getPK());
            }
            Instance newInstance = clone(instance,newSeries);
            QCInstanceHistory instanceHistory = new QCInstanceHistory(targetStudyUID,
                    newSeries.getSeriesInstanceUID(), instance.getSopInstanceUID(),
                    newInstance.getSopInstanceUID(), newInstance.getSopInstanceUID(), true);
            
            instanceHistory.setSeries(oldToNewSeries.get(instance.getSeries()
                    .getSeriesInstanceUID()).getSeriesHistory());
            instancesHistory.add(instanceHistory);
            clonedTargetUIDs.add(new QCEventInstance(newInstance.getSopInstanceUID(), newSeries.getSeriesInstanceUID(), targetStudy.getStudyInstanceUID()));
            clonedSourceUIDs.add(new QCEventInstance(instance.getSopInstanceUID(), instance.getSeries().getSeriesInstanceUID(), sourceStudy.getStudyInstanceUID()));
            }
        instancesHistory.addAll(handleKOPRSR(targetSeriesAttrs, qcRejectionCode, 
                studyHistory, movedSourceUIDs,
                movedTargetUIDs, sourceStudy,
                targetStudy, oldToNewSeries));

        movedSourceUIDs.addAll(clonedSourceUIDs);
        movedTargetUIDs.addAll(clonedTargetUIDs);
        recordHistoryEntry(instancesHistory);
        Instance rejNote = createAndStoreRejectionNote(
                codeService.findOrCreate(new Code(qcRejectionCode)), toMove);
        QCEvent segmentEvent = new QCEvent(QCOperation.SEGMENT,null,null,movedSourceUIDs,movedTargetUIDs);
        segmentEvent.addRejectionNote(rejNote);
        changeRequester.scheduleChangeRequest(movedSourceUIDs, movedTargetUIDs, rejNote);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(segmentEvent);
        return segmentEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#segmentFrame(org.dcm4chee.archive.entity.Instance, 
     * org.dcm4chee.archive.entity.Instance, int, org.dcm4chee.archive.entity.PatientID,
     *  java.lang.String, org.dcm4che3.data.Attributes, org.dcm4che3.data.Attributes)
     */
    @Override
    public void segmentFrame(Instance toMove, Instance toClone, int frame,
            PatientID pid, String targetStudyUID,
            Attributes targetStudyAttrs, Attributes targetSeriesAttrs) {
        LOG.info("{} :  QC info[Segment] info - Segmenting frame is not yet supported",qcSource);
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#reject(java.lang.String[], org.dcm4che3.data.Code)
     */
    @Override
    public QCEvent reject(String[] sopInstanceUIDs, org.dcm4che3.data.Code qcRejectionCode) {
        Collection<Instance> src = locateInstances(sopInstanceUIDs);
        rejectionService.reject(qcSource, src, findOrCreateCode(qcRejectionCode), null);
        ArrayList<QCEventInstance> iuids = new ArrayList<QCEventInstance>();
        for(Instance inst : src) {
            iuids.add(new QCEventInstance(inst.getSopInstanceUID(), inst.getSeries().getSeriesInstanceUID(), inst.getSeries().getStudy().getStudyInstanceUID()));
        }
        Instance rejNote =createAndStoreRejectionNote(qcRejectionCode, src);
        QCEvent rejectEvent = new QCEvent(QCOperation.REJECT, null, null, iuids, null);
        rejectEvent.addRejectionNote(rejNote);
        changeRequester.scheduleChangeRequest(iuids, null, rejNote);
        return rejectEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#restore(java.lang.String[])
     */
    @Override
    public QCEvent restore(String[] sopInstanceUIDs) {
        Collection<Instance> instances = locateInstances(sopInstanceUIDs);
        ArrayList<QCEventInstance> filteredIUIDs = new ArrayList<QCEventInstance>();
        instances = filterQCed(instances);
        rejectionService.restore(qcSource, instances, null);
        for(Instance inst : instances) {
            filteredIUIDs.add(new QCEventInstance(inst.getSopInstanceUID(), inst.getSeries().getSeriesInstanceUID(), inst.getSeries().getStudy().getStudyInstanceUID()));
        }
        
        QCEvent restoreEvent = new QCEvent(QCOperation.RESTORE, null,null, filteredIUIDs, null);
        return restoreEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#canApplyQC(org.dcm4chee.archive.entity.Instance)
     */
    @Override
    public boolean canApplyQC(Instance instance) {
        return instance.getRejectionNoteCode()!=null? false : true;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#notify(org.dcm4chee.archive.qc.QCEvent)
     */
    @Override
    public void notify(QCEvent event) {
        LOG.debug("{} :  QC info[Notify] - Operation successfull,"
                + " notification triggered with event {}",qcSource,event.toString());
        internalNotification.select(new ServiceQualifier(ServiceType.QCPOSTPROCESSING)).fire(event);
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#updateDicomObject(org.dcm4chee.archive.conf.ArchiveDeviceExtension,
     *  org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope, org.dcm4che3.data.Attributes)
     */
    @Override
    public QCEvent updateDicomObject(ArchiveDeviceExtension arcDevExt,
            QCUpdateScope scope, Attributes attrs)
            throws EntityNotFoundException {
        QCActionHistory updateAction = generateQCAction(QCOperation.UPDATE);
        LOG.info("{}:  QC info[Update] info - Performing QC update DICOM header on {} scope : ", qcSource, scope);
        Attributes unmodified;
        PatientAttrsPKTuple unmodifiedAndPK = null;
        String queryString = null, queryParam = null;
        switch(scope) {
        case PATIENT:
            unmodifiedAndPK=updatePatient(arcDevExt, attrs);
            unmodified = unmodifiedAndPK.getUnModifiedAttrs();
            break;
        case STUDY: 
            queryString = "SELECT i.sopInstanceUID from Instance i WHERE i.series.study.studyInstanceUID = ?1";
            queryParam = attrs.getString(Tag.StudyInstanceUID);
            unmodified=updateStudy(arcDevExt, queryParam, attrs);
            break;
        case SERIES: 
            queryString = "SELECT i.sopInstanceUID from Instance i WHERE i.series.seriesInstanceUID = ?1";
            queryParam = attrs.getString(Tag.SeriesInstanceUID);
            unmodified=updateSeries(arcDevExt, queryParam, attrs);
            break;
        case INSTANCE: 
            queryString = "SELECT i.sopInstanceUID from Instance i WHERE i.sopInstanceUID = ?1";
            queryParam = attrs.getString(Tag.SOPInstanceUID);
            unmodified=updateInstance(arcDevExt, queryParam, attrs);
            break;
        default : 
            LOG.error("{} : QC info[Update] Failure - invalid update scope",qcSource);
            throw new EJBException();
        }
        LOG.info("{} : QC info[Update] info - Update successful, adding update history entry",qcSource);
        addUpdateHistoryEntry(updateAction, scope, unmodified,
                scope == QCUpdateScope.PATIENT?Long.toString(unmodifiedAndPK.getPK()):null);
        QCEvent updateEvent = new QCEvent(QCOperation.UPDATE,scope.toString(),
                attrs, null, null);
        if (queryString != null) {
            Query query = em.createQuery(queryString);
            query.setParameter(1, queryParam);
            @SuppressWarnings("unchecked")
            List<String> iuids = query.getResultList();
            if (iuids.size() > 0) {
                ArrayList<QCEventInstance> eventIUIDs = new ArrayList<QCEventInstance>();
                for(String str : iuids)
                    eventIUIDs.add(new QCEventInstance(str, null, null));
                changeRequester.scheduleUpdateOnlyChangeRequest(eventIUIDs);
            }
        }
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(updateEvent);
        return updateEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#patientOperation(org.dcm4che3.data.Attributes, 
     * org.dcm4che3.data.Attributes, org.dcm4chee.archive.conf.ArchiveAEExtension,
     *  org.dcm4chee.archive.qc.PatientCommands)
     */
    public boolean patientOperation(Attributes srcPatientAttrs,
            Attributes targetPatientAttrs, ArchiveAEExtension arcAEExt, 
            PatientCommands command) {
        try {
            
            if(command == PatientCommands.PATIENT_UPDATE_ID)
                patientService.updatePatientID(srcPatientAttrs,
                        targetPatientAttrs,arcAEExt.getStoreParam());
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
            LOG.error("{} :  QC info[Patient Operation] Failure - Patient operation "
                    + "failed, reason {}", qcSource, e);
            return false;
        }
    }

    @Override
    public QCEvent deletePatient(IDWithIssuer pid, org.dcm4che3.data.Code qcRejectionCode) throws Exception {
        ArrayList<QCEventInstance> eventUIDs = new ArrayList<QCEventInstance>();
        ArrayList<Instance> rejectedInstances = new ArrayList<Instance>();
        Patient patient = findPatient(pid);
        Collection<Study> studies = patient.getStudies();
        for(Study study : studies) {
           Collection<Series> allSeries = study.getSeries();
           for(Series series: allSeries) {
               Collection<Instance> insts = series.getInstances();
               for(Instance inst : insts) {
                   eventUIDs.add(new QCEventInstance(inst.getSopInstanceUID(), series.getSeriesInstanceUID(), study.getStudyInstanceUID()));
               }
               rejectedInstances.addAll(insts);
           }
        }
        LOG.info("{}:  QC info[Delete] info - Rejected patient instances {} "
                + "- scheduled to delete",qcSource , pid.toString());
        Instance rejNote = createAndStoreRejectionNote(qcRejectionCode, rejectedInstances);

        QCEvent deleteEvent = new QCEvent(QCOperation.DELETE, null, null, eventUIDs, null);
        deleteEvent.addRejectionNote(rejNote);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(deleteEvent);
        rejectAndScheduleForDeletion(rejectedInstances, qcRejectionCode);
        changeRequester.scheduleChangeRequest(eventUIDs, null, rejNote);
        return deleteEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#deleteStudy(java.lang.String)
     */
    @Override
    public QCEvent deleteStudy(String studyInstanceUID, org.dcm4che3.data.Code qcRejectionCode) throws Exception {
        ArrayList<QCEventInstance> eventUIDs = new ArrayList<QCEventInstance>();
        ArrayList<Instance> rejectedInstances = new ArrayList<Instance>();
        TypedQuery<Study> query = em.createNamedQuery(
                Study.FIND_BY_STUDY_INSTANCE_UID, Study.class).setParameter(1,
                studyInstanceUID);
        Study study = query.getSingleResult();
        
        Collection<Series> allSeries = study.getSeries();
        for(Series series: allSeries) {
            Collection<Instance> insts = series.getInstances();
            for(Instance inst : insts) {
                eventUIDs.add(new QCEventInstance(inst.getSopInstanceUID(), series.getSeriesInstanceUID(), study.getStudyInstanceUID()));
            }
            rejectedInstances.addAll(insts);
        }
        LOG.info("{}:  QC info[Delete] info - Rejected study instances {} "
                + "- scheduled to delete",qcSource , studyInstanceUID);
        Instance rejNote = createAndStoreRejectionNote(qcRejectionCode, rejectedInstances);

        QCEvent deleteEvent = new QCEvent(QCOperation.DELETE, null, null, eventUIDs, null);
        deleteEvent.addRejectionNote(rejNote);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(deleteEvent);
        rejectAndScheduleForDeletion(rejectedInstances, qcRejectionCode);
        changeRequester.scheduleChangeRequest(eventUIDs, null, rejNote);
        study.setRejected(true);
        return deleteEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#deleteSeries(java.lang.String)
     */
    @Override
    public QCEvent deleteSeries(String seriesInstanceUID, org.dcm4che3.data.Code qcRejectionCode) throws Exception {
        ArrayList<QCEventInstance> eventUIDs = new ArrayList<QCEventInstance>();
        TypedQuery<Series> query = em.createNamedQuery(
                Series.FIND_BY_SERIES_INSTANCE_UID, Series.class)
                .setParameter(1, seriesInstanceUID);
        Series series = query.getSingleResult();
        Collection<Instance> insts = series.getInstances();
        Study study = series.getStudy();
        for(Instance inst : insts) {
            eventUIDs.add(new QCEventInstance(inst.getSopInstanceUID(),series.getSeriesInstanceUID(), study.getStudyInstanceUID()));
        }
        study.clearQueryAttributes();
        LOG.info("{}:  QC info[Delete] info - Rejected series instances {} "
                + "- scheduled for delete",qcSource, seriesInstanceUID);
        Instance rejNote = createAndStoreRejectionNote(qcRejectionCode, insts);
        QCEvent deleteEvent = new QCEvent(QCOperation.DELETE, null, null, eventUIDs, null);
        deleteEvent.addRejectionNote(rejNote);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(deleteEvent);
        rejectAndScheduleForDeletion(insts, qcRejectionCode);

        changeRequester.scheduleChangeRequest(eventUIDs, null, rejNote);
        series.setRejected(true);
        return deleteEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#deleteInstance(java.lang.String)
     */
    @Override
    public QCEvent deleteInstance(String sopInstanceUID, org.dcm4che3.data.Code qcRejectionCode) throws Exception {
        Collection<Instance> tmpList = locateInstances(new String [] {sopInstanceUID});
        
        if(tmpList.isEmpty()) {
            LOG.debug("{}:  QC info[Delete] Failure - Error finding "
                    + "instance to delete with SOPInstanceUID={}",qcSource,sopInstanceUID);
            throw new EJBException();
        }
        
        Series series = tmpList.iterator().next().getSeries();
        Study study = series.getStudy();
        LOG.info("{}:  QC info[Delete] info - Rejected instance {} - scheduled for delete", qcSource, sopInstanceUID);
        series.clearQueryAttributes();
        study.clearQueryAttributes();
        List<QCEventInstance> eventUIDs = new ArrayList<QCEventInstance>();
        for(Instance inst : tmpList) {
            eventUIDs.add(new QCEventInstance(inst.getSopInstanceUID(),series.getSeriesInstanceUID(), study.getStudyInstanceUID()));
        }
        Instance rejNote = createAndStoreRejectionNote(qcRejectionCode, tmpList);
        QCEvent deleteEvent = new QCEvent(QCOperation.DELETE, null, null, eventUIDs, null);
        deleteEvent.addRejectionNote(rejNote);
        internalNotification.select(new ServiceQualifier(ServiceType.QCDURINGTRANSACTION)).fire(deleteEvent);
        rejectAndScheduleForDeletion(tmpList, qcRejectionCode);
        changeRequester.scheduleChangeRequest(eventUIDs, null, rejNote);
        return deleteEvent;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#deleteSeriesIfEmpty(java.lang.String, java.lang.String)
     */
    @Override
    public boolean deleteSeriesIfEmpty(String seriesInstanceUID,
            String studyInstanceUID) {
        TypedQuery<Series> query = em.createNamedQuery(
                Series.FIND_BY_SERIES_INSTANCE_UID, Series.class).setParameter(
                1, seriesInstanceUID);
        Series series = query.getSingleResult();
        if(series.getInstances().isEmpty()) {
            em.remove(series);
            LOG.info("{}:  QC info[Delete] info - Removed series entity {}", qcSource, seriesInstanceUID);
            return true;
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#deletePatientIfEmpty(org.dcm4che3.data.IDWithIssuer)
     */
    @Override
    public boolean deletePatientIfEmpty(IDWithIssuer pid) {
        
        Patient patient = findPatient(pid);
        if(patient == null)
            return false;
        if (patient.getStudies().isEmpty()) {
            em.remove(patient);
            LOG.info("{}:  QC info[Delete] info - Removed patient entity {}", qcSource, pid);
            return true;
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#deleteStudyIfEmpty(java.lang.String)
     */
    @Override
    public boolean deleteStudyIfEmpty(String studyInstanceUID) {
        TypedQuery<Study> query = em.createNamedQuery(
                Study.FIND_BY_STUDY_INSTANCE_UID, Study.class).setParameter(1,
                studyInstanceUID);
        Study study = query.getSingleResult();

        if (study.getSeries().isEmpty()) {
            em.remove(study);
            LOG.info("{}:  QC info[Delete] info - Removed study entity {}", qcSource, studyInstanceUID);
            return true;
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#locateInstances(java.lang.String[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<Instance> locateInstances(String[] sopInstanceUIDs) {
        ArrayList<Instance> list = new ArrayList<Instance>();
        if(sopInstanceUIDs == null || sopInstanceUIDs.length == 0) {
            LOG.error("{} : QC info[locateInstance] - Unable to locate instances with null UIDs"
                    + " , returning an empty list", qcSource);
            return list;
        }
        ArrayList<String> uids = new ArrayList<String>();
        uids.addAll(Arrays.asList(sopInstanceUIDs));
        Query query  = em.createNamedQuery(Instance.FIND_BY_SOP_INSTANCE_UID_EAGER_MANY);
        query.setParameter("uids", uids);
        list = (ArrayList<Instance>) query.getResultList();
        
        return list;
    }



    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#move(org.dcm4chee.archive.entity.Instance,
     *  org.dcm4chee.archive.entity.Series, org.dcm4che3.data.Code)
     */

    private Instance move(Instance source, Series target,
            org.dcm4che3.data.Code qcRejectionCode) {
        if(!canApplyQC(source)) {
            LOG.error("{} : QC info[move] Failure - Can't apply QC operation on already QCed"
                    + " or rejected object",qcSource);
            throw new EJBException();
        }
        ArrayList<Instance> list = new ArrayList<Instance>();
        list.add(source);
        reject(list, qcRejectionCode);
        Instance newInstance;
        try {
            
            newInstance = createInstance(source, target);
            Collection<Location> locations  = newInstance.getLocations() ;
            
            if(newInstance.getLocations()==null) {
                locations = new ArrayList<Location>();
                newInstance.setLocations(locations);
            };
            
            locations.add(getFirstFileRef(source.getLocations(), newInstance));
            
        } catch (Exception e) {
            LOG.error("{} : QC info[move] Failure - Unable to"
                    + " create replacement instance for {},"
                    + " rolling back move", qcSource,source);
            throw new EJBException(e);
        }
        return newInstance;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#clone(org.dcm4chee.archive.entity.Instance, org.dcm4chee.archive.entity.Series)
     */

    private Instance clone(Instance source, Series target) {
        Instance newInstance;
        if(!canApplyQC(source)) {
            LOG.error("{} : QC info[clone] Can't apply QC operation on already QCed"
                    + " or rejected object",qcSource);
            throw new EJBException();
        }
        try {
            newInstance = createInstance(source, target);
            Collection<Location> locations  = newInstance.getLocations() ;
            
            if(newInstance.getLocations()==null) {
                locations = new ArrayList<Location>();
                newInstance.setLocations(locations);
            };
            
            locations.add(getFirstFileRef(source.getLocations(), newInstance));
            
        } catch (Exception e) {
            LOG.error("{} : QC info[clone] Unable to create cloned instance for {},"
                    + " rolling back clone", qcSource,source);
            throw new EJBException(e);
        }
        return newInstance;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#reject(java.util.Collection, org.dcm4che3.data.Code)
     */
    
    private void reject(Collection<Instance> instances, org.dcm4che3.data.Code qcRejectionCode) {
        //QCActionHistory rejectAction = generateQCAction(QCOperation.REJECT);
        ArrayList<String> sopInstanceUIDs = new ArrayList<String>();
        try {
        rejectionService.reject(qcSource, instances, findOrCreateCode(qcRejectionCode), null);
        for(Instance inst: instances) {
            sopInstanceUIDs.add(inst.getSopInstanceUID());
        }
        //createRejectHistory(instances, rejectAction);
        }
        catch(Exception e) {
            LOG.error("{} : QC info[reject] Failure - Reject Failure, reason {}", qcSource, e);
            throw new EJBException();
        }
    }

    private void rejectAndScheduleForDeletion(Collection<Instance> insts, org.dcm4che3.data.Code qcRejectionCode) {
        rejectionService.reject(this, insts, codeService.findOrCreate(qcRejectionCode), null); 
        rejectionServiceDeleter.deleteRejected(this, insts);
    }

    /**
     * Creates the QC series history entity.
     * Sets the history attributes to that of the old series.
     * 
     * @param seriesInstanceUID
     *            the series instance uid
     * @param oldAttributes
     *            the old attributes
     * @param studyHistory
     *            the study history
     * @return the QC series history
     */
    private QCSeriesHistory createQCSeriesHistory(String seriesInstanceUID,
            Attributes oldAttributes, QCStudyHistory studyHistory) {
        QCSeriesHistory seriesHistory = new QCSeriesHistory();
        seriesHistory.setStudy(studyHistory);
        if(!oldAttributes.isEmpty())
          seriesHistory.setUpdatedAttributesBlob(new AttributesBlob(oldAttributes));
        seriesHistory.setOldSeriesUID(seriesInstanceUID);
        em.persist(seriesHistory);
        return seriesHistory;
    }

    /**
     * Creates the QC study history entity.
     * Sets the history attributes to that of the old study.
     * 
     * @param studyInstanceUID
     *            the study instance uid
     * @param target 
     * @param oldAttributes
     *            the old attributes
     * @param qcActionHistory
     *            the qc action history
     * @return the QC study history
     */
    private QCStudyHistory createQCStudyHistory(String studyInstanceUID,
            String targetStudyUID, Attributes oldAttributes, QCActionHistory qcActionHistory) {
        QCStudyHistory studyHistory = new QCStudyHistory();
        studyHistory.setAction(qcActionHistory);
        if(!oldAttributes.isEmpty())
          studyHistory.setUpdatedAttributesBlob(new AttributesBlob(oldAttributes));
        studyHistory.setOldStudyUID(studyInstanceUID);
        studyHistory.setNextStudyUID(targetStudyUID);
        em.persist(studyHistory);
        return studyHistory;
    }

    /**
     * Update patient.
     * Updates the attributes of the patient to the provided attributes
     * Throws exception on patient not found.
     * 
     * @param arcDevExt
     *            the arc dev ext
     * @param attrs
     *            the attrs
     * @param patientPK 
     * @return the attributes
     * @throws EntityNotFoundException
     *             the entity not found exception
     */
    private PatientAttrsPKTuple updatePatient(ArchiveDeviceExtension arcDevExt, Attributes attrs)
            throws EntityNotFoundException {
        Patient patient = findPatient(attrs);
        if (patient == null)
            throw new EntityNotFoundException(
                    "Unable to find patient or multiple patients found");

        Attributes original = patient.getAttributes();
        Attributes unmodified = new Attributes();
        unmodified.addAll(patient.getAttributes());

        original.update(attrs, original);
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} : QC info[Update] info - "
                    + "Attributes modified:\n {}",qcSource, attrs.toString());
        }
        patient.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Patient),
                arcDevExt.getFuzzyStr(), arcDevExt.getNullValueForQueryFields());

        return new PatientAttrsPKTuple(patient.getPk(), unmodified);
    }
    
    /**
     * Update study.
     * Updates the attributes of a study
     * Throws exception on study not found.
     * 
     * @param arcDevExt
     *            the arc dev ext
     * @param studyInstanceUID
     *            the study instance uid
     * @param attrs
     *            the attrs
     * @return the attributes
     * @throws EntityNotFoundException
     *             the entity not found exception
     */
    private Attributes updateStudy(ArchiveDeviceExtension arcDevExt,
            String studyInstanceUID, Attributes attrs)
            throws EntityNotFoundException {
        Study study = findStudy(studyInstanceUID);
        if (study == null)
            throw new EntityNotFoundException(
                    "Unable to find study "+ studyInstanceUID);
        Attributes original = study.getAttributes();
        Attributes unmodified = new Attributes();
        unmodified.addAll(study.getAttributes());
        // relations
        if (attrs.contains(Tag.ProcedureCodeSequence)) {
            Collection<Code> procedureCodes = findProcedureCodes(attrs);
            if (!procedureCodes.isEmpty()) {
                study.setProcedureCodes(procedureCodes);
            }
        }
        // one item only
        if (attrs.contains(Tag.IssuerOfAccessionNumberSequence)) {
            Issuer issuerOfAccessionNumber = findIssuerOfAccessionNumber(attrs);
            if (issuerOfAccessionNumber != null) {
                study.setIssuerOfAccessionNumber(issuerOfAccessionNumber);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} : QC info[Update] info - "
                    + "Attributes modified:\n" + attrs.toString());
        }
        original.update(attrs, original);
        study.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Study),
                arcDevExt.getFuzzyStr(), arcDevExt.getNullValueForQueryFields());
        return unmodified;
    }

    /**
     * Update series.
     * Updates the attributes of a series
     * Throws exception on series not found.
     * 
     * @param arcDevExt
     *            the arc dev ext
     * @param seriesInstanceUID
     *            the series instance uid
     * @param attrs
     *            the attrs
     * @return the attributes
     * @throws EntityNotFoundException
     *             the entity not found exception
     */
    private Attributes updateSeries(ArchiveDeviceExtension arcDevExt, 
            String seriesInstanceUID, Attributes attrs)
            throws EntityNotFoundException {
        
        Series series = findSeries(seriesInstanceUID);
        
        if (series == null)
            throw new EntityNotFoundException("Unable to find series "
                    + seriesInstanceUID);
        Attributes original = series.getAttributes();
        Attributes unmodified = new Attributes();
        unmodified.addAll(series.getAttributes());
        // relations
        // institutionCode
        if (attrs.contains(Tag.InstitutionCodeSequence)) {
                Code institutionCode = findInstitutionalCode(attrs);
                if (institutionCode != null) {
                    series.setInstitutionCode(institutionCode);
                }
        }
        // Requested Procedure Step
        if (attrs.contains(Tag.RequestAttributesSequence)) {

            Collection<RequestAttributes> requestAttrs = findRequestAttributes(
                    series, attrs, arcDevExt, original);
                series.setRequestAttributes(requestAttrs);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} : QC info[Update] info - "
                    + "Attributes modified:\n" + attrs.toString());
        }
        
        original.update(attrs, original);
        series.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Series),
                arcDevExt.getFuzzyStr(), arcDevExt.getNullValueForQueryFields());
        return unmodified;
    }

    /**
     * Update instance.
     * Updates the attributes of an instance.
     * Throws exception on instance not found.
     * 
     * @param arcDevExt
     *            the arc dev ext
     * @param sopInstanceUID
     *            the sop instance uid
     * @param attrs
     *            the attrs
     * @return the attributes
     * @throws EntityNotFoundException
     *             the entity not found exception
     */
    private Attributes updateInstance(ArchiveDeviceExtension arcDevExt,
            String sopInstanceUID, Attributes attrs)
            throws EntityNotFoundException {
        
        Instance instance = findInstance(sopInstanceUID);
        
        if (instance == null)
            throw new EntityNotFoundException("Unable to find instance "
                    + sopInstanceUID);
        
        Attributes original = instance.getAttributes();
        Attributes unmodified = new Attributes();
        unmodified.addAll(instance.getAttributes());
        // relations
        // Concept name Code Sequence on root level (SR)
        if (attrs.contains(Tag.ConceptNameCodeSequence)) {
            Code conceptNameCode = findConceptNameCode(attrs);
            if (conceptNameCode != null) {
                instance.setConceptNameCode(conceptNameCode);
            }
        }
        
        // verifying observers
        if (attrs.contains(Tag.VerifyingObserverSequence)) {
            Collection<VerifyingObserver> newObservers = findVerifyingObservers(
                    instance, attrs, original, arcDevExt);
            if (newObservers != null) {
                instance.setVerifyingObservers(newObservers);
            }
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} : QC info[Update] info - "
                    + "Attributes modified:\n" + attrs.toString());
        }
        
        original.update(attrs, original);
        instance.setAttributes(original,
                arcDevExt.getAttributeFilter(Entity.Instance),
                arcDevExt.getFuzzyStr(), arcDevExt.getNullValueForQueryFields());
        return unmodified;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.archive.qc.QCBean#findPatient(org.dcm4che3.data.Attributes)
     */
    @Override
    public Patient findPatient(Attributes attrs) {
        Collection<IDWithIssuer> pids = IDWithIssuer.pidsOf(attrs);
        Collection<PatientID> queryIDs = findPatientIDs(pids);
        
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} : QC info[findPatient] info - "
                    + "Found patient {}", qcSource, pat.toString());
        }
        return pat;
    }

    /**
     * Gets the study from the archive.
     * 
     * @param studyInstanceUID
     *            the study instance UID
     * @return the study
     */
    private Study findStudy(String studyInstanceUID) {
        try{
        Query query = em.createNamedQuery(Study.FIND_BY_STUDY_INSTANCE_UID_EAGER);
        query.setParameter(1, studyInstanceUID);
        Study study = (Study) query.getSingleResult();
        return study;
        }
        catch(NoResultException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} : QC info[findStudy] Failure - "
                        + "Unable to find study {}", qcSource, studyInstanceUID);
            }
            return null;
        }
    }

    /**
     * Gets the series from the archive.
     * 
     * @param seriesInstanceUID
     *            the series instance UID
     * @return the series
     */
    private Series findSeries(String seriesInstanceUID) {
        Query query = em.createNamedQuery(Series.FIND_BY_SERIES_INSTANCE_UID_EAGER);
        query.setParameter(1, seriesInstanceUID);
        Series series = (Series) query.getSingleResult();
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} : QC info[findSeries] info - "
                    + "Found series {}", qcSource, series.toString());
        }
        return series;
    }

    /**
     * Gets an instance of from the archive.
     * 
     * @param sopInstanceUID
     *            the SOP instance UID of the instance
     * @return single instance of QCBeanImpl
     */
    private Instance findInstance(String sopInstanceUID) {
        Query query = em.createNamedQuery(Instance.FIND_BY_SOP_INSTANCE_UID_EAGER);
        query.setParameter(1, sopInstanceUID);
        Instance instance = (Instance) query.getSingleResult();
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} : QC info[findInstance] info - "
                    + "Found instance {}", qcSource, instance.toString());
        }
        return instance;
    }

    /**
     * Gets the procedure codes associated with a study.
     * 
     * @param attrs
     *            the study attributes containing the 
     *            ProcedureCodeSequence
     * @return the procedure codes
     */
    private Collection<Code> findProcedureCodes(Attributes attrs) {
        ArrayList<Code> resultCode = new ArrayList<Code>();
        for (Attributes codeAttrs : attrs
                .getSequence(Tag.ProcedureCodeSequence)) {
            Code code = codeService.findOrCreate(new Code(codeAttrs));
            resultCode.add(code);
        }
        return resultCode;
    }

    /**
     * Gets the concept name code associated with an instance.
     * 
     * @param attrs
     *            the attributes of the instance containing the 
     *            ConceptNameCodeSequence
     * @return the concept name code
     */
    private Code findConceptNameCode(Attributes attrs) {
        Attributes codeAttrs  = attrs.getNestedDataset(Tag.ConceptNameCodeSequence);
        Code code = null;
        if (codeAttrs != null)
            try {
        code = codeService.findOrCreate(new Code(codeAttrs));
            } catch (Exception e) {
                LOG.info("Illegal code item in Sequence {}:\n{}",
                        TagUtils.toString(Tag.ConceptNameCodeSequence), codeAttrs);
            }
        return code;
    }

    /**
     * Gets the institutional code for a series.
     * 
     * @param attrs
     *            the attributes of the series containing the
     *            InstitutionCodeSequence
     * @return the institutional code
     */
    private Code findInstitutionalCode(Attributes attrs) {
        Attributes codeAttrs  = attrs.getSequence(Tag.InstitutionCodeSequence).get(0);
        Code code = codeService.findOrCreate(new Code(codeAttrs));
        return code;
    }
    
    /**
     * Gets the issuer of accession number for a study.
     * 
     * @param attrs
     *            the attributes of the study containing
     *            IssuerOfAccessionNumberSequence
     * @return the issuer of accession number
     */
    private Issuer findIssuerOfAccessionNumber(Attributes attrs) {
        Attributes issuerAttrs = attrs.getSequence(Tag.IssuerOfAccessionNumberSequence).get(0);
        Issuer issuer = issuerService.findOrCreate(new Issuer(issuerAttrs));

        return issuer;
    }

    /**
     * Gets the request attributes for a series.
     * The method will remove any extra request attributes
     * in the series that are not in the provided attributes.
     * the method will also add any missing request attributes
     * from the series that are present in the attributes.
     * The method updates the series and returns the 
     * new associated request attributes collection.
     * 
     * @param series
     *            the series
     * @param attrs
     *            the attribte
     * @param arcDevExt
     *            the arc dev ext
     * @param original
     *            the original
     * @return the request attributes
     */
    private Collection<RequestAttributes> findRequestAttributes(Series series,
            Attributes attrs, ArchiveDeviceExtension arcDevExt, Attributes original) {
        
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
                    RequestAttributes tmp = findRequestAttr(oldItem, series);
                    oldRequests.remove(tmp);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{} : QC info[findRequestAttributes] info - "
                                + "Removing Deprecated request attribute"
                                + " {}", qcSource, tmp.toString());
                    }
                }
                
            }
        // add missing ones
        for (Attributes request : updateSequence) {
            if (oldSequence == null
                    || (oldSequence != null && !oldSequence.contains(request))) {
                Issuer issuerOfAccessionNumber = findIssuerOfAccessionNumber(request);
                RequestAttributes newRequest = new RequestAttributes(request,
                        issuerOfAccessionNumber, arcDevExt.getFuzzyStr(),
                        arcDevExt.getNullValueForQueryFields());
                newRequest.setSeries(series);
                oldRequests.add(newRequest);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} : QC info[findRequestAttributes] info - "
                            + "Adding new request attribute"
                            + " {}", qcSource, newRequest.toString());
                }
            }
        }
        return oldRequests;
    }

    /**
     * Gets the request attribute.
     * Helper method that takes one request attribute
     * and looks it up in the database
     * @param attrs
     *            the attributes
     * @param series
     *            the series
     * @return the request attr
     */
    private RequestAttributes findRequestAttr(Attributes attrs, Series series) {
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

    /**
     * Gets the verifying observers from the archive.
     * The method retrieves the verifying observers associated
     * with an instance in the archive.
     * updates the associated verifying observers with those found 
     * in the attributes and associates them with the instance.
     * The method then returns the new (updated) observers.
     * 
     * @param instance
     *            the instance
     * @param attrs
     *            the attrs
     * @param original
     *            the original
     * @param arcDevExt
     *            the arc dev ext
     * @return the verifying observers
     */
    private Collection<VerifyingObserver> findVerifyingObservers(
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
                    VerifyingObserver tmp = findVerifyingObserver(observer,
                            instance, arcDevExt);
                    oldObservers.remove(tmp);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{} : QC info[findVerifyingObservers] info - "
                                + "Removing deprecated verifying observer"
                                + " {}", qcSource, tmp.getVerifyingObserverName());
                    }
                }
            }
        // add missing ones
        for (Attributes observer : verifyingObserversNew) {
            if (verifyingObserversOld == null
                    || (verifyingObserversOld != null && !verifyingObserversOld
                            .contains(observer))) {
                VerifyingObserver newObserver = new VerifyingObserver(observer,
                        arcDevExt.getFuzzyStr(), arcDevExt.getNullValueForQueryFields());
                newObserver.setInstance(instance);
                oldObservers.add(newObserver);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} : QC info[findVerifyingObservers] info - "
                            + "Adding new verifying observer"
                            + " {}", qcSource, newObserver.getVerifyingObserverName());
                }
            }
        }
        return oldObservers;
    }

    /**
     * Gets the verifying observer.
     * Helper method that takes one observer and 
     * looks it up in the archive database.
     * 
     * @param observerAttrs
     *            the observer attrs
     * @param instance
     *            the instance
     * @param arcDevExt
     *            the arc dev ext
     * @return the verifying observer
     */
    private VerifyingObserver findVerifyingObserver(Attributes observerAttrs,
            Instance instance, ArchiveDeviceExtension arcDevExt) {
        Query query = em.createQuery("Select o from VerifyingObserver o "
                + "where o.instance = ?1");
        query.setParameter(1, instance);
        VerifyingObserver foundObserver = (VerifyingObserver) query.getSingleResult(); 
        return foundObserver;
    }

    /**
     * Find or create patientIDs.
     * The method looks for patient IDs in the database
     * if found they are returned else returns null
     * 
     * @param ids
     *            the ids
     * @return the collection
     */
    private Collection<PatientID> findPatientIDs(Collection<IDWithIssuer> ids) {
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

    /**
     * Find or create code.
     * Uses the archive code service to find persisted codes or
     * to create new ones in case they can't be found in the archive.
     * 
     * @param code
     *            the code
     * @return the code found or created
     */
    private Code findOrCreateCode(org.dcm4che3.data.Code code) {
        return codeService.findOrCreate(new Code(code));
    }

    /**
     * Gets the same source study.
     * returns a study if all the provided instances belong to the
     * same sudy, returns null otherwise.
     * 
     * @param toMove
     *            the to move
     * @return the same source study (if applicable)
     */
    private boolean allInstancesFromSameStudy(Collection<Instance> toMove) {
        Study study = null;
        for(Instance instance: toMove) {
            if(study==null)
                study=instance.getSeries().getStudy();
            Study currentStudy = instance.getSeries().getStudy();
            if(!currentStudy.getStudyInstanceUID()
                    .equalsIgnoreCase(study.getStudyInstanceUID()))
                return false;
        }
        return true;
    }

    /**
     * Gets the patient.
     * Gets the patient associated with the provided patient ID.
     * 
     * @param pid
     *            the pid
     * @return the patient
     */
    private Patient findPatient(IDWithIssuer pid) {
        Collection<IDWithIssuer> pids = new ArrayList<IDWithIssuer>();
        pids.add(pid);
        Collection<PatientID> queryIDs = findPatientIDs(pids);
        
        if(queryIDs.isEmpty())
        return null;
        
        Iterator<PatientID> idIterator = queryIDs.iterator();
        
        return idIterator.next().getPatient();
    }

    /**
     * Creates a new study.
     * The method creates a new study while copying all data from the
     * old study and using it to initialize the newly created study.
     * the method also updates the new study with the provided attributes
     * if the attributes are not empty.
     * The method will throw an EJBException if the patient doesn't exist.
     * 
     * @param pid
     *            the pid
     * @param targetStudyUID
     *            the target study uid
     * @param createdStudyAttrs
     *            the created study attrs
     * @return the study created
     */
    private Study createStudy(IDWithIssuer pid, String targetStudyUID,
            Attributes createdStudyAttrs) {
        
        Study study = new Study();
        Patient patient = findPatient(pid);
        if(patient == null) {
            LOG.error("{} : QC info[createStudy] Failure - Study "
                    + "Creation failed, Patient not found",qcSource);
            throw new EJBException();
        }
        study.setPatient(patient);
        if (createdStudyAttrs.contains(Tag.ProcedureCodeSequence)) {
            Collection<Code> procedureCodes = findProcedureCodes(createdStudyAttrs);
            if (!procedureCodes.isEmpty()) {
                study.setProcedureCodes(procedureCodes);
            }
        }
        
        if (createdStudyAttrs.contains(Tag.IssuerOfAccessionNumberSequence)) {
            Issuer issuerOfAccessionNumber = findIssuerOfAccessionNumber(createdStudyAttrs);
            if (issuerOfAccessionNumber != null) {
                study.setIssuerOfAccessionNumber(issuerOfAccessionNumber);
            }
        }
        
        study.setAttributes(createdStudyAttrs,
                archiveDeviceExtension.getAttributeFilter(Entity.Study),
                archiveDeviceExtension.getFuzzyStr(), archiveDeviceExtension.getNullValueForQueryFields());
        
        em.persist(study);
        LOG.info("{}: QC info[createStudy] info - Creating study {}", qcSource, study);
        return study;
    }

    /**
     * Creates a new series.
     * The method creates a new series while copying all data from the
     * old series and using it to initialize the newly created series.
     * the method also updates the new series with the provided attributes
     * if the attributes are not empty.
     * 
     * @param series
     *            the series
     * @param target
     *            the target
     * @param targetSeriesAttrs
     *            the target series attrs
     * @return the series
     */
    private Series createSeries(Series series, Study target, Attributes targetSeriesAttrs) {
        Attributes data = new Attributes(series.getAttributes());
        data.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        if (targetSeriesAttrs != null)
        	data.update(targetSeriesAttrs, data);
        Series newSeries = new Series();
        newSeries.setStudy(target);
        newSeries.setSourceAET(series.getSourceAET());
        newSeries.setInstitutionCode(series.getInstitutionCode());
        
        if(data.contains(Tag.RequestAttributesSequence)) {
        Collection<RequestAttributes> reqAttrs = copyReqAttrs(data.getSequence(Tag.RequestAttributesSequence), newSeries);
        newSeries.setRequestAttributes(reqAttrs);
        }
        
        newSeries.setAttributes(data,
                archiveDeviceExtension.getAttributeFilter(Entity.Series),
                archiveDeviceExtension.getFuzzyStr(), archiveDeviceExtension.getNullValueForQueryFields());
        em.persist(newSeries);
        LOG.info("{}: QC info[createSeries] info - Creating series {}", qcSource, newSeries);
        return newSeries;
    }

    /**
     * Creates a new instance.
     * The method creates a new instance while copying the data from
     * the old instance to be used for initializing the new instance.
     * the method also sets the instance to the series provided.
     * 
     * @param oldinstance
     *            the oldinstance
     * @param series
     *            the series
     * @return the instance
     * @throws DicomServiceException
     *             the dicom service exception
     */
    private Instance createInstance(Instance oldinstance, Series series)
            throws DicomServiceException {
        //update with provided attrs (only attributes in the filter)
        Attributes data = new Attributes(oldinstance.getAttributes());
        data.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        Instance inst = new Instance();
        inst.setSeries(series);
        inst.setConceptNameCode(oldinstance.getConceptNameCode());
        if(data.contains(Tag.ContentSequence)) {
        Collection<ContentItem> newCItems = copyContentItems(
                oldinstance.getContentItems(), inst);
        inst.setContentItems(newCItems);
        }
        
        if(data.contains(Tag.VerifyingObserverSequence)) {
        Collection<VerifyingObserver> newVObservers = copyVerifyingObservers(
                data.getSequence(Tag.VerifyingObserverSequence), inst);
        inst.setVerifyingObservers(newVObservers);
        }
        
        inst.setRetrieveAETs(oldinstance.getRetrieveAETs());
        inst.setAvailability(oldinstance.getAvailability());
        inst.setAttributes(data,
                archiveDeviceExtension.getAttributeFilter(Entity.Instance),
                archiveDeviceExtension.getFuzzyStr(), archiveDeviceExtension.getNullValueForQueryFields());
        em.persist(inst);
        LOG.info("{}: QC info[createInstance] info - Creating instance {}", qcSource, inst);
        return inst;
    }

    /**
     * Copy requested attributes.
     * The method copies the requested attributes from one series
     * to be used by the {@link #createSeries(Series, Study, Attributes)} method.
     * 
     * @param requestedAttrsSeq
     *            the requested attrs seq
     * @param newSeries
     *            the new series
     * @return the collection
     */
    private Collection<RequestAttributes> copyReqAttrs(
            Sequence requestedAttrsSeq, Series newSeries) {
        Collection<RequestAttributes> reqAttrs = new ArrayList<RequestAttributes>();
        for(Attributes attrs : requestedAttrsSeq) {
            RequestAttributes newReqAttr = new RequestAttributes(
                    attrs,attrs.contains(Tag.IssuerOfAccessionNumberSequence)?findIssuerOfAccessionNumber(attrs):null,
                    archiveDeviceExtension.getFuzzyStr(), archiveDeviceExtension.getNullValueForQueryFields());
            newReqAttr.setSeries(newSeries);
            reqAttrs.add(newReqAttr);
        }
        return reqAttrs;
    }


    /**
     * Copy verifying observers.
     * Used by the {@link #createInstance(Instance, Series)}
     * to copy verifying observers associated with the old instance.
     * 
     * @param oldVerifyingObserverSeq
     *            the old verifying observer seq
     * @param inst
     *            the inst
     * @return the collection
     */
    private Collection<VerifyingObserver> copyVerifyingObservers(
            Sequence oldVerifyingObserverSeq, Instance inst) {
        Collection<VerifyingObserver> verifyingObservers = new ArrayList<VerifyingObserver>();
        for(Attributes observer : oldVerifyingObserverSeq) {
            VerifyingObserver newObserver = new VerifyingObserver(
                    observer,archiveDeviceExtension.getFuzzyStr(),
                    archiveDeviceExtension.getNullValueForQueryFields());
            newObserver.setInstance(inst);
            verifyingObservers.add(newObserver);
        }
        return verifyingObservers;
    }

    /**
     * Copy content items.
     * Used by the {@link #createInstance(Instance, Series)} method
     * to copy the content sequence from one instance to another.
     * 
     * @param contentItems
     *            the content items
     * @param newInstance
     *            the new instance
     * @return the collection
     */
    private Collection<ContentItem> copyContentItems(
            Collection<ContentItem> contentItems, Instance newInstance) {
        Collection<ContentItem> newContentItems = new ArrayList<ContentItem>();
        for(ContentItem item: contentItems) {
            ContentItem newItem = new ContentItem();
            newItem.setConceptCode(item.getConceptCode());
            newItem.setConceptName(item.getConceptName());
            newItem.setRelationshipType(item.getRelationshipType());
            newItem.setTextValue(item.getTextValue());
            newItem.setInstance(newInstance);
            newContentItems.add(newItem);
        }
        return newContentItems;
    }

    /**
     * Gets the first file found in the provided collection.
     * Helper method used by the
     * {@link #move(Instance, Series, org.dcm4che3.data.Code)}
     * method to retrieve file reference from either the
     * file alias table or via the file reference relation.
     *  
     * @param refs
     *            the file references
     * @param inst
     *            the instance
     * @return the first file reference found
     * @throws IllegalArgumentException
     *             the illegal argument exception
     */
    private Location getFirstFileRef(Collection<Location> refs, Instance inst)
            throws IllegalArgumentException{
        if(refs.isEmpty()){
            LOG.error("{} : QC info[getFirstFileRef] Failure - Invalid "
                    + "instance {}, must have a referenced file "
                    + "either via fileref or file alias",qcSource, inst);
            throw new IllegalArgumentException();
        } else {
            return refs.iterator().next();
        }
    }

    /**
     * Adds the identical document sequence.
     * Adds an instance reference in the identical document sequence
     * of a KO/SR object.
     * The method will create an identical document sequence if not found.
     * 
     * @param targetIdent
     *            the KO/SR to be updated
     * @param newInstance
     *            the new instance to add reference for in the KO/SR
     */
    private void addIdenticalDocumentSequence(Instance targetIdent, Instance newInstance) {
        
        Attributes identicalDocumentAttributes = targetIdent.getAttributes();
        
        Attributes newStudyItem = new Attributes();
        newStudyItem.setString(Tag.StudyInstanceUID, VR.UI,
                newInstance.getSeries().getStudy().getStudyInstanceUID());
        Sequence SeriesSequence = newStudyItem.newSequence(Tag.ReferencedSeriesSequence, 1);
        Attributes newSeriesItem = new Attributes();
        newSeriesItem.setString(Tag.SeriesInstanceUID, VR.UI,
                newInstance.getSeries().getSeriesInstanceUID());
        Sequence SopSequence = newSeriesItem.newSequence(Tag.ReferencedSOPSequence, 1);
        Attributes newSopAttributes = new Attributes();
        newSopAttributes.setString(Tag.ReferencedSOPInstanceUID, VR.UI,
                newInstance.getSopInstanceUID());
        newSopAttributes.setString(Tag.ReferencedSOPClassUID, VR.UI,
                newInstance.getSopClassUID());
        SopSequence.add(newSopAttributes);
        SeriesSequence.add(newSeriesItem);
        if (identicalDocumentAttributes.contains(Tag.IdenticalDocumentsSequence)) {
        Sequence seq  = identicalDocumentAttributes.getSequence(
                Tag.IdenticalDocumentsSequence);
        if(!seq.contains(newStudyItem))
            seq.add(newStudyItem);
        }
        else {
            Sequence seq = identicalDocumentAttributes.newSequence(
                    Tag.IdenticalDocumentsSequence, 1);
            if(!seq.contains(newStudyItem))
                seq.add(newStudyItem);
        }
        LOG.info("{} : QC info[addIdenticalDocumentSequence] info - "
                + "Added Identical Document Sequence to reference {} "
                + " to instance {}",qcSource, newInstance, targetIdent);
        targetIdent.getAttributesBlob().setAttributes(identicalDocumentAttributes);
    }

    /**
     * Removes the identical document sequence.
     * removes a reference from the identical document sequence of a
     * provided KO/SR.
     * The method will do nothing if no identical document sequence found.
     * 
     * @param removeFromIdent
     *            the KO/SR to be updated
     * @param oldInstance
     *            the old instance to remove reference for in the KO/SR
     */
    private void removeIdenticalDocumentSequence(Instance removeFromIdent, Instance oldInstance) {
        Attributes identicalDocumentAttributes = removeFromIdent.getAttributes();
        for (Attributes identStudyItems : identicalDocumentAttributes
                .getSequence(Tag.IdenticalDocumentsSequence)) {
            for ( Iterator<Attributes> iter =  identStudyItems
                    .getSequence(Tag.ReferencedSeriesSequence).iterator();iter.hasNext();) {
                Attributes identSeriesItems = iter.next();
                for ( Iterator<Attributes> sopIter = identSeriesItems
                        .getSequence(Tag.ReferencedSOPSequence).iterator(); 
                        sopIter.hasNext();) {
                    Attributes identSopItems = sopIter.next();
                    if (identSopItems.getString(Tag.ReferencedSOPInstanceUID)
                            .equalsIgnoreCase(oldInstance.getSopInstanceUID())) {
                        iter.remove();
                        
                    }
                }
            }
        }
        LOG.info("{} : QC info[removeIdenticalDocumentSequence] info - "
                + "Removed Identical Document Sequence item referencing {} "
                + " from instance {}",qcSource, oldInstance, removeFromIdent);
        removeFromIdent.setAttributes(identicalDocumentAttributes, archiveDeviceExtension
                .getAttributeFilter(Entity.Instance),
                archiveDeviceExtension.getFuzzyStr(), archiveDeviceExtension.getNullValueForQueryFields());
    }

    /**
     * Gets the identical document referenced instances.
     * Retrieves a list of all instances referenced 
     * in the identical document sequence within the instance
     * @param inst
     *            the instance containing the identical document sequence
     * @return the identical document referenced instances collection
     */
    private Collection<Instance> findIdenticalDocumentReferences(Instance inst) {
        Collection<Instance> foundIdents = new ArrayList<Instance>();
        Attributes attrs = inst.getAttributes();
        if(attrs.contains(Tag.IdenticalDocumentsSequence)) {
            for(Attributes studyItems : attrs.getSequence(Tag.IdenticalDocumentsSequence)) {
                for(Attributes seriesItems : studyItems.getSequence(Tag.ReferencedSeriesSequence)) {
                    for(Attributes sopItems : seriesItems.getSequence(Tag.ReferencedSOPSequence)) {
                        Instance newInstance = findInstance(sopItems.getString(Tag.ReferencedSOPInstanceUID));
                        foundIdents.add(newInstance);
                        }
                    }
                }
            }
        return foundIdents;
    }

    /**
     * Gets the reference state.
     * Returns a state depending on the provided instances and the references
     * within the current requested procedure evidence sequence.
     * If the instances were all moved (they are all referenced)
     * {@link ReferenceState#ALLMOVED} is returned.
     * If some of the instances were moved (not all moved were referenced or
     * more instances were referenced than those moved)
     * {@link ReferenceState#SOMEMOVED} is returned.
     * If none of the provided instances were referenced 
     * {@link ReferenceState#NONEMOVED} is returned.
     * 
     * @param studyInstanceUID
     *            the study instance uid
     * @param series
     *            the series
     * @param sourceUIDs
     *            the moved instances
     * @return the reference state
     */
    private ReferenceState findReferenceState(String studyInstanceUID, Instance inst,
            Collection<QCEventInstance> sourceUIDs, String seriesType) {
            Sequence currentEvidenceSequence = inst.getAttributes()
                    .getSequence(Tag.CurrentRequestedProcedureEvidenceSequence);
            Sequence referencedSeriesSeq = inst.getAttributes()
            .getSequence(Tag.ReferencedSeriesSequence);
            Collection<String> allReferencedSopUIDs = seriesType.equalsIgnoreCase("PR")? findAggregatedReferencedSopInstancesForSeries(referencedSeriesSeq): findAggregatedReferencedSopInstancesForStudy(
                    studyInstanceUID, currentEvidenceSequence);
            int allReferencesCount=allReferencedSopUIDs.size();
            if(allReferencesCount==0)
                return ReferenceState.NONEMOVED;
            for(QCEventInstance moved : sourceUIDs) {
                if(allReferencedSopUIDs.contains(moved.getSopInstanceUID()))
                    allReferencesCount--;
            }
            if(allReferencesCount == 0) {
                return ReferenceState.ALLMOVED;
            }
            else if(allReferencesCount < allReferencedSopUIDs.size()
                    && allReferencesCount > 0){
                return ReferenceState.SOMEMOVED;
            }
            
        return ReferenceState.NONEMOVED;
        }

    /**
     * Gets the aggregated referenced SOP instances for study.
     * Helper method used to get all the referenced instances found
     * in the current requested procedure evidence sequence.
     * 
     * @param studyInstanceUID
     *            the study instance UID
     * @param currentEvidenceSequence
     *            the current evidence sequence
     * @return the aggregated referenced SOP instances for study
     */
    private Collection<String> findAggregatedReferencedSopInstancesForStudy(
            String studyInstanceUID, Sequence currentEvidenceSequence) {
        Collection<String> aggregatedSopUIDs = new ArrayList<String>();
        for(Attributes studyItems : currentEvidenceSequence) {
            if(studyItems.getString(Tag.StudyInstanceUID).equalsIgnoreCase(studyInstanceUID)) {
                for(Attributes seriesItems : studyItems.getSequence(Tag.ReferencedSeriesSequence)) {
                    for(Attributes sopItems : seriesItems.getSequence(Tag.ReferencedSOPSequence)) {
                        aggregatedSopUIDs.add(sopItems.getString(Tag.ReferencedSOPInstanceUID));
                    }
                }
            }
    }
        return aggregatedSopUIDs;
    }

    private Collection<String> findAggregatedReferencedSopInstancesForSeries(
            Sequence referencedSeriesSequence) {
        Collection<String> aggregatedSopUIDs = new ArrayList<String>();
        for(Attributes seriesItems : referencedSeriesSequence) {
            for(Attributes sopItems : seriesItems.getSequence(Tag.ReferencedImageSequence)) {
                aggregatedSopUIDs.add(sopItems.getString(Tag.ReferencedSOPInstanceUID));
            }
        }
        return aggregatedSopUIDs;
    }
    /**
     * Find series KO/PR/SR.
     * Returns all series found within the study with the modality
     * types of KO (key objects), SR (structured reports) and
     * PS (Presentation state).
     * 
     * @param parentStudy
     *            the parent study
     * @return the collection
     */
    private Collection<Series> findSeriesKOPRSR(Study parentStudy) {
        Collection<Series> seriesColl = new ArrayList<Series>(); 
        for(Series series: parentStudy.getSeries()) {
            if(series.getModality().equalsIgnoreCase("KO")
                    || series.getModality().equalsIgnoreCase("SR")
                    || series.getModality().equalsIgnoreCase("PR")) {
                seriesColl.add(series);
            }
        }
        return seriesColl;
    }

    /**
     * Handle KO/PR/SR.
     * Uses the referenced state retrieved from 
     * {@link #getReferenceState(String, Series, Collection)} 
     * to decide whither a clone or a move is to be applied on 
     * the found invisible objects.
     * The method also takes care of updating the identical document sequence
     * for third party objects as well as for the cloned objects. 
     * @param targetSeriesAttrs
     *            the target series attrs
     * @param qcRejectionCode
     *            the qc rejection code
     * @param studyHistory
     *            the study history
     * @param sourceUIDs
     *            the source ui ds
     * @param targetUIDs
     *            the target ui ds
     * @param sourceStudy
     *            the source study
     * @param targetStudy
     *            the target study
     * @param oldToNewSeries
     *            the old to new series
     * @return the collection
     */
    private Collection<QCInstanceHistory> handleKOPRSR(Attributes targetSeriesAttrs,
            org.dcm4che3.data.Code qcRejectionCode,
            QCStudyHistory studyHistory, Collection<QCEventInstance> sourceUIDs,
            Collection<QCEventInstance> targetUIDs, Study sourceStudy,
            Study targetStudy, HashMap<String, NewSeriesTuple> oldToNewSeries) {
        sourceStudy = em.find(Study.class, sourceStudy.getPk());
        Collection<QCInstanceHistory> instancesHistory = new ArrayList<QCInstanceHistory>();
        Series newSeries;
        Collection<Series> seriesKOPRSR = findSeriesKOPRSR(sourceStudy);
         for(Series series: seriesKOPRSR) {
             for(Instance inst:series.getInstances()) {
                 ReferenceState referenceState = null;
                 if(series.getModality().equalsIgnoreCase("PR")) {
                     referenceState = findReferenceState(
                             sourceStudy.getStudyInstanceUID(),inst,sourceUIDs, "PR"); 
                 }
                 else {
                     referenceState = findReferenceState(
                             sourceStudy.getStudyInstanceUID(),inst,sourceUIDs, "KOSR");
                 }
                 
                 Instance newInstance ;

                 QCInstanceHistory instanceHistory;
                 switch (referenceState) {
                 
                case ALLMOVED:
                    LOG.info("{} : QC info[handleKOPRSR] info - "
                            + " All referenced items within {} moved "
                            + " attempting to move {}",
                            qcSource, series.getModality(), series.getModality());
                    if(!oldToNewSeries.keySet().contains(
                            inst.getSeries().getSeriesInstanceUID())) {
                        newSeries= createSeries(inst.getSeries(), targetStudy, null);
                        QCSeriesHistory seriesHistory = createQCSeriesHistory(series.getSeriesInstanceUID(),
                                series.getAttributes(), studyHistory);
                        oldToNewSeries.put(inst.getSeries().getSeriesInstanceUID(), 
                                new NewSeriesTuple(newSeries.getPk(),seriesHistory));
                    }
                    else {
                        newSeries = em.find(Series.class,oldToNewSeries.get(
                                inst.getSeries().getSeriesInstanceUID()).getPK());
                    }
                    newInstance= move(inst,newSeries,qcRejectionCode);
                    instanceHistory = new QCInstanceHistory(
                            targetStudy.getStudyInstanceUID(),  newSeries.getSeriesInstanceUID(),
                            inst.getSopInstanceUID(), newInstance.getSopInstanceUID(),
                            newInstance.getSopInstanceUID(), false);
                    instanceHistory.setSeries(oldToNewSeries.get(inst.getSeries()
                            .getSeriesInstanceUID()).getSeriesHistory());
                    instancesHistory.add(instanceHistory);
                    targetUIDs.add(new QCEventInstance(newInstance.getSopInstanceUID(), newSeries.getSeriesInstanceUID(), targetStudy.getStudyInstanceUID()));
                    sourceUIDs.add(new QCEventInstance(inst.getSopInstanceUID(), series.getSeriesInstanceUID(), sourceStudy.getStudyInstanceUID()));
                    if(series.getModality().equalsIgnoreCase("KO") ||
                            series.getModality().equalsIgnoreCase("SR")) {
                    for(Instance ident: findIdenticalDocumentReferences(newInstance)) {
                        removeIdenticalDocumentSequence(ident, inst);
                        removeIdenticalDocumentSequence(inst,ident);
                        addIdenticalDocumentSequence(ident, newInstance);
                        //addIdenticalDocumentSequence(newInstance, ident); already there
                        if(!identicalDocumentSequenceHistoryExists(ident, studyHistory.getAction())
                                && !ident.getSeries().getStudy().getStudyInstanceUID()
                                .equalsIgnoreCase(sourceStudy.getStudyInstanceUID()))
                            addIdenticalDocumentSequenceHistory(ident, studyHistory.getAction());
                    }
                    }
                    break;
                    
                case SOMEMOVED:
                    LOG.info("{} : QC info[handleKOPRSR] info - "
                            + " Some referenced items within {} moved "
                            + " attempting to create a copy {}",
                            qcSource, series.getModality(), series.getModality());
                    if(!oldToNewSeries.keySet().contains(
                            inst.getSeries().getSeriesInstanceUID())) {
                        newSeries= createSeries(inst.getSeries(), targetStudy, null);
                        QCSeriesHistory seriesHistory = createQCSeriesHistory(series.getSeriesInstanceUID(),
                                series.getAttributes(), studyHistory);
                        oldToNewSeries.put(inst.getSeries().getSeriesInstanceUID(), 
                                new NewSeriesTuple(newSeries.getPk(),seriesHistory));
                    }
                    else {
                        newSeries = em.find(Series.class,oldToNewSeries.get(
                                inst.getSeries().getSeriesInstanceUID()).getPK());
                    }
                    newInstance = clone(inst,newSeries);
                    inst = em.find(Instance.class, inst.getPk());
                    newInstance = em.find(Instance.class, newInstance.getPk());
                    instanceHistory = new QCInstanceHistory(
                            targetStudy.getStudyInstanceUID(), newSeries.getSeriesInstanceUID(),
                            inst.getSopInstanceUID(), newInstance.getSopInstanceUID(),
                            newInstance.getSopInstanceUID(), true);
                    instanceHistory.setSeries(oldToNewSeries.get(inst.getSeries()
                            .getSeriesInstanceUID()).getSeriesHistory());
                    instancesHistory.add(instanceHistory);
                    targetUIDs.add(new QCEventInstance(newInstance.getSopInstanceUID(), newSeries.getSeriesInstanceUID(), targetStudy.getStudyInstanceUID()));
                    sourceUIDs.add(new QCEventInstance(inst.getSopInstanceUID(), series.getSeriesInstanceUID(), sourceStudy.getStudyInstanceUID()));
                    if(series.getModality().equalsIgnoreCase("KO") ||
                            series.getModality().equalsIgnoreCase("SR")) {
                    addIdenticalDocumentSequence(newInstance, inst);
                    addIdenticalDocumentSequence(inst, newInstance);
                    
                    for(Instance ident: findIdenticalDocumentReferences(newInstance)) {
                        if(ident.getPk()!=inst.getPk()) {
                        addIdenticalDocumentSequence(ident, newInstance);
//                        addIdenticalDocumentSequence(newInstance, ident); already exists
                        if(!identicalDocumentSequenceHistoryExists(ident, studyHistory.getAction())
                                && !ident.getSeries().getStudy().getStudyInstanceUID()
                                .equalsIgnoreCase(sourceStudy.getStudyInstanceUID()))
                            addIdenticalDocumentSequenceHistory(ident, studyHistory.getAction());
                        }
                    }
                    }
                    break;
                    
                case NONEMOVED:
                    LOG.info("{} : QC info[handleKOPRSR] info - "
                            + " No referenced items within {} where "
                            + "moved during the operation, no invisible object handling required",
                            qcSource, series.getModality());
                    break;
                default:
                    break;
                }
             }
         }
         return instancesHistory;
    }

    /**
     * Generate QC action.
     * Creates a new QCAction History and persists it.
     * 
     * @param operation
     *            the operation
     * @return the QC action history
     */
    private QCActionHistory generateQCAction(QCOperation operation) {
        QCActionHistory action = new QCActionHistory();
        action.setCreatedTime(new Date());
        action.setAction(operation.toString());
        em.persist(action);
        return action;
    }

    /**
     * Record history entry.
     * Persists the provided history instances.
     * Takes care to call {@link #updateOldHistoryRecords(Collection)}
     * 
     * @param instances
     *            the instances to be persisted
     */
    private void recordHistoryEntry(Collection<QCInstanceHistory> instances) {
        updateOldHistoryRecords(instances);
        for(QCInstanceHistory instance : instances) {
            LOG.debug("{} : QC info[recordHistoryEntry] info - "
                    + "Adding history entry {}",qcSource,instance.toString());
            em.persist(instance);
        }
    }

    /**
     * Update old history records.
     * Updates the current for the instance, series and study
     * for the old instance history records changed by the qc operation.
     * 
     * @param instanceRecords
     *            the instance records to be updated
     */
    private void updateOldHistoryRecords(Collection<QCInstanceHistory> instanceRecords) {
        Collection<QCInstanceHistory> associatedRecords = new ArrayList<QCInstanceHistory>();
        for(QCInstanceHistory newInstanceRecord : instanceRecords) {
            if(!newInstanceRecord.isCloned()) {
                if(!newInstanceRecord.getSeries().getStudy().getAction().getAction().equalsIgnoreCase(QCOperation.REJECT.name())) {
                    associatedRecords = findInstanceHistoryByCurrentUID(newInstanceRecord.getOldUID());
                    for(QCInstanceHistory associatedRecord : associatedRecords) {
                        if(associatedRecord.getNextUID().equalsIgnoreCase(associatedRecord.getCurrentUID())) {
                            associatedRecord.setNextUID(newInstanceRecord.getOldUID());
                }
                        associatedRecord.setCurrentUID(newInstanceRecord.getCurrentUID());
                        associatedRecord.setCurrentSeriesUID(newInstanceRecord.getCurrentSeriesUID());
                        associatedRecord.setCurrentStudyUID(newInstanceRecord.getCurrentStudyUID());
                        }
                    }
                }
            }
        if(associatedRecords.isEmpty())
            LOG.debug("{} : QC info[updateOldHistoryRecords] info - No associated history"
                    + " records found, no history update required", qcSource);
        for(QCInstanceHistory record : associatedRecords) {
            LOG.info("{} : QC info[updateOldHistoryRecords] info - Updating the following QCHistory Records"
                    + " for referential integrity :\n", qcSource);
            LOG.info(record.toString());
        }
    }

    /**
     * Gets the records by current UID.
     * Retrieves an instance history record by the current UID.
     * 
     * @param oldUID
     *            the old UID
     * @return the records found by current UID
     */
    @SuppressWarnings("unchecked")
    private Collection<QCInstanceHistory> findInstanceHistoryByCurrentUID(String oldUID) {
        Query query = em.createNamedQuery(QCInstanceHistory.FIND_BY_CURRENT_UID);
        query.setParameter(1, oldUID);
        return query.getResultList();
    }

    /**
     * Record update history entry.
     * Creates an update History entry and persists it.
     * The method takes care to set the attributes of the
     * history entry to the old attributes to allow for undo.
     *  
     * @param action
     *            the action
     * @param scope
     *            the scope
     * @param unmodified
     *            the unmodified
     */
    private void addUpdateHistoryEntry(QCActionHistory action, 
            QCUpdateScope scope, Attributes unmodified, String patientPK) {
        QCUpdateHistory qcUpdateHistory = new QCUpdateHistory();
        qcUpdateHistory.setCreatedTime(new Date());
        qcUpdateHistory.setScope(scope);
        qcUpdateHistory.setUpdatedAttributesBlob(new AttributesBlob(unmodified));
        switch (scope) {
        case PATIENT:
            qcUpdateHistory.setObjectUID(patientPK);
            break;
        case STUDY:
            qcUpdateHistory.setObjectUID(unmodified.getString(Tag.StudyInstanceUID));
            break;
        case SERIES:
            qcUpdateHistory.setObjectUID(unmodified.getString(Tag.SeriesInstanceUID));
            break;
        case INSTANCE:
            qcUpdateHistory.setObjectUID(unmodified.getString(Tag.SOPInstanceUID));
            break;
        default:
            LOG.error("{} : QC info[adUpdateHistoryEntry] Failure -  "
                    + "Unsupported Scode provided to update history creation", qcSource);
            throw new EJBException();
        }
        QCUpdateHistory prevHistoryNode = findPreviousHistoryNode(
                qcUpdateHistory.getObjectUID());
        if(prevHistoryNode != null) {
            prevHistoryNode.setNext(qcUpdateHistory);
        }
        em.persist(qcUpdateHistory);

    }

    /**
     * Filter QCed
     * Removes QCed instances form a collection and returns 
     * the updated collection.
     * Used by the restore method to not restore QCed instances.
     * 
     * @param instances
     *            the instances
     * @return the collection
     */
    private Collection<Instance> filterQCed(Collection<Instance> instances) {
        Collection<Instance> instancesFiltered = instances;
        for(Iterator<Instance> iter = instancesFiltered.iterator(); iter.hasNext();) {
            Instance inst = iter.next();
            if(isQCed(inst.getSopInstanceUID()))
                iter.remove();
            LOG.debug("{} : QC info[filterQCed] Failure - "
                    + " Unable to restore {} -  still QCed",qcSource, inst);
        }
        return instancesFiltered;
    }

    /**
     * Checks if an instance is QCed.
     * Helper method for {@link #filterQCed(Collection)}
     * 
     * @param sopInstanceUID
     *            the sop instance uid
     * @return true, if is q ced
     */
    private boolean isQCed(String sopInstanceUID) {
        Query query = em.createNamedQuery(QCInstanceHistory.FIND_BY_OLD_UID);
        query.setParameter(1, sopInstanceUID);
        return query.getResultList().size()==1?false:true;
    }


    /**
     * Gets the previous history node.
     * Used to set the next pk in a chain of updates.
     * 
     * @param objectUID
     *            the object uid
     * @return the prev history node
     */
    private QCUpdateHistory findPreviousHistoryNode(String objectUID) {
        Query query = em.createNamedQuery(QCUpdateHistory.FIND_FIRST_UPDATE_ENTRY);
        query.setParameter(1, objectUID);
        QCUpdateHistory result=null;
        if(query.getResultList().size()>0)
         result= (QCUpdateHistory) query.getSingleResult();

        return result;  
    }

    /**
     * Adds the identical document sequence history.
     * Adds a history entry for third party studies, series and instances 
     * with identical documents to the QCed ones
     * 
     * @param ident
     *            the instance containing the 
     *            identical document sequence
     * @param identHistoryAction
     *            the history action for the operation
     */
    private void addIdenticalDocumentSequenceHistory(Instance ident, 
            QCActionHistory identHistoryAction) {
        QCStudyHistory identStudyHistory = createQCStudyHistory(
                ident.getSeries().getStudy().getStudyInstanceUID(), 
                ident.getSeries().getStudy().getStudyInstanceUID(), 
                new Attributes(), identHistoryAction);
        QCSeriesHistory identSeriesHistory = createQCSeriesHistory(
                ident.getSeries().getSeriesInstanceUID(), 
                new Attributes(), identStudyHistory);
        QCInstanceHistory identInstanceHistory = new QCInstanceHistory(
                ident.getSopInstanceUID(), identSeriesHistory.getOldSeriesUID(),
                ident.getSopInstanceUID(), ident.getSopInstanceUID(),
                ident.getSopInstanceUID(), false);
        identInstanceHistory.setPreviousAtributesBlob(ident.getAttributesBlob());
        identInstanceHistory.setSeries(identSeriesHistory);
        em.persist(identInstanceHistory);
        LOG.info("{} : QC info[addIdenticalDocumentSequence] info - "
                + " Added identical document sequence history entry for {}",
                qcSource, ident);
    }

    /**
     * Identical document sequence history exists.
     * Checks for the existence of a history entry
     * with identical 
     * @param ident
     *            the instance containing 
     *            the identical document sequence
     * @param identHistoryAction
     *            the history action for the operation
     * @return true, if successful
     */
    private boolean identicalDocumentSequenceHistoryExists(Instance ident,
            QCActionHistory identHistoryAction) {
        QCInstanceHistory identHistory = findIdenticalDocumentHistory(ident, identHistoryAction);
        return identHistory==null ? false : true;
    }

    /**
     * Find a history instance for an identical document.
     * Returns the instance if found.
     * Used to check if the instance was already persisted in the same action.
     * 
     * @param ident
     *            the ident
     * @param identHistoryAction
     *            the ident history action
     * @return the QC instance history
     */
    private QCInstanceHistory findIdenticalDocumentHistory(Instance ident, 
            QCActionHistory identHistoryAction) {
        QCInstanceHistory result;
        Query query = em.createNamedQuery(QCInstanceHistory.FIND_BY_CURRENT_UID_FOR_ACTION);
        query.setParameter(1, ident.getSopInstanceUID());
        query.setParameter(2, identHistoryAction.getAction());
        try {
            result = (QCInstanceHistory) query.getSingleResult();
        }
        catch (NoResultException e) {
            LOG.error("{} : QC info[findIdenticalDocumentHistory] Failure - "
                    + " Unable to find identical document sequence history ",
                    qcSource);
            result = null;
        }
        return result;
    }

    
    public Instance createAndStoreRejectionNote(org.dcm4che3.data.Code rejectionCode, Collection<Instance> instances) {
        if (instances != null && instances.size() > 0) {
            Attributes rejNote = createRejectionNote(rejectionCode, instances);
            ArchiveAEExtension arcAEExt = null;
            for (ApplicationEntity ae : device.getApplicationEntities()) {
                if (ae.isInstalled()) {
                    arcAEExt = ae.getAEExtension(ArchiveAEExtension.class);
                    if (arcAEExt != null && arcAEExt.getStorageSystemGroupID() != null)
                        break;
                    arcAEExt = null;
                }
            }
            if (arcAEExt == null) {
                LOG.error("No ApplicationEntity found for this service to store RejectionNote locally (must be installed and StorageSystemGroup)");
                throw new EJBException("Can not store RejectionNote after QC operation! No Application Entity found in device "+device.getDeviceName());
            }
            try {
                List<Connection> conns = arcAEExt.getApplicationEntity().getConnections();
                String hostname = conns.isEmpty() ? "UNKNOWN" : conns.get(0).getHostname();
                StoreSession session = storeService.createStoreSession(storeService); 
                session.setSource(new GenericParticipant(hostname, "QCAction"));
                session.setRemoteAET(arcAEExt.getApplicationEntity().getAETitle());
                session.setArchiveAEExtension(arcAEExt);
                storeService.initBulkdataStorage(session);
                storeService.initSpoolingStorage(session);
                StoreContext context = storeService.createStoreContext(session);
                Attributes fmi = new Attributes();
                fmi.setString(Tag.TransferSyntaxUID, VR.UI, UID.ImplicitVRLittleEndian);
                storeService.writeSpoolFile(context, fmi, rejNote);
                storeService.store(context);
                LOG.debug("RejectionNote stored! instance:{}", context.getInstance());
                return context.getInstance();
            } catch (DicomServiceException x) {
                LOG.error("Failed to store RejectionNote!", x);
                throw new EJBException(x);
            }
        }
        return null;
    }

    public Attributes createRejectionNote(org.dcm4che3.data.Code rejectionCode, Collection<Instance> instances) {
        Attributes kos = createKOS(rejectionCode, instances.iterator().next());
        Sequence evidenceSeq = kos.newSequence(Tag.CurrentRequestedProcedureEvidenceSequence, 1);
        Sequence contentSeq = kos.newSequence(Tag.ContentSequence, 1);
        HashMap<Long, Attributes> mapEvidenceItem = new HashMap<Long, Attributes>();
        HashMap<Long, Attributes> mapRefSeriesItem = new HashMap<Long, Attributes>();
        Attributes evidenceItem, refSeriesItem, contentItem;
        Sequence refSeriesSeq;
        for (Instance inst : instances) {
            evidenceItem = mapEvidenceItem.get(inst.getSeries().getStudy().getPk());
            if (evidenceItem == null) {
                evidenceItem = new Attributes();
                evidenceItem.setString(Tag.StudyInstanceUID, VR.UI, inst.getSeries().getStudy().getStudyInstanceUID());
                evidenceItem.newSequence(Tag.ReferencedSeriesSequence, 1);
                evidenceSeq.add(evidenceItem);
                mapEvidenceItem.put(inst.getSeries().getStudy().getPk(), evidenceItem);
            }
            refSeriesSeq = evidenceItem.getSequence(Tag.ReferencedSeriesSequence);
            refSeriesItem = mapRefSeriesItem.get(inst.getSeries().getPk());
            if (refSeriesItem == null) {
                refSeriesItem = new Attributes();
                refSeriesItem.setString(Tag.SeriesInstanceUID, VR.UI, inst.getSeries().getSeriesInstanceUID());
                refSeriesItem.newSequence(Tag.ReferencedSOPSequence, 1);
                refSeriesSeq.add(refSeriesItem);
                mapRefSeriesItem.put(inst.getSeries().getPk(), refSeriesItem);
            }
            addReferencedSopSeqItem(refSeriesItem, inst);
            
            contentItem = new Attributes();
            contentItem.setString(Tag.ValueType, VR.CS, getValueType(inst.getSopClassUID()));
            contentItem.setString(Tag.RelationshipType, VR.CS, "CONTAINS");
            contentItem.newSequence(Tag.ReferencedSOPSequence, 1);
            addReferencedSopSeqItem(contentItem, inst);
            contentSeq.add(contentItem);
        }
        return kos;
    }
    
    private void addReferencedSopSeqItem(Attributes attrs, Instance inst) {
        Attributes refSopItem = new Attributes();
        refSopItem.setString(Tag.ReferencedSOPInstanceUID, VR.UI, inst.getSopInstanceUID());
        refSopItem.setString(Tag.ReferencedSOPClassUID, VR.UI, inst.getSopClassUID());
        attrs.getSequence(Tag.ReferencedSOPSequence).add(refSopItem);
    }
        
    private Attributes createKOS(org.dcm4che3.data.Code rejectionCode, Instance instance) {
        Attributes attrs = instance.getSeries().getStudy().getPatient().getAttributes();
        attrs.addAll(instance.getSeries().getStudy().getAttributes());
        Attributes kos = new Attributes(attrs, PATIENT_AND_STUDY_ATTRS);
        kos.setString(Tag.SOPClassUID, VR.UI, UID.KeyObjectSelectionDocumentStorage);
        kos.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        kos.setDate(Tag.ContentDateAndTime, new Date());
        kos.setString(Tag.Modality, VR.CS, "KO");
        kos.setNull(Tag.ReferencedPerformedProcedureStepSequence, VR.SQ);
        kos.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        kos.setString(Tag.SeriesNumber, VR.IS, "999");
        kos.setString(Tag.SeriesDescription, VR.LO, "Rejection Note");
        kos.setString(Tag.InstanceNumber, VR.IS, "1");
        kos.setString(Tag.ValueType, VR.CS, "CONTAINER");
        kos.setString(Tag.ContinuityOfContent, VR.CS, "SEPARATE");
        kos.newSequence(Tag.ConceptNameCodeSequence, 1).add(rejectionCode.toItem());
        Attributes tmplItem = new Attributes(2);
        tmplItem.setString(Tag.MappingResource, VR.CS, "DCMR");
        tmplItem.setString(Tag.TemplateIdentifier, VR.CS, "2010");
        kos.newSequence(Tag.ContentTemplateSequence, 1).add(tmplItem);
        return kos;
    }
 
    private static String getValueType(String sopClassUID) {
        RecordType rt = recordFactory.getRecordType(sopClassUID);
        return (rt == RecordType.IMAGE || rt == RecordType.WAVEFORM) ? rt.name() : "COMPOSITE";
    }

    /**
     * A tuple that carries a series instance UID for a new series as well as a
     * QCSeriesHistory entry Used to associate one series only to one history
     * entry to be passed to each instance history created of the same series.
     * 
     * @author Hesham Elbadawi <bsdreko@gmail.com>
     */
    class NewSeriesTuple {
        private long pk;
        private QCSeriesHistory seriesHistory;

        /**
         * Instantiates a new new series tuple.
         */
        NewSeriesTuple() {
        }
        
        /**
         * Instantiates a new new series tuple.
         * 
         * @param pk
         *            the pk
         * @param seriesHistory
         *            the series history
         */
        NewSeriesTuple(long pk, QCSeriesHistory seriesHistory) {
            this.pk = pk;
            this.seriesHistory = seriesHistory;
        }

        /**
         * Gets the pk.
         * 
         * @return the pk
         */
        public long getPK() {
            return pk;
        }
        
        /**
         * Sets the pk.
         * 
         * @param pk
         *            the new pk
         */
        public void setPK(long pk) {
            this.pk = pk;
        }
        
        /**
         * Gets the series history.
         * 
         * @return the series history
         */
        public QCSeriesHistory getSeriesHistory() {
            return seriesHistory;
        }
        
        /**
         * Sets the series history.
         * 
         * @param seriesHistory
         *            the new series history
         */
        public void setSeriesHistory(QCSeriesHistory seriesHistory) {
            this.seriesHistory = seriesHistory;
        }
        
    }
    class PatientAttrsPKTuple{
        long pk;
        Attributes unmodified;
        PatientAttrsPKTuple(long pk, Attributes attrs) {
            this.pk= pk;
            this.unmodified = attrs;
        }
        public long getPK() {
            return this.pk;
        }
        public Attributes getUnModifiedAttrs() {
            return this.unmodified;
        }
    }
}
