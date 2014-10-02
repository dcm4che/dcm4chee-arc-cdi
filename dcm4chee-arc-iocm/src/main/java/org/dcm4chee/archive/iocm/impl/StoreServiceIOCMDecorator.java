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

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.RejectionParam;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.iocm.InstanceAlreadyRejectedException;
import org.dcm4chee.archive.iocm.RejectionEvent;
import org.dcm4chee.archive.iocm.RejectionService;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator to apply IOCM specifications to the Store Service.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Decorator
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

    @Inject RejectionService rejectionService;

    @Inject Event<RejectionEvent> event;

    /* 
     * Extends default instanceExists method. The first part of the method throws 
     * an exception in case a duplicate rejection note is received. The second part
     * manages received objects previously rejected, according to the IOCM standard. 
     */
    @Override
    public StoreAction instanceExists(EntityManager em, StoreContext context,
            Instance inst) throws DicomServiceException {
        ArchiveDeviceExtension arcDev = context.getStoreSession()
                .getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class);
        RejectionParam rejectionNote =  RejectionParam.forRejectionNoteTitle(
                inst.getConceptNameCode(), arcDev.getRejectionParams());
        if (rejectionNote != null) {
            LOG.info("{}: Subsequent occurrence of Rejection Note[iuid={},code={}]",
                    context.getStoreSession(),
                    inst.getSopInstanceUID(),
                    inst.getConceptNameCode());
            throw new DicomServiceException(DUPLICATE_REJECTION_NOTE,
                    "subsequent occurrence of rejection note");
        }
        RejectionParam rejected = RejectionParam.forRejectionNoteTitle(
                inst.getRejectionNoteCode(), arcDev.getRejectionParams());
        if (rejected != null) {
            StoreAction action = rejected.getAcceptPreviousRejectedInstance();
            LOG.info("{}: Subsequent occurrence of Instance[iuid={}] rejected for {}",
                    context.getStoreSession(),
                    inst.getSopInstanceUID(),
                    inst.getRejectionNoteCode());
            if (action == null || action == StoreAction.FAIL)
                throw new DicomServiceException(OCCURENCE_OF_REJECTED_INSTANCE,
                        "subsequent occurrence of rejected instance");
            return action;
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
        Code code = inst.getConceptNameCode();
        RejectionParam rejectionParam = RejectionParam.forRejectionNoteTitle(
                code, arcDev.getRejectionParams());
        if (rejectionParam != null) {
            HashSet<String> affectedMPPS = new HashSet<String>();
            processRejectionNote(em, context, inst, rejectionParam, affectedMPPS);
            context.setProperty(RejectionParam.class.getName(), rejectionParam);
            context.setProperty("org.dcm4chee.archive.iocm.mppsiuids", affectedMPPS);
        }
        return inst;
    }

    @Override
    public void fireStoreEvent(StoreContext context) {
        storeService.fireStoreEvent(context);
        RejectionParam rejectionNote =
                (RejectionParam) context.getProperty(RejectionParam.class.getName());
        if (rejectionNote != null) {
            @SuppressWarnings("unchecked")
            Collection<String> mppsIUIDs = (Collection<String>)
                    context.getProperty("org.dcm4chee.archive.iocm.mppsiuids");
            event.fire(new RejectionEvent(context, rejectionNote, mppsIUIDs));
        }
    }

    private void processRejectionNote(EntityManager em, StoreContext context,
            Instance rejectionNote, RejectionParam rejectionParam,
            HashSet<String> affectedMPPS) throws DicomServiceException {
        HashSet<String> mppsIUIDs = new HashSet<String>();
        org.dcm4chee.archive.entity.Code rejectionCode = rejectionNote.getConceptNameCode();
        LOG.info("{}: Process Rejection Note[pk={}, iuid={}, code={}]",
                context.getStoreSession(),
                rejectionNote.getPk(),
                rejectionNote.getSopInstanceUID(),
                rejectionCode);
        ArrayList<Series> affectedSeries = new ArrayList<Series>();
        Collection<Instance> rejectedInstances =
                queryRejectedInstances(em, context, rejectionNote,
                        rejectionParam, affectedSeries);
        for (Series series : affectedSeries) {
            String ppsIUID =  series.getPerformedProcedureStepInstanceUID();
            String ppsCUID = series.getPerformedProcedureStepClassUID();
            if (ppsIUID != null
                    && UID.ModalityPerformedProcedureStepSOPClass.equals(ppsCUID)) {
                mppsIUIDs.add(ppsIUID);
            }
        }
        try {
            if (rejectionParam.isRevokeRejection())
                rejectionService.restore(context.getStoreSession(), rejectedInstances,
                        rejectionParam.getOverwritePreviousRejection());
            else
                rejectionService.reject(context.getStoreSession(), rejectedInstances,
                        rejectionCode,
                        rejectionParam.getOverwritePreviousRejection());
        } catch (InstanceAlreadyRejectedException e) {
            Instance inst = e.getInstance();
            LOG.info("{}: referenced {} was already rejected with {} - Rejection Note not applied",
                    context.getStoreSession(),
                    inst, inst.getRejectionNoteCode());
            throw new DicomServiceException(DUPLICATE_REJECTION,
                    "referenced SOP instance already rejected");
        }
    }

    private Collection<Instance> queryRejectedInstances(EntityManager em,
            StoreContext context, Instance rejectionNote, 
            RejectionParam rejectionParam, Collection<Series> affectedSeries)
                    throws DicomServiceException {
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
