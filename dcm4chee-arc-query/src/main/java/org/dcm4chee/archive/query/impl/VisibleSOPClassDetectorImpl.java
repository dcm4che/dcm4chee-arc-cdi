package org.dcm4chee.archive.query.impl;

import java.util.Arrays;

import javax.inject.Inject;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.query.VisibleSOPClassDetector;

public class VisibleSOPClassDetectorImpl implements VisibleSOPClassDetector {

    @Inject
    Device device;

    @Override
    public boolean isVisibleSOPClass(String sopClassUID) {
        
        ArchiveDeviceExtension arcDevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        
        if (arcDevExt.getUseWhitelistOfVisibleImageSRClasses()) {
            return doesImageSRListContainSOPClass(sopClassUID, arcDevExt.getVisibleImageSRClasses());
        } else {
            return !doesImageSRListContainSOPClass(sopClassUID, arcDevExt.getNonVisibleImageSRClasses()); 
        }
    }

    private boolean doesImageSRListContainSOPClass(String sopClassUID, String[] arrayOfSOPClasses) {
        return arrayOfSOPClasses != null && arrayOfSOPClasses.length>0 &&
                Arrays.asList(arrayOfSOPClasses).contains(sopClassUID);
    }
}
