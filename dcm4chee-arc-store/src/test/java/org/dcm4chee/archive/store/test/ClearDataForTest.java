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
import java.util.List;
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
import org.dcm4chee.archive.entity.Code;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.soundex.ESoundex;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
import org.dcm4chee.archive.patient.PatientService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@RunWith(Arquillian.class)
public class ClearDataForTest {

    @Inject
    PatientService patientService;
    @PersistenceContext
    EntityManager em;

    @Resource
    UserTransaction utx;

    @Before
    public void setup() throws NotSupportedException, SystemException {
        utx.begin();
        em.joinTransaction();
    }

    @After
    public void terminate() throws SecurityException, IllegalStateException,
            RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SystemException {
        utx.commit();
        em.clear();
    }

    private static final String[] PIDS = { "RANGE-MATCHING", "ISSUER_OF_ACCNO",
            "REQ_ATTRS_SEQ", "MODS_IN_STUDY", "PROC_CODE_SEQ",
            "CONCEPT_NAME_CODE_SEQ", "VERIFYING_OBSERVER_SEQ", "DOB_20010101",
            "DOB_20020202", "DOB_NONE", "TF_INFO", "FUZZY_GEORGE",
            "FUZZY_JOERG", "FUZZY_LUKE", "FUZZY_NONE", "FUZZY_NUMERICAL",
            "OOMIYA_SHOUGO", "MWL_TEST", };

    private static final String[] ISSUERS = { "DCM4CHEE_TESTDATA",
            "DCM4CHEE_TESTDATA_ACCNO_ISSUER_1","DCM4CHEE_TESTDATA_ACCNO_ISSUER_2", "Issuer No_2", "Issuer No_1" };

    private static final Code[] CODES = {
        new Code("PROC_CODE_1","99DCM4CHEE_TEST",null,"Meaning of PROC_CODE_1"),
        new Code("PROC_CODE_2","99DCM4CHEE_TEST",null,"Meaning of PROC_CODE_2"),
        new Code("CONCEPT_NAME_1","99DCM4CHEE_TEST",null,"Meaning of CONCEPT_NAME_1"),
        new Code("CONCEPT_NAME_2","99DCM4CHEE_TEST",null,"Meaning of CONCEPT_NAME_2"),
        new Code("TCE006","IHERADTF",null,"Additional Teaching File Information"),
        new Code("TCE101","IHERADTF",null,"Author"),
        new Code("TCE104","IHERADTF",null,"Abstract"),
        new Code("TCE105","IHERADTF",null,"Keywords"),
        new Code("TCE109","IHERADTF",null,"Category"),
        new Code("TCE304","IHERADTF",null,"Gastrointestinal"),
        new Code("466.0","I9C",null,"Acute bronchitis"),
        new Code("TCE302","IHERADTF",null,"Pulmonary"),
        };
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
        war.addClass(InitDataForTest.class);
        war.addClass(ParamFactory.class);
        JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies().resolve()
                .withTransitivity().as(JavaArchive.class);
        for (JavaArchive a : archs) {
            a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            war.addAsLibrary(a);
        }

        war.addAsLibraries(archs);
        return war;
    }

    @Test
    public void clearTestData() throws NonUniquePatientException,
            PatientCircularMergedException, SecurityException,
            IllegalStateException, RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SystemException {
        Attributes tmpAttrs = new Attributes();
        for (String pid : PIDS) {
            tmpAttrs.setString(Tag.PatientID, VR.LO, pid);
            tmpAttrs.setString(Tag.IssuerOfPatientID, VR.LO,
                    "DCM4CHEE_TESTDATA");
            Patient tmpPatient = patientService.updateOrCreatePatientOnCStore(
                    tmpAttrs, new IDPatientSelector(), createStoreParam());
            deletePatient(tmpPatient);
            tmpAttrs = new Attributes();
        }
        deleteIssuers();
        deleteCodes();
    }

    private void deleteCodes() {
        for(Code c:CODES)
        {
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

    private void deleteIssuers() {
        
        for (String issuer : ISSUERS) {
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
        AttributeFilter[] filter = new AttributeFilter[1];
        filter[0] = PATIENT_ATTR_FILTER;
        storeParam.setAttributeFilters(filter);
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
    private static final AttributeFilter PATIENT_ATTR_FILTER = new AttributeFilter(
            PATIENT_ATTRS);
}