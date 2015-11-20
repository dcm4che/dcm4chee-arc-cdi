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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4chee.archive.noniocm.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.entity.QCActionHistory.QCLevel;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.noniocm.NonIOCMChangeRequestorQRService;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;


/**
 * @author Franz Willer <franz.willer@gmail.com>
 */
@Stateless
public class NonIOCMChangeRequestorQRServiceEJB implements NonIOCMChangeRequestorQRService {

    private static final String NON_IOCM_QC_HISTORY = "NON_IOCM_QC_HISTORY";

    @PersistenceContext(name="dcm4chee-arc")
    private EntityManager em;

    @Inject
    private IApplicationEntityCache aeCache;

    @Override
    public void updateQueryRequestAttributes(Attributes keys, Collection<String> sourceDeviceAETs) {
        NonIocmQRLevel level = getLevelWithUIDs(keys);
        if (level != null)
            updateRequestUIDs(sourceDeviceAETs, level, keys, findHistory(level, true, level.getUids(keys)), false);
   }
    
    @Override
    public void updateQueryResponseAttributes(QueryContext context, Attributes match) {
        Device remoteDevice = getRemoteDevice(context);
        if (remoteDevice != null) {
            Attributes keys = context.getKeys();
            String studyIUID = match.getString(Tag.StudyInstanceUID);
            NonIocmQRLevel level = keys.getString(Tag.QueryRetrieveLevel) != null ? 
                    NonIocmQRLevel.valueOf(keys.getString(Tag.QueryRetrieveLevel)) : getLevelWithUIDs(keys);
            if (level != null) {
                @SuppressWarnings("unchecked")
                HashMap<String, List<QCInstanceHistory>> historyMap = (HashMap<String, List<QCInstanceHistory>>) context.getProperty(NON_IOCM_QC_HISTORY);
                if (historyMap == null) {
                    historyMap = new HashMap<String, List<QCInstanceHistory>>();
                    context.setProperty(NON_IOCM_QC_HISTORY, historyMap);
                }
                List<QCInstanceHistory> history = historyMap.get(studyIUID);
                if(history == null) {
                    history = findHistory(NonIocmQRLevel.STUDY, false, studyIUID);
                    historyMap.put(studyIUID, history);
                }
                updateResponseUIDs(remoteDevice.getApplicationAETitles(), level, match, history);
            }
        }
    }

    @Override
    public Device getRemoteDevice(QueryContext context) {
        Device d = context.getRemoteDevice();
        if (d == null) {
            try {
                ApplicationEntity ae = aeCache.get(context.getRemoteAET());
                if (ae != null)
                    d = ae.getDevice();
            } catch (ConfigurationException ignore) {
            }
        }
        return d;
    }
    
    @Override
    public void updateRetrieveRequestAttributes(Attributes keys, Collection<String> sourceDeviceAETs) {
        NonIocmQRLevel level = getLevelWithUIDs(keys);
        if (level != null)
            updateRequestUIDs(sourceDeviceAETs, level, keys, findHistory(level, true, level.getUids(keys)), true);
    }
    
    @Override
    public void updateRetrieveResponseAttributes(CStoreSCUContext context,
            Attributes attrs) {
        String studyIUID = attrs.getString(Tag.StudyInstanceUID);
        List<QCInstanceHistory> history = null;
        @SuppressWarnings("unchecked")
        HashMap<String, List<QCInstanceHistory>> historyMap = (HashMap<String, List<QCInstanceHistory>>) context
                .getProperty(NON_IOCM_QC_HISTORY);
        if (historyMap == null) {
            historyMap = new HashMap<String, List<QCInstanceHistory>>();
            context.setProperty(NON_IOCM_QC_HISTORY, historyMap);
        }
        if (historyMap.containsKey(attrs.getString(Tag.StudyInstanceUID))) {
            history = historyMap.get(studyIUID);
        } else {
            history = findHistory(NonIocmQRLevel.STUDY, false, studyIUID);
            historyMap.put(studyIUID, history);
        }
        updateResponseUIDs(context.getRemoteAE().getDevice()
                .getApplicationAETitles(), NonIocmQRLevel.IMAGE, attrs,
                history);
    }

