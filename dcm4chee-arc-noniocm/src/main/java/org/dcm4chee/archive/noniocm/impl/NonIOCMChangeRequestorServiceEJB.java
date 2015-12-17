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

package org.dcm4chee.archive.noniocm.impl;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.NoneIOCMChangeRequestorExtension;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.dto.ActiveService;
import org.dcm4chee.archive.entity.ActiveProcessing;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.QCActionHistory;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.entity.QCSeriesHistory;
import org.dcm4chee.archive.entity.QCStudyHistory;
import org.dcm4chee.archive.noniocm.NonIOCMChangeRequestorService;
import org.dcm4chee.archive.noniocm.NonIocmChangeRequestorMDB;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
import org.dcm4chee.archive.patient.PatientSelectorFactory;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.processing.ActiveProcessingService;
import org.dcm4chee.archive.qc.QCEvent.QCOperation;
import org.dcm4chee.archive.qc.QCOperationNotPermittedException;
import org.dcm4chee.archive.qc.StructuralChangeService;
import org.dcm4chee.archive.sc.STRUCTURAL_CHANGE;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.store.session.StudyUpdatedEvent;
import org.dcm4chee.archive.studyprotection.StudyProtectionHook;
import org.dcm4chee.hooks.Hooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@Stateless
public class NonIOCMChangeRequestorServiceEJB implements NonIOCMChangeRequestorService {

    //Array must be sorted!
    private static final int[] BASIC_CHG_ATTRIBUTES = new int[]{Tag.SOPInstanceUID, Tag.PatientID,Tag.IssuerOfPatientID, Tag.IssuerOfPatientIDQualifiersSequence, Tag.StudyInstanceUID, Tag.SeriesInstanceUID};

    private static final List<ActiveService> NON_IOCM_ACTIVE_SERVICES = Arrays.asList(ActiveService.NON_IOCM_UPDATE);

    private static final Logger LOG = LoggerFactory.getLogger(NonIOCMChangeRequestorServiceEJB.class);

    @Inject
    private ActiveProcessingService activeProcessingService;
    
    @Inject
    private StructuralChangeService scService;
    
    @Inject
    private Hooks<StudyProtectionHook> studyProtectionHooks;

    @Inject
    private PatientService patientService;

    @Inject
    private CodeService codeService;

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory connFactory;

    @Resource(mappedName = "java:/queue/noneiocm")
    private Queue noniocmQueue;

    @Inject
    private Device device;

    @PersistenceContext(name="dcm4chee-arc")
    private EntityManager em;

    @Override
    public boolean isNonIOCMChangeRequestor(String callingAET) {
        return getNonIOCMDevice(callingAET) != null;
    }

    @Override
    public boolean isNonIOCMChangeRequest(String callingAET, String sourceAET) {
        Device d = getNonIOCMDevice(callingAET);
        return d == null ? false : callingAET.equals(sourceAET) ? true : d.getApplicationAETitles().contains(sourceAET);
    }

    @Override
    public int getNonIOCMModalityGracePeriod(String callingAET) {
        NoneIOCMChangeRequestorExtension ext = device.getDeviceExtension(NoneIOCMChangeRequestorExtension.class);
        if (ext == null || ext.getNoneIOCMModalityDevices().isEmpty()) {
            LOG.debug("No NoneIOCMModalityDevices configured!");
            return Integer.MIN_VALUE;
        }
        for (Device d : ext.getNoneIOCMModalityDevices()) {
            if (d.getApplicationAETitles().contains(callingAET)) 
                return ext.getGracePeriod();
        }
        return -1;
    }

    private Device getNonIOCMDevice(String callingAET) {
        if (callingAET != null) {
            NoneIOCMChangeRequestorExtension ext = device.getDeviceExtension(NoneIOCMChangeRequestorExtension.class);
            if (ext == null || ext.getNoneIOCMChangeRequestorDevices().isEmpty()) {
                LOG.debug("No NoneIOCMChangeRequestorDevices configured!");
                return null;
            }
            for (Device d : ext.getNoneIOCMChangeRequestorDevices()) {
                if (d != null && d.getApplicationAETitles().contains(callingAET)) {
                    return d;
                }
            }
        }
        return null;
    }

