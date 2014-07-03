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

package org.dcm4chee.archive.mima.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Issuer;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class MIMAInfo {

    private boolean returnOtherPatientIDs;
    private boolean returnOtherPatientNames;
    private Issuer requestedIssuerOfPatientID;
    private Issuer requestedIssuerOfAccessionNumber;
    private final Hashtable<Set<IDWithIssuer>,String[]> patientNamesCache = new Hashtable<Set<IDWithIssuer>,String[]> ();
    private final Set<IDWithIssuer[]> pixResponseCache = new HashSet<IDWithIssuer[]>();

    public boolean isReturnOtherPatientIDs() {
        return returnOtherPatientIDs;
    }

    public void setReturnOtherPatientIDs(boolean returnOtherPatientIDs) {
        this.returnOtherPatientIDs = returnOtherPatientIDs;
    }

    public boolean isReturnOtherPatientNames() {
        return returnOtherPatientNames;
    }

    public void setReturnOtherPatientNames(boolean returnOtherPatientNames) {
        this.returnOtherPatientNames = returnOtherPatientNames;
    }

    public Issuer getRequestedIssuerOfPatientID() {
        return requestedIssuerOfPatientID;
    }

    public void setRequestedIssuerOfPatientID(Issuer requestedIssuerOfPatientID) {
        this.requestedIssuerOfPatientID = requestedIssuerOfPatientID;
    }

    public Issuer getRequestedIssuerOfAccessionNumber() {
        return requestedIssuerOfAccessionNumber;
    }

    public void setRequestedIssuerOfAccessionNumber(
            Issuer requestedIssuerOfAccessionNumber) {
        this.requestedIssuerOfAccessionNumber = requestedIssuerOfAccessionNumber;
    }

    public void cachePatientNames(Set<IDWithIssuer> ids, String[] names)
    {
        if (!patientNamesCache.containsKey(ids))
            patientNamesCache.put(ids, names);
    }
    
    public String[] getPatientNamesFromCache(Set<IDWithIssuer> ids) {
        return patientNamesCache.get(ids);
    }
    
    public void cachePixResponse (IDWithIssuer[] pixResponse) {
        pixResponseCache.add(pixResponse);
    }
    
    public IDWithIssuer[] getCachedPixResponse (IDWithIssuer pid)
    {
        for (IDWithIssuer[] cachedPixResponse : pixResponseCache) {
            if (Arrays.asList(cachedPixResponse).contains(pid))
                return cachedPixResponse;
        }
        
        return null;
    }
}
