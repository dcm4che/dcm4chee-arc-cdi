package org.dcm4chee.archive.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Created by APUNM on 11.12.2015.
 */
@Path("/{AETitle}")
public interface IStowRS {
    @POST
    @Path("/studies/{StudyInstanceUID}")
    @Consumes("multipart/related")
    Response storeInstances(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            InputStream in) throws Exception;

    @POST
    @Path("/studies")
    @Consumes({"multipart/related","multipart/form-data"})
    Response storeInstances(InputStream in) throws Exception;
}
