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

package org.dcm4chee.archive.patient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class IDPatientSelector implements PatientSelector {

    @Override
    public Patient select(List<Patient> candidates,
            Attributes attrs, Collection<IDWithIssuer> pids)
            throws NonUniquePatientException {

        if (candidates.isEmpty())
            return null;

        if (pids.isEmpty())
            throw new NonUniquePatientException("No Patient ID");

        if (candidates.size() == 1)
            return candidates.get(0);

        if (containsIssuer(pids)) {
            ArrayList<Patient> matchingIssuer =
                    new ArrayList<Patient>(candidates.size());
            for (Patient patient : candidates)
                if (containsIDWithMatchingIssuer(patient.getPatientIDs(), pids))
                    matchingIssuer.add(patient);
    
            if (matchingIssuer.size() == 1)
                return matchingIssuer.get(0);
        }
 
        throw new NonUniquePatientException(
                candidates.size() + " matching patients");
    }

    static boolean containsIssuer(Collection<IDWithIssuer> pids) {

        for (IDWithIssuer pid : pids)
            if (pid.getIssuer() != null)
                return true;

        return false;
    }

    static boolean containsIDWithMatchingIssuer(
            Collection<PatientID> patientIDs, Collection<IDWithIssuer> pids) {
        for (PatientID patientID : patientIDs) {
            String id = patientID.getID();
            Issuer issuer = patientID.getIssuer();
            if (issuer == null)
                continue;

            for (IDWithIssuer pid : pids) {
                if (id.equals(pid.getID())) {
                    org.dcm4che3.data.Issuer issuer2 = pid.getIssuer();
                    if (issuer2 != null && issuer2.matches(issuer))
                        return true;
                }
            }
        }
        return false;
    }

}
