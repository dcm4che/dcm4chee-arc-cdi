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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4chee.archive.datamgmt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceException;

import org.dcm4che3.io.SAXReader;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtBean;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtEJB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
@Path("/manage")
@RequestScoped
public class DataMgmt {

    private static final Logger LOG = LoggerFactory
            .getLogger(DataMgmtEJB.class);
    @Context
    private HttpServletRequest request;

    private static String RSP;

    @Inject
    DataMgmtBean dataManager;

    //Patient Level
    @POST
    @Path("updateXML/patients/{PatientID}")
    @Consumes({ "application/xml" })
    public Response updateXMLPatient(@Context UriInfo uriInfo, InputStream in,
            @PathParam("PatientID") String patientID)
            throws Exception {
        return updateXML("PATIENT",patientID, null, null, null, in);
    }

    @POST
    @Path("updateJSON/patients/{PatientID}")
    @Consumes({ "application/json" })
    public Response updateJSONPatient(@Context UriInfo uriInfo, InputStream in,
            @PathParam("PatientID") String patientID)
            throws Exception {
        return updateJSON("PATIENT", patientID, null, null, null, in);
    }
    
    //Study Level
    @POST
    @Path("updateXML/studies/{StudyInstanceUID}")
    @Consumes({ "application/xml" })
    public Response updateXMLStudies(@Context UriInfo uriInfo, InputStream in,
            @PathParam("StudyInstanceUID") String studyInstanceUID)
            throws Exception {
        return updateXML("STUDY",null, studyInstanceUID, null, null, in);
    }

    @POST
    @Path("updateJSON/studies/{StudyInstanceUID}")
    @Consumes({ "application/json" })
    public Response updateJSONStudies(@Context UriInfo uriInfo, InputStream in,
            @PathParam("StudyInstanceUID") String studyInstanceUID)
            throws Exception {
        return updateJSON("STUDY", null, studyInstanceUID, null, null, in);
    }

    //Series Level
    @POST
    @Path("updateXML/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    @Consumes({ "application/xml" })
    public Response updateXMLSeries(@Context UriInfo uriInfo, InputStream in,
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID)
            throws Exception {
        return updateXML("SERIES",null, studyInstanceUID, seriesInstanceUID, null, in);
    }

    @POST
    @Path("updateJSON/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    @Consumes({ "application/json" })
    public Response updateJSONSeries(@Context UriInfo uriInfo, InputStream in,
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID)
            throws Exception {
        return updateJSON("SERIES",null, studyInstanceUID, seriesInstanceUID, null, in);
    }

    //Instance Level
    @POST
    @Path("updateXML/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}")
    @Consumes({ "application/xml" })
    public Response updateXMLInstances(@Context UriInfo uriInfo, InputStream in,
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID)
            throws Exception {
        return updateXML("IMAGE",null, studyInstanceUID, seriesInstanceUID, sopInstanceUID, in);
    }

    @POST
    @Path("updateJSON/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}")
    @Consumes({ "application/json" })
    public Response updateJSONInstances(@Context UriInfo uriInfo, InputStream in,
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID)
            throws Exception {
        return updateJSON("IMAGE",null, studyInstanceUID, seriesInstanceUID, sopInstanceUID, in);
    }
    
    public Response updateXML(String level,String patientID, 
            String studyInstanceUID, 
            String seriesInstanceUID,
            String sopInstanceUID, InputStream in)
            throws Exception {
        HashMap<String, String> query = new HashMap<String, String>();
        try {
            parseXMLAttributes(in, query);
            LOG.info("Received XML request for DICOM Header Object Update");

            if (query.size() == 0 || level == null)
                throw new WebApplicationException(
                        "Unable to decide level");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received Attributes to update");
                for (String key : query.keySet()) {
                    LOG.debug(key + "=" + query.get(key));
                }
            }
            LOG.info("Performing Update on Level = " + level);
            switch(level)
            {
            case "PATIENT": LOG.info("Updating patient with uid=" + patientID);break;
            case "STUDY": LOG.info("Updating study with uid=" + studyInstanceUID);break;
            case "SERIES": LOG.info("Updating series with uid=" + seriesInstanceUID);break;
            case "IMAGE": LOG.info("Updating image with uid=" + sopInstanceUID);break;
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return Respond();
    }

    public Response updateJSON(String level,String patientID, 
            String studyInstanceUID, 
            String seriesInstanceUID,
            String sopInstanceUID, InputStream in)
            throws Exception {
        HashMap<String, String> query = new HashMap<String, String>();
        try {
            parseJSONAttributes(in, query);
            LOG.info("Received JSON request for DICOM Header Object Update");
            if (query.size() == 0 || level == null)
                throw new WebApplicationException(
                        new Exception("Unable to decide level"),
                        Response.Status.BAD_REQUEST);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received Attributes to update");
                for (String key : query.keySet()) {
                    LOG.debug(key + "=" + query.get(key));
                }
            }
            LOG.info("Performing Update on Level = " + level);
            switch(level)
            {
            case "PATIENT": LOG.info("Updating patient with uid=" + patientID);break;
            case "STUDY": LOG.info("Updating study with uid=" + studyInstanceUID);break;
            case "SERIES": LOG.info("Updating series with uid=" + seriesInstanceUID);break;
            case "IMAGE": LOG.info("Updating image with uid=" + sopInstanceUID);break;
            }
            
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return Respond();
    }

    private void parseJSONAttributes(InputStream in,
            HashMap<String, String> query) throws IOException {

        JSONReader reader = new JSONReader(
                Json.createParser(new InputStreamReader(in, "UTF-8")));
        Attributes ds = new Attributes();
        ds = reader.readDataset(ds);
        ElementDictionary dict = ElementDictionary
                .getStandardElementDictionary();
        for (int i = 0; i < ds.tags().length; i++) {

            if (TagUtils.isPrivateTag(ds.tags()[i])) {
                dict = ElementDictionary.getElementDictionary(ds
                        .getPrivateCreator(ds.tags()[i]));
                query.put(dict.keywordOf(ds.tags()[i]),
                        ds.getString(ds.tags()[i]));
            } else {
                dict = ElementDictionary.getStandardElementDictionary();
                query.put(dict.keywordOf(ds.tags()[i]),
                        ds.getString(ds.tags()[i]));
            }
        }

    }

    private void parseXMLAttributes(InputStream in,
            HashMap<String, String> query) throws SAXException,
            ParserConfigurationException, IOException {
        Attributes ds = new Attributes();
        try {
            ds = SAXReader.parse(in, ds);
            ElementDictionary dict = ElementDictionary
                    .getStandardElementDictionary();
            for (int i = 0; i < ds.tags().length; i++) {

                if (TagUtils.isPrivateTag(ds.tags()[i])) {
                    dict = ElementDictionary.getElementDictionary(ds
                            .getPrivateCreator(ds.tags()[i]));
                    query.put(dict.keywordOf(ds.tags()[i]),
                            ds.getString(ds.tags()[i]));
                } else {
                    dict = ElementDictionary.getStandardElementDictionary();
                    query.put(dict.keywordOf(ds.tags()[i]),
                            ds.getString(ds.tags()[i]));
                }

            }
        } catch (SAXException e) {
            throw e;
        } catch (ParserConfigurationException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    private Response Respond() {
        try {
            if (RSP == null) {
                RSP = "Successfully processed request";
                return Response.status(Status.OK).entity(RSP).build();
            } else {
                return Response.status(Status.NOT_MODIFIED).entity(RSP).build();
            }
        } catch (WebServiceException e) {
            throw e;
        }
    }

}