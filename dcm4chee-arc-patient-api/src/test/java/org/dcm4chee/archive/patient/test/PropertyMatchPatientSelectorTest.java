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

package org.dcm4chee.archive.patient.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.soundex.ESoundex;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.IssuerMissingException;
import org.dcm4chee.archive.patient.MatchDemographics;
import org.dcm4chee.archive.patient.MatchType;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
import org.dcm4chee.archive.patient.PatientSelector;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.patient.PropertyMatchPatientSelector;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
@RunWith(Arquillian.class)
public class PropertyMatchPatientSelectorTest {

    @Inject
    private PatientService service;

    @PersistenceContext
    EntityManager em;

    @Resource
    UserTransaction utx;

    /**
     * Creates the deployment.
     * 
     * @return the web archive
     */
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
        war.addClass(PropertyMatchPatientSelectorTest.class);
        JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies().resolve()
                .withTransitivity().as(JavaArchive.class);
        war.addAsLibraries(archs);

        return war;
    }

    /**
     * Setup.
     * 
     * @throws NotSupportedException
     *             the not supported exception
     * @throws SystemException
     *             the system exception
     */
    @Before
    public void setup() throws NotSupportedException, SystemException {
        utx.begin();
        em.joinTransaction();
    }

    /**
     * Finalize test.
     * 
     * @throws SecurityException
     *             the security exception
     * @throws IllegalStateException
     *             the illegal state exception
     * @throws RollbackException
     *             the rollback exception
     * @throws HeuristicMixedException
     *             the heuristic mixed exception
     * @throws HeuristicRollbackException
     *             the heuristic rollback exception
     * @throws SystemException
     *             the system exception
     */
    @After
    public void finalizeTest() throws SecurityException, IllegalStateException,
            RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SystemException {
        utx.commit();
        em.clear();
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testTwoPatientsWithoutIssuerMatchDemographics()
            throws Exception {

        PatientSelector strict = getSelector(false, MatchDemographics.ALWAYS,
                MatchType.STRICT, MatchType.STRICT, MatchType.IGNORE,
                MatchType.IGNORE);
        
        // broad = apply demog if issuer is missing
        PatientSelector broad = getSelector(false, MatchDemographics.NOISSUER,
                MatchType.BROAD, MatchType.BROAD, MatchType.IGNORE,
                MatchType.IGNORE);

        // two patients, same ID, no issuer, different given name
        Patient patient1 = initupdateOrCreatePatientOnCStore("1001", false, // noissuer
                "Blues^Jack", 1, strict);
        Patient patient2 = initupdateOrCreatePatientOnCStore("1001", false,
                "Blues^Elwood", // different given
                1, strict);
        // third patient with issuer, Broad demographic search, will match Jack
        Patient patient3 = initupdateOrCreatePatientOnCStore("1001", true,
                "Blues^Jack", // different given
                1, broad);

        List<Patient> pats = selectPatientsWithID("1001");
        
        int nr_results = pats.size();
        Set<String> names = new HashSet<String>();
        for (Patient p : pats) names.add(p.getPatientName().getGivenName());
        
         cleanupdateOrCreatePatientOnCStore(new Patient[] { patient1,
         patient2,
         patient3 });
        assertEquals(
                "after feed of patient3, only 2 patients should be present",
                2, nr_results);
        assertEquals(
                "remaining names should be Jack and Elwood",
                new HashSet<String>(Arrays.asList("Jack","Elwood")), names);
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testForceIssuerFails()
            throws Exception {

        PatientSelector strict = getSelector(true, MatchDemographics.ALWAYS,
                MatchType.STRICT, MatchType.STRICT, MatchType.IGNORE,
                MatchType.IGNORE);
        
        // broad = apply demog if issuer is missing
        PatientSelector broad = getSelector(true, MatchDemographics.NOISSUER,
                MatchType.BROAD, MatchType.BROAD, MatchType.IGNORE,
                MatchType.IGNORE);

        Patient patient1 = initupdateOrCreatePatientOnCStore("1001", false, // noissuer
                "Blues^Jack", 1, strict);
        Patient patient2 = initupdateOrCreatePatientOnCStore("1001", false,
                "Blues^Elwood", // different given
                1, strict);
        
        List<Patient> pats = selectPatientsWithID("1001");
        
        int nr_results = pats.size();
        Set<String> names = new HashSet<String>();
        for (Patient p : pats) names.add(p.getPatientName().getGivenName());
        
         cleanupdateOrCreatePatientOnCStore(new Patient[] { patient1,
         patient2,});
        assertEquals(
                "after feed of patient2, 2 patients should be present",
                2, nr_results);
        assertEquals(
                "remaining names should be Jack and Elwood",
                new HashSet<String>(Arrays.asList("Jack","Elwood")), names);    
        }
    
    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testForceIssuerNotFails()
            throws Exception {

        PatientSelector strict = getSelector(true, MatchDemographics.ALWAYS,
                MatchType.STRICT, MatchType.STRICT, MatchType.IGNORE,
                MatchType.IGNORE);
        
        Patient patient1 = initupdateOrCreatePatientOnCStore("1001", true,
                "Blues^Jack", 1, strict);
        Patient patient2 = initupdateOrCreatePatientOnCStore("1001", true,
                "Blues^Jack", // different given
                1, strict);
        
        List<Patient> pats = selectPatientsWithID("1001");
        
        int nr_results = pats.size();
        Set<String> names = new HashSet<String>();
        for (Patient p : pats) names.add(p.getPatientName().getGivenName());
        
         cleanupdateOrCreatePatientOnCStore(new Patient[] { patient1,
         patient2,});
        assertEquals(
                "after feed of patient2, 1 patient should be present",
                1, nr_results);
        assertEquals(
                "remaining name should be Jack",
                new HashSet<String>(Arrays.asList("Jack")), names);    
        }

    
    private List<Patient> selectPatientsWithID(String id) {
        Query query = em
                .createQuery("SELECT p.patient from PatientID p where p.id = ?1");
        query.setParameter(1, id);
        return query.getResultList();
    }

    private Patient initupdateOrCreatePatientOnCStore(String id1,
            boolean withIssuer1, String name1, int issuerRepresentation,
            PatientSelector selector) throws NotSupportedException,
            SystemException, PatientCircularMergedException, SecurityException,
            IllegalStateException, RollbackException, HeuristicMixedException,
            HeuristicRollbackException, IssuerMissingException {
        Attributes patientOneAttributes = new Attributes();
        patientOneAttributes.setString(Tag.PatientName, VR.PN, name1);
        patientOneAttributes.setString(Tag.PatientID, VR.LO, id1);
        if (withIssuer1)
            patientOneAttributes = setIssuer(issuerRepresentation,
                    patientOneAttributes, "G12345");

        Patient patientOne = service.updateOrCreatePatientOnCStore(
                patientOneAttributes, selector, createStoreParam());

        return patientOne;
    }

    /**
     * Sets the issuer.
     * 
     * @param issuerRepresentation
     *            the issuer representation
     * @param patientTwoAttributes
     *            the patient two attributes
     * @param issuer
     *            the issuer
     * @return the attributes
     */
    private Attributes setIssuer(int issuerRepresentation,
            Attributes patientTwoAttributes, String issuer) {
        if (issuerRepresentation == 1) {
            patientTwoAttributes
                    .setString(Tag.IssuerOfPatientID, VR.LO, issuer);
        } else if (issuerRepresentation == 2) {
            Sequence issuerSeq = patientTwoAttributes.newSequence(
                    Tag.IssuerOfPatientIDQualifiersSequence, 1);
            Attributes tempItem = new Attributes();
            tempItem.setString(Tag.UniversalEntityID, VR.UT, issuer);
            tempItem.setString(Tag.UniversalEntityIDType, VR.CS, "ISO");
            issuerSeq.add(tempItem);
        } else {
            patientTwoAttributes
                    .setString(Tag.IssuerOfPatientID, VR.LO, issuer);
            Sequence issuerSeq = patientTwoAttributes.newSequence(
                    Tag.IssuerOfPatientIDQualifiersSequence, 1);
            Attributes tempItem = new Attributes();
            tempItem.setString(Tag.UniversalEntityID, VR.UT, issuer);
            tempItem.setString(Tag.UniversalEntityIDType, VR.CS, "ISO");
            issuerSeq.add(tempItem);
        }

        return patientTwoAttributes;
    }

    /**
     * Creates the store param.
     * 
     * @return the store param
     */
    public static StoreParam createStoreParam() {
        StoreParam storeParam = new StoreParam();
        AttributeFilter[] filter = new AttributeFilter[1];
        filter[0] = PATIENT_ATTR_FILTER;
        storeParam.setAttributeFilters(filter);
        storeParam.setFuzzyStr(new ESoundex());
        return storeParam;
    }

    private PropertyMatchPatientSelector getSelector(boolean mandatoryIssuer,
            MatchDemographics demog, MatchType family, MatchType given,
            MatchType sex, MatchType birth) {

        PropertyMatchPatientSelector s = new PropertyMatchPatientSelector();
        s.setForceIssuer(mandatoryIssuer);
        s.setDemographics(demog);
        s.setFamilyName(family);
        s.setGivenName(given);
        s.setPatientBirthDate(birth);
        s.setPatientSex(sex);

        return s;
    }

    private void cleanupdateOrCreatePatientOnCStore(Patient[] patients)
            throws NotSupportedException, SystemException, SecurityException,
            IllegalStateException, RollbackException, HeuristicMixedException,
            HeuristicRollbackException {
        for (Patient pat : patients) {
            em.remove(pat);
            em.remove(pat.getPatientName());
        }
        em.flush();

        ArrayList<Long> issuersToRemovePKs = new ArrayList<Long>();
        for (Patient p : patients) {
            for (PatientID id : p.getPatientIDs()) {
                if (!issuersToRemovePKs.contains(id.getPk())
                        && id.getIssuer() != null)
                    issuersToRemovePKs.add(id.getIssuer().getPk());
            }
        }

        for (Long pk : issuersToRemovePKs) {
            Issuer tmpIssuer = em.find(Issuer.class, pk);
            if (tmpIssuer != null)
                em.remove(tmpIssuer);
        }
        em.flush();

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

    private static final AttributeFilter PATIENT_ATTR_FILTER = new AttributeFilter(
            PATIENT_ATTRS);

}
