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
import java.util.ArrayList;
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

import org.dcm4che3.io.SAXReader;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONReader.Callback;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Tag;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtBean;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtEJB;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtEJB.PatientCommands;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtEJB.SeriesCommands;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtEJB.StudyCommands;
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

    @Inject
    Device device;

    @Inject
    DataMgmtBean dataManager;
    
    // Patient Level
    // update
    @POST
    @Path("updatexml/patients/")
    @Consumes({ "application/xml" })
    public Response updateXMLPatient(@Context UriInfo uriInfo, InputStream in) throws Exception {
        return updateXML("PATIENT", in);
    }

    @POST
    @Path("updatejson/patients")
    @Consumes({ "application/json" })
    public Response updateJSONPatient(@Context UriInfo uriInfo, InputStream in) throws Exception {
        return updateJSON("PATIENT", in);
    }

    @POST
    @Path("{AETitle}/{PatientOperation}/patients")
    public Response patientOperation(@Context UriInfo uriInfo, InputStream in,
            @PathParam("AETitle") String aeTitle,
            @PathParam("PatientOperation") String patientOperation) {
        PatientCommands command = patientOperation.equalsIgnoreCase("merge")? PatientCommands.PATIENT_MERGE
                : patientOperation.equalsIgnoreCase("link")? PatientCommands.PATIENT_LINK
                        : patientOperation.equalsIgnoreCase("unlink")? PatientCommands.PATIENT_UNLINK
                                : patientOperation.equalsIgnoreCase("updateids")? PatientCommands.PATIENT_UPDATE_ID
                                : null;
        
        if (command == null)
            throw new WebApplicationException(
                    "Unable to decide patient command - supported commands {merge, link, unlink}");
        ArrayList<HashMap<String, String>> query = new ArrayList<HashMap<String, String>>();
        ArrayList<Attributes> attrs = null;
        try {
            attrs = parseJSONAttributesToList(in, query);

            if (query.size()%2!=0)
                throw new WebApplicationException(new Exception(
                        "Unable to decide request data"), Response.Status.BAD_REQUEST);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received Attributes for patient operation - "+patientOperation);
                for(int i=0; i< query.size();i++){
                    LOG.debug(i%2==0 ? "Source data[{}]: ":"Target data[{}]: ",(i/2)+1);
                for (String key : query.get(i).keySet()) {
                    LOG.debug(key + "=" + query.get(i).get(key));
                }
                }
            }
        }
        catch(Exception e)
        {
            throw new WebApplicationException(
                    "Unable to process patient operation request data");
        }

        return aggregatePatientOpResponse(attrs,device.getApplicationEntity(aeTitle),command, patientOperation);
    }
    
    private Response aggregatePatientOpResponse(ArrayList<Attributes> attrs,
            ApplicationEntity applicationEntity, PatientCommands command, String patientOperation) {
        ArrayList<Boolean> listRSP = new ArrayList<Boolean>();
        for(int i=0;i<attrs.size();i++)
        listRSP.add(dataManager.patientOperation(attrs.get(i), attrs.get(++i),
                applicationEntity, command));
        int trueCount=0;
        
        for(boolean rsp: listRSP)
        if(rsp)
        trueCount++;
        return trueCount==listRSP.size()?
                Response.status(Status.OK).entity
                ("Patient operation successful - "+patientOperation).build():
                trueCount==0?Response.status(Status.CONFLICT).entity
                ("Error - Unable to perform patient operation - "+patientOperation).build():
                Response.status(Status.CONFLICT).entity
                ("Warning - Unable to perform some operations - "+patientOperation).build();
    }

    // Study Level
    
    @POST
    @Path("updatexml/studies/{StudyInstanceUID}")
    @Consumes({ "application/xml" })
    public Response updateXMLStudies(@Context UriInfo uriInfo, InputStream in)
            throws Exception {
        return updateXML("STUDY", in);
    }

    @POST
    @Path("updateJSON/studies/")
    @Consumes({ "application/json" })
    public Response updateJSONStudies(@Context UriInfo uriInfo, InputStream in)
            throws Exception {
        return updateJSON("STUDY", in);
    }

    @POST
    @Path("{StudyOperation}/studies")
    public Response studyOperation(@Context UriInfo uriInfo, InputStream in,
            @PathParam("StudyOperation") String studyOperation) {
        StudyCommands command = studyOperation.equalsIgnoreCase("move")? StudyCommands.STUDY_MOVE
                : studyOperation.equalsIgnoreCase("split")? StudyCommands.STUDY_SPLIT
                        : studyOperation.equalsIgnoreCase("segment")? StudyCommands.STUDY_SEGMENT: null;

        if (command == null)
            throw new WebApplicationException(
                    "Unable to decide study command - supported commands {move, split, segment}");
        ArrayList<HashMap<String, String>> query = new ArrayList<HashMap<String, String>>();
        ArrayList<Attributes> attrs = null;
        try {
            attrs = parseJSONAttributesToList(in, query);

            if (query.size()%2!=0)
                throw new WebApplicationException(new Exception(
                        "Unable to decide request data"), Response.Status.BAD_REQUEST);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received Attributes for study operation - "+studyOperation);
                for(int i=0; i< query.size();i++){
                    LOG.debug(i%2==0 ? "Source data[{}]: ":"Target data[{}]: ",(i/2)+1);
                for (String key : query.get(i).keySet()) {
                    LOG.debug(key + "=" + query.get(i).get(key));
                }
                }
            }
        }
        catch(Exception e)
        {
            throw new WebApplicationException(
                    "Unable to process study operation request data");
        }
        return aggregateStudyOpResponse(attrs, command, studyOperation);
    }
    private Response aggregateStudyOpResponse(ArrayList<Attributes> attrs,
            StudyCommands command, String studyOperation) {
        ArrayList<Boolean> listRSP = new ArrayList<Boolean>();
        for(int i=0;i<attrs.size();i++)
        listRSP.add(dataManager.studyOperation(attrs.get(i), attrs.get(++i),command));
        int trueCount=0;
        
        for(boolean rsp: listRSP)
        if(rsp)
        trueCount++;
        return trueCount==listRSP.size()?
                Response.status(Status.OK).entity
                ("Study operation successful - "+studyOperation).build():
                trueCount==0?Response.status(Status.CONFLICT).entity
                ("Error - Unable to perform study operation - "+studyOperation).build():
                Response.status(Status.CONFLICT).entity
                ("Warning - Unable to perform some operations - "+studyOperation).build();
    }
    
    // Series Level
    @POST
    @Path("updatexml/series")
    @Consumes({ "application/xml" })
    public Response updateXMLSeries(@Context UriInfo uriInfo, InputStream in)
            throws Exception {
        return updateXML("SERIES", in);
    }

    @POST
    @Path("updatejson/series")
    @Consumes({ "application/json" })
    public Response updateJSONSeries(@Context UriInfo uriInfo, InputStream in)
            throws Exception {
        return updateJSON("SERIES", in);
    }

    @GET
    @Path("{SeriesOperation}/series")
    public Response splitSeries(@Context UriInfo uriInfo, InputStream in,
            @PathParam("SeriesOperation") String seriesOperation) {
        SeriesCommands command = seriesOperation.equalsIgnoreCase("split")? SeriesCommands.SERIES_SPLIT: null;

        if (command == null)
            throw new WebApplicationException(
                    "Unable to decide series command - supported commands {split}");
        ArrayList<HashMap<String, String>> query = new ArrayList<HashMap<String, String>>();
        ArrayList<Attributes> attrs = null;
        try {
            attrs = parseJSONAttributesToList(in, query);

            if (query.size()%2!=0)
                throw new WebApplicationException(new Exception(
                        "Unable to decide request data"), Response.Status.BAD_REQUEST);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received Attributes for series operation - "+seriesOperation);
                for(int i=0; i< query.size();i++){
                    LOG.debug(i%2==0 ? "Source data[{}]: ":"Target data[{}]: ",(i/2)+1);
                for (String key : query.get(i).keySet()) {
                    LOG.debug(key + "=" + query.get(i).get(key));
                }
                }
            }
        }
        catch(Exception e)
        {
            throw new WebApplicationException(
                    "Unable to process series operation request data");
        }
        return aggregateSeriesOpResponse(attrs, command, seriesOperation);
    }

    private Response aggregateSeriesOpResponse(ArrayList<Attributes> attrs,
            SeriesCommands command, String seriesOperation) {
        ArrayList<Boolean> listRSP = new ArrayList<Boolean>();
        for(int i=0;i<attrs.size();i++)
        listRSP.add(dataManager.seriesOperation(attrs.get(i), attrs.get(++i),command));
        int trueCount=0;
        
        for(boolean rsp: listRSP)
        if(rsp)
        trueCount++;
        return trueCount==listRSP.size()?
                Response.status(Status.OK).entity
                ("Series operation successful - "+seriesOperation).build():
                trueCount==0?Response.status(Status.CONFLICT).entity
                ("Error - Unable to perform series operation - "+seriesOperation).build():
                Response.status(Status.CONFLICT).entity
                ("Warning - Unable to perform some operations - "+seriesOperation).build();
    }

    // Instance Level
    @POST
    @Path("updatexml/instances")
    @Consumes({ "application/xml" })
    public Response updateXMLInstances(@Context UriInfo uriInfo, InputStream in)
            throws Exception {
        return updateXML("IMAGE", in);
    }

    @POST
    @Path("updatejson/instances")
    @Consumes({ "application/json" })
    public Response updateJSONInstances(@Context UriInfo uriInfo, InputStream in)
            throws Exception {
        return updateJSON("IMAGE", in);
    }

    private Response updateXML(String level, InputStream in) throws Exception {
        HashMap<String, String> query = new HashMap<String, String>();
        try {
            Attributes attrs = parseXMLAttributes(in, query);
            LOG.info("Received XML request for DICOM Header Object Update");

            if (query.size() == 0 || level == null)
                throw new WebApplicationException("Unable to decide level");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received Attributes to update");
                for (String key : query.keySet()) {
                    LOG.debug(key + "=" + query.get(key));
                }
            }
            LOG.info("Performing Update on Level = " + level);
            String studyInstanceUID = attrs.getString(Tag.StudyInstanceUID);
            String seriesInstanceUID = attrs.getString(Tag.SeriesInstanceUID);
            String sopInstanceUID = attrs.getString(Tag.SOPInstanceUID);
            IDWithIssuer id = IDWithIssuer.pidOf(attrs);
            switch (level) {
            case "PATIENT":
                if(id == null)
                    throw new WebApplicationException("No patient id specified in the request data");
                LOG.info("Updating patient with uid=" + id);
                updatePatient(attrs, id);
                break;
            case "STUDY":
                if(studyInstanceUID == null)
                    throw new WebApplicationException("No study UID specified in the request data");
                LOG.info("Updating study with uid=" + studyInstanceUID);
                updateStudy(attrs, studyInstanceUID);
                break;
            case "SERIES":
                if(studyInstanceUID == null || seriesInstanceUID == null)
                    throw new WebApplicationException("Either study or series UID was not specified in the request data");
                LOG.info("Updating series with uid=" + seriesInstanceUID);
                updateSeries(attrs, studyInstanceUID, seriesInstanceUID);
                break;
            case "IMAGE":
                if(studyInstanceUID == null || seriesInstanceUID == null || sopInstanceUID == null)
                    throw new WebApplicationException("Either study or series UID was not specified in the request data");
                LOG.info("Updating image with uid=" + sopInstanceUID);
                updateInstance(attrs, studyInstanceUID, seriesInstanceUID,
                        sopInstanceUID);
                break;
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return Response.status(Status.OK).entity("Successfully Processed Update").build();
    }

    private Response updateJSON(String level, InputStream in) throws Exception {
        HashMap<String, String> query = new HashMap<String, String>();
        try {
            Attributes attrs = parseJSONAttributes(in, query);
            LOG.info("Received JSON request for DICOM Header Object Update");
            if (query.size() == 0 || level == null)
                throw new WebApplicationException(new Exception(
                        "Unable to decide level"), Response.Status.BAD_REQUEST);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received Attributes to update");
                for (String key : query.keySet()) {
                    LOG.debug(key + "=" + query.get(key));
                }
            }
            LOG.info("Performing Update on Level = " + level);
            String studyInstanceUID = attrs.getString(Tag.StudyInstanceUID);
            String seriesInstanceUID = attrs.getString(Tag.SeriesInstanceUID);
            String sopInstanceUID = attrs.getString(Tag.SOPInstanceUID);
            IDWithIssuer id = IDWithIssuer.pidOf(attrs);
            switch (level) {
            case "PATIENT":
                if(id == null)
                    throw new WebApplicationException("No patient id specified in the request data");
                LOG.info("Updating patient with uid=" + id);
                updatePatient(attrs, id);
                break;
            case "STUDY":
                if(studyInstanceUID == null)
                    throw new WebApplicationException("No study UID specified in the request data");
                LOG.info("Updating study with uid=" + studyInstanceUID);
                updateStudy(attrs, studyInstanceUID);
                break;
            case "SERIES":
                if(studyInstanceUID == null || seriesInstanceUID == null)
                    throw new WebApplicationException("Either study or series UID was not specified in the request data");
                LOG.info("Updating series with uid=" + seriesInstanceUID);
                updateSeries(attrs, studyInstanceUID, seriesInstanceUID);
                break;
            case "IMAGE":
                if(studyInstanceUID == null || seriesInstanceUID == null || sopInstanceUID == null)
                    throw new WebApplicationException("Either study or series UID was not specified in the request data");
                LOG.info("Updating image with uid=" + sopInstanceUID);
                updateInstance(attrs, studyInstanceUID, seriesInstanceUID,
                        sopInstanceUID);
                break;
            }

        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return Response.status(Status.OK).entity("Successfully Processed Update").build();
    }

    private void updateInstance(Attributes attrs, String studyInstanceUID,
            String seriesInstanceUID, String sopInstanceUID) {
        ArchiveDeviceExtension arcDevExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        dataManager.updateInstance(arcDevExt, studyInstanceUID,
                seriesInstanceUID, sopInstanceUID, attrs);
    }

    private void updateSeries(Attributes attrs, String studyInstanceUID,
            String seriesInstanceUID) throws InstanceNotFoundException {
        ArchiveDeviceExtension arcDevExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        dataManager.updateSeries(arcDevExt, studyInstanceUID,
                seriesInstanceUID, attrs);
    }

    private void updateStudy(Attributes attrs, String studyInstanceUID)
            throws InstanceNotFoundException {
        ArchiveDeviceExtension arcDevExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        dataManager.updateStudy(arcDevExt, studyInstanceUID, attrs);

    }

    private void updatePatient(Attributes attrs, IDWithIssuer id) {
        ArchiveDeviceExtension arcDevExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        dataManager.updatePatient(arcDevExt, id, attrs);

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
    
    private ArrayList<Attributes> parseJSONAttributesToList(InputStream in,
            ArrayList<HashMap<String, String>> query) throws IOException {

        JSONReader reader = new JSONReader(
                Json.createParser(new InputStreamReader(in, "UTF-8")));
        final ArrayList<Attributes> attributesList = new ArrayList<Attributes>();
        
        reader.readDatasets(new Callback() {
            
            @Override
            public void onDataset(Attributes fmi, Attributes dataset) {
                attributesList.add(dataset);
            }
        });
        ElementDictionary dict = ElementDictionary
                .getStandardElementDictionary();

        for (int i = 0; i < attributesList.size(); i++){
            HashMap<String, String> tmpQMap = new HashMap<String, String>(); 
        for (int j = 0; j < attributesList.get(i).tags().length; j++) {
            Attributes ds = attributesList.get(i);
            if (TagUtils.isPrivateTag(ds.tags()[j])) {
                dict = ElementDictionary.getElementDictionary(ds
                        .getPrivateCreator(ds.tags()[j]));
                query.get(i).put(dict.keywordOf(ds.tags()[j]),
                        ds.getString(ds.tags()[j]));
            } else {
                dict = ElementDictionary.getStandardElementDictionary();
                tmpQMap.put(dict.keywordOf(ds.tags()[j]),
                        ds.getString(ds.tags()[j]));
            }
        }
        query.add(tmpQMap);
        }

        return attributesList;
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



}