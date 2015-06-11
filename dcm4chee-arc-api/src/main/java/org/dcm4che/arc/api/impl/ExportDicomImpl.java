package org.dcm4che.arc.api.impl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che.arc.api.ExportDicom;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.QueryOption;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class ExportDicomImpl implements ExportDicom {

    private static final Logger LOG = LoggerFactory
            .getLogger(ExportDicomImpl.class);

    @PersistenceContext(name = "dcm4chee-arc")
    EntityManager em;

    @Inject
    private RetrieveService retrieveService;

    @Inject
    private CStoreSCUService cstoreSCUService;

    @Inject
    private Device device;

    @Inject
    private IApplicationEntityCache aeCache;

    @Override
    public void exportStudy(String destinationAETitle, List<String> studyIuids) {

        CStoreSCUContext ctx = initializeContext(destinationAETitle);
        if(ctx !=null)
        cstoreSCUService.scheduleStoreSCU(UUID.randomUUID().toString(), ctx,
                toInstanceLocators(studyIuids, null, ctx.getLocalAE()), 1, 1, 0);
    }

    @Override
    public void exportInstances(String destinationAETitle, List<String> instanceUids) {
        CStoreSCUContext ctx = initializeContext(destinationAETitle);
        if(ctx !=null)
        cstoreSCUService.scheduleStoreSCU(UUID.randomUUID().toString(), ctx,
                toInstanceLocators(null, instanceUids, ctx.getLocalAE()), 1, 1, 0);

    }

    @Override
    public void exportKeyImages(String destinationAETitle, List<String> studyUIDs,
            List<String> keyObjectDocumentTitles) {
        ArrayList<ArchiveInstanceLocator> referencedInstances = new ArrayList<ArchiveInstanceLocator>();
        CStoreSCUContext ctx = initializeContext(destinationAETitle);
        if(ctx ==null) {
            LOG.error("Error initializing cstore context for export to AE {}", destinationAETitle);
            return;
        }
        
        ArrayList<ArchiveInstanceLocator> locators = (ArrayList<ArchiveInstanceLocator>)
                toInstanceLocators(studyUIDs, null, ctx.getLocalAE());
        for(ArchiveInstanceLocator locator : locators) {
            Attributes objectAttrs = (Attributes) locator.getObject();
            if(objectAttrs != null) {
                Sequence conceptNameCodeSequence = objectAttrs
                        .getSequence(Tag.ConceptNameCodeSequence);
                Attributes titleItem = conceptNameCodeSequence.get(1);
                String title = titleItem.getString(Tag.CodeMeaning);
                if (keyObjectDocumentTitles.contains(title)) {
                    Sequence crpEvidenceSequence = objectAttrs
                            .getSequence(Tag.CurrentRequestedProcedureEvidenceSequence);
                    for (Iterator<Attributes> iter = crpEvidenceSequence
                            .iterator(); iter.hasNext();) {
                       ArrayList<String> iuids = getReferencedInstanceUIDs(iter.next());
                       referencedInstances.addAll(toInstanceLocators(null, iuids, ctx.getLocalAE()));
                    }
                }
            }
            else {
                LOG.error("Error reading blob attributes for object {}, "
                        + "No Export will take place", locator.iuid);
                return;
            }
        }
        cstoreSCUService.scheduleStoreSCU(UUID.randomUUID().toString(), ctx,
                referencedInstances, 1, 1, 0);
    }

    private ArrayList<String> getReferencedInstanceUIDs(Attributes item) {
        ArrayList<String> referencedSopUIDs = new ArrayList<String>();
        Sequence seriesSequence = item.getSequence(Tag.ReferencedSeriesSequence);
        for(Iterator<Attributes> iter = seriesSequence.iterator(); iter.hasNext();) {
            Attributes seriesSeqItems = iter.next();
            Sequence sopSequence = seriesSeqItems.getSequence(Tag.ReferencedSOPSequence);
            for(Iterator<Attributes> iterSops = sopSequence.iterator(); iterSops.hasNext();) {
                referencedSopUIDs.add(iterSops.next().getString(Tag.ReferencedSOPInstanceUID));
            }
        }
        return referencedSopUIDs;
    }

    private CStoreSCUContext initializeContext(String destinationAETitle) {
        ApplicationEntity remoteAE = null;
        try {
            remoteAE = aeCache.findApplicationEntity(destinationAETitle);
        } catch (ConfigurationException e) {
            LOG.error("Error looking up {} AE from configuration ",
                    destinationAETitle);
        }
        ApplicationEntity localAE = pickSuitableLocalAE();
        if (remoteAE == null || localAE == null) {
            LOG.error(
                    "Error retrieveing localAE or destination {} AE from configuration ",
                    destinationAETitle);
            return null;
        }

       return new CStoreSCUContext(pickSuitableLocalAE(),
                remoteAE, ServiceType.DICOMEXPORT);
    }

    private List<ArchiveInstanceLocator> toInstanceLocators(
            List<String> studyUIDs, List<String> iuids, 
            ApplicationEntity localAE) {
        
        ArchiveAEExtension arcAE = localAE
                .getAEExtension(ArchiveAEExtension.class);
        ArrayList<ArchiveInstanceLocator> matches = new ArrayList<>();
        QueryParam queryParam = arcAE.getQueryParam(
                EnumSet.noneOf(QueryOption.class), new String[0]);
        if(studyUIDs != null)
        for (String studyUID : studyUIDs) {
            matches.addAll(retrieveService.calculateMatches(studyUID, null,
                    null, queryParam, false));
        }
        else
            for (String iuid : iuids) {
                matches.addAll(retrieveService.calculateMatches(null, null,
                        iuid, queryParam, false));
            }
        return matches;
    }

    private ApplicationEntity pickSuitableLocalAE() {
        ArchiveDeviceExtension arcDevExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        ApplicationEntity localAE = null;
        try {
            localAE = aeCache.findApplicationEntity(arcDevExt
                    .getDefaultAETitle());
        } catch (ConfigurationException e) {
            LOG.error(
                    "Default AE for device {} can not be loaded from configuration",
                    device);
        }
        return localAE;
    }

}
