package org.dcm4chee.archive.api.impl;

import org.dcm4chee.archive.api.History;
import org.dcm4chee.archive.entity.history.ActionHistory;
import org.dcm4chee.archive.entity.history.StudyHistory;
import org.dcm4chee.archive.qc.QC_OPERATION;
import org.dcm4chee.archive.qc.StructuralChangeService;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Umberto Cappellini on 12/21/15.
 */
@EJB(name = History.JNDI_NAME, beanInterface = History.class)
//@Remote(History.class) //uncomment for remote access
@Stateless
public class HistoryImpl implements History {

    @Inject
    private StructuralChangeService structuralChangeService;


    @Override
    public void updateStudyUID(String old_study_uid, String new_study_uid) {
        ActionHistory action = structuralChangeService.generateQCAction(QC_OPERATION.UPDATE, ActionHistory
                .HierarchyLevel.STUDY);

        StudyHistory history = structuralChangeService.createQCStudyHistory(old_study_uid, new_study_uid, null, action);
    }

    @Override
    public List<String> getActualUIDs(String study_uid) {

        List<StudyHistory> studyHistories = structuralChangeService.findStudyHistory(study_uid);

        if (studyHistories == null || studyHistories.size() == 0)
            return  null;

        ArrayList<String> result = new ArrayList<String>();

        for (StudyHistory history : studyHistories)
            result.add(history.getNextStudyUID());

        return result;
    }
}
