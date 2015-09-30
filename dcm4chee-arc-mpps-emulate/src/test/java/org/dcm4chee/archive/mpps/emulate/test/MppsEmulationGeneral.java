package org.dcm4chee.archive.mpps.emulate.test;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.MPPSCreationRule;
import org.dcm4chee.archive.conf.MPPSEmulationAndStudyUpdateRule;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.mpps.emulate.MPPSEmulator;
import org.dcm4chee.archive.mpps.emulate.MPPSEmulatorEJB;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.store.impl.StoreServiceEJB;
import org.dcm4chee.archive.store.session.StudyUpdatedEvent;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MppsEmulationGeneral {

    protected static final String SOURCE_AET = "EMULATE_MPPS";
    protected static final String LOCAL_AET = "DCM4CHEE";
    protected static final String MPPS_IUID = "1.2.40.0.13.1.1.99.20120130";
    protected static final String STUDY_IUID = "1.2.40.0.13.1.1.99.20110607";
    private static final String PID = "STORE_SERVICE_TEST";
    private static final Code[] CODES = {
            new Code("110514", "DCM", null, "Incorrect worklist entry selected"),
            new Code("113039", "DCM", null, "Data Retention Policy Expired"),
            new Code("113001", "DCM", null, "Rejected for Quality Reasons"),
            new Code("113037", "DCM", null,
                    "Rejected for Patient Safety Reasons"),
            new Code("113038", "DCM", null, "Incorrect Modality Worklist Entry"), };
    private static final String[] ISSUERS = { "DCM4CHEE_TESTDATA" };
    protected static final Logger log = LoggerFactory.getLogger(MppsEmulationGeneral.class);
    public static final String DCM4CHEE_ARC = "dcm4chee-arc";
    @Inject
    private StoreService storeService;
    @Inject
    private StoreServiceEJB storeServiceEJB;
    @Inject
    protected MPPSService mppsService;
    @Inject
    protected MPPSEmulator mppsEmulator;
    @Inject
    PatientService patientService;
    @Inject
    protected Device device;
    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;
    @Resource
    UserTransaction utx;

    protected static final String[] INSTANCES = { "testdata/mpps-create.xml",
            "testdata/mpps-set.xml", "testdata/store-ct-1.xml",
            "testdata/store-ct-2.xml" };

    protected static WebArchive createWar(Class clazz) {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");

        war.addClass(clazz);
        war.addClass(ParamFactory.class);
        war.addClass(MppsEmulationGeneral.class);
        war.addClass(TestConfigProducer.class);
        war.addClass(MPPSEmulator.class);
        war.addClass(MPPSEmulatorEJB.class);

        war.addAsManifestResource(new FileAsset(new File("src/test/resources/MANIFEST.MF")), "MANIFEST.MF");
        JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies().resolve()
                .withoutTransitivity().as(JavaArchive.class);
        for (JavaArchive a : archs) {
            a.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            war.addAsLibrary(a);
        }
        for (String resourceName : INSTANCES)
            war.addAsResource(resourceName);

        war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        war.addAsLibraries(archs);
        war.as(ZipExporter.class).exportTo(new File("target/test.war"), true);
        return war;
    }


    protected static ApplicationEntity createConfigAE(
            MPPSCreationRule creationRule) {

        ApplicationEntity ae = new ApplicationEntity(LOCAL_AET);
        ArchiveAEExtension aeExt = new ArchiveAEExtension();
        ae.addAEExtension(aeExt);
        ae.setAssociationAcceptor(true);
        ae.setAssociationInitiator(true);

        Device device = new Device(DCM4CHEE_ARC);
        ArchiveDeviceExtension ext = new ArchiveDeviceExtension();
        ext.setFuzzyAlgorithmClass("org.dcm4che3.soundex.ESoundex");
        ext.setAttributeFilter(Entity.Patient, new AttributeFilter(ParamFactory.PATIENT_ATTRS));
        ext.setAttributeFilter(Entity.Study, new AttributeFilter(ParamFactory.STUDY_ATTRS));
        ext.setAttributeFilter(Entity.Series, new AttributeFilter(ParamFactory.SERIES_ATTRS));
        ext.setAttributeFilter(Entity.Instance, new AttributeFilter(ParamFactory.INSTANCE_ATTRS));

        device.addDeviceExtension(ext);
        device.addApplicationEntity(ae);

        MPPSEmulationAndStudyUpdateRule rule = new MPPSEmulationAndStudyUpdateRule();
        rule.setCommonName("MPPS Emulation Rule 1");
        rule.setEmulationDelay(0);
        rule.setSourceAEs(new HashSet<ApplicationEntity>());
        rule.setCreationRule(creationRule);
        ext.addMppsEmulationRule(rule);

        return ae;
    }

    protected void store(String name) throws Exception {
        StoreParam storeParam = ParamFactory.createStoreParam();
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
        session.setArchiveAEExtension(device.getApplicationEntity(LOCAL_AET)
                .getAEExtension(ArchiveAEExtension.class));

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Attributes dicom = SAXReader.parse(cl.getResource(name).toString());
        StoreContext storeContext = storeService.createStoreContext(session);
        storeContext.setAttributes(dicom);
        openTransaction();
        storeServiceEJB.updateDB(storeContext);
        closeTransaction();
        log.info("STORE:" + dicom.getString(Tag.SOPInstanceUID));
    }

    protected void mpps_create(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Attributes dicom = SAXReader.parse(cl.getResource(name).toString());
        mppsService.createPerformedProcedureStep(
                device.getApplicationEntity(LOCAL_AET).getAEExtension(
                        ArchiveAEExtension.class), MPPS_IUID, dicom, null,
                mppsService);
        log.info("N-CREATE:" + MPPS_IUID);
    }

    protected void mpps_set(String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Attributes dicom = SAXReader.parse(cl.getResource(name).toString());
        mppsService.updatePerformedProcedureStep(
                device.getApplicationEntity(LOCAL_AET).getAEExtension(
                        ArchiveAEExtension.class), MPPS_IUID, dicom,
                mppsService);
        log.info("N-SET:" + MPPS_IUID);
    }

    protected MPPS find(String sopInstanceUID) {
        try {
            return em
                    .createNamedQuery(MPPS.FIND_BY_SOP_INSTANCE_UID, MPPS.class)
                    .setParameter(1, sopInstanceUID).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    protected void deleteCodes() throws Exception {
        openTransaction();
        for (Code c : CODES) {
            long codePK = getcodePK(c);
            if (codePK != -1) {
                em.remove(em.find(Code.class, codePK));
            }
        }
        closeTransaction();
    }

    private long getcodePK(Code c) {
        Query q = em
                .createNamedQuery("Code.findByCodeValueWithoutSchemeVersion");
        q.setParameter(1, c.getCodeValue());
        q.setParameter(2, c.getCodingSchemeDesignator());
        List<Code> result = q.getResultList();
        if (result.isEmpty())
            return -1;
        else
            return result.get(0).getPk();
    }

    protected void deletePatient() throws Exception {
        openTransaction();
        Attributes tmpAttrs = new Attributes();
        tmpAttrs.setString(Tag.PatientID, VR.LO, PID);
        tmpAttrs.setString(Tag.IssuerOfPatientID, VR.LO, "DCM4CHEE_TESTDATA");
        Patient tmpPatient = patientService.updateOrCreatePatientOnCStore(
                tmpAttrs, new IDPatientSelector(),
                ParamFactory.createStoreParam());
        em.remove(tmpPatient);
        tmpAttrs = new Attributes();
        closeTransaction();
    }

    protected void deleteIssuers() throws Exception {
        openTransaction();
        for (String issuer : ISSUERS) {
            long issuerPK = getIssuerPK(issuer);
            if (issuerPK != -1)
                em.remove(em.find(Issuer.class, issuerPK));
        }
        closeTransaction();
    }

    private long getIssuerPK(String issuer) {
        Query q = em.createNamedQuery("Issuer.findByEntityID");
        q.setParameter(1, issuer);
        List<Issuer> result = q.getResultList();
        if (result.isEmpty())
            return -1;
        else
            return result.get(0).getPk();
    }

    private void openTransaction() throws NotSupportedException,
            SystemException {
        utx.begin();
        em.joinTransaction();
    }

    private void closeTransaction() throws SecurityException,
            IllegalStateException, RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SystemException {
        utx.commit();
        em.clear();
    }

    public MppsEmulationGeneral() {
        super();
    }

    protected StudyUpdatedEvent createMockStudyUpdatedEvent() {
        StudyUpdatedEvent studyUpdatedEvent = new StudyUpdatedEvent();
        studyUpdatedEvent.setSourceAET(SOURCE_AET);
        studyUpdatedEvent.setStudyInstanceUID(STUDY_IUID);
        studyUpdatedEvent.getLocalAETs().add(LOCAL_AET);
        HashSet<String> affectedSeriesUIDs = new HashSet<>();
        affectedSeriesUIDs.add("1.2.40.0.13.1.1.99.20110607.1");
        studyUpdatedEvent.setAffectedSeriesUIDs(affectedSeriesUIDs);
        return studyUpdatedEvent;
    }

    protected void setMPPSCreationRule(MPPSCreationRule never) {
        device.getDeviceExtension(ArchiveDeviceExtension.class)
                .getMppsEmulationRule(SOURCE_AET)
                .setCreationRule(never);
    }
}