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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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

package org.dcm4chee.archive.wado;

import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.DatasetWithFMI;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.store.scu.CStoreSCUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to read a dataset (with file meta information) from an instance locator. Given a locator with fallbacks it
 * selects the first readable location and reads the dataset.
 * <p>
 * It also handles updating the dataset with the newest values from the database (merge) and coercion.
 * <p>
 * Bulk data will not be read to memory but will be included as URI references.
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
class LocatorDatasetReader {

    private static final Logger LOG = LoggerFactory.getLogger(LocatorDatasetReader.class);

    private final CStoreSCUContext context;
    private final CStoreSCUService storescuService;

    private ArchiveInstanceLocator locatorWithFallbacks;
    private ArchiveInstanceLocator selectedLocator;
    private DatasetWithFMI datasetWithFMI;

    public LocatorDatasetReader(ArchiveInstanceLocator locatorWithFallbacks, CStoreSCUContext context, CStoreSCUService storescuService) {
        this.locatorWithFallbacks = locatorWithFallbacks;
        this.context = context;
        this.storescuService = storescuService;
    }

    public LocatorDatasetReader read() throws IOException {
        selectedLocator = locatorWithFallbacks;

        DatasetWithFMI originalDatasetWithFMI = null;
        do {
            try {
                originalDatasetWithFMI = readFrom(selectedLocator);
            } catch (IOException e) {
                LOG.info("Failed to read Data Set with iuid={} from {}@{}",
                        selectedLocator.iuid, selectedLocator.getFilePath(), selectedLocator.getStorageSystem(), e);
                selectedLocator = selectedLocator.getFallbackLocator();
                if (selectedLocator == null)
                    throw e;
                LOG.info("Try read Data Set from alternative location");
            }
        } while (originalDatasetWithFMI == null);

        Attributes dataset = originalDatasetWithFMI.getDataset();

        if (context.getRemoteAE() != null) {
            storescuService.coerceFileBeforeMerge(selectedLocator, dataset, context);
        }
        dataset = Utils.mergeAndNormalize(dataset, (Attributes) selectedLocator.getObject());
        if (context.getRemoteAE() != null) {
            storescuService.coerceAttributes(dataset, context);
        }

        datasetWithFMI = new DatasetWithFMI(originalDatasetWithFMI.getFileMetaInformation(), dataset);

        return this;
    }

    public ArchiveInstanceLocator getSelectedLocator() {
        return selectedLocator;
    }

    public DatasetWithFMI getDatasetWithFMI() {
        return datasetWithFMI;
    }

    private DatasetWithFMI readFrom(ArchiveInstanceLocator inst) throws IOException {
        try (DicomInputStream din = new DicomInputStream(storescuService.getFile(inst).toFile())) {
            din.setIncludeBulkData(IncludeBulkData.URI);
            return din.readDatasetWithFMI();
        }
    }
}
