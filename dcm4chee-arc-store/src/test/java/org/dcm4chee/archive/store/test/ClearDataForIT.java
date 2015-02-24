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
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
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
public class ClearDataForIT extends BaseStoreIT {

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
            "OOMIYA_SHOUGO", "MWL_TEST", "STORE_SERVICE_TEST"};

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
        war.addClass(BaseStoreIT.class);
        war.addClass(InitDataForIT.class);
        war.addClass(ParamFactory.class);
        JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies().resolve()
                .withoutTransitivity().as(JavaArchive.class);
        for (JavaArchive a : archs) {
            a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            war.addAsLibrary(a);
        }
//      war.as(ZipExporter.class).exportTo(
//      new File("test.war"), true);
        war.addAsLibraries(archs);
        return war;
    }

    @Test
    public void clearTestData() throws NonUniquePatientException, SecurityException, IllegalStateException, PatientCircularMergedException, RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
        super.clearTestData(PIDS, ISSUERS, CODES);;
    }

}
