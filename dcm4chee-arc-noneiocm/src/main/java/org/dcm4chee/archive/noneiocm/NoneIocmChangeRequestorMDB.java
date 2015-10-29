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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4chee.archive.noneiocm;

import static org.dcm4chee.archive.noneiocm.NoneIOCMChangeRequestorService.REJ_CODE_QUALITY_REASON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.dto.ActiveService;
import org.dcm4chee.archive.entity.ActiveProcessing;
import org.dcm4chee.archive.processing.ActiveProcessingService;
import org.dcm4chee.archive.qc.QCBean;
import org.dcm4chee.archive.qc.QCOperationNotPermittedException;
import org.dcm4chee.archive.studyprotection.StudyProtectionHook;
import org.dcm4chee.hooks.Hooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * 
 */

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType",
                                  propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination",
                                  propertyValue = "queue/noneiocm"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode",
                                  propertyValue = "Auto-acknowledge") })
public class NoneIocmChangeRequestorMDB implements MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(NoneIocmChangeRequestorMDB.class);
    
    public static final String UPDATED_STUDY_UID_MSG_PROPERTY = "studyIUID";
    
    private static final String JMS_DELIVERY_COUNT_MSG_PROPERTY = "JMSXDeliveryCount";

    @EJB
    private ActiveProcessingService activeProcessingService;
    
    @Inject
    private QCBean qcBean;
    
    @Inject
    private Hooks<StudyProtectionHook> studyProtectionHooks;

    @Override
    public void onMessage(Message msg) {
        String processStudyIUID = getProcessStudyUID(msg);
        if(processStudyIUID == null) {
            return;
        }
        
        List<ActiveProcessing> nonIocmProcessings = getNonIOCMActiveProcessings(processStudyIUID);
        if(nonIocmProcessings == null) {
            return;
        }

        try {
            LOG.debug("Non-IOCM active-processing message for study {}", processStudyIUID);
            
            // check if study protected against structural changes
            if(!areStructuralChangesPermittedForStudy(processStudyIUID)) {
                LOG.info("Non-IOCM active-processing message for study {} will be ignored as study is protected", processStudyIUID);
                activeProcessingService.deleteActiveProcesses(nonIocmProcessings);
                return;
            }
            
            Map<String, String> sopUIDchanged = new HashMap<String, String>();
            Map<String, List<String>> studyUIDchanged = new HashMap<String, List<String>>();
            Map<String, List<String>> seriesUIDchanged = new HashMap<String, List<String>>();
            Map<String, List<String>> patIDchanged = new HashMap<String, List<String>>();
            Map<String, Attributes> patAttrChanged = new HashMap<String, Attributes>();
            
            for (ActiveProcessing processing : nonIocmProcessings) {
                Attributes attrs = processing.getAttributes();
                Attributes item = attrs.getNestedDataset(Tag.ModifiedAttributesSequence);
                if (item.contains(Tag.StudyInstanceUID)) {
                    addChgdIUID(studyUIDchanged, attrs.getString(Tag.StudyInstanceUID), attrs.getString(Tag.SOPInstanceUID));
                } else if (item.contains(Tag.SeriesInstanceUID)) {
                    addChgdIUID(seriesUIDchanged, attrs.getString(Tag.SeriesInstanceUID), attrs.getString(Tag.SOPInstanceUID));
                } else if (item.contains(Tag.SOPInstanceUID)) {
                    sopUIDchanged.put(attrs.getString(Tag.SOPInstanceUID), item.getString(Tag.SOPInstanceUID));
                } else if (item.contains(Tag.PatientID)) {
                    String pid = attrs.getString(Tag.PatientID) + "^^^" + attrs.getString(Tag.IssuerOfPatientID);
                    addChgdIUID(patIDchanged, pid, attrs.getString(Tag.SOPInstanceUID));
                    patAttrChanged.put(pid,  attrs);
                } else {
                    LOG.warn("Cannot determine Non-IOCM change! Ignore this active-processing ({})", processing);
                }
            }
            
            if (sopUIDchanged.size() > 0) {
                try {
                    qcBean.replaced(sopUIDchanged, REJ_CODE_QUALITY_REASON);
                } catch(QCOperationNotPermittedException e1) {
                    LOG.warn("QC Replace-Operation not permitted", e1);
                }
            }
            if (seriesUIDchanged.size() > 0) {
                Attributes seriesAttrs = new Attributes();
                for (Map.Entry<String, List<String>> e : seriesUIDchanged.entrySet()) {
                    seriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI, e.getKey());
                    try {
                        qcBean.split(e.getValue(), null, processStudyIUID, null, seriesAttrs, REJ_CODE_QUALITY_REASON);
                    } catch (QCOperationNotPermittedException e2) {
                        LOG.warn("QC Split-Operation not permitted", e2);
                    }
                }
            }
            if (studyUIDchanged.size() > 0) {
                for (Map.Entry<String, List<String>> e : studyUIDchanged.entrySet()) {
                    try {
                        qcBean.split(e.getValue(), null, e.getKey(), null, null, REJ_CODE_QUALITY_REASON);
                    } catch (QCOperationNotPermittedException e3) {
                        LOG.warn("QC Split-Operation not permitted", e3);
                    }
                }
            }
            if (patIDchanged.size() > 0) {
                for (Map.Entry<String, List<String>> e : patIDchanged.entrySet()) {
                    IDWithIssuer pid = IDWithIssuer.pidOf(patAttrChanged.get(e.getKey()));
                    String studyUID = UIDUtils.createUID();
                    try {
                        qcBean.split(e.getValue(), pid, studyUID, null, null, REJ_CODE_QUALITY_REASON);
                    } catch (QCOperationNotPermittedException e4) {
                        LOG.warn("QC Split-Operation not permitted", e4);
                    }
                }
            }
            
            activeProcessingService.deleteActiveProcesses(nonIocmProcessings);
        } catch (Throwable th) {
            LOG.warn("Failed to process Non-IOCM active-processing message", th);
         
            int msgDeliveryCount = getMessageDeliveryCount(msg);
            //TODO: remove hard-coded number of retries
            // throw exception to force JMS retry
            if (msgDeliveryCount <= 3) {
                throw new EJBException("Failed to process Non-IOCM active-processing message");
            // no retry -> clean-up
            } else {
                activeProcessingService.deleteActiveProcesses(nonIocmProcessings);
            }
        } 
    }
    
    private static void addChgdIUID(Map<String, List<String>> map, String key, String iuid) {
        List<String> chgdIUIDs = map.get(key);
        if (chgdIUIDs == null) {
            chgdIUIDs = new ArrayList<String>();
            map.put(key, chgdIUIDs);
        }
        chgdIUIDs.add(iuid);
    }
    
    private boolean areStructuralChangesPermittedForStudy(String studyIUID) {
        for(StudyProtectionHook studyProtectionHook : studyProtectionHooks) {
            if(studyProtectionHook.isProtected(studyIUID)) {
                return false;
            }
        }
        return true;
    }
    
    private static String getProcessStudyUID(Message msg) {
        try {
            return msg.getStringProperty(UPDATED_STUDY_UID_MSG_PROPERTY);
        } catch (JMSException e) {
            LOG.error("Received invalid Non-IOCM active-processing message", e);
            return null;
        }
    }
    
    private List<ActiveProcessing> getNonIOCMActiveProcessings(String studyUID) {
        try {
            return activeProcessingService.getActiveProcessesByStudy(studyUID, ActiveService.NONE_IOCM_UPDATE);
        } catch (Exception e) {
            LOG.error("Could not fetch Non-IOCM active processings from database", e);
            return null;
        }
    }
    
    private static int getMessageDeliveryCount(Message msg) {
        try {
            return msg.getIntProperty(JMS_DELIVERY_COUNT_MSG_PROPERTY);
        } catch (JMSException e) {
            return Integer.MAX_VALUE;
        }
    }

}
