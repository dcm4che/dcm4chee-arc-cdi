package org.dcm4chee.archive.store;

import org.dcm4chee.archive.conf.ArchiveAEExtension;

public interface StoreSource {

    String getSendingAETitle(ArchiveAEExtension arcAE);

}
