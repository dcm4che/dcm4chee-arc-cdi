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

package org.dcm4chee.task;

/**
 * Task types related to image processing (Compression, Decompression, Transcoding).
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public enum ImageProcessingTaskTypes implements TaskType {

    /**
     * Tasks that perform transcoding (typically compression) on incoming images.
     * <p>
     * E.g. if the archive acts as an Store SCP and compresses the incoming images.
     */
    TRANSCODE_INCOMING,

    /**
     * Tasks that decompress and/or re-compress (i.e. transcode) outgoing images.
     * <p>
     * Examples:
     * <ul>
     * <li>The archive acts as an Store SCU and has to send out decompressed images.</li>
     * <li>The archive handles WADO-RS/WADO-URI requests and has to decompress and/or re-compress instances (e.g.
     * instance stored as JPEG-LS and requested as rendered PNG).</li>
     * </ul>
     */
    TRANSCODE_OUTGOING

}
