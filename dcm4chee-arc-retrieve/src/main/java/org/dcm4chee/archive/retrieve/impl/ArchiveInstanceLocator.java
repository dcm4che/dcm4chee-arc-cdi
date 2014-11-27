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
package org.dcm4chee.archive.retrieve.impl;

import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4chee.storage.conf.StorageSystem;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class ArchiveInstanceLocator extends InstanceLocator {

    private static final long serialVersionUID = 7208477744305290578L;

    private final String fileTimeZoneID;
    private final StorageSystem storageSystem;
    private final String filePath;
    private final String entryName;
    private final String retrieveAETs;
    private final String externalRetrieveAET;

    public static final class Builder {
        private final String cuid;
        private final String iuid;
        private final String tsuid;
        private StorageSystem storageSystem;
        private String filePath;
        private String entryName;
        private String retrieveAETs;
        private String externalRetrieveAET;
        private String fileTimeZoneID;

        public Builder(String cuid, String iuid, String tsuid) {
            this.cuid = cuid;
            this.iuid = iuid;
            this.tsuid = tsuid;
        }

        public Builder storageSystem(StorageSystem storageSystem) {
            this.storageSystem = storageSystem;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder entryName(String entryName) {
            this.entryName = entryName;
            return this;
        }

        public Builder retrieveAETs(String retrieveAETs) {
            this.retrieveAETs = retrieveAETs;
            return this;
        }

        public Builder externalRetrieveAET(String externalRetrieveAET) {
            this.externalRetrieveAET = externalRetrieveAET;
            return this;
        }

        public Builder fileTimeZoneID(String fileTimeZoneID) {
            this.fileTimeZoneID = fileTimeZoneID;
            return this;
        }

        public ArchiveInstanceLocator build() {
            return new ArchiveInstanceLocator(this);
        }
    }

    private ArchiveInstanceLocator(Builder builder) {
        super(builder.cuid, builder.iuid, builder.tsuid, null);
        this.fileTimeZoneID = builder.fileTimeZoneID;
        this.storageSystem = builder.storageSystem;
        this.filePath = builder.filePath;
        this.entryName = builder.entryName;
        this.retrieveAETs = builder.retrieveAETs;
        this.externalRetrieveAET = builder.externalRetrieveAET;
   }

    public String getFileTimeZoneID() {
        return fileTimeZoneID;
    }

    public StorageSystem getStorageSystem() {
        return storageSystem;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getEntryName() {
        return entryName;
    }

    public String getRetrieveAETs() {
        return retrieveAETs;
    }

    public String getExternalRetrieveAET() {
        return externalRetrieveAET;
    }

}
