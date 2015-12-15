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

package org.dcm4chee.archive.web;

import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Service implementing DICOM Supplement 161: WADO by RESTful Services
 * (WADO-RS).
 *
 * @author Alessio Roselli <alessio.roselli@agfa.com>
 */
@Path("/{AETitle}")
public interface WadoRS {
    @GET
    @Path("/studies/{StudyInstanceUID}")
    Response retrieveStudy(
            @PathParam("StudyInstanceUID") String studyInstanceUID)
            throws DicomServiceException;

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    Response retrieveSeries(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID)
            throws DicomServiceException;

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}")
    Response retrieveInstance(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID)
            throws DicomServiceException;

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}/frames/{FrameList}")
    @Produces("multipart/related")
    Response retrieveFrame(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID,
            @PathParam("FrameList") FrameList frameList);

    /**
     * BulkDataURI is expected to be a path to a file.
     */
    @GET
    @Path("/bulkdata/{BulkDataPath:.*}")
    @Produces("multipart/related")
    Response retrieveBulkdata(
            @QueryParam("storageSystemGroup") String storageSystemGroupName,
            @QueryParam("storageSystem") String storageSystemName,
            @PathParam("BulkDataPath") String bulkDataPath,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("length") @DefaultValue("-1") int length);

    @GET
    @Path("/studies/{StudyInstanceUID}/metadata")
    Response retrieveStudyMetadata(
            @PathParam("StudyInstanceUID") String studyInstanceUID)
            throws DicomServiceException;

    // create metadata retrieval for Series
    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/metadata")
    Response retrieveSeriesMetadata(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID)
            throws DicomServiceException;

    // create metadata retrieval for Instances
    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}/metadata")
    Response retrieveInstanceMetadata(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID)
            throws DicomServiceException;

    final class FrameList {
        private final int[] frames;

        public FrameList(String s) {
            String[] ss = StringUtils.split(s, ',');
            int[] values = new int[ss.length];
            for (int i = 0; i < ss.length; i++) {
                try {
                    if ((values[i] = Integer.parseInt(ss[i])) <= 0)
                        throw new WebApplicationException(Response.Status.BAD_REQUEST);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(s);
                }
            }
            this.frames = values;
        }

        public int[] getFrames() {
            return frames;
        }
    }
}