    private NonIocmQRLevel getLevelWithUIDs(Attributes keys) {
        NonIocmQRLevel level = NonIocmQRLevel.IMAGE;
        for (; level != null && level.getUids(keys) == null; level = level
                .parent())
            ;
        return level;
    }

    @SuppressWarnings("unchecked")
    private List<QCInstanceHistory> findHistory(NonIocmQRLevel level, boolean oldToNew, String... uids) {
        Query query = em.createNamedQuery(level.getHistoryQueryName(oldToNew));
        query.setParameter("uids", Arrays.asList(uids));
        return (List<QCInstanceHistory>) query.getResultList();
    }
    
    private void updateRequestUIDs(Collection<String> collection, NonIocmQRLevel qrLevel, Attributes keys, List<QCInstanceHistory> histories, boolean isRetrieve) {
        String[] uids = qrLevel.getUids(keys);
        if (histories != null && histories.size() != 0) {
            QCInstanceHistory history = null;
            for (int i = 0 ; i < uids.length ; i++) {
                for (QCInstanceHistory h : histories) {
                    if (qrLevel.getOldUID(h).equals(uids[i])) {
                        String noneIocmSourceAET = h.getSeries().getNoneIOCMSourceAET();
                        if (noneIocmSourceAET != null && collection.contains(noneIocmSourceAET)) {
                        	uids[i] = qrLevel.getNewUID(h);
                            if (history == null)
                                history = h;
                        }
                    }
                }
            }

            if (history != null) {
                QCLevel qcLevel = isRetrieve ? QCLevel.PATIENT : history.getSeries().getStudy().getAction().getLevel();
                switch (qrLevel) {
                case IMAGE:
                    keys.setString(Tag.SOPInstanceUID, VR.UI, uids);
                    uids = new String[] { NonIocmQRLevel.SERIES.getNewUID(history) };
                case SERIES:
                    if (qcLevel == QCLevel.SERIES || qcLevel == QCLevel.INSTANCE)
                        break;
                    keys.setString(Tag.SeriesInstanceUID, VR.UI, uids);
                    uids = new String[] { NonIocmQRLevel.STUDY.getNewUID(history) };
                case STUDY:
                    if (qcLevel == QCLevel.PATIENT)
                        keys.setString(Tag.StudyInstanceUID, VR.UI, uids);
                default:
                    break;
                }
            }
        }
    }
    private void updateResponseUIDs(Collection<String> remoteAETs, NonIocmQRLevel qrLevel, Attributes match, List<QCInstanceHistory> history) {
        QCInstanceHistory oldest = selectOldestHistory(qrLevel, match, history);
        if (oldest != null && oldest != null) {
            String noneIocmSourceAET = oldest.getSeries().getNoneIOCMSourceAET();
            if (noneIocmSourceAET != null && remoteAETs.contains(noneIocmSourceAET)) {
                QCLevel qcLevel = oldest.getSeries().getStudy().getAction().getLevel();
                switch (qrLevel) {
                case IMAGE:
                    match.setString(Tag.SOPInstanceUID, VR.UI, NonIocmQRLevel.IMAGE.getOldUID(oldest));
                case SERIES:
                    if (qcLevel == QCLevel.STUDY || qcLevel == QCLevel.PATIENT)
                        match.setString(Tag.SeriesInstanceUID, VR.UI, NonIocmQRLevel.SERIES.getOldUID(oldest));
                case STUDY: 
                    if (qcLevel == QCLevel.PATIENT)
                        match.setString(Tag.StudyInstanceUID, VR.UI, NonIocmQRLevel.STUDY.getOldUID(oldest));
                default:
                    break;
                }
            }
        }
    }
    
    private QCInstanceHistory selectOldestHistory(NonIocmQRLevel level, Attributes match, List<QCInstanceHistory> history) {
        if (level.getUids(match) == null)
            return null;
        String uid = level.getUids(match)[0];
        QCInstanceHistory result = null;
        if (history != null && history.size() != 0) {
             for (QCInstanceHistory h : history) {
                if (level.getNewUID(h).equals(uid) && (result == null || result.getPk() > h.getPk())) {
                    result = h;
                }
            }
        }
        return result;
    }

}
