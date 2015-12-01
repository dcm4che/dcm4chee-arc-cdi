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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4chee.archive.store.scu.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.DatasetWithFMI;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.DataWriterAdapter;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.BasicCStoreSCU;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4che3.net.service.CStoreSCU;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.fetch.forward.FetchForwardCallBack;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.dcm4chee.task.ImageProcessingTaskTypes;
import org.dcm4chee.task.MemoryConsumingTask;
import org.dcm4chee.task.TaskType;
import org.dcm4chee.task.WeightWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawio <bsdreko@gmail.com>
 */
public class CStoreSCUImpl extends BasicCStoreSCU<ArchiveInstanceLocator>
        implements CStoreSCU<ArchiveInstanceLocator> {

    private static final Logger LOG = LoggerFactory
            .getLogger(CStoreSCUImpl.class);

    private CStoreSCUContext context;
    private CStoreSCUService service;

    private final WeightWatcher weightWatcher;

    private boolean withoutBulkData;

    public CStoreSCUImpl(ApplicationEntity localAE, ApplicationEntity remoteAE, ServiceType service,
                         CStoreSCUService storeSCUService, WeightWatcher weightWatcher) {
        super();
        this.context = new CStoreSCUContext(localAE, remoteAE, service);
        this.service = storeSCUService;
        this.weightWatcher = weightWatcher;
    }

    public void setWithoutBulkData(boolean withoutBulkData) {
        this.withoutBulkData = withoutBulkData;
    }

    @Override
    public org.dcm4che3.net.service.BasicCStoreSCUResp cstore(
            final java.util.List<ArchiveInstanceLocator> instances,
            final Association storeas, final int priority) {

        ArrayList<ArchiveInstanceLocator> locallyAvailable = (ArrayList<ArchiveInstanceLocator>) filterLocalOrExternalMatches(
                instances, true);
        ArrayList<ArchiveInstanceLocator> externallyAvailable = (ArrayList<ArchiveInstanceLocator>) filterLocalOrExternalMatches(
                instances, false);
        BasicCStoreSCUResp responseForLocallyAvailable = null;

        if (!locallyAvailable.isEmpty())
            responseForLocallyAvailable = pushInstances(locallyAvailable, storeas, priority);
        //initialize remaining response
        BasicCStoreSCUResp finalResponse = extendResponse(responseForLocallyAvailable);

        if (!externallyAvailable.isEmpty()) {
            FetchForwardCallBack moveCallBack = new FetchForwardCallBack() {

                @Override
                public void onFetch(Collection<ArchiveInstanceLocator> instances,
                                    BasicCStoreSCUResp resp) {
                    pushInstances((ArrayList<ArchiveInstanceLocator>) instances, storeas, priority);
                }
            };
            FetchForwardCallBack wadoCallBack = new FetchForwardCallBack() {

                @Override
                public void onFetch(Collection<ArchiveInstanceLocator> instances,
                                    BasicCStoreSCUResp resp) {
                    pushInstances((ArrayList<ArchiveInstanceLocator>) instances, storeas, priority);
                }
            };
            finalResponse = service.getFetchForwardService().fetchForward(instances.size(), finalResponse, externallyAvailable, storeas, priority, wadoCallBack, moveCallBack);
            if (failed.size() > 0) {
                if (failed.size() == nr_instances)
                    status = Status.UnableToPerformSubOperations;
                else
                    status = Status.OneOrMoreFailures;
            } else {
                status = Status.Success;
            }
            setChanged();
            notifyObservers();
        }
        return finalResponse;
    }

    @Override
    protected void storeInstance(Association storeas, ArchiveInstanceLocator instanceLocator) throws IOException, InterruptedException {
        String tsuid;
        DatasetWithFMI datasetWithFMI = null;
        Attributes attrs;
        ArchiveInstanceLocator inst = instanceLocator;
        try {
            ArchiveAEExtension arcAEExt = context.getLocalAE().getAEExtension(
                    ArchiveAEExtension.class);

            do {
                try {
                    datasetWithFMI = readFrom(inst);
                } catch (IOException e) {
                    LOG.info("Failed to read Data Set with iuid={} from {}@{}",
                            inst.iuid, inst.getFilePath(), inst.getStorageSystem(), e);
                    inst = inst.getFallbackLocator();
                    if (inst == null) {
                        throw e;
                    }
                    LOG.info("Try to read Data Set from alternative location");
                }
            } while (datasetWithFMI == null);

            tsuid = selectTransferSyntaxFor(storeas, inst, datasetWithFMI);

            attrs = datasetWithFMI.getDataset();

            // check for suppression criteria
            if (context.getRemoteAE() != null) {
                String templateURI = arcAEExt.getRetrieveSuppressionCriteria()
                        .getSuppressionCriteriaMap().get(context.getRemoteAE().getAETitle());
                if (templateURI != null)
                    inst = service.applySuppressionCriteria(inst, attrs, templateURI,
                            context);
            }

            service.coerceFileBeforeMerge(inst, attrs, context);

            //here we merge file attributes with attributes in the blob
            attrs = Utils.mergeAndNormalize(attrs, (Attributes) inst.getObject());

            service.coerceAttributes(attrs, context);
            if (!inst.iuid.equals(attrs.getString(Tag.SOPInstanceUID))) {
            	String newIUID = attrs.getString(Tag.SOPInstanceUID);
            	LOG.info("SOP Instance UID changed! {} -> {}", inst.iuid, newIUID);
            	inst = changeSOPInstanceUID(inst, newIUID);
            }

        } catch (Exception e) {
            LOG.info("Unable to store {}/{} to {}",
                    UID.nameOf(instanceLocator.cuid), UID.nameOf(instanceLocator.tsuid),
                    storeas.getRemoteAET(), e);
            failed.add(inst);
            return;
        }

        try {
            weightWatcher.execute(new StoreSCUTask(this, storeas, inst, attrs, tsuid));
        } catch (Exception e) {
            if (e instanceof IOException)
                throw (IOException) e;
            else if (e instanceof InterruptedException)
                throw (InterruptedException) e;
            else if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw new RuntimeException(e);
        }
    }

	private ArchiveInstanceLocator changeSOPInstanceUID(ArchiveInstanceLocator inst, String newIUID) {
		ArchiveInstanceLocator newLocator = new ArchiveInstanceLocator.Builder(
		        inst.cuid, 
		        newIUID,
		        inst.tsuid)
		.storageSystem(inst.getStorageSystem())
		.storagePath(inst.getFilePath())
		.entryName(inst.getEntryName())
		.fileTimeZoneID(inst.getFileTimeZoneID())
		.retrieveAETs(inst.getRetrieveAETs())
		.withoutBulkdata(inst.isWithoutBulkdata())
		.seriesInstanceUID(inst.getSeriesInstanceUID())
		.studyInstanceUID(inst.getStudyInstanceUID())
		.externalLocators(inst.getExternalLocators())
		.build();
		if (inst.getFallbackLocator() != null) {
			newLocator.setFallbackLocator(changeSOPInstanceUID(inst.getFallbackLocator(), newIUID));
		}
		return newLocator;
	}


    private DatasetWithFMI readFrom(ArchiveInstanceLocator inst) throws IOException {

        try (DicomInputStream din = new DicomInputStream(service.getFile(inst)
                .toFile())) {
            IncludeBulkData includeBulkData = IncludeBulkData.URI;
            int stopTag = -1;
            if (withoutBulkData) {
                if (((ArchiveInstanceLocator) inst).isWithoutBulkdata()) {
                    includeBulkData = IncludeBulkData.YES;
                } else {
                    includeBulkData = IncludeBulkData.NO;
                    stopTag = Tag.PixelData;
                }
            }
            din.setIncludeBulkData(includeBulkData);
            return din.readDatasetWithFMI(-1, stopTag);
        }
    }

    private BasicCStoreSCUResp extendResponse(
            BasicCStoreSCUResp responseForLocallyAvailable) {
        BasicCStoreSCUResp externalResponse = new BasicCStoreSCUResp();
        externalResponse.setCompleted(responseForLocallyAvailable != null ?
                responseForLocallyAvailable.getCompleted() : 0);
        externalResponse.setFailed(responseForLocallyAvailable != null ?
                responseForLocallyAvailable.getFailed() : 0);
        externalResponse.setFailedUIDs(responseForLocallyAvailable != null ? responseForLocallyAvailable.getFailedUIDs() == null
                ? new String[]{} : responseForLocallyAvailable.getFailedUIDs() : new String[]{});
        externalResponse.setWarning(responseForLocallyAvailable != null ?
                responseForLocallyAvailable.getWarning() : 0);
        return externalResponse;
    }

    private List<ArchiveInstanceLocator> filterLocalOrExternalMatches(
            List<ArchiveInstanceLocator> matches, boolean localMatches) {
        ArrayList<ArchiveInstanceLocator> filteredMatches = new ArrayList<ArchiveInstanceLocator>();

        for (ArchiveInstanceLocator match : matches) {
            if (localMatches) {
                if (match.getStorageSystem() != null)
                    filteredMatches.add(match);
            } else {
                if (match.getStorageSystem() == null)
                    filteredMatches.add(match);
            }

        }
        return filteredMatches;
    }

    private boolean isConfiguredAndAccepted(InstanceLocator ref,
                                            Set<String> negotiated) {
        if (context.getRemoteAE() != null) {
            ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                    context.getRemoteAE().getTransferCapabilitiesWithRole(Role.SCU));
            for (TransferCapability supportedTC : aeTCs) {
                if (ref.cuid.compareTo(supportedTC.getSopClass()) == 0
                        && supportedTC.containsTransferSyntax(ref.tsuid)
                        && negotiated.contains(ref.tsuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getDefaultConfiguredTransferSyntax(InstanceLocator ref) {
        if (context.getRemoteAE() != null) {
            Collection<TransferCapability> aeTCs = context.getRemoteAE().getTransferCapabilitiesWithRole(Role.SCU);
            for (TransferCapability supportedTC : aeTCs) {
                if (ref.cuid.equals(supportedTC.getSopClass())) {
                    if (supportedTC.containsTransferSyntax(UID.ExplicitVRLittleEndian))
                        return UID.ExplicitVRLittleEndian;
                    else
                        return UID.ImplicitVRLittleEndian;
                }
            }
        }
        return UID.ImplicitVRLittleEndian;
    }

    protected String selectTransferSyntaxFor(Association storeas, ArchiveInstanceLocator inst, DatasetWithFMI dataSetWithFMI) throws UnsupportedStoreSCUException {
        Set<String> acceptedTransferSyntax = new HashSet<>(storeas.getTransferSyntaxesFor(inst.cuid));

        // prevent that (possibly) faulty JPEG-LS data leaves the system,
        // we only want to store it decompressed
        if (inst.getStorageSystem().getStorageSystemGroup().isPossiblyFaultyJPEGLS(dataSetWithFMI)) {
            acceptedTransferSyntax.remove(UID.JPEGLSLossless);
        }

        // check for SOP classes elimination
        if (context.getArchiveAEExtension().getRetrieveSuppressionCriteria()
                .isCheckTransferCapabilities()) {
            inst = service.eliminateUnSupportedSOPClasses(inst, context);

            // check if eliminated then throw exception
            if (inst == null)
                throw new UnsupportedStoreSCUException(
                        "Unable to send instance, SOP class not configured");

            if (isConfiguredAndAccepted(inst, acceptedTransferSyntax))
                return inst.tsuid;
            else
                return getDefaultConfiguredTransferSyntax(inst);
        }

        if (acceptedTransferSyntax.contains(inst.tsuid))
            return inst.tsuid;

        if (acceptedTransferSyntax.contains(UID.ExplicitVRLittleEndian))
            return UID.ExplicitVRLittleEndian;
        else
            return UID.ImplicitVRLittleEndian;
    }

    private BasicCStoreSCUResp pushInstances(ArrayList<ArchiveInstanceLocator> instances, Association storeas, int priority) {
        return super.cstore(instances, storeas, priority);
    }

    private static class StoreSCUTask implements MemoryConsumingTask<Void> {
        private final CStoreSCUImpl storeSCU;
        private final Association storeas;
        private final ArchiveInstanceLocator inst;
        private final Attributes attrs;
        private final String targetTransferSyntaxUID;
        private Decompressor decompressor;

        public StoreSCUTask(CStoreSCUImpl storeSCU, Association storeas, ArchiveInstanceLocator inst, Attributes attrs, String targetTransferSyntaxUID) {
            this.storeSCU = storeSCU;
            this.storeas = storeas;
            this.inst = inst;
            this.attrs = attrs;
            this.targetTransferSyntaxUID = targetTransferSyntaxUID;

            String sourceTransferSyntaxUID = inst.tsuid;

            if (!targetTransferSyntaxUID.equals(sourceTransferSyntaxUID)) {
                decompressor = new Decompressor(attrs, inst.tsuid);
            } else {
                decompressor = null;
            }
        }

        @Override
        public TaskType getTaskType() {
            return ImageProcessingTaskTypes.TRANSCODE_OUTGOING;
        }

        @Override
        public long getEstimatedWeight() {
            if (decompressor != null)
                return decompressor.getEstimatedNeededMemory();
            else
                return 0;
        }

        @Override
        public Void call() throws IOException, InterruptedException {
            try {
                if (decompressor != null) {
                    decompressor.decompress();
                }

                DataWriter dataWriter = new DataWriterAdapter(attrs);
                storeSCU.cstore(storeas, inst, targetTransferSyntaxUID, dataWriter);

                // nullify pixeldata so that memory can be freed before the task ends
                attrs.setNull(Tag.PixelData, VR.OW);
            } finally {
                if (decompressor != null) {
                    decompressor.dispose();

                    // also remove reference to decompressor, to be able to free the memory
                    decompressor = null;
                }
            }
            return null;
        }
    }
}
