package org.dcm4chee.archive.mpps.rejection;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.entity.*;
import org.dcm4chee.archive.query.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;

@Stateless
public class IncorrectWorkListEntrySelectedHandlerEJB {

    private static final Logger LOG = LoggerFactory.getLogger(IncorrectWorkListEntrySelectedHandlerEJB.class);

    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;

    @Inject
    QueryService queryService;

    @Inject
    Device device;

    public void checkStatusAndRejectRejectInstancesIfNeeded(MPPS mpps) {
        if (mpps.discontinuedForReason(incorrectWorklistEntrySelectedCode()))
            rejectReferencedInstancesDueToIncorrectlySelectedWorklistEntry(mpps);
    }

    public void rejectReferencedInstancesDueToIncorrectlySelectedWorklistEntry(MPPS pps) {
        HashMap<String, Attributes> referencedInstancesByIuid = new HashMap<>();

        // inited by first local instance if found
        Study study = null;

        for (Attributes seriesRef : pps.getAttributes().getSequence(Tag.PerformedSeriesSequence)) {

            // put all mpps- referenced instances into a map by iuid
            for (Attributes ref : seriesRef.getSequence(Tag.ReferencedImageSequence))
                referencedInstancesByIuid.put(ref.getString(Tag.ReferencedSOPInstanceUID), ref);
            for (Attributes ref : seriesRef.getSequence(Tag.ReferencedNonImageCompositeSOPInstanceSequence))
                referencedInstancesByIuid.put(ref.getString(Tag.ReferencedSOPInstanceUID), ref);

            // inited by first local instance if found
            Series series = null;

            // iterate over the instances we have locally for this series
            for (Instance localInstance : findBySeriesInstanceUID(seriesRef)) {
                String iuid = localInstance.getSopInstanceUID();
                Attributes referencedInstance = referencedInstancesByIuid.get(iuid);
                if (referencedInstance != null) {
                    String cuid = localInstance.getSopClassUID();
                    String cuidInPPS = referencedInstance.getString(Tag.ReferencedSOPClassUID);

                    series = localInstance.getSeries();
                    study = series.getStudy();

                    if (!cuid.equals(cuidInPPS)) {
                        LOG.warn("SOP Class of received Instance[iuid={}, cuid={}] "
                                        + "of Series[iuid={}] of Study[iuid={}] differs from "
                                        + "SOP Class[cuid={}] referenced by MPPS[iuid={}]",
                                iuid, cuid, series.getSeriesInstanceUID(),
                                study.getStudyInstanceUID(), cuidInPPS,
                                pps.getSopInstanceUID());
                    }

                    LOG.info("Reject Instance[pk={},iuid={}] by MPPS Discontinuation Reason - {}",
                            localInstance.getPk(), iuid,
                            incorrectWorklistEntrySelectedCode());

                    localInstance.setRejectionNoteCode(incorrectWorklistEntrySelectedCode());
                    em.merge(localInstance);
                }
            }
            referencedInstancesByIuid.clear();
        }

        // updated derived fields for the study
        if (study!=null) {
            // TODO: when the source AET permanently stored - update here
            String defaultAETitle = device.getDeviceExtensionNotNull(ArchiveDeviceExtension.class).getDefaultAETitle();
            queryService.calculateDerivedFields(study, device.getApplicationEntityNotNull(defaultAETitle));
        }
    }
    private Code incorrectWorklistEntrySelectedCode() {
        return (Code) device
                .getDeviceExtension(ArchiveDeviceExtension.class)
                .getIncorrectWorklistEntrySelectedCode();
    }

    public List<Instance> findBySeriesInstanceUID(Attributes seriesRef) {
        return em.createNamedQuery(Instance.FIND_BY_SERIES_INSTANCE_UID, Instance.class)
                .setParameter(1, seriesRef.getString(Tag.SeriesInstanceUID))
                .getResultList();
    }
}
