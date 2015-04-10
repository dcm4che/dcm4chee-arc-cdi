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

package org.dcm4chee.archive.stow.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.util.SafeClose;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
public class StowClient {

    private static final Logger LOG = LoggerFactory.getLogger(
            StowClient.class);
    
    private StowClientService service;
    private StowContext context;
    
    public StowClient(StowClientService service
            , StowContext context) {
        this.service = service;
        this.context = context; 
    }

    public StowResponse storeOverWebService(
            Collection<ArchiveInstanceLocator> instances) {

        ArrayList<String> failedInstances = new ArrayList<String>();
        ArrayList<String> successfulInstances = new ArrayList<String>();
        String aeTitle = context.getRemoteAE().getAETitle();
        String url = adjustToStowURL(aeTitle, context.getRemoteBaseURL());
        for(ArchiveInstanceLocator inst : instances) {
            
            try{
            storeOverWebService(aeTitle, url, inst);
            successfulInstances.add(inst.iuid);
            }
            catch(IOException e) {
                failedInstances.add(inst.iuid);
            }
        }
        StowResponse response = new StowResponse(failedInstances, successfulInstances);
        response.setRemoteStowURL(url);
        return response;

    }

    private boolean storeOverWebService(String aeTitle, String url
            , ArchiveInstanceLocator inst)
            throws IOException {

        HttpURLConnection connection = null;;
        int rspCode = 0;
        
        try {
            URL stowURL = new URL(url);
            
            String boundary = generateBoundary();
            
            connection = setupStowConnection(boundary, stowURL);
            
            writeRequest(inst, boundary, getConnectionOutputStream(boundary
                    , connection));
            
            rspCode = connection.getResponseCode();
            
            logResponse(aeTitle, connection);
        } catch (IOException e) {
            LOG.error("error writing to http data output stream"
                    + e.getStackTrace().toString());
            throw new IOException("Error while performing stow to " + aeTitle + e);
        }
        finally {
            connection.disconnect();
        }

        return rspCode == 200 ? true : false;
    }


    private void logResponse(String aeTitle, HttpURLConnection connection) {
        try {
            if(LOG.isDebugEnabled())
                LOG.debug("Stowrs response received from {} : \n {}"
                        ,aeTitle, SAXReader.parse(
                                connection.getInputStream()).toString());
        } catch (Exception e) {
            LOG.error("Error creating response attributes, {}",e);
        }
    }

    private void writeRequest(ArchiveInstanceLocator inst
            , String boundary, DataOutputStream wr)
            throws IOException {
        DicomInputStream dis = new DicomInputStream(
                inst.getFile());
        try {
            dis.setIncludeBulkData(IncludeBulkData.URI);
            Attributes dataset = dis.readDataset(-1, -1);
             service.coerceAttributes(dataset, context);
            dataset.addAll((Attributes) inst.getObject());
            Attributes fmi = dataset.createFileMetaInformation(inst.tsuid);
            @SuppressWarnings("resource")
            DicomOutputStream dos = new DicomOutputStream(wr,
                    UID.ExplicitVRLittleEndian);
            dos.writeDataset(fmi, dataset);
            wr.writeBytes("\r\n--" + boundary + "--\r\n");
            wr.flush();
            wr.close();
        } finally {
            SafeClose.close(dis);
        }
    }

    private DataOutputStream getConnectionOutputStream(String boundary,
            HttpURLConnection connection) throws IOException {
        DataOutputStream wr;
        wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes("\r\n--" + boundary + "\r\n");
        wr.writeBytes("Content-Type: application/dicom \r\n");
        wr.writeBytes("\r\n");
        return wr;
    }

    private String generateBoundary() {
        return "--------"+UUID.randomUUID().toString()
        .replaceAll("[^\\d.-]", "");
    }

    private HttpURLConnection setupStowConnection(String boundary, URL url)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url
        .openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "multipart/related; type=application/dicom; boundary="
                        + boundary);
        connection.setRequestProperty("Accept", "application/dicom+xml");
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);
        return connection;
    }

    private String adjustToStowURL(String aeTitle, String remoteBaseURL) {
        String stowPath = "stow/"+aeTitle+"/studies";
        return remoteBaseURL.endsWith("/") 
                ? remoteBaseURL + stowPath 
                        : remoteBaseURL + "/" + stowPath;
    }

}
