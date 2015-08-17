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

package org.dcm4chee.archive.hsm.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchivingQueueMapping;
import org.dcm4chee.archive.conf.ArchivingQueueMappings;
import org.dcm4chee.archive.conf.HsmServiceDeviceExtension;
import org.dcm4chee.storage.archiver.service.ArchiverContext;
import org.dcm4chee.storage.archiver.service.ArchivingQueueProvider;
import org.dcm4chee.storage.archiver.service.ExternalDeviceArchiverContext;
import org.dcm4chee.storage.archiver.service.StorageSystemArchiverContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
@ApplicationScoped
public class ArchivingQueueProviderImpl implements ArchivingQueueProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ArchivingQueueProviderImpl.class);
    
    @Inject
    private Device device;
    
    public Queue getQueue(ArchiverContext cxt) {
        ArchivingQueueMappings queueMappings = getQueueMappings();
        try {
            String queueName;
            if (cxt instanceof StorageSystemArchiverContext) {
                String storageGroupID = ((StorageSystemArchiverContext)cxt).getStorageSystemGroupID();
                ArchivingQueueMapping queueMapping = queueMappings.findByStorageSystemGroupID(storageGroupID);
                if(queueMapping == null) {
                    LOG.info("No archiving queue mapping found for storage group ID '{}'", storageGroupID);
                }
                queueName = (queueMapping != null) ? queueMapping.getArchivingQueueName() : getDefaultStorageSystemArchingQueueName();
                LOG.info("Selected archiving queue '{}' for target storage group '{}'", queueName, storageGroupID);
            } else if (cxt instanceof ExternalDeviceArchiverContext) {
                String extDeviceName = ((ExternalDeviceArchiverContext)cxt).getExternalDeviceName();
                ArchivingQueueMapping queueMapping = queueMappings.findByExternalDeviceName(extDeviceName);
                if(queueMapping == null) {
                    LOG.info("No archiving queue mapping found for external device name '{}'", extDeviceName);
                }
                queueName = (queueMapping != null) ? queueMapping.getArchivingQueueName() : getDefaultExternalDeviceArchingQueueName();
                LOG.info("Selected archiving queue '{}' for target external device '{}'", queueName, extDeviceName);
            } else {
                throw new RuntimeException("Unknown archiving context type " + cxt.getClass().getName());
            }
            
            if(queueName != null) {
                return lookupQueue(queueName);
            }
        } catch (Exception e) {
            LOG.error("Error while trying to lookup JMS archiving queue", e);
        }
        
        return null;
    }

    private Queue lookupQueue(String queueName) throws NamingException {
        InitialContext jndiCtx = new InitialContext();
        return (Queue) jndiCtx.lookup(queueName);
    }
    
    private ArchivingQueueMappings getQueueMappings() {
        HsmServiceDeviceExtension hsmServiceExtension = device.getDeviceExtension(HsmServiceDeviceExtension.class);
        return hsmServiceExtension != null ? hsmServiceExtension.getArchivingQueueMappings() : new ArchivingQueueMappings();
    }
    
    private String getDefaultStorageSystemArchingQueueName() {
        HsmServiceDeviceExtension hsmServiceExtension = device.getDeviceExtension(HsmServiceDeviceExtension.class);
        return hsmServiceExtension != null ? hsmServiceExtension.getArchivingQueueMappings().getDefaultStorageSystemArchivingQueueName() : null;
    }
    
    private String getDefaultExternalDeviceArchingQueueName() {
        HsmServiceDeviceExtension hsmServiceExtension = device.getDeviceExtension(HsmServiceDeviceExtension.class);
        return hsmServiceExtension != null ? hsmServiceExtension.getArchivingQueueMappings().getDefaultExternalDeviceArchivingQueueName() : null;
    }
    
}
