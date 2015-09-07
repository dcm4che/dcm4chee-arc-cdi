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

package org.dcm4chee.archive.rs;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link HostAECache}.
 * 
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@ApplicationScoped
public class HostAECacheImpl implements HostAECache {

    private static final Logger log = LoggerFactory.getLogger(HostAECacheImpl.class);

    private static final String DEFAULT_MAPPING = "*";
    
    @Inject
    private IApplicationEntityCache aeCache;

    @Inject
    Device device;

    @Override
    public ApplicationEntity findAE(HttpSource source) throws ConfigurationException {
        ArchiveDeviceExtension arcDevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);

        String hostNameOrIP = arcDevExt.isHostnameAEResolution() ? source.getHost() : source.getIP();

        String aeTitle = arcDevExt.getHostNameToAETitleMap().get(hostNameOrIP);

        if (aeTitle != null) {
            try {
                return aeCache.findApplicationEntity(aeTitle);
            } catch (ConfigurationException e) {
                log.warn("AET {} (hostname/ip: {}) is not correctly configured. Will use default fallback AET instead.", aeTitle, hostNameOrIP);
            }
        }

        // fallback to default mapping
        String fallbackAETitle = arcDevExt.getHostNameToAETitleMap().get(DEFAULT_MAPPING);
        if (fallbackAETitle == null)
            throw new ConfigurationNotFoundException("AE for hostname/ip " + hostNameOrIP + " not found. No default/fallback configured.");
        return aeCache.findApplicationEntity(fallbackAETitle);
    }

}
