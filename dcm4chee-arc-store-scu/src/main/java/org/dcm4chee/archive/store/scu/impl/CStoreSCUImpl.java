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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DataWriter;
import org.dcm4che3.net.DataWriterAdapter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.ExternalArchiveAEExtension;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.BasicCStoreSCU;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4che3.net.service.CStoreSCU;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ExternalLocationTuple;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.dcm4chee.archive.wado.client.InstanceAvailableCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawio <bsdreko@gmail.com>
 *
 */
public class CStoreSCUImpl extends BasicCStoreSCU<ArchiveInstanceLocator>
        implements CStoreSCU<ArchiveInstanceLocator> {

    private static final Logger LOG = LoggerFactory
            .getLogger(CStoreSCUImpl.class);

    private CStoreSCUContext context;
    private CStoreSCUService service;
    private boolean withoutBulkData;

    /**
     * @param localAE
     * @param remoteAE
     * @param storeSCUService
     */
    public CStoreSCUImpl(ApplicationEntity localAE, ApplicationEntity remoteAE, ServiceType service,
            CStoreSCUService storeSCUService) {
        super();
        this.context = new CStoreSCUContext(localAE, remoteAE, service);
        this.service = storeSCUService;
    }

    public void setWithoutBulkData(boolean withoutBulkData) {
        this.withoutBulkData = withoutBulkData;
    }

    @Override
    public org.dcm4che3.net.service.BasicCStoreSCUResp cstore(
            java.util.List<ArchiveInstanceLocator> instances,
            Association storeas, int priority) {

        ArrayList<ArchiveInstanceLocator> localyAvailable = (ArrayList<ArchiveInstanceLocator>) filterLocalOrExternalMatches(
                instances, true);
        ArrayList<ArchiveInstanceLocator> externallyAvailable = (ArrayList<ArchiveInstanceLocator>) filterLocalOrExternalMatches(
                instances, false);
        BasicCStoreSCUResp responseForLocalyAvailable = null;
        BasicCStoreSCUResp responseForExternallyAvailable = null;

        if(!localyAvailable.isEmpty())
        responseForLocalyAvailable = super.cstore(
                localyAvailable, storeas, priority);

        if (!externallyAvailable.isEmpty())
            responseForExternallyAvailable = getExternalCStoreResponse(externallyAvailable, storeas, priority);

        return mergeResponses(responseForExternallyAvailable, responseForLocalyAvailable);

    }

    private BasicCStoreSCUResp mergeResponses(
            BasicCStoreSCUResp responseForExternallyAvailable,
            BasicCStoreSCUResp responseForLocalyAvailable) {
        if(responseForExternallyAvailable == null)
            return responseForLocalyAvailable;
        else if(responseForLocalyAvailable == null)
            return responseForExternallyAvailable;
            else{
                BasicCStoreSCUResp aggregatedResponse = 
                        new BasicCStoreSCUResp();
                aggregatedResponse.setStatus(Math.max(
                        responseForExternallyAvailable.getStatus(),
                        responseForLocalyAvailable.getStatus()));
                aggregatedResponse.setCompleted(
                        responseForExternallyAvailable.getCompleted()
                        +responseForLocalyAvailable.getCompleted());
                aggregatedResponse.setFailed(
                        responseForExternallyAvailable.getFailed()
                        +responseForLocalyAvailable.getFailed());
                aggregatedResponse.setWarning(
                        responseForExternallyAvailable.getWarning()
                        +responseForLocalyAvailable.getWarning());
                aggregatedResponse.setFailedUIDs(aggregateArray(
                        responseForExternallyAvailable.getFailedUIDs(),
                        responseForLocalyAvailable.getFailedUIDs()));
                return aggregatedResponse;
            }
                
    }

    private String[] aggregateArray(String[] arr1, String[] arr2) {
        String[] aggregated = new String[arr1.length + arr2.length];
        int i = 0;
        for (String str : arr1)
            aggregated[++i] = str;
        for (String str : arr2)
            aggregated[++i] = str;
        return aggregated;
    }

    private BasicCStoreSCUResp getExternalCStoreResponse(
            final ArrayList<ArchiveInstanceLocator> externallyAvailable,
            final Association storeas, final int priority) {
        final BasicCStoreSCUResp response = new BasicCStoreSCUResp();
        for(int current = 0; current < externallyAvailable.size(); current++) {
            if(storeas.isReadyForDataTransfer()) {
            final ArchiveInstanceLocator externalLoc = externallyAvailable
                    .get(current);
            ArrayList<ApplicationEntity> remoteArchiveAETitles = 
                    listBestExternalLocation(storeas, externalLoc, context.getLocalAE());
            if(remoteArchiveAETitles.isEmpty())
                return null;
            for (int i = 0; i < remoteArchiveAETitles.size(); i++) {
                if (service.getWadoFetchService().fetchInstance(
                        this.context.getLocalAE(),
                        remoteArchiveAETitles.get(i),
                        externalLoc.getStudyInstanceUID(),
                        externalLoc.getSeriesInstanceUID(), 
                        externalLoc.iuid,
                        new InstanceAvailableCallback() {

                            @Override
                            public void onInstanceAvailable(
                                    ArchiveInstanceLocator inst) {
                                ArrayList<ArchiveInstanceLocator> matches
                                = new ArrayList<ArchiveInstanceLocator>();
                                matches.add(inst);
                                push(externallyAvailable.size(), matches,
                                        storeas, priority, response);
                            }
                        }) != null)
                    break;
            }
        }
        }
        return response;
    }

    private ArrayList<ApplicationEntity> listBestExternalLocation(
            Association storeas, ArchiveInstanceLocator externalLoc, ApplicationEntity localAE) {
        ArrayList<Device> externalDevices = new ArrayList<Device>();
        ArrayList<ApplicationEntity> externalAEs = new ArrayList<ApplicationEntity>();
      //for ordering based on availability
        ArrayList<ExternalLocationTuple> extLocTuples = (ArrayList<ExternalLocationTuple>) externalLoc
                .getExternalLocators();
        if(extLocTuples.size() > 1)
        Collections.sort(extLocTuples, fetchAvailabilityComparator());
                        //for ordering based on priority
        for(ExternalLocationTuple externalTuple : extLocTuples) {
                try {
                    externalDevices.add(service.getConfig()
                            .findDevice(externalTuple.getRetrieveDeviceName()));
                } catch (ConfigurationException e) {
                    LOG.error("Unable to find external archive {} in configuration",
                            externalTuple.getRetrieveDeviceName());
                }
        }
        if(externalDevices.size() > 1)
        Collections.sort(externalDevices, fetchDevicePriorityComparator(localAE) );
        for(Device dev : externalDevices) {
            TransferCapability tc = new TransferCapability("",
                    externalLoc.cuid, Role.SCP, new String[]{});
            ArrayList<ApplicationEntity> deviceAEs = (ArrayList<ApplicationEntity>) 
                    dev.getAEsSupportingTransferCapability(tc, true); 
                    Collections.sort(deviceAEs,fetchAEPriorityComparator());
            externalAEs.addAll(deviceAEs);
        }
            
        return externalAEs;
    }



    private Comparator<? super ExternalLocationTuple> fetchAvailabilityComparator() {
        return new Comparator<ExternalLocationTuple>() {
            @Override
            public int compare(ExternalLocationTuple loc1, ExternalLocationTuple loc2) {
                return loc1.getAvailability().compareTo(loc2.getAvailability());
            }
        };
    }

    private Comparator<? super Device> fetchDevicePriorityComparator(final ApplicationEntity localAE) {
        return new Comparator<Device>() {
            @Override
            public int compare(Device dev1, Device dev2) {
                ArchiveDeviceExtension archDevExt = localAE.getDevice()
                        .getDeviceExtension(ArchiveDeviceExtension.class);
                int priority1 = Integer.parseInt(archDevExt.getExternalArchivesMap().get(dev1.getDeviceName()));
                int priority2 = Integer.parseInt(archDevExt.getExternalArchivesMap().get(dev2.getDeviceName()));
                return priority1 < priority2 ? -1:priority1 == priority2 ? 0 : 1;
            }
        };
    }

    private Comparator<ApplicationEntity> fetchAEPriorityComparator() {
        return new Comparator<ApplicationEntity>() {
            @Override
            public int compare(ApplicationEntity ae1, ApplicationEntity ae2) {
                int priority1 = ae1.getAEExtension(ExternalArchiveAEExtension.class).getAeFetchPriority();
                int priority2 = ae2.getAEExtension(ExternalArchiveAEExtension.class).getAeFetchPriority();
                return priority1 < priority2 ? -1:priority1 == priority2 ? 0 : 1;
            }
        };
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
    @Override
    protected DataWriter createDataWriter(ArchiveInstanceLocator inst,
            String tsuid) throws IOException, UnsupportedStoreSCUException {
        if (inst == null || !(inst instanceof ArchiveInstanceLocator))
            throw new UnsupportedStoreSCUException("Unable to send instance");

        ArchiveAEExtension arcAEExt = context.getLocalAE().getAEExtension(
                ArchiveAEExtension.class);

        Attributes attrs = null;
        do {
            try {
                attrs = readFrom(inst);
            } catch (IOException e) {
                LOG.info("Failed to read Data Set with iuid={} from {}@{}",
                        inst.iuid, inst.getFilePath(), inst.getStorageSystem(), e);
                inst = inst.getFallbackLocator();
                if (inst == null) {
                    throw e;
                }
                LOG.info("Try to read Data Set from alternative location");
            }
        } while (attrs == null);

        // check for suppression criteria
        String templateURI = arcAEExt.getRetrieveSuppressionCriteria()
                .getSuppressionCriteriaMap().get(context.getRemoteAE().getAETitle());
        if (templateURI != null)
            inst = service.applySuppressionCriteria(inst, attrs, templateURI,
                    context);

        service.coerceFileBeforeMerge(inst, attrs, context);

        attrs = Utils.mergeAndNormalize(attrs, (Attributes) inst.getObject());


        service.coerceAttributes(attrs, context);
        if (!tsuid.equals(inst.tsuid))
            Decompressor.decompress(attrs, inst.tsuid);
        return new DataWriterAdapter(attrs);
    }

    private Attributes readFrom(ArchiveInstanceLocator inst) throws IOException {

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
            return din.readDataset(-1, stopTag);
        }
    }

    @Override
    protected String selectTransferSyntaxFor(Association storeas,
            ArchiveInstanceLocator inst) throws UnsupportedStoreSCUException {
        Set<String> acceptedTransferSyntax = storeas
                .getTransferSyntaxesFor(inst.cuid);
        // check for SOP classes elimination
        if (context.getArchiveAEExtension().getRetrieveSuppressionCriteria()
                .isCheckTransferCapabilities()) {
            inst = service.eliminateUnSupportedSOPClasses(inst, context);

            // check if eliminated then throw exception
            if (inst == null)
                throw new UnsupportedStoreSCUException(
                        "Unable to send instance, SOP class not configured");

            if (isConfiguredAndAccepted(inst,
                    storeas.getTransferSyntaxesFor(inst.cuid)))
                return inst.tsuid;
            else
                return getDefaultConfiguredTransferSyntax(inst);
        }

        if (acceptedTransferSyntax.contains(inst.tsuid))
            return inst.tsuid;

        return storeas.getTransferSyntaxesFor(inst.cuid).contains(
                UID.ExplicitVRLittleEndian) ? UID.ExplicitVRLittleEndian
                : UID.ImplicitVRLittleEndian;
    }

    private boolean isConfiguredAndAccepted(InstanceLocator ref,
            Set<String> negotiated) {
        ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                context.getRemoteAE().getTransferCapabilitiesWithRole(Role.SCU));
        for (TransferCapability supportedTC : aeTCs) {
            if (ref.cuid.compareTo(supportedTC.getSopClass()) == 0
                    && supportedTC.containsTransferSyntax(ref.tsuid)
                    && negotiated.contains(ref.tsuid)) {
                return true;
            }
        }
        return false;
    }

    private String getDefaultConfiguredTransferSyntax(InstanceLocator ref) {
        ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                context.getRemoteAE().getTransferCapabilitiesWithRole(Role.SCU));
        for (TransferCapability supportedTC : aeTCs) {
            if (ref.cuid.compareTo(supportedTC.getSopClass()) == 0) {
                return supportedTC
                        .containsTransferSyntax(UID.ExplicitVRLittleEndian) ? UID.ExplicitVRLittleEndian
                        : UID.ImplicitVRLittleEndian;
            }
        }
        return UID.ImplicitVRLittleEndian;
    }
    private void push(int instanceCount,
            java.util.List<ArchiveInstanceLocator> instances,
            Association storeas, int priority, BasicCStoreSCUResp resp) {
        BasicCStoreSCUResp storeResp = super.cstore(instances, storeas, priority);
        resp.setCompleted(resp.getCompleted() + storeResp.getCompleted());
        resp.setFailed(resp.getFailed() + storeResp.getFailed());
        
        if(resp.getFailedUIDs() !=null)
            resp.setFailedUIDs(updateFailed(resp.getFailedUIDs(),
                    storeResp.getFailedUIDs()));
        else
        resp.setFailedUIDs(storeResp.getFailedUIDs());
        
        resp.setWarning(resp.getWarning() + storeResp.getWarning());
        resp.setStatus(storeResp.getStatus());
        super.nr_instances = instanceCount;
        if(resp.getCompleted() == instanceCount)
            super.status = Status.Success;
        else
        super.status = Status.Pending;
        super.setChanged();
    }

    private String[] updateFailed(String[] failedUIDs, String[] failedUIDs2) {
        String[] result = Arrays.copyOf(failedUIDs, failedUIDs.length + failedUIDs2.length);
        System.arraycopy(failedUIDs2, 0, result, failedUIDs.length, failedUIDs2.length);
        return result;
    }
}
