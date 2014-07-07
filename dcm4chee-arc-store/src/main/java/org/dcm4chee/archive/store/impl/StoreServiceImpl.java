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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4chee.archive.store.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXTransformer.SetupTransformer;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.ContentItem;
import org.dcm4chee.archive.entity.FileRef;
import org.dcm4chee.archive.entity.FileSystem;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.RequestAttributes;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.VerifyingObserver;
import org.dcm4chee.archive.issuer.IssuerService;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.store.StoreAction;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.store.StoreSessionClosed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@ApplicationScoped
public class StoreServiceImpl implements StoreService {

    static Logger LOG = LoggerFactory.getLogger(StoreServiceImpl.class);

    @Inject
    private PatientService patientService;

    @Inject
    private IssuerService issuerService;

    @Inject
    private CodeService codeService;

    @Inject
    private FileSystemEJB fileSystemEJB;

    @Inject
    private StoreServiceEJB storeServiceEJB;

    @Inject
    private Event<StoreContext> storeEvent;

    @Inject
    @StoreSessionClosed
    private Event<StoreSession> storeSessionClosed;

    @Override
    public StoreSession createStoreSession(StoreService storeService) {
        return new StoreSessionImpl(storeService);
    }

    @Override
    public void initStorageFileSystem(StoreSession session)
            throws DicomServiceException {
        ArchiveAEExtension arcAE = session.getArchiveAEExtension();
        String groupID = arcAE.getFileSystemGroupID();
        FileSystem fs = fileSystemEJB.findCurrentFileSystem(groupID,
                arcAE.getInitFileSystemURI());
        if (fs == null)
            throw new DicomServiceException(Status.OutOfResources,
                    "No writeable File System in File System Group " + groupID);
        session.setStorageFileSystem(fs);
    }

