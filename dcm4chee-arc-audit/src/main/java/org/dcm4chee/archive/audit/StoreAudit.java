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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4chee.archive.audit;

import javax.servlet.http.HttpServletRequest;

import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.ParticipantObjectDescription;
import org.dcm4che3.audit.AuditMessages.EventActionCode;
import org.dcm4che3.audit.AuditMessages.EventID;
import org.dcm4che3.audit.AuditMessages.RoleIDCode;
import org.dcm4che3.audit.Instance;
import org.dcm4che3.audit.ParticipantObjectIdentification;
import org.dcm4che3.audit.SOPClass;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.audit.AuditMessage;
import org.dcm4chee.archive.store.StoreSession;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
public class StoreAudit extends AuditMessage {

    private StoreSession session;
    private Attributes attributes;
    private String eventOutcomeIndicator;
    private AuditLogger logger;

    /**
     */
    public StoreAudit(StoreSession session, Attributes attributes,
            String eventOutcomeIndicator, AuditLogger logger) {
        super();
        this.session = session;
        this.attributes = attributes;
        this.eventOutcomeIndicator = eventOutcomeIndicator;
        this.logger = logger;
        init();
    }

    private void init() {
        this.setEventIdentification(AuditMessages.createEventIdentification(
                EventID.DICOMInstancesTransferred, EventActionCode.Create,
                logger.timeStamp(), eventOutcomeIndicator, null));

        // Active Participant 1: The process that received the data.
        this.getActiveParticipant().add(
                logger.createActiveParticipant(false, RoleIDCode.Source));

        // Active Participant 2: The requestor
        this.getActiveParticipant().add(
                AuditMessages.createActiveParticipant(session.getRemoteAET(),
                        AuditMessages.alternativeUserIDForAETitle(session
                                .getRemoteAET()), null, true,
                        getRemoteHost(session),
                        AuditMessages.NetworkAccessPointTypeCode.MachineName,
                        null, AuditMessages.RoleIDCode.Destination));

        // Participating Object: Studies being transferred
        this.getParticipantObjectIdentification()
                .add(AuditMessages.createParticipantObjectIdentification(
                        attributes.getString(Tag.StudyInstanceUID),
                        AuditMessages.ParticipantObjectIDTypeCode.StudyInstanceUID,
                        null, null,
                        AuditMessages.ParticipantObjectTypeCode.SystemObject,
                        AuditMessages.ParticipantObjectTypeCodeRole.Report,
                        null, null,
                        createPODescription(createSOPClass(attributes, logger))));

        // Participating Object: Patient
        this.getParticipantObjectIdentification()
                .add(AuditMessages
                        .createParticipantObjectIdentification(
                                getPatientID(attributes),
                                AuditMessages.ParticipantObjectIDTypeCode.PatientNumber,
                                null,
                                null,
                                AuditMessages.ParticipantObjectTypeCode.Person,
                                AuditMessages.ParticipantObjectTypeCodeRole.Patient,
                                null, null, null));

        this.getAuditSourceIdentification().add(
                logger.createAuditSourceIdentification());
    }

    private static String getRemoteHost(StoreSession session) {

        if (session.getSource() != null) {

            if (session.getSource() instanceof Association
                    && ((Association) session.getSource()).getSocket() != null)
                return ((Association) session.getSource()).getSocket()
                        .getInetAddress().getCanonicalHostName();

            if (session.getSource() instanceof HttpServletRequest)
                return ((HttpServletRequest) session.getSource())
                        .getRemoteHost();
        }

        return "UNKNOWN";
    }

    private static String getPatientID(Attributes attrs) {
        String patID = attrs.getString(Tag.PatientID);
        if (patID == null)
            return "UNKNOWN";
        String issuer = attrs.getString(Tag.IssuerOfPatientID);
        return (issuer == null || issuer.length() == 0) ? patID : patID + "^^^"
                + issuer;
    }

    private static ParticipantObjectDescription createPODescription(SOPClass sop) {
        ParticipantObjectDescription desc = new ParticipantObjectDescription();
        desc.getSOPClass().add(sop);
        return desc;
    }

    private static SOPClass createSOPClass(Attributes attributes,
            AuditLogger logger) {
        SOPClass sop = new SOPClass();
        sop.setUID(attributes.getString(Tag.SOPClassUID));
        sop.setNumberOfInstances(1);
        if (logger.isIncludeInstanceUID()) {
            Instance instance = new Instance();
            instance.setUID(attributes.getString(Tag.SOPInstanceUID));
            sop.getInstance().add(instance);
            sop.setNumberOfInstances(sop.getInstance().size());
        }
        return sop;
    }

    /**
     * Add a new instance to the existing store audit
     */
    public void addInstance(Attributes attributes, AuditLogger logger) {
        String sopclassuid = attributes.getString(Tag.SOPClassUID);

        ParticipantObjectDescription desc = this
                .getParticipantObjectIdentification().get(0)
                .getParticipantObjectDescription();

        SOPClass sop = null;

        for (SOPClass existingsop : desc.getSOPClass())
            if (existingsop.getUID().equals(sopclassuid))
                sop = existingsop;

        if (sop == null) { // sop class were not found
            desc.getSOPClass().add(createSOPClass(attributes, logger));
        } else { // sop class already existed

            Integer numberOfInstances = sop.getNumberOfInstances();
            sop.setNumberOfInstances(numberOfInstances == null ? 1
                    : ++numberOfInstances);

            if (logger.isIncludeInstanceUID()) {
                Instance instance = new Instance();
                instance.setUID(attributes.getString(Tag.SOPInstanceUID));
                sop.getInstance().add(instance);
            }
        }

    }
}
