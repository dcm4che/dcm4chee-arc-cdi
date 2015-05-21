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

package org.dcm4chee.archive.qc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.dcm4chee.archive.dto.QCEventInstance;
import org.dcm4chee.archive.entity.Instance;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * 
 */

public class PerformedChangeRequest {

    private static ArrayList<Collection<QCEventInstance>> updatedInstanceUIDs = new ArrayList<Collection<QCEventInstance>>();
    private static ArrayList<Instance> rejNotes = new ArrayList<Instance>();
    private static ArrayList<String[]> notifiedAets = new ArrayList<String[]>();
    
    private static int lastChecked = -1;
   
    public static void addChangeRequest(Collection<QCEventInstance> updatedInstances, Instance rejNote, String[] aets) {
        updatedInstanceUIDs.add(updatedInstances);
        rejNotes.add(rejNote);
        notifiedAets.add(aets);
    }

    public static void reset() {
        updatedInstanceUIDs.clear();
        rejNotes.clear();
        notifiedAets.clear();
    }
    
    public static int count() {
        return updatedInstanceUIDs.size();
    }
    
    public static void checkChangeRequest(int idx, Collection<QCEventInstance> updatedInstances, Instance rejNote, String[] aets) {
        if (idx < 0) {
            idx = rejNotes.size()-1;
            assertTrue("No new changerequest available", idx > lastChecked);
            lastChecked = idx;
        }
        assertEquals("updatedInstanceUIDs", updatedInstances, updatedInstanceUIDs.get(idx));
        assertEquals("RejectionNote", rejNote, rejNotes.get(idx));
        assertTrue("notifiedAets", Arrays.equals(aets, notifiedAets.get(idx)));
    }
    
    public static void checkNoNewChangeRequest() {
        assertTrue("New changerequest found", (rejNotes.size()-1) == lastChecked);
    }
    
    public static void checkChangeRequests(int idx, Collection<QCEventInstance> expectedUpdatedInstances, Collection<Instance> expectedRejNotes, String[] aets) {
        if (idx < 0) {
            idx = rejNotes.size()-1;
            assertTrue("No new changerequest available", idx > lastChecked);
            lastChecked = idx;
        }
        ArrayList<Instance> chkRejNotes = new ArrayList<Instance>(expectedRejNotes);
        ArrayList<QCEventInstance> chkUpdatedInstances = new ArrayList<QCEventInstance>(expectedUpdatedInstances);
        for (int i = 0, len = expectedRejNotes.size(); i < len ; i++) {
            assertTrue("RejectionNote is not expected! "+rejNotes.get(idx), chkRejNotes.remove(rejNotes.get(idx)));
            assertTrue("Updated instances not expected! "+updatedInstanceUIDs.get(idx), chkUpdatedInstances.removeAll(updatedInstanceUIDs.get(idx)));
            assertTrue("notifiedAets", Arrays.equals(aets, notifiedAets.get(idx)));
            idx--;
        }
        assertTrue("UpdatedInstanceUIDs not part of change requests", chkUpdatedInstances.isEmpty());
        assertTrue("RejectionNote not part of change requests", chkRejNotes.isEmpty());
    }

}
