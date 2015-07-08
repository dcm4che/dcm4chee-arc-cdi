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
 * Portions created by the Initial Developer are Copyright (C) 2015
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

package org.dcm4chee.archive.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.Device;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.service.impl.StorageServiceImpl;
import org.dcm4chee.archive.api.StudyLocation;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.QueryParam;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ExternalLocationTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link StudyLocation}.
 * 
 * @author Steve Kroetsch <stevekroetsch@hotmail.com>
 */
@EJB(name = StudyLocation.JNDI_NAME, beanInterface = StudyLocation.class)
@Stateless
@Local(StudyLocation.class)
public class StudyLocationImpl implements StudyLocation {

    private static final Logger LOG = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Inject
    private DicomConfiguration dicomConfiguration;

    @Inject
    private org.dcm4chee.archive.retrieve.RetrieveService retrieveService;

    @Inject
    private Device device;

    @Override
    public List<ArchiveInstanceLocator> calculateMatches(String uid) {
        return retrieveService.calculateMatches(uid, null, null, queryParam(), false);
    }

    private QueryParam queryParam() {
        QueryParam param = new QueryParam();
        param.setMatchLinkedPatientIDs(false);
        param.setQueryRetrieveView(new QueryRetrieveView());
        return param;
    }

    @Override
    public List<StorageSystem> getStorageSystems(List<ArchiveInstanceLocator> matches) {
        List<StorageSystem> lst = new ArrayList<StorageSystem>();
        for (ArchiveInstanceLocator match : matches) {
            while (match != null) {
                StorageSystem system = match.getStorageSystem();
                if (system != null && !lst.contains(system))
                    lst.add(system);
                match = match.getFallbackLocator();
            }
        }

        Collections.sort(lst, new Comparator<StorageSystem>() {
            @Override
            public int compare(StorageSystem ss1, StorageSystem ss2) {
                return ss1.getStorageAccessTime() - ss2.getStorageAccessTime();
            }
        });

        return lst;
    }

    @Override
    public List<Device> getExternalRetrieveDevices(List<ArchiveInstanceLocator> matches) {
        Set<String> deviceNames = new LinkedHashSet<String>();
        for (ArchiveInstanceLocator match : matches) {
            while (match != null) {
                List<ExternalLocationTuple> tuples = match.getExternalLocators();
                if (tuples != null) {
                    for (ExternalLocationTuple tuple : tuples)
                        deviceNames.add(tuple.getRetrieveDeviceName());
                }
                match = match.getFallbackLocator();
            }
        }

        ArrayList<Device> lst = new ArrayList<Device>();
        for (String deviceName : deviceNames) {
            try {
                lst.add(dicomConfiguration.findDevice(deviceName));
            } catch (ConfigurationException e) {
                LOG.error("Unable to find external archive {} in configuration",
                        deviceName);
            }
        }

        ArchiveDeviceExtension devExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        final Map<String, String> externalArchivesMap = devExt.getExternalArchivesMap();
        Collections.sort(lst, new Comparator<Device>() {
            @Override
            public int compare(Device dev1, Device dev2) {
                int p1 = Integer.parseInt(externalArchivesMap.get(dev1.getDeviceName()) );
                int p2 = Integer.parseInt(externalArchivesMap.get(dev2.getDeviceName()));
                return p1 < p2 ? -1 : p1 == p2 ? 0 : 1;
            }
        });

        return lst;
    }

    @Override
    public List<StorageSystem> getStorageSystems(String uid) {
        return getStorageSystems(calculateMatches(uid));
    }

    @Override
    public List<Device> getExternalRetrieveDevices(String uid) {
        return getExternalRetrieveDevices(calculateMatches(uid));
    }
}
