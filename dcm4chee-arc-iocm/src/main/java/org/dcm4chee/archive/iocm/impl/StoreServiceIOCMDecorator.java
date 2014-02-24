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

package org.dcm4chee.archive.iocm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.net.Status;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.entity.Availability;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.iocm.RejectionEvent;
import org.dcm4chee.archive.iocm.RejectionType;
import org.dcm4chee.archive.store.StoreAction;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Decorator @Priority(Interceptor.Priority.APPLICATION)
public abstract class StoreServiceIOCMDecorator implements StoreService {

    public static int DUPLICATE_REJECTION_NOTE = Status.CannotUnderstand + 0x800;
    public static int OCCURENCE_OF_REJECTED_INSTANCE = Status.CannotUnderstand + 0x801;
    public static int STUDY_MISMATCH = Status.CannotUnderstand + 0x802;
    public static int CLASS_INSTANCE_CONFLICT = Status.CannotUnderstand + 0x803;
    public static int NO_SUCH_OBJECT_INSTANCE = Status.CannotUnderstand + 0x804;
    public static int DUPLICATE_REJECTION = Status.CannotUnderstand + 0x805;
    public static int NO_MPPS_REF = Status.CannotUnderstand + 0x806;
    public static int NO_SUCH_MPPS = Status.CannotUnderstand + 0x807;
    public static int MPPS_IN_PROGRESS = Status.CannotUnderstand + 0x808;
    public static int MPPS_REMAINING_INSTANCES = Status.CannotUnderstand + 0x809;

    static Logger LOG = LoggerFactory.getLogger(StoreServiceIOCMDecorator.class);

    @Inject @Delegate StoreService storeService;

    @Inject Event<RejectionEvent> event;

