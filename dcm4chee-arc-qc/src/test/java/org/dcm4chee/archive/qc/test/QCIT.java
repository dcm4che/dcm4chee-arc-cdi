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

package org.dcm4chee.archive.qc.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

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
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TransferCapability;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.IOCMConfig;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.dto.QCEventInstance;
import org.dcm4chee.archive.qc.QCEvent;
import org.dcm4chee.archive.entity.AttributesBlob;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.QCActionHistory;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.entity.QCSeriesHistory;
import org.dcm4chee.archive.entity.QCStudyHistory;
import org.dcm4chee.archive.entity.QCUpdateHistory;
import org.dcm4chee.archive.entity.QCUpdateHistory.QCUpdateScope;
import org.dcm4chee.archive.entity.RequestAttributes;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.VerifyingObserver;
import org.dcm4chee.archive.qc.QCBean;
import org.dcm4chee.archive.qc.QCRetrieveBean;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QCIT {

    private static final String[] IOCM_DESTINATIONS = new String[]{"IOCM_DEST"};
    private static final String[] NONE_IOCM_DESTINATIONS = new String[]{"NONE_IOCM_DEST"};

    @Inject
    private StoreService storeService;

    @Inject
    private QCBean qcManager;

    @Inject
    private QCRetrieveBean qcRetrieveManager;

    @Inject
    private Device device;

    @PersistenceContext(name = "dcm4chee-arc")
    EntityManager em;

    @Inject
    UserTransaction utx;

    private static ArchiveDeviceExtension archDevExt;

    private static final String[] ALL_RESOURCES = {
            "testdata/updateSet/update-patient.xml",
            "testdata/updateSet/update-study.xml",
            "testdata/updateSet/update-patient-attrs.xml",
            "testdata/updateSet/update-study-attrs.xml",
            "testdata/updateSet/update-series.xml",
            "testdata/updateSet/update-series-attrs.xml",
            "testdata/updateSet/update-instance.xml",
            "testdata/updateSet/update-instance-attrs.xml",
            "testdata/updateSet/merge-instance1.xml",
            "testdata/updateSet/merge-instance2.xml",
            "testdata/updateSet/merge-instance3.xml",
            "testdata/updateSet/merge-instance4.xml",
            "testdata/updateSet/merge-instance5.xml",
            "testdata/updateSet/split-instance1.xml",
            "testdata/updateSet/split-instance2.xml",
            "testdata/updateSet/split-instance3.xml",
            "testdata/updateSet/split-instance4.xml",
            "testdata/updateSet/split-instance5.xml" };

    private static final String[] UPDATE_RESOURCES = {
            "testdata/updateSet/update-patient.xml",
            "testdata/updateSet/update-study.xml",
            "testdata/updateSet/update-series.xml",
            "testdata/updateSet/update-instance.xml" };

    private static final String[] UPDATE_ATTRS = {
            "testdata/updateSet/update-patient-attrs.xml",
            "testdata/updateSet/update-study-attrs.xml",
            "testdata/updateSet/update-series-attrs.xml",
            "testdata/updateSet/update-instance-attrs.xml" };

    private static final String[] MERGE_RESOURCES = {
            "testdata/updateSet/merge-instance1.xml",
            "testdata/updateSet/merge-instance2.xml",
            "testdata/updateSet/merge-instance3.xml",
            "testdata/updateSet/merge-instance4.xml",
            "testdata/updateSet/merge-instance5.xml" };

    private static final String[] SPLIT_RESOURCES = {
            "testdata/updateSet/split-instance1.xml",
            "testdata/updateSet/split-instance2.xml",
            "testdata/updateSet/split-instance3.xml",
            "testdata/updateSet/split-instance4.xml",
            "testdata/updateSet/split-instance5.xml" };

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

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
        war.addClass(QCIT.class);
        war.addClass(ParamFactory.class);
        war.addClass(PerformedChangeRequest.class);
        war.addClass(ChangeRequesterMockDecorator.class);
        JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies().resolve()
                .withoutTransitivity().as(JavaArchive.class);
        for (JavaArchive a : archs) {
            //a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
                a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            war.addAsLibrary(a);
        }
        for (String resourceName : ALL_RESOURCES)
            war.addAsResource(resourceName);
        
        war.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));
        String manifest="Manifest-Version: 1.0\n" + "Dependencies: org.codehaus.jackson.jackson-jaxrs,org.codehaus.jackson.jackson-mapper-asl,org.dcm4che.net,"+
                "org.dcm4che.soundex, org.dcm4che.conf.api,org.dcm4che.json\n";
        war.setManifest(new StringAsset(manifest));
        war.addAsManifestResource(new File("src/test/resources/META-INF/beans.xml"), "beans.xml");
        
        if (System.getProperty("exportWar") != null)
            war.as(ZipExporter.class).exportTo(new File("test.war"), true);
        
        return war;
    }

    @Before
    public void init() throws Exception {
        archDevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        IOCMConfig iocmCfg = new IOCMConfig();
        iocmCfg.setCallingAET("DCM4CHEE");
        iocmCfg.setIocmDestinations(IOCM_DESTINATIONS);
        iocmCfg.setNoneIocmDestinations(NONE_IOCM_DESTINATIONS);
        iocmCfg.setIocmMaxRetries(2);
        iocmCfg.setIocmRetryInterval(1000);
        archDevExt.setIocmConfig(iocmCfg);
        ApplicationEntity aeIOCM = new ApplicationEntity();
        aeIOCM.setAETitle("IOCM_TEST");
        aeIOCM.setAeInstalled(true);
        ApplicationEntity ae = device.getApplicationEntity("DCM4CHEE");
        aeIOCM.addConnection(ae.getConnections().get(0));
        for (TransferCapability tc : ae.getTransferCapabilities()) {
            TransferCapability tcNew = new TransferCapability(tc.getCommonName(), tc.getSopClass(), tc.getRole(),
                    tc.getTransferSyntaxes());
            aeIOCM.addTransferCapability(tcNew);
        }
        device.addApplicationEntity(aeIOCM);
        /*_*/
        clearDB();
    }

    /*
     * Update DICOM Attributes tests
     */

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testAUpdatePatientAttrs() throws Exception {

        store(UPDATE_RESOURCES[0]);
        utx.begin();
        String[] instancesSopUID = { "1.1.1" };
        ArrayList<Instance> instances = (ArrayList<Instance>) qcManager
                .locateInstances(instancesSopUID);
        Patient pat = instances.get(0).getSeries().getStudy().getPatient();
        Attributes attrs = load(UPDATE_ATTRS[0]);
        QCEvent event = qcManager.updateDicomObject(archDevExt,
                QCUpdateScope.PATIENT, attrs);
        pat = em.merge(pat);
        utx.commit();
        assertTrue(pat.getAttributes().getString(Tag.PatientBirthDate)
                .equalsIgnoreCase(attrs.getString(Tag.PatientBirthDate)));
        PerformedChangeRequest.checkNoNewChangeRequest();
    }

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testBUpdateStudyAttrs() throws Exception {
        store(UPDATE_RESOURCES[1]);
        utx.begin();
        String[] instancesSOPUID = { "1.1.1.1" };
        ArrayList<Instance> instances = (ArrayList<Instance>) qcManager
                .locateInstances(instancesSOPUID);
        Study study = instances.get(0).getSeries().getStudy();
        ArrayList<QCEventInstance> eventUIDs = new ArrayList<QCEventInstance>();
        eventUIDs.add(new QCEventInstance(instancesSOPUID[0], instances.get(0).getSeries().getSeriesInstanceUID(), study.getStudyInstanceUID()));
        Attributes attrs = load(UPDATE_ATTRS[1]);
        QCEvent event = qcManager.updateDicomObject(archDevExt,
                QCUpdateScope.STUDY, attrs);
        study = em.merge(study);
        utx.commit();

        Attributes newStudyAttrs = study.getAttributes();

        assertTrue(newStudyAttrs.getString(Tag.StudyDescription)
                .equalsIgnoreCase(attrs.getString(Tag.StudyDescription)));

        ArrayList<Code> procCodes = new ArrayList<Code>();
        procCodes.addAll(study.getProcedureCodes());
        assertTrue(procCodes.get(0).getCodeValue()
                .equalsIgnoreCase("PROC_CODE_1"));

        assertTrue(procCodes.get(0).getCodingSchemeDesignator()
                .equalsIgnoreCase("99DCM4CHEE_TEST"));

        assertTrue(newStudyAttrs.getString(Tag.AccessionNumber)
                .equalsIgnoreCase(attrs.getString(Tag.AccessionNumber)));

        Issuer issuer = study.getIssuerOfAccessionNumber();
        Attributes issuerOfAccessionNumberItem = attrs.getSequence(
                Tag.IssuerOfAccessionNumberSequence).get(0);
        assertTrue(issuer.getLocalNamespaceEntityID().equalsIgnoreCase(
                issuerOfAccessionNumberItem
                        .getString(Tag.LocalNamespaceEntityID)));
        PerformedChangeRequest.checkChangeRequest(-1, eventUIDs, null, NONE_IOCM_DESTINATIONS);
    }

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testCUpdateStudyAttrsAndLinkToNextUpdateHistory()
            throws Exception {
        store(UPDATE_RESOURCES[1]);
        utx.begin();
        String[] instancesSOPUID = { "1.1.1.1" };
        ArrayList<Instance> instances = (ArrayList<Instance>) qcManager
                .locateInstances(instancesSOPUID);
        Study study = instances.get(0).getSeries().getStudy();
        Attributes attrs = load(UPDATE_ATTRS[1]);
        QCEvent event = qcManager.updateDicomObject(archDevExt,
                QCUpdateScope.STUDY, attrs);
        study = em.merge(study);
        utx.commit();
        utx.begin();

        Attributes attrs2 = new Attributes();
        attrs2.setString(Tag.StudyDescription, VR.LO, "update of update");
        attrs2.setString(Tag.StudyInstanceUID, VR.UI,
                study.getStudyInstanceUID());
        QCEvent event2 = qcManager.updateDicomObject(archDevExt,
                QCUpdateScope.STUDY, attrs2);

        // now retrieve the history
        Query query = em.createQuery("SELECT q FROM QCUpdateHistory q"
                + " WHERE q.objectUID = ?1" + " AND q.next IS NOT NULL");
        query.setParameter(1, study.getStudyInstanceUID());
        QCUpdateHistory prevHistoryNode = (QCUpdateHistory) query
                .getSingleResult();
        QCUpdateHistory nextHistoryNode = prevHistoryNode.getNext();
        utx.commit();

        Attributes newStudyAttrs = study.getAttributes();

        assertTrue(newStudyAttrs.getString(Tag.StudyDescription)
                .equalsIgnoreCase(attrs.getString(Tag.StudyDescription)));
        assertTrue(prevHistoryNode.getObjectUID().equalsIgnoreCase(
                nextHistoryNode.getObjectUID()));
        ArrayList<QCEventInstance> eventUIDs = new ArrayList<QCEventInstance>();
        eventUIDs.add(new QCEventInstance(instancesSOPUID[0], instances.get(0).getSeries().getSeriesInstanceUID(), study.getStudyInstanceUID()));
        PerformedChangeRequest.checkChangeRequest(-1, eventUIDs, null, NONE_IOCM_DESTINATIONS);
    }

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testDUpdateSeriesAttrs() throws Exception {
        store(UPDATE_RESOURCES[2]);
        utx.begin();
        String[] instanceSOPUID = { "1.1.1.2" };
        ArrayList<Instance> instances = new ArrayList<Instance>();
        instances.addAll(qcManager.locateInstances(instanceSOPUID));
        Series series = instances.get(0).getSeries();
        Attributes attrs = load(UPDATE_ATTRS[2]);
        QCEvent event = qcManager.updateDicomObject(archDevExt,
                QCUpdateScope.SERIES, attrs);
        series = em.merge(series);

        ArrayList<RequestAttributes> reqAttrs = new ArrayList<RequestAttributes>();
        reqAttrs.addAll(series.getRequestAttributes());
        utx.commit();
        assertTrue(reqAttrs.get(0).getRequestedProcedureID()
                .equalsIgnoreCase("P-9915"));
        assertTrue(reqAttrs.get(0).getScheduledProcedureStepID()
                .equalsIgnoreCase("9915.1"));
        assertTrue(reqAttrs.get(0).getRequestingPhysician() != null);
        assertTrue(series.getInstitutionCode() != null);
        assertTrue(!series.getAttributes()
                .getSequence(Tag.RequestAttributesSequence).isEmpty());
        ArrayList<QCEventInstance> eventUIDs = new ArrayList<QCEventInstance>();
        eventUIDs.add(new QCEventInstance(instanceSOPUID[0], instances.get(0).getSeries().getSeriesInstanceUID(), series.getStudy().getStudyInstanceUID()));
        PerformedChangeRequest.checkChangeRequest(-1, eventUIDs, null, NONE_IOCM_DESTINATIONS);
    }

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testEUpdateInstanceAttrs() throws Exception {
        store(UPDATE_RESOURCES[3]);
        utx.begin();
        String[] instanceSOPUID = { "1.1.1.3" };
        ArrayList<Instance> instances = new ArrayList<Instance>();
        instances.addAll(qcManager.locateInstances(instanceSOPUID));
        Instance instance = instances.get(0);
        Attributes attrs = load(UPDATE_ATTRS[3]);
        QCEvent event = qcManager.updateDicomObject(archDevExt,
                QCUpdateScope.INSTANCE, attrs);
        instance = em.merge(instance);

        Attributes instanceAttributes = instance.getAttributes();
        Code code = instance.getConceptNameCode();
        ArrayList<VerifyingObserver> verifyingObservers = new ArrayList<VerifyingObserver>();
        verifyingObservers.addAll(instance.getVerifyingObservers());
        utx.commit();
        assertTrue(instanceAttributes.getString(Tag.VerificationFlag)
                .equalsIgnoreCase("VERIFIED"));
        assertTrue(code.getCodeMeaning().equalsIgnoreCase("NEWTITLE"));

        assertTrue(verifyingObservers.get(0).getVerifyingObserverName()
                .getFamilyName().equalsIgnoreCase("VerifyingObserver1"));
        assertTrue(!instanceAttributes.getSequence(
                Tag.VerifyingObserverSequence).isEmpty());
        ArrayList<QCEventInstance> eventUIDs = new ArrayList<QCEventInstance>();
        eventUIDs.add(new QCEventInstance(instanceSOPUID[0], instances.get(0).getSeries().getSeriesInstanceUID(),  instances.get(0).getSeries().getStudy().getStudyInstanceUID()));
        PerformedChangeRequest.checkChangeRequest(-1, eventUIDs, null, NONE_IOCM_DESTINATIONS);
    }

    /*
     * Combined operations tests
     */

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testFmergeStudiesUpdateAccessionNumberUpdateBodyPartExamined()
            throws Exception {
        utx.begin();
        QCActionHistory prevaction = new QCActionHistory();
        prevaction.setCreatedTime(new Date());
        prevaction.setAction("SPLIT");
        em.persist(prevaction);
        QCStudyHistory prevStudyHistory = new QCStudyHistory(null, prevaction);
        prevStudyHistory.setOldStudyUID("X.X.X");
        prevStudyHistory.setNextStudyUID("3.3.3.3");
        em.persist(prevStudyHistory);
        QCSeriesHistory prevSeriesHistory = new QCSeriesHistory(null,
                prevStudyHistory);
        prevSeriesHistory.setOldSeriesUID("Y.Y.Y");
        em.persist(prevSeriesHistory);
        QCInstanceHistory prevInstForKO = new QCInstanceHistory("3.3.3.3",
                "2.2.2.100", "KOX", "KO1", "KO1", false);
        prevInstForKO.setSeries(prevSeriesHistory);
        em.persist(prevInstForKO);
        utx.commit();

        for (String resource : MERGE_RESOURCES) {
            store(resource);
        }

        utx.begin();
        String[] sourceStudyUids = { "3.3.3.3" };
        String targetStudyUID = "3.3.3.2";
        Attributes enrichedStudyAttrs = new Attributes();
        Attributes enrichedSeriesAttrs = new Attributes();
        enrichedStudyAttrs.setString(Tag.AccessionNumber, VR.SH, "123456789");
        enrichedSeriesAttrs.setString(Tag.BodyPartExamined, VR.CS, "ABDOMIN");

        QCEvent event = qcManager.mergeStudies(sourceStudyUids, targetStudyUID,
                enrichedStudyAttrs, enrichedSeriesAttrs,
                new org.dcm4che3.data.Code(
                        "(113001, DCM, \"Rejected for Quality Reasons\")"));
        utx.commit();

        QCInstanceHistory firstKO = getInstanceHistoryByOldUID("KOX");
        QCInstanceHistory newKO = getInstanceHistoryByOldUID("KO1");
        String[] instanceSOPUID = { newKO.getCurrentUID() };

        ArrayList<Instance> instances = new ArrayList<Instance>();
        instances.addAll(qcManager.locateInstances(instanceSOPUID));

        // check file alias table links created
        ArrayList<Location> refs = (ArrayList<Location>) getFileAliasRefs(instances
                .get(0));
        assertTrue(refs.size() == 1);

        // check moved
        assertTrue(instances.get(0).getSeries().getStudy()
                .getStudyInstanceUID().equalsIgnoreCase("3.3.3.2"));

        // check attributes update applied
        assertTrue(instances.get(0).getSeries().getStudy().getAccessionNumber()
                .equalsIgnoreCase("123456789"));
        assertTrue(instances.get(0).getSeries().getBodyPartExamined()
                .equalsIgnoreCase("ABDOMIN"));

        // history created
        // study
        assertTrue(newKO.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("3.3.3.3"));
        // series
        assertTrue(newKO.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("2.2.2.100"));
        // current updated
        // instance
        assertTrue(firstKO.getCurrentUID().equalsIgnoreCase(
                newKO.getCurrentUID()));
        // series
        assertTrue(firstKO.getCurrentSeriesUID().equalsIgnoreCase(
                newKO.getCurrentSeriesUID()));
        // study
        assertTrue(firstKO.getCurrentStudyUID().equalsIgnoreCase(
                newKO.getCurrentStudyUID()));
        // next updated
        assertTrue(firstKO.getNextUID().equalsIgnoreCase("KO1"));
        // is last item
        assertTrue(newKO.getCurrentUID().equalsIgnoreCase(newKO.getNextUID()));

        String[] identSOPUID = { "KO1IDENT" };
        ArrayList<Instance> thirdParty = new ArrayList<Instance>();
        thirdParty.addAll(qcManager.locateInstances(identSOPUID));

        // identical document sequence updated
        checkTwoDocsReferenceEachOtherInIdenticalSeq(instances.get(0)
                .getAttributes(), thirdParty.get(0).getAttributes());

        PerformedChangeRequest.checkChangeRequests(-1, event.getTarget(), event.getRejectionNotes(), IOCM_DESTINATIONS);
    }

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testGSplitTargetStudyExistsNoEnrichAllReferencedInstancesMoved()
            throws Exception {
        initSplitOrSegmentData();

        // test split all from series ( tests all KO references moved case)
        utx.begin();
        String[] toMoveSOPUIDs = { "IMG1", "IMG2" };

        QCEvent event = qcManager.split(Arrays.asList(toMoveSOPUIDs),
                new IDWithIssuer("Bugs1231", new org.dcm4che3.data.Issuer(
                        "BugsIssuer1231", null, null)), "STUDY2",
                new Attributes(), new Attributes(), new org.dcm4che3.data.Code(
                        "(113001, DCM, \"Rejected for Quality Reasons\")"));
        utx.commit();

        QCInstanceHistory firstKO = getInstanceHistoryByOldUID("IMGX");
        QCInstanceHistory newIMG1 = getInstanceHistoryByOldUID("IMG1");
        QCInstanceHistory newIMG2 = getInstanceHistoryByOldUID("IMG2");
        QCInstanceHistory newKO = getInstanceHistoryByOldUID("KO1");

        String[] instanceSOPUID = { newIMG1.getCurrentUID(),
                newIMG2.getCurrentUID(), newKO.getCurrentUID() };

        ArrayList<Instance> instances = new ArrayList<Instance>();
        instances.addAll(qcManager.locateInstances(instanceSOPUID));
        // check file alias table links created
        ArrayList<Location> refs = (ArrayList<Location>) getFileAliasRefs(instances
                .get(0));
        ArrayList<Location> refs2 = (ArrayList<Location>) getFileAliasRefs(instances
                .get(1));
        ArrayList<Location> refs3 = (ArrayList<Location>) getFileAliasRefs(instances
                .get(2));
        assertTrue(refs.size() == 1);
        assertTrue(refs2.size() == 1);
        assertTrue(refs3.size() == 1);

        // moved
        assertTrue(instances.get(0).getSeries().getStudy()
                .getStudyInstanceUID().equalsIgnoreCase("STUDY2"));
        assertTrue(instances.get(1).getSeries().getStudy()
                .getStudyInstanceUID().equalsIgnoreCase("STUDY2"));
        assertTrue(instances.get(2).getSeries().getStudy()
                .getStudyInstanceUID().equalsIgnoreCase("STUDY2"));

        // same new series for instances from same old series
        assertTrue(instances
                .get(0)
                .getSeries()
                .getSeriesInstanceUID()
                .equalsIgnoreCase(
                        instances.get(1).getSeries().getSeriesInstanceUID()));
        // different series for KO
        assertFalse(instances
                .get(0)
                .getSeries()
                .getSeriesInstanceUID()
                .equalsIgnoreCase(
                        instances.get(2).getSeries().getSeriesInstanceUID()));

        // history created
        assertTrue(newIMG1.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));
        assertTrue(newIMG2.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));
        assertTrue(newKO.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));

        assertTrue(newIMG1.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES2"));
        assertTrue(newIMG2.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES2"));
        assertTrue(newKO.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES1"));

        assertTrue(newIMG1.getOldUID().equalsIgnoreCase("IMG1"));
        assertTrue(newIMG2.getOldUID().equalsIgnoreCase("IMG2"));
        assertTrue(newKO.getOldUID().equalsIgnoreCase("KO1"));

        // current updated
        // instance 1
        assertTrue(firstKO.getCurrentUID().equalsIgnoreCase(
                newIMG1.getCurrentUID()));
        // series
        assertTrue(firstKO.getCurrentSeriesUID().equalsIgnoreCase(
                newIMG1.getCurrentSeriesUID()));
        // study
        assertTrue(firstKO.getCurrentStudyUID().equalsIgnoreCase(
                newIMG1.getCurrentStudyUID()));
        // next updated
        assertTrue(firstKO.getNextUID().equalsIgnoreCase("IMG1"));
        // is last item
        assertTrue(newIMG1.getCurrentUID().equalsIgnoreCase(
                newIMG1.getNextUID()));

        // instance 2
        // is last item
        assertTrue(newIMG2.getCurrentUID().equalsIgnoreCase(
                newIMG2.getNextUID()));

        // for invisible objects
        assertTrue(newKO.getCurrentStudyUID().equalsIgnoreCase(
                newIMG1.getCurrentStudyUID()));
        assertTrue(newKO.getNextUID().equalsIgnoreCase(newKO.getCurrentUID()));

        String[] identSOPUID = { "KO1IDENT" };
        ArrayList<Instance> thirdParty = new ArrayList<Instance>();
        thirdParty.addAll(qcManager.locateInstances(identSOPUID));

        // identical document sequence updated
        checkTwoDocsReferenceEachOtherInIdenticalSeq(instances.get(2)
                .getAttributes(), thirdParty.get(0).getAttributes());

        PerformedChangeRequest.checkChangeRequest(-1, event.getTarget(), getRejectionNote(event), IOCM_DESTINATIONS);
    }

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testHSplitTargetStudyNotExistsEnrichSomeReferencedInstancesMovedNewStudy()
            throws Exception {
        initSplitOrSegmentData();

        // test split some from series ( tests some KO references moved case,
        // implies a clone)
        utx.begin();
        String[] toMoveSOPUIDs = { "IMG1" };

        Attributes enrichSeriesAttributes = new Attributes();
        enrichSeriesAttributes.setString(Tag.BodyPartExamined, VR.CS, "HAND");
        Attributes createdStudyAttrs = new Attributes();
        createdStudyAttrs.setString(Tag.AccessionNumber, VR.SH, "2525");
        createdStudyAttrs.setString(Tag.StudyDescription, VR.LO,
                "Created Study by split");
        createdStudyAttrs.setString(Tag.StudyInstanceUID, VR.UI,
                "CREATEDSTUDYFRSPLIT");
        QCEvent event = qcManager.split(Arrays.asList(toMoveSOPUIDs),
                new IDWithIssuer("Bugs1231", new org.dcm4che3.data.Issuer(
                        "BugsIssuer1231", null, null)), "CREATEDSTUDYFRSPLIT",
                createdStudyAttrs, enrichSeriesAttributes,
                new org.dcm4che3.data.Code(
                        "(113001, DCM, \"Rejected for Quality Reasons\")"));
        utx.commit();

        QCInstanceHistory firstIMG = getInstanceHistoryByOldUID("IMGX");
        QCInstanceHistory newIMG1 = getInstanceHistoryByOldUID("IMG1");
        QCInstanceHistory newKO = getInstanceHistoryByOldUID("KO1");

        String[] instanceSOPUID = { newIMG1.getCurrentUID(),
                newKO.getCurrentUID() };

        ArrayList<Instance> instances = new ArrayList<Instance>();
        instances.addAll(qcManager.locateInstances(instanceSOPUID));

        String[] identSOPUID = { "KO1IDENT" };
        ArrayList<Instance> thirdParty = new ArrayList<Instance>();
        thirdParty.addAll(qcManager.locateInstances(identSOPUID));

        // check file alias table links created
        ArrayList<Location> refs = (ArrayList<Location>) getFileAliasRefs(instances
                .get(0));
        ArrayList<Location> refs2 = (ArrayList<Location>) getFileAliasRefs(instances
                .get(1));
        assertTrue(refs.size() == 1);
        assertTrue(refs2.size() == 1);
        // test study created enriched with new attributes
        assertTrue(instances.get(0).getSeries().getStudy().getAccessionNumber()
                .equalsIgnoreCase("2525"));
        // test series enriched with new attributes
        assertTrue(instances.get(0).getSeries().getBodyPartExamined()
                .equalsIgnoreCase("HAND"));
        // test history created
        assertTrue(newIMG1.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));
        assertTrue(newKO.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));

        assertTrue(newIMG1.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES2"));
        assertTrue(newKO.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES1"));
        // current updated
        // instance
        assertTrue(firstIMG.getCurrentUID().equalsIgnoreCase(
                newIMG1.getCurrentUID()));
        // series
        assertTrue(firstIMG.getCurrentSeriesUID().equalsIgnoreCase(
                newIMG1.getCurrentSeriesUID()));
        // study
        assertTrue(firstIMG.getCurrentStudyUID().equalsIgnoreCase(
                newIMG1.getCurrentStudyUID()));
        // next updated
        assertTrue(firstIMG.getNextUID().equalsIgnoreCase("IMG1"));
        // is last item
        assertTrue(newIMG1.getCurrentUID().equalsIgnoreCase(
                newIMG1.getNextUID()));

        assertTrue(newIMG1.getOldUID().equalsIgnoreCase("IMG1"));
        assertTrue(newKO.getOldUID().equalsIgnoreCase("KO1"));

        // test old attributes are persisted in history
        // study

        AttributesBlob studyHistoryBlob = newIMG1.getSeries().getStudy()
                .getUpdatedAttributesBlob();
        AttributesBlob seriesHistoryBlob = newIMG1.getSeries()
                .getUpdatedAttributesBlob();

        assertTrue(studyHistoryBlob != null);
        assertTrue(seriesHistoryBlob != null);
        assertTrue(studyHistoryBlob.getAttributes()
                .getString(Tag.AccessionNumber).equalsIgnoreCase("A12345"));
        // series
        assertTrue(seriesHistoryBlob.getAttributes()
                .getString(Tag.BodyPartExamined).equalsIgnoreCase("BRAIN"));

        // test clone
        assertTrue(newKO.isCloned());

        String[] oldKOID = { newKO.getOldUID() };
        ArrayList<Instance> matches = new ArrayList<Instance>();
        matches.addAll(qcManager.locateInstances(oldKOID));
        // test old KO is not rejected
        assertTrue(matches.get(0).getRejectionNoteCode() == null);

        // test all three KOs reference each other in the identical document
        // sequence
        allReferencedInIdenticalDocumentSequence(matches.get(0),
                instances.get(1), thirdParty.get(0));

        PerformedChangeRequest.checkChangeRequest(-1, event.getTarget(), getRejectionNote(event), IOCM_DESTINATIONS);
    }

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testISegmentNoEnrichMoveOnly() throws Exception {
        initSplitOrSegmentData();
        // test split all from series ( tests all KO references moved case)
        utx.begin();
        String[] toMoveSOPUIDs = { "IMG1", "IMG2" };
        QCEvent event = qcManager.segment(Arrays.asList(toMoveSOPUIDs),
                new ArrayList<String>(), new IDWithIssuer("Bugs1231",
                        new org.dcm4che3.data.Issuer("BugsIssuer1231", null,
                                null)), "STUDY2", new Attributes(),
                new Attributes(), new org.dcm4che3.data.Code(
                        "(113001, DCM, \"Rejected for Quality Reasons\")"));
        utx.commit();

        QCInstanceHistory firstKO = getInstanceHistoryByOldUID("IMGX");
        QCInstanceHistory newIMG1 = getInstanceHistoryByOldUID("IMG1");
        QCInstanceHistory newIMG2 = getInstanceHistoryByOldUID("IMG2");
        QCInstanceHistory newKO = getInstanceHistoryByOldUID("KO1");

        String[] instanceSOPUID = { newIMG1.getCurrentUID(),
                newIMG2.getCurrentUID(), newKO.getCurrentUID() };

        ArrayList<Instance> instances = new ArrayList<Instance>();
        instances.addAll(qcManager.locateInstances(instanceSOPUID));
        // check file alias table links created
        ArrayList<Location> refs = (ArrayList<Location>) getFileAliasRefs(instances
                .get(0));
        ArrayList<Location> refs2 = (ArrayList<Location>) getFileAliasRefs(instances
                .get(1));
        ArrayList<Location> refs3 = (ArrayList<Location>) getFileAliasRefs(instances
                .get(2));
        assertTrue(refs.size() == 1);
        assertTrue(refs2.size() == 1);
        assertTrue(refs3.size() == 1);

        // moved
        assertTrue(instances.get(0).getSeries().getStudy()
                .getStudyInstanceUID().equalsIgnoreCase("STUDY2"));
        assertTrue(instances.get(1).getSeries().getStudy()
                .getStudyInstanceUID().equalsIgnoreCase("STUDY2"));
        assertTrue(instances.get(2).getSeries().getStudy()
                .getStudyInstanceUID().equalsIgnoreCase("STUDY2"));

        // same new series for instances from same old series
        assertTrue(instances
                .get(0)
                .getSeries()
                .getSeriesInstanceUID()
                .equalsIgnoreCase(
                        instances.get(1).getSeries().getSeriesInstanceUID()));
        // different series for KO
        assertFalse(instances
                .get(0)
                .getSeries()
                .getSeriesInstanceUID()
                .equalsIgnoreCase(
                        instances.get(2).getSeries().getSeriesInstanceUID()));

        // history created
        assertTrue(newIMG1.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));
        assertTrue(newIMG2.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));
        assertTrue(newKO.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));

        assertTrue(newIMG1.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES2"));
        assertTrue(newIMG2.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES2"));
        assertTrue(newKO.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES1"));

        assertTrue(newIMG1.getOldUID().equalsIgnoreCase("IMG1"));
        assertTrue(newIMG2.getOldUID().equalsIgnoreCase("IMG2"));
        assertTrue(newKO.getOldUID().equalsIgnoreCase("KO1"));

        // current updated
        // instance 1
        assertTrue(firstKO.getCurrentUID().equalsIgnoreCase(
                newIMG1.getCurrentUID()));
        // series
        assertTrue(firstKO.getCurrentSeriesUID().equalsIgnoreCase(
                newIMG1.getCurrentSeriesUID()));
        // study
        assertTrue(firstKO.getCurrentStudyUID().equalsIgnoreCase(
                newIMG1.getCurrentStudyUID()));
        // next updated
        assertTrue(firstKO.getNextUID().equalsIgnoreCase("IMG1"));
        // is last item
        assertTrue(newIMG1.getCurrentUID().equalsIgnoreCase(
                newIMG1.getNextUID()));

        // instance 2
        // is last item
        assertTrue(newIMG2.getCurrentUID().equalsIgnoreCase(
                newIMG2.getNextUID()));

        // for invisible objects
        assertTrue(newKO.getCurrentStudyUID().equalsIgnoreCase(
                newIMG1.getCurrentStudyUID()));
        assertTrue(newKO.getNextUID().equalsIgnoreCase(newKO.getCurrentUID()));

        String[] identSOPUID = { "KO1IDENT" };
        ArrayList<Instance> thirdParty = new ArrayList<Instance>();
        thirdParty.addAll(qcManager.locateInstances(identSOPUID));

        // identical document sequence updated
        checkTwoDocsReferenceEachOtherInIdenticalSeq(instances.get(2)
                .getAttributes(), thirdParty.get(0).getAttributes());
        PerformedChangeRequest.checkChangeRequest(-1, event.getTarget(), getRejectionNote(event), IOCM_DESTINATIONS);
    }

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testJSegmentNoEnrichCloneOnly() throws Exception {
        initSplitOrSegmentData();
        // test split all from series ( tests all KO references moved case)
        utx.begin();
        String[] toCloneSOPUIDs = { "IMG1", "IMG2" };

        QCEvent event = qcManager.segment(new ArrayList<String>(), Arrays
                .asList(toCloneSOPUIDs), new IDWithIssuer("Bugs1231",
                new org.dcm4che3.data.Issuer("BugsIssuer1231", null, null)),
                "STUDY2", new Attributes(), new Attributes(),
                new org.dcm4che3.data.Code(
                        "(113001, DCM, \"Rejected for Quality Reasons\")"));
        utx.commit();

        QCInstanceHistory firstKO = getInstanceHistoryByOldUID("IMGX");
        QCInstanceHistory newIMG1 = getInstanceHistoryByOldUID("IMG1");
        QCInstanceHistory newIMG2 = getInstanceHistoryByOldUID("IMG2");

        String[] instanceSOPUID = { newIMG1.getCurrentUID(),
                newIMG2.getCurrentUID(), };

        ArrayList<Instance> instances = new ArrayList<Instance>();
        instances.addAll(qcManager.locateInstances(instanceSOPUID));
        // check file alias table links created
        ArrayList<Location> refs = (ArrayList<Location>) getFileAliasRefs(instances
                .get(0));
        ArrayList<Location> refs2 = (ArrayList<Location>) getFileAliasRefs(instances
                .get(1));
        assertTrue(refs.size() == 1);
        assertTrue(refs2.size() == 1);

        // check cloned
        assertTrue(instances.get(0).getSeries().getStudy()
                .getStudyInstanceUID().equalsIgnoreCase("STUDY2"));
        assertTrue(instances.get(1).getSeries().getStudy()
                .getStudyInstanceUID().equalsIgnoreCase("STUDY2"));

        // //check old ones are not rejected
        // for(Instance oldInstance : toCloneInstances)
        // assertTrue(oldInstance.getRejectionNoteCode()==null);

        // test if all are cloned in the history
        assertTrue(newIMG1.isCloned());
        assertTrue(newIMG2.isCloned());

        // same new series for instances from same old series
        assertTrue(instances
                .get(0)
                .getSeries()
                .getSeriesInstanceUID()
                .equalsIgnoreCase(
                        instances.get(1).getSeries().getSeriesInstanceUID()));

        // history created
        assertTrue(newIMG1.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));
        assertTrue(newIMG2.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));

        assertTrue(newIMG1.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES2"));
        assertTrue(newIMG2.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES2"));

        assertTrue(newIMG1.getOldUID().equalsIgnoreCase("IMG1"));
        assertTrue(newIMG2.getOldUID().equalsIgnoreCase("IMG2"));

        PerformedChangeRequest.checkChangeRequest(-1, event.getTarget(), getRejectionNote(event), IOCM_DESTINATIONS);
    }

    @Test
    @Ignore("Test have to be adapted/activated after QC changes by Hesham")
    public void testKSegmentEnrichSeriesEnrichStudyMoveClone() throws Exception {
        initSplitOrSegmentData();

        utx.begin();
        String[] toMoveSOPUIDs = { "IMG1" };
        String[] toCloneSOPUIDs = { "IMG2" };

        Attributes enrichSeriesAttributes = new Attributes();
        enrichSeriesAttributes.setString(Tag.BodyPartExamined, VR.CS, "HAND");
        Attributes targetStudyAttrs = new Attributes();
        targetStudyAttrs.setString(Tag.AccessionNumber, VR.SH, "2525");
        targetStudyAttrs.setString(Tag.StudyDescription, VR.LO,
                "SEGMENTEDSTUDY");
        targetStudyAttrs.setString(Tag.StudyInstanceUID, VR.UI, "STUDY2");
        QCEvent event = qcManager.segment(Arrays.asList(toMoveSOPUIDs), Arrays
                .asList(toCloneSOPUIDs), new IDWithIssuer("Bugs1231",
                new org.dcm4che3.data.Issuer("BugsIssuer1231", null, null)),
                "STUDY2", targetStudyAttrs, enrichSeriesAttributes,
                new org.dcm4che3.data.Code(
                        "(113001, DCM, \"Rejected for Quality Reasons\")"));
        utx.commit();

        QCInstanceHistory firstIMG = getInstanceHistoryByOldUID("IMGX");
        QCInstanceHistory newIMG1 = getInstanceHistoryByOldUID("IMG1");
        QCInstanceHistory newIMG2 = getInstanceHistoryByOldUID("IMG2");
        QCInstanceHistory newKO = getInstanceHistoryByOldUID("KO1");

        String[] instanceSOPUID = { newIMG1.getCurrentUID(),
                newKO.getCurrentUID() };

        ArrayList<Instance> instances = new ArrayList<Instance>();
        instances.addAll(qcManager.locateInstances(instanceSOPUID));

        String[] identSOPUID = { "KO1IDENT" };
        ArrayList<Instance> thirdParty = new ArrayList<Instance>();
        thirdParty.addAll(qcManager.locateInstances(identSOPUID));

        // check file alias table links created
        ArrayList<Location> refs = (ArrayList<Location>) getFileAliasRefs(instances
                .get(0));
        ArrayList<Location> refs2 = (ArrayList<Location>) getFileAliasRefs(instances
                .get(1));
        assertTrue(refs.size() == 1);
        assertTrue(refs2.size() == 1);
        // test study created enriched with new attributes
        assertTrue(instances.get(0).getSeries().getStudy().getAccessionNumber()
                .equalsIgnoreCase("2525"));
        // test series enriched with new attributes
        assertTrue(instances.get(0).getSeries().getBodyPartExamined()
                .equalsIgnoreCase("HAND"));
        // test history created
        assertTrue(newIMG1.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));
        assertTrue(newKO.getSeries().getStudy().getOldStudyUID()
                .equalsIgnoreCase("STUDY1"));

        assertTrue(newIMG1.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES2"));
        assertTrue(newKO.getSeries().getOldSeriesUID()
                .equalsIgnoreCase("SERIES1"));
        // current updated
        // instance
        assertTrue(firstIMG.getCurrentUID().equalsIgnoreCase(
                newIMG1.getCurrentUID()));
        // series
        assertTrue(firstIMG.getCurrentSeriesUID().equalsIgnoreCase(
                newIMG1.getCurrentSeriesUID()));
        // study
        assertTrue(firstIMG.getCurrentStudyUID().equalsIgnoreCase(
                newIMG1.getCurrentStudyUID()));
        // next updated
        assertTrue(firstIMG.getNextUID().equalsIgnoreCase("IMG1"));
        // is last item
        assertTrue(newIMG1.getCurrentUID().equalsIgnoreCase(
                newIMG1.getNextUID()));

        assertTrue(newIMG1.getOldUID().equalsIgnoreCase("IMG1"));
        assertTrue(newKO.getOldUID().equalsIgnoreCase("KO1"));

        // test old attributes are persisted in history
        // study

        AttributesBlob studyHistoryBlob = newIMG1.getSeries().getStudy()
                .getUpdatedAttributesBlob();
        AttributesBlob seriesHistoryBlob = newIMG1.getSeries()
                .getUpdatedAttributesBlob();

        assertTrue(studyHistoryBlob != null);
        assertTrue(seriesHistoryBlob != null);
        assertTrue(studyHistoryBlob.getAttributes()
                .getString(Tag.AccessionNumber).equalsIgnoreCase("A12345"));
        // series
        assertTrue(seriesHistoryBlob.getAttributes()
                .getString(Tag.BodyPartExamined).equalsIgnoreCase("BRAIN"));

        // test clone
        assertTrue(newKO.isCloned());

        String[] oldKOID = { newKO.getOldUID() };
        ArrayList<Instance> matches = new ArrayList<Instance>();
        matches.addAll(qcManager.locateInstances(oldKOID));
        // test old KO is not rejected
        assertTrue(matches.get(0).getRejectionNoteCode() == null);

        // test all three KOs reference each other in the identical document
        // sequence
        allReferencedInIdenticalDocumentSequence(matches.get(0),
                instances.get(1), thirdParty.get(0));

        // test old attributes are logged
        QCInstanceHistory identKO = getInstanceHistoryByOldUID("KO1IDENT");
        assertTrue(identKO.getPreviousAtributesBlob() != null);
        // test IMG2 is cloned
        assertTrue(newIMG2.isCloned());
        // test getQCed for STUDY1 (IMG2 was not QCed)
        assertTrue(qcRetrieveManager.requiresReferenceUpdate("STUDY1", null));

        PerformedChangeRequest.checkChangeRequest(-1, event.getTarget(), getRejectionNote(event), IOCM_DESTINATIONS);
    }

    private boolean allReferencedInIdenticalDocumentSequence(Instance instance,
            Instance instance2, Instance instance3) {
        Attributes attrs = instance.getAttributes();
        Attributes attrs2 = instance2.getAttributes();
        Attributes attrs3 = instance3.getAttributes();
        return (checkTwoDocsReferenceEachOtherInIdenticalSeq(attrs2, attrs)
                && checkTwoDocsReferenceEachOtherInIdenticalSeq(attrs2, attrs3)
                && checkTwoDocsReferenceEachOtherInIdenticalSeq(attrs, attrs2)
                && checkTwoDocsReferenceEachOtherInIdenticalSeq(attrs, attrs3)
                && checkTwoDocsReferenceEachOtherInIdenticalSeq(attrs3, attrs) && checkTwoDocsReferenceEachOtherInIdenticalSeq(
                    attrs3, attrs2));
    }

    private boolean checkTwoDocsReferenceEachOtherInIdenticalSeq(
            Attributes attrs, Attributes attrs2) {
        int result = 0;
        Sequence seq1 = attrs.getSequence(Tag.IdenticalDocumentsSequence);
        Sequence seq2 = attrs.getSequence(Tag.IdenticalDocumentsSequence);

        for (Attributes item : seq1) {
            if (itemContainsReferencedSOPID(item,
                    attrs2.getString(Tag.SOPInstanceUID)))
                result++;
        }
        for (Attributes item : seq2) {
            if (itemContainsReferencedSOPID(item,
                    attrs.getString(Tag.SOPInstanceUID)))
                result++;
        }
        if (result == 2)
            return true;

        return false;
    }

    private boolean itemContainsReferencedSOPID(Attributes item, String string) {

        for (Attributes seriesItem : item
                .getSequence(Tag.ReferencedSeriesSequence)) {
            for (Attributes sopItem : seriesItem
                    .getSequence(Tag.ReferencedSOPSequence)) {
                if (sopItem.getString(Tag.ReferencedSOPInstanceUID)
                        .equalsIgnoreCase(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initSplitOrSegmentData() throws NotSupportedException,
            SystemException, RollbackException, HeuristicMixedException,
            HeuristicRollbackException {
        utx.begin();

        QCActionHistory prevaction = new QCActionHistory();
        prevaction.setCreatedTime(new Date());
        prevaction.setAction("SPLIT");
        em.persist(prevaction);
        QCStudyHistory prevStudyHistory = new QCStudyHistory(null, prevaction);
        prevStudyHistory.setOldStudyUID("X.X.X");
        prevStudyHistory.setNextStudyUID("STUDY1");
        em.persist(prevStudyHistory);
        QCSeriesHistory prevSeriesHistory = new QCSeriesHistory(null,
                prevStudyHistory);
        prevSeriesHistory.setOldSeriesUID("Y.Y.Y");
        em.persist(prevSeriesHistory);
        QCInstanceHistory prevInstForKO = new QCInstanceHistory("STUDY1",
                "SERIES2", "IMGX", "IMG1", "IMG1", false);
        prevInstForKO.setSeries(prevSeriesHistory);
        em.persist(prevInstForKO);
        utx.commit();
        for (String resource : SPLIT_RESOURCES)
            store(resource);

    }

    private Collection<Location> getFileAliasRefs(Instance instance) {
        Query query = em.createQuery("SELECT i.locations FROM Instance"
                + " i where i.sopInstanceUID = ?1");
        query.setParameter(1, instance.getSopInstanceUID());
        return query.getResultList();
    }

    private QCInstanceHistory getInstanceHistoryByOldUID(String uid) {
        Query query = em.createNamedQuery(QCInstanceHistory.FIND_BY_OLD_UID);
        query.setParameter(1, uid);
        return (QCInstanceHistory) query.getSingleResult();
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
            session.setSource(new GenericParticipant("", "qcTest"));
            session.setRemoteAET("none");
            session.setArchiveAEExtension(arcAEExt);
            storeService.initBulkdataStorage(session);
            storeService.initSpoolingStorage(session);
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

    private Attributes load(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return SAXReader.parse(cl.getResource(name).toString());
    }
    
    private Instance getRejectionNote(QCEvent event) {
        Collection<Instance> rejNotes = event.getRejectionNotes();
        return (rejNotes == null || rejNotes.isEmpty()) ? null : rejNotes.iterator().next();
    }
}
