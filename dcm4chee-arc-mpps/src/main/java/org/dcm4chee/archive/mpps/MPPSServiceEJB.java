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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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
package org.dcm4chee.archive.mpps;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.BasicMPPSSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.mpps.rejection.IncorrectWorkListEntrySelectedHandlerEJB;
import org.dcm4chee.archive.patient.PatientSelectorFactory;
import org.dcm4chee.archive.patient.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Roman K
 */
@Stateless
public class MPPSServiceEJB {
    private static final Logger LOG = LoggerFactory.getLogger(MPPSServiceEJB.class);

    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    private PatientService patientService;

    @Inject
    private CodeService codeService;

    @Inject
    Device device;

    @Inject
    IncorrectWorkListEntrySelectedHandlerEJB incorrectWorkListEntrySelectedHandler;

    public MPPS createPerformedProcedureStep(
            ApplicationEntity ae,
            String mppsIuid,
            Attributes attrs) throws DicomServiceException {

        // mpps with such iuid must not exist
        try {
            findPPS(mppsIuid);
            throw new DicomServiceException(Status.DuplicateSOPinstance, "PPS with iuid " + mppsIuid + " already exists")
                    .setUID(Tag.AffectedSOPInstanceUID, mppsIuid);
        } catch (NoResultException ignore) {
        }

        StoreParam storeParam = ae.getAEExtensionNotNull(ArchiveAEExtension.class).getStoreParam();
        Patient patient;
        try {
            patient = patientService.updateOrCreatePatientOnMPPSNCreate(attrs, PatientSelectorFactory.createSelector(storeParam), storeParam);
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }

        Attributes mppsAttrs = new Attributes(attrs.size());

        // Filter out patient attrs - they are already in Patient blob
        mppsAttrs.addNotSelected(attrs,storeParam.getAttributeFilter(Entity.Patient).getCompleteSelection(attrs));
        MPPS mpps = new MPPS();
        mpps.setSopInstanceUID(mppsIuid);
        mpps.setAttributes(mppsAttrs, storeParam.getNullValueForQueryFields());
        mpps.setPatient(patient);

        em.persist(mpps);
        return mpps;
    }

    public MPPS updatePerformedProcedureStep(ApplicationEntity ae,
                                             String iuid,
                                             Attributes modified) throws DicomServiceException {
        MPPS mpps;
        try {
            mpps = findPPS(iuid);
        } catch (NoResultException e) {
            throw new DicomServiceException(Status.NoSuchObjectInstance).setUID(Tag.AffectedSOPInstanceUID, iuid);
        }

        if (mpps.getStatus() != MPPS.Status.IN_PROGRESS)
            BasicMPPSSCP.mayNoLongerBeUpdated();

        // overwrite attributes
        Attributes attrs = mpps.getAttributes();
        attrs.addAll(modified);
        StoreParam storeParam = ae.getAEExtensionNotNull(ArchiveAEExtension.class).getStoreParam();
        mpps.setAttributes(attrs, storeParam.getNullValueForQueryFields());

        // check if allowed to change
        if (mpps.getStatus() != MPPS.Status.IN_PROGRESS) {
            if (!attrs.containsValue(Tag.PerformedSeriesSequence))
                throw new DicomServiceException(Status.MissingAttributeValue)
                        .setAttributeIdentifierList(Tag.PerformedSeriesSequence);
        }

        if (mpps.getStatus() == MPPS.Status.DISCONTINUED) {

            // set discontinuation code
            Attributes codeItem = attrs.getNestedDataset(Tag.PerformedProcedureStepDiscontinuationReasonCodeSequence);
            if (codeItem != null) {
                Code code = codeService.findOrCreate(new Code(codeItem));
                mpps.setDiscontinuationReasonCode(code);
            }

        }
        em.merge(mpps);

        // TODO: this should be decoupled - but we have to think about fail/retry strategy here
        // What happens if we receive this before all the instances are stored? - See the StoreServiceMPPSDecorator
        // reject stored instances that are a result of an incorrectly chosen worklist entry
        incorrectWorkListEntrySelectedHandler.checkStatusAndRejectRejectInstancesIfNeeded(mpps);

        return mpps;
    }

    public MPPS findPPS(String sopInstanceUID) {
        return em.createNamedQuery(MPPS.FIND_BY_SOP_INSTANCE_UID_EAGER, MPPS.class)
                .setParameter(1, sopInstanceUID)
                .getSingleResult();
    }

}
