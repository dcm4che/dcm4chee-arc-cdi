package org.dcm4chee.archive.store.hooks;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4chee.archive.entity.PersonName;

/**
 * Created by Umberto Cappellini on 12/11/15.
 */
public interface PersonNameManagerHook {

        PersonName findOrCreate(int nametag, Attributes attrs, FuzzyStr fuzzyStr, String nullValue)
                throws DicomServiceException;

        PersonName update(PersonName previous, int nametag, Attributes attrs, FuzzyStr fuzzyStr, String nullValue)
                throws DicomServiceException;
}
