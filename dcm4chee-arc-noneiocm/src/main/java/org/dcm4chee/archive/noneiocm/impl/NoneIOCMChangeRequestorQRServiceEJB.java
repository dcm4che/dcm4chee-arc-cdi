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

package org.dcm4chee.archive.noneiocm.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4chee.archive.entity.QCActionHistory.QCLevel;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.noneiocm.NoneIOCMChangeRequestorQRService;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;


/**
 * @author Franz Willer <franz.willer@gmail.com>
 */
@Stateless
public class NoneIOCMChangeRequestorQRServiceEJB implements NoneIOCMChangeRequestorQRService {

	private static final String NONE_IOCM_QC_HISTORY = "NONE_IOCM_QC_HISTORY";

    @PersistenceContext(name="dcm4chee-arc")
    EntityManager em;


    @Override
    public void updateQueryRequestAttributes(Attributes keys, Collection<String> sourceDeviceAETs) {
        String[] uids = null;
        NoneIocmQRLevel level = NoneIocmQRLevel.valueOf(keys.getString(Tag.QueryRetrieveLevel));
        for ( ; level != null ; level = level.parent()) {
            uids = level.getUids(keys);
            if (uids != null && uids.length > 0)
                break;
        }
        if (uids != null)
            updateRequestUIDs(sourceDeviceAETs, level, keys, findHistory(level, true, uids));
   }
    
    @Override
    public void updateQueryResponseAttributes(QueryContext context, Attributes match) {
        Attributes keys = context.getKeys();
        String studyIUID = match.getString(Tag.StudyInstanceUID);
        NoneIocmQRLevel level = NoneIocmQRLevel.valueOf(keys.getString(Tag.QueryRetrieveLevel));
        List<QCInstanceHistory> history = null;
        @SuppressWarnings("unchecked")
        HashMap<String, List<QCInstanceHistory>> historyMap = (HashMap<String, List<QCInstanceHistory>>) context.getProperty(NONE_IOCM_QC_HISTORY);
        if (historyMap == null) {
            historyMap = new HashMap<String, List<QCInstanceHistory>>();
            context.setProperty(NONE_IOCM_QC_HISTORY, historyMap);
        }
        if (historyMap.containsKey(match.getString(Tag.StudyInstanceUID))) {
            history = historyMap.get(studyIUID);
        } else {
            history = findHistory(NoneIocmQRLevel.STUDY, false, studyIUID);
            historyMap.put(studyIUID, history);
        }
        updateResponseUIDs(context.getRemoteDevice().getApplicationAETitles(), level, match, history);
   }

	@Override
	public void updateRetrieveResponseAttributes(CStoreSCUContext context, Attributes attrs) {
        String studyIUID = attrs.getString(Tag.StudyInstanceUID);
		List<QCInstanceHistory> history = null;
        @SuppressWarnings("unchecked")
        HashMap<String, List<QCInstanceHistory>> historyMap = (HashMap<String, List<QCInstanceHistory>>) context.getProperty(NONE_IOCM_QC_HISTORY);
        if (historyMap == null) {
            historyMap = new HashMap<String, List<QCInstanceHistory>>();
            context.setProperty(NONE_IOCM_QC_HISTORY, historyMap);
        }
        if (historyMap.containsKey(attrs.getString(Tag.StudyInstanceUID))) {
            history = historyMap.get(studyIUID);
        } else {
            history = findHistory(NoneIocmQRLevel.STUDY, false, studyIUID);
            historyMap.put(studyIUID, history);
        }
        updateResponseUIDs(context.getRemoteAE().getDevice().getApplicationAETitles(), NoneIocmQRLevel.IMAGE, attrs, history);
	}
    
    @SuppressWarnings("unchecked")
    private List<QCInstanceHistory> findHistory(NoneIocmQRLevel level, boolean oldToNew, String... uids) {
        Query query = em.createNamedQuery(level.getHistoryQueryName(oldToNew));
        query.setParameter("uids", Arrays.asList(uids));
        return (List<QCInstanceHistory>) query.getResultList();
    }
    
    private QCInstanceHistory selectOldestHistory(NoneIocmQRLevel level, String uid, List<QCInstanceHistory> history) {
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
    
    private void updateRequestUIDs(Collection<String> collection, NoneIocmQRLevel qrLevel, Attributes keys, List<QCInstanceHistory> histories) {
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
	        	QCLevel qcLevel = history.getSeries().getStudy().getAction().getLevel();
	            switch (qrLevel) {
	            case IMAGE:
	                keys.setString(Tag.SOPInstanceUID, VR.UI, uids);
	                uids = new String[]{NoneIocmQRLevel.SERIES.getNewUID(history)};
	            case SERIES:
	                if (qcLevel == QCLevel.SERIES || qcLevel == QCLevel.INSTANCE)
	                	break;
	                keys.setString(Tag.SeriesInstanceUID, VR.UI, uids);
	                uids = new String[]{NoneIocmQRLevel.STUDY.getNewUID(history)};
	            case STUDY: 
	            	if (qcLevel == QCLevel.PATIENT)
	                	keys.setString(Tag.StudyInstanceUID, VR.UI, uids);
	            default:
	                break;
	            }
            }
        }
    }
    private void updateResponseUIDs(Collection<String> remoteAETs, NoneIocmQRLevel qrLevel, Attributes match, List<QCInstanceHistory> history) {
        QCInstanceHistory oldest = selectOldestHistory(qrLevel, qrLevel.getUids(match)[0], history);
        if (oldest != null && oldest != null) {
            String noneIocmSourceAET = oldest.getSeries().getNoneIOCMSourceAET();
            if (noneIocmSourceAET != null && remoteAETs.contains(noneIocmSourceAET)) {
	            QCLevel qcLevel = oldest.getSeries().getStudy().getAction().getLevel();
	            switch (qrLevel) {
	            case IMAGE:
	                match.setString(Tag.SOPInstanceUID, VR.UI, NoneIocmQRLevel.IMAGE.getOldUID(oldest));
	            case SERIES:
	                if (qcLevel == QCLevel.STUDY || qcLevel == QCLevel.PATIENT)
	                    match.setString(Tag.SeriesInstanceUID, VR.UI, NoneIocmQRLevel.SERIES.getOldUID(oldest));
	            case STUDY: 
	            	if (qcLevel == QCLevel.PATIENT)
	            		match.setString(Tag.StudyInstanceUID, VR.UI, NoneIocmQRLevel.STUDY.getOldUID(oldest));
	            default:
	                break;
	            }
            }
        }
    }
}
