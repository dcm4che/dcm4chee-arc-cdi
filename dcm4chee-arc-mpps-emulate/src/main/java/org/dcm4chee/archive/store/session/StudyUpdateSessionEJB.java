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

package org.dcm4chee.archive.store.session;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.MPPSEmulationAndStudyUpdateRule;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.entity.StudyUpdateSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Umberto Cappellini
 * @author Roman K
 */

@Stateless
public class StudyUpdateSessionEJB {
    private static final Logger LOG = LoggerFactory.getLogger(StudyUpdateSessionEJB.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    Device device;

    /**
     * Adds/updates existing StudyUpdateSession entity with information about currently stored study
     */
    @SuppressWarnings("unchecked")
    public void addStoredInstance(String sourceAET,
                                  String localAET,
                                  String studyInstanceUID,
                                  String sopInstanceUID,
                                  StoreAction storeAction,
                                  MPPSEmulationAndStudyUpdateRule rule) {

        Date emulationTime = new Date(System.currentTimeMillis() + rule.getEmulationDelay() * 1000L);

        try {
            // try to find an existing study update session
            StudyUpdateSession entity = em
                    .createNamedQuery(
                            StudyUpdateSession.FIND_BY_STUDY_INSTANCE_UID_AND_SOURCE_AET,
                            StudyUpdateSession.class)
                    .setParameter(1, studyInstanceUID)
                    .setParameter(2, sourceAET)
                    .getSingleResult();

            // lock the row to ensure 'storedInstances' stays consistent
            // we are neither on critical path nor blocking other c-stores
            em.lock(entity, LockModeType.PESSIMISTIC_WRITE);

            // bump the timeout
            entity.setEmulationTime(emulationTime);

            // add stored instance to the list
            entity.getStoredInstances().add(new StudyUpdatedEvent.StoredInstance(sopInstanceUID, storeAction));

            em.merge(entity);
            LOG.debug("Modified study update session for Study[iuid={}] received from {}", studyInstanceUID, sourceAET);

        } catch (NoResultException nre) {

            // create new study update session
            StudyUpdateSession entity = new StudyUpdateSession();
            entity.setSourceAET(sourceAET);
            entity.setLocalAET(localAET);
            entity.setStudyInstanceUID(studyInstanceUID);
            entity.setEmulationTime(emulationTime);
            entity.getStoredInstances().add(new StudyUpdatedEvent.StoredInstance(sopInstanceUID, storeAction));
            em.persist(entity);

            LOG.info("Created study update session for Study[iuid={}] received from {}", studyInstanceUID, sourceAET);
        }
    }

    public StudyUpdateSession getNextFinishedStoreStudySession() {
        List<StudyUpdateSession> resultList = em
                .createNamedQuery(StudyUpdateSession.FIND_READY_TO_FINISH, StudyUpdateSession.class)
                .setMaxResults(1)
                .getResultList();

        if (resultList.isEmpty()) return null;

        StudyUpdateSession studyUpdateSession = resultList.get(0);
        em.remove(studyUpdateSession);
        return studyUpdateSession;
    }

}
