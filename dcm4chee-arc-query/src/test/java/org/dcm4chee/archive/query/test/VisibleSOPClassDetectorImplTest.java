package org.dcm4chee.archive.query.test;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.query.VisibleSOPClassDetector;
import org.dcm4chee.archive.query.impl.VisibleSOPClassDetectorImpl;
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
