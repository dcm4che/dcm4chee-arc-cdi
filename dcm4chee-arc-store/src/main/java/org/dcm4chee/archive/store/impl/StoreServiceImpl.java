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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXTransformer.SetupTransformer;
import org.dcm4che3.net.*;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.archive.monitoring.api.Monitored;
import org.dcm4chee.archive.patient.PatientSelectorFactory;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.store.StoreSessionClosed;
import org.dcm4chee.archive.util.RetryBean;
import org.dcm4chee.storage.ObjectAlreadyExistsException;
import org.dcm4chee.storage.RetrieveContext;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.dcm4chee.storage.service.RetrieveService;
import org.dcm4chee.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Umberto Cappellini
 */
@ApplicationScoped
public class StoreServiceImpl implements StoreService {

    static Logger LOG = LoggerFactory.getLogger(StoreServiceImpl.class);

    static ExecutorService executor = Executors.newCachedThreadPool();

    @Inject
    private StoreServiceEJB storeServiceEJB;

    @Inject
    private StorageService storageService;

    @Inject
    private RetrieveService retrieveService;

    @Inject
    private PatientService patientService;

    @Inject
    private Event<StoreContext> storeEvent;

    @Inject
    private LocationMgmt locationManager;

    @Inject
    private MemoryOrFileSpooler memoryOrfileSpooler;

    @Inject
    private FileSpooler fileSpooler;

    @Inject
    @StoreSessionClosed
    private Event<StoreSession> storeSessionClosed;

    @Inject
    private Device device;

    @Inject RetryBean<Void,DicomServiceException> retry;

    private int[] storeFilters = null;

    @Override
    public StoreSession createStoreSession(StoreService storeService) {
        return new StoreSessionImpl(storeService);
    }

    @Override
    public void init(StoreSession session) throws DicomServiceException {
        initBulkdataStorage(session);
        initMetadataStorage(session);
        initSpoolingStorage(session);
    }

    private void initBulkdataStorage(StoreSession session)
            throws DicomServiceException {
        ArchiveAEExtension arcAE = session.getArchiveAEExtension();
        String groupID = arcAE.getStorageSystemGroupID();
        if (groupID == null) {
            String groupType = arcAE.getStorageSystemGroupType();
            if (groupType != null) {
                StorageSystemGroup group = storageService
                        .selectBestStorageSystemGroup(groupType);
                if (group != null)
                    groupID = group.getGroupID();
            }
        }
        StorageSystem storageSystem = storageService.selectStorageSystem(
                groupID, 0);
        if (storageSystem == null)
            throw new DicomServiceException(Status.OutOfResources,
                    "No writeable Storage System in Storage System Group "
                            + groupID);
        session.setStorageSystem(storageSystem);
    }

    private void initMetadataStorage(StoreSession session)
            throws DicomServiceException {
        ArchiveAEExtension arcAE = session.getArchiveAEExtension();
        String groupID = arcAE.getMetaDataStorageSystemGroupID();
        if (groupID != null) {
            StorageSystem storageSystem = storageService.selectStorageSystem(
                    groupID, 0);
            if (storageSystem == null)
                throw new DicomServiceException(Status.OutOfResources,
                        "No writeable Storage System in Storage System Group "
                                + groupID);
            session.setMetaDataStorageSystem(storageSystem);
        }
    }

    private void initSpoolingStorage(StoreSession session)
            throws DicomServiceException {

        StorageSystem system = session.getStorageSystem();
        if (system == null) {
            throw new DicomServiceException(Status.ProcessingFailure,
                    "No writeable storage group conifugred");
        }

        //spool is in the same dir of the destination, to ease the move operation
        Path spoolingPath = Paths.get(system.getStorageSystemPath(), "spool");

        try {
            LOG.info("INIT spool storage - {}", spoolingPath);
            Files.createDirectories(spoolingPath);
        } catch (IOException e) {
            throw new DicomServiceException(Status.OutOfResources,
                    "No writeable storage system in group " +
                            system.getStorageSystemGroup().getGroupID(), e);
        }

        session.setSpoolStorageSystem(system);
        session.setSpoolDirectory(spoolingPath);
    }

    @Override
    public StoreContext createStoreContext(StoreSession session) {
        return new StoreContextImpl(session);
    }

    @Override
    public void writeSpoolFile(StoreContext context, Attributes fmi, InputStream data)
            throws DicomServiceException {
        writeSpoolFile(context, fmi, null, data);
    }

