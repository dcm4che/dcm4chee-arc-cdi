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
 * Portions created by the Initial Developer are Copyright (C) 2014
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

 package org.dcm4chee.archive.conf;

import static org.dcm4che3.net.TransferCapability.Role.SCP;
import static org.dcm4che3.net.TransferCapability.Role.SCU;

import java.security.KeyStore;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.dcm4che3.net.DefaultTransferCapabilities;
import org.dcm4che3.conf.api.AttributeCoercion;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.CompressionRule;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceType;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.ExternalArchiveAEExtension;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4chee.storage.conf.Archiver;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.Container;
import org.dcm4chee.storage.conf.FileCache;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;

public class DeviceMocker {

    protected KeyStore keystore;


    protected static final String PIX_MANAGER = "HL7RCV^DCM4CHEE";
    protected static final String[] OTHER_DEVICES = {
        "dcmqrscp",
        "stgcmtscu",
        "storescp",
        "mppsscp",
        "ianscp",
        "storescu",
        "mppsscu",
        "findscu",
        "getscu",
        "movescu",
        "hl7snd"
    };
    protected static final String[] OTHER_AES = {
        "DCMQRSCP",
        "STGCMTSCU",
        "STORESCP",
        "MPPSSCP",
        "IANSCP",
        "STORESCU",
        "MPPSSCU",
        "FINDSCU",
        "GETSCU"
    };
    protected static final Issuer SITE_A =
            new Issuer("Site A", "1.2.40.0.13.1.1.999.111.1111", "ISO");
    private static final Issuer SITE_B =
            new Issuer("Site B", "1.2.40.0.13.1.1.999.222.2222", "ISO");
    private static final Code INST_B =
            new Code("222.2222", "99DCM4CHEE", null, "Site B");
    protected static final Issuer[] OTHER_ISSUER = {
            SITE_B, // DCMQRSCP
        null, // STGCMTSCU
            SITE_A, // STORESCP
            SITE_A, // MPPSSCP
        null, // IANSCP
            SITE_A, // STORESCU
            SITE_A, // MPPSSCU
            SITE_A, // FINDSCU
            SITE_A, // GETSCU
    };
    protected static final Code INST_A =
            new Code("111.1111", "99DCM4CHEE", null, "Site A");
    protected static final Code[] OTHER_INST_CODES = {
            INST_B, // DCMQRSCP
        null, // STGCMTSCU
        null, // STORESCP
        null, // MPPSSCP
        null, // IANSCP
            INST_A, // STORESCU
        null, // MPPSSCU
        null, // FINDSCU
        null, // GETSCU
    };
    protected static final int[] OTHER_PORTS = {
        11113, 2763, // DCMQRSCP
        11114, 2765, // STGCMTSCU
        11115, 2766, // STORESCP
        11116, 2767, // MPPSSCP
        11117, 2768, // IANSCP
        Connection.NOT_LISTENING, Connection.NOT_LISTENING, // STORESCU
        Connection.NOT_LISTENING, Connection.NOT_LISTENING, // MPPSSCU
        Connection.NOT_LISTENING, Connection.NOT_LISTENING, // FINDSCU
        Connection.NOT_LISTENING, Connection.NOT_LISTENING, // GETSCU
    };
    private static final int PENDING_CMOVE_INTERVAL = 5000;
    private static final int CONFIGURATION_STALE_TIMEOUT = 60;
    private static final int WADO_ATTRIBUTES_STALE_TIMEOUT = 60;
    private static final int QIDO_MAX_NUMBER_OF_RESULTS = 1000;
    private static final int[] PATIENT_ATTRS = {
        Tag.SpecificCharacterSet,
        Tag.PatientName,
        Tag.PatientID,
        Tag.IssuerOfPatientID,
        Tag.IssuerOfPatientIDQualifiersSequence,
        Tag.PatientBirthDate,
        Tag.PatientBirthTime,
        Tag.PatientSex,
        Tag.PatientInsurancePlanCodeSequence,
        Tag.PatientPrimaryLanguageCodeSequence,
        Tag.OtherPatientNames,
        Tag.OtherPatientIDsSequence,
        Tag.PatientBirthName,
        Tag.PatientAge,
        Tag.PatientSize,
        Tag.PatientSizeCodeSequence,
        Tag.PatientWeight,
        Tag.PatientAddress,
        Tag.PatientMotherBirthName,
        Tag.MilitaryRank,
        Tag.BranchOfService,
        Tag.MedicalRecordLocator,
        Tag.MedicalAlerts,
        Tag.Allergies,
        Tag.CountryOfResidence,
        Tag.RegionOfResidence,
        Tag.PatientTelephoneNumbers,
        Tag.EthnicGroup,
        Tag.Occupation,
        Tag.SmokingStatus,
        Tag.AdditionalPatientHistory,
        Tag.PregnancyStatus,
        Tag.LastMenstrualDate,
        Tag.PatientReligiousPreference,
        Tag.PatientSpeciesDescription,
        Tag.PatientSpeciesCodeSequence,
        Tag.PatientSexNeutered,
        Tag.PatientBreedDescription,
        Tag.PatientBreedCodeSequence,
        Tag.BreedRegistrationSequence,
        Tag.ResponsiblePerson,
        Tag.ResponsiblePersonRole,
        Tag.ResponsibleOrganization,
        Tag.PatientComments,
        Tag.ClinicalTrialSponsorName,
        Tag.ClinicalTrialProtocolID,
        Tag.ClinicalTrialProtocolName,
        Tag.ClinicalTrialSiteID,
        Tag.ClinicalTrialSiteName,
        Tag.ClinicalTrialSubjectID,
        Tag.ClinicalTrialSubjectReadingID,
        Tag.PatientIdentityRemoved,
        Tag.DeidentificationMethod,
        Tag.DeidentificationMethodCodeSequence,
        Tag.ClinicalTrialProtocolEthicsCommitteeName,
        Tag.ClinicalTrialProtocolEthicsCommitteeApprovalNumber,
        Tag.SpecialNeeds,
        Tag.PertinentDocumentsSequence,
        Tag.PatientState,
        Tag.PatientClinicalTrialParticipationSequence,
        Tag.ConfidentialityConstraintOnPatientDataDescription
    };
    private static final int[] STUDY_ATTRS = {
        Tag.SpecificCharacterSet,
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.IssuerOfAccessionNumberSequence,
        Tag.ReferringPhysicianName,
        Tag.StudyDescription,
        Tag.ProcedureCodeSequence,
        Tag.PatientAge,
        Tag.PatientSize,
        Tag.PatientSizeCodeSequence,
        Tag.PatientWeight,
        Tag.Occupation,
        Tag.AdditionalPatientHistory,
        Tag.PatientSexNeutered,
        Tag.StudyInstanceUID,
        Tag.StudyID
    };
    private static final Map<Integer, String> STUDY_PRIVATE_ATTRS = new TreeMap<>();
    static {
        STUDY_PRIVATE_ATTRS.put(ExtendedStudyDictionary.StudyLastUpdateDateTime, "EXTENDED STUDY");
        STUDY_PRIVATE_ATTRS.put(ExtendedStudyDictionary.StudyStatus, "EXTENDED STUDY");
    }
    
