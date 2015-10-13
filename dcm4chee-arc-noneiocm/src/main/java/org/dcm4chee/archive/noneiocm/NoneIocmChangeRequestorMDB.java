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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.dto.ActiveService;
import org.dcm4chee.archive.dto.QCEventInstance;
import org.dcm4chee.archive.entity.ActiveProcessing;
import org.dcm4chee.archive.processing.ActiveProcessingService;
import org.dcm4chee.archive.qc.QCBean;
import org.dcm4chee.archive.qc.QCEvent;
import org.dcm4chee.archive.qc.QCOperationNotPermittedException;
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
public class NoneIocmChangeRequestorMDB implements MessageListener{

    private static final Logger LOG = LoggerFactory.getLogger(NoneIocmChangeRequestorMDB.class);

    @Inject
    private ActiveProcessingService activeProcessingService;
    
    @Inject
    private QCBean qcBean;

    @Override
    public void onMessage(Message message) {
        try {
            String apsStudyIUID = message.getStringProperty("studyIUID");
            List<ActiveProcessing> aps = activeProcessingService.getActiveProcessesByStudy(apsStudyIUID, ActiveService.NONE_IOCM_UPDATE);
            LOG.debug("NONE IOCM ActiveProcessing for study {}: {}", apsStudyIUID, aps);
            Attributes attrs, item;
            HashMap<String, String> sopUIDchanged = new HashMap<String, String>();
            HashMap<String, List<String>> studyUIDchanged = new HashMap<String, List<String>>();
            HashMap<String, List<String>> seriesUIDchanged = new HashMap<String, List<String>>();
            HashMap<String, List<String>> patIDchanged = new HashMap<String, List<String>>();
            HashMap<String, Attributes> patAttrChanged = new HashMap<String, Attributes>();
            for (ActiveProcessing p : aps) {
                attrs = p.getAttributes();
                item = attrs.getNestedDataset(Tag.ModifiedAttributesSequence);
                if (item.contains(Tag.StudyInstanceUID)) {
                    addChgdIUID(studyUIDchanged, attrs.getString(Tag.StudyInstanceUID), attrs.getString(Tag.SOPInstanceUID));
                } else if (item.contains(Tag.SeriesInstanceUID)) {
                    addChgdIUID(seriesUIDchanged, attrs.getString(Tag.SeriesInstanceUID), attrs.getString(Tag.SOPInstanceUID));
                } else if (item.contains(Tag.SOPInstanceUID)) {
                    sopUIDchanged.put(attrs.getString(Tag.SOPInstanceUID), item.getString(Tag.SOPInstanceUID));
                } else if (item.contains(Tag.PatientID)) {
                    String pid = attrs.getString(Tag.PatientID)+"^^^"+attrs.getString(Tag.IssuerOfPatientID);
                    addChgdIUID(patIDchanged, pid, attrs.getString(Tag.SOPInstanceUID));
                    patAttrChanged.put(pid,  attrs);
                } else {
                    LOG.warn("Cannot determine NoneIOCM change! Ignore this active process ({})", p);
                }
            }
            Code rejNoteCode = NoneIOCMChangeRequestorService.REJ_CODE_QUALITY_REASON;
            if (sopUIDchanged.size() > 0) {
            	QCEvent event = qcBean.replaced(sopUIDchanged, rejNoteCode);
            	ArrayList<String> targets = new ArrayList<String>(event.getTarget().size());
            	for ( QCEventInstance qcI : event.getTarget()) {
            		targets.add(qcI.getSopInstanceUID());
            	}
        		activeProcessingService.deleteActiveProcessBySOPInstanceUIDsAndService(targets, ActiveService.NONE_IOCM_UPDATE);
            }
            if (seriesUIDchanged.size() > 0) {
                Attributes seriesAttrs = new Attributes();
                for ( Map.Entry<String, List<String>> e : seriesUIDchanged.entrySet()) {
                    seriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI, e.getKey());
                    qcBean.split(e.getValue(), null, apsStudyIUID, null, seriesAttrs, rejNoteCode);
                    activeProcessingService.deleteActiveProcessBySOPInstanceUIDsAndService(e.getValue(), ActiveService.NONE_IOCM_UPDATE);
                }
            }
            if (studyUIDchanged.size() > 0) {
                for ( Map.Entry<String, List<String>> e : studyUIDchanged.entrySet()) {
                    qcBean.split(e.getValue(), null, e.getKey(), null, null, rejNoteCode);
                    activeProcessingService.deleteActiveProcessBySOPInstanceUIDsAndService(e.getValue(), ActiveService.NONE_IOCM_UPDATE);
                }
            }
            if (patIDchanged.size() > 0) {
                for ( Map.Entry<String, List<String>> e : patIDchanged.entrySet()) {
                    IDWithIssuer pid = IDWithIssuer.pidOf(patAttrChanged.get(e.getKey()));
                    String studyUID = UIDUtils.createUID();
                    qcBean.split(e.getValue(), pid, studyUID, null, null, rejNoteCode);
                    activeProcessingService.deleteActiveProcessBySOPInstanceUIDsAndService(e.getValue(), ActiveService.NONE_IOCM_UPDATE);
                }
            }
        } catch(QCOperationNotPermittedException e) {
            LOG.warn("QC operation not permitted", e);
        } catch (Throwable th) {
            LOG.warn("Failed to process " + message, th);
        } 
    }
    
    private List<String> addChgdIUID(Map<String, List<String>> map, String key, String iuid) {
        List<String> chgdIUIDs = map.get(key);
        if (chgdIUIDs == null) {
            chgdIUIDs = new ArrayList<String>();
            map.put(key, chgdIUIDs);
        }
        chgdIUIDs.add(iuid);
        return chgdIUIDs;
    }

}
