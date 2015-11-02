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

package org.dcm4chee.archive.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.dcm4chee.archive.code.CodeService;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.QueryRetrieveView;
import org.dcm4chee.archive.entity.Code;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
public class ArchiveDeviceProducer {

    private static final String DEVICE_NAME_PROPERTY =
            "org.dcm4chee.archive.deviceName";
    private static final String DEF_DEVICE_NAME =
            "dcm4chee-arc";

    @Inject
    private DicomConfigurationManager conf;

    @Inject
    private CodeService codeService;

    private Device device;

    @PostConstruct
    private void init() {
        try {
            device = findDevice();
            conf.preventDeviceModifications(device);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        findOrCreateRejectionCodes(device);
        initImageReaderFactory(device);
        initImageWriterFactory(device);
    }

    @Produces
    public Device getDevice() {
        return device;
    }

    // synchronized to prevent concurrent reconfigure
    public synchronized void reloadConfiguration() throws Exception {
        Device deviceLoaded = findDevice();
        findOrCreateRejectionCodes(deviceLoaded);
        initImageReaderFactory(deviceLoaded);
        initImageWriterFactory(deviceLoaded);
        this.device.reconfigure(deviceLoaded);
    }

    private Device findDevice() throws ConfigurationException {
        String name = System.getProperty(DEVICE_NAME_PROPERTY, DEF_DEVICE_NAME);
        Device arcDevice = conf.findDevice(name);
        if (arcDevice == null)
            throw new ConfigurationException("Archive device '" + name
                    + "' does not exist in the configuration");

        return arcDevice;
    }

    private void findOrCreateRejectionCodes(Device device) {
        Collection<Code> found = new ArrayList<Code>();
        ArchiveDeviceExtension arcDev =
                device.getDeviceExtensionNotNull(ArchiveDeviceExtension.class);
        arcDev.setIncorrectWorklistEntrySelectedCode(
                findOrCreate(arcDev.getIncorrectWorklistEntrySelectedCode(), found));
        for (QueryRetrieveView view : arcDev.getQueryRetrieveViews()) {
            findOrCreate(view.getShowInstancesRejectedByCodes(), found);
            findOrCreate(view.getHideRejectionNotesWithCodes(), found);
        }
    }

    private void findOrCreate(org.dcm4che3.data.Code[] codes,
            Collection<Code> found) {
        for (int i = 0; i < codes.length; i++) {
            codes[i] = findOrCreate(codes[i], found);
        }
    }

    private Code findOrCreate(org.dcm4che3.data.Code code,
            Collection<Code> found) {
        try {
            return (Code) code;
        } catch (ClassCastException e) {
            for (Code code2 : found) {
                if (code2.equalsIgnoreMeaning(code))
                    return code2;
            }
            Code code2 = codeService.findOrCreate(new Code(code));
            found.add(code2);
            return code2;
        }
    }

    private void initImageReaderFactory(Device device) {
        ImageReaderExtension ext = device.getDeviceExtension(ImageReaderExtension.class);
        if (ext != null) {
            ImageReaderFactory imageReaderFactory = ext.getImageReaderFactory();
            ImageReaderFactory.setDefault(imageReaderFactory);
            imageReaderFactory.init();
        } else {
            ImageReaderFactory.resetDefault();
            ImageReaderFactory.getDefault(); // init now
        }
    }

    private void initImageWriterFactory(Device device) {
        ImageWriterExtension ext = device.getDeviceExtension(ImageWriterExtension.class);
        if (ext != null) {
            ImageWriterFactory imageWriterFactory = ext.getImageWriterFactory();
            ImageWriterFactory.setDefault(imageWriterFactory);
            imageWriterFactory.init();
        } else {
            ImageWriterFactory.resetDefault();
            ImageWriterFactory.getDefault(); // init now
        }
    }

}
