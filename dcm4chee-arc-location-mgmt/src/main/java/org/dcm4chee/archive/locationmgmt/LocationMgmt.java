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

package org.dcm4chee.archive.locationmgmt;

import java.util.Collection;
import java.util.List;

import javax.jms.JMSException;
import javax.persistence.NoResultException;

import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Study;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public interface LocationMgmt {

    boolean doDelete(Location ref);

    int doDelete(Collection<Long> refPks, boolean checkStudyMarked);

    void scheduleDelete(Collection<Location> refs, int delay,
            boolean checkStudyMarked) throws JMSException;

    void scheduleDeleteByPks(Collection<Long> refPks, int delay, boolean checkStudyMarked)
            throws JMSException;

    void failDelete(Location ref);

    Location getLocation(Long pk);

    void findOrCreateStudyOnStorageGroup(Study study, String groupID);

    void findOrCreateStudyOnStorageGroup(String studyUID,
            String groupID);

    List<Instance> findInstancesDueDelete(int studyRetention,
            String studyRetentionUnit, String groupID, String studyInstanceUID,
            String seriesInstanceUID);

    long calculateDataVolumePerDayInBytes(String groupID, 
            int dvdAverageOnNDays);

    boolean isMarkedForDelete(String studyInstanceUID, String groupID);

    List<Location> findFailedToDeleteLocations(String groupID);

    void markForDeletion(String studyInstanceUID, String groupID)
            throws NoResultException;

    Collection<Long> filterForMarkedForDeletionStudiesOnGroup(Collection<Long> refPKs);

	void purgeStudiesRejectedOrDeletedOnAllGroups();

}
