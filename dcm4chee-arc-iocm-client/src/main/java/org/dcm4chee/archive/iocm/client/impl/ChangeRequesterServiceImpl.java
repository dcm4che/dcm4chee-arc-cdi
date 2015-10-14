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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4che3.net.web.WebServiceAEExtension;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.IOCMConfig;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.QCEventInstance;
import org.dcm4chee.archive.dto.Service;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.fetch.forward.FetchForwardCallBack;
import org.dcm4chee.archive.fetch.forward.FetchForwardService;
import org.dcm4chee.archive.iocm.client.ChangeRequesterService;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUResponse;
import org.dcm4chee.archive.store.verify.StoreVerifyService;
import org.dcm4chee.archive.stow.client.StowContext;
import org.dcm4chee.archive.stow.client.StowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 */
@ApplicationScoped
public class ChangeRequesterServiceImpl implements ChangeRequesterService {

    private static Logger LOG = LoggerFactory.getLogger(ChangeRequesterServiceImpl.class);

    @Inject
    private Device device;
    
    @Inject
    private StoreVerifyService storeVerify;
    
    @Inject
    private RetrieveService retrieveService;
    
    @Inject
    private FetchForwardService fetchForwardService;

    @PersistenceContext(name = "dcm4chee-arc", unitName = "dcm4chee-arc")
    private EntityManager em;
    
    @Inject
    private IApplicationEntityCache aeCache;

    private transient ArchiveDeviceExtension archDeviceExt;
    
    public void scheduleChangeRequest(List<QCEventInstance> sourceInstanceUIDs, List<QCEventInstance> updatedInstanceUIDs, Instance rejNote) {
        LOG.debug("ChangeRequestor: scheduleChangeRequest called! rejNote:{}\nupdated:{}", rejNote, updatedInstanceUIDs);
        IOCMConfig cfg = getIOCMConfig();
        if (cfg == null) {
            LOG.info("IOCMConfig not configured! Skipped!");
            return;
        }
        ApplicationEntity callingAE = device.getApplicationEntity(cfg.getCallingAET());
        String[] targets = cfg.getIocmDestinations();
        LOG.debug("targetAETs from IOCMConfig:{}", Arrays.toString(targets));
        Collection<String> storeRememberAETs = this.getStoreAndRememberAETitles(sourceInstanceUIDs);
        if (rejNote != null) {
            List<ArchiveInstanceLocator> locators = locate(rejNote.getSopInstanceUID());
            scheduleStore(callingAE, targets, locators, storeRememberAETs);
        }
        if (updatedInstanceUIDs != null && updatedInstanceUIDs.size() > 0) {
            List<ArchiveInstanceLocator> locators = locate(toIUIDArray(updatedInstanceUIDs));
            scheduleStore(callingAE, targets, locators, storeRememberAETs);

            String[] noneIOCM = cfg.getNoneIocmDestinations();
            LOG.debug("NoneIocmDestinations from IOCMConfig:{}", Arrays.toString(noneIOCM));
            if (noneIOCM != null && noneIOCM.length > 0) {
            	scheduleStore(callingAE, noneIOCM, locators, storeRememberAETs);
            }
        }

    }

	private List<ArchiveInstanceLocator> scheduleStore(ApplicationEntity callingAE, String[] targets,
			final List<ArchiveInstanceLocator> locators, Collection<String> storeRememberAETs) {
		List<ArchiveInstanceLocator> failedForward = null;
		List<ArchiveInstanceLocator> externalLocators = extractExternalLocators(locators);
        if(!externalLocators.isEmpty()) {
        	LOG.info("Perform fetchForward of {} external instances", externalLocators.size());
            FetchForwardCallBack fetchCallBack = new FetchForwardCallBack() {
                @Override
                public void onFetch(Collection<ArchiveInstanceLocator> instances,
                        BasicCStoreSCUResp resp) {
                	locators.addAll(instances);
                }
            };
            failedForward = fetchForwardService.fetchForward(archDeviceExt.getDefaultAETitle(), externalLocators, fetchCallBack, fetchCallBack);
            LOG.info("FetchForward finished!");
        }
		for (String target : targets) {
			ApplicationEntity targetAE;
			try {
				targetAE = aeCache.findApplicationEntity(target);
			} catch (ConfigurationException e) {
				LOG.error("Target AE Title {} not found in configuration! skipped.");
				continue;
			}
			WebServiceAEExtension wsExt = targetAE.getAEExtension(WebServiceAEExtension.class);
			ServiceType serviceType = storeRememberAETs.contains(target) ? ServiceType.STOREREMEMBER : ServiceType.IOCMSERVICE;
			if (wsExt != null && wsExt.getStowRSBaseURL() != null) {
				LOG.info("Store objects to {} and verify with ", wsExt.getStowRSBaseURL(), wsExt.getQidoRSBaseURL());
				StowContext stowCtx = new StowContext(callingAE, targetAE, serviceType );
				stowCtx.setStowRemoteBaseURL(wsExt.getStowRSBaseURL());
				stowCtx.setQidoRemoteBaseURL(wsExt.getQidoRSBaseURL());
				storeVerify.scheduleStore(null, stowCtx, locators);
			} else {
				LOG.info("Store objects to {}", targetAE.getAETitle());
				storeVerify.scheduleStore(null, new CStoreSCUContext(callingAE, targetAE, serviceType), 
		            locators );
			}
			LOG.info("Store finished");
		}
		return failedForward == null ? new ArrayList<ArchiveInstanceLocator>() : failedForward;
	}

