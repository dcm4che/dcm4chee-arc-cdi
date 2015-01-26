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

package org.dcm4chee.archive.store.impl;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.TimeZone;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.dto.Participant;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.conf.StorageSystem;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class StoreSessionImpl implements StoreSession {

    private final StoreService storeService;
    private Participant source;
    private String remoteAET;
    private ArchiveAEExtension arcAE;
    private StoreParam storeParam;
    private MessageDigest messageDigest;
    private StorageSystem storageSystem;
    private Path spoolDirectory;
    private HashMap<String,Object> properties = new HashMap<String,Object>();
    private Device sourceDevice;
    private TimeZone sourceTimeZone;

    public StoreSessionImpl(StoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public Device getDevice() {
        return arcAE.getApplicationEntity().getDevice();
    }

    @Override
    public Participant getSource() {
        return source;
    }

    @Override
    public void setSource(Participant source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return source.toString();
    }

    @Override
    public String getLocalAET() {
        return arcAE.getApplicationEntity().getAETitle();
    }

    @Override
    public StoreService getStoreService() {
        return storeService;
    }

    @Override
    public String getRemoteAET() {
        return remoteAET;
    }

    @Override
    public void setRemoteAET(String remoteAET) {
        this.remoteAET = remoteAET;
    }

    @Override
    public ArchiveAEExtension getArchiveAEExtension() {
        return arcAE;
    }

    @Override
    public void setArchiveAEExtension(ArchiveAEExtension arcAE) {
        this.arcAE = arcAE;
        this.arcAE = arcAE;
        this.storeParam = arcAE.getStoreParam();
    }

    @Override
    public StoreParam getStoreParam() {
        return storeParam;
    }

    @Override
    public void setStoreParam(StoreParam storeParam) {
        this.storeParam = storeParam;
    }

    @Override
    public StorageSystem getStorageSystem() {
        return storageSystem;
    }

    @Override
    public void setStorageSystem(StorageSystem storageSystem) {
        this.storageSystem = storageSystem;
        try {
            String algorithm = storageSystem.getDigestAlgorithm();
            this.messageDigest = algorithm != null
                    ? MessageDigest.getInstance(algorithm)
                    : null;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path getSpoolDirectory() {
        return spoolDirectory;
    }

    @Override
    public void setSpoolDirectory(Path spoolDirectory) {
        this.spoolDirectory = spoolDirectory;
    }

    @Override
    public MessageDigest getMessageDigest() {
        return messageDigest;
    }

    @Override
    public void setMessageDigest(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Object removeProperty(String key) {
        return properties .remove(key);
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Device getSourceDevice() {
        return sourceDevice;
    }

    @Override
    public void setSourceDevice(Device source) {
        this.sourceDevice=source;
    }
    @Override
    public TimeZone getSourceTimeZone() {
        return sourceTimeZone;
    }
    @Override
    public void setSourceTimeZone(TimeZone sourceTimeZone) {
        this.sourceTimeZone = sourceTimeZone;
    }

    @Override
    public String getSourceTimeZoneID() {
        return sourceTimeZone != null ? sourceTimeZone.getID() : null;
    }
}

