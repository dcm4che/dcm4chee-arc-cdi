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

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.ScheduledProcedureStep;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.VerifyingObserver;
import org.dcm4chee.archive.issuer.IssuerService;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
import org.dcm4chee.archive.patient.PatientService;
import org.dcm4chee.archive.request.RequestService;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
@ApplicationScoped
public class DefaultStoreService implements StoreService {

    private static Logger LOG = LoggerFactory
            .getLogger(DefaultStoreService.class);

    @Inject
    private PatientService patientService;

    @Inject
    private IssuerService issuerService;

    @Inject
    private CodeService codeService;

    @Inject
    private RequestService requestService;

    @Inject
    private FileSystemEJB fileSystemEJB;

    @Inject
    private StoreServiceEJB storeServiceEJB;

    @Override
    public FileSystem selectFileSystem(StoreSource src, ArchiveAEExtension arcAE)
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
    public StoreContext createStoreContext(StoreSource source,
            ArchiveAEExtension arcAE, FileSystem fs, Path file, byte[] digest) {
        return new DefaultStoreContext(source, arcAE, fs, file, digest);
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
            StoreSource storeSource = storeContext.getStoreSource();
            Templates tpl = arcAE.getAttributeCoercionTemplates(
                    storeContext.getSOPClassUID(),
                    Dimse.C_STORE_RQ, TransferCapability.Role.SCP, 
                    storeSource.getSendingAETitle(arcAE));
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
            return patientService.findPatientOnStorage(storeContext
                    .getAttributes());
        } catch (NonUniquePatientException e) {
            LOG.info("Could not find unique Patient Record for received Study - create new Patient Record");
            return null;
        } catch (PatientCircularMergedException e) {
            LOG.warn(
                    "Detect circular merged Patient Record for received Study - create new Patient Record",
                    e);
            return null;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    @Override
    public Patient createPatient(EntityManager em, StoreContext storeContext) {
        return patientService.createPatientOnStorage(
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
        study.setIssuerOfAccessionNumber(issuer(attrs
                .getNestedDataset(Tag.IssuerOfAccessionNumberSequence)));
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
        series.setScheduledProcedureSteps(getScheduledProcedureSteps(
                data.getSequence(Tag.RequestAttributesSequence), data,
                study.getPatient(), storeParam));
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
            Series series) {
        Attributes data = storeContext.getAttributes();
        StoreParam storeParam = storeContext.getStoreParam();
        FileSystem fs = storeContext.getFileSystem();
        Attributes coercedAttrs = storeContext.getCoercedAttributes();
        if (!coercedAttrs.isEmpty() && storeParam.isStoreOriginalAttributes()) {
            Attributes item = new Attributes(4);
            Sequence origAttrsSeq = data.ensureSequence(
                    Tag.OriginalAttributesSequence, 1);
            origAttrsSeq.add(item);
            item.setDate(Tag.AttributeModificationDateTime, VR.DT, new Date());
            item.setString(Tag.ModifyingSystem, VR.LO,
                    storeParam.getModifyingSystem());
            item.setString(Tag.SourceOfPreviousValues, VR.LO,
                    storeContext.getSendingAETitle());
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
    public void createFileRef(EntityManager em, StoreContext storeContext,
            Instance instance) {
        FileRef fileRef = storeContext.getFileRef();
        fileRef.setInstance(instance);
        em.persist(fileRef);
    }

    private Collection<ScheduledProcedureStep> getScheduledProcedureSteps(
            Sequence requestAttrsSeq, Attributes data, Patient patient,
            StoreParam storeParam) {
        if (requestAttrsSeq == null)
            return null;
        ArrayList<ScheduledProcedureStep> list = new ArrayList<ScheduledProcedureStep>(
                requestAttrsSeq.size());
        for (Attributes requestAttrs : requestAttrsSeq) {
            if (requestAttrs.containsValue(Tag.ScheduledProcedureStepID)
                    && requestAttrs.containsValue(Tag.RequestedProcedureID)
                    && (requestAttrs.containsValue(Tag.AccessionNumber) || data
                            .contains(Tag.AccessionNumber))) {
                Attributes attrs = new Attributes(data.bigEndian(), data.size()
                        + requestAttrs.size());
                attrs.addAll(data);
                attrs.addAll(requestAttrs);
                ScheduledProcedureStep sps = requestService
                        .findOrCreateScheduledProcedureStep(attrs, patient,
                                storeParam);
                list.add(sps);
            }
        }
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

    private Issuer issuer(Attributes item) {
        if (item == null)
            return null;

        return issuerService.findOrCreate(new Issuer(item));
    }

    @Override
    public boolean replaceInstance(EntityManager em, StoreContext storeContext,
            Instance inst) {
        String externalRetrieveAET = inst.getExternalRetrieveAET();
        String sendingAETitle = storeContext.getSendingAETitle();
        if (externalRetrieveAET != null
                && externalRetrieveAET.equals(sendingAETitle)) {
            Series series = inst.getSeries();
            Study study = series.getStudy();
            Patient patient = study.getPatient();
            Collection<FileRef> fileRefs = inst.getFileRefs();
            if (fileRefs.isEmpty()) {
                storeContext.getService().updatePatient(storeContext, patient);
                storeContext.getService().updateStudy(storeContext, study);
                storeContext.getService().updateSeries(storeContext, series);
                storeContext.getService().updateInstance(storeContext, inst);
                storeContext.getService().createFileRef(em, storeContext, inst);
            }
            Attributes storedAttrs = storeContext.getAttributes();
            Attributes coercedAtts = storeContext.getCoercedAttributes();
            storedAttrs.update(patient.getAttributes(), coercedAtts);
            storedAttrs.update(study.getAttributes(), coercedAtts);
            storedAttrs.update(series.getAttributes(), coercedAtts);
            storedAttrs.update(inst.getAttributes(), coercedAtts);
            return false;
        }

        inst.setReplaced(true);
        return true;
    }

    @Override
    public void updatePatient(StoreContext storeContext, Patient patient) {
        patientService.updatePatientOnStorage(patient,
                storeContext.getAttributes(), storeContext.getStoreParam());
    }

    @Override
    public void updateStudy(StoreContext storeContext, Study study) {
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
    public void updateSeries(StoreContext storeContext, Series series) {
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
    public void updateInstance(StoreContext storeContext, Instance inst) {
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

}