    @Override
    public NonIOCMChangeType getChangeType(Instance inst, Attributes attrs) {
        Attributes patAttrs = inst.getSeries().getStudy().getPatient().getAttributes();
        IDWithIssuer currentPID = IDWithIssuer.pidOf(patAttrs);
        return getChangeType(currentPID, inst.getSeries().getStudy().getStudyInstanceUID(), inst.getSeries().getSeriesInstanceUID(), inst.getSopInstanceUID(), attrs);
    }
    
    private NonIOCMChangeType getChangeType(IDWithIssuer currentPID, String studyIUID, String seriesIUID, String sopIUID, Attributes attrs) {
        if (!sopIUID.equals(attrs.getString(Tag.SOPInstanceUID)))
            throw new IllegalArgumentException("Current and new Instance must have the same SOP Instance UID!");
        boolean seriesChg = !seriesIUID.equals(attrs.getString(Tag.SeriesInstanceUID));
        boolean studyChg = !studyIUID.equals(attrs.getString(Tag.StudyInstanceUID));
        boolean patIDChg = !currentPID.equals(IDWithIssuer.pidOf(attrs));
        if (patIDChg) {
            if (studyChg || seriesChg) {
                LOG.warn("Illegal NoneICOM PatID change request! Study IUID and Series IUID must not be changed!");
                return NonIOCMChangeType.ILLEGAL_CHANGE;
            }
            return NonIOCMChangeType.PAT_ID_CHANGE;
        } else if (studyChg) {
            if (seriesChg) {
                LOG.warn("Illegal NoneICOM Study IUID change request! Series IUID must not be changed!");
                return NonIOCMChangeType.ILLEGAL_CHANGE;
            }
            return NonIOCMChangeType.STUDY_IUID_CHANGE;
        } else if (seriesChg) {
            return NonIOCMChangeType.SERIES_IUID_CHANGE;
        }
        return NonIOCMChangeType.INSTANCE_CHANGE;
    }

    public List<QCInstanceHistory> findInstanceHistory(String sopInstanceUID) {
        return new ArrayList<QCInstanceHistory>();
    }

    @Override
    public NonIOCMChangeType performChange(Instance inst, StoreContext context) {
        Attributes chgAttrs = new Attributes(context.getAttributes(), BASIC_CHG_ATTRIBUTES);
        Attributes origAttrs = new Attributes();
        Sequence origSQ = chgAttrs.ensureSequence(Tag.ModifiedAttributesSequence, 1);
        origSQ.add(origAttrs);
        NonIOCMChangeType chgType = getChangeType(inst, context.getAttributes());
        LOG.debug("performChange start for changeType:{}", chgType);
        switch (chgType) {
        case PAT_ID_CHANGE:
            origAttrs.addSelected(inst.getSeries().getStudy().getPatient().getAttributes(), 
                    context.getStoreSession().getStoreParam().getAttributeFilter(Entity.Patient).getSelection());
            StoreSession session = context.getStoreSession();
            try {
                patientService.updateOrCreatePatientOnCStore(context.getAttributes(),
                        PatientSelectorFactory.createSelector(session.getStoreParam()), session.getStoreParam());
            } catch (PatientCircularMergedException e) {
                LOG.error("Patient for received Instance is merged circular!", e);
            }
            activeProcessingService.addActiveProcess(inst.getSeries().getStudy().getStudyInstanceUID(), 
                    inst.getSeries().getSeriesInstanceUID(), inst.getSopInstanceUID(), ActiveService.NON_IOCM_UPDATE, chgAttrs);
            break;
        case STUDY_IUID_CHANGE:
            origAttrs.setString(Tag.StudyInstanceUID, VR.UI, inst.getSeries().getStudy().getStudyInstanceUID());
            activeProcessingService.addActiveProcess(inst.getSeries().getStudy().getStudyInstanceUID(), 
                    inst.getSeries().getSeriesInstanceUID(), inst.getSopInstanceUID(), ActiveService.NON_IOCM_UPDATE, chgAttrs);
            break;
        case SERIES_IUID_CHANGE:
            origAttrs.setString(Tag.SeriesInstanceUID, VR.UI, inst.getSeries().getSeriesInstanceUID());
            activeProcessingService.addActiveProcess(inst.getSeries().getStudy().getStudyInstanceUID(), 
                    inst.getSeries().getSeriesInstanceUID(), inst.getSopInstanceUID(), ActiveService.NON_IOCM_UPDATE, chgAttrs);
            break;
        case INSTANCE_CHANGE:
            origAttrs.setString(Tag.SOPInstanceUID, VR.UI, inst.getSopInstanceUID());
            String newUID = UIDUtils.createUID();
            context.getAttributes().setString(Tag.SOPInstanceUID, VR.UI, newUID);
            context.setProperty(StoreServiceNonIOCMDecorator.NONE_IOCM_HIDE_NEW_INSTANCE, newUID);
            chgAttrs.setString(Tag.SOPInstanceUID, VR.UI, newUID);
            activeProcessingService.addActiveProcess(inst.getSeries().getStudy().getStudyInstanceUID(), 
                    inst.getSeries().getSeriesInstanceUID(), newUID, ActiveService.NON_IOCM_UPDATE, chgAttrs);
            break;
        default:
        }
        return chgType;
    }

