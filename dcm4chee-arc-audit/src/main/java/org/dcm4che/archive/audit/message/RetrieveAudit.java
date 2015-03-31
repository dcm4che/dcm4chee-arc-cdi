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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.AuditMessages.EventActionCode;
import org.dcm4che3.audit.AuditMessages.EventID;
import org.dcm4che3.audit.Instance;
import org.dcm4che3.audit.ParticipantObjectDescription;
import org.dcm4che3.audit.SOPClass;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.Participant;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
public class RetrieveAudit extends AuditMessage {

    private Participant source;
    private Participant destination;
    private Participant requestor;
    private List<ArchiveInstanceLocator> instances;
    private EventID eventID; 
    private String eventOutcomeIndicator;
    private AuditLogger logger;

    /**
     */
    public RetrieveAudit(
            Participant source,
            Participant destination,
            Participant requestor,
            List<ArchiveInstanceLocator> instances,
            EventID eventID,
            String eventOutcomeIndicator,
            AuditLogger logger) {
        super();
        this.source = source;
        this.destination = destination;
        this.requestor = requestor;
        this.instances = instances;
        this.eventID = eventID;
        this.eventOutcomeIndicator = eventOutcomeIndicator;
        this.logger = logger;
        init();
    }

    private void init() {

        // Event
        this.setEventIdentification(AuditMessages.createEventIdentification(
                eventID, EventActionCode.Read,
                logger.timeStamp(), eventOutcomeIndicator, null));

        // Active Participant 1: Process that sent the data
        this.getActiveParticipant().add(
                AuditMessages.createActiveParticipant((source
                        .getIdentity() != null) ? source.getIdentity()
                        : "ANONYMOUS", null, null, true, source.getHost(),
                        AuditMessages.NetworkAccessPointTypeCode.MachineName,
                        null, AuditMessages.RoleIDCode.Destination));

        // Active Participant 2: The process that received the data.
        this.getActiveParticipant().add(
                AuditMessages.createActiveParticipant((destination
                        .getIdentity() != null) ? destination.getIdentity()
                        : "ANONYMOUS", null, null, true, destination.getHost(),
                        AuditMessages.NetworkAccessPointTypeCode.MachineName,
                        null, AuditMessages.RoleIDCode.Destination));

        // Active Participant 3: The process that requested the operation.
        if (requestor!=null)
        this.getActiveParticipant().add(
                AuditMessages.createActiveParticipant((requestor
                        .getIdentity() != null) ? requestor.getIdentity()
                        : "ANONYMOUS", null, null, true, requestor.getHost(),
                        AuditMessages.NetworkAccessPointTypeCode.MachineName,
                        null, AuditMessages.RoleIDCode.ApplicationLauncher));

        
        // Participating Object: one for each Study being transferred
        HashMap<String, List<ArchiveInstanceLocator>> map = groupInstancePerStudy(instances);
        for (String studyuid : map.keySet()) {
            ParticipantObjectDescription pod = createRetrieveObjectPOD(logger,
                    map.get(studyuid));
            this.getParticipantObjectIdentification()
                    .add(AuditMessages
                            .createParticipantObjectIdentification(
                                    studyuid,
                                    AuditMessages.ParticipantObjectIDTypeCode.StudyInstanceUID,
                                    null,
                                    null,
                                    AuditMessages.ParticipantObjectTypeCode.SystemObject,
                                    AuditMessages.ParticipantObjectTypeCodeRole.Report,
                                    null, null, pod));
        }

        // Participating Object: Patient
        String patientID = getPatientID(instances);
        if (patientID!=null) {
        this.getParticipantObjectIdentification()
                .add(AuditMessages
                        .createParticipantObjectIdentification(
                                patientID,
                                AuditMessages.ParticipantObjectIDTypeCode.PatientNumber,
                                null,
                                null,
                                AuditMessages.ParticipantObjectTypeCode.Person,
                                AuditMessages.ParticipantObjectTypeCodeRole.Patient,
                                null, null, null));
        }

        this.getAuditSourceIdentification().add(
                logger.createAuditSourceIdentification());
    }

    /**
     * Returns (and logs) only instances having a study instance uid.
     */
    private HashMap<String, List<ArchiveInstanceLocator>> groupInstancePerStudy(
            List<ArchiveInstanceLocator> instances) {

        HashMap<String, List<ArchiveInstanceLocator>> map = new HashMap<String, List<ArchiveInstanceLocator>>();

        for (ArchiveInstanceLocator instance : instances) {

            if (instance.getObject() != null
                    && instance.getObject() instanceof Attributes) {

                String studyInstanceUID = ((Attributes) instance.getObject())
                        .getString(Tag.StudyInstanceUID);
                if (studyInstanceUID != null) {

                    if (map.get(studyInstanceUID) == null)
                        map.put(studyInstanceUID,
                                new ArrayList<ArchiveInstanceLocator>());

                    map.get(studyInstanceUID).add(instance);
                }
            }
        }

        return map;
    }

    private static ParticipantObjectDescription createRetrieveObjectPOD(
            AuditLogger logger, List<ArchiveInstanceLocator> instances) {

        if (instances == null || instances.size() == 0) {
            return null;
        } else {
            ParticipantObjectDescription pod = new ParticipantObjectDescription();
            SOPClass sc = new SOPClass();
            sc.setUID(instances.get(0).cuid);
            if (logger.isIncludeInstanceUID()) {
                for (InstanceLocator instanceLocator : instances) {
                    Instance instance = new Instance();
                    instance.setUID(instanceLocator.iuid);
                    sc.getInstance().add(instance);
                }
            }
            sc.setNumberOfInstances(instances.size());
            pod.getSOPClass().add(sc);
            return pod;
        }
    }
    
    private String getPatientID(List<ArchiveInstanceLocator> instances) {
        String id = null;
        if (instances!=null)
            for (InstanceLocator instance : instances)
                if (instance.getObject() !=null &&
                    instance.getObject() instanceof Attributes &&
                    ((Attributes)instance.getObject()).getString(Tag.PatientID)!=null) {
                    id = ((Attributes) instance.getObject())
                            .getString(Tag.PatientID);
                    break;
                }
        return id;
    }

}
