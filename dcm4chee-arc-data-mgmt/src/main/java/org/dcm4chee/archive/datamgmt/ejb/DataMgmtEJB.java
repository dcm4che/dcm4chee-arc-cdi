package org.dcm4chee.archive.datamgmt.ejb;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.ws.rs.core.MultivaluedMap;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.soundex.ESoundex;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
@Stateless
public class DataMgmtEJB {

    private static final Logger log = LoggerFactory
            .getLogger(DataMgmtEJB.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    public boolean updateInstances(String sopInstanceUID, StoreSession session,
            StoreService storeService,
            MultivaluedMap<String, String> queryParams)
            throws DicomServiceException {
        Instance inst = null;
        Series series = null;
        Study study = null;
        try {
            inst = em.find(Instance.class, getInstancePK(sopInstanceUID));
            series = inst.getSeries();
            study = series.getStudy();
        } catch (NoResultException e) {
            return false;
        }
        if (inst != null && series != null && study != null) {
            queryParams.remove("Type");
            queryParams.remove("SOPInstanceUID");
            StoreContext context = storeService.createStoreContext(session);
            byte[] blob = inst.getEncodedAttributes();
            Attributes temp = initUpdate(queryParams, blob, context, study);
            // set the instance attributes with the new ones (takes care of
            // updating db cols)
            inst.setAttributes(temp, session.getStoreParam()
                    .getAttributeFilter(Entity.Instance), null);
            try {
                // update the instance (takes care of updating the blob)
                storeService.updateInstance(em, context, inst);
                log.info("Instance new values: " + temp.toString());
                em.persist(inst);
            } catch (Exception e) {
                if (e instanceof PersistenceException)
                    e.printStackTrace();
                else
                    throw e;
            }
            return true;
        }
        return false;
    }

    public boolean updateSeries(String seriesInstanceUID, StoreSession session,
            StoreService storeService,
            MultivaluedMap<String, String> queryParams)
            throws DicomServiceException {
        Series series = null;
        Study study = null;
        try {
            series = em.find(Series.class, getSeriesPK(seriesInstanceUID));
            study = series.getStudy();
        } catch (NoResultException e) {
            return false;
        }
        if (series != null && study != null) {

            queryParams.remove("Type");
            queryParams.remove("SeriesInstanceUID");
            StoreContext context = storeService.createStoreContext(session);
            byte[] blob = series.getEncodedAttributes();
            Attributes temp = initUpdate(queryParams, blob, context, study);
            // set the instance attributes with the new ones (takes care of
            // updating db cols)
            series.setAttributes(temp, session.getStoreParam()
                    .getAttributeFilter(Entity.Instance), new ESoundex());
            try {
                // update the instance (takes care of updating the blob)
                storeService.updateSeries(em, context, series);
                log.info("Instance new values: " + temp.toString());
                em.persist(series);
            } catch (Exception e) {
                if (e instanceof PersistenceException)
                    e.printStackTrace();
                else
                    throw e;
            }
            return true;
        }
        return false;
    }

    public boolean updateStudy(String studyInstanceUID, StoreSession session,
            StoreService storeService,
            MultivaluedMap<String, String> queryParams)
            throws DicomServiceException {
        Study study = null;
        try {
            study = em.find(Study.class, getStudyPK(studyInstanceUID));
        } catch (NoResultException e) {
            return false;
        }
        if (study != null) {
            queryParams.remove("Type");
            queryParams.remove("StudyInstanceUID");
            StoreContext context = storeService.createStoreContext(session);
            byte[] blob = study.getEncodedAttributes();
            Attributes temp = initUpdate(queryParams, blob, context, study);
            // set the studfy attributes with the new ones (takes care of
            // updating db cols)
            study.setAttributes(temp, session.getStoreParam()
                    .getAttributeFilter(Entity.Instance), new ESoundex());
            try {
                // update the study (takes care of updating the blob)
                storeService.updateStudy(em, context, study);
                log.info("Study new values: \n" + temp.toString());
                em.persist(study);
            } catch (Exception e) {
                if (e instanceof PersistenceException)
                    e.printStackTrace();
                else
                    throw e;
            }
            return true;
        }
        return false;
    }

    public boolean updatePatient(String patientID, StoreSession session,
            StoreService storeService,
            MultivaluedMap<String, String> queryParams)
            throws DicomServiceException {
        Patient patient = null;
        try {
            patient = em.find(Patient.class, getPatientPK(patientID));
        } catch (NoResultException e) {
            return false;
        }
        if (patient != null) {
            queryParams.remove("Type");
            queryParams.remove("patientID");
            StoreContext context = storeService.createStoreContext(session);
            byte[] blob = patient.getEncodedAttributes();
            Attributes temp = initUpdate(queryParams, blob, context, null);
            // set the studfy attributes with the new ones (takes care of
            // updating db cols)
            patient.setAttributes(temp, session.getStoreParam()
                    .getAttributeFilter(Entity.Instance), new ESoundex());
            try {
                // update the study (takes care of updating the blob)
                storeService.updatePatient(em, context, patient);
                log.info("Study new values: \n" + temp.toString());
                em.persist(patient);
            } catch (Exception e) {
                if (e instanceof PersistenceException)
                    e.printStackTrace();
                else
                    throw e;
            }
            return true;
        }
        return false;
    }

    private long getInstancePK(String sopInstanceUID) {
        Instance inst = (Instance) em
                .createQuery(
                        "SELECT i FROM Instance i "
                                + "WHERE i.sopInstanceUID = ?1 ")
                .setParameter(1, sopInstanceUID).getSingleResult();
        return inst.getPk();
    }

    private long getStudyPK(String studyInstanceUID) {
        Study study = (Study) em
                .createQuery(
                        "SELECT i FROM Study i "
                                + "WHERE i.studyInstanceUID = ?1 ")
                .setParameter(1, studyInstanceUID).getSingleResult();
        return study.getPk();
    }

    private long getSeriesPK(String seriesInstanceUID) {
        Series series = (Series) em
                .createQuery(
                        "SELECT i FROM Series i "
                                + "WHERE i.seriesInstanceUID = ?1 ")
                .setParameter(1, seriesInstanceUID).getSingleResult();
        return series.getPk();
    }

    private long getPatientPK(String patientID) {
        Patient patient = (Patient) em
                .createQuery(
                        "SELECT i FROM Patient i " + "WHERE i.patientID = ?1 ")
                .setParameter(1, patientID).getSingleResult();
        return patient.getPk();
    }

    private Attributes initUpdate(MultivaluedMap<String, String> queryParams,
            byte[] blob, StoreContext context, Study study)
            throws DicomServiceException {
        ElementDictionary dict = ElementDictionary
                .getStandardElementDictionary();
        Attributes tomodify = new Attributes();
        for (String item : queryParams.keySet()) {
            int tag = dict.tagForKeyword(item);
            tomodify.setString(tag, dict.vrOf(tag), queryParams.getFirst(item));
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(blob);
        // extract the attributes
        try (DicomInputStream in = new DicomInputStream(bis)) {
            in.setIncludeBulkData(IncludeBulkData.URI);
            Attributes fmi = in.readFileMetaInformation();
            Attributes ds = in.readDataset(-1, -1);
            context.setTransferSyntax(fmi != null ? fmi
                    .getString(Tag.TransferSyntaxUID)
                    : UID.ImplicitVRLittleEndian);
            // required in update study (no null check)
            if (study != null)
                if (!ds.containsValue(Tag.SOPClassesInStudy)) {
                    for (String s : study.getSOPClassesInStudy()) {
                        ds.setString(Tag.SOPClassUID, VR.UI, s);
                    }
                }
            context.setAttributes(ds);
        } catch (IOException e) {
            throw new DicomServiceException(0xC900);
        }
        // update extracted attributes with modifications
        Attributes temp = context.getAttributes();
        Attributes modified = new Attributes();
        log.info("To be modified: \n" + tomodify.toString());
        temp.update(tomodify, modified);
        log.debug("Updated attrbiutes: \n" + modified);
        return temp;
    }

}