    @Override
    public QCInstanceHistory getLastQCInstanceHistory(String sopIUID) {
        Query query = em.createNamedQuery(QCInstanceHistory.FIND_BY_OLD_UID);
        query.setParameter(1, sopIUID);
        @SuppressWarnings("unchecked")
        List<QCInstanceHistory> tmp = (List<QCInstanceHistory>) query.getResultList();
        return tmp.size() == 0 ? null : tmp.get(0);
    }

    @Override
    public void hideOrUnhideInstance(Instance instance, org.dcm4che3.data.Code rejNoteCode) {
        Code code = rejNoteCode == null ? null : codeService.findOrCreate(new Code(NonIOCMChangeRequestorService.REJ_CODE_QUALITY_REASON));
        instance.setRejectionNoteCode(code);
        em.merge(instance);
    }

    @Override
    public void handleModalityChange(Instance inst, StoreContext context, int gracePeriodInSeconds) {
        if(withinGracePeriodAndNonIOCMSource(inst, gracePeriodInSeconds)) {
            context.setOldNONEIOCMChangeUID(inst.getSopInstanceUID());
            Attributes attrs = context.getAttributes();
            attrs.setString(null, Tag.SOPInstanceUID, VR.UI, 
                    UIDUtils.createUID());
            inst.setAttributes(attrs, context.getStoreSession().getStoreParam().getAttributeFilter(Entity.Instance),
                    context.getStoreSession().getStoreParam().getFuzzyStr(), context.getStoreSession().getStoreParam().getNullValueForQueryFields());
            em.merge(inst);
            context.setInstance(inst);
            context.setOldNONEIOCMChangeUID(inst.getSopInstanceUID());
        }
        else {
            context.setStoreAction(StoreAction.IGNORE);
        }
    }

    @Override
    public void onStoreInstance(StoreContext context) {
        //check here if the stored instance was received by NoneIOCM
        //Source modality within grace period

        if(context.getOldNONEIOCMChangeUID() != null) {
            //create Split QC history for none IOCM
            QCActionHistory action = new QCActionHistory();
            action.setAction(QCOperation.SPLIT.toString());
            action.setCreatedTime(new Date(System.currentTimeMillis()));
            em.persist(action);
            QCStudyHistory studyHistory = new QCStudyHistory();
            studyHistory.setAction(action);
            studyHistory.setUpdatedAttributesBlob(null); //no change (expect same attrs in study)
            studyHistory.setOldStudyUID(context.getAttributes().getString(Tag.StudyInstanceUID));
            studyHistory.setNextStudyUID(context.getAttributes().getString(Tag.StudyInstanceUID));
            em.persist(studyHistory);
            QCSeriesHistory seriesHistory = new QCSeriesHistory();
            seriesHistory.setStudy(studyHistory);
            seriesHistory.setUpdatedAttributesBlob(null); //no change (expect same attrs in series)
            seriesHistory.setOldSeriesUID(context.getAttributes().getString(Tag.SeriesInstanceUID));
            em.persist(seriesHistory);
            QCInstanceHistory instanceHistory = new QCInstanceHistory(
                    context.getAttributes().getString(Tag.StudyInstanceUID),
                    context.getAttributes().getString(Tag.SeriesInstanceUID),
                    context.getOldNONEIOCMChangeUID(),
                    context.getInstance().getSopInstanceUID(),
                    context.getInstance().getSopInstanceUID(), false);
            instanceHistory.setSeries(seriesHistory);
            em.persist(instanceHistory);
        }
    }
    
