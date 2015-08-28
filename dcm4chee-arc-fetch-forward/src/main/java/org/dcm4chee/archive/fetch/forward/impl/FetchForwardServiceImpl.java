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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4chee.archive.fetch.forward.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.ExternalArchiveAEExtension;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ExternalLocationTuple;
import org.dcm4chee.archive.fetch.forward.FetchForwardCallBack;
import org.dcm4chee.archive.fetch.forward.FetchForwardEJB;
import org.dcm4chee.archive.fetch.forward.FetchForwardService;
import org.dcm4chee.archive.retrieve.scu.CMoveSCUService;
import org.dcm4chee.archive.wado.client.InstanceAvailableCallback;
import org.dcm4chee.archive.wado.client.WadoClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
@ApplicationScoped
public class FetchForwardServiceImpl implements FetchForwardService {

    private static final Logger LOG  = LoggerFactory.getLogger(FetchForwardServiceImpl.class);
    @Inject
    private CMoveSCUService cmoveSCUService;

    @Inject
    private Device device;

    @Inject
    private FetchForwardEJB ejb;

    @Inject
    private WadoClientService wadoClientService;

    @Inject
    private DicomConfiguration config;

    @Override
    public List<ArchiveInstanceLocator> fetchForwardUsingCmove(String localAETitle,
            List<ArchiveInstanceLocator> externallyAvailable,
            FetchForwardCallBack callBack) {
        final ArrayList<ArchiveInstanceLocator> failedInstances = new ArrayList<ArchiveInstanceLocator>();
        ApplicationEntity localAE = device.getApplicationEntity(localAETitle);
        final HashMap<ArchiveInstanceLocator, ArrayList<ApplicationEntity>> instanceRetrieveMap = new HashMap<ArchiveInstanceLocator, ArrayList<ApplicationEntity>>();
        for (int current = 0; current < externallyAvailable.size(); current++) {
            final ArchiveInstanceLocator externalLoc = externallyAvailable
                    .get(current);
            ArrayList<ApplicationEntity> remoteArchiveAETitles = listBestExternalLocation(externalLoc, localAE);
            if (remoteArchiveAETitles.isEmpty())
                continue;
            // collect retrieveMap
            instanceRetrieveMap.put(externalLoc, remoteArchiveAETitles);
        }
        ApplicationEntity fetchAE;
        try {
            fetchAE = getFetchAE(localAE);
        } catch (ConfigurationException e) {
            LOG.error("Unable to get fetchAE from configuration for device {}",
                    device);
            
            for(ArchiveInstanceLocator loc : externallyAvailable)
            failedInstances.add(loc);
            
            return failedInstances;
        }
        HashMap<String, Integer> studyUIDs = toStudyUIDs(instanceRetrieveMap
                .keySet());
        for (String studyUID : studyUIDs.keySet()) {
            cmoveSCUService
                    .moveStudy(
                            fetchAE,
                            studyUID,
                            studyUIDs.get(studyUID),
                            null,
                            getPreferedStudyLocationsList(instanceRetrieveMap,
                                    studyUID), fetchAE.getAETitle());
        }
        ArrayList<ArchiveInstanceLocator> updatedLocators = new ArrayList<ArchiveInstanceLocator>(); 
        for(Iterator<ArchiveInstanceLocator> iter = externallyAvailable.iterator(); iter.hasNext();) {
            ArchiveInstanceLocator current = iter.next();
            ArchiveInstanceLocator newLocator = ejb.updateLocator(current);
            if(newLocator != null)
                updatedLocators.add(newLocator);
            else 
                failedInstances.add(current);
        }
        //send fetched instances
        callBack.onFetch(updatedLocators, null);
        return failedInstances;
    }

