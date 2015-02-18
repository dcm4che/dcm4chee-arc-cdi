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

package org.dcm4chee.archive.store.test;

import java.io.File;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
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
public class InitDataForIT {
    
    private static final String SOURCE_AET = "SOURCE_AET";

    private static final String[] RETRIEVE_AETS = { "RETRIEVE_AET" };

    private static final Logger log = Logger.getLogger(InitDataForIT.class);
    
    @Inject
    private StoreService storeService;

    @Inject
    private Device device;
    
//    @PersistenceContext
//    EntityManager em;
//    
//    @Resource
//    UserTransaction utx;
    
    private static final String[] INSTANCES = {
        "testdata/date-range-1.xml",
        "testdata/date-range-2.xml",
        "testdata/date-range-3.xml",
        "testdata/date-range-4.xml",
        "testdata/date-range-5.xml",
        "testdata/date-range-6.xml",
        "testdata/date-range-7.xml",
        "testdata/accno-issuer-1.xml",
        "testdata/accno-issuer-2.xml",
        "testdata/accno-issuer-3.xml",
        "testdata/req-attrs-seq-1.xml",
        "testdata/req-attrs-seq-2.xml",
        "testdata/req-attrs-seq-3.xml",
        "testdata/mods-in-study-1.xml",
        "testdata/mods-in-study-2.xml",
        "testdata/mods-in-study-3.xml",
        "testdata/mods-in-study-4.xml",
        "testdata/mods-in-study-5.xml",
        "testdata/proc-code-seq-1.xml",
        "testdata/proc-code-seq-2.xml",
        "testdata/proc-code-seq-3.xml",
        "testdata/concept-name-code-seq-1.xml",
        "testdata/concept-name-code-seq-2.xml",
        "testdata/concept-name-code-seq-3.xml",
        "testdata/verifying-observer-seq-1.xml",
        "testdata/verifying-observer-seq-2.xml",
        "testdata/verifying-observer-seq-3.xml",
        "testdata/birthdate-1.xml",
        "testdata/birthdate-2.xml",
        "testdata/birthdate-3.xml",
        "testdata/tf-info-1.xml",
        "testdata/tf-info-2.xml",
        "testdata/fuzzy-1.xml",
        "testdata/fuzzy-2.xml",
        "testdata/fuzzy-3.xml",
        "testdata/fuzzy-4.xml",
        "testdata/fuzzy-5.xml",
        "testdata/person-name-1.xml"
   };
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war= ShrinkWrap.create(WebArchive.class, "test.war");
        war.addClass(InitDataForIT.class);
        war.addClass(ParamFactory.class);
        JavaArchive[] archs =   Maven.resolver()
                .loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies()
                .resolve().withoutTransitivity()
                .as(JavaArchive.class);
        for(JavaArchive a: archs) {
            a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            war.addAsLibrary(a);
        }
        for (String resourceName : INSTANCES)
            war.addAsResource(resourceName);
//        war.as(ZipExporter.class).exportTo(
//                new File("test.war"), true);
        return war;
    }
    @Test
    public void testInitData() throws Exception {
        log.info("Started test");
        StoreParam storeParam = ParamFactory.createStoreParam();
        storeParam.setRetrieveAETs(RETRIEVE_AETS);
        //store param ok
        //store session
        StoreSession session = storeService.createStoreSession(storeService); 
        session.setStoreParam(storeParam);
        StorageSystem storageSystem = new StorageSystem();
        storageSystem.setStorageSystemID("test_ss");        
        StorageSystemGroup grp = new StorageSystemGroup();
        grp.setGroupID("test_grp");
        grp.addStorageSystem(storageSystem);
        session.setStorageSystem(storageSystem);
        session.setSource(new GenericParticipant("localhost", "testidentity"));
        session.setRemoteAET(SOURCE_AET);
        session.setArchiveAEExtension(device.getApplicationEntity("DCM4CHEE")
                .getAEExtension(ArchiveAEExtension.class));
        //Store context only needs the attributes and the store session earlier created
//        utx.begin();
//        em.joinTransaction();
        for (String res : INSTANCES) {
            StoreContext storeContext = storeService.createStoreContext(session);
            storeContext.setAttributes(load(res));
            storeService.updateDB(storeContext);
        }
//        utx.commit();
        // clear the persistence context (first-level cache)
//        em.clear();
     
    }
    private Attributes load(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return SAXReader.parse(cl.getResource(name).toString());
    }
}


