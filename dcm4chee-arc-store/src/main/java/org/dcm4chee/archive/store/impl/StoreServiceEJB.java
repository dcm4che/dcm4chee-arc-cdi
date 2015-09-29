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
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.*;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.issuer.IssuerService;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.store.NewStudyCreated;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.conf.StorageSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@Stateless
public class StoreServiceEJB {

    static Logger LOG = LoggerFactory.getLogger(StoreServiceEJB.class);

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @Inject
    @NewStudyCreated
    private Event<String> newStudyCreatedEvent;

    @Inject
    private LocationMgmt locationManager;

    @Inject
    private IssuerService issuerService;

    @Inject
    private CodeService codeService;

    @Inject
    private PatientService patientService;

    @Inject
    private Device device;

    public void updateDB(StoreContext context)
            throws DicomServiceException {

        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Instance instance = service.findOrCreateInstance(em, context);
        context.setInstance(instance);

        switch(context.getStoreAction()) {
            case IGNORE:
            case UPDATEDB:
                return;
            default:
                Collection<Location> locations = instance.getLocations(2);
                try {
                	findOrCreateStudyOnStorageGroup(context);
                    StorageContext metadataContext = context.getMetadataContext().get();
                    if (metadataContext != null) {
                        Location metadata = createMetadataLocation(context);
                        locations.add(metadata);
                    }

                    StorageContext bulkdataContext = context.getBulkdataContext().get();
                    if (bulkdataContext != null) {
                        Location bulkdata = createBulkdataLocation(context);
                        locations.add(bulkdata);
                        context.setFileRef(bulkdata);

                        updateRetrieveAETs(session, instance);
                        updateAvailability(session, instance);
                    }

                } catch (Exception e) {
                    throw new DicomServiceException(Status.UnableToProcess, e);
                }
        }
     }

