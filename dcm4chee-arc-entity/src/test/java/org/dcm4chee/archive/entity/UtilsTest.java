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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
public class UtilsTest {

    /**
     * Japanese Patient with ASCII (default) study
     * 
     * Japanese Contains ASCII, therefore no changes to the Codecs
     */
    @Test
    public void testMergeJapanesePatientWithASCIIStudy() throws IOException {

        Attributes patientAttrs = new Attributes();
        patientAttrs.setString(Tag.PatientID, VR.LO, "patID");
        patientAttrs.setString(Tag.IssuerOfPatientID, VR.LO, "issuer");
        Attributes otherids = new Attributes();
        otherids.setString(Tag.PatientID, VR.LO, "otherID");
        patientAttrs.newSequence(Tag.OtherPatientIDsSequence, 1).add(otherids);
        patientAttrs.setBytes(Tag.PatientName, VR.LO,
                SpecificCharacterSetValues.JAPANESE_PERSON_NAME_JISX0201_BYTES);
        String[] patientCodecs = new String[] { "ISO_IR 13", "ISO_IR 87" }; // JIS_X_201/JIS_X_208
        patientAttrs.setString(Tag.SpecificCharacterSet, VR.CS, patientCodecs);

        assertTrue(new HashSet<String>(Arrays.asList(patientCodecs))
                .equals(new HashSet<String>(Arrays.asList(patientAttrs
                        .getSpecificCharacterSet().toCodes()))));

        assertTrue(Arrays.equals(
                new Attributes(patientAttrs).getBytes(Tag.PatientName),
                SpecificCharacterSetValues.JAPANESE_PERSON_NAME_JISX0201_BYTES));

        assertTrue(new Attributes(patientAttrs)
                .getString(Tag.PatientName)
                .equals(SpecificCharacterSetValues.JAPANESE_PERSON_NAME_JISX0201));

        Attributes studyAttrs = new Attributes();
        studyAttrs.setString(Tag.StudyInstanceUID, VR.UI, "studyID");

        Attributes seriesAttrs = new Attributes();
        seriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI, "seriesID");

        Attributes instanceAttrs = new Attributes();
        instanceAttrs.setString(Tag.SOPClassUID, VR.UI, "1.2.3.4");
        instanceAttrs.setString(Tag.SOPInstanceUID, VR.UI, "4.3.2.1");

        Attributes mergedAttrs = Utils.mergeAndNormalize(patientAttrs,
                studyAttrs, seriesAttrs, instanceAttrs);

        String[] expectedMergedCodecs = new String[] { "ISO_IR 13", "ISO_IR 87" };
        
        assertTrue(new HashSet<String>(Arrays.asList(expectedMergedCodecs))
                .equals(new HashSet<String>(Arrays.asList(mergedAttrs
                        .getSpecificCharacterSet().toCodes()))));

        assertTrue(Arrays.equals(
                new Attributes(mergedAttrs).getBytes(Tag.PatientName),
                SpecificCharacterSetValues.JAPANESE_PERSON_NAME_JISX0201_BYTES));

