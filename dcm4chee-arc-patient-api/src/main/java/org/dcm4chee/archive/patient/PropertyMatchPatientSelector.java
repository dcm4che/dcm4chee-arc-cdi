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
import org.dcm4che3.data.PersonName;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.PersonName.Component;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
public class PropertyMatchPatientSelector implements PatientSelector {

    private boolean forceIssuer = false;
    private MatchDemographics demographics = MatchDemographics.NOID;
    private MatchType familyName = MatchType.IGNORE;
    private MatchType givenName = MatchType.IGNORE;
    private MatchType middleName = MatchType.IGNORE;
    private MatchType patientSex = MatchType.IGNORE;
    private MatchType patientBirthDate = MatchType.IGNORE;

    @Override
    public Patient select(List<Patient> patients, Attributes attrs,
            Collection<IDWithIssuer> pids) throws NonUniquePatientException,
            MatchTypeException, IssuerMissingException {

        if (pids == null || pids.size() == 0) {
            // incoming ids are emty. Force checking by Demographics.
            if (demographics.equals(MatchDemographics.NOID)
                    || demographics.equals(MatchDemographics.NOISSUER)
                    || demographics.equals(MatchDemographics.ALWAYS))
                filterByDemographics(patients, attrs);
        } else if (!containsIssuer(pids)) {
            // incoming have no id with issuer. Filter by id and then check
            // demographics, if configured.
            
            if (forceIssuer)
                throw new IssuerMissingException("Patient has no Issuer, configured as mandatory");
            
            filterByPatientID(patients, pids);
            if (demographics.equals(MatchDemographics.NOISSUER)
                    || demographics.equals(MatchDemographics.ALWAYS))
                filterByDemographics(patients, attrs);
        } else {
            // incoming has at least one id with issuer

            List<Patient> matchingpatients = new ArrayList<Patient>();

            // collect all patients with matching id + issuer
            // at the end in the patients list there are only patients
            // with matching ids and no issuer
            for (Iterator<Patient> iter = patients.iterator(); iter.hasNext();) {
                Patient candidate = iter.next();
                if (containsIDWithMatchingIssuer(candidate.getPatientIDs(),
                        pids)) {
                    matchingpatients.add(candidate);
                    iter.remove();
                } else if (!containsIDWithMatchingIDNoIssuer(
                        candidate.getPatientIDs(), pids)) {
                    iter.remove();
                }
            }
            
            if (matchingpatients.size() == 0 && forceIssuer)
                throw new IssuerMissingException("No Patient with right ID+Issuer found");

            // filter by demographics (if configured) 
            // remaining matching patients not having issuer
            if (patients.size() > 0
                    && (demographics.equals(MatchDemographics.NOISSUER) || demographics
                            .equals(MatchDemographics.ALWAYS)))
                filterByDemographics(patients, attrs);

            matchingpatients.addAll(patients);

            // match by demographis all matching patients, if configured
            if (matchingpatients.size() > 0
                    && demographics.equals(MatchDemographics.ALWAYS))
                filterByDemographics(matchingpatients, attrs);
            
            patients = matchingpatients;
        }

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

    private void filterByPatientID(List<Patient> patients,
            Collection<IDWithIssuer> pids) {

        for (Iterator<Patient> iter = patients.iterator(); iter.hasNext();) {

            Patient candidate = iter.next();
            Collection<PatientID> candidateIDs = candidate.getPatientIDs();

            boolean found = false;

            for (PatientID candidateID : candidateIDs)
                for (IDWithIssuer pid : pids)
                    if (candidateID.getID() != null && pid.getID() != null
                            && candidateID.getID().equals(pid.getID()))
                        found = true;

            if (!found)
                iter.remove();
        }
    }

    private void filterByDemographics(List<Patient> patients, Attributes attrs)
            throws MatchTypeException {

        for (Iterator<Patient> iter = patients.iterator(); iter.hasNext();) {

            boolean matches = true;

            Patient candidate = iter.next();

            PersonName cName = new PersonName(candidate.getAttributes()
                    .getString(Tag.PatientName));

            PersonName iName = new PersonName(attrs.getString(Tag.PatientName));

            matches = matches
                    && matchAttr(cName.get(Component.FamilyName),
                            iName.get(Component.FamilyName), familyName);

            if (matches)
                matches = matches
                        && matchAttr(cName.get(Component.GivenName),
                                iName.get(Component.GivenName), givenName);

            if (matches)
                matches = matches
                        && matchAttr(cName.get(Component.MiddleName),
                                iName.get(Component.MiddleName), middleName);

            if (matches)
                matches = matches
                        && matchAttr(
                                candidate.getAttributes().getString(
                                        Tag.PatientSex),
                                attrs.getString(Tag.PatientSex), patientSex);

            if (matches)
                matches = matches
                        && matchAttr(
                                candidate.getAttributes().getString(
                                        Tag.PatientBirthDate),
                                attrs.getString(Tag.PatientBirthDate),
                                patientBirthDate);

            if (!matches)
                iter.remove();
        }
    }

    private boolean matchAttr(String one, String two, MatchType type)
            throws MatchTypeException {
        if (type.equals(MatchType.IGNORE))
            return true;
        else if (one == null && two == null)
            return false;
        else if (one == null && two != null)
            return type.equals(MatchType.BROAD);
        else if (one != null && two == null)
            return type.equals(MatchType.BROAD);
        else if (type.equals(MatchType.STRICT) || type.equals(MatchType.BROAD))
            return one.trim().equalsIgnoreCase(two.trim());
        else
            throw new MatchTypeException(
                    "match type must be one of [IGNORE, BROAD, STRICT]");
    }

    private boolean containsIssuer(Collection<IDWithIssuer> pids) {

        for (IDWithIssuer pid : pids)
            if (pid.getIssuer() != null)
                return true;

        return false;
    }

    private boolean containsIDWithMatchingIDNoIssuer(
            Collection<PatientID> patientIDs, Collection<IDWithIssuer> pids) {
        for (PatientID patientID : patientIDs) {

            for (IDWithIssuer pid : pids) {
                if (patientID.getID().equals(pid.getID())
                        && patientID.getIssuer() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsIDWithMatchingIssuer(
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

    public void setForceIssuer(boolean forceIssuer) {
        this.forceIssuer = forceIssuer;
    }

    public void setDemographics(String demographics) {
        this.demographics = MatchDemographics.valueOf(demographics);
    }

    public void setDemographics(MatchDemographics demographics) {
        this.demographics = demographics;
    }
    
    public void setFamilyName(String familyName) {
        this.familyName = MatchType.valueOf(familyName);
    }

    public void setFamilyName(MatchType familyName) {
        this.familyName = familyName;
    }
    
    public void setGivenName(String givenName) {
        this.givenName = MatchType.valueOf(givenName);
    }

    public void setGivenName(MatchType givenName) {
        this.givenName = givenName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = MatchType.valueOf(middleName);
    }
    
    public void setMiddleName(MatchType middleName) {
        this.middleName = middleName;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = MatchType.valueOf(patientSex);
    }
    
    public void setPatientSex(MatchType patientSex) {
        this.patientSex = patientSex;
    }

    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = MatchType.valueOf(patientBirthDate);
    }

    public void setPatientBirthDate(MatchType patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }
}
