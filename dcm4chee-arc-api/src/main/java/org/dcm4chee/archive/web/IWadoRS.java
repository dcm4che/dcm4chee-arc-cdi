package org.dcm4chee.archive.web;

import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Created by APUNM on 11.12.2015.
 */
@Path("/{AETitle}")
public interface IWadoRS {
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
