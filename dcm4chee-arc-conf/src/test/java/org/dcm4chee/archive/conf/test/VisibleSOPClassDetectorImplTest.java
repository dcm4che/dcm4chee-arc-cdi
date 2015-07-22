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
package org.dcm4chee.archive.conf.test;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.VisibleSOPClassDetector;
import org.dcm4chee.archive.conf.VisibleSOPClassDetectorImpl;
import org.easymock.EasyMock;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class VisibleSOPClassDetectorImplTest extends EasyMockSupport {
    private final String[] VISIBLE_SOP_CLASS_UIDS = {"1.2.3", "4.5.6"};
    private final String[] NON_VISIBLE_SOP_CLASS_UIDS = {"7.8.9", "10.11.12"};
    
    @Rule
    public EasyMockRule mocks = new EasyMockRule(this);

    @TestSubject
    private VisibleSOPClassDetector api = new VisibleSOPClassDetectorImpl();
    
    @Mock(type = MockType.STRICT)
    private Device mockDevice;
    
    @Test
    public void isVisibleSOPClass_withWhitelistAndSopClassInVisibleList_returnsTrue() {
        ArchiveDeviceExtension devExt = new ArchiveDeviceExtension();
        devExt.setUseWhitelistOfVisibleImageSRClasses(true);
        devExt.setVisibleImageSRClasses(VISIBLE_SOP_CLASS_UIDS);
        
        EasyMock.expect(mockDevice.getDeviceExtension(ArchiveDeviceExtension.class)).andReturn(
                devExt);
        
        replayAll();
        
        Assert.assertTrue(api.isVisibleSOPClass("1.2.3"));
        
        verifyAll();
    }
    
    @Test
    public void isVisibleSOPClass_withWhitelistAndSopClassNotInVisibleList_returnsFalse() {
        ArchiveDeviceExtension devExt = new ArchiveDeviceExtension();
        devExt.setUseWhitelistOfVisibleImageSRClasses(true);
        devExt.setVisibleImageSRClasses(VISIBLE_SOP_CLASS_UIDS);
        
        EasyMock.expect(mockDevice.getDeviceExtension(ArchiveDeviceExtension.class)).andReturn(
                devExt);
        
        replayAll();
        
        Assert.assertFalse(api.isVisibleSOPClass("7.8.9"));
        
        verifyAll();
    }
    
    @Test
    public void isVisibleSOPClass_withBlacklistAndSopClassInNonVisibleList_returnsFalse() {
        ArchiveDeviceExtension devExt = new ArchiveDeviceExtension();
        devExt.setUseWhitelistOfVisibleImageSRClasses(false);
        devExt.setNonVisibleImageSRClasses(NON_VISIBLE_SOP_CLASS_UIDS);
        
        EasyMock.expect(mockDevice.getDeviceExtension(ArchiveDeviceExtension.class)).andReturn(
                devExt);
        
        replayAll();
        
        Assert.assertFalse(api.isVisibleSOPClass("7.8.9"));
        
        verifyAll();
    }
    
    @Test
    public void isVisibleSOPClass_withBlacklistAndSopClassNotInNonVisibleList_returnsTrue() {
        ArchiveDeviceExtension devExt = new ArchiveDeviceExtension();
        devExt.setUseWhitelistOfVisibleImageSRClasses(false);
        devExt.setVisibleImageSRClasses(NON_VISIBLE_SOP_CLASS_UIDS);
        
        EasyMock.expect(mockDevice.getDeviceExtension(ArchiveDeviceExtension.class)).andReturn(
                devExt);
        
        replayAll();
        
        Assert.assertTrue(api.isVisibleSOPClass("1.2.3"));
        
        verifyAll();
    }
}