    public Instance createInstance(StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        Instance inst = new Instance();
        inst.setSeries(service.findOrCreateSeries(em, context));
        inst.setConceptNameCode(singleCode(data, Tag.ConceptNameCodeSequence));
        inst.setVerifyingObservers(createVerifyingObservers(
                data.getSequence(Tag.VerifyingObserverSequence),
                storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields(), inst));
        inst.setContentItems(createContentItems(
                data.getSequence(Tag.ContentSequence), inst));
        inst.setRetrieveAETs(session.getStorageSystem().getStorageSystemGroup()
                .getRetrieveAETs());
        inst.setAvailability(session.getStorageSystem().getAvailability());
        inst.setAttributes(data,
                storeParam.getAttributeFilter(Entity.Instance),
                storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
        em.persist(inst);
        LOG.info("{}: Create {}", session, inst);
        return inst;
    }

    public Series createSeries(StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        Series series = new Series();
        series.setStudy(service.findOrCreateStudy(em, context));
        series.setInstitutionCode(singleCode(data, Tag.InstitutionCodeSequence));
        series.setRequestAttributes(createRequestAttributes(
                data.getSequence(Tag.RequestAttributesSequence),
                storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields(), series));
        series.setSourceAET(session.getRemoteAET());
        series.addCalledAET(session.getLocalAET());
        series.setAttributes(data,
                storeParam.getAttributeFilter(Entity.Series),
                storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
        em.persist(series);
        LOG.info("{}: Create {}", session, series);
        return series;
    }

    public Study createStudy(StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes attrs = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        Study study = new Study();
        study.setPatient(service.findOrCreatePatient(em, context));
        study.setProcedureCodes(codeList(attrs, Tag.ProcedureCodeSequence));
        study.setAttributes(attrs, storeParam.getAttributeFilter(Entity.Study),
                storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
        study.setIssuerOfAccessionNumber(findOrCreateIssuer(attrs
                .getNestedDataset(Tag.IssuerOfAccessionNumberSequence)));
        em.persist(study);
        LOG.info("{}: Create {}", session, study);
        newStudyCreatedEvent.fire(study.getStudyInstanceUID());
        return study;
    }

    private Location createBulkdataLocation(StoreContext context) throws InterruptedException, ExecutionException {

        StorageContext bulkdataContext = context.getBulkdataContext().get();
        if (bulkdataContext == null) return null;

        StorageSystem bulkdataSystem = bulkdataContext.getStorageSystem();

        Device source = context.getStoreSession().getSourceDevice();
        TimeZone timezone = source!=null ? source.getTimeZoneOfDevice() : null;
        if (timezone == null) timezone = context.getStoreSession().getDevice().getTimeZoneOfDevice();
        if (timezone == null) timezone = TimeZone.getDefault();

        Location bulkdataLocation = new Location.Builder()
                .timeZone(timezone.getID())
                .storageSystemGroupID(bulkdataSystem.getStorageSystemGroup().getGroupID())
                .storageSystemID(bulkdataSystem.getStorageSystemID())
                .storagePath(bulkdataContext.getFilePath().toString())
                .digest(bulkdataContext.getFileDigest())
                .otherAttsDigest(null)
                .size(bulkdataContext.getFileSize())
                .transferSyntaxUID(context.getTransferSyntax())
                .build();

        em.persist(bulkdataLocation);
        LOG.info("{}: Create {}", context.getStoreSession(), bulkdataLocation);
        return bulkdataLocation;
    }

    private Location createMetadataLocation(StoreContext context) throws InterruptedException, ExecutionException  {

        StorageContext metadataContext = context.getMetadataContext().get();
        if (metadataContext == null) return null;

        StorageSystem metadataSystem = metadataContext.getStorageSystem();

        Device source = context.getStoreSession().getSourceDevice();
        TimeZone timezone = source!=null ? source.getTimeZoneOfDevice() : null;
        if (timezone == null) timezone = context.getStoreSession().getDevice().getTimeZoneOfDevice();
        if (timezone == null) timezone = TimeZone.getDefault();

        Location metadataLocation = new Location.Builder()
                .timeZone(timezone.getID())
                .storageSystemGroupID(metadataSystem.getStorageSystemGroup().getGroupID())
                .storageSystemID(metadataSystem.getStorageSystemID())
                .storagePath(metadataContext.getFilePath().toString())
                .transferSyntaxUID(UID.ExplicitVRLittleEndian)
                .withoutBulkdata(true)
                .build();

        em.persist(metadataLocation);
        LOG.info("{}: Create {}", context.getStoreSession(), metadataLocation);
        return metadataLocation;
    }

    private Collection<ContentItem> createContentItems(Sequence seq,
            Instance inst) {
        if (seq == null || seq.isEmpty())
            return null;

        Collection<ContentItem> list = new ArrayList<>(seq.size());
        for (Attributes item : seq) {
            String type = item.getString(Tag.ValueType);
            ContentItem contentItem = null;
            if ("CODE".equals(type)) {
                contentItem = new ContentItem(item.getString(
                        Tag.RelationshipType).toUpperCase(), singleCode(item,
                        Tag.ConceptNameCodeSequence), singleCode(item,
                        Tag.ConceptCodeSequence));
                list.add(contentItem);
            } else if ("TEXT".equals(type)) {
                contentItem = new ContentItem(item.getString(
                        Tag.RelationshipType).toUpperCase(), singleCode(item,
                        Tag.ConceptNameCodeSequence), item.getString(
                        Tag.TextValue, "*"));
            }
            if (contentItem != null) {
                contentItem.setInstance(inst);
                list.add(contentItem);
            }
        }
        return list;
    }

    private Collection<RequestAttributes> createRequestAttributes(Sequence seq,
            FuzzyStr fuzzyStr, String nullValue, Series series) {
        if (seq == null || seq.isEmpty())
            return null;

        ArrayList<RequestAttributes> list = new ArrayList<>(
                seq.size());
        for (Attributes item : seq) {
            RequestAttributes request = new RequestAttributes(
                    item,
                    findOrCreateIssuer(item
                            .getNestedDataset(Tag.IssuerOfAccessionNumberSequence)),
                    fuzzyStr, nullValue);
            request.setSeries(series);
            list.add(request);
        }
        return list;
    }

    private Issuer findOrCreateIssuer(Attributes item) {
        return item != null ? issuerService.findOrCreate(new Issuer(item))
                : null;
    }

    private void findOrCreateStudyOnStorageGroup(StoreContext context) {
        locationManager.findOrCreateStudyOnStorageGroup(context.getInstance()
                .getSeries().getStudy(), context.getStoreSession()
                .getStorageSystem().getStorageSystemGroup().getGroupID());
    }

    public void updateStudy(StoreContext context, Study study) {
        StoreSession session = context.getStoreSession();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        study.clearQueryAttributes();
        AttributeFilter studyFilter = storeParam
                .getAttributeFilter(Entity.Study);
        Attributes studyAttrs = study.getAttributes();
        Attributes modified = new Attributes();
        // check if trashed
        if (isRejected(study)) {
            em.remove(study.getAttributesBlob());
            study.setAttributes(new Attributes(data), studyFilter,
                    storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
        } else {
            if (!context.isFetch()
                    && !session.getLocalAET().equals(
                       device.getDeviceExtension(ArchiveDeviceExtension.class).getFetchAETitle())
                    && Utils.updateAttributes(studyAttrs, data, modified, studyFilter,
                       MetadataUpdateStrategy.COERCE_MERGE)) {
                study.setAttributes(studyAttrs, studyFilter,
                        storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
                LOG.info("{}: Update {}:\n{}\nmodified:\n{}", session, study,
                        studyAttrs, modified);
            }
        }
        if (!context.isFetch()
                && !session.getLocalAET().equals(
                device.getDeviceExtension(
                        ArchiveDeviceExtension.class)
                        .getFetchAETitle()))
            updatePatient(context, study.getPatient());
    }

    public void updatePatient(StoreContext context, Patient patient) {
        StoreSession session = context.getStoreSession();
        patientService.updatePatientByCStore(patient, context.getAttributes(),
                session.getStoreParam());
    }

    public void updateSeries(StoreContext context, Series series) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        series.clearQueryAttributes();
        Attributes seriesAttrs = series.getAttributes();
        AttributeFilter seriesFilter = storeParam
                .getAttributeFilter(Entity.Series);
        Attributes modified = new Attributes();
        series.addCalledAET(session.getLocalAET());
        // check if trashed
        if (isRejected(series)) {
            em.remove(series.getAttributesBlob());
            series.setAttributes(new Attributes(data), seriesFilter,
                    storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
        } else {
            if (!context.isFetch()
                    && !session.getLocalAET().equals(
                       device.getDeviceExtension(ArchiveDeviceExtension.class).getFetchAETitle())
                    && Utils.updateAttributes(seriesAttrs, data, modified, seriesFilter,
                       MetadataUpdateStrategy.COERCE_MERGE)) {
                series.setAttributes(seriesAttrs, seriesFilter,
                        storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
                LOG.info("{}: Update {}:\n{}\nmodified:\n{}", session, series,
                        seriesAttrs, modified);
            }
        }
        updateStudy(context, series.getStudy());
    }

    public void updateInstance(StoreContext context, Instance inst) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        Attributes data = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        Attributes instAttrs = inst.getAttributes();
        AttributeFilter instFilter = storeParam
                .getAttributeFilter(Entity.Instance);
        Attributes modified = new Attributes();
        if (!context.isFetch()
                && !session.getLocalAET().equals(
                    device.getDeviceExtension(ArchiveDeviceExtension.class).getFetchAETitle())
                && Utils.updateAttributes(instAttrs, data, modified, instFilter,
                   MetadataUpdateStrategy.OVERWRITE)) {
            inst.setAttributes(data, instFilter, storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
            LOG.info("{}: {}:\n{}\nmodified:\n{}", session, inst, instAttrs,
                    modified);
        }
        updateSeries(context, inst.getSeries());
    }

    private Collection<Code> codeList(Attributes attrs, int seqTag) {
        Sequence seq = attrs.getSequence(seqTag);
        if (seq == null || seq.isEmpty())
            return Collections.emptyList();

        ArrayList<Code> list = new ArrayList<>(seq.size());
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

    private Collection<VerifyingObserver> createVerifyingObservers(
            Sequence seq, FuzzyStr fuzzyStr, String nullValue, Instance instance) {
        if (seq == null || seq.isEmpty())
            return null;

        ArrayList<VerifyingObserver> list = new ArrayList<>(
                seq.size());
        for (Attributes item : seq) {
            VerifyingObserver observer = new VerifyingObserver(item, fuzzyStr, nullValue);
            observer.setInstance(instance);
            list.add(observer);
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

    private void updateRetrieveAETs(StoreSession session, Instance instance) {
        ArrayList<String> retrieveAETs = new ArrayList<>();
        retrieveAETs.addAll(Arrays.asList(session.getStorageSystem()
                .getStorageSystemGroup().getRetrieveAETs()));

        for (String aet : instance.getRetrieveAETs())
            if (!retrieveAETs.contains(aet))
                retrieveAETs.add(aet);
        String[] retrieveAETsArray = new String[retrieveAETs.size()];
        instance.setRetrieveAETs(retrieveAETs.toArray(retrieveAETsArray));
    }

    private void updateAvailability(StoreSession session, Instance instance) {
        if (session.getStorageSystem().getAvailability().ordinal() < instance
                .getAvailability().ordinal())
            instance.setAvailability(session.getStorageSystem()
                    .getAvailability());
    }

    private boolean isRejected(Study study) {
        if(study.isRejected()) {
            study.setRejected(false);
            return true;
        }
        return false;
    }

    private boolean isRejected(Series series) {
        if(series.isRejected()) {
            series.setRejected(false);
            return true;
        }
        return false;
    }

}
