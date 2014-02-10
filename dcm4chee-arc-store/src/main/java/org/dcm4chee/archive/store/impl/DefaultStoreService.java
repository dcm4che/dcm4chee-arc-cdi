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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.xml.transform.Templates;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomInputStream.IncludeBulkData;
import org.dcm4che.io.SAXTransformer;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.Status;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.soundex.FuzzyStr;
import org.dcm4che.util.TagUtils;
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
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.RequestAttributes;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.VerifyingObserver;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.store.StoreAction;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.event.StoreCompletedEvent;
import org.dcm4chee.archive.store.event.StoreEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
@Named("DefaultStoreService")
@ApplicationScoped
public class DefaultStoreService implements StoreService {

    private static Logger LOG = LoggerFactory
            .getLogger(DefaultStoreService.class);

    @Inject
    private PatientService patientService;

    @Inject
    private CodeService codeService;

    @Inject
    private FileSystemEJB fileSystemEJB;

    @Inject
    private StoreServiceEJB storeServiceEJB;

    @Inject
    Event<StoreEvent> storeEvent;

    @Inject
    Event<StoreCompletedEvent> storeCompletedEvent;

    @Override
    public FileSystem selectFileSystem(Object source, ArchiveAEExtension arcAE)
            throws DicomServiceException {
        String groupID = arcAE.getFileSystemGroupID();
        FileSystem fs = fileSystemEJB.findCurrentFileSystem(groupID,
                arcAE.getInitFileSystemURI());
        if (fs == null)
            throw new DicomServiceException(Status.OutOfResources,
                    "No writeable File System in File System Group " + groupID);
        return fs;
    }

    @Override
    public StoreContext createStoreContext(StoreService service, Object source,
            String sourceAET, ArchiveAEExtension arcAE, FileSystem fs,
            Path file, byte[] digest) {
        return new DefaultStoreContext(service, source, sourceAET, arcAE, fs, file, digest);
    }

    @Override
    public void parseAttributes(StoreContext storeContext)
            throws DicomServiceException {
        try (DicomInputStream in = new DicomInputStream(storeContext.getFile()
                .toFile());) {
            in.setIncludeBulkData(IncludeBulkData.URI);
            Attributes fmi = in.readFileMetaInformation();
            Attributes ds = in.readDataset(-1, -1);
            storeContext.setTransferSyntax(fmi != null ? fmi
                    .getString(Tag.TransferSyntaxUID)
                    : UID.ImplicitVRLittleEndian);
            storeContext.setAttributes(ds);
        } catch (IOException e) {
            throw new DicomServiceException(DATA_SET_NOT_PARSEABLE);
        }
    }