    @Override
    public void initSpoolDirectory(StoreSession session)
            throws DicomServiceException {
        ArchiveAEExtension arcAE = session.getArchiveAEExtension();
        FileSystem fs = session.getStorageFileSystem();
        Path parentDir = fs.getPath().resolve(arcAE.getSpoolDirectoryPath());
        try {
            Files.createDirectories(parentDir);
            Path dir = Files.createTempDirectory(parentDir, null);
            LOG.info("{}: M-WRITE spool directory - {}", session, dir);
            session.setSpoolDirectory(dir);
        } catch (IOException e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public StoreContext createStoreContext(StoreSession session) {
        return new StoreContextImpl(session);
    }

    @Override
    public void writeSpoolFile(StoreContext context, Attributes fmi,
            InputStream data) throws DicomServiceException {
        writeSpoolFile(context, fmi, null, data);
    }

    @Override
    public void writeSpoolFile(StoreContext context, Attributes fmi,
            Attributes attrs) throws DicomServiceException {
        writeSpoolFile(context, fmi, attrs, null);
        context.setTransferSyntax(fmi.getString(Tag.TransferSyntaxUID));
        context.setAttributes(attrs);
    }

    @Override
    public void onClose(StoreSession session) {
        deleteSpoolDirectory(session);
        storeSessionClosed.fire(session);
    }

    @Override
    public void cleanup(StoreContext context) {
        if (context.getFileRef() == null) {
            deleteFinalFile(context);
        }
    }

    private void deleteFinalFile(StoreContext context) {
        Path file = context.getFinalFile();
        if (file == null)
            return;

        StoreSession session = context.getStoreSession();
        try {
            Files.delete(file);
            LOG.info("{}: M-DELETE final file - {}", session, file);
        } catch (IOException e) {
            LOG.warn("{}: Failed to M-DELETE final file - {}", session, file, e);
        }
    }

    private void deleteSpoolDirectory(StoreSession session) {
        Path dir = session.getSpoolDirectory();
        try {
            for (Path file : Files.newDirectoryStream(dir)) {
                try {
                    Files.delete(file);
                    LOG.info("{}: M-DELETE spool file - {}", session, file);
                } catch (IOException e) {
                    LOG.warn("{}: Failed to M-DELETE spool file - {}", session,
                            file, e);
                }
            }
            Files.delete(dir);
            LOG.info("{}: M-DELETE spool directory - {}", session, dir);
        } catch (IOException e) {
            LOG.warn("{}: Failed to M-DELETE spool directory - {}", session,
                    dir, e);
        }
    }

    private void writeSpoolFile(StoreContext context, Attributes fmi,
            Attributes ds, InputStream in) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        MessageDigest digest = session.getMessageDigest();
        try {
            context.setSpoolFile(spool(session, fmi, ds, in, ".dcm", digest));
            if (digest != null)
                context.setSpoolFileDigest(TagUtils.toHexString(digest.digest()));
        } catch (IOException e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public void parseSpoolFile(StoreContext context)
            throws DicomServiceException {
        Path path = context.getSpoolFile();
        try (DicomInputStream in = new DicomInputStream(path.toFile());) {
            in.setIncludeBulkData(IncludeBulkData.URI);
            Attributes fmi = in.readFileMetaInformation();
            Attributes ds = in.readDataset(-1, -1);
            context.setTransferSyntax(fmi != null ? fmi
                    .getString(Tag.TransferSyntaxUID)
                    : UID.ImplicitVRLittleEndian);
            context.setAttributes(ds);
        } catch (IOException e) {
            throw new DicomServiceException(DATA_SET_NOT_PARSEABLE);
        }
    }

    @Override
    public Path spool(StoreSession session, InputStream in, String suffix)
            throws IOException {
        return spool(session, null, null, in, suffix, null);
    }

    private Path spool(StoreSession session, Attributes fmi, Attributes ds,
            InputStream in, String suffix, MessageDigest digest)
            throws IOException {
        Path spoolDirectory = session.getSpoolDirectory();
        Path path = Files.createTempFile(spoolDirectory, null, suffix);
        OutputStream out = Files.newOutputStream(path);
        try {
            if (digest != null) {
                digest.reset();
                out = new DigestOutputStream(out, digest);
            }
            out = new BufferedOutputStream(out);
            if (fmi != null) {
                @SuppressWarnings("resource")
                DicomOutputStream dout = new DicomOutputStream(out,
                        UID.ExplicitVRLittleEndian);
                if (ds == null)
                    dout.writeFileMetaInformation(fmi);
                else
                    dout.writeDataset(fmi, ds);
                out = dout;
            }
            if (in instanceof PDVInputStream) {
                ((PDVInputStream) in).copyTo(out);
            } else if (in != null) {
                StreamUtils.copy(in, out);
            }
        } finally {
            SafeClose.close(out);
        }
        LOG.info("{}: M-WRITE spool file - {}", session, path);
        return path;
    }

    @Override
    public void store(StoreContext context) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        try {
            service.coerceAttributes(context);
            service.processFile(context);
            service.updateDB(context);
        } catch (DicomServiceException e) {
            context.setStoreAction(StoreAction.FAIL);
            context.setThrowable(e);
            throw e;
        } finally {
            service.fireStoreEvent(context);
            service.cleanup(context);
        }
    }

    @Override
    public void fireStoreEvent(StoreContext context) {
        storeEvent.fire(context);
    }

    /*
     * coerceAttributes applies a loaded XSL stylesheet on the keys if given
     * currently 15/4/2014 modifies date and time attributes in the keys per
     * request
     */
    @Override
    public void coerceAttributes(final StoreContext context)
            throws DicomServiceException {

        final StoreSession session = context.getStoreSession();
        ArchiveAEExtension arcAE = session.getArchiveAEExtension();
        Attributes attrs = context.getAttributes();
        try{
            Attributes modified = context.getCoercedOriginalAttributes();
            Templates tpl = arcAE.getAttributeCoercionTemplates(
                    attrs.getString(Tag.SOPClassUID), Dimse.C_STORE_RQ,
                    TransferCapability.Role.SCP, session.getRemoteAET());
            if (tpl != null) {
                attrs.update(
                        SAXTransformer.transform(attrs, tpl, false, false, new SetupTransformer() {
                            
                            @Override
                            public void setup(Transformer transformer) {
                                setParameters(transformer, session);
                            }
                        }),
                        modified);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
            //store service time zone support moved to decorator
            
    }
    private void setParameters(Transformer tr, StoreSession session) {
        setAETParameters(tr, session);
        Date date = new Date();
        String currentDate = DateUtils.formatDA(null, date);
        String currentTime = DateUtils.formatTM(null, date);
        tr.setParameter("date", currentDate);
        tr.setParameter("time", currentTime);
        tr.setParameter("calling", session.getRemoteAET());
        tr.setParameter("called", session.getLocalAET());
    }

    private void setAETParameters(Transformer tr, StoreSession session) {

    }
    
    @Override
    public void processFile(StoreContext context) throws DicomServiceException {
        try {
            StoreSession session = context.getStoreSession();
            StoreService service = session.getStoreService();
            Path source = context.getSpoolFile();
            Path target = service.calcStorePath(context);
            Files.createDirectories(target.getParent());
            String fileName = target.getFileName().toString();
            int copies = 1;
            for (;;) {
                try {
                    context.setFinalFile(Files.move(source, target));
                    context.setFinalFileDigest(context.getSpoolFileDigest());
                    LOG.info("{}: M-WRITE final file - {}", session, target);
                    LOG.info("{}: M-DELETE spool file - {}", session, source);
                    return;
                } catch (FileAlreadyExistsException e) {
                    target = target.resolveSibling(fileName + '.' + copies++);
                }
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public Path calcStorePath(StoreContext context) {
        StoreSession session = context.getStoreSession();
        ArchiveAEExtension arcAE = session.getArchiveAEExtension();
        AttributesFormat format = arcAE.getStorageFilePathFormat();
        if (format == null)
            throw new IllegalStateException(
                    "No StorageFilePathFormat configured for "
                            + session.getLocalAET());
        String path;
        synchronized (format) {
            path = format.format(context.getAttributes());
        }
        FileSystem fs = session.getStorageFileSystem();
        return fs.getPath().resolve(path.replace('/', File.separatorChar));
    }

    @Override
    public void updateDB(StoreContext context) throws DicomServiceException {
        storeServiceEJB.updateDB(context);
        updateAttributes(context);
    }

    @Override
    public void updateDB(EntityManager em, StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Instance instance = service.findOrCreateInstance(em, context);
        context.setInstance(instance);
        if (context.getStoreAction() != StoreAction.IGNORE
                && context.getFinalFile() != null) {
            context.setFileRef(createFileRef(em, context, instance));
        }
    }

    private void updateAttributes(StoreContext context) {
        Instance instance = context.getInstance();
        Series series = instance.getSeries();
        Study study = series.getStudy();
        Patient patient = study.getPatient();
        Attributes attrs = context.getAttributes();
        Attributes modified = new Attributes();
        attrs.update(patient.getAttributes(), modified);
        attrs.update(study.getAttributes(), modified);
        attrs.update(series.getAttributes(), modified);
        attrs.update(instance.getAttributes(), modified);
        if (!modified.isEmpty()) {
            modified.addAll(context.getCoercedOriginalAttributes());
            context.setCoercedOrginalAttributes(modified);
        }
        logCoercedAttributes(context);
    }

    private void logCoercedAttributes(StoreContext context) {
        StoreSession session = context.getStoreSession();
        Attributes attrs = context.getCoercedOriginalAttributes();
        if (!attrs.isEmpty()) {
            LOG.info("{}: Coerced Attributes:\n{}New Attributes:\n{}", session,
                    attrs,
                    new Attributes(context.getAttributes(), attrs.tags()));
        }
    }

    @Override
    public StoreAction instanceExists(EntityManager em, StoreContext context,
            Instance instance) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        Collection<FileRef> fileRefs = instance.getFileRefs();
        if (fileRefs.isEmpty())
            return StoreAction.RESTORE;

        if (hasSameSourceAET(instance, session.getRemoteAET())
                && !hasFileRefWithDigest(fileRefs, context.getSpoolFileDigest()))
            return StoreAction.REPLACE;

        return StoreAction.IGNORE;
    }

    private boolean hasSameSourceAET(Instance instance, String remoteAET) {
        return remoteAET.equals(instance.getSeries().getSourceAET());
    }

    private boolean hasFileRefWithDigest(Collection<FileRef> fileRefs,
            String digest) {
        if (digest == null)
            return false;

        for (FileRef fileRef : fileRefs) {
            if (digest.equals(fileRef.getDigest()))
                return true;
        }
        return false;
    }

    @Override
    public Instance findOrCreateInstance(EntityManager em, StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        try {
            Attributes attrs = context.getAttributes();
            Instance inst = em
                    .createNamedQuery(Instance.FIND_BY_SOP_INSTANCE_UID,
                            Instance.class)
                    .setParameter(1, attrs.getString(Tag.SOPInstanceUID))
                    .getSingleResult();
            StoreAction action = service.instanceExists(em, context, inst);
            LOG.info("{}: {} already exists - {}", session, inst, action);
            context.setStoreAction(action);
            switch (action) {
            case RESTORE:
                service.updateInstance(em, context, inst);
            case IGNORE:
                return inst;
            case REPLACE:
                inst.setReplaced(true);
            }
        } catch (NoResultException e) {
            context.setStoreAction(StoreAction.STORE);
        } catch (DicomServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
        return service.createInstance(em, context);
    }

    @Override
    public Series findOrCreateSeries(EntityManager em, StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes attrs = context.getAttributes();
        try {
            Series series = em
                    .createNamedQuery(Series.FIND_BY_SERIES_INSTANCE_UID,
                            Series.class)
                    .setParameter(1, attrs.getString(Tag.SeriesInstanceUID))
                    .getSingleResult();
            service.updateSeries(em, context, series);
            return series;
        } catch (NoResultException e) {
            return service.createSeries(em, context);
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public Study findOrCreateStudy(EntityManager em, StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes attrs = context.getAttributes();
        try {
            Study study = em
                    .createNamedQuery(Study.FIND_BY_STUDY_INSTANCE_UID,
                            Study.class)
                    .setParameter(1, attrs.getString(Tag.StudyInstanceUID))
                    .getSingleResult();
            service.updateStudy(em, context, study);
            return study;
        } catch (NoResultException e) {
            return service.createStudy(em, context);
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public Patient findOrCreatePatient(EntityManager em, StoreContext context)
            throws DicomServiceException {
        try {
            StoreSession session = context.getStoreSession();
            return patientService.updateOrCreatePatientOnCStore(
                    context.getAttributes(),
                    new IDPatientSelector(),
                    session.getStoreParam());
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public Study createStudy(EntityManager em, StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes attrs = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        FileSystem fs = session.getStorageFileSystem();
        Study study = new Study();
        study.setPatient(service.findOrCreatePatient(em, context));
        study.setProcedureCodes(codeList(attrs, Tag.ProcedureCodeSequence));
        study.setModalitiesInStudy(attrs.getString(Tag.Modality, null));
        study.setSOPClassesInStudy(attrs.getString(Tag.SOPClassUID, null));
        study.setRetrieveAETs(storeParam.getRetrieveAETs());
        study.setExternalRetrieveAET(storeParam.getExternalRetrieveAET());
        study.setAvailability(fs.getAvailability());
        study.setAttributes(attrs, storeParam.getAttributeFilter(Entity.Study),
                storeParam.getFuzzyStr());
        study.setIssuerOfAccessionNumber(findOrCreateIssuer(
                attrs.getNestedDataset(Tag.IssuerOfAccessionNumberSequence)));
        em.persist(study);
        LOG.info("{}: Create {}", session, study);
        return study;
    }

    private Issuer findOrCreateIssuer(Attributes item) {
        return item != null
                ? issuerService.findOrCreate(new Issuer(item))
                : null;
    }

    @Override
    public Series createSeries(EntityManager em, StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        FileSystem fs = session.getStorageFileSystem();
        Series series = new Series();
        series.setStudy(service.findOrCreateStudy(em, context));
        series.setInstitutionCode(singleCode(data, Tag.InstitutionCodeSequence));
        series.setRequestAttributes(createRequestAttributes(
                data.getSequence(Tag.RequestAttributesSequence),
                storeParam.getFuzzyStr()));
        series.setSourceAET(session.getRemoteAET());
        series.setRetrieveAETs(storeParam.getRetrieveAETs());
        series.setExternalRetrieveAET(storeParam.getExternalRetrieveAET());
        series.setAvailability(fs.getAvailability());
        series.setAttributes(data,
                storeParam.getAttributeFilter(Entity.Series),
                storeParam.getFuzzyStr());
        em.persist(series);
        LOG.info("{}: Create {}", session, series);
        return series;
    }

    @Override
    public Instance createInstance(EntityManager em, StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        FileSystem fs = session.getStorageFileSystem();
        Instance inst = new Instance();
        inst.setSeries(service.findOrCreateSeries(em, context));
        inst.setConceptNameCode(singleCode(data, Tag.ConceptNameCodeSequence));
        inst.setVerifyingObservers(createVerifyingObservers(
                data.getSequence(Tag.VerifyingObserverSequence),
                storeParam.getFuzzyStr()));
        inst.setContentItems(createContentItems(data
                .getSequence(Tag.ContentSequence)));
        inst.setRetrieveAETs(storeParam.getRetrieveAETs());
        inst.setExternalRetrieveAET(storeParam.getExternalRetrieveAET());
        inst.setAvailability(fs.getAvailability());
        inst.setAttributes(data,
                storeParam.getAttributeFilter(Entity.Instance),
                storeParam.getFuzzyStr());
        em.persist(inst);
        LOG.info("{}: Create {}", session, inst);
        return inst;
    }

    private FileRef createFileRef(EntityManager em, StoreContext context,
            Instance instance) {

        StoreSession session = context.getStoreSession();
        FileSystem fs = session.getStorageFileSystem();
        Path filePath = context.getFinalFile();
        FileRef fileRef = new FileRef(fs, unixFilePath(fs.getPath(), filePath),
                context.getTransferSyntax(), filePath.toFile().length(),
                context.getFinalFileDigest());
        // Time zone store adjustments
        TimeZone sourceTimeZone = session.getSourceTimeZone();
        if (sourceTimeZone != null)
            fileRef.setSourceTimeZone(sourceTimeZone.getID());
        fileRef.setInstance(instance);
        em.persist(fileRef);
        LOG.info("{}: Create {}", session, fileRef);
        return fileRef;
    }

    private String unixFilePath(Path fsPath, Path filePath) {
        return fsPath.relativize(filePath).toString()
                .replace(File.separatorChar, '/');
    }

    @Override
    public void updateStudy(EntityManager em, StoreContext context, Study study) {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        study.addModalityInStudy(data.getString(Tag.Modality, null));
        study.addSOPClassInStudy(data.getString(Tag.SOPClassUID, null));
        study.resetNumberOfInstances();
        AttributeFilter studyFilter = storeParam
                .getAttributeFilter(Entity.Study);
        Attributes studyAttrs = study.getAttributes();
        Attributes modified = new Attributes();
        if (studyAttrs.updateSelected(data, modified,
                studyFilter.getSelection())) {
            study.setAttributes(studyAttrs, studyFilter,
                    storeParam.getFuzzyStr());
            LOG.info("{}: Update {}:\n{}\nmodified:\n{}", session, study,
                    studyAttrs, modified);
        }
        service.updatePatient(em, context, study.getPatient());
    }

    @Override
    public void updatePatient(EntityManager em, StoreContext context,
            Patient patient) {
        StoreSession session = context.getStoreSession();
        patientService.updatePatientByCStore(patient, 
                context.getAttributes(),
                session.getStoreParam());
    }

    @Override
    public void updateSeries(EntityManager em, StoreContext context,
            Series series) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        series.resetNumberOfInstances();
        Attributes seriesAttrs = series.getAttributes();
        AttributeFilter seriesFilter = storeParam
                .getAttributeFilter(Entity.Series);
        Attributes modified = new Attributes();
        if (seriesAttrs.updateSelected(data, modified,
                seriesFilter.getSelection())) {
            series.setAttributes(seriesAttrs, seriesFilter,
                    storeParam.getFuzzyStr());
            LOG.info("{}: Update {}:\n{}\nmodified:\n{}", session, series,
                    seriesAttrs, modified);
        }
        service.updateStudy(em, context, series.getStudy());
    }

    @Override
    public void updateInstance(EntityManager em, StoreContext context,
            Instance inst) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        Attributes instAttrs = inst.getAttributes();
        AttributeFilter instFilter = storeParam
                .getAttributeFilter(Entity.Instance);
        Attributes modified = new Attributes();
        if (instAttrs.updateSelected(data, modified, instFilter.getSelection())) {
            inst.setAttributes(data, instFilter, storeParam.getFuzzyStr());
            LOG.info("{}: {}:\n{}\nmodified:\n{}", session, inst, instAttrs,
                    modified);
        }
        service.updateSeries(em, context, inst.getSeries());
    }

    private Collection<RequestAttributes> createRequestAttributes(Sequence seq,
            FuzzyStr fuzzyStr) {
        if (seq == null || seq.isEmpty())
            return null;

        ArrayList<RequestAttributes> list = new ArrayList<RequestAttributes>(
                seq.size());
        for (Attributes item : seq)
            list.add(new RequestAttributes(item,
                    findOrCreateIssuer(
                            item.getNestedDataset(Tag.IssuerOfAccessionNumberSequence)),
                    fuzzyStr));
        return list;
    }

    private Collection<VerifyingObserver> createVerifyingObservers(
            Sequence seq, FuzzyStr fuzzyStr) {
        if (seq == null || seq.isEmpty())
            return null;

        ArrayList<VerifyingObserver> list = new ArrayList<VerifyingObserver>(
                seq.size());
        for (Attributes item : seq)
            list.add(new VerifyingObserver(item, fuzzyStr));
        return list;
    }

    private Collection<ContentItem> createContentItems(Sequence seq) {
        if (seq == null || seq.isEmpty())
            return null;

        Collection<ContentItem> list = new ArrayList<ContentItem>(seq.size());
        for (Attributes item : seq) {
            String type = item.getString(Tag.ValueType);
            if ("CODE".equals(type)) {
                list.add(new ContentItem(item.getString(Tag.RelationshipType)
                        .toUpperCase(), singleCode(item,
                        Tag.ConceptNameCodeSequence), singleCode(item,
                        Tag.ConceptCodeSequence)));
            } else if ("TEXT".equals(type)) {
                list.add(new ContentItem(item.getString(Tag.RelationshipType)
                        .toUpperCase(), singleCode(item,
                        Tag.ConceptNameCodeSequence), item.getString(
                        Tag.TextValue, "*")));
            }
        }
        return list;
    }

    private Code singleCode(Attributes attrs, int seqTag) {
        Attributes item = attrs.getNestedDataset(seqTag);
        if (item != null)
            try {
                return codeService.findOrCreate(new Code(item));
            } catch (Exception e) {
                LOG.info("Illegal code item in Sequence {}:\n{}",
                        TagUtils.toString(seqTag), item);
            }
        return null;
    }

    private Collection<Code> codeList(Attributes attrs, int seqTag) {
        Sequence seq = attrs.getSequence(seqTag);
        if (seq == null || seq.isEmpty())
            return Collections.emptyList();

        ArrayList<Code> list = new ArrayList<Code>(seq.size());
        for (Attributes item : seq) {
            try {
                list.add(codeService.findOrCreate(new Code(item)));
            } catch (Exception e) {
                LOG.info("Illegal code item in Sequence {}:\n{}",
                        TagUtils.toString(seqTag), item);
            }
        }
        return list;
    }

}
