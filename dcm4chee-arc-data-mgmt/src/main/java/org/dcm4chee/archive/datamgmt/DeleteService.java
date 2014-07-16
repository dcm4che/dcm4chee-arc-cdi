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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.dcm4chee.archive.datamgmt.ejb.DataMgmtBean;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

@Path("/datamgmgt")
@RequestScoped
public class DeleteService {

    @Context
    private HttpServletRequest request;

    @Inject
    DataMgmtBean dataManager;

    private String RSP;

    @GET
    @Path("/delete/studies/{StudyInstanceUID}")
    public Response deleteStudyGet(
            @PathParam("StudyInstanceUID") String studyInstanceUID) {
        RSP = "Deleted Study with UID = "
                + dataManager.deleteStudy(studyInstanceUID)
                        .getStudyInstanceUID();

        return Response.ok(RSP).build();
    }

    @GET
    @Path("/delete/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    public Response deleteSeriesGet(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) {
        RSP = "Deleted Series with UID = "
                + dataManager.deleteSeries(seriesInstanceUID)
                        .getSeriesInstanceUID();

        return Response.ok(RSP).build();
    }

    @GET
    @Path("/delete/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}")
    public Response deleteInstanceGet(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID)
            throws Exception {
        RSP = "Deleted Instance with UID = "
                + dataManager.deleteInstance(sopInstanceUID)
                        .getSopInstanceUID();

        return Response.ok(RSP).build();
    }

    @DELETE
    @Path("/delete/studies/{StudyInstanceUID}")
    public Response deleteStudy(
            @PathParam("StudyInstanceUID") String studyInstanceUID) {
        RSP = "Deleted Study with UID = "
                + dataManager.deleteStudy(studyInstanceUID)
                        .getStudyInstanceUID();

        return Response.ok(RSP).build();

    }

    @DELETE
    @Path("/delete/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    public Response deleteSeries(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) {
        RSP = "Deleted Series with UID = "
                + dataManager.deleteSeries(seriesInstanceUID)
                        .getSeriesInstanceUID();

        return Response.ok(RSP).build();
    }

    @DELETE
    @Path("/delete/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}")
    public Response deleteInstance(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID)
            throws Exception {
        RSP = "Deleted Instance with UID = "
                + dataManager.deleteInstance(sopInstanceUID)
                        .getSopInstanceUID();

        return Response.ok(RSP).build();
    }

    @GET
    @Path("/deleteifempty/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    public Response deleteSeriesIfEmptyGet(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) {
        RSP = "Series with UID = "
                + seriesInstanceUID
                + " was empty and Deleted = "
                + dataManager.deleteSeriesIfEmpty(seriesInstanceUID,
                        studyInstanceUID);

        return Response.ok(RSP).build();
    }

    @GET
    @Path("/deleteifempty/studies/{StudyInstanceUID}")
    public Response deleteStudyIfEmptyGet(
            @PathParam("StudyInstanceUID") String studyInstanceUID) {
        RSP = "Study with UID = " + studyInstanceUID
                + " was empty and Deleted = "
                + dataManager.deleteStudyIfEmpty(studyInstanceUID);

        return Response.ok(RSP).build();
    }

    @DELETE
    @Path("/purge/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    public Response deleteSeriesIfEmpty(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) {
        RSP = "Series with UID = "
                + seriesInstanceUID
                + " was empty and Deleted = "
                + dataManager.deleteSeriesIfEmpty(seriesInstanceUID,
                        studyInstanceUID);

        return Response.ok(RSP).build();
    }

    @DELETE
    @Path("/purge/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}")
    public Response deleteStudyIfEmpty(
            @PathParam("StudyInstanceUID") String studyInstanceUID) {
        RSP = "Study with UID = " + studyInstanceUID
                + " was empty and Deleted = "
                + dataManager.deleteStudyIfEmpty(studyInstanceUID);

        return Response.ok(RSP).build();
    }

}