    private static final int[] SERIES_ATTRS = {
        Tag.SpecificCharacterSet,
        Tag.Modality,
        Tag.Manufacturer,
        Tag.InstitutionName,
        Tag.InstitutionCodeSequence,
        Tag.StationName,
        Tag.SeriesDescription,
        Tag.InstitutionalDepartmentName,
        Tag.PerformingPhysicianName,
        Tag.ManufacturerModelName,
        Tag.ReferencedPerformedProcedureStepSequence,
        Tag.BodyPartExamined,
        Tag.SeriesInstanceUID,
        Tag.SeriesNumber,
        Tag.Laterality,
        Tag.PerformedProcedureStepStartDate,
        Tag.PerformedProcedureStepStartTime,
        Tag.RequestAttributesSequence
    };
    private static final int[] INSTANCE_ATTRS = {
        Tag.SpecificCharacterSet,
        Tag.ImageType,
        Tag.SOPClassUID,
        Tag.SOPInstanceUID,
        Tag.ContentDate,
        Tag.ContentTime,
        Tag.ReferencedSeriesSequence,
        Tag.InstanceNumber,
        Tag.NumberOfFrames,
        Tag.Rows,
        Tag.Columns,
        Tag.BitsAllocated,
        Tag.ObservationDateTime,
        Tag.ConceptNameCodeSequence,
        Tag.VerifyingObserverSequence,
        Tag.ReferencedRequestSequence,
        Tag.CompletionFlag,
        Tag.VerificationFlag,
        Tag.DocumentTitle,
        Tag.MIMETypeOfEncapsulatedDocument,
        Tag.ContentLabel,
        Tag.ContentDescription,
        Tag.PresentationCreationDate,
        Tag.PresentationCreationTime,
        Tag.ContentCreatorName,
        Tag.OriginalAttributesSequence,
        Tag.IdenticalDocumentsSequence,
        Tag.CurrentRequestedProcedureEvidenceSequence
    };
    private static final String ATTRIBUTE_COERCION_ENSURE_PID_XSL =
            "${jboss.server.config.url}/dcm4chee-arc/ensure-pid.xsl";
    private static final String ATTRIBUTE_COERCION_NULLIFY_PN_XSL =
            "${jboss.server.config.url}/dcm4chee-arc/nullify-pn.xsl";
    private static final String  ATTRIBUTE_COERCION_ENMSURE_AET_XSL =
            "${jboss.server.config.url}/dcm4chee-arc/ensure-retrieve-ae-title.xsl";
    private static final String WADO_SR_TEMPLATE_URI =
            "${jboss.server.config.url}/dcm4chee-arc/sr-report-html-dicom-native.xsl";
    private static final String DCM4CHEE_ARC_KEY_JKS =
            "${jboss.server.config.url}/dcm4chee-arc/key.jks";
    private static final String HL7_ADT2DCM_XSL =
            "${jboss.server.config.url}/dcm4chee-arc/hl7-adt2dcm.xsl";
    private static final Code INCORRECT_WORKLIST_ENTRY_SELECTED =
            new Code("110514", "DCM", null, "Incorrect worklist entry selected");
    private static final Code REJECTED_FOR_QUALITY_REASONS =
            new Code("113001", "DCM", null, "Rejected for Quality Reasons");
    private static final Code REJECT_FOR_PATIENT_SAFETY_REASONS =
            new Code("113037", "DCM", null, "Rejected for Patient Safety Reasons");
    private static final Code INCORRECT_MODALITY_WORKLIST_ENTRY =
            new Code("113038", "DCM", null, "Incorrect Modality Worklist Entry");
    private static final Code DATA_RETENTION_POLICY_EXPIRED =
            new Code("113039", "DCM", null, "Data Retention Policy Expired");
    private static final Code REVOKE_REJECTION =
            new Code("REVOKE_REJECTION", "99DCM4CHEE", null, "Restore rejected Instances");
    private static final Code[] REJECTION_CODES = {
            INCORRECT_WORKLIST_ENTRY_SELECTED,
            REJECTED_FOR_QUALITY_REASONS,
            REJECT_FOR_PATIENT_SAFETY_REASONS,
            INCORRECT_MODALITY_WORKLIST_ENTRY,
            DATA_RETENTION_POLICY_EXPIRED
    };
    private static final QueryRetrieveView REGULAR_USE_VIEW =
            createQueryRetrieveView("regularUse",
                    new Code[]{REJECTED_FOR_QUALITY_REASONS},
                    new Code[]{DATA_RETENTION_POLICY_EXPIRED},
                    false);
    private static final QueryRetrieveView HIDE_REJECTED_VIEW =
            createQueryRetrieveView("hideRejected",
                    new Code[0],
                    new Code[]{DATA_RETENTION_POLICY_EXPIRED},
                    false);
    private static final QueryRetrieveView TRASH_VIEW =
            createQueryRetrieveView("trashView",
                    REJECTION_CODES,
                    new Code[0],
                    true);
    private static final String[] HL7_MESSAGE_TYPES = {
        "ADT^A02",
        "ADT^A03",
        "ADT^A06",
        "ADT^A07",
        "ADT^A08",
        "ADT^A40",
        "ORM^O01"
    };
    private static final int MPPS_EMULATOR_POLL_INTERVAL = 60;
    private static final int ARCHIVING_POLL_INTERVAL = 60;
    private static final int SYNC_LOCATION_STATUS_POLL_INTERVAL = 3600;

