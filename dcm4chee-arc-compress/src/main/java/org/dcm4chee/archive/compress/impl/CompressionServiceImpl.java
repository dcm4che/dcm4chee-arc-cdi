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
 * Portions created by the Initial Developer are Copyright (C) 2011-2013
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import javax.enterprise.context.ApplicationScoped;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.CompressionRule;
import org.dcm4che3.imageio.codec.Compressor;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.compress.CompressionService;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ApplicationScoped
public class CompressionServiceImpl implements CompressionService {

    @Override
    public void compress(CompressionRule rule, Path src, OutputStream out,
            MessageDigest digest, String tsuid, Attributes attrs)
            throws IOException {
        Compressor compressor = new Compressor(attrs, tsuid);
        try {
            if (digest != null) {
                digest.reset();
                out = new DigestOutputStream(out, digest);
            }
            DicomOutputStream dout = new DicomOutputStream(
                    new BufferedOutputStream(out,65536), 
                    UID.ExplicitVRLittleEndian);
            out = dout;
            String ts = rule.getTransferSyntax();
            compressor.compress(ts, rule.getImageWriteParams());
            Attributes fmi = attrs.createFileMetaInformation(ts);
            fmi.setString(Tag.TransferSyntaxUID, VR.UI, ts);
            dout.writeDataset(fmi, attrs);
            dout.flush();
        } finally {
            SafeClose.close(compressor);
        }
    }
}
