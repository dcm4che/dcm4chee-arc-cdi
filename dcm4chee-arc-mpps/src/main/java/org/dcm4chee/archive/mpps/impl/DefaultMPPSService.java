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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4chee.archive.mpps.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.net.*;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.*;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.mpps.MPPSServiceEJB;
import org.dcm4chee.archive.mpps.event.MPPSCreate;
import org.dcm4chee.archive.mpps.event.MPPSEvent;
import org.dcm4chee.archive.mpps.event.MPPSFinal;
import org.dcm4chee.archive.mpps.event.MPPSUpdate;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.query.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.xml.transform.Templates;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

@ApplicationScoped
public class DefaultMPPSService implements MPPSService {

    private static Logger LOG = LoggerFactory
            .getLogger(DefaultMPPSService.class);

    @Inject
    private MPPSServiceEJB ejb;

    @Inject
    private PatientService patientService;

    @Inject
    private QueryService queryService;

    @Inject
    @MPPSCreate
    private Event<MPPSEvent> createMPPSEvent;

    @Inject
    @MPPSUpdate
    private Event<MPPSEvent> updateMPPSEvent;

    @Inject
    @MPPSFinal
    private Event<MPPSEvent> finalMPPSEvent;

    @Inject
    private Device device;



    @Override
    public MPPS createPerformedProcedureStep(
            ArchiveAEExtension arcAE,
            String iuid,
            Attributes attrs,
            Patient patient,
            MPPSService service) throws DicomServiceException {

        return createPerformedProcedureStep(arcAE.getApplicationEntity(), iuid, attrs);
    }

    @Override
    public MPPS updatePerformedProcedureStep(ArchiveAEExtension arcAE,String iuid, Attributes modified, MPPSService service)
            throws DicomServiceException {
        return updatePerformedProcedureStep(arcAE.getApplicationEntity(), iuid, modified);
    }

    @Override
    public MPPS createPerformedProcedureStep(ApplicationEntity ae, String mppsSopInstanceUID, Attributes attrs) throws DicomServiceException {
        MPPS mpps = ejb.createPerformedProcedureStep(ae, mppsSopInstanceUID, attrs);
        return mpps;
    }

    @Override
    public MPPS updatePerformedProcedureStep(ApplicationEntity ae, String mppsSopInstanceUID, Attributes attrs) throws DicomServiceException {
        return ejb.updatePerformedProcedureStep(ae, mppsSopInstanceUID, attrs);
    }

    @Override
    public void coerceAttributes(Association as, Dimse dimse,
                                 Attributes attrs) throws DicomServiceException {
        try {
            ApplicationEntity ae = as.getApplicationEntity();
            ArchiveAEExtension arcAE = ae.getAEExtensionNotNull(ArchiveAEExtension.class);
            Templates tpl = arcAE.getAttributeCoercionTemplates(
                    UID.ModalityPerformedProcedureStepSOPClass,
                    dimse, TransferCapability.Role.SCP,
                    as.getRemoteAET());
            if (tpl != null) {
                Attributes modified = new Attributes();
                attrs.update(SAXTransformer.transform(attrs, tpl, false, false),
                        modified);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    @Override
    public void fireCreateMPPSEvent(ApplicationEntity ae, Attributes data,
                                    MPPS mpps) {
        createMPPSEvent.fire(
                new MPPSEvent(ae, Dimse.N_CREATE_RQ, data, mpps));
    }

    @Override
    public void fireUpdateMPPSEvent(ApplicationEntity ae, Attributes data,
                                    MPPS mpps) {
        updateMPPSEvent.fire(
                new MPPSEvent(ae, Dimse.N_SET_RQ, data, mpps));
    }

    @Override
    public void fireFinalMPPSEvent(ApplicationEntity ae, Attributes data,
                                   MPPS mpps) {
        finalMPPSEvent.fire(
                new MPPSEvent(ae, Dimse.N_SET_RQ, data, mpps));
    }

    // TODO: REVIEW: MPPSFinal - what if it is received before the referenced contents are stored? we should use StudyUpdatedEvent here ...
    public void onMPPSFinalEvent(@Observes @MPPSFinal MPPSEvent event) {
        LOG.info("Received MPPS complete event , initiating derived fields calculation");
        ArchiveDeviceExtension arcDevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        ApplicationEntity archiveAE = event.getApplicationEntity();
        ArchiveAEExtension arcAEExt = archiveAE.getAEExtension(ArchiveAEExtension.class);
        QueryRetrieveView view = arcDevExt.getQueryRetrieveView(arcAEExt.getQueryRetrieveViewID());
        if (view == null) {
            LOG.warn("Cannot re-calculate derived fields - query retrieve view ID is not specified for AE {}", archiveAE.getAETitle());
            return;
        }

        QueryParam param = new QueryParam();
        param.setQueryRetrieveView(view);
        ArrayList<String> studyInstanceUID = getStudyUIDFromMPPSAttrs(event.getPerformedProcedureStep().getAttributes());
        //now for each study
        try {
            for (String studyUID : studyInstanceUID) {
                Study study = ejb.findStudyByUID(studyUID);

                //create study view
                queryService.createStudyView(study.getPk(), param);

                //create series view
                for (Series series : study.getSeries())
                    queryService.createSeriesView(series.getPk(), param);
            }
        } catch (Exception e) {
            LOG.error("Error while calculating derived fields on MPPS COMPLETE", e);
        }
    }

    private ArrayList<String> getStudyUIDFromMPPSAttrs(Attributes attributes) {
        ArrayList<String> suids = new ArrayList<>();
        Sequence ssas = attributes.getSequence(Tag.ScheduledStepAttributesSequence);
        for (Iterator<Attributes> iter = ssas.iterator(); iter.hasNext(); ) {
            Attributes ssasItem = iter.next();
            String studyIUID = ssasItem.getString(Tag.StudyInstanceUID);
            if (studyIUID != null)
                suids.add(studyIUID);
        }
        return suids;
    }

}
