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

import java.io.File;

import javax.inject.Inject;

import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.store.StoreService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
* @author Gunter Zeilinger <gunterze@gmail.com>
* @author Hesham Elbadawi <bsdreko@gmail.com>
*/
@RunWith(Arquillian.class)
public class ClearDataForTest {

    private static final String[] PIDS = {
        "RANGE-MATCHING",
        "ISSUER_OF_ACCNO",
        "REQ_ATTRS_SEQ",
        "MODS_IN_STUDY",
        "PROC_CODE_SEQ",
        "CONCEPT_NAME_CODE_SEQ",
        "VERIFYING_OBSERVER_SEQ",
        "DOB_20010101",
        "DOB_20020202",
        "DOB_NONE",
        "TF_INFO",
        "FUZZY_GEORGE",
        "FUZZY_JOERG",
        "FUZZY_LUKE",
        "FUZZY_NONE",
        "FUZZY_NUMERICAL",
        "OOMIYA_SHOUGO",
        "MWL_TEST",
    };

    @Inject
    private PatientService patientService;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war= ShrinkWrap.create(WebArchive.class, "test.war");
        war.addClass(InitDataForTest.class);
        war.addClass(ParamFactory.class);
        File[] libs =  Maven.resolver().loadPomFromFile("testpom.xml").importTestDependencies().importRuntimeAndTestDependencies().resolve().withoutTransitivity().asFile();
        JavaArchive storeDependency = Maven.resolver().resolve("org.dcm4che.dcm4chee-arc:dcm4chee-arc-store:4.4.0-SNAPSHOT").withoutTransitivity().asSingle(JavaArchive.class);
        
        storeDependency.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        war.addAsLibraries(storeDependency);
        war.addAsLibraries(libs);
        return war;
    }

    @Test
    public void clearTestData() throws NonUniquePatientException {
        for (String pid : PIDS)
            patientService.deletePatient(
                    new IDWithIssuer(pid, "DCM4CHEE_TESTDATA"));
   }

}