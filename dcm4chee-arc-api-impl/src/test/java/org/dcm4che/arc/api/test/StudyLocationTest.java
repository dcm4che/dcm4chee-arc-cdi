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
package org.dcm4che.arc.api.test;

import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.api.StudyLocation;
import org.dcm4chee.archive.api.impl.StudyLocationImpl;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.ExternalLocationTuple;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Steve Kroetsch <stevekroetsch@hotmail.com>
 *
 */
public class StudyLocationTest extends EasyMockSupport {

    @Rule
    public EasyMockRule mocks = new EasyMockRule(this);

    @TestSubject
    private StudyLocation api = new StudyLocationImpl();

    @Mock(type = MockType.STRICT)
    private Device mockDevice;

    @Mock(type = MockType.STRICT)
    private DicomConfiguration mockDicomConfiguration;

    @Test
    public void testGetStorageSystems() {
        List<ArchiveInstanceLocator> matches = new ArrayList<ArchiveInstanceLocator>();

        StorageSystemGroup grp1 = new StorageSystemGroup();
        grp1.setGroupID("G1");
        grp1.setBaseStorageAccessTime(1000);
        StorageSystem sys1 = new StorageSystem();
        sys1.setStorageSystemID("S1");
        sys1.setStorageSystemPath("target");
        sys1.setStorageSystemGroup(grp1);

        StorageSystemGroup grp2 = new StorageSystemGroup();
        grp2.setGroupID("G2");
        grp2.setBaseStorageAccessTime(2000);
        StorageSystem sys2 = new StorageSystem();
        sys2.setStorageSystemID("S2");
        sys2.setStorageSystemPath("target");
        sys2.setStorageSystemGroup(grp2);

        ArchiveInstanceLocator loc1 = new ArchiveInstanceLocator.Builder(
                UID.CTImageStorage, "1.1", UID.JPEGLSLossless).storageSystem(sys1)
                .storagePath("test").build();
        ArchiveInstanceLocator loc2 = new ArchiveInstanceLocator.Builder(
                UID.CTImageStorage, "1.1", UID.JPEGLSLossless).storageSystem(sys2)
                .storagePath("test").build();
        loc1.setFallbackLocator(loc2);
        matches.add(loc1);

        ArchiveInstanceLocator loc3 = new ArchiveInstanceLocator.Builder(
                UID.CTImageStorage, "1.2", UID.JPEGLSLossless).storageSystem(sys1)
                .storagePath("test").build();
        ArchiveInstanceLocator loc4 = new ArchiveInstanceLocator.Builder(
                UID.CTImageStorage, "1.2", UID.JPEGLSLossless).storageSystem(sys2)
                .storagePath("test").build();
        loc3.setFallbackLocator(loc4);
        matches.add(loc1);

        ArchiveInstanceLocator loc5 = new ArchiveInstanceLocator.Builder(
                UID.CTImageStorage, "1.3", UID.JPEGLSLossless).storageSystem(sys1)
                .storagePath("test").build();
        ArchiveInstanceLocator loc6 = new ArchiveInstanceLocator.Builder(
                UID.CTImageStorage, "1.3", UID.JPEGLSLossless).storageSystem(sys2)
                .storagePath("test").build();
        loc5.setFallbackLocator(loc6);
        matches.add(loc1);

        List<StorageSystem> lst = api.getStorageSystems(matches);

        Assert.assertEquals(lst.size(), 2);
        Assert.assertEquals("S1", lst.get(0).getStorageSystemID());
        Assert.assertEquals("S2", lst.get(1).getStorageSystemID());
    }

    @Test
    public void testGetExternalRetrieveDevices() throws ConfigurationException {
        Device dev1 = new Device("D1");
        Device dev2 = new Device("D2");

        List<ExternalLocationTuple> externalLocators = new ArrayList<ExternalLocationTuple>(
                2);
        externalLocators.add(new ExternalLocationTuple(dev1.getDeviceName(),
                Availability.ONLINE));
        externalLocators.add(new ExternalLocationTuple(dev2.getDeviceName(),
                Availability.NEARLINE));

        List<ArchiveInstanceLocator> matches = new ArrayList<ArchiveInstanceLocator>();

        ArchiveInstanceLocator loc1 = new ArchiveInstanceLocator.Builder(
                UID.CTImageStorage, "1.1", UID.JPEGLSLossless).externalLocators(
                externalLocators).build();
        matches.add(loc1);
        ArchiveInstanceLocator loc2 = new ArchiveInstanceLocator.Builder(
                UID.CTImageStorage, "1.2", UID.JPEGLSLossless).externalLocators(
                externalLocators).build();
        matches.add(loc2);
        ArchiveInstanceLocator loc3 = new ArchiveInstanceLocator.Builder(
                UID.CTImageStorage, "1.3", UID.JPEGLSLossless).externalLocators(
                externalLocators).build();
        matches.add(loc3);

        expect(mockDicomConfiguration.findDevice(isA(String.class))).andReturn(dev1);
        expect(mockDicomConfiguration.findDevice(isA(String.class))).andReturn(dev2);

        Map<String, String> externalArchivesMap = new HashMap<String, String>();
        externalArchivesMap.put(dev1.getDeviceName(), "1");
        externalArchivesMap.put(dev2.getDeviceName(), "2");

        ArchiveDeviceExtension devExt = new ArchiveDeviceExtension();
        devExt.setExternalArchivesMap(externalArchivesMap);
        expect(mockDevice.getDeviceExtension(ArchiveDeviceExtension.class)).andReturn(
                devExt);

        replayAll();

        List<Device> lst = api.getExternalRetrieveDevices(matches);
        Assert.assertEquals(lst.size(), 2);
        Assert.assertSame(dev1, lst.get(0));
        Assert.assertSame(dev2, lst.get(1));

        verifyAll();
    }
}
