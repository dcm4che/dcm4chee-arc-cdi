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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4chee.archive.store.test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.soundex.ESoundex;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
import org.dcm4chee.archive.patient.PatientService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public class BaseStoreIT {

    @Inject
    PatientService patientService;
    @PersistenceContext
    EntityManager em;

    @Resource
    UserTransaction utx;


    protected void clearTestData(String[] pids, String[] issuers, Code[] codes ) throws NonUniquePatientException,
            PatientCircularMergedException, SecurityException,
            IllegalStateException, RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SystemException {
        Attributes tmpAttrs = new Attributes();
        for (String pid : pids) {
            tmpAttrs.setString(Tag.PatientID, VR.LO, pid);
            tmpAttrs.setString(Tag.IssuerOfPatientID, VR.LO,
                    "DCM4CHEE_TESTDATA");
            Patient tmpPatient = patientService.updateOrCreatePatientOnCStore(
                    tmpAttrs, new IDPatientSelector(), createStoreParam());
            deletePatient(tmpPatient);
            tmpAttrs = new Attributes();
        }
        deleteIssuers(issuers);
        deleteCodes(codes);
    }

    private void deleteCodes(Code[] codes) {
        for(Code c:codes) {
            long codePK = getcodePK(c);
            if(codePK!=-1){
            em.remove(em.find(Code.class, codePK));
            em.flush();
            }
        }
    }

    private long getcodePK(Code c) {
        Query q = em.createNamedQuery("Code.findByCodeValueWithoutSchemeVersion");
        q.setParameter(1, c.getCodeValue());
        q.setParameter(2, c.getCodingSchemeDesignator());
        List<Code> result = q.getResultList();
        if(result.isEmpty())
            return -1;
        else
        return result.get(0).getPk();
    }

    private void deletePatient(Patient p) throws NonUniquePatientException,
            SecurityException, IllegalStateException, RollbackException,
            HeuristicMixedException, HeuristicRollbackException,
            SystemException {
        em.remove(p);
        em.flush();
    }

    private void deleteIssuers(String[] issuers) {
        
        for (String issuer : issuers) {
            long issuerPK = getIssuerPK(issuer);
            if(issuerPK!=-1)
                em.remove(em.find(Issuer.class, issuerPK));
        }
        em.flush();
    }

    private long getIssuerPK(String issuer) {
        Query q = em.createNamedQuery("Issuer.findByEntityID");
        q.setParameter(1, issuer);
        List<Issuer> result = q.getResultList();
        if(result.isEmpty())
            return -1;
        else
            return result.get(0).getPk();
    }

    private static StoreParam createStoreParam() {
        StoreParam storeParam = new StoreParam();
        storeParam.setAttributeFilters(ATTRIBUTE_FILTERS);
        storeParam.setFuzzyStr(new ESoundex());
        return storeParam;
    }

    private static final int[] PATIENT_ATTRS = { Tag.SpecificCharacterSet,
            Tag.PatientName, Tag.PatientID, Tag.IssuerOfPatientID,
            Tag.IssuerOfPatientIDQualifiersSequence, Tag.PatientBirthDate,
            Tag.PatientBirthTime, Tag.PatientSex,
            Tag.PatientInsurancePlanCodeSequence,
            Tag.PatientPrimaryLanguageCodeSequence, Tag.OtherPatientNames,
            Tag.OtherPatientIDsSequence, Tag.PatientBirthName, Tag.PatientAge,
            Tag.PatientSize, Tag.PatientSizeCodeSequence, Tag.PatientWeight,
            Tag.PatientAddress, Tag.PatientMotherBirthName, Tag.MilitaryRank,
            Tag.BranchOfService, Tag.MedicalRecordLocator, Tag.MedicalAlerts,
            Tag.Allergies, Tag.CountryOfResidence, Tag.RegionOfResidence,
            Tag.PatientTelephoneNumbers, Tag.EthnicGroup, Tag.Occupation,
            Tag.SmokingStatus, Tag.AdditionalPatientHistory,
            Tag.PregnancyStatus, Tag.LastMenstrualDate,
            Tag.PatientReligiousPreference, Tag.PatientSpeciesDescription,
            Tag.PatientSpeciesCodeSequence, Tag.PatientSexNeutered,
            Tag.PatientBreedDescription, Tag.PatientBreedCodeSequence,
            Tag.BreedRegistrationSequence, Tag.ResponsiblePerson,
            Tag.ResponsiblePersonRole, Tag.ResponsibleOrganization,
            Tag.PatientComments, Tag.ClinicalTrialSponsorName,
            Tag.ClinicalTrialProtocolID, Tag.ClinicalTrialProtocolName,
            Tag.ClinicalTrialSiteID, Tag.ClinicalTrialSiteName,
            Tag.ClinicalTrialSubjectID, Tag.ClinicalTrialSubjectReadingID,
            Tag.PatientIdentityRemoved, Tag.DeidentificationMethod,
            Tag.DeidentificationMethodCodeSequence,
            Tag.ClinicalTrialProtocolEthicsCommitteeName,
            Tag.ClinicalTrialProtocolEthicsCommitteeApprovalNumber,
            Tag.SpecialNeeds, Tag.PertinentDocumentsSequence, Tag.PatientState,
            Tag.PatientClinicalTrialParticipationSequence,
            Tag.ConfidentialityConstraintOnPatientDataDescription };

    public static final Map<Entity, AttributeFilter> ATTRIBUTE_FILTERS;

    static {
        ATTRIBUTE_FILTERS = new HashMap<>();
        ATTRIBUTE_FILTERS.put(Entity.Patient, new AttributeFilter(PATIENT_ATTRS));
    }
}