    @Override
    public void coerceAttributes(StoreContext storeContext)
            throws DicomServiceException {
        try {
            ArchiveAEExtension arcAE = storeContext.getArchiveAEExtension();
            Attributes attrs = storeContext.getAttributes();
            Attributes modified = storeContext.getCoercedAttributes();
            Templates tpl = arcAE.getAttributeCoercionTemplates(
                    storeContext.getSOPClassUID(),
                    Dimse.C_STORE_RQ, TransferCapability.Role.SCP, 
                    storeContext.getSourceAET());
            if (tpl != null)
                attrs.update(SAXTransformer.transform(attrs, tpl, false, false),
                        modified);
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure);
        }
   }

    @Override
    public void moveFile(StoreContext storeContext)
            throws DicomServiceException {
        try {
            storeContext.setFile(move(storeContext.getFile(),
                    storeContext.getStorePath()));
        } catch (IOException e) {
            throw new DicomServiceException(Status.ProcessingFailure);
        }
    }

    private Path move(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        for (;;) {
            try {
                return Files.move(source, target);
            } catch (FileAlreadyExistsException e) {
                target = target
                        .resolveSibling(target.getFileName().toString() + '-');
            }
        }
    }

    @Override
    public void updateDB(StoreContext storeContext)
            throws DicomServiceException {
        storeServiceEJB.updateDB(storeContext);
    }

    @Override
    public void updateDB(EntityManager em, StoreContext storeContext)
            throws DicomServiceException {
        StoreService service = storeContext.getService();
        Instance instance = service.findInstance(em, storeContext);
        Attributes attrs = storeContext.getAttributes();
        Attributes attrsAfterDBUpdate = new Attributes(attrs);
        storeContext.setAttributesAfterUpdateDB(attrsAfterDBUpdate);
        Attributes coercedAtts = 
                new Attributes(storeContext.getCoercedAttributes());
        storeContext.setCoercedAttributesAfterUpdateDB(coercedAtts);
        storeContext.setStoreAction(StoreAction.STORE);
        if (instance == null || service.replaceInstance(em, storeContext, instance)) {
            Series series = service.findSeries(em, storeContext);
            if (series == null) {
                Study study = service.findStudy(em, storeContext);
                if (study == null) {
                    Patient patient = service.findPatient(em, storeContext);
                    if (patient == null) {
                        patient = service.createPatient(em, storeContext);
                    } else {
                        service.updatePatient(em, storeContext, patient);
                        coerceAttributes(attrsAfterDBUpdate, patient, coercedAtts);
                    }
                    study = service.createStudy(em, storeContext, patient);
                } else {
                    Patient patient = study.getPatient();
                    service.updatePatient(em, storeContext, patient);
                    service.updateStudy(em, storeContext, study);
                    coerceAttributes(attrsAfterDBUpdate, study, coercedAtts);
                }
                series = service.createSeries(em, storeContext, study);
            } else {
                Study study = series.getStudy();
                Patient patient = study.getPatient();
                service.updatePatient(em, storeContext, patient);
                service.updateStudy(em, storeContext, study);
                service.updateSeries(em, storeContext, series);
                coerceAttributes(attrsAfterDBUpdate, series, coercedAtts);
            }
            instance = service.createInstance(em, storeContext, series, coercedAtts);
            storeContext.setInstance(instance);
        } else if (service.restoreInstance(em, storeContext, instance)) {
            coerceAttributes(attrsAfterDBUpdate, instance, coercedAtts);
            storeContext.setStoreAction(StoreAction.RESTORE);
        } else {
           storeContext.setStoreAction(StoreAction.IGNORE);
           coerceAttributes(attrsAfterDBUpdate, instance, coercedAtts);
           return;
        }
        FileRef fileRef = service.createFileRef(em, storeContext, instance);
        storeContext.setFileRef(fileRef);
    }

    private void coerceAttributes(Attributes attrs, Patient patient,
            Attributes result) {
        attrs.update(patient.getAttributes(), result);
    }

    private void coerceAttributes(Attributes attrs, Study study,
            Attributes result) {
        coerceAttributes(attrs, study.getPatient(), result);
        attrs.update(study.getAttributes(), result);
    }

    private void coerceAttributes(Attributes attrs, Series series,
            Attributes result) {
        coerceAttributes(attrs, series.getStudy(), result);
        attrs.update(series.getAttributes(), result);
    }

    private void coerceAttributes(Attributes attrs, Instance inst,
            Attributes result) {
        coerceAttributes(attrs, inst.getSeries(), result);
        attrs.update(inst.getAttributes(), result);
        result.remove(Tag.OriginalAttributesSequence);
    }

    @Override
    public Instance findInstance(EntityManager em, StoreContext storeContext)
            throws DicomServiceException {
        try {
            return em.createNamedQuery(
                    Instance.FIND_BY_SOP_INSTANCE_UID, Instance.class)
                 .setParameter(1, storeContext.getSOPInstanceUID())
                 .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    @Override
    public Series findSeries(EntityManager em, StoreContext storeContext)
            throws DicomServiceException {
        try {
            return em.createNamedQuery(
                    Series.FIND_BY_SERIES_INSTANCE_UID, Series.class)
                 .setParameter(1, storeContext.getSeriesInstanceUID())
                 .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    @Override
    public Study findStudy(EntityManager em, StoreContext storeContext)
            throws DicomServiceException {
        try {
            return em.createNamedQuery(
                    Study.FIND_BY_STUDY_INSTANCE_UID, Study.class)
                 .setParameter(1, storeContext.getStudyInstanceUID())
                 .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    @Override
    public Patient findPatient(EntityManager em, StoreContext storeContext)
            throws DicomServiceException {
        try {
            return patientService.findPatientFollowMerged(storeContext
                    .getAttributes(), new IDPatientSelector());
        } catch (NonUniquePatientException e) {
            LOG.info("Could not find unique Patient Record for received Study - create new Patient Record", e);
            return null;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    @Override
    public Patient createPatient(EntityManager em, StoreContext storeContext) {
        return patientService.createPatient(
                storeContext.getAttributes(), storeContext.getStoreParam());
    }

    @Override
    public Study createStudy(EntityManager em, StoreContext storeContext,
            Patient patient) {
        Attributes attrs = storeContext.getAttributes();
        StoreParam storeParam = storeContext.getStoreParam();
        FileSystem fs = storeContext.getFileSystem();
        Study study = new Study();
        study.setPatient(patient);
        study.setProcedureCodes(codeList(attrs, Tag.ProcedureCodeSequence));
        study.setModalitiesInStudy(attrs.getString(Tag.Modality, null));
        study.setSOPClassesInStudy(attrs.getString(Tag.SOPClassUID, null));
        study.setRetrieveAETs(storeParam.getRetrieveAETs());
        study.setExternalRetrieveAET(storeParam.getExternalRetrieveAET());
        study.setAvailability(fs.getAvailability());
        study.setAttributes(attrs, storeParam.getAttributeFilter(Entity.Study),
                storeParam.getFuzzyStr());
        em.persist(study);
        return study;
    }

    @Override
    public Series createSeries(EntityManager em, StoreContext storeContext,
            Study study) {
        Attributes data = storeContext.getAttributes();
        StoreParam storeParam = storeContext.getStoreParam();
        FileSystem fs = storeContext.getFileSystem();
        Series series = new Series();
        series.setStudy(study);
        series.setInstitutionCode(singleCode(data, Tag.InstitutionCodeSequence));
//        series.setScheduledProcedureSteps(getScheduledProcedureSteps(
//                data.getSequence(Tag.RequestAttributesSequence), data,
//                study.getPatient(), storeParam));
        series.setRequestAttributes(createRequestAttributes(
                data.getSequence(Tag.RequestAttributesSequence),
                storeParam.getFuzzyStr()));
        series.setSourceAET(storeContext.getReceivingAETitle());
        series.setRetrieveAETs(storeParam.getRetrieveAETs());
        series.setExternalRetrieveAET(storeParam.getExternalRetrieveAET());
        series.setAvailability(fs.getAvailability());
        series.setAttributes(data,
                storeParam.getAttributeFilter(Entity.Series),
                storeParam.getFuzzyStr());
        em.persist(series);
        return series;
    }

    @Override
    public Instance createInstance(EntityManager em, StoreContext storeContext,
            Series series, Attributes coercedAttrs) {
        Attributes data = storeContext.getAttributes();
        StoreParam storeParam = storeContext.getStoreParam();
        FileSystem fs = storeContext.getFileSystem();
        if (!coercedAttrs.isEmpty() && storeParam.isStoreOriginalAttributes()) {
            Attributes item = new Attributes(4);
            Sequence origAttrsSeq = data.ensureSequence(
                    Tag.OriginalAttributesSequence, 1);
            origAttrsSeq.add(item);
            item.setDate(Tag.AttributeModificationDateTime, VR.DT, new Date());
            item.setString(Tag.ModifyingSystem, VR.LO,
                    storeParam.getModifyingSystem());
            item.setString(Tag.SourceOfPreviousValues, VR.LO,
                    storeContext.getSourceAET());
            item.newSequence(Tag.ModifiedAttributesSequence, 1).add(
                    coercedAttrs);
        }
        Instance inst = new Instance();
        inst.setSeries(series);
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
        return inst;
    }

    @Override
    public FileRef createFileRef(EntityManager em, StoreContext storeContext,
            Instance instance) {
        FileSystem fs = storeContext.getFileSystem();
        Path filePath = storeContext.getFile();
        FileRef fileRef = new FileRef(
                fs,
                unixFilePath(fs.getPath(), filePath),
                storeContext.getTransferSyntax(),
                filePath.toFile().length(),
                TagUtils.toHexString(storeContext.getDigest()));
        fileRef.setInstance(instance);
        em.persist(fileRef);
        return fileRef;
    }


    private String unixFilePath(Path fsPath, Path filePath) {
        return fsPath.relativize(filePath).toString()
            .replace(File.separatorChar, '/');
    }

//    private Collection<ScheduledProcedureStep> getScheduledProcedureSteps(
//            Sequence requestAttrsSeq, Attributes data, Patient patient,
//            StoreParam storeParam) {
//        if (requestAttrsSeq == null)
//            return null;
//        ArrayList<ScheduledProcedureStep> list = new ArrayList<ScheduledProcedureStep>(
//                requestAttrsSeq.size());
//        for (Attributes requestAttrs : requestAttrsSeq) {
//            if (requestAttrs.containsValue(Tag.ScheduledProcedureStepID)
//                    && requestAttrs.containsValue(Tag.RequestedProcedureID)
//                    && (requestAttrs.containsValue(Tag.AccessionNumber) || data
//                            .contains(Tag.AccessionNumber))) {
//                Attributes attrs = new Attributes(data.bigEndian(), data.size()
//                        + requestAttrs.size());
//                attrs.addAll(data);
//                attrs.addAll(requestAttrs);
//                ScheduledProcedureStep sps = requestService
//                        .findOrCreateScheduledProcedureStep(attrs, patient,
//                                storeParam);
//                list.add(sps);
//            }
//        }
//        return list;
//    }

    private Collection<RequestAttributes> createRequestAttributes(
            Sequence seq, FuzzyStr fuzzyStr) {
        if (seq == null || seq.isEmpty())
            return null;

        ArrayList<RequestAttributes> list = new ArrayList<RequestAttributes>(
                seq.size());
        for (Attributes item : seq)
            list.add(new RequestAttributes(item, fuzzyStr));
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

    @Override
    public void updatePatient(EntityManager em, StoreContext storeContext, Patient patient) {
        patientService.updatePatient(patient,
                storeContext.getAttributes(),
                storeContext.getStoreParam(),
                false);
    }

    @Override
    public void updateStudy(EntityManager em, StoreContext storeContext, Study study) {
        Attributes data = storeContext.getAttributes();
        StoreParam storeParam = storeContext.getStoreParam();
        study.addModalityInStudy(data.getString(Tag.Modality, null));
        study.addSOPClassInStudy(data.getString(Tag.SOPClassUID, null));
        study.resetNumberOfInstances();
        AttributeFilter studyFilter = storeParam
                .getAttributeFilter(Entity.Study);
        Attributes studyAttrs = study.getAttributes();
        if (studyAttrs.mergeSelected(data, studyFilter.getSelection())) {
            study.setAttributes(studyAttrs, studyFilter,
                    storeParam.getFuzzyStr());
        }
    }

    @Override
    public void updateSeries(EntityManager em, StoreContext storeContext, Series series) {
        Attributes data = storeContext.getAttributes();
        StoreParam storeParam = storeContext.getStoreParam();
        series.resetNumberOfInstances();
        Attributes seriesAttrs = series.getAttributes();
        AttributeFilter seriesFilter = storeParam
                .getAttributeFilter(Entity.Series);
        if (seriesAttrs.mergeSelected(data, seriesFilter.getSelection())) {
            series.setAttributes(seriesAttrs, seriesFilter,
                    storeParam.getFuzzyStr());
        }
    }

    @Override
    public void updateInstance(EntityManager em, StoreContext storeContext, Instance inst) {
        Attributes data = storeContext.getAttributes();
        StoreParam storeParam = storeContext.getStoreParam();
        Attributes instAttrs = inst.getAttributes();
        AttributeFilter instFilter = storeParam
                .getAttributeFilter(Entity.Instance);
        Attributes updated = new Attributes();
        if (instAttrs.updateSelected(data, updated, instFilter.getSelection())) {
            inst.setAttributes(data, instFilter, storeParam.getFuzzyStr());
        }
    }

    @Override
    public boolean replaceInstance(EntityManager em,
            StoreContext storeContext, Instance prevInst) {
        if (!equalsSource(storeContext, prevInst)) {
            return false;
        }

        Series series = prevInst.getSeries();
        Study study = series.getStudy();
        prevInst.setReplaced(true);
        series.resetNumberOfInstances();
        study.resetNumberOfInstances();
        storeContext.setStoreAction(StoreAction.REPLACE);
        return true;
    }

    private boolean equalsSource(StoreContext storeContext, Instance prevInst) {
        String sourceAET = storeContext.getSourceAET();
        return sourceAET != null && sourceAET.equals(
                prevInst.getSeries().getSourceAET());
    }

    @Override
    public boolean restoreInstance(EntityManager em, StoreContext storeContext,
            Instance prevInst) {
        
        // only restore if no file references
        if (!prevInst.getFileRefs().isEmpty())
            return false;

        StoreService service = storeContext.getService();
        Series series = prevInst.getSeries();
        Study study = series.getStudy();
        Patient patient = study.getPatient();
        service.updatePatient(em, storeContext, patient);
        service.updateStudy(em, storeContext, study);
        service.updateSeries(em, storeContext, series);
        service.updateInstance(em, storeContext, prevInst);
        storeContext.setStoreAction(StoreAction.RESTORE);
        return true;
    }

    @Override
    public void fireStoreEvent(StoreContext ctx) {
        storeEvent.fire(new StoreEvent(
                ctx.getStoreSource(),
                ctx.getArchiveAEExtension().getApplicationEntity(),
                ctx.getStoreAction(),
                ctx.getInstance(),
                ctx.getAttributesAfterUpdateDB()));
    }

    @Override
    public void fireStoreCompleteEvent(Object source, ApplicationEntity ae) {
        storeCompletedEvent.fire(new StoreCompletedEvent(source, ae));
    }
}