    @Override
    public void writeSpoolFile(StoreContext context, Attributes fmi, Attributes attrs)
            throws DicomServiceException {
        writeSpoolFile(context, fmi, attrs, null);
        context.setTransferSyntax(fmi.getString(Tag.TransferSyntaxUID));
        context.setAttributes(attrs);
    }

    @Override
    public void onClose(StoreSession session) {
        StorageSystem system = session.getStorageSystem();
        syncFilesOnAssociationClose(session);
        deleteSpoolDirectory(session);
        storeSessionClosed.fire(session);
    }

    @Override
    public void cleanup(StoreContext context) {
        if (context.getFileRef() == null) {
            cleanFinalFile(context);
            cleanMetaData(context);
        }
    }

    private void cleanMetaData(StoreContext context) {
        Future<StorageContext> futureMetadataContext = context.getMetadataContext();
        context.setMetadataContext(null);

        if (futureMetadataContext != null) {
            Path metadataPath = null;
            try {
                StorageContext metadataContext = futureMetadataContext.get();
                if (metadataContext != null) {
	                metadataPath = metadataContext.getFilePath();
	                if (metadataPath != null) {
	                    storageService.deleteObject(metadataContext, metadataPath.toString());
	                }
                } else {
                	LOG.info("Skip cleanMetaData. Missing StoreContext for metadata!");
                }
            } catch (Exception e) {
                LOG.warn("{} failed to clean metadata path {}",
                        context.getStoreSession(), metadataPath, e);
            }
        }
    }

    private void cleanFinalFile(StoreContext context) {
        Future<StorageContext> futureBulkdataContext = context.getBulkdataContext();
        context.setBulkdataContext(null);

        if (futureBulkdataContext != null) {
            Path bulkdataPath = null;
            try {
                StorageContext bulkdataContext = futureBulkdataContext.get();
                bulkdataPath = bulkdataContext.getFilePath();
                if (bulkdataPath != null) {
                    storageService.deleteObject(bulkdataContext, bulkdataPath.toString());
                }
            } catch (Exception e) {
                LOG.warn("{} failed to clean Final File path {}",
                        context.getStoreSession(), bulkdataPath, e);
            }
        }
    }

