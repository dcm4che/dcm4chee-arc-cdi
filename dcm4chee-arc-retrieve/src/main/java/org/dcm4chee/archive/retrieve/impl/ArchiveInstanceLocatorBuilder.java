package org.dcm4chee.archive.retrieve.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.entity.Availability;
import org.dcm4chee.archive.entity.QFileRef;
import org.dcm4chee.archive.entity.QFileSystem;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.util.QueryBuilder;

import com.mysema.query.Tuple;

class ArchiveInstanceLocatorBuilder {
    private ArchiveInstanceLocator locator;
    private Availability availability = Availability.UNAVAILABLE;
    private String cuid;
    private String iuid;
    private String retrieveAETs;
    private String externalRetrieveAET;
    private Attributes attrs;

    ArchiveInstanceLocatorBuilder(Tuple tuple, Attributes seriesAttrs) {
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
        if (availability != Availability.ONLINE) {
            addFileRef(tuple.get(QFileRef.fileRef.filePath),
                    tuple.get(QFileRef.fileRef.transferSyntaxUID),
                    tuple.get(QFileRef.fileRef.fileTimeZone),
                    tuple.get(QFileSystem.fileSystem.uri),
                    tuple.get(QFileSystem.fileSystem.availability));
            addFileRef(tuple.get(RetrieveServiceEJB.filealiastableref.filePath),
                    tuple.get(RetrieveServiceEJB.filealiastableref.fileTimeZone),
                    tuple.get(RetrieveServiceEJB.filealiastableref.transferSyntaxUID),
                    tuple.get(RetrieveServiceEJB.filealiastablereffilesystem.uri),
                    tuple.get(RetrieveServiceEJB.filealiastablereffilesystem.availability));
        }
    }
 
    private void addFileRef(String filePath, String tsuid,
            String fileTimeZone, String fsURI, Availability availability) {
        if (availability != null && availability.compareTo(this.availability) < 0) {
            locator = new ArchiveInstanceLocator(
                    cuid, iuid, tsuid, fsURI + '/' + filePath, fileTimeZone);
            this.availability = availability;
        }
    }

    ArchiveInstanceLocator build() {
        if (locator == null) {
            new ArchiveInstanceLocator(cuid, iuid, null, aetURI(), null);
        }
        locator.setObject(attrs);
        return locator;
    }

    private String aetURI() {
        String aet;
        if (retrieveAETs != null)
            aet = StringUtils.cut(retrieveAETs, 0, '\\');
        else if (externalRetrieveAET != null)
            aet = externalRetrieveAET;
        else
            return null;
        return "aet:" + aet;
    }
}