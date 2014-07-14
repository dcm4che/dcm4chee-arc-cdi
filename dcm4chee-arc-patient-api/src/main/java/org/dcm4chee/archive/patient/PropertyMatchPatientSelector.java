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

package org.dcm4chee.archive.patient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
public class PropertyMatchPatientSelector implements PatientSelector {

    private MatchType issuer = MatchType.IGNORE;
    private MatchType family = MatchType.IGNORE;
    private MatchType given = MatchType.IGNORE;
    private MatchType middle = MatchType.IGNORE;
    private MatchType sex = MatchType.IGNORE;
    private MatchType birth = MatchType.IGNORE;

    @Override
    public Patient select(List<Patient> patients, Attributes attrs,
            Collection<IDWithIssuer> pids) throws NonUniquePatientException {

        // check issuer of id(s)
        // check other ids over the one used to match (transforms the OR in the
        // match to and AND)
        // criteria to apply demographic match (i.e. no demog if id+issuer
        // matches)
        // add replace with regular expression to all strings (use
        // String.replace(reg,""))
        // add match to only the first N characters for all strings
        filterByIssuer(patients, pids);
        
        
        switch (patients.size()) {
        case 0:
            return null;
        case 1:
            return patients.get(0);
        default:
            throw new NonUniquePatientException(patients.size()
                    + " matching patients");
        }
    }

    private void filterByIssuer(List<Patient> patients,
            Collection<IDWithIssuer> pids) {

        for (Iterator<Patient> iter = patients.iterator(); iter.hasNext();) {

            Patient candidate = iter.next();
            Collection<PatientID> candidateIDs = candidate.getPatientIDs();

            if ((candidateIDs == null || candidateIDs.size() == 0)) {

                // case one: both empty, do not remove, match by demographics

                // case two: candidate ids is empty, pids not empty, remove
                // candidate
                if (pids != null && pids.size() > 0)
                    iter.remove();

            } else {

                // case three: candidate ids not empty, pids empty, remove
                // candidate
                if (pids == null || pids.size() == 0) {
                    iter.remove();
                } else { // case four both ids not empty

                    boolean found = false;

                    for (PatientID candidateID : candidateIDs)
                        for (IDWithIssuer pid : pids) {

                            if (candidateID.getID() != null
                                    && pid.getID() != null
                                    && candidateID.getID().equals(pid.getID())) {

                                if (candidateID.getIssuer() == null)
                                    if (pid.getIssuer() != null
                                            && getIssuer().equals(
                                                    MatchType.BROAD))
                                        found = true;
                                    else if (pid.getIssuer() == null
                                            && getIssuer().equals(
                                                    MatchType.BROAD))
                                        found = true;
                                    else // both not null
                                    if (candidateID.getIssuer().equals(
                                            pid.getIssuer()))
                                        found = true;

                            }
                        }
                    
                    if (!found)
                        iter.remove();
                }
            }
        }

    }

    public MatchType getIssuer() {
        return issuer;
    }

    public void setIssuer(MatchType issuer) {
        this.issuer = issuer;
    }

    public MatchType getFamily() {
        return family;
    }

    public void setFamily(MatchType family) {
        this.family = family;
    }

    public MatchType getGiven() {
        return given;
    }

    public void setGiven(MatchType given) {
        this.given = given;
    }

    public MatchType getMiddle() {
        return middle;
    }

    public void setMiddle(MatchType middle) {
        this.middle = middle;
    }

    public MatchType getSex() {
        return sex;
    }

    public void setSex(MatchType sex) {
        this.sex = sex;
    }

    public MatchType getBirth() {
        return birth;
    }

    public void setBirth(MatchType birth) {
        this.birth = birth;
    }
}
