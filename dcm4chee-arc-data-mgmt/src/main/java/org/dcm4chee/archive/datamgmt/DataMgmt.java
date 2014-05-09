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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.stream.JsonParser;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceException;

import org.dcm4che3.io.SAXReader;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.ws.rs.MediaTypes;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.datamgmt.ejb.DataMgmtEJB;
import org.dcm4chee.archive.rs.HttpSource;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
@Path("/datamgmt")
public class DataMgmt {

    private static final Logger log = LoggerFactory
            .getLogger(DataMgmtEJB.class);
    @Context
    private HttpServletRequest request;

    private String RSP;

    @Inject
    StoreService storeService;

    @Inject
    DataMgmtEJB dataManager;

    @Inject
    IApplicationEntityCache aeCache;

    private static final String archAETitle = "DCM4CHEE";

    @HeaderParam("Content-Type")
    private MediaType contentType;
    private String boundary;
    
    @POST
    @Path("updateDICOM")
    @Consumes({ "application/xml", "multipart/form-data", "application/json",
            "application/octet-stream", "application/dicom",
            "application/dicom+xml" })
    public Response updateDICOM(@Context UriInfo uriInfo, InputStream in)
            throws Exception {
        log.info("------------" + request.getHeader("Content-Type"));
        log.info("########" + contentType.getType() + "###########" + "sub####"
                + contentType.getSubtype());
        MultivaluedMap<String, String> queryParams = uriInfo
                .getQueryParameters();
        MultivaluedMap<String, String> query = null;
        String type = queryParams.getFirst("Type");
        if (type == null) {
            try{
                        MediaType rootBodyMediaType = MediaType
                                .valueOf(contentType.getType()+"/"+contentType.getSubtype());
                        if (rootBodyMediaType
                                .isCompatible(MediaTypes.APPLICATION_DICOM_TYPE)
                                || rootBodyMediaType
                                        .isCompatible(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
                            // TODO binary
                            log.info("binary type");
                        } else if (rootBodyMediaType
                                .isCompatible(MediaType.APPLICATION_XML_TYPE)
                                || rootBodyMediaType
                                        .isCompatible(MediaTypes.APPLICATION_DICOM_XML_TYPE)) {
                            query = queryParams;
                            parseXMLAttributes(in, query);
                            log.info("xml type");
                        } else if (rootBodyMediaType
                                .isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                            // TODO xml
                            log.info("json type");
                            BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
                            log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                            String x="";
                            while(rdr.ready())
                            {
                                x+=rdr.readLine();
                            }
                            log.info(x);
                            log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                            JsonParser parser = Json.createParser(new StringReader(x));
                            while (parser.hasNext()) {
                               JsonParser.Event event = parser.next();
                               switch(event) {
                                  case START_ARRAY:
                                  case END_ARRAY:
                                  case START_OBJECT:
                                  case END_OBJECT:
                                  case VALUE_FALSE:
                                  case VALUE_NULL:
                                  case VALUE_TRUE:
                                      log.info(event.toString());
                                     break;
                                  case KEY_NAME:
                                     log.info(event.toString() + " " +
                                                      parser.getString() + " - ");
                                     break;
                                  case VALUE_STRING:
                                  case VALUE_NUMBER:
                                      log.info(event.toString() + " " +
                                                        parser.getString());
                                     break;
                               }
                            }
                        } else
                            throw new WebApplicationException(
                                    Response.Status.UNSUPPORTED_MEDIA_TYPE);
            } catch (IllegalArgumentException e) {
                throw new WebApplicationException(e,
                        Response.Status.BAD_REQUEST);
            }
            
           // processParams(query);
        } else {
            try {
                if (type.compareTo("DICOM") == 0) {
                    log.info("binary type");
                } else if (type.compareTo("XML") == 0) {
                    log.info("xml type");
                    query = queryParams;
                    parseXMLAttributes(in, query);
                } else if (type.compareTo("JSON") == 0) {
                    log.info("json type");
                } else {
                    log.info("form type");
                    query = queryParams;
                }
                processParams(query);
            } catch (IllegalArgumentException e) {
                throw new WebApplicationException(e,
                        Response.Status.BAD_REQUEST);
            }
        }
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

    private void parseXMLAttributes(InputStream in,
            MultivaluedMap<String, String> query) throws SAXException,
            ParserConfigurationException, IOException {
        Attributes ds;
        File tmpspoolFile = new File("tmpspoolxml-DataMgmt");
        try {
            try {
                BufferedReader rdr = new BufferedReader(new InputStreamReader(
                        in));
                String line;
                String tmpxml = null;
                while (!(line = rdr.readLine()).startsWith("<?xml"))
                    ;
                tmpxml = line;
                while (!(line = rdr.readLine())
                        .startsWith("</NativeDicomModel")) {
                    tmpxml += line;
                }
                tmpxml += line;
                byte[] bytes = tmpxml.getBytes();
                // constructs upload file path
                FileOutputStream fwrt = new FileOutputStream(tmpspoolFile);
                fwrt.write(bytes);
                fwrt.flush();
                fwrt.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ds = SAXReader.parse(tmpspoolFile.getAbsolutePath());
            log.info("XML content \n" + ds.toString());
            ElementDictionary dict = ElementDictionary
                    .getStandardElementDictionary();
            for (int i = 0; i < ds.tags().length; i++) {
                query.add(dict.keywordOf(ds.tags()[i]),
                        ds.getString(ds.tags()[i]));
            }
        } catch (SAXException e) {
            throw e;
        } catch (ParserConfigurationException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            tmpspoolFile.delete();
        }
    }

    private void processParams(MultivaluedMap<String, String> query)
            throws Exception {
        // TODO Auto-generated method stub
        if (query.containsKey("PatientID")) {
            try {
                if (!updatePatient(query))
                    RSP = "No Matching Patient Found";
            } catch (Exception e) {
                RSP = "Unable to process request\nException at update patient: \n"
                        + e.getStackTrace();
                throw e;
            }
        } else if (query.containsKey("StudyInstanceUID")) {
            try {
                if (!updateStudy(query))
                    RSP = "No Matching Study Found";
            } catch (Exception e) {
                RSP = "Unable to process request\nException at update study: \n"
                        + e.getStackTrace();
                throw e;
            }
        } else if (query.containsKey("SeriesInstanceUID")) {
            try {
                if (!updateSeries(query))
                    RSP = "No Matching Series Found";
            } catch (Exception e) {
                RSP = "Unable to process request\nException at update series: \n"
                        + e.getStackTrace();
                throw e;
            }
        } else if (query.containsKey("SOPInstanceUID")) {
            log.info("SOP");
            try {
                if (!updateInstance(query))
                    RSP = "No Matching Instance Found";
            } catch (Exception e) {
                RSP = "Unable to process request\nException at update instance: \n"
                        + e.getStackTrace();
                throw e;
            }
        }

    }

    private boolean updatePatient(MultivaluedMap<String, String> queryParams)
            throws DicomServiceException, ConfigurationException {
        String patientID = queryParams.getFirst("PatientID");
        // create a session
        StoreSession session;
        try {
            session = storeService.createStoreSession(storeService);
        } catch (DicomServiceException e) {
            throw e;
        }
        // get archive extension for store service attributes filter purposes
        ArchiveAEExtension arcAE;
        try {
            arcAE = aeCache.get(archAETitle).getAEExtension(
                    ArchiveAEExtension.class);
        } catch (ConfigurationException e) {
            throw e;
        }
        // sourceAE is the HttpSource
        session.setSource(new HttpSource(request));
        // remoteAETitle is the hostname of the request issuer
        session.setRemoteAET(request.getRemoteHost());
        session.setArchiveAEExtension(arcAE);
        return dataManager.updatePatient(patientID, session, storeService,
                queryParams);
    }

    private boolean updateInstance(MultivaluedMap<String, String> queryParams)
            throws DicomServiceException, ConfigurationException {
        String sopInstanceUID = queryParams.getFirst("SOPInstanceUID");
        // create a session
        StoreSession session;
        try {
            session = storeService.createStoreSession(storeService);
        } catch (DicomServiceException e) {
            throw e;
        }
        // get archive extension for store service attributes filter purposes
        ArchiveAEExtension arcAE;
        try {
            arcAE = aeCache.get(archAETitle).getAEExtension(
                    ArchiveAEExtension.class);
        } catch (ConfigurationException e) {
            throw e;
        }
        // sourceAE is the HttpSource
        session.setSource(new HttpSource(request));
        // remoteAETitle is the hostname of the request issuer
        session.setRemoteAET(request.getRemoteHost());
        session.setArchiveAEExtension(arcAE);
        return dataManager.updateInstances(sopInstanceUID, session,
                storeService, queryParams);
    }

    private boolean updateSeries(MultivaluedMap<String, String> queryParams)
            throws DicomServiceException, ConfigurationException {
        String seriesInstanceUID = queryParams.getFirst("SeriesInstanceUID");
        // create a session
        StoreSession session;
        try {
            session = storeService.createStoreSession(storeService);
        } catch (DicomServiceException e) {
            throw e;
        }
        // get archive extension for store service attributes filter purposes
        ArchiveAEExtension arcAE;
        try {
            arcAE = aeCache.get(archAETitle).getAEExtension(
                    ArchiveAEExtension.class);
        } catch (ConfigurationException e) {
            throw e;
        }
        // sourceAE is the HttpSource
        session.setSource(new HttpSource(request));
        // remoteAETitle is the hostname of the request issuer
        session.setRemoteAET(request.getRemoteHost());
        session.setArchiveAEExtension(arcAE);
        return dataManager.updateSeries(seriesInstanceUID, session,
                storeService, queryParams);
    }

    private boolean updateStudy(MultivaluedMap<String, String> queryParams)
            throws DicomServiceException, ConfigurationException {
        String studyInstanceUID = queryParams.getFirst("StudyInstanceUID");
        // create a session
        StoreSession session;
        try {
            session = storeService.createStoreSession(storeService);
        } catch (DicomServiceException e) {
            throw e;
        }
        // get archive extension for store service attributes filter purposes
        ArchiveAEExtension arcAE;
        try {
            arcAE = aeCache.get(archAETitle).getAEExtension(
                    ArchiveAEExtension.class);
        } catch (ConfigurationException e) {
            throw e;
        }
        // sourceAE is the HttpSource
        session.setSource(new HttpSource(request));
        // remoteAETitle is the hostname of the request issuer
        session.setRemoteAET(request.getRemoteHost());
        session.setArchiveAEExtension(arcAE);
        return dataManager.updateStudy(studyInstanceUID, session, storeService,
                queryParams);

    }

}
