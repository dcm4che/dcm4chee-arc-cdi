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
import org.dcm4che3.audit.AuditMessages.EventOutcomeIndicator;
import org.dcm4che3.audit.AuditMessages.RoleIDCode;
import org.dcm4che3.audit.Instance;
import org.dcm4che3.audit.ParticipantObjectDescription;
import org.dcm4che3.audit.SOPClass;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4chee.archive.dto.Participant;
import org.dcm4chee.archive.retrieve.impl.RetrieveEvent;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
public class RetrieveAudit extends AuditMessage {

    private RetrieveEvent event;
    private AuditLogger logger;

    /**
     */
    public RetrieveAudit(
            RetrieveEvent event,
            AuditLogger logger) {
        super();
        this.event = event;
        this.logger = logger;
        init();
    }

    private void init() {

        // Event
        this.setEventIdentification(AuditMessages.createEventIdentification(
                EventID.DICOMInstancesTransferred, EventActionCode.Read,
                logger.timeStamp(), EventOutcomeIndicator.Success, null));

        // Active Participant 1: Process that sent the data
        this.getActiveParticipant().add(
                AuditMessages.createActiveParticipant((event.getSource()
                        .getIdentity() != null) ? event.getSource().getIdentity()
                        : "ANONYMOUS", null, null, true, event.getSource().getHost(),
                        AuditMessages.NetworkAccessPointTypeCode.MachineName,
                        null, AuditMessages.RoleIDCode.Destination));

        // Active Participant 2: The process that received the data.
        this.getActiveParticipant().add(
                AuditMessages.createActiveParticipant((event.getDestination()
                        .getIdentity() != null) ? event.getDestination().getIdentity()
                        : "ANONYMOUS", null, null, true, event.getDestination().getHost(),
                        AuditMessages.NetworkAccessPointTypeCode.MachineName,
                        null, AuditMessages.RoleIDCode.Destination));

        // Active Participant 3: The process that requested the operation.
        if (event.getRequestor()!=null)
        this.getActiveParticipant().add(
                AuditMessages.createActiveParticipant((event.getRequestor()
                        .getIdentity() != null) ? event.getRequestor().getIdentity()
                        : "ANONYMOUS", null, null, true, event.getRequestor().getHost(),
                        AuditMessages.NetworkAccessPointTypeCode.MachineName,
                        null, AuditMessages.RoleIDCode.ApplicationLauncher));

        
        // Participating Object: one for each Study being transferred
        HashMap<String, List<InstanceLocator>> map = groupInstancePerStudy(event.getInstances());
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
        String patientID = getPatientID(event.getInstances());
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
    private HashMap<String, List<InstanceLocator>> groupInstancePerStudy(
            List<InstanceLocator> instances) {

        HashMap<String, List<InstanceLocator>> map = new HashMap<String, List<InstanceLocator>>();

        for (InstanceLocator instance : instances) {

            if (instance.getObject() != null
                    && instance.getObject() instanceof Attributes) {

                String studyInstanceUID = ((Attributes) instance.getObject())
                        .getString(Tag.StudyInstanceUID);
                if (studyInstanceUID != null) {

                    if (map.get(studyInstanceUID) == null)
                        map.put(studyInstanceUID,
                                new ArrayList<InstanceLocator>());

                    map.get(studyInstanceUID).add(instance);
                }
            }
        }

        return map;
    }

    private static ParticipantObjectDescription createRetrieveObjectPOD(
            AuditLogger logger, List<InstanceLocator> instances) {

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
    
    private String getPatientID(List<InstanceLocator> instances) {
        String id = null;
        if (instances!=null)
            for (InstanceLocator instance : instances)
                if (instance.getObject() !=null &&
                    instance.getObject() instanceof Attributes &&
                    ((Attributes)instance.getObject()).getString(Tag.PatientID)!=null) {
                        id = ((Attributes)instance.getObject()).getString(Tag.PatientID);
                        break;
                }
        return id;
    }

}
