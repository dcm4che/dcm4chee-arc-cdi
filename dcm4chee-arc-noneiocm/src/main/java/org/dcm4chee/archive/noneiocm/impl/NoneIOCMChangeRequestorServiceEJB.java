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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.QCActionHistory;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.noneiocm.NoneIOCMChangeRequestorService;
import org.dcm4chee.archive.qc.QCBean;
import org.dcm4chee.archive.qc.QCEvent;
import org.dcm4chee.archive.qc.QCEvent.QCOperation;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.session.StudyUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@Stateless
public class NoneIOCMChangeRequestorServiceEJB implements NoneIOCMChangeRequestorService {

    private Logger LOG = LoggerFactory.getLogger(NoneIOCMChangeRequestorServiceEJB.class);

    private final org.dcm4che3.data.Code REJ_CODE_QUALITY_REASON = new org.dcm4che3.data.Code("(113001, DCM, \"Rejected for Quality Reasons\")");

    @Inject
    private QCBean qcBean;

    @PersistenceContext(name="dcm4chee-arc")
    EntityManager em;

    public NoneIOCMChangeType getChangeType(Instance inst, StoreContext context) {
        Attributes attrs = context.getAttributes();
        if (!inst.getSopInstanceUID().equals(attrs.getString(Tag.SOPInstanceUID)))
            throw new IllegalArgumentException();
        boolean seriesChg = !inst.getSeries().getSeriesInstanceUID().equals(attrs.getString(Tag.SeriesInstanceUID));
        boolean studyChg = !inst.getSeries().getStudy().getStudyInstanceUID().equals(attrs.getString(Tag.StudyInstanceUID));
        Attributes patAttrs = inst.getSeries().getStudy().getPatient().getAttributes();
        IDWithIssuer currentPID = IDWithIssuer.pidOf(patAttrs);
        IDWithIssuer newPID = IDWithIssuer.pidOf(attrs);
        boolean patIDChg = !currentPID.equals(newPID);
        if (patIDChg) {
            if (studyChg || seriesChg) {
                LOG.warn("Illegal NoneICOM PatID change request! Study IUID and Series IUID must not be changed!");
                return NoneIOCMChangeType.ILLEGAL_CHANGE;
            }
            return NoneIOCMChangeType.PAT_ID_CHANGE;
        } else if (studyChg) {
            if (seriesChg) {
                LOG.warn("Illegal NoneICOM Study IUID change request! Series IUID must not be changed!");
                return NoneIOCMChangeType.ILLEGAL_CHANGE;
            }
            return NoneIOCMChangeType.STUDY_IUID_CHANGE;
        } else if (seriesChg) {
            return NoneIOCMChangeType.SERIES_IUID_CHANGE;
        }
        return NoneIOCMChangeType.INSTANCE_CHANGE;
    }
    public List<QCInstanceHistory> findInstanceHistory(String sopInstanceUID) {
        return new ArrayList<QCInstanceHistory>();
    }
    @Override
    public NoneIOCMChangeType performChange(Instance inst, StoreContext context) {
        NoneIOCMChangeType chgType = getChangeType(inst, context);
        LOG.info("######## performChange start for changeType:{}", chgType);
        switch (chgType) {
        case STUDY_IUID_CHANGE:
            split(inst, context);
            LOG.info("{}: Performed study UID change for non iocm request");
            break;
        case INSTANCE_CHANGE:

            break;
        default:

            break;
        }
        
        return chgType;
    }

    @Override
    public void handleModalityChange(Instance inst, StoreContext context, int gracePeriodInSeconds) {
        
        if(withinGracePeriod(inst, gracePeriodInSeconds)) {
            context.setOldNONEIOCMChangeUID(inst.getSopInstanceUID());
            Attributes attrs = context.getAttributes();
            attrs.setString(null, Tag.SOPInstanceUID, VR.UI, 
                    UIDUtils.createUID());
            inst.setAttributes(attrs, context.getStoreSession().getStoreParam().getAttributeFilter(Entity.Instance),
                    context.getStoreSession().getStoreParam().getFuzzyStr(), context.getStoreSession().getStoreParam().getNullValueForQueryFields());
            em.merge(inst);
            context.setAttributes(attrs);
            context.setInstance(inst);
            context.setOldNONEIOCMChangeUID(inst.getSopInstanceUID());
        }
        else {
            context.setStoreAction(StoreAction.IGNORE);
        }
    }

    @Override
    public void onStoreInstance(StoreContext context) {
        //check here if the stored instance was received by NoneIOCM
        //Source modality within grace period
        
        if(context.getOldNONEIOCMChangeUID() != null) {
            //create Split QC history for none IOCM                     
        }
    }
    private boolean withinGracePeriod(Instance inst, int gracePeriodInSeconds) {
        Query query = em.createNamedQuery(QCInstanceHistory
                .FIND_BY_CURRENT_UID_FOR_ACTION, QCInstanceHistory.class);
        query.setParameter(1, inst.getSopInstanceUID());
        query.setParameter(2, QCOperation.DELETE.toString());
        QCInstanceHistory foundQCInstanceHistory = (QCInstanceHistory) query.getSingleResult();
        
        if(foundQCInstanceHistory == null)
            return false;
        
        long createdTime = foundQCInstanceHistory.getSeries()
                .getStudy().getAction().getCreatedTime().getTime();
        long now = System.currentTimeMillis();
        
        return (now - createdTime) < gracePeriodInSeconds; 
    }
    private QCEvent split(Instance inst, StoreContext context) {
        LOG.info("######## Start Split instance to new Study");
        Attributes newAttrs = context.getAttributes();
        IDWithIssuer pid = new IDWithIssuer(newAttrs.getString(Tag.PatientID), new org.dcm4che3.data.Issuer(
                newAttrs.getString(Tag.IssuerOfPatientID), null, null));
        String newStudyIUID = newAttrs.getString(Tag.StudyInstanceUID);
        Attributes studyAttrs = new Attributes();
        studyAttrs.addAll(inst.getSeries().getStudy().getAttributes());
        studyAttrs.setString(Tag.StudyInstanceUID, VR.UI, newStudyIUID);
        Attributes seriesAttrs = new Attributes();
        seriesAttrs.addAll(inst.getSeries().getAttributes());
        String newSeriesIUID = getNewSeriesIuidFromHistory(seriesAttrs.getString(Tag.SeriesInstanceUID), newStudyIUID);
        LOG.info("Found new SeriesInstanceUID from history:{}", newSeriesIUID);
        if (newSeriesIUID == null) {
            seriesAttrs.remove(Tag.SeriesInstanceUID);
        } else {
            seriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI, newSeriesIUID);
        }
        return qcBean.split(Arrays.asList(inst.getSopInstanceUID()), pid, newStudyIUID, studyAttrs, seriesAttrs, REJ_CODE_QUALITY_REASON);
    }

    private String getNewSeriesIuidFromHistory(String oldSeriesIUID, String newStudyIUID) {
        Query query = em.createQuery("SELECT h FROM QCInstanceHistory h WHERE h.currentStudyUID = ?1 AND h.series.oldSeriesUID = ?2 ORDER BY h.pk DESC");
        query.setParameter(1, newStudyIUID);
        query.setParameter(2, oldSeriesIUID);
        @SuppressWarnings("unchecked")
        List<QCInstanceHistory> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0).getCurrentSeriesUID();
    }

    public void onStudyUpdated(@Observes StudyUpdatedEvent studyUpdatedEvent) {
        LOG.info("###### onStudyUpdated:{}", studyUpdatedEvent);
    }

}
