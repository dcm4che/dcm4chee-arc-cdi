package org.dcm4chee.archive.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Created by APUNM on 11.12.2015.
 */
@Path("/{AETitle}")
public interface IQidoRS {

    @GET
    @Path("/studies")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForStudiesXML() throws Exception;

    @GET
    @Path("/studies")
    @Produces("application/json")
    Response searchForStudiesJSON() throws Exception;

    @GET
    @Path("/series")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForSeriesXML() throws Exception;

    @GET
    @Path("/series")
    @Produces("application/json")
    Response searchForSeriesJSON() throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/series")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForSeriesOfStudyXML(
            @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/series")
    @Produces("application/json")
    Response searchForSeriesOfStudyJSON(
            @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception;

    @GET
    @Path("/instances")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForInstancesXML() throws Exception;

    @GET
    @Path("/instances")
    @Produces("application/json")
    Response searchForInstancesJSON() throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/instances")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForInstancesOfStudyXML(
            @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/instances")
    @Produces("application/json")
    Response searchForInstancesOfStudyJSON(
            @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForInstancesOfSeriesXML(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances")
    @Produces("application/json")
    Response searchForInstancesOfSeriesJSON(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) throws Exception;
}