    private static QueryRetrieveView[] QUERY_RETRIEVE_VIEWS = {
            HIDE_REJECTED_VIEW,
            REGULAR_USE_VIEW,
            TRASH_VIEW};
    private final String[] WADO_SUPPORTED_SR_SOP_CLASSES = {
        UID.BasicTextSRStorage,
        UID.EnhancedSRStorage,
        UID.ComprehensiveSRStorage,
        UID.Comprehensive3DSRStorage,
        UID.ProcedureLogStorage,
        UID.MammographyCADSRStorage,
        UID.KeyObjectSelectionDocumentStorage,
        UID.ChestCADSRStorage,
        UID.XRayRadiationDoseSRStorage,
        UID.RadiopharmaceuticalRadiationDoseSRStorage,
        UID.ColonCADSRStorage,
        UID.ImplantationPlanSRStorage
    };
    protected DicomConfiguration config;
    protected HL7Configuration hl7Config;

    private static QueryRetrieveView createQueryRetrieveView(String viewID,
            Code[] showInstancesRejectedByCodes, Code[] hideRejectionNoteCodes,
            boolean hideNotRejectedInstances) {
        QueryRetrieveView view = new QueryRetrieveView();
        view.setViewID(viewID);
        view.setShowInstancesRejectedByCodes(showInstancesRejectedByCodes);
        view.setHideRejectionNotesWithCodes(hideRejectionNoteCodes);
        view.setHideNotRejectedInstances(hideNotRejectedInstances);
        return view;
    }

    private static MPPSEmulationRule createMPPSEmulationRule(
            String commonName,
            int emulationDelay,
            String emulatorAET,
            String... sourceAETs) {
        MPPSEmulationRule rule = new MPPSEmulationRule();
        rule.setCommonName(commonName);
        rule.setEmulationDelay(emulationDelay);
        rule.setEmulatorAET(emulatorAET);
        rule.setSourceAETs(sourceAETs);
        rule.setCreationRule(MPPSCreationRule.ALWAYS);
        return rule ;
    }

    protected Device createARRDevice(String name, Connection.Protocol protocol, int port) {
        Device arrDevice = new Device(name);
        AuditRecordRepository arr = new AuditRecordRepository();
        arrDevice.addDeviceExtension(arr);
        Connection auditUDP = new Connection("audit-udp", "localhost", port);
        auditUDP.setProtocol(protocol);
        arrDevice.addConnection(auditUDP);
        arr.addConnection(auditUDP);
        return arrDevice ;
    }

    protected Device createDevice(String name) throws Exception {
        return init(new Device(name), null, null);
    }

    private Device init(Device device, Issuer issuer, Code institutionCode)
            throws Exception {
        String name = device.getDeviceName();

        //TODO: implement
//        device.setThisNodeCertificates(config.deviceRef(name),
//                (X509Certificate) keystore.getCertificate(name));

        device.setIssuerOfPatientID(issuer);
        device.setIssuerOfAccessionNumber(issuer);
        if (institutionCode != null) {
            device.setInstitutionNames(institutionCode.getCodeMeaning());
            device.setInstitutionCodes(institutionCode);
        }
        return device;
    }