	public void scheduleUpdateOnlyChangeRequest(List<QCEventInstance> updatedInstanceUIDs) {
        if (updatedInstanceUIDs == null || updatedInstanceUIDs.isEmpty()) {
            LOG.info("No updated instance UIDs given! Skipped!");
            return;
        }
        IOCMConfig cfg = getIOCMConfig();
        if (cfg == null) {
            LOG.info("IOCMConfig not configured! Skipped!");
            return;
        }
        String[] noneIOCM = cfg.getNoneIocmDestinations();
        Collection<String> storeRememberAETs = this.getStoreAndRememberAETitles(updatedInstanceUIDs);
        LOG.debug("NoneIocmDestinations from IOCMConfig:{}", Arrays.toString(noneIOCM));
        if (noneIOCM != null && noneIOCM.length > 0) {
            List<ArchiveInstanceLocator> locators = locate(toIUIDArray(updatedInstanceUIDs));
        	scheduleStore(device.getApplicationEntity(cfg.getCallingAET()), 
        			noneIOCM, locators, storeRememberAETs);
        }
    }
    
    private IOCMConfig getIOCMConfig() {
        if (archDeviceExt == null) {
            archDeviceExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
            if (archDeviceExt == null)
                return null;
        }
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

    private List<ArchiveInstanceLocator> extractExternalLocators(List<ArchiveInstanceLocator> locators) {
    	ArrayList<ArchiveInstanceLocator> externalLocators = new ArrayList<ArchiveInstanceLocator>();
    	for (Iterator<ArchiveInstanceLocator> iter = locators.iterator(); iter.hasNext();) {
    		ArchiveInstanceLocator loc = iter.next();
    		if (loc.getStorageSystem() == null) {
    			externalLocators.add(loc);
    			iter.remove();
    		}
    	}
    	return externalLocators;
    }
    
    private Collection<String> getStoreAndRememberAETitles(List<QCEventInstance> qcEventInstances) {
    	List<String> uids = new ArrayList<String>(qcEventInstances.size());
    	for (QCEventInstance qci : qcEventInstances) {
    		uids.add(qci.getSopInstanceUID());
    	}
    	Query q = em.createQuery("SELECT DISTINCT i.retrieveAETs from Instance i "
                    + " where i.sopInstanceUID IN ?1");
    	q.setParameter(1, uids);
    	@SuppressWarnings("unchecked")
		List<String> retrieveAETs = (List<String>) q.getResultList();
    	HashSet<String> allRetrieveAETs = new HashSet<String>(retrieveAETs.size());
    	for (String aets : retrieveAETs) {
    		for (String aet : StringUtils.split(aets, '\\')) {
    			allRetrieveAETs.add(aet);
    		}
    	}
    	Collection<String> localAETitles = device.getApplicationAETitles();
    	allRetrieveAETs.removeAll(localAETitles);
    	return allRetrieveAETs;
    }

    private String[] toIUIDArray(Collection<QCEventInstance> qcEventInstances) {
        String[] arr = new String[qcEventInstances.size()];
        int index = 0;
        for(QCEventInstance inst : qcEventInstances)
            arr[index++] = inst.getSopInstanceUID();
        return arr;
    }
    
    public void verifyStorage(@Observes @Service(ServiceType.IOCMSERVICE) CStoreSCUResponse storeResponse) {
        updateRetrieveAETs(filterFailedLocators(storeResponse), storeResponse.getRemoteAET());
        //TODO - delegate this update to store and verify service
    }

    public void verifyStorage(@Observes @Service(ServiceType.IOCMSERVICE) StowResponse storeResponse) {
        //no remote AET here
    }

    private void updateRetrieveAETs(Collection<ArchiveInstanceLocator> instances, String remoteAET) {
        
        Collection<String> uids = new ArrayList<String>();
        for(ArchiveInstanceLocator arcInst : instances) {
            uids.add(arcInst.iuid);
        }
        
        Query query = em.createNamedQuery(Instance.FIND_BY_SOP_INSTANCE_UID_EAGER_MANY);
        query.setParameter("uids", uids);
        @SuppressWarnings("unchecked")
        List<Instance> results = query.getResultList();
        if(results == null)
            return;
        for(Instance inst : results) {
            if(containsAET(inst.getAllRetrieveAETs(), remoteAET)) {
                if(inst.getAllRetrieveAETs() == null)
                    inst.setRetrieveAETs(remoteAET);
                else
                    inst.addRetrieveAET(remoteAET);
            }
        }
    }

    private boolean containsAET(String[] allRetrieveAETs, String remoteAET) {
        if (allRetrieveAETs != null) {
            for (String aet : allRetrieveAETs)
                if (aet.compareTo(remoteAET) == 0)
                    return true;
        }
        return false;
    }


    private Collection<ArchiveInstanceLocator> filterFailedLocators(CStoreSCUResponse storeResponse) {
        Collection<ArchiveInstanceLocator> sent = new ArrayList<ArchiveInstanceLocator>();
        for(ArchiveInstanceLocator loc : storeResponse.getInstances()) {
            if(!Arrays.asList(storeResponse.getFailedUIDs()).contains(loc.iuid)) {
                sent.add(loc);
            }
        }
        return sent;
    }
}
