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
package org.dcm4che.archive.store.remember.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

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
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.stow.client.StowClientService;
import org.dcm4chee.archive.stow.client.StowContext;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.dcm4che.archive.store.remember.test.ParamFactory;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoreAndRememberOTWServiceIT {



    private static final String[] DELETE_QUERIES = {
        "DELETE FROM rel_instance_location", "DELETE FROM location",
        "DELETE FROM content_item", "DELETE FROM verify_observer",
        "DELETE FROM instance", "DELETE FROM series_query_attrs",
        "DELETE FROM series_req", "DELETE FROM series",
        "DELETE FROM study_query_attrs", "DELETE FROM rel_study_pcode",
        "DELETE FROM study", "DELETE FROM rel_linked_patient_id",
        "DELETE FROM patient_id", "DELETE FROM id_issuer",
        "DELETE FROM patient", "DELETE FROM soundex_code",
        "DELETE FROM person_name", "DELETE FROM qc_instance_history",
        "DELETE FROM qc_series_history", "DELETE FROM qc_study_history",
        "DELETE FROM qc_action_history", "DELETE FROM qc_update_history",
        "DELETE FROM code", "DELETE FROM dicomattrs" };

        @Inject
        private StowClientService stowClientService;

        @Inject
        private StoreANdRememberOTWObserverTest observer;

        @Inject
        private StoreService storeService;

        @Inject
        private Device device;

        @PersistenceContext(name = "dcm4chee-arc")
        EntityManager em;

        @Inject
        UserTransaction utx;

        private static StorageDeviceExtension archStorageDevExt;

        @Deployment
        public static WebArchive createDeployment() {
            WebArchive war = ShrinkWrap.create(WebArchive.class, "dcm4chee-arc.war");
            war.addClass(StoreAndRememberOTWServiceIT.class);
            war.addClass(ParamFactory.class);
            war.addClass(StoreANdRememberOTWObserverTest.class);
            JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                    .importRuntimeAndTestDependencies().resolve()
                    .withoutTransitivity().as(JavaArchive.class);
            for (JavaArchive a : archs) {
                a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
                war.addAsLibrary(a);
            }
                war.addAsResource("instance.xml");
                war.addAsWebInfResource("ejb-jar.xml");
                war.addAsWebInfResource("jboss-web.xml");
                war.addAsWebInfResource("web.xml");

            if (System.getProperty("exportWar") != null)
                war.as(ZipExporter.class).exportTo(new File("test.war"), true);
            
            return war;
        }

        @Before
        public void init() throws Exception {
            archStorageDevExt = device.getDeviceExtension(
                    StorageDeviceExtension.class);
            clearDB();
            store("instance.xml");
            }

        /*
         * Store and remember test nopo verification via qido applied
         */
        @Test
        public void testAStoreAndRememberOTWNoVerify() {
            String sopUID = "1.1.1.2";
            ApplicationEntity arcAE = device.getApplicationEntity("DCM4CHEE");
            StowContext ctx = new StowContext(arcAE, arcAE);
            ctx.setStowRemoteBaseURL("http://localhost:8080/dcm4chee-arc/");
            ArrayList<ArchiveInstanceLocator> locators = 
                    new ArrayList<ArchiveInstanceLocator>();
            locators.add(locateInstance(sopUID));
            stowClientService.scheduleStow("web-1234", ctx, locators, 1, 1, 1l);
            //test assertions in the observer
        }


        /*
         * Store and remember test nopo verification via qido applied
         */
        @Test
        public void testBVerifyStorage() {
            String sopUID = "1.1.1.2";
            ApplicationEntity arcAE = device.getApplicationEntity("DCM4CHEE");
            StowContext ctx = new StowContext(arcAE, arcAE);
            ctx.setStowRemoteBaseURL("http://localhost:8080/dcm4chee-arc/");
            ArrayList<ArchiveInstanceLocator> locators = 
                    new ArrayList<ArchiveInstanceLocator>();
            locators.add(locateInstance(sopUID));
            stowClientService.scheduleStow("web-1234", ctx, locators, 1, 1, 1l);
            //test assertions in the observer
        }

        private ArchiveInstanceLocator locateInstance(String sopInstanceUID) {
            ArrayList<Instance> list = new ArrayList<Instance>();
            ArrayList<String> uids = new ArrayList<String>();
            uids.add(sopInstanceUID);
            Query query  = em.createNamedQuery(
                    Instance.FIND_BY_SOP_INSTANCE_UID_EAGER_MANY);
            query.setParameter("uids", uids);
            list = (ArrayList<Instance>) query.getResultList();
            Instance inst = list.get(0);
            String sopClassUID = inst.getSopClassUID(); 
            Location location = ((ArrayList<Location>)getFileAliasRefs(inst)).get(0);
            StorageSystem system = archStorageDevExt.getStorageSystem(
                    location.getStorageSystemGroupID(), location.getStorageSystemID());
            ArchiveInstanceLocator instanceLocator = new ArchiveInstanceLocator.Builder(
                    sopClassUID, sopInstanceUID, location.getTransferSyntaxUID())
            .storageSystem(system)
            .storagePath(location.getStoragePath())
            .entryName(location.getEntryName())
            .fileTimeZoneID(location.getTimeZone())
            .withoutBulkdata(true)
            .build();
            if(instanceLocator.getObject() == null)
                instanceLocator.setObject(new Attributes());
            return instanceLocator;
        }


        private Collection<Location> getFileAliasRefs(Instance instance) {
            Query query = em.createQuery("SELECT i.locations FROM Instance"
                    + " i where i.sopInstanceUID = ?1");
            query.setParameter(1, instance.getSopInstanceUID());
            return query.getResultList();
        }

        private boolean store(String updateResource) {

            ArchiveAEExtension arcAEExt = device.getApplicationEntity("DCM4CHEE")
                    .getAEExtension(ArchiveAEExtension.class);

            try {
                utx.begin();
                em.joinTransaction();
                StoreParam storeParam = ParamFactory.createStoreParam();
                StoreSession session = storeService
                        .createStoreSession(storeService);
                session = storeService.createStoreSession(storeService);
                session.setSource(new GenericParticipant("", "storeandremembertest"));
                session.setRemoteAET("none");
                session.setArchiveAEExtension(arcAEExt);
                storeService.initStorageSystem(session);
                storeService.initSpoolDirectory(session);
                StoreContext context = storeService.createStoreContext(session);
                Attributes fmi = new Attributes();
                fmi.setString(Tag.TransferSyntaxUID, VR.UI, "1.2.840.10008.1.2");
                storeService.writeSpoolFile(context, fmi, load(updateResource));
                storeService.parseSpoolFile(context);
                storeService.store(context);
                utx.commit();
                em.clear();
            } catch (Exception e) {
                return false;
            }

            return true;
        }

    private void clearDB() throws NotSupportedException, SystemException,
            SecurityException, IllegalStateException, RollbackException,
            HeuristicMixedException, HeuristicRollbackException {
        utx.begin();
        for (String queryStr : DELETE_QUERIES) {
            Query query = em.createNativeQuery(queryStr);
            query.executeUpdate();
        }
        utx.commit();
    }

    private Attributes load(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return SAXReader.parse(cl.getResource(name).toString());
    }
}
