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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.AuditMessages.EventActionCode;
import org.dcm4che3.audit.AuditMessages.EventID;
import org.dcm4che3.audit.AuditMessages.EventTypeCode;
import org.dcm4che3.audit.AuditMessages.ParticipantObjectIDTypeCode;
import org.dcm4che3.audit.AuditMessages.ParticipantObjectTypeCode;
import org.dcm4che3.audit.AuditMessages.ParticipantObjectTypeCodeRole;
import org.dcm4che3.audit.AuditMessages.RoleIDCode;
import org.dcm4che3.audit.ParticipantObjectDetail;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4chee.archive.dto.Participant;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class SecurityAlertAudit extends AuditMessage {

    private String node, eventOutcomeIndicator;
    private Throwable exception;
    private AuditLogger logger;
    private Participant source;

    /**
     */
    public SecurityAlertAudit(String node, 
            String eventOutcomeIndicator, 
            Throwable exception, 
            AuditLogger logger, 
            Participant source) {
        super();
        this.node = node;
        this.eventOutcomeIndicator = eventOutcomeIndicator;
        this.exception = exception;
        this.logger = logger;
        this.source = source;
        init();
    }

    private void init() {

        // Event
        this.setEventIdentification(AuditMessages.createEventIdentification(
                EventID.SecurityAlert, EventActionCode.Execute, logger
                        .timeStamp(), eventOutcomeIndicator, null,
                        EventTypeCode.NodeAuthentication));

        // Active Participant 1: Thise node
        this.getActiveParticipant().add(
                logger.createActiveParticipant(false, RoleIDCode.Application));

        // Participating Object: Alert Subject
        ParticipantObjectDetail poid = null;
        if (exception!=null)
        {
            byte[] exceptionBytes = (exception.getClass().getCanonicalName() + ":" + exception.getMessage()).getBytes();
            poid = new ParticipantObjectDetail();
            poid.setType("Alert Description");
            poid.setValue(exceptionBytes);
        }
        
        this.getParticipantObjectIdentification()
                .add(AuditMessages
                        .createParticipantObjectIdentification(
                                node,
                                ParticipantObjectIDTypeCode.NodeID, //IDTypeCode 
                                null, //name
                                null, //query
                                ParticipantObjectTypeCode.SystemObject, //TypeCode 
                                ParticipantObjectTypeCodeRole.SecurityResource, //Role
                                null, //lifeCycle 
                                null, //sensitivity
                                null, //description
                                poid));
        
        this.setAuditSourceIdentification(logger.createAuditSourceIdentification());
    }
    
    private static byte[] toBytes (String s)
    {
        if (s == null)
            return null;
        else
        {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = null;
                out = new ObjectOutputStream(bos);   
                out.writeObject(s);
                byte[] ret = bos.toByteArray();
                out.close();
                bos.close();
                return ret;
            } catch (IOException ioe) {
               return null;
            }
        }
    }
}
