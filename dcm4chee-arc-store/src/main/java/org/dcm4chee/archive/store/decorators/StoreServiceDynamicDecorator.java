package org.dcm4chee.archive.store.decorators;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.persistence.EntityManager;

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
import org.dcm4chee.conf.decorators.DynamicDecoratorWrapper;
import org.dcm4chee.storage.StorageContext;

/**
 * We have to be careful with scopes here. Currently the assumption is that all
 * services/dynadecorator beans are @ApplicationScoped
 */
@Decorator
public abstract class StoreServiceDynamicDecorator extends DynamicDecoratorWrapper<StoreService> implements StoreService {

    @Inject
    @Delegate
    StoreService delegate;
    
    public StoreSession createStoreSession(StoreService storeService) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).createStoreSession(storeService);
    }

    public StoreContext createStoreContext(StoreSession session) {
        return wrapWithDynamicDecorators(delegate).createStoreContext(session);
    }

    public void init(StoreSession session) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).init(session);
    }

    public void writeSpoolFile(StoreContext session, Attributes fmi, Attributes attrs) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).writeSpoolFile(session, fmi, attrs);
    }

    public void writeSpoolFile(StoreContext context, Attributes fmi, InputStream data) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).writeSpoolFile(context, fmi, data);
    }

    public void onClose(StoreSession session) {
        wrapWithDynamicDecorators(delegate).onClose(session);
    }

    public void store(StoreContext context) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).store(context);
    }

    public Path spool(StoreSession session, InputStream in, String suffix) throws IOException {
        return wrapWithDynamicDecorators(delegate).spool(session, in, suffix);
    }

    public void spool(StoreContext context) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).spool(context);
    }

    public void coerceAttributes(StoreContext context) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).coerceAttributes(context);
    }

    public StorageContext processFile(StoreContext context) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).processFile(context);
    }

    public void updateDB(StoreContext context) throws DicomServiceException {
        wrapWithDynamicDecorators(delegate).updateDB(context);
    }

    public org.dcm4chee.archive.entity.Instance findOrCreateInstance(EntityManager em, StoreContext context) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).findOrCreateInstance(em, context);
    }

    public Series findOrCreateSeries(EntityManager em, StoreContext context) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).findOrCreateSeries(em, context);
    }

    public Study findOrCreateStudy(EntityManager em, StoreContext context) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).findOrCreateStudy(em, context);
    }

    public Patient findOrCreatePatient(EntityManager em, StoreContext context) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).findOrCreatePatient(em, context);
    }

    public StoreAction instanceExists(EntityManager em, StoreContext context, org.dcm4chee.archive.entity.Instance instance) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).instanceExists(em, context, instance);
    }

    public void cleanup(StoreContext context) {
        wrapWithDynamicDecorators(delegate).cleanup(context);
    }

    public void fireStoreEvent(StoreContext context) {
        wrapWithDynamicDecorators(delegate).fireStoreEvent(context);
    }

    public StorageContext storeMetaData(StoreContext context) throws DicomServiceException {
        return wrapWithDynamicDecorators(delegate).storeMetaData(context);
    }

    public void beginProcessFile(StoreContext context) {
        wrapWithDynamicDecorators(delegate).beginProcessFile(context);
    }

    public void beginStoreMetadata(StoreContext context) {
        wrapWithDynamicDecorators(delegate).beginStoreMetadata(context);
    }

    public Instance adjustForNoneIOCM(Instance instanceToStore, StoreContext context) {
        return wrapWithDynamicDecorators(delegate).adjustForNoneIOCM(instanceToStore, context);
     }

}
