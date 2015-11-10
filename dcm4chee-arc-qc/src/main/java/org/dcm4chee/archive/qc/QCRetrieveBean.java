package org.dcm4chee.archive.qc;

import java.util.Collection;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.QCInstanceHistory;
import org.dcm4chee.archive.sc.StructuralChangeContainer;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;

public interface QCRetrieveBean {

    /**
     * Requires Reference Update.
     * Used by the RetrieveQCDecorator to check if the object being retrieved
     * requires a reference update.
     * Checks for if the study was QCed (exists as old or next in the study table)
     * The studyinstance uid is provided or a patient in which case all 
     * studies of the patient are used for the query.

     * @param studyInstanceUID
     *            UID for a study under investigation
     * @param attrs
     *            Attributes with id for the patient 
     *            whose studies are under investigation
     * @return the QC event
     */
    public boolean requiresReferenceUpdate(String studyInstanceUID, Patient patient);

    /**
     * Scan For Referenced Study UIDs.
     * Used by the RetrieveQCDecorator to get the list of referenced
     * study uids required for the query to get the upadted references.
     * 
     * @param attrs
     *            Attributes for the retrieved object
     * @return boolean
     */
    public void scanForReferencedStudyUIDs(Attributes attrs, Collection<String> initialColl);

    /**
     * Get Referenced History
     * Used by the RetrieveQCDecorator to get the list of all instances
     * from the history tables belonging to the given studies.
     * @param retrieveContext 
     * 
     * @param referencedStudyInstanceUIDs
     *            A collection of study instance uids
     * @return a collection of instance history
     */
    public Collection<QCInstanceHistory> getReferencedHistory(CStoreSCUContext ctx,
            Collection<String> referencedStudyInstanceUIDs);

//    /**
//     * Re-Calculate Query Attributes
//     * Used by the QCPostProcessor to recalculate query 
//     * attributes for study and series.
//     * @param event 
//     * 
//     * @return void
//     */
//    public void recalculateQueryAttributes(QCEvent event);
    
    /**
     * Re-Calculate Query Attributes
     * Used by the QCPostProcessor to recalculate query 
     * attributes for study and series.
     * @param event 
     * 
     * @return void
     */
    public void recalculateQueryAttributes(StructuralChangeContainer changeContainer);
}
