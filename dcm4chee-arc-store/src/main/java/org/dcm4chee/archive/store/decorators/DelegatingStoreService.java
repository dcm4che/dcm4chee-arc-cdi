package org.dcm4chee.archive.store.decorators;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.conf.decorators.DelegatingService;
import org.dcm4chee.conf.decorators.DelegatingServiceImpl;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@DelegatingService
public class DelegatingStoreService extends DelegatingServiceImpl<StoreService> implements StoreService {

	public StoreSession createStoreSession(StoreService storeService) throws DicomServiceException {
        return getNextDecorator().createStoreSession(storeService);
    }

    public StoreContext createStoreContext(StoreSession session) {
        return getNextDecorator().createStoreContext(session);
    }

    public void initStorageSystem(StoreSession session) throws DicomServiceException {
        getNextDecorator().initStorageSystem(session);
    }

    public void initMetaDataStorageSystem(StoreSession session) throws DicomServiceException {
        getNextDecorator().initMetaDataStorageSystem(session);
    }

    public void initSpoolDirectory(StoreSession session) throws DicomServiceException {
        getNextDecorator().initSpoolDirectory(session);
    }

    public void writeSpoolFile(StoreContext session, Attributes fmi, Attributes attrs) throws DicomServiceException {
        getNextDecorator().writeSpoolFile(session, fmi, attrs);
    }

    public void writeSpoolFile(StoreContext context, Attributes fmi, InputStream data) throws DicomServiceException {
        getNextDecorator().writeSpoolFile(context, fmi, data);
    }

    public void parseSpoolFile(StoreContext context) throws DicomServiceException {
        getNextDecorator().parseSpoolFile(context);
    }

    public void onClose(StoreSession session) {
        getNextDecorator().onClose(session);
    }

    public void store(StoreContext context) throws DicomServiceException {
        getNextDecorator().store(context);
    }

    public Path spool(StoreSession session, InputStream in, String suffix) throws IOException {
        return getNextDecorator().spool(session, in, suffix);
    }

    public void coerceAttributes(StoreContext context) throws DicomServiceException {
        getNextDecorator().coerceAttributes(context);
    }

    public void processFile(StoreContext context) throws DicomServiceException {
        getNextDecorator().processFile(context);
    }

    public void updateDB(StoreContext context) throws DicomServiceException {
        getNextDecorator().updateDB(context);
    }

    public void updateDB(EntityManager em, StoreContext context) throws DicomServiceException {
        getNextDecorator().updateDB(em, context);
    }

    public Instance findOrCreateInstance(EntityManager em, StoreContext context) throws DicomServiceException {
        return getNextDecorator().findOrCreateInstance(em, context);
    }

    public Series findOrCreateSeries(EntityManager em, StoreContext context) throws DicomServiceException {
        return getNextDecorator().findOrCreateSeries(em, context);
    }

    public Study findOrCreateStudy(EntityManager em, StoreContext context) throws DicomServiceException {
        return getNextDecorator().findOrCreateStudy(em, context);
    }

    public Patient findOrCreatePatient(EntityManager em, StoreContext context) throws DicomServiceException {
        return getNextDecorator().findOrCreatePatient(em, context);
    }

    public StoreAction instanceExists(EntityManager em, StoreContext context, Instance instance) throws DicomServiceException {
        return getNextDecorator().instanceExists(em, context, instance);
    }

    public Instance createInstance(EntityManager em, StoreContext context) throws DicomServiceException {
        return getNextDecorator().createInstance(em, context);
    }

    public Series createSeries(EntityManager em, StoreContext context) throws DicomServiceException {
        return getNextDecorator().createSeries(em, context);
    }

    public Study createStudy(EntityManager em, StoreContext context) throws DicomServiceException {
        return getNextDecorator().createStudy(em, context);
    }

    public void updateInstance(EntityManager em, StoreContext context, Instance inst) throws DicomServiceException {
        getNextDecorator().updateInstance(em, context, inst);
    }

    public void updateSeries(EntityManager em, StoreContext context, Series series) throws DicomServiceException {
        getNextDecorator().updateSeries(em, context, series);
    }

    public void updateStudy(EntityManager em, StoreContext context, Study study) throws DicomServiceException {
        getNextDecorator().updateStudy(em, context, study);
    }

    public void updatePatient(EntityManager em, StoreContext context, Patient patient) {
        getNextDecorator().updatePatient(em, context, patient);
    }

    public void cleanup(StoreContext context) {
        getNextDecorator().cleanup(context);
    }

    public void fireStoreEvent(StoreContext context) {
        getNextDecorator().fireStoreEvent(context);
    }

    public void storeMetaData(StoreContext context) throws DicomServiceException {
        getNextDecorator().storeMetaData(context);
    }

}
