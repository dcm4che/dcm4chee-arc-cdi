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
package org.dcm4chee.archive.qc.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.qc.QCBean;
import org.dcm4chee.archive.qc.QCRetrieveBean;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class QCRetrieveBeanImpl.
 * Implementes QBRetrieveBean
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

public class QCRetrieveBeanImpl implements QCRetrieveBean{

    private static Logger LOG = LoggerFactory.getLogger(QCRetrieveBeanImpl.class);

    private static final String CACHED_UIDS = "qc_cached_uids";
    private static final String CACHED_HISTORY_OBJECTS="qc_cached_history";
    private static final String NUM_QC_REF_QUERIES="number_of_reference_queries_for_qc";
    
    @Inject
    QCBean qcManager;
    
    @PersistenceContext(name="dcm4chee-arc")
    EntityManager em;

    private String qcSource="Quality Control";

    @Override
    public boolean requiresReferenceUpdate(String studyInstanceUID, Patient pat) {
        
        if(studyInstanceUID != null) {
            Query query = em.createNamedQuery(QCInstanceHistory.STUDY_EXISTS_IN_QC_HISTORY_AS_OLD_OR_NEXT);
            query.setParameter(1, studyInstanceUID);
            query.setMaxResults(1);
            return query.getResultList().isEmpty()? false: true;
        }
        ArrayList<String> uids = new ArrayList<String>();
        
        if(pat == null) {
            LOG.error("{}:  QC info[requiresReferenceUpdate] Failure - "
                    + "Attributes supplied missing PatientID",qcSource);
            throw new IllegalArgumentException("Attributes supplied missing PatientID");
            }
        
        for(Study study : pat.getStudies()) {
            uids.add(study.getStudyInstanceUID());
            }
        Query query = em.createNamedQuery(QCInstanceHistory.STUDIES_EXISTS_IN_QC_HISTORY_AS_OLD_OR_NEXT);
        query.setParameter("uids", uids);
        query.setMaxResults(1);
        return query.getResultList().isEmpty()? false : true;
    }

    @Override
    public void scanForReferencedStudyUIDs(Attributes attrs, Collection<String> initialColl) {
        if(attrs.contains(Tag.StudyInstanceUID)) {
            initialColl.add(attrs.getString(Tag.StudyInstanceUID));
        }
        for(int i : attrs.tags()) {
            if(attrs.getVR(i) == VR.SQ) {
                for(Attributes item : attrs.getSequence(i))
                scanForReferencedStudyUIDs(item, initialColl);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<QCInstanceHistory> getReferencedHistory(CStoreSCUContext ctx,
            Collection<String> referencedStudyInstanceUIDs) {
        boolean performQuery=true;
        //check if cache has UIDs and filter the referencedStudyInstanceUIDs list to remove the cached ones
            Collection<String> diff = getUIDsMissingFromCache(referencedStudyInstanceUIDs,
                    (Collection<String>) ctx.getProperty(CACHED_UIDS));
            if(diff.isEmpty())
                performQuery=false;
            Collection<QCInstanceHistory> resultList = new ArrayList<QCInstanceHistory>();
        if(performQuery){
        Query query = em.createNamedQuery(QCInstanceHistory
                .FIND_DISTINCT_INSTANCES_WHERE_STUDY_OLD_OR_CURRENT_IN_LIST);
        query.setParameter("uids", diff);
        for(Iterator<Object[]> iter =  query.getResultList().iterator();iter.hasNext();) {
            Object[] row = iter.next();
            resultList.add((QCInstanceHistory) row[0]);
        }
        
        if(ctx.getProperty(NUM_QC_REF_QUERIES)==null) {
           ctx.setProperty(NUM_QC_REF_QUERIES,1);
        }
        else {
            ctx.setProperty(NUM_QC_REF_QUERIES, ((int)ctx.getProperty(NUM_QC_REF_QUERIES)+1));
        }
        }
        
        return complementCache(ctx,resultList,diff);
    }

    @SuppressWarnings("unchecked")
    private Collection<QCInstanceHistory> complementCache(CStoreSCUContext ctx,
            Collection<QCInstanceHistory> resultList, Collection<String> diff) {
        Collection<String> cachedUIDs 
        = (Collection<String>) ctx.getProperty(CACHED_UIDS);
        
        Collection<QCInstanceHistory> cachedHistory 
        = (Collection<QCInstanceHistory>) ctx.getProperty(CACHED_HISTORY_OBJECTS);
        
        if(diff.isEmpty())
            return cachedHistory;
        else {
            if(cachedUIDs==null)
                cachedUIDs = new ArrayList<String>();
            if(cachedHistory==null)
                cachedHistory = new ArrayList<QCInstanceHistory>();
            for(QCInstanceHistory qci : resultList) {
                if(!cachedHistory.contains(qci)) {
                    cachedHistory.add(qci);
                }
            }
            for(String uid : diff) {
                if(!cachedUIDs.contains(uid)) {
                    cachedUIDs.add(uid);
                }
            }
        }
        ctx.setProperty(CACHED_UIDS, cachedUIDs);
        ctx.setProperty(CACHED_HISTORY_OBJECTS, cachedHistory);
                return cachedHistory;
    }

    private Collection<String> getUIDsMissingFromCache(
            Collection<String> referencedStudyInstanceUIDs,
            Collection<String> cachedUIDS) {
        
        if(cachedUIDS==null)
            return referencedStudyInstanceUIDs;
        
        for(Iterator<String> iter = referencedStudyInstanceUIDs.iterator();iter.hasNext();) {
            String uid = iter.next();
            if(cachedUIDS.contains(uid)) {
                iter.remove();
            }
        }
        return referencedStudyInstanceUIDs;
    }
}
