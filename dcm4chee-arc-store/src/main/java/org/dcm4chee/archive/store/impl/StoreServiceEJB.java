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

import org.dcm4che3.data.*;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.*;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.issuer.IssuerService;
import org.dcm4chee.archive.locationmgmt.LocationMgmt;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.store.NewStudyCreated;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.store.hooks.PersonNameManagerHook;
import org.dcm4chee.archive.util.ArchiveDeidentifier;
import org.dcm4chee.hooks.Hooks;
import org.dcm4chee.hooks.Limit;
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

    @PersistenceContext(name = "dcm4chee-arc", unitName="dcm4chee-arc")
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

    @Inject
    @Limit(min=1, max=1)
    private Hooks<PersonNameManagerHook> personNameSelector;

    public void updateDB(StoreContext context)
            throws DicomServiceException {

        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Instance instance = service.findOrCreateInstance(em, context);
        context.setInstance(instance);

        if (context.getStoreAction() != StoreAction.IGNORE &&
                context.getStoreAction() != StoreAction.UPDATEDB) {
            try {
                findOrCreateStudyOnStorageGroup(context);
                Future<StorageContext> metadataContextFuture = context.getMetadataContext();
                if (metadataContextFuture != null && metadataContextFuture.get() != null) {
                    Location metadata = createMetadataLocation(context);
                    metadata.addInstance(instance);
                }

                Future<StorageContext> bulkdataContextFuture = context.getBulkdataContext();
                if (bulkdataContextFuture != null && bulkdataContextFuture.get() != null) {
                    Location bulkdata = createBulkdataLocation(context);
                    bulkdata.addInstance(instance);
                    context.setFileRef(bulkdata);

                    updateRetrieveAETs(session, instance);
                    updateAvailability(session, instance);
                }

            } catch (Exception e) {
                throw new DicomServiceException(Status.UnableToProcess, e);
            }
        }

        calculateImplicitlyCoercedAttributes(context);
    }

    private void calculateImplicitlyCoercedAttributes(StoreContext context) {
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
            context.setCoercedOriginalAttributes(modified);
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
        inst.setVerifyingObservers(createVerifyingObservers(context,
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
        //update time on db
        updateInstanceTime(inst, device, context);
        em.persist(inst);
        LOG.info("{}: Create {}", session, inst);
        return inst;
    }

    public Series createSeries(StoreContext context)
            throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes attrs = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        String nullValue = session.getStoreParam().getNullValueForQueryFields();
        FuzzyStr fuzzyStr = session.getStoreParam().getFuzzyStr();
        Series series = new Series();
        series.setStudy(service.findOrCreateStudy(em, context));
        series.setPerformingPhysicianName(personNameSelector.iterator().next().findOrCreate(
                Tag.PerformingPhysicianName, attrs, fuzzyStr, nullValue));
        series.setInstitutionCode(singleCode(attrs, Tag.InstitutionCodeSequence));
        series.setRequestAttributes(createRequestAttributes(
                attrs.getSequence(Tag.RequestAttributesSequence),
                storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields(), series));
        series.setSourceAET(session.getRemoteAET());
        series.addCalledAET(session.getLocalAET());
        series.setAttributes(attrs,
                storeParam.getAttributeFilter(Entity.Series),
                storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
        //update time on db
        updateSeriesTime(series, device, context);
        em.persist(series);
        LOG.info("{}: Create {}", session, series);
        return series;
    }

    public Study createStudy(StoreContext context) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        StoreService service = session.getStoreService();
        Attributes attrs = context.getAttributes();
        AttributeFilter studyFilter = session.getStoreParam().getAttributeFilter(Entity.Study);
        String nullValue = session.getStoreParam().getNullValueForQueryFields();
        FuzzyStr fuzzyStr = session.getStoreParam().getFuzzyStr();
        Study study = new Study();
        study.setPatient(service.findOrCreatePatient(em, context));
        study.setReferringPhysicianName(personNameSelector.iterator().next().findOrCreate(
                Tag.ReferringPhysicianName, attrs, fuzzyStr, nullValue));
        study.setProcedureCodes(codeList(attrs, Tag.ProcedureCodeSequence));
        study.setAttributes(attrs, studyFilter, fuzzyStr, nullValue);
        study.setIssuerOfAccessionNumber(findOrCreateIssuer(attrs
                .getNestedDataset(Tag.IssuerOfAccessionNumberSequence)));
        //update time on db
        updateStudyTime(study, device, context);
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
            FuzzyStr fuzzyStr, String nullValue, Series series) throws DicomServiceException {
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
            request.setRequestingPhysician(personNameSelector.iterator().next().findOrCreate(
                    Tag.RequestingPhysician, item, fuzzyStr, nullValue));
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

    public void updateStudy(StoreContext context, Study study) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        Attributes attrs = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        String nullValue = session.getStoreParam().getNullValueForQueryFields();
        FuzzyStr fuzzyStr = session.getStoreParam().getFuzzyStr();
        study.clearQueryAttributes();
        AttributeFilter studyFilter = storeParam.getAttributeFilter(Entity.Study);
        Attributes studyAttrs = study.getAttributes();
        Attributes modified = new Attributes();
        // check if trashed
        if (isRejected(study)) {
            em.remove(study.getAttributesBlob());
            study.setAttributes(new Attributes(attrs), studyFilter, fuzzyStr, nullValue);
        } else {
            if (!isFetch(context, session)) {

                boolean attrs_updated = Utils.updateAttributes(studyAttrs, attrs, modified, studyFilter,
                        MetadataUpdateStrategy.COERCE_MERGE);

                if (attrs_updated) {
                    study.setAttributes(studyAttrs, studyFilter, fuzzyStr, nullValue);
                    boolean deident = storeParam.isDeIdentifyLogs();
                    LOG.info("{}: Update {}:\n{}\nmodified:\n{}", session, study,
                            deident ? studyAttrs.toString(ArchiveDeidentifier.DEFAULT) : studyAttrs,
                            deident ? modified.toString(ArchiveDeidentifier.DEFAULT) : modified);
                }

                study.setReferringPhysicianName(personNameSelector.iterator().next()
                        .update(study.getReferringPhysicianName(),
                                Tag.ReferringPhysicianName, attrs, fuzzyStr, nullValue));
            }
        }
        //update time on db
        updateStudyTime(study, device, context);
        updatePatient(context, study.getPatient());
    }

    public void updatePatient(StoreContext context, Patient patient) {
        StoreSession session = context.getStoreSession();

        if (!isFetch(context, session)) {
            patientService.updatePatientByCStore(patient, context.getAttributes(),
                    session.getStoreParam());
        }
    }

    public void updateSeries(StoreContext context, Series series) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        Attributes attrs = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        String nullValue = session.getStoreParam().getNullValueForQueryFields();
        FuzzyStr fuzzyStr = session.getStoreParam().getFuzzyStr();
        series.clearQueryAttributes();
        Attributes seriesAttrs = series.getAttributes();
        AttributeFilter seriesFilter = storeParam.getAttributeFilter(Entity.Series);
        Attributes modified = new Attributes();
        series.addCalledAET(session.getLocalAET());
        // check if trashed
        if (isRejected(series)) {
            em.remove(series.getAttributesBlob());
            series.setAttributes(new Attributes(attrs), seriesFilter, fuzzyStr, nullValue);
        } else {
            if (!isFetch(context, session)) {

                boolean attrs_updated = Utils.updateAttributes(seriesAttrs, attrs, modified, seriesFilter,
                        MetadataUpdateStrategy.COERCE_MERGE);

                if (attrs_updated) {
                    series.setAttributes(seriesAttrs, seriesFilter, fuzzyStr, nullValue);
                    boolean deident = storeParam.isDeIdentifyLogs();
                    LOG.info("{}: Update {}:\n{}\nmodified:\n{}", session, series,
                            deident ? seriesAttrs.toString(ArchiveDeidentifier.DEFAULT) : seriesAttrs,
                            deident ? modified.toString(ArchiveDeidentifier.DEFAULT) : modified);
                }

                series.setPerformingPhysicianName(personNameSelector.iterator().next()
                        .update(series.getPerformingPhysicianName(),
                                Tag.PerformingPhysicianName, attrs, fuzzyStr, nullValue));
            }
        }
        //update time on db
        updateSeriesTime(series, device, context);
        updateStudy(context, series.getStudy());
    }

    public void updateInstance(StoreContext context, Instance inst) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        Attributes attrs = context.getAttributes();
        StoreParam storeParam = session.getStoreParam();
        Attributes instAttrs = inst.getAttributes();
        AttributeFilter instFilter = storeParam
                .getAttributeFilter(Entity.Instance);
        Attributes modified = new Attributes();

        if (!isFetch(context, session)) {

            boolean attrs_updated = Utils.updateAttributes(instAttrs, attrs, modified, instFilter,
                    MetadataUpdateStrategy.COERCE_MERGE);

            if (attrs_updated) {
                inst.setAttributes(attrs, instFilter, storeParam.getFuzzyStr(), storeParam.getNullValueForQueryFields());
                LOG.info("{}: {}:\n{}\nmodified:\n{}", session, inst, instAttrs,
                        modified);
            }

        }
        //update time on db
        updateInstanceTime(inst, device, context);
        updateSeries(context, inst.getSeries());
    }

    private boolean isFetch(StoreContext context, StoreSession session) {
        return  context.isFetch()
                ||
                session.getLocalAET().equals(device.getDeviceExtension(ArchiveDeviceExtension.class).getFetchAETitle());
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

    private Collection<VerifyingObserver> createVerifyingObservers(StoreContext context,
            Sequence seq, FuzzyStr fuzzyStr, String nullValue, Instance instance) {
        if (seq == null || seq.isEmpty())
            return null;

        ArrayList<VerifyingObserver> list = new ArrayList<>(
                seq.size());
        for (Attributes item : seq) {
            VerifyingObserver observer = new VerifyingObserver(item, fuzzyStr, nullValue);
            observer.setInstance(instance);
            //update time on db
            updateVerifyingObserverTime(observer,device,context);
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

    private void updateInstanceTime(Instance inst, Device arcDevice, StoreContext ctx) {
        if(!arcDevice.getDeviceExtension(ArchiveDeviceExtension.class).isDisableTimeZoneSupport()) {
            Attributes temp = getModifiedAttributes(arcDevice, ctx
                    , Tag.ContentDateAndTime
                    , inst.getAttributes()
                    .getDate(Tag.ContentDateAndTime));
            Date dt = temp.getDate(Tag.ContentDateAndTime);
            inst.setContentDateTime(dt);
        }
    }

    private void updateSeriesTime(Series series, Device arcDevice, StoreContext ctx) {
        if(!arcDevice.getDeviceExtension(ArchiveDeviceExtension.class).isDisableTimeZoneSupport()) {
            Attributes temp = getModifiedAttributes(arcDevice, ctx
                    , Tag.PerformedProcedureStepStartDateAndTime
                    , series.getAttributes()
                    .getDate(Tag.PerformedProcedureStepStartDateAndTime));
            Date dt = temp.getDate(Tag.PerformedProcedureStepStartDateAndTime);
            series.setPerformedProcedureStepStartDateTime(dt);
        }
    }

    private void updateStudyTime(Study study, Device arcDevice, StoreContext ctx) {
        if(!arcDevice.getDeviceExtension(ArchiveDeviceExtension.class).isDisableTimeZoneSupport()) {
            Attributes temp = getModifiedAttributes(arcDevice, ctx
                    , Tag.StudyDateAndTime
                    , study.getAttributes()
                    .getDate(Tag.StudyDateAndTime));
            Date dt = temp.getDate(Tag.StudyDateAndTime);
            study.setStudyDateTime(dt);
        }
    }

    private void updateVerifyingObserverTime(VerifyingObserver observer, Device arcDevice, StoreContext ctx) {
        if(!arcDevice.getDeviceExtension(ArchiveDeviceExtension.class).isDisableTimeZoneSupport()) {
            Attributes temp = getModifiedAttributes(arcDevice, ctx
                    , Tag.VerificationDateTime
                    , observer.getVerificationDateTime());

            observer.setVerificationDateTime(temp.getDate(Tag.VerificationDateTime));
        }
    }

    private Attributes getModifiedAttributes(Device arcDevice, StoreContext ctx, long tagDateAndTime, Date date) {
        if(date == null)
            return new Attributes();

        Attributes temp = new Attributes();
        temp.setDate(tagDateAndTime, new DatePrecision(), date);
        ArchiveDeviceExtension arcDevExt = arcDevice.getDeviceExtension(ArchiveDeviceExtension.class);
        temp.setDefaultTimeZone(ctx.getSourceTimeZone());
        temp.setTimezone(arcDevExt.getDataBaseTimeZone());
        temp.remove(Tag.TimezoneOffsetFromUTC);
        return temp;
    }
}