    protected Device createDevice(String name,
                                  Issuer issuer, Code institutionCode, String aet,
                                  String host, int port, int tlsPort) throws Exception {
         Device device = init(new Device(name), issuer, institutionCode);
         if(name.equalsIgnoreCase(OTHER_DEVICES[0])
                 || name.equalsIgnoreCase("dcm4chee-arc"))
             device.setPrimaryDeviceTypes(new String[]{DeviceType.ARCHIVE.toString()});
         ApplicationEntity ae = new ApplicationEntity(aet);
         ExternalArchiveAEExtension externalArchiveExt = 
                 new ExternalArchiveAEExtension();
         if(containsArchiveType(device.getPrimaryDeviceTypes()))
         ae.addAEExtension(externalArchiveExt);
         externalArchiveExt.setAeFetchPriority(0);
         externalArchiveExt.setPrefersForwarding(false);
         ae.setAssociationAcceptor(true);
         device.addApplicationEntity(ae);
         Connection dicom = new Connection("dicom", host, port);
         device.addConnection(dicom);
         ae.addConnection(dicom);
         Connection dicomTLS = new Connection("dicom-tls", host, tlsPort);
         dicomTLS.setTlsCipherSuites(
                 Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
                 Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
         device.addConnection(dicomTLS);
         ae.addConnection(dicomTLS);
         return device;
    }

    private boolean containsArchiveType(String[] primaryDeviceTypes) {
        for(String str : primaryDeviceTypes)
            if(str == DeviceType.ARCHIVE.toString())
                return true;
        return false;
    }

    protected Device createHL7Device(String name,
                                     Issuer issuer, Code institutionCode, String appName,
                                     String host, int port, int tlsPort) throws Exception {
         Device device = new Device(name);
         HL7DeviceExtension hl7Device = new HL7DeviceExtension();
         device.addDeviceExtension(hl7Device);
         init(device, issuer, institutionCode);
         HL7Application hl7app = new HL7Application(appName);
         hl7Device.addHL7Application(hl7app);
         Connection hl7 = new Connection("hl7", host, port);
         hl7.setProtocol(Connection.Protocol.HL7);
         device.addConnection(hl7);
         hl7app.addConnection(hl7);
         Connection hl7TLS = new Connection("hl7-tls", host, tlsPort);
         hl7TLS.setProtocol(Connection.Protocol.HL7);
         hl7TLS.setTlsCipherSuites(
                 Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
                 Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
         device.addConnection(hl7TLS);
         hl7app.addConnection(hl7TLS);
         return device;
     }

    protected Device createArchiveDevice(String name, Device arrDevice)
            throws Exception {
        Device device = new Device(name);
        device.setPrimaryDeviceTypes(new String[] { DeviceType.ARCHIVE.toString()});
        Connection dicom = new Connection("dicom", "localhost", 11112);
        dicom.setBindAddress("0.0.0.0");
        dicom.setMaxOpsInvoked(0);
        dicom.setMaxOpsPerformed(0);
        device.addConnection(dicom);

        Connection dicomTLS = new Connection("dicom-tls", "localhost", 2762);
        dicomTLS.setBindAddress("0.0.0.0");
        dicomTLS.setMaxOpsInvoked(0);
        dicomTLS.setMaxOpsPerformed(0);
        dicomTLS.setTlsCipherSuites(
                Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
                Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
        device.addConnection(dicomTLS);

        addArchiveDeviceExtension(device);
        addHL7DeviceExtension(device);
        addAuditLogger(device, arrDevice);
        addStorageDeviceExtension(device);
        device.addDeviceExtension(new ImageReaderExtension(ImageReaderFactory.getDefault()));
        device.addDeviceExtension(new ImageWriterExtension(ImageWriterFactory.getDefault()));

        device.setManufacturer("dcm4che.org");
        device.setManufacturerModelName("dcm4chee-arc");
        device.setSoftwareVersions("4.2.0.Alpha3");
        device.setKeyStoreURL(DCM4CHEE_ARC_KEY_JKS);
        device.setKeyStoreType("JKS");
        device.setKeyStorePin("secret");

//        device.setThisNodeCertificates(config.deviceRef(name),
//                (X509Certificate) keystore.getCertificate(name));
//        for (String other : OTHER_DEVICES)
//            device.setAuthorizedNodeCertificates(config.deviceRef(other),
//                    (X509Certificate) keystore.getCertificate(other));

        device.addApplicationEntity(createAE("DCM4CHEE", dicom, dicomTLS,
                    DefaultTransferCapabilities.IMAGE_TSUIDS, DefaultTransferCapabilities.VIDEO_TSUIDS, DefaultTransferCapabilities.OTHER_TSUIDS,
                    HIDE_REJECTED_VIEW, null, PIX_MANAGER));
        device.addApplicationEntity(
                createQRAE("DCM4CHEE_ADMIN", dicom, dicomTLS,
                    DefaultTransferCapabilities.IMAGE_TSUIDS, DefaultTransferCapabilities.VIDEO_TSUIDS, DefaultTransferCapabilities.OTHER_TSUIDS,
                    REGULAR_USE_VIEW, null, PIX_MANAGER));
        device.addApplicationEntity(createAE("DCM4CHEE_FETCH", dicom, dicomTLS,
                DefaultTransferCapabilities.IMAGE_TSUIDS, DefaultTransferCapabilities.VIDEO_TSUIDS, DefaultTransferCapabilities.OTHER_TSUIDS,
                HIDE_REJECTED_VIEW, null, PIX_MANAGER));
        device.addApplicationEntity(
                createQRAE("DCM4CHEE_TRASH", dicom, dicomTLS,
                    DefaultTransferCapabilities.IMAGE_TSUIDS, DefaultTransferCapabilities.VIDEO_TSUIDS, DefaultTransferCapabilities.OTHER_TSUIDS,
                    TRASH_VIEW, null, PIX_MANAGER));

        return device ;
    }

    private void addStorageDeviceExtension(Device device) {
        StorageSystem fs1 = new StorageSystem();
        fs1.setStorageSystemID("fs1");
        fs1.setProviderName("org.dcm4chee.storage.filesystem");
        fs1.setStorageSystemPath("/var/local/dcm4chee-arc/fs1");
        fs1.setAvailability(Availability.ONLINE);

        StorageSystem arc = new StorageSystem();
        arc.setStorageSystemID("nearline");
        arc.setProviderName("org.dcm4chee.storage.filesystem");
        arc.setStorageSystemPath("/var/local/dcm4chee-arc/nearline");
        arc.setAvailability(Availability.NEARLINE);
        Map<String,String> exts = new LinkedHashMap<String, String>();
        exts.put(".archived", "ARCHIVED");
        arc.setStatusFileExtensions(exts);

        StorageSystem metadata = new StorageSystem();
        metadata.setStorageSystemID("metadata");
        metadata.setProviderName("org.dcm4chee.storage.filesystem");
        metadata.setStorageSystemPath("/var/local/dcm4chee-arc/metadata");
        metadata.setAvailability(Availability.ONLINE);

        Container container = new Container();
        container.setProviderName("org.dcm4chee.storage.zip");

        FileCache fileCache = new FileCache();
        fileCache.setProviderName("org.dcm4chee.storage.filecache");
        fileCache.setFileCacheRootDirectory(
                "/var/local/dcm4chee-arc/nearline-cache/data");
        fileCache.setJournalRootDirectory(
                "/var/local/dcm4chee-arc/nearline-cache");
        fileCache.setMinFreeSpace("100MiB");
        fileCache.setCacheAlgorithm(FileCache.Algorithm.LRU);

        StorageSystemGroup online = new StorageSystemGroup();
        online.setGroupID("DEFAULT");
        online.setRetrieveAETs(new String[] {"DCM4CHEE"});
        online.setDigestAlgorithm("MD5");
        online.addStorageSystem(fs1);
        online.setBaseStorageAccessTime(1000);
        online.setStorageFilePathFormat("{now,date,yyyy/MM/dd}/{0020000D,hash}/{0020000E,hash}/{00080018,hash}");
        online.setActiveStorageSystemIDs(fs1.getStorageSystemID());

        StorageSystemGroup nearline = new StorageSystemGroup();
        nearline.setRetrieveAETs(new String[] {"DCM4CHEE"});
        nearline.setGroupID("ARCHIVE");
        nearline.addStorageSystem(arc);
        nearline.setBaseStorageAccessTime(2000);
        nearline.setStorageFilePathFormat("{now,date,yyyy/MM/dd}/{0020000D,hash}/{0020000E,hash}/{now,date,HHmmssSSS}");
        nearline.setActiveStorageSystemIDs(arc.getStorageSystemID());
        nearline.setContainer(container);
        nearline.setFileCache(fileCache);

        StorageSystemGroup metadataG = new StorageSystemGroup();
        metadataG.setGroupID("METADATA");
        metadataG.addStorageSystem(metadata);
        metadataG.setBaseStorageAccessTime(0);
        metadataG.setStorageFilePathFormat("{now,date,yyyy/MM/dd}/{0020000D,hash}/{0020000E,hash}/{00080018,hash}");
        metadataG.setActiveStorageSystemIDs(metadata.getStorageSystemID());

        Archiver archiver = new Archiver();
        archiver.setVerifyContainer(true);
        archiver.setObjectStatus("TO_ARCHIVE");

        StorageDeviceExtension ext = new StorageDeviceExtension();
        ext.setArchiver(archiver);
        ext.addStorageSystemGroup(online);
        ext.addStorageSystemGroup(nearline);
        ext.addStorageSystemGroup(metadataG);
        device.addDeviceExtension(ext);
    }

    private void addAuditLogger(Device device, Device arrDevice) {
        Connection auditUDP = new Connection("audit-udp", "localhost");
        auditUDP.setProtocol(Connection.Protocol.SYSLOG_UDP);
        device.addConnection(auditUDP);

        AuditLogger auditLogger = new AuditLogger();
        device.addDeviceExtension(auditLogger);
        auditLogger.addConnection(auditUDP);
        auditLogger.setAuditSourceTypeCodes("4");
        auditLogger.setAuditRecordRepositoryDevice(arrDevice);
    }

    private void addHL7DeviceExtension(Device device) {
        HL7DeviceExtension ext = new HL7DeviceExtension();
        device.addDeviceExtension(ext);

        Connection hl7 = new Connection("hl7", "localhost", 2575);
        hl7.setBindAddress("0.0.0.0");
        hl7.setProtocol(Connection.Protocol.HL7);
        device.addConnection(hl7);

        Connection hl7TLS = new Connection("hl7-tls", "localhost", 12575);
        hl7TLS.setBindAddress("0.0.0.0");
        hl7TLS.setProtocol(Connection.Protocol.HL7);
        hl7TLS.setTlsCipherSuites(
                Connection.TLS_RSA_WITH_AES_128_CBC_SHA,
                Connection.TLS_RSA_WITH_3DES_EDE_CBC_SHA);
        device.addConnection(hl7TLS);

        HL7Application hl7App = new HL7Application("*");
        ArchiveHL7ApplicationExtension hl7AppExt = new ArchiveHL7ApplicationExtension();
        hl7App.addHL7ApplicationExtension(hl7AppExt);
        hl7App.setAcceptedMessageTypes(HL7_MESSAGE_TYPES);
        hl7App.setHL7DefaultCharacterSet("8859/1");
        hl7AppExt.addTemplatesURI("adt2dcm", HL7_ADT2DCM_XSL);
        ext.addHL7Application(hl7App);
        hl7App.addConnection(hl7);
        hl7App.addConnection(hl7TLS);
    }

    private void addArchiveDeviceExtension(Device device) {
        ArchiveDeviceExtension ext = new ArchiveDeviceExtension();
        device.addDeviceExtension(ext);
        ext.setIncorrectWorklistEntrySelectedCode(INCORRECT_WORKLIST_ENTRY_SELECTED);
        ext.setFuzzyAlgorithmClass("org.dcm4che3.soundex.ESoundex");
        ext.setConfigurationStaleTimeout(CONFIGURATION_STALE_TIMEOUT);
        ext.setWadoAttributesStaleTimeout(WADO_ATTRIBUTES_STALE_TIMEOUT);
        ext.setRejectionParams(createRejectionNotes());
        ext.setQueryRetrieveViews(QUERY_RETRIEVE_VIEWS);
        ext.setMppsEmulationPollInterval(MPPS_EMULATOR_POLL_INTERVAL);
        ext.setArchivingSchedulerPollInterval(ARCHIVING_POLL_INTERVAL);
        ext.setSyncLocationStatusPollInterval(SYNC_LOCATION_STATUS_POLL_INTERVAL);
        ext.setSyncLocationStatusStorageSystemGroupIDs("ARCHIVE");
        ext.setAttributeFilter(Entity.Patient,
                new AttributeFilter(PATIENT_ATTRS));
        ext.setAttributeFilter(Entity.Study,new AttributeFilter(STUDY_ATTRS, STUDY_PRIVATE_ATTRS));
        ext.setAttributeFilter(Entity.Series,
                new AttributeFilter(SERIES_ATTRS));
        ext.setAttributeFilter(Entity.Instance,
                new AttributeFilter(INSTANCE_ATTRS));
        ext.setFetchAETitle("DCM4CHEE_FETCH");
    }

    private RejectionParam[] createRejectionNotes() {
        return new RejectionParam[] {
            createRejectionParam(REJECTED_FOR_QUALITY_REASONS, false,
                    StoreAction.IGNORE),
            createRejectionParam(REJECT_FOR_PATIENT_SAFETY_REASONS, false,
                    null, REJECTED_FOR_QUALITY_REASONS),
            createRejectionParam(INCORRECT_WORKLIST_ENTRY_SELECTED, false,
                    null, REJECTED_FOR_QUALITY_REASONS),
            createRejectionParam(INCORRECT_MODALITY_WORKLIST_ENTRY, false,
                    null, REJECTED_FOR_QUALITY_REASONS),
            createRejectionParam(DATA_RETENTION_POLICY_EXPIRED, false,
                    StoreAction.REPLACE, REJECTED_FOR_QUALITY_REASONS),
            createRejectionParam(REVOKE_REJECTION, true, null,
                    REJECTED_FOR_QUALITY_REASONS,
                    REJECT_FOR_PATIENT_SAFETY_REASONS,
                    INCORRECT_WORKLIST_ENTRY_SELECTED,
                    INCORRECT_MODALITY_WORKLIST_ENTRY,
                    DATA_RETENTION_POLICY_EXPIRED)
        };
    }

    private RejectionParam createRejectionParam(Code title,
            boolean revokeRejection, StoreAction storeAction,
            Code... overwritePreviousRejection) {
        RejectionParam param = new RejectionParam();
        param.setRejectionNoteTitle(title);
        param.setRevokeRejection(revokeRejection);
        param.setAcceptPreviousRejectedInstance(storeAction);
        param.setOverwritePreviousRejection(overwritePreviousRejection);
        param.setRetentionTime(-1);
        param.setRetentionTimeUnit(TimeUnit.DAYS);
        return param;
    }

    private ApplicationEntity createAE(String aet,
            Connection dicom, Connection dicomTLS, 
            String[] image_tsuids, String[] video_tsuids, String[] other_tsuids,
            QueryRetrieveView queryRetrieveView,
            String pixConsumer, String pixManager) {
        ApplicationEntity ae = new ApplicationEntity(aet);
        ae.addConnection(dicom);
        ae.addConnection(dicomTLS);

        ArchiveAEExtension aeExt = new ArchiveAEExtension();
        ae.addAEExtension(aeExt);
        ae.setAssociationAcceptor(true);
        ae.setAssociationInitiator(true);
        aeExt.setStorageSystemGroupID("DEFAULT");
        aeExt.setMetaDataStorageSystemGroupID("METADATA");
        aeExt.setSpoolDirectoryPath("spool");
        aeExt.setPreserveSpoolFileOnFailure(true);
        aeExt.setSuppressWarningCoercionOfDataElements(false);
        aeExt.setCheckNonDBAttributesOnStorage(false);
        aeExt.setIgnoreDuplicatesOnStorage(true);

        aeExt.setMatchUnknown(true);
        aeExt.setSendPendingCGet(true);
        aeExt.setSendPendingCMoveInterval(PENDING_CMOVE_INTERVAL);
        aeExt.setQueryRetrieveViewID(queryRetrieveView.getViewID());
        aeExt.setWadoSRTemplateURI(WADO_SR_TEMPLATE_URI);
        aeExt.setWadoSupportedSRClasses(WADO_SUPPORTED_SR_SOP_CLASSES);
        aeExt.setQIDOMaxNumberOfResults(QIDO_MAX_NUMBER_OF_RESULTS);
        aeExt.setQidoClientAcceptType("application/json");
        aeExt.setDefaultExternalRetrieveAETAvailability(Availability.ONLINE);
        aeExt.addAttributeCoercion(new AttributeCoercion(
                "Supplement missing PID",
                null,
                Dimse.C_STORE_RQ,
                SCP,
                new String[]{"ENSURE_PID"},
                null,                           // Source Device Names
                ATTRIBUTE_COERCION_ENSURE_PID_XSL));
        aeExt.addAttributeCoercion(new AttributeCoercion(
                "Remove person names",
                null,
                Dimse.C_STORE_RQ,
                SCU,
                new String[]{"WITHOUT_PN"},
                null,                           // Source Device Names
                ATTRIBUTE_COERCION_NULLIFY_PN_XSL));
        aeExt.addAttributeCoercion(new AttributeCoercion(
                "Ensure Retrieve AETitle",
                null,
                Dimse.C_FIND_RSP,
                SCP,
                new String[]{"WITHOUT_PN"},
                null,                           // Source Device Names
                ATTRIBUTE_COERCION_ENMSURE_AET_XSL));
        aeExt.addCompressionRule(new CompressionRule(
                "JPEG 8-bit Lossy",
                new String[]{
                        "MONOCHROME1",
                        "MONOCHROME2",
                        "RGB"},
                new int[]{8},                // Bits Stored
                0,                              // Pixel Representation
                new String[]{"JPEG_LOSSY"},  // Source AETs
                null,                           // Source Device Names
                null,                           // SOP Classes
                null,                           // Image Types
                null,                           // Body Parts
                UID.JPEGBaseline1,
                "compressionQuality=0.8",
                "maxPixelValueError=10",
                "avgPixelValueBlockSize=8"
        ));
        aeExt.addCompressionRule(new CompressionRule(
                "JPEG 12-bit Lossy",
                new String[] {
                    "MONOCHROME1",
                    "MONOCHROME2", },
                new int[] { 9, 10, 11, 12 },    // Bits Stored
                0,                              // Pixel Representation
                new String[] { "JPEG_LOSSY" },  // Source AETs
                null,                           // Source Device Names
                null,                           // SOP Classes
                null,                           // Image Types
                null,                           // Body Parts
                UID.JPEGExtended24,
                "compressionQuality=0.8",
                "maxPixelValueError=20",
                "avgPixelValueBlockSize=8"
                ));
        aeExt.addCompressionRule(new CompressionRule(
                "JPEG Lossless",
                new String[] {
                    "MONOCHROME1",
                    "MONOCHROME2",
                    "PALETTE COLOR",
                    "RGB",
                    "YBR_FULL" },
                new int[] { 8, 9, 10, 11, 12, 13, 14, 15, 16 },    // Bits Stored
                -1,                              // Pixel Representation
                new String[] { "JPEG_LOSSLESS" },  // Source AETs
                null,                           // Source Device Names
                null,                           // SOP Classes
                null,                           // Image Types
                null,                           // Body Parts
                UID.JPEGLossless,
                "maxPixelValueError=0"
                ));
        aeExt.addCompressionRule(new CompressionRule(
                "JPEG LS Lossless",
                new String[] {
                    "MONOCHROME1",
                    "MONOCHROME2",
                    "PALETTE COLOR",
                    "RGB",
                    "YBR_FULL" },
                new int[] { 8, 9, 10, 11, 12, 13, 14, 15, 16 },    // Bits Stored
                -1,                             // Pixel Representation
                new String[] { "JPEG_LS" },     // Source AETs
                null,                           // Source Device Names
                null,                           // SOP Classes
                null,                           // Image Types
                null,                           // Body Parts
                UID.JPEGLSLossless,
                "maxPixelValueError=0"
                ));
        aeExt.addCompressionRule(new CompressionRule(
                "JPEG 2000 Lossless",
                new String[] {
                    "MONOCHROME1",
                    "MONOCHROME2",
                    "PALETTE COLOR",
                    "RGB",
                    "YBR_FULL" },
                new int[] { 8, 9, 10, 11, 12, 13, 14, 15, 16 },  // Bits Stored
                -1,                             // Pixel Representation
                new String[] { "JPEG_2000" },   // Source AETs
                null,                           // Source Device Names
                null,                           // SOP Classes
                null,                           // Image Types
                null,                           // Body Parts
                UID.JPEG2000LosslessOnly,
                "maxPixelValueError=0"
                ));

        ArchivingRule archivingRule = new ArchivingRule();
        archivingRule.setCommonName("Archiving Rule");
        archivingRule.setStorageSystemGroupIDs("ARCHIVE");
        archivingRule.setDelayAfterInstanceStored(60);
        archivingRule.setAeTitles(new String[] { "ARCHIVE" });
        aeExt.addArchivingRule(archivingRule);
                
        DefaultTransferCapabilities.addTCs(ae, null, SCP, DefaultTransferCapabilities.IMAGE_CUIDS, image_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCP, DefaultTransferCapabilities.VIDEO_CUIDS, video_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCP, DefaultTransferCapabilities.OTHER_CUIDS, other_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCU, DefaultTransferCapabilities.IMAGE_CUIDS, image_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCU, DefaultTransferCapabilities.VIDEO_CUIDS, video_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCU, DefaultTransferCapabilities.OTHER_CUIDS, other_tsuids);
        DefaultTransferCapabilities.addTCs(ae, EnumSet.allOf(QueryOption.class), SCP, DefaultTransferCapabilities.QUERY_CUIDS, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTCs(ae, EnumSet.of(QueryOption.RELATIONAL), SCP, DefaultTransferCapabilities.RETRIEVE_CUIDS, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.CompositeInstanceRetrieveWithoutBulkDataGET, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.StorageCommitmentPushModelSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.ModalityPerformedProcedureStepSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCU, UID.ModalityPerformedProcedureStepSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCU, UID.InstanceAvailabilityNotificationSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCU, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian);

        aeExt.setReturnOtherPatientIDs(true);
        aeExt.setReturnOtherPatientNames(true);
        aeExt.setLocalPIXConsumerApplication(pixConsumer);
        aeExt.setRemotePIXManagerApplication(pixManager);

        // patient selector
        PatientSelectorConfig ps = new PatientSelectorConfig();
        ps.setPatientSelectorClassName("org.dcm4chee.archive.patient.DemographicsPatientSelector");
        Map<String,String> sels = new TreeMap<>();
        sels.put("familyName", "BROAD");
        sels.put("givenName", "BROAD");
        ps.setPatientSelectorProperties(sels);
        aeExt.setPatientSelectorConfig(ps);
        aeExt.addMppsEmulationRule(
                createMPPSEmulationRule("MPPS Emulation Rule 1", 120,
                        "DCM4CHEE", "EMULATE_MPPS"));
        ExternalArchiveAEExtension extArcAEExt = new ExternalArchiveAEExtension();
        ae.addAEExtension(extArcAEExt);
        extArcAEExt.setAeFetchPriority(0);
        WebServiceAEExtension wsAEExt = new WebServiceAEExtension();
        ae.addAEExtension(wsAEExt);
        wsAEExt.setQidoRSBaseURL("http://localhost:8080/dcm4chee-arc/qido/"+aet);
        wsAEExt.setWadoRSBaseURL("http://localhost:8080/dcm4chee-arc/wado/"+aet);
        wsAEExt.setStowRSBaseURL("http://localhost:8080/dcm4chee-arc/stow/"+aet);
        wsAEExt.setWadoURIBaseURL("http://localhost:8080/dcm4chee-arc/wado/"+aet);
        return ae;
    }

    protected ApplicationEntity createAnotherAE(String aet,
                                                String[] image_tsuids, String[] video_tsuids, String[] other_tsuids,
                                                String pixConsumer, String pixManager) {
        ApplicationEntity ae = new ApplicationEntity(aet);
        ArchiveAEExtension aeExt = new ArchiveAEExtension();
        ae.addAEExtension(aeExt);
        ae.setAssociationAcceptor(true);
        ae.setAssociationInitiator(true);
        aeExt.setStorageSystemGroupID("notDEFAULT");
        aeExt.setSpoolDirectoryPath("archive/anotherspool");
        aeExt.setPreserveSpoolFileOnFailure(true);
        aeExt.setSuppressWarningCoercionOfDataElements(false);
        aeExt.setMatchUnknown(true);
        aeExt.setSendPendingCGet(true);
        aeExt.setSendPendingCMoveInterval(4000);
        aeExt.setQIDOMaxNumberOfResults(500);
        aeExt.addAttributeCoercion(new AttributeCoercion(
                "Supplement missing PID",
                null,
                Dimse.C_ECHO_RQ,
                SCU,
                new String[]{"ENSURE_PID"},
                null,                           // Source Device Names
                ATTRIBUTE_COERCION_ENSURE_PID_XSL));
        aeExt.addAttributeCoercion(new AttributeCoercion(
                "Remove person names",
                null,
                Dimse.C_STORE_RQ,
                SCP,
                new String[]{"WITHOUT_PN"},
                null,                           // Source Device Names
                ATTRIBUTE_COERCION_NULLIFY_PN_XSL));
        aeExt.addCompressionRule(new CompressionRule(
                "JPEG Lossless",
                new String[] {
                    "MONOCHROME1",
                    "MONOCHROME2",
                    "PALETTE COLOR",
                    "RGB",
                    "YBR_FULL" },
                new int[] { 8, 9, 10, 11, 12, 13, 14, 15, 16 },    // Bits Stored
                -1,                              // Pixel Representation
                new String[] { "JPEG_LOSSLESS" },  // Source AETs
                null,                           // Source Device Names
                null,                           // SOP Classes
                null,                           // Image Types
                null,                           // Body Parts
                UID.JPEGLossless,
                "maxPixelValueError=0"
                ));
        aeExt.addCompressionRule(new CompressionRule(
                "JPEG LS Lossless",
                new String[] {
                    "MONOCHROME1",
                    "MONOCHROME2",
                    "PALETTE COLOR",
                    "RGB",
                    "YBR_FULL" },
                new int[] { 8, 9, 10, 11, 12, 13, 14, 15, 16 },    // Bits Stored
                -1,                             // Pixel Representation
                new String[] { "JPEG_LS" },     // Source AETs
                null,                           // Source Device Names
                null,                           // SOP Classes
                null,                           // Image Types
                null,                           // Body Parts
                UID.JPEGLSLossless,
                "maxPixelValueError=0"
                ));
        aeExt.addCompressionRule(new CompressionRule(
                "JPEG 2000 Lossless",
                new String[] {
                    "MONOCHROME1",
                    "MONOCHROME2",
                    "PALETTE COLOR",
                    "RGB",
                    "YBR_FULL" },
                new int[] { 8, 9, 10, 11, 12, 13, 14, 15, 16 },  // Bits Stored
                -1,                             // Pixel Representation
                new String[] { "JPEG_2000" },   // Source AETs
                null,                           // Source Device Names
                null,                           // SOP Classes
                null,                           // Image Types
                null,                           // Body Parts
                UID.JPEG2000LosslessOnly,
                "maxPixelValueError=0"
                ));
        DefaultTransferCapabilities.addTCs(ae, null, SCP, DefaultTransferCapabilities.IMAGE_CUIDS, image_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCP, DefaultTransferCapabilities.VIDEO_CUIDS, video_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCP, DefaultTransferCapabilities.OTHER_CUIDS, other_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCU, DefaultTransferCapabilities.IMAGE_CUIDS, image_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCU, DefaultTransferCapabilities.VIDEO_CUIDS, video_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCU, DefaultTransferCapabilities.OTHER_CUIDS, other_tsuids);
        DefaultTransferCapabilities.addTCs(ae, EnumSet.allOf(QueryOption.class), SCP, DefaultTransferCapabilities.QUERY_CUIDS, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTCs(ae, EnumSet.of(QueryOption.RELATIONAL), SCP, DefaultTransferCapabilities.RETRIEVE_CUIDS, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.CompositeInstanceRetrieveWithoutBulkDataGET, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.StorageCommitmentPushModelSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.ModalityPerformedProcedureStepSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCU, UID.ModalityPerformedProcedureStepSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCU, UID.InstanceAvailabilityNotificationSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCU, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian);

        aeExt.setReturnOtherPatientIDs(false);
        aeExt.setReturnOtherPatientNames(true);
        aeExt.setLocalPIXConsumerApplication(pixConsumer);
        aeExt.setRemotePIXManagerApplication(pixManager);
        return ae;
    }

    private ApplicationEntity createQRAE(String aet,
            Connection dicom, Connection dicomTLS,
            String[] image_tsuids, String[] video_tsuids, String[] other_tsuids,
            QueryRetrieveView queryRetrieveView,
            String pixConsumer, String pixManager) {
        ApplicationEntity ae = new ApplicationEntity(aet);
        ae.addConnection(dicom);
        ae.addConnection(dicomTLS);

        ArchiveAEExtension aeExt = new ArchiveAEExtension();
        ae.addAEExtension(aeExt);
        ae.setAssociationAcceptor(true);
        ae.setAssociationInitiator(true);
        aeExt.setMatchUnknown(true);
        aeExt.setSendPendingCGet(true);
        aeExt.setSendPendingCMoveInterval(PENDING_CMOVE_INTERVAL);
        aeExt.setQueryRetrieveViewID(queryRetrieveView.getViewID());

        aeExt.setWadoSRTemplateURI(WADO_SR_TEMPLATE_URI);
        aeExt.setWadoSupportedSRClasses(WADO_SUPPORTED_SR_SOP_CLASSES);
        aeExt.setQIDOMaxNumberOfResults(QIDO_MAX_NUMBER_OF_RESULTS);

        DefaultTransferCapabilities.addTCs(ae, null, SCU, DefaultTransferCapabilities.IMAGE_CUIDS, image_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCU, DefaultTransferCapabilities.VIDEO_CUIDS, video_tsuids);
        DefaultTransferCapabilities.addTCs(ae, null, SCU, DefaultTransferCapabilities.OTHER_CUIDS, other_tsuids);
        DefaultTransferCapabilities.addTCs(ae, EnumSet.allOf(QueryOption.class), SCP, DefaultTransferCapabilities.QUERY_CUIDS, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTCs(ae, EnumSet.of(QueryOption.RELATIONAL), SCP, DefaultTransferCapabilities.RETRIEVE_CUIDS, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.CompositeInstanceRetrieveWithoutBulkDataGET, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCP, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian);
        DefaultTransferCapabilities.addTC(ae, null, SCU, UID.VerificationSOPClass, UID.ImplicitVRLittleEndian);

        aeExt.setReturnOtherPatientIDs(true);
        aeExt.setReturnOtherPatientNames(true);
        aeExt.setLocalPIXConsumerApplication(pixConsumer);
        aeExt.setRemotePIXManagerApplication(pixManager);
        return ae;
    }

}
