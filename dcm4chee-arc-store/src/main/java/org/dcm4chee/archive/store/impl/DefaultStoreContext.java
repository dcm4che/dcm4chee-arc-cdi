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

import java.io.File;
import java.nio.file.Path;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.util.AttributesFormat;
import org.dcm4che.util.TagUtils;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.Availability;
import org.dcm4chee.archive.entity.FileRef;
import org.dcm4chee.archive.entity.FileSystem;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSource;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DefaultStoreContext implements StoreContext {

    private StoreSource source;
    private StoreService service;
    private ArchiveAEExtension arcAE;
    private FileSystem fs;
    private Path file;
    private byte[] digest;
    private StoreParam storeParam;
    private String transferSyntax;
    private Attributes attributes;
    private Attributes coercedAttributes = new Attributes();
    private String sopCUID;
    private String sopIUID;
    private String seriesIUID;
    private String studyIUID;

    public DefaultStoreContext(StoreSource source, ArchiveAEExtension arcAE,
            FileSystem fs, Path file, byte[] digest) {
        this.source = source;
        this.arcAE = arcAE;
        this.fs = fs;
        this.file = file;
        this.digest = digest;
        this.storeParam = arcAE.getStoreParam();
    }


    @Override
    public StoreSource getStoreSource() {
        return source;
    }

    @Override
    public FileSystem getFileSystem() {
        return fs;
    }

    @Override
    public ArchiveAEExtension getArchiveAEExtension() {
        return arcAE;
    }

    @Override
    public Attributes getCoercedAttributes() {
        return coercedAttributes;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Attributes attrs) {
        this.attributes = attrs;
        this.sopCUID = attrs.getString(Tag.SOPClassUID);
        this.sopIUID = attrs.getString(Tag.SOPInstanceUID);
        this.seriesIUID = attrs.getString(Tag.SeriesInstanceUID);
        this.studyIUID = attrs.getString(Tag.StudyInstanceUID);
    }

    @Override
    public String getSOPClassUID() {
        return sopCUID;
    }

    @Override
    public String getSOPInstanceUID() {
        return sopIUID;
    }

    @Override
    public String getSeriesInstanceUID() {
        return seriesIUID;
    }

    @Override
    public String getStudyInstanceUID() {
        return studyIUID;
    }

    @Override
    public String getTransferSyntax() {
        return transferSyntax;
    }

    @Override
    public void setTransferSyntax(String tsuid) {
        this.transferSyntax = tsuid;
    }

    @Override
    public Path getFile() {
        return file;
    }

    @Override
    public void setFile(Path file) {
        this.file = file;
    }
    
    @Override
    public void setDigest(byte[] digest) {
        this.digest = digest;
    }

    @Override
    public Path getStorePath() {
        AttributesFormat format = arcAE.getStorageFilePathFormat();
        if (format == null)
            throw new IllegalStateException(
                    "No StorageFilePathFormat configured for "
                            + getReceivingAETitle());
        String path;
        synchronized (format) {
            path = format.format(attributes);
        }
        return fs.getPath().resolve(
                path.replace('/', File.separatorChar));
    }

    @Override
    public String getReceivingAETitle() {
        return arcAE.getApplicationEntity().getAETitle();
    }

    @Override
    public StoreParam getStoreParam() {
        return storeParam;
    }

    @Override
    public String getSendingAETitle() {
        return source.getSendingAETitle(arcAE);
    }

    @Override
    public Availability getAvailability() {
        return fs.getAvailability();
    }


    @Override
    public FileRef getFileRef() {
        return new FileRef(fs, unixFilePath(), 
                transferSyntax, file.toFile().length(),
                TagUtils.toHexString(digest));
    }

    private String unixFilePath() {
        return fs.getPath().relativize(file).toString()
            .replace(File.separatorChar, '/');
    }

    @Override
    public StoreService getService() {
        return service;
    }

    @Override
    public void setService(StoreService service) {
        this.service = service;
    }
    
}
