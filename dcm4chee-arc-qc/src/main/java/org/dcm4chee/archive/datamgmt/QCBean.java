package org.dcm4chee.archive.datamgmt;

import java.util.Collection;

import javax.persistence.EntityNotFoundException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Code;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.datamgmt.entities.QCActionHistory;
import org.dcm4chee.archive.datamgmt.entities.QCInstanceHistory;
import org.dcm4chee.archive.datamgmt.entities.QCSeriesHistory;
import org.dcm4chee.archive.datamgmt.entities.QCStudyHistory;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;

public interface QCBean {

    public void mergeStudies(String[] sourceStudyUids, String targetStudyUid, Code qcRejectionCode);

    public void merge(String sourceStudyUid, String targetStudyUid, boolean samePatient, Code qcRejectionCode);

    public void split(Collection<Instance> toMove, Attributes createdStudy, Code qcRejectionCode);

    public void segment(Collection<Instance> toMove, Collection<Instance> toClone, Attributes targetStudy, Code qcRejectionCode);

    public void segmentFrame(Instance toMove, Instance toClone, int frame,
            Attributes targetStudy);

    public void move(Instance source, Series target, Attributes targetInstance, Code qcRejectionCode);

    public void clone(Instance source, Series target, Attributes targetInstance);

    public void recordHistoryEntry(QCActionHistory action,
            Collection<QCStudyHistory> study, Collection<QCSeriesHistory> series,
            Collection<QCInstanceHistory> instance);

    public QCInstanceHistory findUIDChangesFromHistory(Instance instance);

    public void removeHistoryEntry(QCActionHistory action);

    public boolean canApplyQC(Instance sopInstanceUID);

    public void notify(QCEvent event);

    void updateDicomObject(ArchiveDeviceExtension arcDevExt, String scope, Attributes attrs) throws EntityNotFoundException;
    
    boolean patientOperation(Attributes sourcePatientAttributes, Attributes targetPatientAttributes, ArchiveAEExtension arcAEExt, PatientCommands command);
    
    public void undoLastAction(QCStudyHistory study);

    public Collection<Instance> locateInstances(String[] strings);

    public Code findOrCreateCode(Code code);

    public Study deleteStudy(String studyInstanceUID) throws Exception;

    public Series deleteSeries(String seriesInstanceUID) throws Exception;

    public Instance deleteInstance(String sopInstanceUID) throws Exception;

    boolean deleteStudyIfEmpty(String studyInstanceUID);

    boolean deleteSeriesIfEmpty(String seriesInstanceUID,
            String studyInstanceUID);

    void reject(String[] sopInstanceUIDs, Code qcRejectionCode);

    void reject(Collection<Instance> instances, Code qcRejectionCode);

    void restore(String[] sopInstanceUIDs);

    void restore(Collection<Instance> instances);
}
