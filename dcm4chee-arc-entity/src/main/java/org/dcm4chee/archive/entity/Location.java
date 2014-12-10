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

package org.dcm4chee.archive.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@Entity
@Table(name = "location")
public class Location implements Serializable {

    private static final long serialVersionUID = -3832203362617593125L;

    public enum Status { OK, DELETE_FAILED };

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private Date createdTime;

    @Basic(optional = false)
    @Column(name = "storage_group_id", updatable = false)
    private String storageSystemGroupID;

    @Basic(optional = false)
    @Column(name = "storage_id", updatable = false)
    private String storageSystemID;

    @Basic(optional = false)
    @Column(name = "storage_path", updatable = false)
    private String storagePath;

    @Basic(optional = true)
    @Column(name = "entry_name", updatable = false)
    private String entryName;

    @Basic(optional = false)
    @Column(name = "tsuid", updatable = false)
    private String transferSyntaxUID;

    @Basic(optional = true)
    @Column(name = "time_zone", updatable = false)
    private String timeZone;
    
    @Basic(optional = false)
    @Column(name = "object_size", updatable = false)
    private long size;

    @Basic(optional = true)
    @Column(name = "digest", updatable = false)
    private String digest;

    @Basic(optional = false)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", updatable = true)
    private Status status;

    @ManyToMany(mappedBy="locations")
    private Collection<Instance> instances;

    public static final class Builder {
        private String storageSystemGroupID;
        private String storageSystemID;
        private String storagePath;
        private String entryName;
        private String transferSyntaxUID;
        private String timeZone;
        private long size;
        private String digest;
        private Status status = Status.OK;

        public Builder storageSystemGroupID(String storageSystemGroupID) {
            this.storageSystemGroupID = storageSystemGroupID;
            return this;
        }

        public Builder storageSystemID(String storageSystemID) {
            this.storageSystemID = storageSystemID;
            return this;
        }

        public Builder storagePath(String storagePath) {
            this.storagePath = storagePath;
            return this;
        }

        public Builder entryName(String entryName) {
            this.entryName = entryName;
            return this;
        }

        public Builder transferSyntaxUID(String transferSyntaxUID) {
            this.transferSyntaxUID = transferSyntaxUID;
            return this;
        }

        public Builder timeZone(String timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public Builder digest(String digest) {
            this.digest = digest;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Location build() {
            return new Location(this);
        }
    }

    public Location() {}

    private Location(Builder builder) {
        storageSystemGroupID = builder.storageSystemGroupID;
        storageSystemID = builder.storageSystemID;
        storagePath = builder.storagePath;
        entryName = builder.entryName;
        transferSyntaxUID = builder.transferSyntaxUID;
        timeZone = builder.timeZone;
        size = builder.size;
        digest = builder.digest;
        status = builder.status;
    }

    @PrePersist
    public void onPrePersist() {
        Date now = new Date();
        createdTime = now;
    }

    public long getPk() {
        return pk;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public String getStorageSystemGroupID() {
        return storageSystemGroupID;
    }

    public String getStorageSystemID() {
        return storageSystemID;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getEntryName() {
        return entryName;
    }

    public String getTransferSyntaxUID() {
        return transferSyntaxUID;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public long getSize() {
        return size;
    }

    public String getDigest() {
        return digest;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Collection<Instance> getInstances() {
        return instances;
    }

    public void setInstances(Collection<Instance> instances) {
        this.instances = instances;
    }

    @Override
    public String toString() {
        return "Storage[pk=" + pk
                + ", groupID=" + storageSystemGroupID
                + ", systemID=" + storageSystemID
                + ", path=" + storagePath
                + ", entry=" + entryName
                + ", tsuid=" + transferSyntaxUID
                + ", size=" + size
                + ", status=" + status
                + "]";
    }

}