        assertTrue(new Attributes(mergedAttrs)
                .getString(Tag.PatientName)
                .equals(SpecificCharacterSetValues.JAPANESE_PERSON_NAME_JISX0201));

    }
    
    /**
     * Japanese Patient with ISO LATIN study
     * 
     * Japanese does not Contain ISO LATIN, therefore all is changed to UTF-8
     */
    @Test
    public void testMergeJapanesePatientWithISOLATINStudy() throws IOException {

        Attributes patientAttrs = new Attributes();
        patientAttrs.setString(Tag.PatientID, VR.LO, "patID");
        patientAttrs.setString(Tag.IssuerOfPatientID, VR.LO, "issuer");
        Attributes otherids = new Attributes();
        otherids.setString(Tag.PatientID, VR.LO, "otherID");
        patientAttrs.newSequence(Tag.OtherPatientIDsSequence, 1).add(otherids);
        patientAttrs.setBytes(Tag.PatientName, VR.LO,
                SpecificCharacterSetValues.JAPANESE_PERSON_NAME_JISX0201_BYTES);
        String[] patientCodecs = new String[] { "ISO_IR 13", "ISO_IR 87" }; // JIS_X_201/JIS_X_208
        patientAttrs.setString(Tag.SpecificCharacterSet, VR.CS, patientCodecs);

        assertTrue(new HashSet<String>(Arrays.asList(patientCodecs))
                .equals(new HashSet<String>(Arrays.asList(patientAttrs
                        .getSpecificCharacterSet().toCodes()))));

        assertTrue(Arrays.equals(
                new Attributes(patientAttrs).getBytes(Tag.PatientName),
                SpecificCharacterSetValues.JAPANESE_PERSON_NAME_JISX0201_BYTES));

        assertTrue(new Attributes(patientAttrs)
                .getString(Tag.PatientName)
                .equals(SpecificCharacterSetValues.JAPANESE_PERSON_NAME_JISX0201));

        Attributes studyAttrs = new Attributes();
        String[] studyCodecs = new String[] { "ISO_IR 100" }; // ISO_8859_1
        studyAttrs.setString(Tag.StudyInstanceUID, VR.UI, "studyID");
        studyAttrs.setString(Tag.SpecificCharacterSet, VR.CS, studyCodecs);

        Attributes seriesAttrs = new Attributes();
        seriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI, "seriesID");

        Attributes instanceAttrs = new Attributes();
        instanceAttrs.setString(Tag.SOPClassUID, VR.UI, "1.2.3.4");
        instanceAttrs.setString(Tag.SOPInstanceUID, VR.UI, "4.3.2.1");

        Attributes mergedAttrs = Utils.mergeAndNormalize(patientAttrs,
                studyAttrs, seriesAttrs, instanceAttrs);

        String[] expectedMergedCodecs = new String[] { "ISO_IR 192" }; //UTF-8
        
        assertTrue(new HashSet<String>(Arrays.asList(expectedMergedCodecs))
                .equals(new HashSet<String>(Arrays.asList(mergedAttrs
                        .getSpecificCharacterSet().toCodes()))));

        assertTrue(Arrays.equals(
                new Attributes(mergedAttrs).getBytes(Tag.PatientName),
                SpecificCharacterSetValues.JAPANESE_PERSON_NAME_UTF8_BYTES));

        assertTrue(new Attributes(mergedAttrs)
                .getString(Tag.PatientName)
                .equals(SpecificCharacterSetValues.JAPANESE_PERSON_NAME_JISX0201));

    }
    
    /**
     * German (ISO LATIN) Patient with ASCII (default) study
     *  
     * German Contains ASCII, therefore no changes to the Codecs
     */
    @Test
    public void testMergeGermanPatientWithASCIIStudy() throws IOException {

        Attributes patientAttrs = new Attributes();
        patientAttrs.setString(Tag.PatientID, VR.LO, "patID");
        patientAttrs.setString(Tag.IssuerOfPatientID, VR.LO, "issuer");
        Attributes otherids = new Attributes();
        otherids.setString(Tag.PatientID, VR.LO, "otherID");
        patientAttrs.newSequence(Tag.OtherPatientIDsSequence, 1).add(otherids);
        patientAttrs.setBytes(Tag.PatientName, VR.LO,
                SpecificCharacterSetValues.GERMAN_PERSON_NAME_BYTE);
        String[] patientCodecs = new String[] { "ISO_IR 100" }; // ISO_8859_1
        patientAttrs.setString(Tag.SpecificCharacterSet, VR.CS, patientCodecs);

        assertTrue(new HashSet<String>(Arrays.asList(patientCodecs))
                .equals(new HashSet<String>(Arrays.asList(patientAttrs
                        .getSpecificCharacterSet().toCodes()))));

        assertTrue(Arrays.equals(
                new Attributes(patientAttrs).getBytes(Tag.PatientName),
                SpecificCharacterSetValues.GERMAN_PERSON_NAME_BYTE));

        assertTrue(new Attributes(patientAttrs)
                .getString(Tag.PatientName)
                .equals(SpecificCharacterSetValues.GERMAN_PERSON_NAME));

        Attributes studyAttrs = new Attributes();
        studyAttrs.setString(Tag.StudyInstanceUID, VR.UI, "studyID");

        Attributes seriesAttrs = new Attributes();
        seriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI, "seriesID");

        Attributes instanceAttrs = new Attributes();
        instanceAttrs.setString(Tag.SOPClassUID, VR.UI, "1.2.3.4");
        instanceAttrs.setString(Tag.SOPInstanceUID, VR.UI, "4.3.2.1");

        Attributes mergedAttrs = Utils.mergeAndNormalize(patientAttrs,
                studyAttrs, seriesAttrs, instanceAttrs);

        String[] expectedMergedCodecs = new String[] { "ISO_IR 100" };

        
        assertTrue(new HashSet<String>(Arrays.asList(expectedMergedCodecs))
                .equals(new HashSet<String>(Arrays.asList(mergedAttrs
                        .getSpecificCharacterSet().toCodes()))));

        assertTrue(Arrays.equals(
                new Attributes(mergedAttrs).getBytes(Tag.PatientName),
                SpecificCharacterSetValues.GERMAN_PERSON_NAME_BYTE));

        assertTrue(new Attributes(mergedAttrs)
                .getString(Tag.PatientName)
                .equals(SpecificCharacterSetValues.GERMAN_PERSON_NAME));

    }
    
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format(" %02x", b));
        }
        return builder.toString();
    }

}
