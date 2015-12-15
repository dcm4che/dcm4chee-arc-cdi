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

package org.dcm4chee.archive.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Service implementing DICOM Supplement 166: Query based on ID for DICOM Objects (QIDO).
 *
 * @author Alessio Roselli <alessio.roselli@agfa.com>
 */
@Path("/{AETitle}")
public interface QidoRS {

    @GET
    @Path("/studies")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForStudiesXML() throws Exception;

    @GET
    @Path("/studies")
    @Produces("application/json")
    Response searchForStudiesJSON() throws Exception;

    @GET
    @Path("/series")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForSeriesXML() throws Exception;

    @GET
    @Path("/series")
    @Produces("application/json")
    Response searchForSeriesJSON() throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/series")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForSeriesOfStudyXML(
            @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/series")
    @Produces("application/json")
    Response searchForSeriesOfStudyJSON(
            @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception;

    @GET
    @Path("/instances")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForInstancesXML() throws Exception;

    @GET
    @Path("/instances")
    @Produces("application/json")
    Response searchForInstancesJSON() throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/instances")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForInstancesOfStudyXML(
            @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/instances")
    @Produces("application/json")
    Response searchForInstancesOfStudyJSON(
            @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances")
    @Produces("multipart/related;type=application/dicom+xml")
    Response searchForInstancesOfSeriesXML(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) throws Exception;

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances")
    @Produces("application/json")
    Response searchForInstancesOfSeriesJSON(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID) throws Exception;
}
