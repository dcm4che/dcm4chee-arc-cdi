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
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.PersonName.Component;
import org.dcm4che3.data.Tag;
import org.dcm4chee.archive.entity.Patient;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class DemographicsPatientSelector implements PatientSelector {

    private boolean forceIssuer = false;
    private MatchDemographics demographics = MatchDemographics.NOID;
    private MatchType familyName = MatchType.IGNORE;
    private MatchType givenName = MatchType.IGNORE;
    private MatchType middleName = MatchType.IGNORE;
    private MatchType patientSex = MatchType.IGNORE;
    private MatchType patientBirthDate = MatchType.IGNORE;

    public DemographicsPatientSelector() {
        super();
    }

    @Override
    public Patient select(List<Patient> patients, Attributes attrs,
            Collection<IDWithIssuer> pids) throws NonUniquePatientException {

        if (patients.isEmpty())
            return null;

        EnumSet<MatchDemographics> matchDemographics =
                EnumSet.allOf(MatchDemographics.class);
        if (pids != null && !pids.isEmpty()) {
            matchDemographics.remove(MatchDemographics.NOID);
            if (IDPatientSelector.containsIssuer(pids)) {
                // incoming has at least one id with issuer
                List<Patient> patientsWithMatchingIssuer =
                        new ArrayList<Patient>(patients.size());
                for (Patient pat : patients) {
                    if (IDPatientSelector.containsIDWithMatchingIssuer(
                            pat.getPatientIDs(), pids))
                        patientsWithMatchingIssuer.add(pat);
                }
                if (!patientsWithMatchingIssuer.isEmpty()) {
                    // there are patients with matching id + issuer - ignore others
                    matchDemographics.remove(MatchDemographics.NOISSUER);
                    patients = patientsWithMatchingIssuer;
                }
            }
        }
        if (forceIssuer && matchDemographics.contains(MatchDemographics.NOISSUER))
            return null;

        if (matchDemographics.contains(demographics))
            filterByDemographics(patients, attrs);

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

    private void filterByDemographics(List<Patient> patients, Attributes attrs) {

        for (Iterator<Patient> iter = patients.iterator(); iter.hasNext();) {

            Patient candidate = iter.next();
            Attributes cAttrs = candidate.getAttributes();
            PersonName cName = new PersonName(cAttrs.getString(Tag.PatientName));
            PersonName iName = new PersonName(attrs.getString(Tag.PatientName));

            if (!familyName.match(cName, iName, Component.FamilyName)
                    || !givenName.match(cName, iName, Component.GivenName)
                    || !middleName.match(cName, iName, Component.MiddleName)
                    || !patientSex.match(cAttrs, attrs, Tag.PatientSex)
                    || !patientBirthDate.match(cAttrs, attrs, Tag.PatientBirthDate)) {
                iter.remove();
            }
        }
    }

    public void setForceIssuer(boolean forceIssuer) {
        this.forceIssuer = forceIssuer;
    }

    public void setDemographics(String demographics) {
        this.demographics = MatchDemographics.valueOf(demographics);
    }
    
    public void setFamilyName(String familyName) {
        this.familyName = MatchType.valueOf(familyName);
    }
    
    public void setGivenName(String givenName) {
        this.givenName = MatchType.valueOf(givenName);
    }

    public void setMiddleName(String middleName) {
        this.middleName = MatchType.valueOf(middleName);
    }
    
    public void setPatientSex(String patientSex) {
        this.patientSex = MatchType.valueOf(patientSex);
    }
    
    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = MatchType.valueOf(patientBirthDate);
    }

    public enum MatchDemographics {
        NOID,           // match demographics only if no ID is present
        NOISSUER,       // match demographics only if issuer of ID is unknown
        ALWAYS,         // match demographics always
    }

    public enum MatchType {
        IGNORE {        // do not use for matching
            @Override
            boolean match(String one, String two) {
                return true;
            }
            @Override
            boolean match(PersonName pn1, PersonName pn2, Component c) {
                return true;
            }
            @Override
            boolean match(Attributes attrs1, Attributes attrs2, int tag) {
                return true;
            }
        },
        STRICT {        // both must exist and be equal
            @Override
            boolean match(String one, String two) {
                return one != null && one.equalsIgnoreCase(two);
            }
        },
        BROAD {         // as STRICT but matches also with null
            @Override
            boolean match(String one, String two) {
                 return one == null || two == null || one.equalsIgnoreCase(two) ;
            }
        };
        abstract boolean match(String one, String two);

        boolean match(PersonName pn1, PersonName pn2, Component c) {
            return match(pn1.get(c), pn2.get(c));
        }

        boolean match(Attributes attrs1, Attributes attrs2, int tag) {
            return match(attrs1.getString(tag), attrs2.getString(tag));
        }
    }

}
