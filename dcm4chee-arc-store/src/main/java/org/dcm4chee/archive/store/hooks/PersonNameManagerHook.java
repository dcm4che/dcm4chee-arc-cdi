package org.dcm4chee.archive.store.hooks;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.soundex.FuzzyStr;
import org.dcm4chee.archive.entity.PersonName;

/**
 * Hook to select a specific PersonName entity associated to a Referring, Performing or Requesting Physician.
 *
 * Created by Umberto Cappellini on 12/11/15.
 */
public interface PersonNameManagerHook {

        /**
         * Given incoming attributes, selects an existing PersonName entity if existing,
         * or creates a new one.
         *
         * @param nametag DICOM Tag to be used to retrieve the physician name from the attributes.
         * @param attrs Incoming DICOM attributes.
         * @param fuzzyStr Fuzzy string used to calculate the phonetic parts of the name.
         * @param nullValue Value to be persisted representing an empty name part.
         * @return The selected or created PersonName
         * @throws DicomServiceException
         */
        PersonName findOrCreate(int nametag, Attributes attrs, FuzzyStr fuzzyStr, String nullValue)
                throws DicomServiceException;

        /**
         * Given incoming attributes and the existing PersonName entity, updates the existing with
         * new data, or creates a new one if the existing is null.
         *
         * @param previous existing PersonName entity to be updated
         * @param nametag DICOM Tag to be used to retrieve the physician name from the attributes.
         * @param attrs Incoming DICOM attributes.
         * @param fuzzyStr Fuzzy string used to calculate the phonetic parts of the name.
         * @param nullValue Value to be persisted representing an empty name part.
         * @return The updated or created PersonName
         * @throws DicomServiceException
         */
        PersonName update(PersonName previous, int nametag, Attributes attrs, FuzzyStr fuzzyStr, String nullValue)
                throws DicomServiceException;
}
