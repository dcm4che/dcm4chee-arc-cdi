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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.management.Query;

import org.dcm4che.archive.audit.message.PixQueryAudit;
import org.dcm4che.archive.audit.message.QueryAudit;
import org.dcm4che.archive.audit.message.RetrieveAudit;
import org.dcm4che.archive.audit.message.SecurityAlertAudit;
import org.dcm4che.archive.audit.message.StartStopAudit;
import org.dcm4che.archive.audit.message.StoreAudit;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.AuditMessages.*;
import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4chee.archive.ArchiveServiceStarted;
import org.dcm4chee.archive.ArchiveServiceStopped;
import org.dcm4chee.archive.event.ConnectionEvent;
import org.dcm4chee.archive.event.StartStopEvent;
import org.dcm4chee.archive.mima.impl.PixQueryEvent;
import org.dcm4chee.archive.query.impl.QueryEvent;
import org.dcm4chee.archive.retrieve.impl.ArchiveInstanceLocator;
import org.dcm4chee.archive.retrieve.impl.RetrieveAfterSendEvent;
import org.dcm4chee.archive.retrieve.impl.RetrieveBeforeSendEvent;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.store.StoreSessionClosed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observer receiving events (like store, query, association) to be audited.
 * Implements the ITI-20 transaction of IHE actor Secure Node (see IHE ITI
 * Technical Framework, Vol. 2 - Section 3.20).
 * 
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
@ApplicationScoped
public class AuditObserver {

    private static final String AUDIT_MESSAGES_SUCCESS = "SuccessAudits";
    private static final String AUDIT_MESSAGES_FAILURE = "FailedAudits";

    protected static final Logger LOG = LoggerFactory
            .getLogger(AuditObserver.class);

    public void receiveStoreContext(@Observes StoreContext context) {

        StoreSession session = context.getStoreSession();
        AuditLogger logger = getLogger(session.getDevice());
        String studyID = context.getAttributes()
                .getString(Tag.StudyInstanceUID);

        HashMap<String, StoreAudit> auditMap = getOrCreateAuditsMap(session,
                context.isFail());

        if (auditMap.get(studyID) == null)
            auditMap.put(
                    studyID,
                    new StoreAudit(session.getRemoteAET(), session.getSource(), 
                            context.getAttributes(), context
                            .isFail() ? EventOutcomeIndicator.SeriousFailure
                            : EventOutcomeIndicator.Success, logger));
        else {
            StoreAudit existingAudit = auditMap.get(studyID);
            existingAudit.addInstance(context.getAttributes());
        }
    }

    public void receiveStoreSessionClosed(
            @Observes @StoreSessionClosed StoreSession session) {

        AuditLogger logger = getLogger(session.getDevice());
        
        // send all the audits at once
        HashMap<String, StoreAudit> success = (HashMap<String, StoreAudit>) session
                .getProperty(AUDIT_MESSAGES_SUCCESS);
        if (success != null)
            for (String studyUID : success.keySet())
                sendAuditMessage(success.get(studyUID), logger);

        HashMap<String, StoreAudit> failures = (HashMap<String, StoreAudit>) session
                .getProperty(AUDIT_MESSAGES_FAILURE);
        if (failures != null)
            for (String studyUID : failures.keySet())
                sendAuditMessage(failures.get(studyUID), logger);
    }
    
    public void receiveArchiveServiceStarted(
            @Observes @ArchiveServiceStarted StartStopEvent event) {        
        AuditLogger logger = getLogger(event.getDevice());
        sendAuditMessage (new StartStopAudit(true, logger, event.getSource()),logger);
    }

    public void receiveArchiveServiceStopped(
            @Observes @ArchiveServiceStopped StartStopEvent event) {
        AuditLogger logger = getLogger(event.getDevice());
        sendAuditMessage (new StartStopAudit(false, logger, event.getSource()), logger);
    }
    
    public void receiveQuery(
            @Observes QueryEvent event) {
        AuditLogger logger = getLogger(event.getDevice());
        sendAuditMessage (new QueryAudit(event, logger), logger);
    }
    
