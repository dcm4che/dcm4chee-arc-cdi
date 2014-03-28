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
import java.util.HashSet;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.entity.Availability;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
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
    public static int NO_MPPS = Status.CannotUnderstand + 0x806;

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
            HashSet<String> affectedMPPS = new HashSet<String>();
            applyRejectionNote(em, context, inst, rejectionType, affectedMPPS);
            context.setProperty(RejectionType.class.getName(), rejectionType);
            context.setProperty("org.dcm4chee.archive.iocm.mppsiuids", affectedMPPS);
            inst.setAvailability(Availability.UNAVAILABLE);
        }
        return inst;
    }

    @Override
    public void fireStoreEvent(StoreContext context) {
        storeService.fireStoreEvent(context);
        RejectionType rejectionType =
                (RejectionType) context.getProperty(RejectionType.class.getName());
        if (rejectionType != null) {
            @SuppressWarnings("unchecked")
            Collection<String> mppsIUIDs = (Collection<String>)
                    context.getProperty("org.dcm4chee.archive.iocm.mppsiuids");
            event.fire(new RejectionEvent(context, rejectionType, mppsIUIDs));
        }
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

    private void applyRejectionNote(EntityManager em, StoreContext context,
            Instance rejectionNote, RejectionType rejectionType, HashSet<String> affectedMPPS)
                    throws DicomServiceException {
        HashSet<String> mppsIUIDs = new HashSet<String>();
        Code rejectionCode = rejectionNote.getConceptNameCode();
        LOG.info("{}: Process Rejection Note[pk={}, iuid={}, code={}]",
                context.getStoreSession(),
                rejectionNote.getPk(),
                rejectionNote.getSopInstanceUID(),
                rejectionCode);
        ArrayList<Series> affectedSeries = new ArrayList<Series>();
        Collection<Instance> rejectedInstances =
                queryRejectedInstances(em, context, rejectionType, affectedSeries);
        for (Series series : affectedSeries) {
            String ppsIUID =  series.getPerformedProcedureStepInstanceUID();
            String ppsCUID = series.getPerformedProcedureStepClassUID();
            if (ppsIUID != null
                    && UID.ModalityPerformedProcedureStepSOPClass.equals(ppsCUID)) {
                mppsIUIDs.add(ppsIUID);
            } else if (rejectionType == RejectionType.IncorrectModalityWorklistEntry) {
                LOG.info("{}: No MPPS referenced by {} - Rejection Note not applied",
                        context.getStoreSession(), series);
                throw new DicomServiceException(DUPLICATE_REJECTION,
                        "referenced SOP instance does not reference MPPS");
            }
        }
        for (Instance rejectedInst : rejectedInstances) {
            rejectedInst.setRejectionNoteCode(rejectionCode);
            rejectedInst.setAvailability(Availability.UNAVAILABLE);
            LOG.info("{}: Reject Instance[pk={}, iuid={}] for {}]",
                    context.getStoreSession(),
                    rejectedInst.getPk(),
                    rejectedInst.getSopInstanceUID(),
                    rejectionCode);
        }
        for (Series series : affectedSeries) {
            series.resetNumberOfInstances();
        }
        if (!affectedSeries.isEmpty()) {
            affectedSeries.get(0).getStudy().resetNumberOfInstances();
        }
    }

    private Collection<Instance> queryRejectedInstances(EntityManager em,
            StoreContext context, RejectionType rejectionType,
            ArrayList<Series> affectedSeries) throws DicomServiceException {
        ArchiveDeviceExtension arcDev = context.getStoreSession()
                .getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class);
        ArrayList<Instance> result = new ArrayList<Instance>();
        HashMap<String,Attributes> refSOPs = new HashMap<String,Attributes>();
        Attributes attrs = context.getAttributes();
        String studyIUID = attrs.getString(Tag.StudyInstanceUID);
        for (Attributes refStudy : attrs
                .getSequence(Tag.CurrentRequestedProcedureEvidenceSequence)) {
            if (!studyIUID.equals(refStudy.getString(Tag.StudyInstanceUID))) {
                LOG.info("{}: Study[iuid={}] of Rejection Note does not match "
                        + "Study[iuid={}] of referenced Instances - Rejection Note not applied",
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
                Series series = null;
                for (Instance inst : em.createNamedQuery(
                        Instance.FIND_BY_SERIES_INSTANCE_UID, Instance.class)
                    .setParameter(1, refSeries.getString(Tag.SeriesInstanceUID))
                    .getResultList()) {
                    Attributes refSOP = refSOPs.remove(inst.getSopInstanceUID());
                    if (refSOP != null) {
                        if (series == null) {
                            series = inst.getSeries();
                            affectedSeries.add(series);
                        }
                        if (!inst.getSopClassUID().equals(
                                refSOP.getString(Tag.ReferencedSOPClassUID))) {
                            LOG.info("{}: SOP Class of referenced Instance[iuid={}, cuid={}] "
                                    + "does not match referenced SOP Class[cuid={}] - Rejection Note not applied",
                                    context.getStoreSession(),
                                    inst.getSopInstanceUID(), inst.getSopClassUID(),
                                    refSOP.getString(Tag.ReferencedSOPClassUID)
                                    );
                            throw new DicomServiceException(CLASS_INSTANCE_CONFLICT,
                                    "referenced SOP instance is not a member of the referenced SOP class");
                        }
                        if (inst.getRejectionNoteCode() != null
                                && rejectionTypeOf(inst.getRejectionNoteCode(), arcDev)
                                    != RejectionType.RejectedForQualityReasons) {
                            LOG.info("{}: referenced Instance[iuid={}, cuid={}] "
                                    + "was already rejected with Code {} - Rejection Note not applied",
                                    context.getStoreSession(),
                                    inst.getSopInstanceUID(), inst.getSopClassUID(),
                                    inst.getRejectionNoteCode()
                                    );
                            throw new DicomServiceException(DUPLICATE_REJECTION,
                                    "referenced SOP instance already rejected");
                        }
                        result.add(inst);
                    }
                }
                if (!refSOPs.isEmpty()) {
                    LOG.info("{}: {} referenced Instance(s) not found - Rejection Note not applied",
                            context.getStoreSession(), refSOPs.size());
                    throw new DicomServiceException(NO_SUCH_OBJECT_INSTANCE,
                            "referenced SOP instance does not exists");
                }
            }
        }
        return result;
    }

}
