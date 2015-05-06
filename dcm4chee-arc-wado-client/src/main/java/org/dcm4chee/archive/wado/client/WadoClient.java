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

package org.dcm4chee.archive.wado.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.dcm4che3.data.Tag;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4chee.archive.store.StoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 *
 */
public class WadoClient {

    private static final Logger LOG = LoggerFactory.getLogger(WadoClient.class);

    private WadoClientService service;

    public WadoClient(WadoClientService service) {
        this.service = service;
    }

    public WadoClientResponse fetch(final String localAET,
            final String remoteAET, String studyUID, String seriesUID,
            String iuid, String baseURL) throws IOException {
        URL newUrl = new URL(toWadoRSURL(baseURL, studyUID, seriesUID, iuid));

        HttpURLConnection connection = (HttpURLConnection) newUrl
                .openConnection();

        connection.setDoOutput(true);

        connection.setDoInput(true);

        connection.setInstanceFollowRedirects(false);

        connection.setRequestMethod("GET");

        connection.setRequestProperty("charset", "utf-8");

        connection.addRequestProperty("Accept", "application/dicom");

        // connection.setConnectTimeout();
        connection.setUseCaches(false);

        int responseCode = connection.getResponseCode();
        String reponseMessage = connection.getResponseMessage();
        String boundary = getBoundaryFromContentType(connection
                .getHeaderField("Content-Type"));
        InputStream in = null;
        in = connection.getInputStream();
        if (in == null)
            return new WadoClientResponse(new ArrayList<String>(),
                    new ArrayList<String>(), new ArrayList<String>());
        ArrayList<String> requested = new ArrayList<String>();
        final ArrayList<String> failed = new ArrayList<String>();
        final ArrayList<String> completed = new ArrayList<String>();
        try {
            new MultipartParser(boundary).parse(in,
                    new MultipartParser.Handler() {

                        @Override
                        public void bodyPart(int partNumber,
                                MultipartInputStream in) throws IOException {
                            StoreContext ctx = null;
                            String receivedIUID = null;
                            try {
                                ctx = service.spool(localAET, remoteAET, in,
                                        service.getCallBack());
                                receivedIUID = ctx.getAttributes().getString(
                                        Tag.SOPInstanceUID);
                                if (service.store(ctx)) {
                                    LOG.debug("Successfully fetched instance "
                                            + "{} from {}", receivedIUID,
                                            remoteAET);
                                    completed.add(receivedIUID);
                                } else {
                                    LOG.debug(
                                            "Failed to fetch instance {} from {}",
                                            receivedIUID, remoteAET);
                                    failed.add(receivedIUID);
                                }
                            } catch (Exception e) {
                                LOG.debug(
                                        "Failed to fetch instance {} from {}",
                                        receivedIUID, remoteAET);
                                failed.add(receivedIUID);
                            }
                        }
                    });
            connection.disconnect();
            requested.addAll(failed);
            requested.addAll(completed);
        } catch (Exception e) {
            System.out.println("Error parsing Server response - " + e);
        }
        return new WadoClientResponse(completed, requested, failed);
    }

    private String getBoundaryFromContentType(String headerField) {
        return headerField.split(";")[2].split("=")[1];
    }

    private String toWadoRSURL(String baseURL, String studyUID,
            String seriesUID, String iuid) {
        if (studyUID == null || studyUID.isEmpty())
            throw new IllegalArgumentException("Study UID can not be empty");
        return baseURL + (studyUID != null ? "/studies/" + studyUID : "")
                + (seriesUID != null ? "/series/" + seriesUID : "")
                + (iuid != null ? "/instances/" + iuid : "");
    }

}