    private boolean withinGracePeriodAndNonIOCMSource(Instance inst, int gracePeriodInSeconds) {
        Query query = em.createNamedQuery(QCInstanceHistory
                .FIND_BY_CURRENT_UID_FOR_ACTION, QCInstanceHistory.class);
        query.setParameter(1, inst.getSopInstanceUID());
        query.setParameter(2, QCOperation.DELETE.toString());
        QCInstanceHistory foundQCInstanceHistory = (QCInstanceHistory) query.getSingleResult();

        if(foundQCInstanceHistory == null)
            return false;

        if(foundQCInstanceHistory.getSeries().getNoneIOCMSourceAET() == null)
            return false;

        long createdTime = foundQCInstanceHistory.getSeries()
                .getStudy().getAction().getCreatedTime().getTime();
        long now = System.currentTimeMillis();

        return (now - createdTime) < gracePeriodInSeconds; 
    }

    @Override
    public void onStudyUpdated(@Observes StudyUpdatedEvent studyUpdatedEvent) {
        LOG.debug("onStudyUpdated:{}", studyUpdatedEvent);
        if (isNonIOCMChangeRequestor(studyUpdatedEvent.getSourceAET())) {
            String updatedStudyUID = studyUpdatedEvent.getStudyInstanceUID();
            LOG.debug("Received Study-Updated event for study {} updated by a non-IOCM source", updatedStudyUID);
            if (activeProcessingService.isStudyUnderProcessingByServices(updatedStudyUID, NON_IOCM_ACTIVE_SERVICES)) {
                LOG.debug("Schedule NoneIOCM change request for study {}", updatedStudyUID);
                try {
                    scheduleNonIocmChangeRequest(updatedStudyUID, 0);
                } catch (JMSException e) {
                    LOG.error(format("Schedule of Non-IOCM-Change-Request for study %s failed!", updatedStudyUID), e);
                }
            } else {
                LOG.debug("No active Non-IOCM service found for study {}", updatedStudyUID);
            }
        }
    }

    private void scheduleNonIocmChangeRequest(String studyIUID, int delay) throws JMSException {
        Connection conn = connFactory.createConnection();
        try {
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(noniocmQueue);
            Message msg = session.createMessage();
            if (delay > 0) {
                msg.setLongProperty("_HQ_SCHED_DELIVERY", System.currentTimeMillis() + delay);
            }
            msg.setStringProperty(NonIocmChangeRequestorMDB.UPDATED_STUDY_UID_MSG_PROPERTY, studyIUID);
            producer.send(msg);
        } finally {
            conn.close();
        }
    }
    