    private void deleteSpoolDirectory(StoreSession session) {
        Path dir = session.getSpoolDirectory();
        if (dir!=null) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(dir)) {
                for (Path file : directory) {
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
    }

    private void writeSpoolFile(StoreContext context, Attributes fmi,
            Attributes ds, InputStream in) throws DicomServiceException {
        context.setFileMetainfo(fmi);
        context.setInputStream(in);
        context.setAttributes(ds);
        try {
            fileSpooler.spool(context, true);
        } catch (IOException e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public void parseSpoolFile(StoreContext context)
            throws DicomServiceException {
        Path path = context.getSpoolingContext().getFilePath();
        try (DicomInputStream in = new DicomInputStream(path.toFile())) {
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
    public Path spool(StoreSession session, InputStream in, String suffix) throws IOException {
        StoreContext context = createStoreContext(session);
        context.setInputStream(in);
        context.setSpoolFileSuffix(suffix);
        fileSpooler.spool(context, false); //do not parse
        return context.getSpoolingContext().getFilePath();
    }

    @Override
    public void spool(StoreContext context) throws DicomServiceException {
        // spools either in memory or file
        memoryOrfileSpooler.spool(context, true);
    }

    @Override
    public void store(StoreContext context) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        updateFetchStatus(context);

        try {
            // spools either in memory or file
            //service.spool(context);

            // stores metadata (async)
            service.beginStoreMetadata(context);
            
            // stores complete file meta+bulkdata (async)
            service.beginProcessFile(context);
            
            try {
                context.getBulkdataContext().get();
                context.getMetadataContext().get();
            } catch (ExecutionException x) {
                LOG.warn("Store Bulkdata failed!", x);
                for ( Throwable cause = x.getCause() ; cause != null ; cause = cause.getCause()) {
                    if (cause instanceof DicomServiceException)
                        throw (DicomServiceException) cause;
                }
                throw new DicomServiceException(Status.ProcessingFailure, x);
            } catch (InterruptedException e) {
                LOG.warn("Waiting for storage completed was interrupted!", e);
                throw new DicomServiceException(Status.ProcessingFailure, e);
            }
            // coerce attrs
            service.coerceAttributes(context);

            // updates
            service.updateDB(context);

        } catch (DicomServiceException e) {
            context.setStoreAction(StoreAction.FAIL);
            context.setThrowable(e);
            throw e;
        } finally {
            syncFilesOnStore(session);
            service.fireStoreEvent(context);
            service.cleanup(context);
        }
    }

    @Override
    public void fireStoreEvent(StoreContext context) {
        storeEvent.fire(context);
    }

    private void updateFetchStatus(StoreContext context) {

        StoreSession session = context.getStoreSession();

        String fetchAET = session.getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class)
                .getFetchAETitle();

        String localAET = session.getLocalAET();
        if (fetchAET.equals(localAET)) {
            context.setFetch(true);
        }
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
        try {
            Attributes modified = context.getCoercedOriginalAttributes();
            Templates tpl = session.getRemoteAET() != null ? arcAE
                    .getAttributeCoercionTemplates(
                            attrs.getString(Tag.SOPClassUID), Dimse.C_STORE_RQ,
                            TransferCapability.Role.SCP, session.getRemoteAET())
                    : null;
            if (tpl != null) {
                attrs.update(SAXTransformer.transform(attrs, tpl, false, false,
                        new SetupTransformer() {

                            @Override
                            public void setup(Transformer transformer) {
                                setParameters(transformer, session);
                            }
                        }), modified);
            }
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    private void setParameters(Transformer tr, StoreSession session) {
        Date date = new Date();
        String currentDate = DateUtils.formatDA(null, date);
        String currentTime = DateUtils.formatTM(null, date);
        tr.setParameter("date", currentDate);
        tr.setParameter("time", currentTime);
        tr.setParameter("calling", session.getRemoteAET());
        tr.setParameter("called", session.getLocalAET());
    }

    @Override
    @Monitored(name="processFile")
    public StorageContext processFile(StoreContext context) throws DicomServiceException {

        StorageSystem bulkdataStorageSystem = context.getStoreSession().getStorageSystem();
        if (bulkdataStorageSystem == null)
            return null;

        Attributes fmi = context.getFileMetainfo();
        Attributes attributes = context.getAttributes();
        StorageContext bulkdataContext = storageService.createStorageContext(bulkdataStorageSystem);
        String bulkdataRoot = calculatePath(bulkdataStorageSystem, attributes);
        String bulkdataPath = bulkdataRoot;
        StorageContext spoolingContext = context.getSpoolingContext();

        int copies = 1;
        if (spoolingContext.getFilePath() != null) {
            //spool in file
            while (spoolingContext.getFilePath() != null) {
                try {
                    storageService.moveFile(bulkdataContext, spoolingContext.getFilePath(), bulkdataPath);
                    bulkdataContext.setFileDigest(spoolingContext.getFileDigest());
                    bulkdataContext.setFileSize(spoolingContext.getFileSize());
                    bulkdataContext.setFilePath(Paths.get(bulkdataPath));
                    spoolingContext.setFilePath(null);
                } catch (IOException e) {
                    bulkdataPath = bulkdataRoot + '.' + copies++;
                }
            }
        } else {
            //spool in memory
            int bufferLength = bulkdataStorageSystem.getBufferedOutputLength();
            OutputStream out = null;

            try {
                while (out == null) {
                    try {
                        out = storageService.openOutputStream(bulkdataContext, bulkdataPath);
                    } catch (ObjectAlreadyExistsException e) {
                        bulkdataPath = bulkdataRoot + '.' + copies++;
                    }
                }
                out = new BufferedOutputStream(out, bufferLength);
                out = new DicomOutputStream(out, UID.ExplicitVRLittleEndian);
                ((DicomOutputStream) out).writeDataset(fmi, attributes);
            } catch (Exception e) {
                throw new DicomServiceException(Status.UnableToProcess, e);
            } finally {
                try {
                    SafeClose.close(out);
                    bulkdataContext.setFilePath(Paths.get(bulkdataPath));
                    bulkdataContext.setFileSize(Files.size(Paths.get(bulkdataStorageSystem.getStorageSystemPath(), bulkdataPath)));
                    bulkdataContext.setFileDigest(spoolingContext.getFileDigest());
                    context.getStoreSession().addStoredFile(bulkdataPath);
                } catch (IOException e) {
                    throw new DicomServiceException(Status.UnableToProcess, e);
                }
            }
        }

        return bulkdataContext;
    }

    @Override
    public void updateDB(final StoreContext context) throws DicomServiceException {

        ArchiveDeviceExtension dE = context.getStoreSession().getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class);

        try {
            StorageContext bulkdataContext = context.getBulkdataContext().get();
            String nodbAttrsDigest = noDBAttsDigest(bulkdataContext.getFilePath(),context.getStoreSession());
            context.setNoDBAttsDigest(nodbAttrsDigest);
        } catch (IOException|InterruptedException|ExecutionException e1) {
            throw new DicomServiceException(Status.UnableToProcess, e1);
        }

        // try to call updateDB, eventually retries
        retry.retry(new Callable<Void>() {
            @Override
            public Void call() throws DicomServiceException {
                storeServiceEJB.updateDB(context);
                return null;
            }
        });

        updateAttributes(context);
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

        Collection<Location> fileRefs = instance.getLocations();

        if (fileRefs.isEmpty())
            return StoreAction.RESTORE;

        if (context.getStoreSession().getArchiveAEExtension()
                .isIgnoreDuplicatesOnStorage())
            return StoreAction.IGNORE;

        if (!hasSameSourceAET(instance, session.getRemoteAET()))
            return StoreAction.IGNORE;

        if (hasFileRefWithDigest(fileRefs, context.getSpoolingContext().getFileDigest()))
            return StoreAction.IGNORE;

        if (context.getStoreSession().getArchiveAEExtension()
                .isCheckNonDBAttributesOnStorage()
                && (hasFileRefWithOtherAttsDigest(fileRefs,
                        context.getNoDBAttsDigest())))
            return StoreAction.UPDATEDB;

        return StoreAction.REPLACE;
    }

    private String calculatePath(StorageSystem system, Attributes attributes) {
        String pattern = system.getStorageSystemGroup().getStorageFilePathFormat();
        AttributesFormat format = AttributesFormat.valueOf(pattern);
        synchronized (format) {
            return format.format(attributes);
        }
    }

    private boolean hasSameSourceAET(Instance instance, String remoteAET) {
        return remoteAET.equals(instance.getSeries().getSourceAET());
    }

    private boolean hasFileRefWithDigest(Collection<Location> fileRefs,
            String digest) {
        if (digest == null)
            return false;

        for (Location fileRef : fileRefs) {
            if (digest.equals(fileRef.getDigest()))
                return true;
        }
        return false;
    }

    private boolean hasFileRefWithOtherAttsDigest(
            Collection<Location> fileRefs, String digest) {
        if (digest == null)
            return false;

        for (Location fileRef : fileRefs) {
            if (digest.equals(fileRef.getOtherAttsDigest()))
                return true;
        }
        return false;
    }

    @Override
    public Instance findOrCreateInstance(EntityManager em, StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Collection<Location> replaced = new ArrayList<>();

        try {

            Attributes attrs = context.getAttributes();
            Instance inst = em
                    .createNamedQuery(Instance.FIND_BY_SOP_INSTANCE_UID_EAGER,
                            Instance.class)
                    .setParameter(1, attrs.getString(Tag.SOPInstanceUID))
                    .getSingleResult();
            StoreAction action = service.instanceExists(em, context, inst);
            LOG.info("{}: {} already exists - {}", session, inst, action);
            context.setStoreAction(action);
            switch (action) {
            case RESTORE:
            case UPDATEDB:
                storeServiceEJB.updateInstance(context, inst);
            case IGNORE:
                unmarkLocationsForDelete(inst, context);
                return inst;
            case REPLACE:
                for (Iterator<Location> iter = inst.getLocations().iterator(); iter
                        .hasNext();) {
                    Location fileRef = iter.next();
                    // no other instances referenced through alias table
                    if (fileRef.getInstances().size() == 1) {
                        // delete
                        replaced.add(fileRef);
                    } else {
                        // remove inst
                        fileRef.getInstances().remove(inst);
                    }
                    iter.remove();
                }
                em.remove(inst);
            }
        } catch (NoResultException e) {
            context.setStoreAction(StoreAction.STORE);
        } catch (DicomServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }

        Instance newInst = storeServiceEJB.createInstance(context);

        // delete replaced
        try {
            if (replaced.size()>0)
                locationManager.scheduleDelete(replaced, 0,false);
        } catch (Exception e) {
            LOG.error("StoreService : Error deleting replaced location - {}", e);
        }
        return adjustForNoneIOCM(newInst, context);
    }

    @Override
    public Series findOrCreateSeries(EntityManager em, StoreContext context)
            throws DicomServiceException {
        Attributes attrs = context.getAttributes();
        try {
            Series series = em
                    .createNamedQuery(Series.FIND_BY_SERIES_INSTANCE_UID_EAGER,
                            Series.class)
                    .setParameter(1, attrs.getString(Tag.SeriesInstanceUID))
                    .getSingleResult();
            storeServiceEJB.updateSeries(context, series);
            return series;
        } catch (NoResultException e) {
            return storeServiceEJB.createSeries(context);
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public Study findOrCreateStudy(EntityManager em, StoreContext context)
            throws DicomServiceException {
        Attributes attrs = context.getAttributes();
        try {
            Study study = em
                    .createNamedQuery(Study.FIND_BY_STUDY_INSTANCE_UID_EAGER,
                            Study.class)
                    .setParameter(1, attrs.getString(Tag.StudyInstanceUID))
                    .getSingleResult();
            storeServiceEJB.updateStudy(context, study);
            return study;
        } catch (NoResultException e) {
            return storeServiceEJB.createStudy(context);
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    @Override
    public Patient findOrCreatePatient(EntityManager em, StoreContext context)
            throws DicomServiceException {
        try {
            // ArchiveAEExtension arcAE = context.getStoreSession()
            // .getArchiveAEExtension();
            // PatientSelector selector = arcAE.getPatientSelector();
            // System.out.println("Selector Class Name:"+selector.getPatientSelectorClassName());
            // for (String key :
            // selector.getPatientSelectorProperties().keySet())
            // System.out.println("Property:("+key+","+selector.getPatientSelectorProperties().get(key)+")");

            StoreSession session = context.getStoreSession();
            return patientService.updateOrCreatePatientOnCStore(context
                    .getAttributes(), PatientSelectorFactory
                    .createSelector(session.getStoreParam()),
                    session.getStoreParam());
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }
    }

    private int[] getStoreFilters(Attributes attrs) {

        if (storeFilters == null) {

            ArchiveDeviceExtension dExt = device
                    .getDeviceExtension(ArchiveDeviceExtension.class);
            storeFilters = merge(
                    dExt.getAttributeFilter(Entity.Patient)
                            .getCompleteSelection(attrs),
                    dExt.getAttributeFilter(Entity.Study).getCompleteSelection(
                            attrs), dExt.getAttributeFilter(Entity.Series)
                            .getCompleteSelection(attrs), dExt
                            .getAttributeFilter(Entity.Instance)
                            .getCompleteSelection(attrs));
            Arrays.sort(storeFilters);
        }

        return storeFilters;
    }

    @Override
    public StorageContext storeMetaData(StoreContext context) throws DicomServiceException {
        StorageSystem metadataStorage = context.getStoreSession().getMetaDataStorageSystem();
        if (metadataStorage==null)
            return null;

        Attributes attributes = context.getAttributes();
        StorageContext metadataContext = storageService.createStorageContext(metadataStorage);
        String metadataRoot = calculatePath(metadataStorage, attributes);
        String metadataPath = metadataRoot;
        int copies = 1;

        int bufferLength = metadataStorage.getBufferedOutputLength();
        MessageDigest digest = metadataContext.getDigest();
        OutputStream out = null;

        try {
            while (out == null) {
                try {
                    out = storageService.openOutputStream(metadataContext, metadataPath);
                    metadataContext.setFilePath(Paths.get(metadataPath));
                } catch (ObjectAlreadyExistsException e) {
                    metadataPath = metadataRoot + '.' + copies++;
                }
            }

            Attributes metadata = new Attributes(attributes.bigEndian(), attributes.size());
            metadata.addWithoutBulkData(attributes, BulkDataDescriptor.DEFAULT);
            if (digest != null) {
                digest.reset();
                out = new DigestOutputStream(out, digest);
            }

            out = new BufferedOutputStream(out, bufferLength);
            out = new DicomOutputStream(out, UID.ExplicitVRLittleEndian);
            ((DicomOutputStream)out).writeDataset(metadata.
                    createFileMetaInformation(UID.ExplicitVRLittleEndian), metadata);

        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        } finally {

            SafeClose.close(out);
            metadataContext.setFileDigest(digest == null ? null : TagUtils.toHexString(digest.digest()));
        }
        return metadataContext;
    }

    public int[] merge(final int[]... arrays) {
        int size = 0;
        for (int[] a : arrays)
            size += a.length;

        int[] res = new int[size];

        int destPos = 0;
        for (int i = 0; i < arrays.length; i++) {
            if (i > 0)
                destPos += arrays[i - 1].length;
            int length = arrays[i].length;
            System.arraycopy(arrays[i], 0, res, destPos, length);
        }

        return res;
    }

    private void unmarkLocationsForDelete(Instance inst, StoreContext context) {
        for (Location loc : inst.getLocations()) {
            if (loc.getStatus() == Location.Status.DELETE_FAILED) {
                if (loc.getStorageSystemGroupID().compareTo(
                        context.getStoreSession().getArchiveAEExtension()
                                .getStorageSystemGroupID()) == 0) 
                    loc.setStatus(Location.Status.OK);
                else if(belongsToAnyOnline(loc))
                    loc.setStatus(Location.Status.OK);
                else
                    loc.setStatus(Location.Status.ARCHIVE_FAILED);
            }
        }
    }

    private boolean belongsToAnyOnline(Location loc) {
        for (ApplicationEntity ae : device.getApplicationEntities()) {
            ArchiveAEExtension arcAEExt = ae
                    .getAEExtension(ArchiveAEExtension.class);
            if (arcAEExt.getStorageSystemGroupID().compareTo(
                    loc.getStorageSystemGroupID()) == 0)
                return true;
        }
        return false;
    }

    /**
     * Given a reference to a stored object, retrieves it and calculates the
     * digest of all the attributes (including bulk data), not stored in the
     * database. This step is optionally skipped by configuration.
     */
    private String noDBAttsDigest(Path path, StoreSession session) throws IOException {

        if (session.getArchiveAEExtension().isCheckNonDBAttributesOnStorage()) {

            // retrieves and parses the object
            RetrieveContext retrieveContext = retrieveService.createRetrieveContext(session.getStorageSystem());
            InputStream stream = retrieveService.openInputStream(retrieveContext, path.toString());
            DicomInputStream dstream = new DicomInputStream(stream);
            dstream.setIncludeBulkData(IncludeBulkData.URI);
            Attributes attrs = dstream.readDataset(-1, -1);
            dstream.close();

            // selects attributes non stored in the db
            Attributes noDBAtts = new Attributes();
            noDBAtts.addNotSelected(attrs, getStoreFilters(attrs));

            return Utils.digestAttributes(noDBAtts, session.getMessageDigest());
        } else
            return null;
    }

    @Override
    public void beginProcessFile(final StoreContext context) {

        final StoreSession session = context.getStoreSession();
        final StoreService service = session.getStoreService();

        Future<StorageContext> futureBulkDataContext = executor.submit
                (new Callable<StorageContext>() {
                    @Override
                    public StorageContext call() throws DicomServiceException {
                        return service.processFile(context);
                    }
                });
        context.setBulkdataContext(futureBulkDataContext);
    }

    @Override
    public void beginStoreMetadata(final StoreContext context) {

        final StoreSession session = context.getStoreSession();
        final StoreService service = session.getStoreService();

        Future<StorageContext> futureMetadataContext = executor.submit
                (new Callable<StorageContext>() {
                    @Override
                    public StorageContext call() throws DicomServiceException {
                        return service.storeMetaData(context);
                    }
                });
        context.setMetadataContext(futureMetadataContext);
    }

    @Override
    public Instance adjustForNoneIOCM(Instance instanceToStore,  StoreContext context) {
        //here decorators can set the action depending if the instance
        //was previously deleted or not
        return instanceToStore;
    }

    private void syncFilesOnAssociationClose (StoreSession session) {
        syncFiles(session, true);
    }

    private void syncFilesOnStore (StoreSession session) {
        syncFiles(session, false);
    }

    private void syncFiles(StoreSession session, boolean onClose) {
        final List<String> storedFiles = session.getStoredFiles();
        final StorageSystem system = session.getStorageSystem();

        if (storedFiles.size() == 0)
            return;

        try {
            switch (session.getStorageSystem().getSyncPolicy()) {
                case ALWAYS:
                    storageService.syncFiles(system, storedFiles);
                    break;
                case AFTER_STORE_RSP:
                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        public void run() {
                            try {
                                storageService.syncFiles(system,storedFiles);
                            } catch (IOException e) {
                                LOG.error("File syncing failed:", e);
                            }
                        }
                    });
                    break;
                case EVERY_5_STORE:
                    if (storedFiles.size()>=5)
                        storageService.syncFiles(system, storedFiles);
                    break;
                case EVERY_25_STORE:
                    if (storedFiles.size()>=25)
                        storageService.syncFiles(system, storedFiles);
                    break;
                case ON_ASSOCIATION_CLOSE:
                    if (onClose)
                        storageService.syncFiles(system,storedFiles);
                    break;
            }
        } catch (IOException e) {
            LOG.error("File syncing failed:", e);
        }
    }
}
