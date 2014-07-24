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
import javax.management.InstanceNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import org.dcm4che3.net.Device;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtBean;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtEJB;
import org.dcm4chee.archive.entity.Issuer;
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
    Device device;

    @Inject
    DataMgmtBean dataManager;

    //Patient Level
    //update
    @POST
    @Path("updateXML/patients/{PatientID}/{issuer:.*}")
    @Consumes({ "application/xml" })
    public Response updateXMLPatient(@Context UriInfo uriInfo, InputStream in,
            @PathParam("PatientID") String patientID,
            @PathParam("issuer") String issuer)
            throws Exception {
        String[] issuerVals = issuer.split("/");
        Issuer matchingIssuer=null;
        if(issuerVals.length>0)
        {
            if(issuerVals.length==1)
            {
                matchingIssuer =  dataManager.getIssuer(issuerVals[0],null,null);
            }
            else if(issuerVals.length==2)
            {
                matchingIssuer =  dataManager.getIssuer(null,issuerVals[0],issuerVals[1]);
            }
            else if(issuerVals.length==3)
            {
                matchingIssuer =  dataManager.getIssuer(issuerVals[0],issuerVals[1],issuerVals[2]);
            }
        }
             
        IDWithIssuer id = new IDWithIssuer(patientID,matchingIssuer);
        return updateXML("PATIENT",id, null, null, null, in);
    }

    @POST
    @Path("updateJSON/patients/{PatientID}/{issuer:.*}")
    @Consumes({ "application/json" })
    public Response updateJSONPatient(@Context UriInfo uriInfo, InputStream in,
            @PathParam("PatientID") String patientID,
            @PathParam("issuer") String issuer)
            throws Exception {
        String[] issuerVals = issuer.split("/");
        Issuer matchingIssuer=null;
        if(issuerVals.length>0)
        {
            if(issuerVals.length==1)
            {
                matchingIssuer =  dataManager.getIssuer(issuerVals[0],null,null);
            }
            else if(issuerVals.length==2)
            {
                matchingIssuer =  dataManager.getIssuer(null,issuerVals[0],issuerVals[1]);
            }
            else if(issuerVals.length==3)
            {
                matchingIssuer =  dataManager.getIssuer(issuerVals[0],issuerVals[1],issuerVals[2]);
            }
        }
             
        IDWithIssuer id = new IDWithIssuer(patientID,matchingIssuer);
        return updateJSON("PATIENT", id, null, null, null, in);
    }
    //move study
    @GET
    @Path("moveStudy/studies/{StudyInstanceUID}/patients/{PatientID}/{issuer:.*}")
    public Response moveStudy(@Context UriInfo uriInfo, InputStream in,
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("PatientID") String patientID,
            @PathParam("issuer") String issuer){
        String[] issuerVals = issuer.split("/");
        Issuer matchingIssuer=null;
        if(issuerVals.length>0)
        {
            if(issuerVals.length==1)
            {
                matchingIssuer =  dataManager.getIssuer(issuerVals[0],null,null);
            }
            else if(issuerVals.length==2)
            {
                matchingIssuer =  dataManager.getIssuer(null,issuerVals[0],issuerVals[1]);
            }
            else if(issuerVals.length==3)
            {
                matchingIssuer =  dataManager.getIssuer(issuerVals[0],issuerVals[1],issuerVals[2]);
            }
        }
             
        IDWithIssuer id = new IDWithIssuer(patientID,matchingIssuer);
        return moveStudy(studyInstanceUID,id);
    }
    private Response moveStudy(String studyInstanceUID, IDWithIssuer id) {
        boolean moved = dataManager.moveStudy(studyInstanceUID,id);
        Response rspMoved = Response.status(Status.OK).entity("Study Moved Successfully").build();
        Response rspError = Response.status(Status.CONFLICT).entity("Error: Study Not Moved").build();
        return moved?rspMoved:rspError;
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

    @GET
    @Path ("splitStudy/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/targetstudies/{TargetStudyInstanceUID}")
    public Response splitStudy(@Context UriInfo uriInfo, InputStream in,
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("TargetStudyInstanceUID") String targetStudyInstanceUID)
            {
        boolean split = dataManager.splitStudy(studyInstanceUID, seriesInstanceUID, targetStudyInstanceUID);
        Response rspSplit = Response.status(Status.OK).entity("Study Split Successfully").build();
        Response rspError = Response.status(Status.CONFLICT).entity("Error: Study Not Split").build();
        return split?rspSplit:rspError;
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
    
    public Response updateXML(String level,IDWithIssuer id, 
            String studyInstanceUID, 
            String seriesInstanceUID,
            String sopInstanceUID, InputStream in)
            throws Exception {
        HashMap<String, String> query = new HashMap<String, String>();
        try {
            Attributes attrs = parseXMLAttributes(in, query);
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
            case "PATIENT": LOG.info("Updating patient with uid=" + id);updatePatient(attrs,id);break;
            case "STUDY": LOG.info("Updating study with uid=" + studyInstanceUID);updateStudy(attrs,studyInstanceUID);break;
            case "SERIES": LOG.info("Updating series with uid=" + seriesInstanceUID);updateSeries(attrs,studyInstanceUID,seriesInstanceUID);break;
            case "IMAGE": LOG.info("Updating image with uid=" + sopInstanceUID);updateInstance(attrs,studyInstanceUID,seriesInstanceUID,sopInstanceUID);break;
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return Respond();
    }

    public Response updateJSON(String level,IDWithIssuer id, 
            String studyInstanceUID, 
            String seriesInstanceUID,
            String sopInstanceUID, InputStream in)
            throws Exception {
        HashMap<String, String> query = new HashMap<String, String>();
        try {
            Attributes attrs = parseJSONAttributes(in, query);
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
            case "PATIENT": LOG.info("Updating patient with uid=" + id);updatePatient(attrs,id);break;
            case "STUDY": LOG.info("Updating study with uid=" + studyInstanceUID);updateStudy(attrs,studyInstanceUID);break;
            case "SERIES": LOG.info("Updating series with uid=" + seriesInstanceUID);updateSeries(attrs,studyInstanceUID,seriesInstanceUID);break;
            case "IMAGE": LOG.info("Updating image with uid=" + sopInstanceUID);updateInstance(attrs,studyInstanceUID,seriesInstanceUID,sopInstanceUID);break;
            }
            
            
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return Respond();
    }

    private void updateInstance(Attributes attrs,
            String studyInstanceUID, String seriesInstanceUID,
            String sopInstanceUID) {
        ArchiveDeviceExtension arcDevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        dataManager.updateInstance(arcDevExt,studyInstanceUID,seriesInstanceUID, sopInstanceUID, attrs);
    }

    private void updateSeries(Attributes attrs,
            String studyInstanceUID, String seriesInstanceUID) throws InstanceNotFoundException {
        ArchiveDeviceExtension arcDevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        dataManager.updateSeries(arcDevExt,studyInstanceUID,seriesInstanceUID, attrs);
    }

    private void updateStudy(Attributes attrs,
            String studyInstanceUID) throws InstanceNotFoundException {
        ArchiveDeviceExtension arcDevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        dataManager.updateStudy(arcDevExt,studyInstanceUID, attrs);
        
    }

    private void updatePatient(Attributes attrs, IDWithIssuer id) {
        ArchiveDeviceExtension arcDevExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
        dataManager.updatePatient(arcDevExt,id, attrs);
        
    }

    private Attributes parseJSONAttributes(InputStream in,
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
        return ds;
    }

    private Attributes parseXMLAttributes(InputStream in,
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
        return ds;
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