    @Override
    public void processNonIOCMRequest(String processStudyIUID,  List<ActiveProcessing> nonIocmProcessings) {
        // check if study protected against structural changes
        if (!areStructuralChangesPermittedForStudy(processStudyIUID)) {
            LOG.info("Non-IOCM active-processing message for study {} will be ignored as study is protected", processStudyIUID);
            return;
        }

        Map<String, String> sopUIDchanged = new HashMap<String, String>();
        Map<String, List<String>> studyUIDchanged = new HashMap<String, List<String>>();
        Map<String, List<String>> seriesUIDchanged = new HashMap<String, List<String>>();
        Map<String, List<String>> patIDchanged = new HashMap<String, List<String>>();
        Map<String, Attributes> patAttrChanged = new HashMap<String, Attributes>();

        for (ActiveProcessing processing : nonIocmProcessings) {
            Attributes attrs = processing.getAttributes();
            Attributes item = attrs.getNestedDataset(Tag.ModifiedAttributesSequence);
            if (item.contains(Tag.StudyInstanceUID)) {
                addChgdIUID(studyUIDchanged, attrs.getString(Tag.StudyInstanceUID),
                        attrs.getString(Tag.SOPInstanceUID));
            } else if (item.contains(Tag.SeriesInstanceUID)) {
                addChgdIUID(seriesUIDchanged, attrs.getString(Tag.SeriesInstanceUID),
                        attrs.getString(Tag.SOPInstanceUID));
            } else if (item.contains(Tag.SOPInstanceUID)) {
                sopUIDchanged.put(attrs.getString(Tag.SOPInstanceUID),
                        item.getString(Tag.SOPInstanceUID));
            } else if (item.contains(Tag.PatientID)) {
                String pid = attrs.getString(Tag.PatientID) + "^^^"
                        + attrs.getString(Tag.IssuerOfPatientID);
                addChgdIUID(patIDchanged, pid, attrs.getString(Tag.SOPInstanceUID));
                patAttrChanged.put(pid, attrs);
            } else {
                LOG.warn("Cannot determine Non-IOCM change! Ignore this active-processing ({})",
                        processing);
            }
        }

        if (sopUIDchanged.size() > 0) {
            try {
                scService.replaced(STRUCTURAL_CHANGE.NON_IOCM, sopUIDchanged, REJ_CODE_QUALITY_REASON);
            } catch (QCOperationNotPermittedException e1) {
                LOG.warn("QC Replace-Operation not permitted", e1);
            }
        }
        if (seriesUIDchanged.size() > 0) {
            Attributes seriesAttrs = new Attributes();
            for (Map.Entry<String, List<String>> e : seriesUIDchanged.entrySet()) {
                seriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI, e.getKey());
                try {
                    scService.split(STRUCTURAL_CHANGE.NON_IOCM, e.getValue(), null, processStudyIUID, null, seriesAttrs, REJ_CODE_QUALITY_REASON);
                } catch (QCOperationNotPermittedException e2) {
                    LOG.warn("QC Split-Operation not permitted", e2);
                }
            }
        }
        if (studyUIDchanged.size() > 0) {
            for (Map.Entry<String, List<String>> e : studyUIDchanged.entrySet()) {
                try {
                    scService.split(STRUCTURAL_CHANGE.NON_IOCM, e.getValue(), null, e.getKey(), null, null, REJ_CODE_QUALITY_REASON);
                } catch (QCOperationNotPermittedException e3) {
                    LOG.warn("QC Split-Operation not permitted", e3);
                }
            }
        }
        if (patIDchanged.size() > 0) {
            for (Map.Entry<String, List<String>> e : patIDchanged.entrySet()) {
                IDWithIssuer pid = IDWithIssuer.pidOf(patAttrChanged.get(e.getKey()));
                String studyUID = UIDUtils.createUID();
                try {
                    scService.split(STRUCTURAL_CHANGE.NON_IOCM, e.getValue(), pid, studyUID, null, null, REJ_CODE_QUALITY_REASON);
                } catch (QCOperationNotPermittedException e4) {
                    LOG.warn("QC Split-Operation not permitted", e4);
                }
            }
        }
    }
    
    private static void addChgdIUID(Map<String, List<String>> map, String key, String iuid) {
        List<String> chgdIUIDs = map.get(key);
        if (chgdIUIDs == null) {
            chgdIUIDs = new ArrayList<String>();
            map.put(key, chgdIUIDs);
        }
        chgdIUIDs.add(iuid);
    }
    
    private boolean areStructuralChangesPermittedForStudy(String studyIUID) {
        for(StudyProtectionHook studyProtectionHook : studyProtectionHooks) {
            if(studyProtectionHook.isProtected(studyIUID)) {
                return false;
            }
        }
        return true;
    }
    
}
