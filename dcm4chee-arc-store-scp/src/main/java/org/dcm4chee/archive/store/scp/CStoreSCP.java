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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4chee.archive.store.scp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.PDVInputStream;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.service.BasicCStoreSCP;
import org.dcm4che.net.service.DicomService;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.FileSystem;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
@Typed(DicomService.class)
public class CStoreSCP extends BasicCStoreSCP {

    @Inject
    private StoreService storeService;

    @Override
    protected void store(Association as, PresentationContext pc, Attributes rq,
            PDVInputStream data, Attributes rsp) throws IOException {

        try {
            ApplicationEntity ae = as.getApplicationEntity();
            ArchiveAEExtension arcAE = ae.getAEExtensionNotNull(ArchiveAEExtension.class);
            AssociationSource source = new AssociationSource(as);
            FileSystem fs = (FileSystem) as.getProperty("CStoreSCP.FileSystem");
            Path spoolDirectory = (Path) as.getProperty("CStoreSCP.SpoolDirectory");
            if (fs == null) {
                fs = storeService.selectFileSystem(source, arcAE);
                spoolDirectory = Files.createTempDirectory(
                        fs.getPath().resolve(arcAE.getSpoolDirectoryPath()),
                        null);
                as.setProperty("CStoreSCP.FileSystem", fs);
                as.setProperty("CStoreSCP.SpoolDirectory", spoolDirectory);
            }
            String tsuid = pc.getTransferSyntax();
            MessageDigest digest = arcAE.getMessageDigest();
            Path file = spool(spoolDirectory, fmiOf(as, tsuid, rq), data, 
                    digest);
            StoreContext storeContext = storeService.createStoreContext(
                    storeService, source, arcAE, fs, file,
                    digest != null ? digest.digest() : null);
            storeService.parseAttributes(storeContext);
            storeService.coerceAttributes(storeContext);
            storeService.moveFile(storeContext);
            storeService.updateDB(storeContext);
            Attributes coercedAttrs = storeContext.getCoercedAttributes();
            if (!coercedAttrs.isEmpty() 
                    && !arcAE.isSuppressWarningCoercionOfDataElements()) {
                rsp.setInt(Tag.Status, VR.US, Status.CoercionOfDataElements);
                rsp.setInt(Tag.OffendingElement, VR.AT, coercedAttrs.tags());
            }
        } catch (DicomServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    private Path spool(Path spoolDirectory, Attributes fmi,
            PDVInputStream data, MessageDigest digest) throws IOException {
        Path path = Files.createTempFile(spoolDirectory, null, ".dcm");
        try (
            DicomOutputStream out = new DicomOutputStream(
                    new BufferedOutputStream(newDigestOutputStream(path, digest)),
                    UID.ExplicitVRLittleEndian)
        ) {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        }
        return path;
    }

    private OutputStream newDigestOutputStream(Path path, MessageDigest digest)
            throws IOException {
        OutputStream out = Files.newOutputStream(path);
        return digest != null
                ? out
                : new DigestOutputStream(out, digest);
    }

    private Attributes fmiOf(Association as, String tsuid,
            Attributes rq) {
        return as.createFileMetaInformation(
                rq.getString(Tag.AffectedSOPInstanceUID),
                rq.getString(Tag.AffectedSOPClassUID),
                tsuid);
    }

    @Override
    public void onClose(Association as) {
        deleteDirectory((Path) as.getProperty("CStoreSCP.SpoolDirectory"));
    }

    private void deleteDirectory(Path spoolDirectory) {
        if (spoolDirectory == null)
            return;

        try {
            for (Path path : Files.newDirectoryStream(spoolDirectory)) {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Files.delete(spoolDirectory);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