    public void receiveRetrieveBeforeSend(
            @Observes RetrieveBeforeSendEvent event) {
        AuditLogger logger = getLogger(event.getDevice());
        sendAuditMessage (new RetrieveAudit(event.getSource(),
                event.getDestination(), event.getRequestor(),
                event.getInstances(),
                EventID.BeginTransferringDICOMInstances,
                EventOutcomeIndicator.Success, logger), logger);
    }
    
    public void receiveRetrieveAfterSend(
            @Observes RetrieveAfterSendEvent event) {
        
        AuditLogger logger = getLogger(event.getDevice());
        
        List<InstanceLocator> success = new ArrayList<InstanceLocator>();
        
        //completed and warning instances are considered as "success"
        //failed instances are considered "minor failures", unless all the instances
        //are failed, in that case is a "major failure".
        if (event.getCompleted()!= null && event.getCompleted().size()>0)
            success.addAll(event.getCompleted());
        
        if (event.getWarning()!= null && event.getCompleted().size()>0)
            success.addAll(event.getWarning());
        

        if (success.size()>0)
            sendAuditMessage (new RetrieveAudit(event.getSource(),
                    event.getDestination(), event.getRequestor(),
                    success,
                    EventID.DICOMInstancesTransferred,
                    EventOutcomeIndicator.Success, logger), logger);
        
        if (event.getFailed()!=null && event.getFailed().size()>0) {
            
            if (success.size()>0) //there are failures and successes
                sendAuditMessage (new RetrieveAudit(event.getSource(),
                        event.getDestination(), event.getRequestor(),
                        event.getFailed(),
                        EventID.DICOMInstancesTransferred,
                        EventOutcomeIndicator.MinorFailure, logger), logger);
            else //all the instance are failed: major failure
                sendAuditMessage (new RetrieveAudit(event.getSource(),
                        event.getDestination(), event.getRequestor(),
                        event.getFailed(),
                        EventID.DICOMInstancesTransferred,
                        EventOutcomeIndicator.MajorFailure, logger), logger);
        }
    } 
    
    public void receiveConnectionEvent(
            @Observes ConnectionEvent event) {
        AuditLogger logger = getLogger(event.getDevice());
        sendAuditMessage (new SecurityAlertAudit(event.getNode(),
                event.isFail() ? EventOutcomeIndicator.MinorFailure : EventOutcomeIndicator.Success,
                event.getException(),        
                logger,
                event.getSource()), logger);
    }
    
    public void receivePixQueryEvent(
            @Observes PixQueryEvent event) {
        AuditLogger logger = getLogger(event.getDevice());
        sendAuditMessage (new PixQueryAudit(event.getDestination(), 
                event.getPatId(), event.getMessageControlId(), 
                event.getQuery(), logger), logger);
    }
    
    private HashMap<String, StoreAudit> getOrCreateAuditsMap(
            StoreSession session, boolean fail) {
        String mapType = fail ? AUDIT_MESSAGES_FAILURE : AUDIT_MESSAGES_SUCCESS;

        // if not existing, create a new map for audits (failed or success)
        if (session.getProperty(mapType) == null)
            session.setProperty(mapType, new HashMap<String, StoreAudit>());

        return (HashMap<String, StoreAudit>) session.getProperty(mapType);
    }

    private AuditLogger getLogger(Device device) {

        if (device.getDeviceExtension(AuditLogger.class) == null) {
            AuditLogger auditLogger = new AuditLogger();
            device.addDeviceExtension(auditLogger);
        }

        return device.getDeviceExtension(AuditLogger.class);
    }

    private void sendAuditMessage(AuditMessage msg, AuditLogger logger) {

        if (msg == null)
            return;

        if (logger == null || !logger.isInstalled())
            return;

        try {
            logger.write(logger.timeStamp(), msg);

        } catch (Exception e) {
            
            LOG.error("Failed to write audit log message: {}", e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
    }

}
