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

import org.dcm4che3.data.SpecificCharacterSet;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class SpecificCharacterSetValues {

    protected static final String LT_DELIMS = "\n\f\r";
    protected static final String PN_DELIMS = "^=\\";
    protected static final String GERMAN_PERSON_NAME = "Äneas^Rüdiger";
    protected static final String FRENCH_PERSON_NAME = "Buc^Jérôme";
    protected static final String RUSSIAN_PERSON_NAME = "Люкceмбypг";
    protected static final String ARABIC_PERSON_NAME = "قباني^لنزار";
    protected static final String GREEK_PERSON_NAME = "Διονυσιος";
    protected static final String HEBREW_PERSON_NAME = "שרון^דבורה";
    protected static final String JAPANESE_PERSON_NAME_ASCII = "Yamada^Tarou=山田^太郎=やまだ^たろう";
    protected static final String JAPANESE_PERSON_NAME_JISX0201 = "ﾔﾏﾀﾞ^ﾀﾛｳ=山田^太郎=やまだ^たろう";
    protected static final String KOREAN_PERSON_NAME = "Hong^Gildong=洪^吉洞=홍^길동";
    protected static final String KOREAN_LONG_TEXT = "The 1st line includes 길동.\r\n"
            + "The 2nd line includes 길동, too.\r\n" + "The 3rd line.";
    protected static final String CHINESE_PERSON_NAME_UTF8 = "Wang^XiaoDong=王^小東=";
    protected static final String CHINESE_PERSON_NAME_GB18030 = "Wang^XiaoDong=王^小东=";

    protected static final byte[] GERMAN_PERSON_NAME_BYTE = { (byte) 0xc4,
            (byte) 0x6e, (byte) 0x65, (byte) 0x61, (byte) 0x73, (byte) 0x5e,
            (byte) 0x52, (byte) 0xfc, (byte) 0x64, (byte) 0x69, (byte) 0x67,
            (byte) 0x65, (byte) 0x72 };

    protected static final byte[] FRENCH_PERSON_NAME_BYTE = { (byte) 0x42,
            (byte) 0x75, (byte) 0x63, (byte) 0x5e, (byte) 0x4a, (byte) 0xe9,
            (byte) 0x72, (byte) 0xf4, (byte) 0x6d, (byte) 0x65 };

    protected static final byte[] RUSSIAN_PERSON_NAME_BYTE = { (byte) 0xbb,
            (byte) 0xee, (byte) 0xda, (byte) 0x63, (byte) 0x65, (byte) 0xdc,
            (byte) 0xd1, (byte) 0x79, (byte) 0x70, (byte) 0xd3 };

    protected static final byte[] ARABIC_PERSON_NAME_BYTE = { (byte) 0xe2,
            (byte) 0xc8, (byte) 0xc7, (byte) 0xe6, (byte) 0xea, (byte) 0x5e,
            (byte) 0xe4, (byte) 0xe6, (byte) 0xd2, (byte) 0xc7, (byte) 0xd1 };

    protected static final byte[] GREEK_PERSON_NAME_BYTE = { (byte) 0xc4,
            (byte) 0xe9, (byte) 0xef, (byte) 0xed, (byte) 0xf5, (byte) 0xf3,
            (byte) 0xe9, (byte) 0xef, (byte) 0xf2 };

    protected static final byte[] HEBREW_PERSON_NAME_BYTE = { (byte) 0xf9,
            (byte) 0xf8, (byte) 0xe5, (byte) 0xef, (byte) 0x5e, (byte) 0xe3,
            (byte) 0xe1, (byte) 0xe5, (byte) 0xf8, (byte) 0xe4 };

    protected static final byte[] JAPANESE_PERSON_NAME_ASCII_BYTES = {
            (byte) 0x59, (byte) 0x61, (byte) 0x6d, (byte) 0x61, (byte) 0x64,
            (byte) 0x61, (byte) 0x5e, (byte) 0x54, (byte) 0x61, (byte) 0x72,
            (byte) 0x6f, (byte) 0x75, (byte) 0x3d, (byte) 0x1b, (byte) 0x24,
            (byte) 0x42, (byte) 0x3b, (byte) 0x33, (byte) 0x45, (byte) 0x44,
            (byte) 0x1b, (byte) 0x28, (byte) 0x42, (byte) 0x5e, (byte) 0x1b,
            (byte) 0x24, (byte) 0x42, (byte) 0x42, (byte) 0x40, (byte) 0x4f,
            (byte) 0x3a, (byte) 0x1b, (byte) 0x28, (byte) 0x42, (byte) 0x3d,
            (byte) 0x1b, (byte) 0x24, (byte) 0x42, (byte) 0x24, (byte) 0x64,
            (byte) 0x24, (byte) 0x5e, (byte) 0x24, (byte) 0x40, (byte) 0x1b,
            (byte) 0x28, (byte) 0x42, (byte) 0x5e, (byte) 0x1b, (byte) 0x24,
            (byte) 0x42, (byte) 0x24, (byte) 0x3f, (byte) 0x24, (byte) 0x6d,
            (byte) 0x24, (byte) 0x26, (byte) 0x1b, (byte) 0x28, (byte) 0x42 };

    protected static final byte[] JAPANESE_PERSON_NAME_JISX0201_BYTES = {
            (byte) 0xd4, (byte) 0xcf, (byte) 0xc0, (byte) 0xde, (byte) 0x5e,
            (byte) 0xc0, (byte) 0xdb, (byte) 0xb3, (byte) 0x3d, (byte) 0x1b,
            (byte) 0x24, (byte) 0x42, (byte) 0x3b, (byte) 0x33, (byte) 0x45,
            (byte) 0x44, (byte) 0x1b, (byte) 0x28, (byte) 0x4a, (byte) 0x5e,
            (byte) 0x1b, (byte) 0x24, (byte) 0x42, (byte) 0x42, (byte) 0x40,
            (byte) 0x4f, (byte) 0x3a, (byte) 0x1b, (byte) 0x28, (byte) 0x4a,
            (byte) 0x3d, (byte) 0x1b, (byte) 0x24, (byte) 0x42, (byte) 0x24,
            (byte) 0x64, (byte) 0x24, (byte) 0x5e, (byte) 0x24, (byte) 0x40,
            (byte) 0x1b, (byte) 0x28, (byte) 0x4a, (byte) 0x5e, (byte) 0x1b,
            (byte) 0x24, (byte) 0x42, (byte) 0x24, (byte) 0x3f, (byte) 0x24,
            (byte) 0x6d, (byte) 0x24, (byte) 0x26, (byte) 0x1b, (byte) 0x28,
            (byte) 0x4a };

    protected static final byte[] JAPANESE_PERSON_NAME_UTF8_BYTES = {
            (byte) 0xef, (byte) 0xbe, (byte) 0x94, (byte) 0xef, (byte) 0xbe,
            (byte) 0x8f, (byte) 0xef, (byte) 0xbe, (byte) 0x80, (byte) 0xef,
            (byte) 0xbe, (byte) 0x9e, (byte) 0x5e, (byte) 0xef, (byte) 0xbe,
            (byte) 0x80, (byte) 0xef, (byte) 0xbe, (byte) 0x9b, (byte) 0xef,
            (byte) 0xbd, (byte) 0xb3, (byte) 0x3d, (byte) 0xe5, (byte) 0xb1,
            (byte) 0xb1, (byte) 0xe7, (byte) 0x94, (byte) 0xb0, (byte) 0x5e,
            (byte) 0xe5, (byte) 0xa4, (byte) 0xaa, (byte) 0xe9, (byte) 0x83,
            (byte) 0x8e, (byte) 0x3d, (byte) 0xe3, (byte) 0x82, (byte) 0x84,
            (byte) 0xe3, (byte) 0x81, (byte) 0xbe, (byte) 0xe3, (byte) 0x81,
            (byte) 0xa0, (byte) 0x5e, (byte) 0xe3, (byte) 0x81, (byte) 0x9f,
            (byte) 0xe3, (byte) 0x82, (byte) 0x8d, (byte) 0xe3, (byte) 0x81,
            (byte) 0x86 };

    protected static final byte[] KOREAN_PERSON_NAME_BYTES = { (byte) 0x48,
            (byte) 0x6f, (byte) 0x6e, (byte) 0x67, (byte) 0x5e, (byte) 0x47,
            (byte) 0x69, (byte) 0x6c, (byte) 0x64, (byte) 0x6f, (byte) 0x6e,
            (byte) 0x67, (byte) 0x3d, (byte) 0x1b, (byte) 0x24, (byte) 0x29,
            (byte) 0x43, (byte) 0xfb, (byte) 0xf3, (byte) 0x5e, (byte) 0x1b,
            (byte) 0x24, (byte) 0x29, (byte) 0x43, (byte) 0xd1, (byte) 0xce,
            (byte) 0xd4, (byte) 0xd7, (byte) 0x3d, (byte) 0x1b, (byte) 0x24,
            (byte) 0x29, (byte) 0x43, (byte) 0xc8, (byte) 0xab, (byte) 0x5e,
            (byte) 0x1b, (byte) 0x24, (byte) 0x29, (byte) 0x43, (byte) 0xb1,
            (byte) 0xe6, (byte) 0xb5, (byte) 0xbf };

    protected static final byte[] KOREAN_LONG_TEXT_BYTES = { (byte) 0x1b,
            (byte) 0x24, (byte) 0x29, (byte) 0x43, (byte) 0x54, (byte) 0x68,
            (byte) 0x65, (byte) 0x20, (byte) 0x31, (byte) 0x73, (byte) 0x74,
            (byte) 0x20, (byte) 0x6c, (byte) 0x69, (byte) 0x6e, (byte) 0x65,
            (byte) 0x20, (byte) 0x69, (byte) 0x6e, (byte) 0x63, (byte) 0x6c,
            (byte) 0x75, (byte) 0x64, (byte) 0x65, (byte) 0x73, (byte) 0x20,
            (byte) 0xb1, (byte) 0xe6, (byte) 0xb5, (byte) 0xbf, (byte) 0x2e,
            (byte) 0x0d, (byte) 0x0a, (byte) 0x1b, (byte) 0x24, (byte) 0x29,
            (byte) 0x43, (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20,
            (byte) 0x32, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x6c,
            (byte) 0x69, (byte) 0x6e, (byte) 0x65, (byte) 0x20, (byte) 0x69,
            (byte) 0x6e, (byte) 0x63, (byte) 0x6c, (byte) 0x75, (byte) 0x64,
            (byte) 0x65, (byte) 0x73, (byte) 0x20, (byte) 0xb1, (byte) 0xe6,
            (byte) 0xb5, (byte) 0xbf, (byte) 0x2c, (byte) 0x20, (byte) 0x74,
            (byte) 0x6f, (byte) 0x6f, (byte) 0x2e, (byte) 0x0d, (byte) 0x0a,
            (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x33,
            (byte) 0x72, (byte) 0x64, (byte) 0x20, (byte) 0x6c, (byte) 0x69,
            (byte) 0x6e, (byte) 0x65, (byte) 0x2e };

    protected static final byte[] CHINESE_PERSON_NAME_UTF8_BYTES = {
            (byte) 0x57, (byte) 0x61, (byte) 0x6e, (byte) 0x67, (byte) 0x5e,
            (byte) 0x58, (byte) 0x69, (byte) 0x61, (byte) 0x6f, (byte) 0x44,
            (byte) 0x6f, (byte) 0x6e, (byte) 0x67, (byte) 0x3d, (byte) 0xe7,
            (byte) 0x8e, (byte) 0x8b, (byte) 0x5e, (byte) 0xe5, (byte) 0xb0,
            (byte) 0x8f, (byte) 0xe6, (byte) 0x9d, (byte) 0xb1, (byte) 0x3d };

    protected static final byte[] CHINESE_PERSON_NAME_GB18030_BYTES = {
            (byte) 0x57, (byte) 0x61, (byte) 0x6e, (byte) 0x67, (byte) 0x5e,
            (byte) 0x58, (byte) 0x69, (byte) 0x61, (byte) 0x6f, (byte) 0x44,
            (byte) 0x6f, (byte) 0x6e, (byte) 0x67, (byte) 0x3d, (byte) 0xcd,
            (byte) 0xf5, (byte) 0x5e, (byte) 0xd0, (byte) 0xa1, (byte) 0xb6,
            (byte) 0xab, (byte) 0x3d };

    protected SpecificCharacterSet iso8859_1() {
        return SpecificCharacterSet.valueOf(new String[] { "ISO_IR 100" });
    }

    protected SpecificCharacterSet iso8859_5() {
        return SpecificCharacterSet.valueOf(new String[] { "ISO_IR 144" });
    }

    protected SpecificCharacterSet iso8859_6() {
        return SpecificCharacterSet.valueOf(new String[] { "ISO_IR 127" });
    }

    protected SpecificCharacterSet iso8859_7() {
        return SpecificCharacterSet.valueOf(new String[] { "ISO_IR 126" });
    }

    protected SpecificCharacterSet iso8859_8() {
        return SpecificCharacterSet.valueOf(new String[] { "ISO_IR 138" });
    }

    protected SpecificCharacterSet jisX0208() {
        return SpecificCharacterSet.valueOf(new String[] { null,
                "ISO 2022 IR 87" });
    }

    protected SpecificCharacterSet jisX0201() {
        return SpecificCharacterSet.valueOf(new String[] { "ISO 2022 IR 13",
                "ISO 2022 IR 87" });
    }

    protected SpecificCharacterSet ksx1001() {
        return SpecificCharacterSet.valueOf(new String[] { null,
                "ISO 2022 IR 149" });
    }

    protected SpecificCharacterSet utf8() {
        return SpecificCharacterSet.valueOf(new String[] { "ISO_IR 192" });
    }

    protected SpecificCharacterSet gb18030() {
        return SpecificCharacterSet.valueOf(new String[] { "GB18030" });
    }
}
