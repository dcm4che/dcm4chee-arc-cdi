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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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
package org.dcm4chee.archive.mpps;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
@Stateless
public class MPPSServiceEJB {
    private static final Logger LOG = LoggerFactory.getLogger(MPPSServiceEJB.class);
    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    public void persistPPS(MPPS mpps) {
        em.persist(mpps);
    }

    public List<Instance> findBySeriesInstanceUID(Attributes seriesRef) {
       return em
        .createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID,
                Instance.class)
        .setParameter(1, seriesRef.getString(Tag.SeriesInstanceUID))
        .getResultList();
    }

    public MPPS findPPS(String sopInstanceUID) {
        MPPS pps = null;
        try {
            pps = em.createNamedQuery(
            MPPS.FIND_BY_SOP_INSTANCE_UID, MPPS.class)
                    .setParameter(1, sopInstanceUID).getSingleResult();
        } catch (Exception e) {
        }
        return pps;
    }

    public Study findStudyByUID(String studyUID) {
        String queryStr = "SELECT s FROM Study s JOIN FETCH s.series se WHERE s.studyInstanceUID = ?1";
            Query query = em.createQuery(queryStr);
            Study study = null;
            try {
                query.setParameter(1, studyUID);
             study = (Study) query.getSingleResult();
            }
            catch(NoResultException e) {
                LOG.error(
                        "Unable to find study {}, related to"
                        + " an already performed procedure",studyUID);
            }
            return study;
    }
}
