//
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
 * Portions created by the Initial Developer are Copyright (C) 2015
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

package org.dcm4chee.archive.api.impl;

import java.util.ArrayList;
import java.util.Iterator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.dcm4chee.archive.api.StudyAETs;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@EJB(name = StudyAETs.JNDI_NAME, beanInterface = StudyAETs.class)
@Stateless
public class StudyAETsImpl implements StudyAETs {
 
    private static final Logger LOG = LoggerFactory.getLogger(StudyAETsImpl.class);

    @PersistenceContext(name = "dcm4chee-arc", unitName="dcm4chee-arc")
    EntityManager em;

    @Override
    public String[] getCalledAETsForStudy(String studyInstanceUID) {
        try {
            Study study = em
                    .createNamedQuery(Study.FIND_BY_STUDY_INSTANCE_UID_EAGER,
                            Study.class)
                    .setParameter(1, studyInstanceUID)
                    .getSingleResult();
            ArrayList<String> calledAETs = new ArrayList<String>();
            for(Iterator<Series> iter = study.getSeries().iterator(); iter.hasNext();)
                for(String calledAET : iter.next().getCalledAETs())
                    if(!calledAETs.contains(calledAET))
                        calledAETs.add(calledAET);
            return calledAETs.toArray(new String[]{});
        } catch (NoResultException e) {
            LOG.error("Unable to find study {} used to request "
                    + "CalledAETs - reason {}", studyInstanceUID, e);
            return null;
        }
    }

    @Override
    public String[] getCalledAEtsForSeries(String seriesInstanceUID) {
        try {
            Series series = em
                    .createNamedQuery(Series.FIND_BY_SERIES_INSTANCE_UID_EAGER,
                            Series.class)
                    .setParameter(1, seriesInstanceUID)
                    .getSingleResult();
            return series.getCalledAETs();
        } catch (NoResultException e) {
            LOG.error("Unable to find series {} used to request "
                    + "CalledAETs - reason {}", seriesInstanceUID, e);
            return null;
        }
    }

    @Override
    public String[] getRetrievesAEtForInstance(String sopInstanceUID) {
        Instance inst;
        try {
            inst = em.createNamedQuery(
                    Instance.FIND_BY_SOP_INSTANCE_UID_EAGER, Instance.class)
                    .setParameter(1, sopInstanceUID).getSingleResult();
            return inst.getAllRetrieveAETs();
        } catch (NoResultException e) {
            LOG.error("Unable to find instance {} used to request "
                    + "RetrieveAETs - reason {}", sopInstanceUID, e);
            return null;
        }
    }

    @Override
    public String[] getSourceAETsForStudy(String studyInstanceUID) {
        try {
            Study study = em
                    .createNamedQuery(Study.FIND_BY_STUDY_INSTANCE_UID_EAGER,
                            Study.class)
                    .setParameter(1, studyInstanceUID)
                    .getSingleResult();
            ArrayList<String> sourceAETs = new ArrayList<>();
            for(Series series : study.getSeries())
                    if(!sourceAETs.contains(series.getSourceAET()))
                        sourceAETs.add(series.getSourceAET());
            return sourceAETs.toArray(new String[]{});
        } catch (NoResultException e) {
            LOG.error("Unable to find study {} used to request "
                    + "SourceAETs - reason {}", studyInstanceUID, e);
            return null;
        }
    }

}
