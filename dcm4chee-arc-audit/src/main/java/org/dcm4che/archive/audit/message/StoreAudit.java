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

package org.dcm4che.archive.audit.message;

import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.AuditMessages.EventActionCode;
import org.dcm4che3.audit.AuditMessages.EventID;
import org.dcm4che3.audit.AuditMessages.ParticipantObjectDescription;
import org.dcm4che3.audit.AuditMessages.RoleIDCode;
import org.dcm4che3.audit.Instance;
import org.dcm4che3.audit.ParticipantObjectDetail;
import org.dcm4che3.audit.ParticipantObjectIdentification;
import org.dcm4che3.audit.SOPClass;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4chee.archive.dto.Participant;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
public class StoreAudit extends AuditMessage {

    private String remoteAET;
    private Participant source;
    private Attributes attributes;
    private String eventOutcomeIndicator;
    private AuditLogger logger;

    /**
     */
    public StoreAudit(String remoteAET, Participant source, Attributes attributes,
            String eventOutcomeIndicator, AuditLogger logger) {
    	this(remoteAET, source, attributes, EventActionCode.Create,
            eventOutcomeIndicator, logger);
    }
    public StoreAudit(String remoteAET, Participant source, Attributes attributes, String eventActionCode,
            String eventOutcomeIndicator, AuditLogger logger) {
        super();
        this.remoteAET = remoteAET;
        this.source= source; 
        this.attributes = attributes;
        this.eventOutcomeIndicator = eventOutcomeIndicator;
        this.logger = logger;
        init(eventActionCode);
    }

    private void init(String eventActionCode) {
        
        // Event
        this.setEventIdentification(AuditMessages.createEventIdentification(
                EventID.DICOMInstancesTransferred, eventActionCode,
                logger.timeStamp(), eventOutcomeIndicator, null));

        // Active Participant 1: The requestor
        this.getActiveParticipant().add(
                AuditMessages.createActiveParticipant(remoteAET,
                        AuditMessages.alternativeUserIDForAETitle(remoteAET), null, 
                        true, source.getHost(),
                        AuditMessages.NetworkAccessPointTypeCode.MachineName,
                        null, AuditMessages.RoleIDCode.Source));

        // Active Participant 2: The process that received the data.
        this.getActiveParticipant().add(
                logger.createActiveParticipant(false, RoleIDCode.Destination));

        // Participating Object: Studies being transferred
        this.getParticipantObjectIdentification()
                .add(AuditMessages.createParticipantObjectIdentification(
                        attributes.getString(Tag.StudyInstanceUID),
                        AuditMessages.ParticipantObjectIDTypeCode.StudyInstanceUID,
                        "Study being transferred", null,
                        AuditMessages.ParticipantObjectTypeCode.SystemObject,
                        AuditMessages.ParticipantObjectTypeCodeRole.Report,
                        null, null,
                        createPODescription(createSOPClass(attributes))));

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

        this.setAuditSourceIdentification(logger.createAuditSourceIdentification());
    }

    private String getPatientID(Attributes attrs) {
        String patID = attrs.getString(Tag.PatientID);
        if (patID == null)
            return "UNKNOWN";
        String issuer = attrs.getString(Tag.IssuerOfPatientID);
        return (issuer == null || issuer.length() == 0) ? patID : patID + "^^^"
                + issuer;
    }

    private ParticipantObjectDescription createPODescription(SOPClass sop) {
        ParticipantObjectDescription desc = new ParticipantObjectDescription();
        desc.getSOPClass().add(sop);
        return desc;
    }

    private SOPClass createSOPClass(Attributes attributes) {
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
    public void addInstance(Attributes attributes) {
        String sopclassuid = attributes.getString(Tag.SOPClassUID);

        ParticipantObjectIdentification poi = this.getParticipantObjectIdentification().get(0); //first POI is studyIUID

        SOPClass sop = null;

        for (SOPClass existingsop : poi.getSOPClass())
            if (existingsop.getUID().equals(sopclassuid))
                sop = existingsop;

        if (sop == null) { // sop class were not found
            poi.getSOPClass().add(createSOPClass(attributes));
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
