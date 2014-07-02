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

package org.dcm4chee.archive.ctrl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dcm4che3.conf.api.AttributeCoercion;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.IApplicationEntityCache;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.ArchiveService;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.HostNameAEEntry;
import org.dcm4chee.archive.rs.HttpSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
@Path("/ctrl")
@RequestScoped
public class ArchiveCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(ArchiveCtrl.class);

    private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    @Inject
    IApplicationEntityCache cache;

    @Inject
    private ArchiveService service;

    @Context
    private HttpServletRequest request;

    @GET
    @Path("running")
    public String isRunning() {
        return String.valueOf(service.isRunning());
    }

    @GET
    @Path("start")
    public Response start() throws Exception {
        service.start(new HttpSource(request));
        return Response.status(Status.OK).build();
    }

    @GET
    @Path("stop")
    public Response stop() {
        service.stop(new HttpSource(request));
        return Response.status(Status.OK).build();
    }

    @GET
    @Path("reload")
    public Response reload() throws Exception {
        service.reload();
        return Response.status(Status.OK).build();
    }

    @GET
    @Path("/whoami")
    @Produces(MediaType.TEXT_HTML)
    public Response whoami() throws ConfigurationException {
        ArchiveDeviceExtension arcDevExt = service.getDevice()
                .getDeviceExtension(ArchiveDeviceExtension.class);
        
        HostNameAEEntry caller = null;
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(IPADDRESS_PATTERN);
        String host = arcDevExt.isHostnameAEresoultion() ? request
                .getRemoteHost() : request.getRemoteAddr();
        matcher = pattern.matcher(host);
        if (arcDevExt.isHostnameAEresoultion() && matcher.matches())
            return Response
                    .status(Status.CONFLICT)
                    .entity("Unable to perform name resoultion for the host\n reset the map entry to ip address and disable name resolution")
                    .build();

        for (HostNameAEEntry entry : arcDevExt.getHostNameAEList()) {
            if (entry.getHostName().compareTo(host) == 0) {
                caller = entry;
            }
        }
        if(caller == null)
        {
            caller = arcDevExt.getHostNameAEFallBackEntry();
            LOG.debug("Unable to match request remote host with an AE, \n"
                    + "(Probably due to missing map entry in the configuration or due to hostname resolution failure)\n"
                    + "Using FallBack AE with Title"+caller.getAeTitle());
        }
        ApplicationEntity ae = null;
        try{
       ae = cache.get(caller.getAeTitle());

       if(ae == null)
           throw new NullPointerException();
        }
        catch(Exception e)
        {
            //happens if incorrect AETitle (not in the configuration backend)
            return Response.ok(
                    "<div>Calling Device: <br>Host:" + caller.getHostName()
                            + "<br>AETitle: " + "Incorrect AETitle or not found in the configuration"
                            + "<br>Device Name: " + "unable to retrieve device since not AE is found"
                            + "</div>").build();
        }
        Device callerDevice = ae.getDevice();
        
        return Response.ok(
                "<div>Calling Device: <br>Host:" + caller.getHostName()
                        + "<br>AETitle: " + caller.getAeTitle()
                        + "<br>Device Name: " + callerDevice.getDeviceName()
                        + "</div>").build();

    }

}
