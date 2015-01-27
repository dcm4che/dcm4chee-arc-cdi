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
import java.util.HashMap;
import java.util.TimeZone;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4chee.archive.conf.StoreAction;
import org.dcm4chee.archive.entity.Location;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.storage.StorageContext;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class StoreContextImpl implements StoreContext {

    private final StoreSession session;
    private Path spoolFile;
    private String spoolFileDigest;
    private String finalFileDigest;
    private String noDBAttsDigest;    
    private long finalFileSize;
    private String storagePath;
    private String transferSyntax;
    private Attributes attributes;
    private Attributes coercedAttributes = new Attributes();
    private StoreAction storeAction;
    private StorageContext storageContext;
    private Instance instance;
    private Location fileRef;
    private Throwable throwable;
    private HashMap<String,Object> properties = new HashMap<String,Object>();
    private TimeZone sourceTimeZone;
    
    public StoreContextImpl(StoreSession session) {
        this.session = session;
    }

    @Override
    public StoreSession getStoreSession() {
        return session;
    }

    @Override
    public Path getSpoolFile() {
        return spoolFile;
    }

    @Override
    public void setSpoolFile(Path spoolFile) {
        this.spoolFile = spoolFile;
    }

    @Override
    public String getSpoolFileDigest() {
        return spoolFileDigest;
    }

    @Override
    public void setSpoolFileDigest(String spoolFileDigest) {
        this.spoolFileDigest = spoolFileDigest;
    }

    @Override
    public long getFinalFileSize() {
        return finalFileSize;
    }

    @Override
    public void setFinalFileSize(long finalFileSize) {
        this.finalFileSize = finalFileSize;
    }

    @Override
    public String getTransferSyntax() {
        return transferSyntax;
    }

    @Override
    public String getFinalFileDigest() {
        return finalFileDigest;
    }

    @Override
    public void setFinalFileDigest(String finalFileDigest) {
        this.finalFileDigest = finalFileDigest;
    }

    @Override    
    public String getNoDBAttsDigest() {
        return noDBAttsDigest;
    }

    @Override
    public void setNoDBAttsDigest(String noDBAttsDigest) {
        this.noDBAttsDigest = noDBAttsDigest;
    }

    @Override
    public void setTransferSyntax(String transferSyntax) {
        this.transferSyntax = transferSyntax;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public Attributes getCoercedOriginalAttributes() {
        return coercedAttributes;
    }

    @Override
    public void setCoercedOrginalAttributes(Attributes attrs) {
        this.coercedAttributes = attrs;
    }

    @Override
    public StoreAction getStoreAction() {
        return storeAction;
    }

    @Override
    public void setStoreAction(StoreAction storeAction) {
        this.storeAction = storeAction;
    }

    @Override
    public Location getFileRef() {
        return fileRef;
    }

    @Override
    public void setFileRef(Location fileRef) {
        this.fileRef = fileRef;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    @Override
    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    @Override
    public Object getProperty(String key) {
        return properties .get(key);
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
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
    
    @Override
    public boolean isFail() {
        return getStoreAction()!=null && getStoreAction().equals(StoreAction.FAIL);
    }

    @Override
    public String getStoragePath() {
        return storagePath;
    }

    @Override
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public String calcStoragePath() {
        AttributesFormat format = session.getArchiveAEExtension()
                .getStorageFilePathFormat();
        synchronized (format) {
            return format.format(attributes);
        }
    }

    @Override
    public StorageContext getStorageContext() {
        return storageContext;
    }

    @Override
    public void setStorageContext(StorageContext storageContext) {
        this.storageContext = storageContext;
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
