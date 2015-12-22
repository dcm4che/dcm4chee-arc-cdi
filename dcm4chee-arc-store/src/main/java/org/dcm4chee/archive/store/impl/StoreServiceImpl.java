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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.io.SAXTransformer.SetupTransformer;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.archive.monitoring.api.Monitored;
import org.dcm4chee.archive.patient.PatientSelectorFactory;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.store.StoreSessionClosed;
import org.dcm4chee.archive.util.ArchiveDeidentifier;
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
        if (groupID == null)
            throw new DicomServiceException(Status.OutOfResources,
                    "Storage System Group not configured");
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
                    "No writeable storage group configured");
        }

        // spool is in the same dir of the destination, to ease the move operation
        Path spoolingPath = Paths.get(system.getStorageSystemPath(), "spool");

        Path spoolDirectory;
        try {
            LOG.info("INIT spool storage within {}", spoolingPath);
            if (!Files.exists(spoolingPath))
                Files.createDirectories(spoolingPath);
            spoolDirectory = Files.createTempDirectory(spoolingPath, null);
        } catch (IOException e) {
            throw new DicomServiceException(Status.OutOfResources,
                    "Cannot create spool directory for group " +
                            system.getStorageSystemGroup().getGroupID() +
                            " in " + spoolingPath, e);
        }

        session.setSpoolStorageSystem(system);
        session.setSpoolDirectory(spoolDirectory);
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
        Path spoolDirectory = session.getSpoolDirectory();
        if (spoolDirectory != null) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(spoolDirectory)) {
                for (Path file : dirStream) {
                    try {
                        Files.delete(file);
                        LOG.info("{}: M-DELETE spool file - {}", session, file);
                    } catch (IOException e) {
                        LOG.warn("{}: Failed to M-DELETE spool file - {}", session,
                                file, e);
                    }
                }
                Files.delete(spoolDirectory);
                LOG.info("{}: M-DELETE spool directory - {}", session, spoolDirectory);
            } catch (IOException e) {
                LOG.warn("{}: Failed to M-DELETE spool directory - {}", session,
                        spoolDirectory, e);
            }
        }
    }

    private void writeSpoolFile(StoreContext context, Attributes fmi,
            Attributes ds, InputStream in) throws DicomServiceException {
        context.setFileMetainfo(fmi);
        context.setInputStream(in);
        if(ds != null)
            context.setOriginalAttributes(ds);
        try {
            fileSpooler.spool(context, true);
        } catch (IOException e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
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
        Attributes originalAttributes = context.getOriginalAttributes();
        StorageContext bulkdataContext = storageService.createStorageContext(bulkdataStorageSystem);
        String bulkdataRoot = calculatePath(bulkdataStorageSystem, originalAttributes);
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
                ((DicomOutputStream) out).writeDataset(fmi, originalAttributes);
            } catch (Exception e) {
                throw new DicomServiceException(Status.UnableToProcess, e);
            } finally {
                try {
                    SafeClose.close(out);
                    bulkdataContext.setFilePath(Paths.get(bulkdataPath));
                    bulkdataContext.setFileSize(Files.size(Paths.get(bulkdataStorageSystem.getStorageSystemPath(), bulkdataPath)));
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

        // try to call updateDB, eventually retries
        retry.retry(new RetryBean.Retryable<Void, DicomServiceException>() {
            @Override
            public Void call() throws DicomServiceException {
                storeServiceEJB.updateDB(context);
                return null;
            }
        });

        logCoercedAttributes(context);
    }

    private void logCoercedAttributes(StoreContext context) {
        StoreSession session = context.getStoreSession();
        boolean deident = session.getStoreParam().isDeIdentifyLogs();
        Attributes attrs = context.getCoercedOriginalAttributes();
        if (!attrs.isEmpty()) {
            Attributes newatts = new Attributes(context.getAttributes(), attrs.tags());
            LOG.info("{}: Coerced Attributes:\n{}New Attributes:\n{}", session,
                    deident ? attrs.toString(ArchiveDeidentifier.DEFAULT) : attrs,
                    deident ? newatts.toString(ArchiveDeidentifier.DEFAULT) : newatts);
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

        // we have to synchronize with the asynchronous bulkdata processing now
        StorageContext bulkdataContext = null;
        try {
            Future<StorageContext> bulkdataContextFuture = context.getBulkdataContext();
            if (bulkdataContextFuture != null) {
                bulkdataContext = bulkdataContextFuture.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        }

        if(bulkdataContext != null) {

            if (hasFileRefWithDigest(fileRefs, bulkdataContext.getFileDigest()))
                return StoreAction.IGNORE;

            if (context.getStoreSession().getArchiveAEExtension().isCheckNonDBAttributesOnStorage()) {
                String nodbAttrsDigest;
                try {
                    nodbAttrsDigest = noDBAttsDigest(bulkdataContext.getFilePath(), context.getStoreSession());
                } catch (IOException e) {
                    throw new DicomServiceException(Status.UnableToProcess, e);
                }

                // TODO if are only setting the no-db-attr-digest in this case, it will not always get saved!
                // TODO ... but it doesn't matter, because it seems we aren't storing it anyways at the moment
                context.setNoDBAttsDigest(nodbAttrsDigest);

                if (hasFileRefWithOtherAttsDigest(fileRefs, context.getNoDBAttsDigest())) {
                    return StoreAction.UPDATEDB;
                }
            }
        }

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
                for (Iterator<Location> iter = inst.getLocations().iterator(); iter.hasNext();) {
                    Location fileRef = iter.next();
                    // remove inst
                    fileRef.getInstances().remove(inst);
                    // no other instances referenced by location
                    if (fileRef.getInstances().isEmpty()) {
                        // delete
                        replaced.add(fileRef);
                    }
                }
                em.remove(inst);
            }
        } catch (NoResultException e) {
            context.setStoreAction(StoreAction.STORE);
        }

        Instance newInst = storeServiceEJB.createInstance(context);

        // delete replaced
        try {
            if (replaced.size()>0)
                locationManager.scheduleDelete(replaced, 0,false);
        } catch (Exception e) {
            LOG.error("StoreService : Error deleting replaced location - {}", e);
        }
        return service.adjustForNoneIOCM(newInst, context);
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

        Attributes originalAttributes = context.getOriginalAttributes();
        StorageContext metadataContext = storageService.createStorageContext(metadataStorage);
        String metadataRoot = calculatePath(metadataStorage, originalAttributes);
        String metadataPath = metadataRoot;
        int copies = 1;

        int bufferLength = metadataStorage.getBufferedOutputLength();
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

            Attributes metadata = new Attributes(originalAttributes.bigEndian(), originalAttributes.size());
            metadata.addWithoutBulkData(originalAttributes, BulkDataDescriptor.DEFAULT);

            out = new BufferedOutputStream(out, bufferLength);
            out = new DicomOutputStream(out, UID.ExplicitVRLittleEndian);
            ((DicomOutputStream)out).writeDataset(metadata.
                    createFileMetaInformation(UID.ExplicitVRLittleEndian), metadata);

            out.close();
            out = null;
        } catch (Exception e) {
            throw new DicomServiceException(Status.UnableToProcess, e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.error("Error closing out", e);
                }
            }
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
        } else {
            return null;
        }
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
                    storageService. syncFiles(system, storedFiles);
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
                    if (storedFiles.size()>=5 || onClose)
                        storageService.syncFiles(system, storedFiles);
                    break;
                case EVERY_25_STORE:
                    if (storedFiles.size()>=25 || onClose)
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
