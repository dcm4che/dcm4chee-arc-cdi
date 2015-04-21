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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.IOCMConfig;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.iocm.client.ChangeRequesterService;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 */
@ApplicationScoped
public class ChangeRequesterServiceImpl implements ChangeRequesterService {

    private static final int DEFAULT_PRIORITY = 5;

    private static Logger LOG = LoggerFactory.getLogger(ChangeRequesterServiceImpl.class);

    @Inject
    private Device device;
    
    @Inject
    private CStoreSCUService storescuService;
    
    @Inject
    private RetrieveService retrieveService;

    private transient ArchiveDeviceExtension archDeviceExt;
    
    public void scheduleChangeRequest(Collection<String> updatedInstanceUIDs, Instance rejNote) {
        LOG.debug("ChangeRequestor: scheduleChangeRequest called! rejNote:{}\nupdated:{}", rejNote, updatedInstanceUIDs);
        IOCMConfig cfg = getIOCMConfig();
        if (cfg == null) {
            LOG.info("IOCMConfig not configured! Skipped!");
            return;
        }
        String[] targetAETs = cfg.getIocmDestinations();
        LOG.debug("targetAETs from IOCMConfig:{}", Arrays.toString(targetAETs));
        if (rejNote != null) {
            List<ArchiveInstanceLocator> locators = locate(rejNote.getSopInstanceUID());
            for (int i = 0 ; i < targetAETs.length ; i++) {
                storescuService.scheduleStoreSCU(
                        UUID.randomUUID().toString(),
                        new CStoreSCUContext(device.getApplicationEntity(cfg
                                .getCallingAET()), device
                                .getApplicationEntity(targetAETs[i]),
                                ServiceType.IOCMSERVICE), locators, cfg
                                .getIocmMaxRetries(), DEFAULT_PRIORITY, cfg
                                .getIocmRetryInterval());
            }
        }
        if (updatedInstanceUIDs != null && updatedInstanceUIDs.size() > 0) {
            List<ArchiveInstanceLocator> locators = locate(updatedInstanceUIDs.toArray(new String[updatedInstanceUIDs.size()]));
            for (int i = 0 ; i < targetAETs.length ; i++) {
                storescuService.scheduleStoreSCU(
                        UUID.randomUUID().toString(),
                        new CStoreSCUContext(device.getApplicationEntity(cfg
                                .getCallingAET()), device
                                .getApplicationEntity(targetAETs[i]),
                                ServiceType.IOCMSERVICE), locators, cfg
                                .getIocmMaxRetries(), DEFAULT_PRIORITY, cfg
                                .getIocmRetryInterval());
            }

            String[] noneIOCM = cfg.getNoneIocmDestinations();
            LOG.debug("NoneIocmDestinations from IOCMConfig:{}", Arrays.toString(noneIOCM));
            if (noneIOCM != null && noneIOCM.length > 0) {
                for (int i = 0 ; i < noneIOCM.length ; i++) {
                    storescuService.scheduleStoreSCU(
                            UUID.randomUUID().toString(),
                            new CStoreSCUContext(device.getApplicationEntity(cfg
                                    .getCallingAET()), device
                                    .getApplicationEntity(noneIOCM[i]),
                                    ServiceType.IOCMSERVICE), locators, cfg
                                    .getIocmMaxRetries(), DEFAULT_PRIORITY, cfg
                                    .getIocmRetryInterval());
                }
            }
        }

    }
    
    public void scheduleUpdateOnlyChangeRequest(Collection<String> updatedInstanceUIDs) {
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
        LOG.debug("NoneIocmDestinations from IOCMConfig:{}", Arrays.toString(noneIOCM));
        if (noneIOCM != null && noneIOCM.length > 0) {
            List<ArchiveInstanceLocator> locators = locate(updatedInstanceUIDs.toArray(new String[updatedInstanceUIDs.size()]));
            for (int i = 0 ; i < noneIOCM.length ; i++) {
                storescuService.scheduleStoreSCU(
                        UUID.randomUUID().toString(),
                        new CStoreSCUContext(device.getApplicationEntity(cfg
                                .getCallingAET()), device
                                .getApplicationEntity(noneIOCM[i]),
                                ServiceType.IOCMSERVICE), locators, cfg
                                .getIocmMaxRetries(), DEFAULT_PRIORITY, cfg
                                .getIocmRetryInterval());
            }
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
}
