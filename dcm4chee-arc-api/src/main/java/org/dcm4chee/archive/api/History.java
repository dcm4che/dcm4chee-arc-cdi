package org.dcm4chee.archive.api;

import java.util.List;

/**
 * An API to access History related info
 * <p/>
 * Created by Umberto Cappellini on 12/21/15.
 */
public interface History {

    public static final String JNDI_NAME = "java:global/org.dcm4chee.archive.api.History";

    /**
     * Update History that a study UID collision has occurred and an existing
     * Study UID has been replaced by a new one.
     *
     * @param old_study_uid original study uid
     * @param new_study_uid new study uid
     */
    void updateStudyUID(String old_study_uid, String new_study_uid);

    /**
     * Given an original study uid, returns one ore more (a list) of study
     * UIDs that replaced it.
     *
     * @param study_uid
     * @return
     */
    List<String> getActualUIDs(String study_uid);
}