    @Override
    public BasicCStoreSCUResp fetchForwardUsingCmove(final int allInstances,
            final BasicCStoreSCUResp finalResponse,
            List<ArchiveInstanceLocator> externallyAvailable,
            final Association storeas, final int priority, FetchForwardCallBack callBack) {
        ApplicationEntity localAE = device.getApplicationEntity(storeas.getLocalAET());
        final HashMap<ArchiveInstanceLocator, ArrayList<ApplicationEntity>> instanceRetrieveMap = new HashMap<ArchiveInstanceLocator, ArrayList<ApplicationEntity>>();
        for (int current = 0; current < externallyAvailable.size(); current++) {
            final ArchiveInstanceLocator externalLoc = externallyAvailable
                    .get(current);
            ArrayList<ApplicationEntity> remoteArchiveAETitles = listBestExternalLocation(externalLoc, localAE);
            if (remoteArchiveAETitles.isEmpty())
                continue;
            // collect retrieveMap
            instanceRetrieveMap.put(externalLoc, remoteArchiveAETitles);
        }
        ApplicationEntity fetchAE;
        try {
            fetchAE = getFetchAE(localAE);
        } catch (ConfigurationException e) {
            LOG.error("Unable to get fetchAE from configuration for device {}",
                    device);
            return finalResponse;
        }
        HashMap<String, Integer> studyUIDs = toStudyUIDs(instanceRetrieveMap
                .keySet());
        for (String studyUID : studyUIDs.keySet()) {
            cmoveSCUService
                    .moveStudy(
                            fetchAE,
                            studyUID,
                            studyUIDs.get(studyUID),
                            null,
                            getPreferedStudyLocationsList(instanceRetrieveMap,
                                    studyUID), fetchAE.getAETitle());
        }
        ArrayList<ArchiveInstanceLocator> updatedLocators = new ArrayList<ArchiveInstanceLocator>(); 
        for(Iterator<ArchiveInstanceLocator> iter = externallyAvailable.iterator(); iter.hasNext();) {
            ArchiveInstanceLocator newLocator = ejb.updateLocator(iter.next());
            if(newLocator != null)
                updatedLocators.add(newLocator);
        }
        //send fetched instances
        callBack.onFetch(updatedLocators, finalResponse);
        
        return finalResponse;
    }

