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

package org.dcm4chee.archive.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4chee.archive.conf.PrivateTag;
import org.dcm4chee.storage.conf.Availability;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Utils {

    public static byte[] encodeAttributes(Attributes attrs) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        try {
            @SuppressWarnings("resource")
            DicomOutputStream dos = new DicomOutputStream(out,
                    UID.ExplicitVRLittleEndian);
            dos.writeDataset(null, attrs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    /**
     * 
     */
    public static String digestAttributes(Attributes attrs, MessageDigest digest) throws IOException {
        
        OutputStream nulloutputstream = new OutputStream() {
            /** Discards the specified byte. */
            @Override public void write(int b) {
            }
            /** Discards the specified byte array. */
            @Override public void write(byte[] b, int off, int len) {
            }};
        
        if (digest != null) {
            digest.reset();
            nulloutputstream = new DigestOutputStream(nulloutputstream, digest);
        }
        
        DicomOutputStream dout = new DicomOutputStream(nulloutputstream,
                UID.ExplicitVRLittleEndian);
        dout.writeDataset(null, attrs);
        dout.flush();
        dout.close();
        
        return TagUtils.toHexString(digest.digest());
    }

    public static Attributes decodeAttributes(byte[] b) {
        if (b == null || b.length == 0)
            return new Attributes(0);
        ByteArrayInputStream is = new ByteArrayInputStream(b);
        try {
            @SuppressWarnings("resource")
            DicomInputStream dis = new DicomInputStream(is);
            return dis.readDataset(-1, -1);
        } catch (IOException e) {
            throw new BlobCorruptedException(e);
        }
    }

    public static void decodeAttributes(Attributes attrs, byte[] b) {
        if (b == null || b.length == 0)
            return;
        ByteArrayInputStream is = new ByteArrayInputStream(b);
        try {
            @SuppressWarnings("resource")
            DicomInputStream dis = new DicomInputStream(is);
            dis.readFileMetaInformation();
            dis.readAttributes(attrs, -1, -1);
        } catch (IOException e) {
            throw new BlobCorruptedException(e);
        }
    }

    public static void setStudyQueryAttributes(Attributes attrs,
            int numberOfStudyRelatedSeries, int numberOfStudyRelatedInstances,
            String modalitiesInStudy, String sopClassesInStudy,
            int numberVisibleInstances, PrivateTag numberVisibleInstancesTag,
            Date lastUpdateTime, PrivateTag lastUpdateTimeTag) {

        attrs.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS,
                numberOfStudyRelatedSeries);
        attrs.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS,
                numberOfStudyRelatedInstances);
        attrs.setString(Tag.ModalitiesInStudy, VR.CS,
                StringUtils.split(modalitiesInStudy, '\\'));
        attrs.setString(Tag.SOPClassesInStudy, VR.CS,
                StringUtils.split(sopClassesInStudy, '\\'));
        if (lastUpdateTimeTag!=null && lastUpdateTime!=null)
            attrs.setDate(lastUpdateTimeTag.getCreator(),
            lastUpdateTimeTag.getIntTag(),VR.DT,lastUpdateTime);
        if (numberVisibleInstancesTag!=null)
            attrs.setInt(numberVisibleInstancesTag.getCreator(),
                    numberVisibleInstancesTag.getIntTag(), VR.IS,
                    numberVisibleInstances);
    }

    public static void setSeriesQueryAttributes(Attributes attrs,
            int numberOfSeriesRelatedInstances, int numberVisibleInstances,
            PrivateTag numberVisibleInstancesTag, Date lastUpdateTime,
            PrivateTag lastUpdateTimeTag) {
        attrs.setInt(Tag.NumberOfSeriesRelatedInstances, VR.IS,
                numberOfSeriesRelatedInstances);
        if (numberVisibleInstancesTag!=null)
            attrs.setInt(numberVisibleInstancesTag.getCreator(),
                    numberVisibleInstancesTag.getIntTag(), VR.IS,
                    numberVisibleInstances);
        if (lastUpdateTimeTag!=null && lastUpdateTime!=null)
            attrs.setDate(lastUpdateTimeTag.getCreator(),
                    lastUpdateTimeTag.getIntTag(), VR.DT, lastUpdateTime);
    }

    public static String[] decodeAETs(String aetsSeparated) {
        return StringUtils.split(aetsSeparated, '\\');
    }

    public static void setRetrieveAET(Attributes attrs, String retrieveAETs) {
        if (retrieveAETs != null)
                attrs.setString(Tag.RetrieveAETitle, VR.AE,
                        StringUtils.split(retrieveAETs, '\\'));
    }

    public static void setRetrieveAET(Attributes attrs, String[] retrieveAETs) {
        if (retrieveAETs != null)
                attrs.setString(Tag.RetrieveAETitle, VR.AE, retrieveAETs);
    }

    public static void setAvailability(Attributes attrs,
            Availability availability) {
        attrs.setString(Tag.InstanceAvailability, VR.CS,
                availability.toString());
    }

    public static String[] intersection(String[] ss1, String[] ss2) {
        int l = 0;
        for (int i = 0; i < ss1.length; i++)
            if (contains(ss2, ss1[i]))
                ss1[l++] = ss1[i];
        return l == ss1.length ? ss1 : Arrays.copyOf(ss1, l);
    }

    public static boolean contains(String[] ss, String s0) {
        for (String s : ss)
            if (s0.equals(s))
                return true;
        return false;
    }

    public static Attributes mergeAndNormalize(Attributes... attrsList) {

        SpecificCharacterSet globalCs = null;

        for (Attributes attrs : attrsList) {

            SpecificCharacterSet cs = attrs.getSpecificCharacterSet();

            if (globalCs == null) {
                globalCs = cs; // first
            } else {

                if (!cs.equals(globalCs)) {
                    if (!(globalCs.containsASCII() && cs.isASCII())) {
                        if (globalCs.isUTF8()) {
                            // convert to UTF8
                            convertToUTF8(attrsList);
                            break;
                        } else if (cs.isUTF8()) {
                            globalCs = cs;
                            convertToUTF8(attrsList);
                            break;
                        } else if (globalCs.isASCII() && cs.containsASCII()) {
                            // do not decode
                            globalCs = cs;
                        } else {
                            // incompatible codes, set all to UTF-8
                            globalCs = SpecificCharacterSet
                                    .valueOf("ISO_IR 192"); // UTF-8
                            convertToUTF8(attrsList);
                            break;
                        }
                    }
                }
            }
        }

        // merge
        Attributes mergedAttrs = new Attributes();
        for (Attributes attrs : attrsList)
            mergedAttrs.addAll(attrs);

        mergedAttrs.setString(Tag.SpecificCharacterSet, VR.CS,
                globalCs.toCodes());

        return mergedAttrs;
    }

    private static void convertToUTF8(Attributes... attrsList) {
        for (Attributes attrs : attrsList) {
            if (!attrs.getSpecificCharacterSet().isUTF8())
                attrs.setSpecificCharacterSet("ISO_IR 192"); // UTF-8
        }
    }
}
