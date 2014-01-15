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

package org.dcm4chee.archive.store.impl;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.dcm4che.data.BulkData;
import org.dcm4che.data.Tag;
import org.dcm4che.imageio.codec.CompressionRule;
import org.dcm4che.net.Status;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.util.TagUtils;
import org.dcm4chee.archive.compress.CompressionService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.FileSystem;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSource;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * 
 */
@Decorator
public abstract class StoreServiceCompressDecorator implements StoreService{

    // injected StoreService to be decorated
    @Inject @Delegate @Any StoreService storeService;
    
    //injected compression service
    @Inject
    private CompressionService compressionService;
    

    public void moveFile(StoreContext storeContext)
            throws DicomServiceException {

        // if possible, compress the file, store on file system and
        // update store context. Otherwise call the standard moveFile.
        if (!compress(storeContext)) {
            storeService.moveFile(storeContext);
        }
    }

    private boolean compress(StoreContext storeContext) {

        if (storeContext == null
                || storeContext.getArchiveAEExtension() == null
                || storeContext.getAttributes() == null
                || storeContext.getSendingAETitle() == null
                || storeContext.getTransferSyntax() == null
                || storeContext.getStorePath() == null
                || storeContext.getFile() == null) {
            return false;
        } else {

            if (!(storeContext.getAttributes().getValue(Tag.PixelData) instanceof BulkData))
                return false;

            CompressionRule compressionRule = storeContext
                    .getArchiveAEExtension().getCompressionRules()
                    .findCompressionRule(storeContext.getSendingAETitle(), storeContext.getAttributes());
            
            if (compressionRule == null)
                return false;

            try {
                Path storePath = createFile(storeContext.getStorePath());
                MessageDigest digest = messageDigestOf(storeContext.getArchiveAEExtension());
                compressionService.compress(compressionRule,
                        storeContext.getFile().toFile(), 
                        storePath.toFile(), 
                        digest, 
                        storeContext.getTransferSyntax(), 
                        storeContext.getAttributes());
                if (digest != null) {
                    //update store context after compression
                    storeContext.setDigest(digest.digest());
                    storeContext.setFile(storePath);
                    storeContext.setTransferSyntax(compressionRule.getTransferSyntax());
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
    
    private Path createFile(Path path) throws IOException {
        for (;;) {
            try {
                return Files.createFile(path);
            } catch (FileAlreadyExistsException e) {
                path = path.resolveSibling(
                        path.getFileName().toString() + '-');
            }
        }
    }
    
    private MessageDigest messageDigestOf(ArchiveAEExtension aeExt)
            throws DicomServiceException {
        String algorithm = aeExt.getDigestAlgorithm();
        try {
            return algorithm != null
                    ? MessageDigest.getInstance(algorithm)
                    : null;
        } catch (NoSuchAlgorithmException e) {
            throw new DicomServiceException(
                    Status.ProcessingFailure, e);
        }
    }
    
    
   
}
