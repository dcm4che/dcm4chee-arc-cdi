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

package org.dcm4chee.archive.compress.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.CompressionRule;
import org.dcm4che3.imageio.codec.CompressionRules;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.store.decorators.DelegatingStoreService;
import org.dcm4chee.archive.task.WeightWatcher;
import org.dcm4chee.conf.decorators.DynamicDecorator;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Extends the logic of the Store Service with rule-based on-the-fly compression.
 *
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@DynamicDecorator
public class StoreServiceCompressDecorator extends DelegatingStoreService {

    private static Logger LOG = LoggerFactory.getLogger(StoreServiceCompressDecorator.class);

    @Inject
    private StorageService storageService;

    @Inject
    private WeightWatcher weightWatcher;

    @Override
    public StorageContext processFile(StoreContext context) throws DicomServiceException {
        // if possible, compress the file, store on file system and
        // update store context. Otherwise call the standard processFile.
        StorageContext bulkdataContext = compress(context);
        if (bulkdataContext == null) { // compression wasn't needed/failed
            bulkdataContext = getNextDecorator().processFile(context);
        }

        // in any case, nullify PixelData, just to be consistent with StoreCompressedTask which also has to nullify PixelData
        // (to ensure garbage collection of Compressor is possible)
        if (context.getAttributes().contains(Tag.PixelData)) {
            context.getAttributes().setNull(Tag.PixelData, VR.OB);
        }

        return bulkdataContext;
    }

    private StorageContext compress(StoreContext context)
            throws DicomServiceException {

        StoreSession session = context.getStoreSession();

        ArchiveAEExtension archiveAE = session.getArchiveAEExtension();
        CompressionRules rules = archiveAE.getCompressionRules();

        Attributes attributes = context.getAttributes();
        Object pixelData = attributes.getValue(Tag.PixelData);

        if (!(pixelData instanceof BulkData || pixelData instanceof byte[]))
            return null; // already compressed or no pixel data at all

        CompressionRule rule = rules.findCompressionRule(session.getRemoteAET(), attributes);

        if (rule == null)
            return null;

        LOG.info("Compression rule selected: " + rule.getCommonName());

        StorageContext bulkdataContext;
        try {
            bulkdataContext = weightWatcher.execute(new StoreCompressedTask(storageService, context, rule));
        } catch (Exception e) {
            if (e instanceof DicomServiceException)
                throw (DicomServiceException) e;
            throw new DicomServiceException(Status.UnableToProcess, e);
        }

        return bulkdataContext;
    }

}
