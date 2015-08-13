//
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

package org.dcm4chee.archive.store.remember.impl;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.verify.StoreVerifyService;
import org.dcm4chee.storage.ContainerEntry;
import org.dcm4chee.storage.archiver.service.ExternalDeviceArchiverContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
public class ExternalArchiverServiceImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalArchiverServiceImpl.class);
    
    @Inject
    private DicomConfiguration conf;
    
    @Inject
    private StoreVerifyService storeVerifyService;
    
    @Inject
    private RetrieveService retrieveService;
    
    @Inject
    private Device device;
    
    public void store(ExternalDeviceArchiverContext context, int retries) throws Exception {
        ApplicationEntity remoteAE = getRemoteAE(context);
        ApplicationEntity localAE = getLocalAE();
        if (localAE == null || remoteAE == null) {
            return;
        }

        String[] instanceUIDs = new String[context.getEntries().size()];
        int i = 0;
        for (ContainerEntry entry : context.getEntries()) {
            instanceUIDs[i++] = entry.getName();
        }

        List<ArchiveInstanceLocator> insts = locate(instanceUIDs);
        CStoreSCUContext cxt = new CStoreSCUContext(localAE, remoteAE, ServiceType.STOREREMEMBER);
        storeVerifyService.store(cxt, insts);
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
    
    private ApplicationEntity getRemoteAE(ExternalDeviceArchiverContext cxt) {
        try {
            Device extDeviceTarget = conf.findDevice(cxt.getExternalDeviceName());
            Collection<ApplicationEntity> aes = extDeviceTarget.getApplicationEntities();
            if(aes.isEmpty()) {
                LOG.warn("Did not find suitable AE for archiving on device {}", extDeviceTarget);
                return null;
            }
            
            return aes.iterator().next();
        } catch(Exception e) {
            return null;
        }
    }
    
    private ApplicationEntity getLocalAE() {
       //TODO: make used AE for store-verify configurable
       ApplicationEntity localAE = device.getApplicationEntity("DCM4CHEE");
       return localAE;
    }
  
}