    @Override
    public StoreAction instanceExists(EntityManager em, StoreContext context,
            Instance inst) throws DicomServiceException {
        ArchiveDeviceExtension arcDev = context.getStoreSession()
                .getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class);
        RejectionType rejectionNote = rejectionTypeOf(
                inst.getConceptNameCode(), arcDev);
        if (rejectionNote != null) {
            LOG.info("{}: Subsequent occurrence of Rejection Note[iuid={},code={}]",
                    context.getStoreSession(),
                    inst.getSopInstanceUID(),
                    inst.getConceptNameCode());
            throw new DicomServiceException(DUPLICATE_REJECTION_NOTE,
                    "subsequent occurrence of rejection note");
        }
        RejectionType rejected = rejectionTypeOf(
                inst.getRejectionNoteCode(), arcDev);
        if (rejected != null) {
            if (rejected == RejectionType.DataRetentionPeriodExpired)
                return StoreAction.REPLACE;
            if (rejected == RejectionType.RejectedForQualityReasons)
                return StoreAction.IGNORE;
            LOG.info("{}: Subsequent occurrence of Instance[iuid={}] rejected for {}",
                    context.getStoreSession(),
                    inst.getSopInstanceUID(),
                    inst.getRejectionNoteCode());
            throw new DicomServiceException(OCCURENCE_OF_REJECTED_INSTANCE,
                    "subsequent occurrence of rejected instance");
        }
        return storeService.instanceExists(em, context, inst);
    }

    @Override
    public Instance findOrCreateInstance(EntityManager em, StoreContext context)
            throws DicomServiceException {
        Instance inst = storeService.findOrCreateInstance(em, context);
        ArchiveDeviceExtension arcDev = context.getStoreSession()
                .getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class);
        RejectionType rejectionType = rejectionTypeOf(
                inst.getConceptNameCode(), arcDev);
        if (rejectionType != null) {
            reject(em, context, inst, rejectionType);
            inst.setAvailability(Availability.UNAVAILABLE);
            context.setProperty(RejectionType.class.getName(), rejectionType);
        }
        return inst;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fireStoreEvent(StoreContext context) {
        storeService.fireStoreEvent(context);
        RejectionType rejectionType =
                (RejectionType) context.getProperty(RejectionType.class.getName());
        if (rejectionType != null)
            event.fire(new RejectionEvent(context, rejectionType,
                    (Collection<MPPS>) context.getProperty(
                            "org.dcm4chee.archive.iocm.RejectedMPPS")));
    }

    private RejectionType rejectionTypeOf(Code code,
            ArchiveDeviceExtension arcDev) {
        if (arcDev != null && code != null) {
            if (code.equals((Code) arcDev.getIncorrectModalityWorklistEntryCode()))
                return RejectionType.IncorrectModalityWorklistEntry;
            if (code.equals((Code) arcDev.getIncorrectWorklistEntrySelectedCode()))
                return RejectionType.IncorrectWorklistEntrySelected;
            if (code.equals((Code) arcDev.getRejectedForPatientSafetyReasonsCode()))
                return RejectionType.RejectedForPatientSafetyReasons;
            if (code.equals((Code) arcDev.getRejectedForQualityReasonsCode()))
                return RejectionType.RejectedForQualityReasons;
            if (code.equals((Code) arcDev.getDataRetentionPeriodExpiredCode()))
                return RejectionType.DataRetentionPeriodExpired;
        }
        return null;
    }

    private void reject(EntityManager em, StoreContext context, Instance rejectionNote,
            RejectionType rejectionType) throws DicomServiceException {
        Code rejectionCode = rejectionNote.getConceptNameCode();
        LOG.info("{}: Process Rejection Note[pk={}, iuid={}, code={}]",
                context.getStoreSession(),
                rejectionNote.getPk(),
                rejectionNote.getSopInstanceUID(),
                rejectionCode);
        Collection<Instance> rejectedInstances = queryRejectedInstances(em, context);
        if (rejectionType == RejectionType.IncorrectModalityWorklistEntry)
            context.setProperty("org.dcm4chee.archive.iocm.RejectedMPPS",
                    queryRejectedMPPS(em, context, rejectedInstances));
        for (Instance rejectedInst : rejectedInstances) {
            rejectedInst.setRejectionNoteCode(rejectionCode);
            rejectedInst.setAvailability(Availability.UNAVAILABLE);
            Series series = rejectedInst.getSeries();
            Study study = series.getStudy();
            series.resetNumberOfInstances();
            study.resetNumberOfInstances();
            LOG.info("{}: Reject Instance[pk={}, iuid={}] for {}]",
                    context.getStoreSession(),
                    rejectedInst.getPk(),
                    rejectedInst.getSopInstanceUID(),
                    rejectionCode);
        }
    }

    private Collection<Instance> queryRejectedInstances(EntityManager em,
            StoreContext context) throws DicomServiceException {
        ArrayList<Instance> result = new ArrayList<Instance>();
        HashMap<String,Attributes> refSOPs = new HashMap<String,Attributes>();
        Attributes attrs = context.getAttributes();
        String studyIUID = attrs.getString(Tag.StudyInstanceUID);
        for (Attributes refStudy : attrs
                .getSequence(Tag.CurrentRequestedProcedureEvidenceSequence)) {
            if (!studyIUID.equals(refStudy.getString(Tag.StudyInstanceUID))) {
                LOG.info("{}: Study[iuid={}] of Rejection Note does not match "
                        + "Study[iuid={}] of referenced Instances - rejection fails",
                        context.getStoreSession(),
                        studyIUID, refStudy.getString(Tag.StudyInstanceUID));
                throw new DicomServiceException(STUDY_MISMATCH,
                        "referenced SOP instances belong to different Study than Rejection Note");
            }
            for (Attributes refSeries
                    : refStudy.getSequence(Tag.ReferencedSeriesSequence)) {
                for (Attributes refSOP
                        : refSeries.getSequence(Tag.ReferencedSOPSequence)) {
                    refSOPs.put(refSOP.getString(Tag.ReferencedSOPInstanceUID), refSOP);
                }
                for (Instance inst : em.createNamedQuery(
                        Instance.FIND_BY_SERIES_INSTANCE_UID, Instance.class)
                    .setParameter(1, refSeries.getString(Tag.SeriesInstanceUID))
                    .getResultList()) {
                    Attributes refSOP = refSOPs.remove(inst.getSopInstanceUID());
                    if (refSOP != null) {
                        if (!inst.getSopClassUID().equals(
                                refSOP.getString(Tag.ReferencedSOPClassUID))) {
                            LOG.info("{}: SOP Class of referenced Instance[iuid={}, cuid={}] "
                                    + "does not match referenced SOP Class[cuid={}] - rejection fails",
                                    context.getStoreSession(),
                                    inst.getSopInstanceUID(), inst.getSopClassUID(),
                                    refSOP.getString(Tag.ReferencedSOPClassUID)
                                    );
                            throw new DicomServiceException(CLASS_INSTANCE_CONFLICT,
                                    "referenced SOP instance is not a member of the referenced SOP class");
                        }
                        if (inst.getRejectionNoteCode() != null) {
                            LOG.info("{}: referenced Instance[iuid={}, cuid={}] "
                                    + "was already rejected with Code {} - rejection fails",
                                    context.getStoreSession(),
                                    inst.getSopInstanceUID(), inst.getSopClassUID(),
                                    inst.getRejectionNoteCode()
                                    );
                            throw new DicomServiceException(DUPLICATE_REJECTION,
                                    "referenced SOP instance was already rejected");
                        }
                        result.add(inst);
                    }
                }
                if (!refSOPs.isEmpty()) {
                    LOG.info("{}: {} referenced Instance(s) not found - rejection fails",
                            context.getStoreSession(), refSOPs.size());
                    throw new DicomServiceException(NO_SUCH_OBJECT_INSTANCE,
                            "referenced SOP instance does not exists");
                }
            }
        }
        return result;
    }

    private Collection<MPPS> queryRejectedMPPS(EntityManager em, StoreContext context,
            Collection<Instance> c) throws DicomServiceException {
        ArrayList<MPPS> mppsList = new ArrayList<MPPS>();
        HashMap<String,HashMap<String,Attributes>> refSOPsByMPPS =
                new HashMap<String,HashMap<String,Attributes>>();
        for (Instance inst : c) {
            Series series = inst.getSeries();
            String ppsiuid = series.getPerformedProcedureStepInstanceUID();
            String ppscuid = series.getPerformedProcedureStepClassUID();
            if (ppsiuid == null 
                    || !UID.ModalityPerformedProcedureStepSOPClass.equals(ppscuid)) {
                LOG.info("{}: referenced Instance[iuid={}, cuid={}] "
                        + "does not reference MPPS - rejection fails",
                        context.getStoreSession(),
                        inst.getSopInstanceUID(), inst.getSopClassUID());
                throw new DicomServiceException(NO_MPPS_REF,
                        "referenced SOP instance does not reference MPPS");
            }
            HashMap<String, Attributes> refSOPs = refSOPsByMPPS.get(ppsiuid);
            if (refSOPs == null) {
                MPPS mpps;
                try {
                    mpps = em.createNamedQuery(
                            MPPS.FIND_BY_SOP_INSTANCE_UID, MPPS.class)
                        .setParameter(1, ppsiuid)
                        .getSingleResult();
                } catch (NoResultException e) {
                    LOG.info("{}: MPPS[uid={}] of referenced Instance[iuid={}, cuid={}] "
                            + "does not exists - rejection fails",
                            context.getStoreSession(), ppsiuid,
                            inst.getSopInstanceUID(), inst.getSopClassUID());
                    throw new DicomServiceException(NO_SUCH_MPPS,
                            "MPPS of rejected SOP instance does not exists");
                }
                if (mpps.getStatus() == MPPS.Status.IN_PROGRESS)
                    throw new DicomServiceException(MPPS_IN_PROGRESS,
                            "MPPS of rejected SOP instance still in progress");
                refSOPs = new HashMap<String, Attributes>();
                for (Attributes refSeries : mpps.getAttributes()
                        .getSequence(Tag.PerformedSeriesSequence)) {
                    for (Attributes refSOP
                            : refSeries.getSequence(Tag.ReferencedImageSequence)) {
                        refSOPs.put(refSOP.getString(Tag.ReferencedSOPInstanceUID), refSOP);
                    }
                    for (Attributes refSOP
                            : refSeries.getSequence(Tag.ReferencedNonImageCompositeSOPInstanceSequence)) {
                        refSOPs.put(refSOP.getString(Tag.ReferencedSOPInstanceUID), refSOP);
                    }
                }
                refSOPsByMPPS.put(ppsiuid, refSOPs);
                mppsList.add(mpps);
            }
            refSOPs.remove(inst.getSopInstanceUID());
        }
        for (Entry<String, HashMap<String, Attributes>> entry : refSOPsByMPPS.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                LOG.info("{}: {} Instances referenced by MPPS[uid={}] not rejected - rejection fails",
                        context.getStoreSession(), entry.getValue().size(), entry.getKey());
                throw new DicomServiceException(MPPS_REMAINING_INSTANCES,
                        "Not all Instances referenced by MPPS rejected");
            }
        }
        return mppsList;
    }

}
