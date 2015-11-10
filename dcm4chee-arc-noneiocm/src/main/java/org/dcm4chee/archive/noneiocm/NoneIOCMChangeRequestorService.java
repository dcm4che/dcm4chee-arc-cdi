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

package org.dcm4chee.archive.noneiocm;

import java.util.List;

import javax.enterprise.event.Observes;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.entity.ActiveProcessing;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.session.StudyUpdatedEvent;


/**
 * @author Franz Willer <franz.willer@gmail.com>
 *
 */
public interface NoneIOCMChangeRequestorService {

    public enum NoneIOCMChangeType {
        PAT_ID_CHANGE, STUDY_IUID_CHANGE, SERIES_IUID_CHANGE, INSTANCE_CHANGE, NO_CHANGE, ILLEGAL_CHANGE
    }
    
    public final org.dcm4che3.data.Code REJ_CODE_QUALITY_REASON = new org.dcm4che3.data.Code("(113001, DCM, \"Rejected for Quality Reasons\")");

    boolean isNoneIOCMChangeRequestor(String callingAET);
    
    boolean isNoneIOCMChangeRequest(String callingAET, String sourceAET);
    
    int getNoneIOCMModalityGracePeriod(String callingAET);
    
    NoneIOCMChangeType getChangeType(Instance inst, Attributes attrs);
    
    NoneIOCMChangeType performChange(Instance inst, StoreContext context);

    List<QCInstanceHistory> findInstanceHistory(String sopInstanceUID);

    void onStudyUpdated(@Observes StudyUpdatedEvent studyUpdatedEvent);

    public void onStoreInstance(@Observes StoreContext context);

    void handleModalityChange(Instance inst, StoreContext context, int gracePeriodInSeconds);
    
    void hideOrUnhideInstance(Instance instance, org.dcm4che3.data.Code rejNoteCode);
    
	QCInstanceHistory getLastQCInstanceHistory(String sopIUID);
	
	void processNonIOCMRequest(String processStudyIUID,  List<ActiveProcessing> nonIocmProcessings);

}
