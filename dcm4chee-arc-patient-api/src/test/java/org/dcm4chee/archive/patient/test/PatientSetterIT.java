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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.dcm4che3.data.Tag;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.entity.PersonName;
import org.dcm4chee.archive.patient.PatientService;
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
 */
@RunWith(Arquillian.class)
public class PatientSetterIT {

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
        war.addClass(PatientSetterIT.class);
        JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies().resolve()
                .withoutTransitivity().as(JavaArchive.class);
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

    /**
     * Testupdate or create patient on c store one patient created old name kept
     * merged i ds same issuer.
     * 
     * @throws Exception
     *             the exception
     */
    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testupdateOrCreatePatientOnCStoreOnePatientCreatedOldNameKeptMergedIDsSameIssuer()
            throws Exception {
        
        PatientID pid = new PatientID();
        Issuer issuer = new Issuer("G111222", "G111222","ISO");
        pid.setIssuer(issuer);
        pid.setID("123");
        
        PersonName pn = new PersonName();
        pn.setFamilyName("Bunny");
        pn.setGivenName("Bugs");
        
        Patient p = new Patient();        
        p.setPatientIDs(new ArrayList<PatientID>(Arrays.asList(pid)));
        p.setPatientName(pn);
        p.setPatientSex("M");
        p.setPatientBirthDate("19800203");
        
        em.persist(issuer);
        em.persist(pid);
        em.persist(pn);
        em.persist(p);
        
        Patient[] patients = new Patient[]{p};
        
        cleanupdateOrCreatePatientOnCStore(patients);
    }
 
    /**
     * Cleanupdate or create patient on c store.
     * 
     * @param patients
     *            the patients
     * @throws NotSupportedException
     *             the not supported exception
     * @throws SystemException
     *             the system exception
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
     */
    private void cleanupdateOrCreatePatientOnCStore(Patient[] patients) {
        for (Patient pat : patients) {
            em.remove(pat);
            em.remove(pat.getPatientName());
        }
        em.flush();

        ArrayList<Long> issuersToRemovePKs = new ArrayList<Long>();
        for (Patient p : patients) {
            for (PatientID id : p.getPatientIDs()) {
                if (!issuersToRemovePKs.contains(id.getPk()))
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

    public static final Map<Entity, AttributeFilter> ATTRIBUTE_FILTERS;

    static {
        ATTRIBUTE_FILTERS = new HashMap<>();
        ATTRIBUTE_FILTERS.put(Entity.Patient, new AttributeFilter(PATIENT_ATTRS));
    }
}
