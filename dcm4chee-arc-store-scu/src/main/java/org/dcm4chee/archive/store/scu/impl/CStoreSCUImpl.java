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
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import org.dcm4che3.net.Status;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.BasicCStoreSCU;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4che3.net.service.CStoreSCU;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.fetch.forward.FetchForwardCallBack;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
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
            final java.util.List<ArchiveInstanceLocator> instances,
            final Association storeas, final int priority) {

        ArrayList<ArchiveInstanceLocator> localyAvailable = (ArrayList<ArchiveInstanceLocator>) filterLocalOrExternalMatches(
                instances, true);
        ArrayList<ArchiveInstanceLocator> externallyAvailable = (ArrayList<ArchiveInstanceLocator>) filterLocalOrExternalMatches(
                instances, false);
        BasicCStoreSCUResp responseForLocalyAvailable = null;

        if(!localyAvailable.isEmpty())
        responseForLocalyAvailable = super.cstore(
                localyAvailable, storeas, priority);
        //initialize remaining response
        BasicCStoreSCUResp finalResponse = extendResponse(responseForLocalyAvailable);
        
        if (!externallyAvailable.isEmpty()) {
            WebServiceAEExtension wsAEExt = context.getLocalAE()
                    .getAEExtension(WebServiceAEExtension.class);
            if(wsAEExt != null && wsAEExt.getWadoRSBaseURL() != null) {
            finalResponse = service.getFetchForwardService().fetchForwardUsingWado(instances.size(), finalResponse, externallyAvailable, storeas, priority, new FetchForwardCallBack() {
                
                @Override
                public void onFetch(Collection<ArchiveInstanceLocator> instances,
                        BasicCStoreSCUResp resp) {
                    push(instances.size(), (List<ArchiveInstanceLocator>) instances,
                            storeas, priority, resp);
                }
            });
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
            else {
                finalResponse = service.getFetchForwardService().fetchForwardUsingCmove(instances.size(), finalResponse, externallyAvailable, storeas, priority, new FetchForwardCallBack() {
                    
                    @Override
                    public void onFetch(Collection<ArchiveInstanceLocator> instances,
                            BasicCStoreSCUResp resp) {
                       pushInstances((ArrayList<ArchiveInstanceLocator>) instances, storeas, priority);
                    }
                });
                if (failed.size() > 0) {
                    if (failed.size() == nr_instances)
                        status = Status.UnableToPerformSubOperations;
                    else
                        status = Status.OneOrMoreFailures;
                } else {
                    status = Status.Success;
                }
            }
        }
        return finalResponse;

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

    private BasicCStoreSCUResp extendResponse(
            BasicCStoreSCUResp responseForLocalyAvailable) {
        BasicCStoreSCUResp externalResponse = new BasicCStoreSCUResp();
        externalResponse.setCompleted(responseForLocalyAvailable != null? 
                responseForLocalyAvailable.getCompleted() : 0);
        externalResponse.setFailed(responseForLocalyAvailable != null?
                responseForLocalyAvailable.getFailed() : 0);
        externalResponse.setFailedUIDs(responseForLocalyAvailable != null? responseForLocalyAvailable.getFailedUIDs() == null
                ? new String[]{} : responseForLocalyAvailable.getFailedUIDs() : new String[] {});
        externalResponse.setWarning(responseForLocalyAvailable != null?
                responseForLocalyAvailable.getWarning() : 0);
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

    private void pushInstances(ArrayList<ArchiveInstanceLocator> instances, Association storeas, int priority) {
        super.cstore(instances, storeas, priority);
    }

    private void push(int instanceCount,
            java.util.List<ArchiveInstanceLocator> instances,
            Association storeas, int priority, final BasicCStoreSCUResp resp) {
        BasicCStoreSCUResp storeResp = super.cstore(instances, storeas, priority);
        resp.setCompleted(resp.getCompleted()+1);
        resp.setFailed(resp.getFailed() + storeResp.getFailed());
        
        if(resp.getFailedUIDs() !=null && storeResp.getFailedUIDs() != null)
            resp.setFailedUIDs(updateFailed(resp.getFailedUIDs(),
                    storeResp.getFailedUIDs()));
        else
        resp.setFailedUIDs(storeResp.getFailedUIDs() == null ? 
                new String[] {} : storeResp.getFailedUIDs());
        
        resp.setWarning(resp.getWarning() + storeResp.getWarning());
        resp.setStatus(storeResp.getStatus());
        super.nr_instances = instanceCount;
        super.status = Status.Pending;
        super.setChanged();
        super.notifyObservers();
    }

    private String[] updateFailed(String[] failedUIDs, String[] failedUIDs2) {
        String[] result = Arrays.copyOf(failedUIDs, failedUIDs.length + failedUIDs2.length);
        System.arraycopy(failedUIDs2, 0, result, failedUIDs.length, failedUIDs2.length);
        return result;
    }

}
