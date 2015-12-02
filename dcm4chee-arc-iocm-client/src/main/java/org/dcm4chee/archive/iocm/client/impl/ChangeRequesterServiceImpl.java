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

package org.dcm4chee.archive.iocm.client.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.IOCMConfig;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.fetch.forward.FetchForwardCallBack;
import org.dcm4chee.archive.fetch.forward.FetchForwardService;
import org.dcm4chee.archive.iocm.client.ChangeRequestContext;
import org.dcm4chee.archive.iocm.client.ChangeRequesterService;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.sc.StructuralChangeContext.InstanceIdentifier;
import org.dcm4chee.archive.store.remember.StoreAndRememberContext;
import org.dcm4chee.archive.store.remember.StoreAndRememberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 */
@Stateless
public class ChangeRequesterServiceImpl implements ChangeRequesterService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeRequesterServiceImpl.class);

    @Inject
    private Device device;
    
    @Inject
    private RetrieveService retrieveService;
    
    @Inject
    private FetchForwardService fetchForwardService;

    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;
    
    @Inject
    private DicomConfiguration conf;
    
    @Inject
    private StoreAndRememberService storeAndRememberService;
    
   
    @Override
    public void scheduleChangeRequest(ChangeRequestContext changeRequestCtx) {
        scheduleChangeRequestInt(changeRequestCtx.getUpdatedInstances(), changeRequestCtx.getRejectionNotes(), changeRequestCtx.getExternalStoreAndRememberAETs());
    }
    
    private void scheduleChangeRequestInt(Set<InstanceIdentifier> updatedInstanceUIDs, Set<Instance> rejNotes, Collection<String> extDevicesAETs) {
        LOG.debug("scheduleChangeRequest() called! rejNote:{}\nupdated:{}", rejNotes, updatedInstanceUIDs);
        IOCMConfig cfg = getIOCMConfig();
        if (cfg == null) {
            LOG.info("IOCMConfig not configured! Skipped!");
            return;
        }
        
        ApplicationEntity callingAE = device.getApplicationEntity(cfg.getCallingAET());
        if(callingAE == null) {
            LOG.error("No calling AE configured for IOCM change requests");
            return;
        }
        
        String[] iocmTargetAETs = cfg.getIocmDestinations();
        LOG.debug("IOCM target AE titles from IOCMConfig:{}", Arrays.toString(iocmTargetAETs));
        
        // schedule store of rejection note
        if (rejNotes != null) {
            List<ArchiveInstanceLocator> rejNoteLocator = locate(extractUIDsFromInstances(rejNotes));
            scheduleStore(callingAE, iocmTargetAETs, rejNoteLocator, extDevicesAETs);
        }
        
        // schedule store of updated instances for IOCM & Non-IOCM AEs
        if (updatedInstanceUIDs != null && updatedInstanceUIDs.size() > 0) {
            List<ArchiveInstanceLocator> locators = locate(extractUIDsFromInstanceIds(updatedInstanceUIDs));
            scheduleStore(callingAE, iocmTargetAETs, locators, extDevicesAETs);

            String[] nonIOCMAETs = cfg.getNoneIocmDestinations();
            LOG.debug("NoneIocmDestinations from IOCMConfig:{}", Arrays.toString(nonIOCMAETs));
            if (nonIOCMAETs != null && nonIOCMAETs.length > 0) {
            	scheduleStore(callingAE, nonIOCMAETs, locators, extDevicesAETs);
            }
        }
    }
    
    private Map<String,String> getExternalCoordinates(String... targetAETs) {
        Map<String,String> extCoordinates = new HashMap<>();
        for(String targetAET : targetAETs) {
            ApplicationEntity targetAE = conf.findApplicationEntity(targetAET);
            if(targetAE == null) {
                LOG.error("Could not find AE " + targetAET);
                continue;
            }
            Device targetDevice = targetAE.getDevice();
            extCoordinates.put(targetDevice.getDeviceName(), targetAET);
        }
        
        return extCoordinates;
    }
	
	private void scheduleStore(ApplicationEntity callingAE, String[] targetAETs,
            final List<ArchiveInstanceLocator> instanceLocators, Collection<String> extDeviceAETs) {
        List<ArchiveInstanceLocator> externalLocators = extractAndRemoveExternalLocators(instanceLocators);
        if(!externalLocators.isEmpty()) {
            LOG.info("Perform fetchForward of {} external instances", externalLocators.size());
            FetchForwardCallBack fetchCallBack = new FetchForwardCallBack() {
                @Override
                public void onFetch(Collection<ArchiveInstanceLocator> instances,
                        BasicCStoreSCUResp resp) {
                    instanceLocators.addAll(instances);
                }
            };
            fetchForwardService.fetchForward(getArchiveDeviceExtension().getDefaultAETitle(), externalLocators, fetchCallBack, fetchCallBack);
            LOG.info("FetchForward finished!");
        }
        
        String[] instanceUIDs = new String[instanceLocators.size()];
        for(int i = 0; i < instanceLocators.size(); i++) {
            instanceUIDs[i] = instanceLocators.get(i).iuid;
        }
        
        Map<String,String> extCoordinates = getExternalCoordinates(targetAETs);
        
        for (Entry<String,String> extCoordinate : extCoordinates.entrySet()) {
            String targetDeviceName = extCoordinate.getKey();
            String targetAET = extCoordinate.getValue();
            StoreAndRememberContext storeRememberCxt = storeAndRememberService.createContextBuilder()
                    .instances(instanceUIDs)
                    .externalDeviceName(targetDeviceName)
                    .localAE(callingAE.getAETitle())
                    .remoteAE(targetAET)
                    .remember(extDeviceAETs.contains(targetAET))
                    .build();
            storeAndRememberService.scheduleStoreAndRemember(storeRememberCxt, 0);
        }
    }

	public void scheduleUpdateOnlyChangeRequest(Set<InstanceIdentifier> updatedInstanceUIDs) {
	    Collection<String> extDevicesAETs = getExternalDevicesAETitles(updatedInstanceUIDs);
        scheduleChangeRequestInt(updatedInstanceUIDs, Collections.<Instance>emptySet(), extDevicesAETs);
    }
    
	
	private ArchiveDeviceExtension getArchiveDeviceExtension() {
	    return device.getDeviceExtension(ArchiveDeviceExtension.class);
	}
	
    private IOCMConfig getIOCMConfig() {
        ArchiveDeviceExtension archDeviceExt = getArchiveDeviceExtension();
        return archDeviceExt.getIocmConfig();
    }
    
    private List<ArchiveInstanceLocator> locate(String... iuids) {
        ArchiveDeviceExtension arcDev = device.getDeviceExtension(ArchiveDeviceExtension.class);
        Attributes keys = new Attributes();
        keys.setString(Tag.SOPInstanceUID, VR.UI, iuids);
        QueryParam queryParam = arcDev.getQueryParam();
        QueryRetrieveView view = new QueryRetrieveView();
        view.setViewID("IOCM");
        view.setHideNotRejectedInstances(false);
        queryParam.setQueryRetrieveView(view);
        return retrieveService.calculateMatches(null, keys, queryParam, true);
    }

    private static List<ArchiveInstanceLocator> extractAndRemoveExternalLocators(List<ArchiveInstanceLocator> instanceLocators) {
    	ArrayList<ArchiveInstanceLocator> externalLocators = new ArrayList<ArchiveInstanceLocator>();
    	for (Iterator<ArchiveInstanceLocator> iter = instanceLocators.iterator(); iter.hasNext();) {
    		ArchiveInstanceLocator loc = iter.next();
    		if (loc.getStorageSystem() == null) {
    			externalLocators.add(loc);
    			iter.remove();
    		}
    	}
    	return externalLocators;
    }
    
    private Set<String> getExternalDevicesAETitles(Collection<InstanceIdentifier> instanceIDs) {
    	List<String> uids = new ArrayList<String>(instanceIDs.size());
    	for (InstanceIdentifier qci : instanceIDs) {
    		uids.add(qci.getSopInstanceUID());
    	}
		
		List<String> retrieveAETss = em.createQuery("SELECT DISTINCT i.retrieveAETs from Instance i "
                + " where i.sopInstanceUID IN ?1", String.class)
                .setParameter(1, uids)
                .getResultList();
		
    	HashSet<String> allRetrieveAETss = new HashSet<String>(retrieveAETss.size());
    	for (String retrieveAETs : retrieveAETss) {
    		for (String aet : StringUtils.split(retrieveAETs, '\\')) {
    			allRetrieveAETss.add(aet);
    		}
    	}
    	Collection<String> localAETitles = device.getApplicationAETitles();
    	allRetrieveAETss.removeAll(localAETitles);
    	return allRetrieveAETss;
    }

    private static String[] extractUIDsFromInstanceIds(Collection<InstanceIdentifier> instanceIDs) {
        String[] uids = new String[instanceIDs.size()];
        int index = 0;
        for(InstanceIdentifier instanceID : instanceIDs)
            uids[index++] = instanceID.getSopInstanceUID();
        return uids;
    }
    
    private static String[] extractUIDsFromInstances(Collection<Instance> instances) {
        String[] uids = new String[instances.size()];
        int index = 0;
        for(Instance inst : instances)
            uids[index++] = inst.getSopInstanceUID();
        return uids;
    }

    @Override
    public ChangeRequestContext createChangeRequestContext(Set<InstanceIdentifier> sourceInstanceUIDs, final Set<InstanceIdentifier> updatedInstanceUIDs, final Set<Instance> rejectionNotes) {
        final Set<String> externalStoreAndRememberAETs = getExternalDevicesAETitles(sourceInstanceUIDs);
        return new ChangeRequestContextImpl(updatedInstanceUIDs, rejectionNotes, externalStoreAndRememberAETs);
    }
    
    private static class ChangeRequestContextImpl implements ChangeRequestContext {
        private final Set<InstanceIdentifier> updatedInstanceUIDs;
        private final Set<Instance> rejectionNotes;
        private final Set<String> externalStoreAndRememberAETs;
       
        private ChangeRequestContextImpl(Set<InstanceIdentifier> updatedInstanceUIDs, 
                Set<Instance> rejectionNotes, Set<String> externalStoreAndRememberAETs) {
            this.updatedInstanceUIDs = Collections.unmodifiableSet(updatedInstanceUIDs);
            this.rejectionNotes = Collections.unmodifiableSet(rejectionNotes);
            this.externalStoreAndRememberAETs = Collections.unmodifiableSet(externalStoreAndRememberAETs);
        }
       
        @Override
        public Set<InstanceIdentifier> getUpdatedInstances() {
            return updatedInstanceUIDs;
        }
       
        @Override
        public Set<Instance> getRejectionNotes() {
            return rejectionNotes;
        }
      
        @Override
        public Set<String> getExternalStoreAndRememberAETs() {
            return externalStoreAndRememberAETs;
        }
        
    }
    
}