    @Override
    public ArrayList<ArchiveInstanceLocator> fetchForward(String localAETitle,
            List<ArchiveInstanceLocator> externallyAvailable, 
            final FetchForwardCallBack wadoFetchCallBack, 
            final FetchForwardCallBack moveFetchCallBack) {
        ArrayList<String> sentInstances = new ArrayList<String>();
        final ArrayList<ArchiveInstanceLocator> failedInstances = new ArrayList<ArchiveInstanceLocator>();
        ApplicationEntity localAE = device.getApplicationEntity(localAETitle);
        final HashMap<ArchiveInstanceLocator, ArrayList<ApplicationEntity>> instanceRetrieveMap = new HashMap<ArchiveInstanceLocator, ArrayList<ApplicationEntity>>();
        final ArrayList<ArchiveInstanceLocator> updatedLocators = new ArrayList<ArchiveInstanceLocator>();
        for (int current = 0; current < externallyAvailable.size(); current++) {
            final ArchiveInstanceLocator externalLoc = externallyAvailable
                    .get(current);
            
            if(sentInstances.contains(externalLoc.iuid))
                continue;
            
            ArrayList<ApplicationEntity> remoteArchiveAEs = listBestExternalLocation(externalLoc, localAE);
            if (remoteArchiveAEs.isEmpty())
                continue;
            // collect retrieveMap
            instanceRetrieveMap.put(externalLoc, remoteArchiveAEs);
            for (int i = 0; i < remoteArchiveAEs.size(); i++) {
                //pick protocol
                WebServiceAEExtension webAEExt = remoteArchiveAEs.get(i).getAEExtension(WebServiceAEExtension.class);
                if(webAEExt != null && webAEExt.getWadoRSBaseURL()!=null) {
                if (wadoClientService.fetchInstance(
                        localAE,
                        remoteArchiveAEs.get(i),
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
                                if(wadoFetchCallBack != null)
                                wadoFetchCallBack.onFetch(matches, null);
                                updatedLocators.add(inst);
                            }
                        }) != null)
                    break;
            }
                else {
                    ApplicationEntity fetchAE;
                    try {
                        fetchAE = getFetchAE(localAE);
                    } catch (ConfigurationException e) {
                        LOG.error("Unable to get fetchAE from configuration for device {}",
                                device);
                        
                        for(ArchiveInstanceLocator loc : externallyAvailable)
                        failedInstances.add(loc);
                        
                        return failedInstances;
                    }
                    HashMap<String, Integer> studyUIDs = toStudyUIDs(instanceRetrieveMap
                            .keySet());
                    for (String studyUID : studyUIDs.keySet()) {
                        cmoveSCUService
                                .moveStudy(
                                        fetchAE,
                                        studyUID,
                                        studyUIDs.get(studyUID),
                                        null,
                                        getPreferedStudyLocationsList(instanceRetrieveMap,
                                                studyUID), fetchAE.getAETitle());
                    }
                    for(Iterator<ArchiveInstanceLocator> iter = externallyAvailable.iterator(); iter.hasNext();) {
                        ArchiveInstanceLocator currentLocation = iter.next();
                        ArchiveInstanceLocator newLocator = ejb.updateLocator(currentLocation);
                        if(newLocator != null)
                            updatedLocators.add(newLocator);
                        else 
                            failedInstances.add(currentLocation);
                    }
                    //send fetched instances
                    if(moveFetchCallBack != null)
                    moveFetchCallBack.onFetch(updatedLocators, null);
                    for(ArchiveInstanceLocator loc : updatedLocators)
                    sentInstances.add(loc.iuid);
                    break;
                }
            }
        }
        externallyAvailable.clear();
         externallyAvailable.addAll(updatedLocators);
        return failedInstances;
        }


    @Override
    public ArrayList<ArchiveInstanceLocator> fetchForwardUsingWado(
            String localAETitle,
            List<ArchiveInstanceLocator> externallyAvailable,
            final FetchForwardCallBack callBack) {
        final ArrayList<ArchiveInstanceLocator> failedInstances = new ArrayList<ArchiveInstanceLocator>();
        ApplicationEntity localAE = device.getApplicationEntity(localAETitle);
        for(int current = 0; current < externallyAvailable.size(); current++) {
            final ArchiveInstanceLocator externalLoc = externallyAvailable
                    .get(current);
            ArrayList<ApplicationEntity> remoteArchiveAETitles = 
                    listBestExternalLocation(externalLoc, localAE);
            if(remoteArchiveAETitles.isEmpty())
                continue;
            for (int i = 0; i < remoteArchiveAETitles.size(); i++) {
                if (wadoClientService.fetchInstance(
                        localAE,
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
                                if(inst.getStorageSystem() != null)
                                    callBack.onFetch(matches, null);
                                else
                                    failedInstances.add(inst);
                            }
                        }) != null)
                    break;
            }
    }
        return failedInstances;
    }


    @Override
    public BasicCStoreSCUResp fetchForwardUsingWado(
            final int allInstances, final BasicCStoreSCUResp finalResponse, final List<ArchiveInstanceLocator> externallyAvailable,
            final Association storeas, final int priority, final FetchForwardCallBack callBack) {
        ApplicationEntity localAE = device.getApplicationEntity(storeas.getLocalAET());
        for(int current = 0; current < externallyAvailable.size(); current++) {
            if(storeas.isReadyForDataTransfer()) {
            final ArchiveInstanceLocator externalLoc = externallyAvailable
                    .get(current);
            ArrayList<ApplicationEntity> remoteArchiveAETitles = 
                    listBestExternalLocation(externalLoc, localAE);
            if(remoteArchiveAETitles.isEmpty())
                continue;
            for (int i = 0; i < remoteArchiveAETitles.size(); i++) {
                if (wadoClientService.fetchInstance(
                        localAE,
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
                                callBack.onFetch(matches, finalResponse);
                            }
                        }) != null)
                    break;
            }
        }
        }

        return finalResponse;
    }

    @Override
    public BasicCStoreSCUResp fetchForward(
            final int allInstances, final BasicCStoreSCUResp finalResponse, final List<ArchiveInstanceLocator> externallyAvailable,
            final Association storeas, final int priority, final FetchForwardCallBack wadoFetchCallBack, 
            final FetchForwardCallBack moveFetchCallBack) {
        final HashMap<ArchiveInstanceLocator, ArrayList<ApplicationEntity>> instanceRetrieveMap = new HashMap<ArchiveInstanceLocator, ArrayList<ApplicationEntity>>();
        ArrayList<String> sentInstances = new ArrayList<String>();
        ApplicationEntity localAE = device.getApplicationEntity(storeas.getLocalAET());
        for(int current = 0; current < externallyAvailable.size(); current++) {
            if(storeas.isReadyForDataTransfer()) {
            final ArchiveInstanceLocator externalLoc = externallyAvailable
                    .get(current);
            
            if(sentInstances.contains(externalLoc.iuid))
                continue;
            
            ArrayList<ApplicationEntity> remoteArchiveAEs = 
                    listBestExternalLocation(externalLoc, localAE);
            if(remoteArchiveAEs.isEmpty())
                continue;

            instanceRetrieveMap.put(externalLoc, remoteArchiveAEs);
            for (int i = 0; i < remoteArchiveAEs.size(); i++) {
                //pick protocol
                WebServiceAEExtension webAEExt = remoteArchiveAEs.get(i).getAEExtension(WebServiceAEExtension.class);
                if(webAEExt != null && webAEExt.getWadoRSBaseURL()!=null) {
                if (wadoClientService.fetchInstance(
                        localAE,
                        remoteArchiveAEs.get(i),
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
                                wadoFetchCallBack.onFetch(matches, finalResponse);
                            }
                        }) != null)
                    break;
            }
                else {
                    ApplicationEntity fetchAE;
                    try {
                        fetchAE = getFetchAE(localAE);
                    } catch (ConfigurationException e) {
                        LOG.error("Unable to get fetchAE from configuration for device {}",
                                device);
                        return finalResponse;
                    }
                    HashMap<String, Integer> studyUIDs = toStudyUIDs(instanceRetrieveMap
                            .keySet());
                    for (String studyUID : studyUIDs.keySet()) {
                        cmoveSCUService
                                .moveStudy(
                                        fetchAE,
                                        studyUID,
                                        studyUIDs.get(studyUID),
                                        null,
                                        getPreferedStudyLocationsList(instanceRetrieveMap,
                                                studyUID), fetchAE.getAETitle());
                    }
                    ArrayList<ArchiveInstanceLocator> updatedLocators = new ArrayList<ArchiveInstanceLocator>(); 
                    for(Iterator<ArchiveInstanceLocator> iter = externallyAvailable.iterator(); iter.hasNext();) {
                        ArchiveInstanceLocator newLocator = ejb.updateLocator(iter.next());
                        if(newLocator != null)
                            updatedLocators.add(newLocator);
                    }
                    //send fetched instances
                    moveFetchCallBack.onFetch(updatedLocators, finalResponse);
                    
                    for(ArchiveInstanceLocator loc : updatedLocators)
                    sentInstances.add(loc.iuid);
                    break;
                }
            }
        }
        }

        return finalResponse;
    }


    private ApplicationEntity getFetchAE(ApplicationEntity localAE) throws ConfigurationException {
        return localAE
                .getDevice()
                .getApplicationEntity(
                        localAE
                                .getDevice()
                                .getDeviceExtension(
                                        ArchiveDeviceExtension.class)
                                .getFetchAETitle());
    }

    private HashMap<String, Integer> toStudyUIDs(Set<ArchiveInstanceLocator> set) {
        HashMap<String, Integer> studyUIDs = new HashMap<String, Integer>(); 
        for(ArchiveInstanceLocator loc : set) {
            String studyUID = loc.getStudyInstanceUID();
            if(!studyUIDs.containsKey(studyUID))
                studyUIDs.put(studyUID, 1);
            else
                studyUIDs.put(studyUID, new Integer(studyUIDs.get(studyUID).intValue() + 1));
        }
        return studyUIDs;
    }

    private ArrayList<ApplicationEntity> listBestExternalLocation(ArchiveInstanceLocator externalLoc, ApplicationEntity localAE) {
        ArrayList<ApplicationEntity> externalAEs = new ArrayList<ApplicationEntity>();
        
        // for ordering based on availability
        List<ExternalLocationTuple> extLocTuples = externalLoc.getExternalLocators();
        Collections.sort(extLocTuples, fetchAvailabilityComparator());
        
        List<Device> extDevices = new ArrayList<Device>();
        // for ordering based on priority
        for (ExternalLocationTuple extLocTuple : extLocTuples) {
            String extRetrieveDeviceName = extLocTuple.getRetrieveDeviceName();
            try {
                extDevices.add(config.findDevice(extRetrieveDeviceName));
            } catch (ConfigurationException e) {
                LOG.error("Unable to find external archive {} in configuration", extRetrieveDeviceName);
            }
        }
        Collections.sort(extDevices, fetchDevicePriorityComparator(localAE));
        
        for (Device extDevice : extDevices) {
            TransferCapability tc = new TransferCapability("", externalLoc.cuid, Role.SCP,
                    new String[] { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian });
            List<ApplicationEntity> extDeviceAEs = (List<ApplicationEntity>) extDevice.getAEsSupportingTransferCapability(tc, true);
            Collections.sort(extDeviceAEs, fetchAEPriorityComparator());
            externalAEs.addAll(extDeviceAEs);
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
                return priority1 - priority2;
            }
        };
    }

    private Comparator<ApplicationEntity> fetchAEPriorityComparator() {
        return new Comparator<ApplicationEntity>() {
            @Override
            public int compare(ApplicationEntity ae1, ApplicationEntity ae2) {
                int priority1 = ae1.getAEExtension(ExternalArchiveAEExtension.class).getAeFetchPriority();
                int priority2 = ae2.getAEExtension(ExternalArchiveAEExtension.class).getAeFetchPriority();
                return priority1 - priority2;
            }
        };
    }

    private List<ApplicationEntity> getPreferedStudyLocationsList(
            HashMap<ArchiveInstanceLocator, ArrayList<ApplicationEntity>> instanceRetrieveMap,
            String studyUID) {
        ArrayList<ApplicationEntity> preferedStudyAEs = new ArrayList<ApplicationEntity>();
        for(ArchiveInstanceLocator loc : instanceRetrieveMap.keySet()) {
            if(loc.getStudyInstanceUID().equalsIgnoreCase(studyUID)) {
                for(ApplicationEntity ae : instanceRetrieveMap.get(loc)) {
                    boolean skip = false;
                    for(ApplicationEntity currentAE : preferedStudyAEs) {
                        if(currentAE.getAETitle().equalsIgnoreCase(ae.getAETitle()))
                            skip = true;
                    }
                    if(!skip)
                        preferedStudyAEs.add(ae);
            }
            }
        }
        return preferedStudyAEs ;
    }

    @Override
    public Response redirectRequest(ApplicationEntity redirectAE, String queryString) {
        WebServiceAEExtension wsAEExt = redirectAE.getAEExtension(WebServiceAEExtension.class);
        if (wsAEExt != null) {
            String wadoUri = wsAEExt.getWadoURIBaseURL();
            if (wadoUri != null) {
                wadoUri = wadoUri + "?" + queryString;
                try {
                    return Response.temporaryRedirect(new URI(wadoUri)).build();
                } catch (URISyntaxException e) {
                    LOG.error("Unable to redirect request to {} - Exception {}", wadoUri,
                            e.getMessage());
                    return Response.status(Status.CONFLICT).build();
                }
            } 
        }
            
        return null;
    }

    @Override
    public ApplicationEntity getPrefersForwardingAE(String localAETitle, List<ArchiveInstanceLocator> externalLocations) {
        ApplicationEntity localAE = device.getApplicationEntity(localAETitle);
        for (ArchiveInstanceLocator loc : externalLocations) {
            List<ApplicationEntity> externalRetrieveAes = listBestExternalLocation(loc, localAE);
            for (ApplicationEntity ae : externalRetrieveAes) {
                ExternalArchiveAEExtension extArchiveAEExtension = ae
                        .getAEExtension(ExternalArchiveAEExtension.class);
                if (extArchiveAEExtension != null && extArchiveAEExtension.isPrefersForwarding()) {
                    return ae;
                }
            }
        }

        return null;
    }

}
