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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.Tag;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.query.QueryContext;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
class QueryInfo {

    private final boolean returnOtherPatientIDs;
    private final boolean returnOtherPatientNames;
    private final Issuer requestedIssuerOfPatientID;
    private final Issuer requestedIssuerOfAccessionNumber;
    private final ArrayList<PatientIDsWithPatientNames> cachedPidsWithNames =
            new ArrayList<PatientIDsWithPatientNames>(1);

    public QueryInfo(QueryContext queryContext) {
        ArchiveAEExtension arcAE = queryContext.getArchiveAEExtension();
        Attributes keys = queryContext.getKeys();
        QueryParam queryParam = queryContext.getQueryParam();
        returnOtherPatientIDs = arcAE.isReturnOtherPatientIDs()
                && keys.contains(Tag.OtherPatientIDsSequence);
        returnOtherPatientNames = arcAE.isReturnOtherPatientNames()
                && keys.contains(Tag.OtherPatientNames);
        requestedIssuerOfPatientID = keys.contains(Tag.PatientID)
                ? keys.contains(Tag.IssuerOfPatientID)
                    ? Issuer.fromIssuerOfPatientID(keys)
                    : queryParam.getDefaultIssuerOfPatientID()
                : null;
        requestedIssuerOfAccessionNumber = (keys.contains(Tag.AccessionNumber)
                || keys.contains(Tag.RequestAttributesSequence))
                    ?  keys.contains(Tag.IssuerOfAccessionNumberSequence)
                        ? Issuer.valueOf(keys.getNestedDataset(Tag.IssuerOfAccessionNumberSequence))
                        : queryParam.getDefaultIssuerOfAccessionNumber()
                    : null;
        IDWithIssuer[] pids = queryContext.getPatientIDs();
        if (pixQueryAlreadyPerformed(pids)) {
            cachedPidsWithNames.add(new PatientIDsWithPatientNames(pids));
        }
    }

    private boolean pixQueryAlreadyPerformed(IDWithIssuer[] pids) {
        switch (pids.length) {
        case 0:
            return false;
        case 1:
            return !(containsWildcard(pids[0].getID()) || pids[0].getIssuer() == null);
        default:
            return true;
        }
    }

    private boolean containsWildcard(String s) {
        return s.indexOf('*') >= 0 || s.indexOf('?') >= 0;
    }

    public boolean isReturnOtherPatientIDs() {
        return returnOtherPatientIDs;
    }

    public boolean isReturnOtherPatientNames() {
        return returnOtherPatientNames;
    }

    public Issuer getRequestedIssuerOfPatientID() {
        return requestedIssuerOfPatientID;
    }

    public Issuer getRequestedIssuerOfAccessionNumber() {
        return requestedIssuerOfAccessionNumber;
    }

    public PatientIDsWithPatientNames getPatientIDsWithPatientNames(
            IDWithIssuer pid) {
        for (PatientIDsWithPatientNames entry : cachedPidsWithNames) {
            IDWithIssuer pid2 = entry.getPatientIDByIssuer(pid.getIssuer());
            if (pid2 != null && pid2.getID().equals(pid.getID()))
                return entry;
        }
        return null;
    }

    public void addPatientIDsWithPatientNames(
            PatientIDsWithPatientNames pidsWithNames) {
        cachedPidsWithNames.add(pidsWithNames);
    }
 
}
