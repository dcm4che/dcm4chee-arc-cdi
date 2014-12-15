package org.dcm4chee.archive.retrieve.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.entity.QLocation;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.dcm4chee.storage.conf.StorageDeviceExtension;
import org.dcm4chee.storage.conf.StorageSystem;

import com.mysema.query.Tuple;

class ArchiveInstanceLocatorBuilder {

    private final StorageDeviceExtension storageConf;
    private ArchiveInstanceLocator locator;
    private String cuid;
    private String iuid;
    private String retrieveAETs;
    private String externalRetrieveAET;
    private Attributes attrs;

    ArchiveInstanceLocatorBuilder(StorageDeviceExtension storageConf,
            Tuple tuple, Attributes seriesAttrs) {
        this.storageConf = storageConf;
        this.cuid= tuple.get(QInstance.instance.sopClassUID);
        this.iuid = tuple.get(QInstance.instance.sopInstanceUID);
        this.retrieveAETs = tuple.get(QInstance.instance.retrieveAETs);
        this.externalRetrieveAET =
                tuple.get(QInstance.instance.externalRetrieveAET);
        byte[] encodedInstanceAttrs = 
                tuple.get(QueryBuilder.instanceAttributesBlob.encodedAttributes);
        Attributes instanceAttrs = Utils.decodeAttributes(encodedInstanceAttrs);
        this.attrs = Utils.mergeAndNormalize(seriesAttrs, instanceAttrs );
    }

    void addFileRefs(Tuple tuple) {
        addFileRef(
                tuple.get(QLocation.location.storageSystemGroupID),
                tuple.get(QLocation.location.storageSystemID),
                tuple.get(QLocation.location.storagePath),
                tuple.get(QLocation.location.entryName),
                tuple.get(QLocation.location.transferSyntaxUID),
                tuple.get(QLocation.location.timeZone));
    }
 
    private void addFileRef(String groupID, String systemID,
            String filePath, String entryName, String tsuid,
            String fileTimeZone) {
        if (groupID == null)
            return;

        StorageSystem storageSystem = storageConf.getStorageSystem(groupID, systemID);
        if (storageSystem == null)
            return;

        if (locator == null
                || locator.getStorageSystem().getStorageAccessTime()
                    > storageSystem.getStorageAccessTime())
            locator = new ArchiveInstanceLocator.Builder(cuid, iuid, tsuid)
                    .storageSystem(storageSystem)
                    .filePath(filePath)
                    .entryName(entryName)
                    .retrieveAETs(retrieveAETs)
                    .externalRetrieveAET(externalRetrieveAET)
                    .build();
    }

    ArchiveInstanceLocator build() {
        if (locator == null) {
            locator = new ArchiveInstanceLocator.Builder(cuid, iuid, null)
                .retrieveAETs(retrieveAETs)
                .externalRetrieveAET(externalRetrieveAET)
                .build();
        }
        locator.setObject(attrs);
        return locator;
    }
}