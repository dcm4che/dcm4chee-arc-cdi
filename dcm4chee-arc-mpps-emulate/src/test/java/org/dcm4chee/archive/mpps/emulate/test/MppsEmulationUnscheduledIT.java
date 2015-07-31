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

package org.dcm4chee.archive.mpps.emulate.test;

import static org.junit.Assert.assertTrue;

import org.dcm4chee.archive.conf.MPPSCreationRule;
import org.dcm4chee.archive.entity.MPPS;
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
 * @author Umberto Cappellini
 */
@RunWith(Arquillian.class)
public class MppsEmulationUnscheduledIT extends MppsEmulationGeneral {

    @Before
    public void setup() throws Exception {

        // use case: modality sends the instances referenceing the mpps,
        // but no mpps
        store("testdata/store-ct-1.xml");
        store("testdata/store-ct-2.xml");
    }

    @After
    public void clear() throws Exception {
        deletePatient();
        deleteIssuers();
        deleteCodes();
    }
    
    @Test
    public void emulate() throws Exception {
        MPPS receivedMpps = find(MPPS_IUID);
        MPPS emulatedMpps = null;
        assertTrue("mpps not null", receivedMpps == null);

        //sub test 1
        log.info("calling emulator with rule:" + MPPSCreationRule.NEVER);
        emulatedMpps = studyUpdateSessionEJB.emulatePerformedProcedureStep(
                createConfigAE(MPPSCreationRule.NEVER), SOURCE_AET,
                STUDY_IUID, mppsService);
        assertTrue("emulated mpps created", emulatedMpps == null);
        log.info("emulated mpps not created.");

        //sub test 2
        log.info("calling emulator with rule:" + MPPSCreationRule.NO_MPPS_CREATE);
        emulatedMpps = studyUpdateSessionEJB.emulatePerformedProcedureStep(
                createConfigAE(MPPSCreationRule.NO_MPPS_CREATE), SOURCE_AET,
                STUDY_IUID, mppsService);
        assertTrue("emulated mpps not created", emulatedMpps != null);
        log.info("created emulated mpps:" + emulatedMpps.getSopInstanceUID());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
        war.addClass(MppsEmulationUnscheduledIT.class);
        war.addClass(ParamFactory.class);
        war.addClass(MppsEmulationGeneral.class);
        JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies().resolve()
                .withoutTransitivity().as(JavaArchive.class);
        for (JavaArchive a : archs) {
            a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            war.addAsLibrary(a);
        }
        for (String resourceName : INSTANCES)
            war.addAsResource(resourceName);
        war.addAsLibraries(archs);
        // war.as(ZipExporter.class).exportTo(
        // new File("C:\\Temp\\emul.zip"), true);
        return war;
    }
    
}
