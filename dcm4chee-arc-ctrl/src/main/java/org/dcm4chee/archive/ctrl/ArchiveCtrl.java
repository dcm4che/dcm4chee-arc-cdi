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

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.ArchiveService;
import org.dcm4chee.archive.rs.HostAECache;
import org.dcm4chee.archive.rs.HttpSource;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
@Path("/ctrl")
@RequestScoped
public class ArchiveCtrl {

    @Inject
    private HostAECache cache;

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
        
        HttpSource source = new HttpSource(request);
        ApplicationEntity ae = cache.findAE(source);
        Device callerDevice = ae.getDevice();
        
        return Response.ok(
                "<div>Calling Device: <br>Host:" + request.getRemoteHost()
                        + "<br>AETitle: " + ae.getAETitle()
                        + "<br>Device Name: " + callerDevice.getDeviceName()
                        + "</div>").build();

    }

}
