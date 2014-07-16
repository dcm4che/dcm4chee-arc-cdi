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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4chee.archive.datamgmt.ejb;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.dcm4chee.archive.entity.FileRef;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@Stateless
public class DataMgmtEJB implements DataMgmtBean {

    private static final Logger log = LoggerFactory
            .getLogger(DataMgmtEJB.class);

    @PersistenceContext(unitName = "dcm4chee-arc")
    private EntityManager em;

    public long getInstancePK(String sopInstanceUID) {
        Instance inst = (Instance) em
                .createQuery(
                        "SELECT i FROM Instance i "
                                + "WHERE i.sopInstanceUID = ?1 ")
                .setParameter(1, sopInstanceUID).getSingleResult();
        return inst.getPk();
    }

    public long getStudyPK(String studyInstanceUID) {
        Study study = (Study) em
                .createQuery(
                        "SELECT i FROM Study i "
                                + "WHERE i.studyInstanceUID = ?1 ")
                .setParameter(1, studyInstanceUID).getSingleResult();
        return study.getPk();
    }

    public long getSeriesPK(String seriesInstanceUID) {
        Series series = (Series) em
                .createQuery(
                        "SELECT i FROM Series i "
                                + "WHERE i.seriesInstanceUID = ?1 ")
                .setParameter(1, seriesInstanceUID).getSingleResult();
        return series.getPk();
    }

    public long getPatientPK(String patientID) {
        Patient patient = (Patient) em
                .createQuery(
                        "SELECT i FROM Patient i " + "WHERE i.patientID = ?1 ")
                .setParameter(1, patientID).getSingleResult();
        return patient.getPk();
    }

    public Study deleteStudy(String studyInstanceUID) {
        TypedQuery<Study> query = em.createNamedQuery(
                Study.FIND_BY_STUDY_INSTANCE_UID, Study.class).setParameter(1,
                studyInstanceUID);
        Study study = query.getSingleResult();
        em.remove(study);
        log.info("Removed study entity - "+studyInstanceUID);
        return study;
    }

    @Override
    public Series deleteSeries(String seriesInstanceUID) {
        TypedQuery<Series> query = em.createNamedQuery(
                Series.FIND_BY_SERIES_INSTANCE_UID, Series.class).setParameter(
                1, seriesInstanceUID);
        Series series = query.getSingleResult();
        Study study = series.getStudy();
        int numInstancesNew = study.getNumberOfInstances()
                - series.getNumberOfInstances();
        em.remove(series);
        if (numInstancesNew > 0)
            study.setNumberOfInstances(numInstancesNew);
        else
            study.resetNumberOfInstances();
        
        study.setNumberOfSeries(study.getNumberOfSeries()-1);
        log.info("Removed series entity - "+seriesInstanceUID);
        return series;
    }

    @Override
    public Instance deleteInstance(String sopInstanceUID)
            throws Exception {
        TypedQuery<Instance> query = em.createNamedQuery(
                Instance.FIND_BY_SOP_INSTANCE_UID, Instance.class)
                .setParameter(1, sopInstanceUID);
        Instance inst = query.getSingleResult();
        Series series = inst.getSeries();
        Study study = series.getStudy();

        int numInstancesNew = series.getNumberOfInstances() - 1;
        int numInstancesNewStudy = study.getNumberOfInstances() - 1;
        em.remove(inst);
        log.info("Removed instance entity - "+sopInstanceUID);
        if (numInstancesNew > 0)
            series.setNumberOfInstances(numInstancesNew);
        else
            series.resetNumberOfInstances();
        if (numInstancesNewStudy > 0)
            study.setNumberOfInstances(numInstancesNewStudy);
        else
            study.resetNumberOfInstances();
        List<FileRef> fileRef = (List<FileRef>) inst.getFileRefs();
        for (FileRef file : fileRef) {

            try {
                log.info("Deleted file: " + file.getFilePath());
                if(!new File(file.getFileSystem().getPath().toFile(), file.getFilePath()).delete())
                    throw new Exception();
            } catch (NoSuchFileException e) {
                log.error("No such file or directory\n"
                        + e.getStackTrace().toString());
            } catch (IOException e1) {
                log.error("No sufficient permissions to delete file\n"
                        + e1.getStackTrace().toString());
            }
        }

        return inst;
    }

    @Override
    public boolean deleteSeriesIfEmpty(String seriesInstanceUID,
            String studyInstanceUID) {
        TypedQuery<Series> query = em.createNamedQuery(
                Series.FIND_BY_SERIES_INSTANCE_UID, Series.class).setParameter(
                1, seriesInstanceUID);
        Series series = query.getSingleResult();

        if (series.getNumberOfInstances() == -1){
            em.remove(series);
            log.info("Removed series entity - "+seriesInstanceUID);
        return true;
        }

        return false;
    }

    @Override
    public boolean deleteStudyIfEmpty(String studyInstanceUID) {
        TypedQuery<Study> query = em.createNamedQuery(
                Study.FIND_BY_STUDY_INSTANCE_UID, Study.class).setParameter(1,
                studyInstanceUID);
        Study study = query.getSingleResult();

        if (study.getNumberOfInstances() == -1){
            em.remove(study);
            log.info("Removed study entity - "+studyInstanceUID);
         return true;
        }

        return false;
    }

}
