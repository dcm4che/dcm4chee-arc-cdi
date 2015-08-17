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
import org.dcm4che3.data.UID;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.net.*;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.*;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.mpps.MPPSContext;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.mpps.MPPSServiceEJB;
import org.dcm4chee.archive.mpps.event.MPPSCreate;
import org.dcm4chee.archive.mpps.event.MPPSEvent;
import org.dcm4chee.archive.mpps.event.MPPSFinal;
import org.dcm4chee.archive.mpps.event.MPPSUpdate;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.query.QueryService;
import org.dcm4chee.archive.util.TransactionSynchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.xml.transform.Templates;

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

    @Inject
    TransactionSynchronization transaction;

    /**
     * We need to move coercion out of the interface to remove this 'self' injection
     */
    @Inject
    MPPSService mppsService;


    @Deprecated
    @Override
    public MPPS createPerformedProcedureStep(
            ArchiveAEExtension arcAE,
            String iuid,
            Attributes attrs,
            Patient patient,
            MPPSService service) throws DicomServiceException {

        createPerformedProcedureStep(iuid, attrs, new MPPSContext(null, arcAE.getApplicationEntity().getAETitle()));
        return null;
    }

    @Deprecated
    @Override
    public MPPS updatePerformedProcedureStep(ArchiveAEExtension arcAE, String iuid, Attributes modified, MPPSService service)
            throws DicomServiceException {
        updatePerformedProcedureStep(iuid, modified, new MPPSContext(null, arcAE.getApplicationEntity().getAETitle()));
        return null;
    }

    @Override
    public void createPerformedProcedureStep(final String mppsSopInstanceUID, final Attributes attrs, final MPPSContext mppsContext) throws DicomServiceException {
        ApplicationEntity ae = device.getApplicationEntityNotNull(mppsContext.getReceivingAET());
        mppsService.coerceAttributes(mppsContext, Dimse.N_CREATE_RQ, attrs);
        ejb.createPerformedProcedureStep(ae, mppsSopInstanceUID, attrs);

        transaction.afterSuccessfulCommit(new Runnable() {
            @Override
            public void run() {
                createMPPSEvent.fire(new MPPSEvent(mppsSopInstanceUID, Dimse.N_CREATE_RQ, attrs, mppsContext));
            }
        });
    }

    @Override
    public void updatePerformedProcedureStep(final String mppsSopInstanceUID, final Attributes attrs, final MPPSContext mppsContext) throws DicomServiceException {

        ApplicationEntity ae = device.getApplicationEntityNotNull(mppsContext.getReceivingAET());
        final MPPS mpps = ejb.updatePerformedProcedureStep(ae, mppsSopInstanceUID, attrs);

        transaction.afterSuccessfulCommit(new Runnable() {
            @Override
            public void run() {
                if (mpps.getStatus() == MPPS.Status.IN_PROGRESS)
                    updateMPPSEvent.fire(new MPPSEvent(mppsSopInstanceUID, Dimse.N_SET_RQ, attrs, mppsContext));
                else
                    finalMPPSEvent.fire(new MPPSEvent(mppsSopInstanceUID, Dimse.N_SET_RQ, attrs, mppsContext));
            }
        });

    }

    @Override
    public void coerceAttributes(MPPSContext context, Dimse dimse, Attributes attrs) throws DicomServiceException {
        try {
            ApplicationEntity ae = device.getApplicationEntityNotNull(context.getReceivingAET());
            ArchiveAEExtension arcAE = ae.getAEExtensionNotNull(ArchiveAEExtension.class);
            Templates tpl = arcAE.getAttributeCoercionTemplates(
                    UID.ModalityPerformedProcedureStepSOPClass,
                    dimse, TransferCapability.Role.SCP,
                    context.getSendingAET());
            if (tpl != null) {
                Attributes modified = new Attributes();
                attrs.update(SAXTransformer.transform(attrs, tpl, false, false),
                        modified);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }


}
