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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
package org.dcm4chee.archive.wado;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.Decompressor;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.InstanceLocator;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.ws.rs.MediaTypes;
import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.dto.GenericParticipant;
import org.dcm4chee.archive.dto.ServiceType;
import org.dcm4chee.archive.fetch.forward.FetchForwardCallBack;
import org.dcm4chee.archive.fetch.forward.FetchForwardService;
import org.dcm4chee.archive.retrieve.impl.RetrieveAfterSendEvent;
import org.dcm4chee.archive.rs.HostAECache;
import org.dcm4chee.archive.rs.HttpSource;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.jboss.resteasy.plugins.providers.multipart.ContentIDUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service implementing DICOM Supplement 161: WADO by RESTful Services
 * (WADO-RS).
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@Path("/wado/{AETitle}")
public class WadoRS extends Wado {

    @Inject
    private Event<RetrieveAfterSendEvent> retrieveEvent;

    @Inject
    private HostAECache aeCache;

    @Inject
    private FetchForwardService fetchForwardService;

    private static final int STATUS_OK = 200;
    private static final int STATUS_PARTIAL_CONTENT = 206;
    private static final int STATUS_NOT_ACCEPTABLE = 406;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_ID = "Content-ID";
    private static final String CONTENT_LOCATION = "Content-Location";

    private static final Logger LOG = LoggerFactory.getLogger(WadoRS.class);

    public static final class FrameList {
        final int[] frames;

        public FrameList(String s) {
            String[] ss = StringUtils.split(s, ',');
            int[] values = new int[ss.length];
            for (int i = 0; i < ss.length; i++) {
                try {
                    if ((values[i] = Integer.parseInt(ss[i])) <= 0)
                        throw new WebApplicationException(Status.BAD_REQUEST);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(s);
                }
            }
            this.frames = values;
        }
    }

    @Context
    private HttpServletRequest request;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders headers;

    private CStoreSCUContext context;

    private boolean acceptAll;

    private boolean acceptZip;

    private boolean acceptDicomXML;

    private boolean acceptDicomJSON;

    private boolean acceptDicom;

    private boolean acceptOctetStream;

    private boolean acceptBulkdata;

    private List<String> acceptedTransferSyntaxes;

    private List<MediaType> acceptedBulkdataMediaTypes;

    private String method;

    private String toBulkDataURI(String uri) {
        return uriInfo.getBaseUri() + "wado/" + aetitle + "/bulkdata/"
                + URI.create(uri).getPath();
    }

    private void init(String method) {

        ApplicationEntity sourceAE = aeCache.findAE(new HttpSource(request));
        if (sourceAE == null) {
            LOG.info("Unable to find the mapped AE for host {} or even the fallback AE, elimination/coercion will not be applied", request.getRemoteHost());
        }

        context = new CStoreSCUContext(arcAE.getApplicationEntity(), sourceAE, ServiceType.WADOSERVICE);

        this.method = method;
        List<MediaType> acceptableMediaTypes = headers
                .getAcceptableMediaTypes();
        ApplicationEntity ae = device.getApplicationEntity(aetitle);
        if (ae == null || !ae.isInstalled())
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

        this.acceptedTransferSyntaxes = new ArrayList<String>(
                acceptableMediaTypes.size());

        this.acceptedBulkdataMediaTypes = new ArrayList<MediaType>(
                acceptableMediaTypes.size());

        for (MediaType mediaType : acceptableMediaTypes) {
            if (mediaType.isWildcardType())
                acceptAll = true;
            else if (mediaType.isCompatible(MediaTypes.APPLICATION_ZIP_TYPE))
                acceptZip = true;
            else if (mediaType.isCompatible(MediaTypes.MULTIPART_RELATED_TYPE)) {
                try {
                    MediaType relatedType = MediaType.valueOf(mediaType
                            .getParameters().get("type"));
                    if (relatedType
                            .isCompatible(MediaTypes.APPLICATION_DICOM_TYPE)) {
                        acceptDicom = true;
                        acceptedTransferSyntaxes.add(mediaType
                                .getParameters().get("transfer-syntax"));
                    } else if (relatedType
                            .isCompatible(MediaTypes.APPLICATION_DICOM_XML_TYPE)) {
                        acceptDicomXML = true;
                    } else {
                        acceptBulkdata = true;
                        if (relatedType
                                .isCompatible(MediaType.APPLICATION_OCTET_STREAM_TYPE))
                            acceptOctetStream = true;
                        acceptedBulkdataMediaTypes.add(relatedType);
                    }
                } catch (IllegalArgumentException e) {
                    throw new WebApplicationException(Status.BAD_REQUEST);
                }
            } else if (headers.getAcceptableMediaTypes().contains(
                    MediaType.APPLICATION_JSON_TYPE)) {
                acceptDicomJSON = true;
            }
        }
    }

    private String selectDicomTransferSyntaxes(InstanceLocator ref) {
        List<String> supportedTransferSyntaxes = acceptedTransferSyntaxes;
        if(context.getLocalAE()!=null)
        if (arcAE.getRetrieveSuppressionCriteria()
                .isCheckTransferCapabilities()){
        if(confSupportsTransferSyntax(ref)!=null)
            return ref.tsuid;
        else
            return getDefaultConfiguredTransferSyntax(ref);
        }
        for (String ts1 : supportedTransferSyntaxes) {
            if (ts1 == null || ts1.equals(ref.tsuid))
                return ref.tsuid;
        }
        if (ImageReaderFactory.canDecompress(ref.tsuid)) {
            if (supportedTransferSyntaxes.contains(UID.ExplicitVRLittleEndian)) {
                return UID.ExplicitVRLittleEndian;
            }
            if (supportedTransferSyntaxes.contains(UID.ImplicitVRLittleEndian)) {
                return UID.ImplicitVRLittleEndian;
            }
        }
        return null;
    }

    private String confSupportsTransferSyntax(InstanceLocator ref) {
        ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                context.getLocalAE().getTransferCapabilitiesWithRole(
                        Role.SCU));
            for (TransferCapability supportedTC : aeTCs){
                if (ref.cuid.compareTo(supportedTC.getSopClass()) == 0 && 
                        supportedTC.containsTransferSyntax(ref.tsuid)) {
                    return ref.tsuid;
                }
            }
            return null;
    }

    private String getDefaultConfiguredTransferSyntax(InstanceLocator ref)
    {
        ArrayList<TransferCapability> aeTCs = new ArrayList<TransferCapability>(
                context.getLocalAE().getTransferCapabilitiesWithRole(
                        Role.SCU));
        for (TransferCapability supportedTC : aeTCs){
            if (ref.cuid.compareTo(supportedTC.getSopClass()) == 0 ) {
                return supportedTC.containsTransferSyntax(UID.ExplicitVRLittleEndian)?
                        UID.ExplicitVRLittleEndian:UID.ImplicitVRLittleEndian;
            }
        }
        return UID.ImplicitVRLittleEndian;
    }
    private MediaType selectBulkdataMediaTypeForTransferSyntax(String ts) {
        MediaType requiredMediaType = null;
        try {
            requiredMediaType = MediaTypes.forTransferSyntax(ts);
        } catch (IllegalArgumentException e) {
            // ignored
        }
        if (requiredMediaType == null)
            return null;

        if (acceptAll)
            return requiredMediaType;

        boolean defaultTS = !requiredMediaType.getParameters().containsKey(
                "transfer-syntax");
        for (MediaType mediaType : acceptedBulkdataMediaTypes) {
            if (mediaType.isCompatible(requiredMediaType)) {
                String ts1 = mediaType.getParameters().get("transfer-syntax");
                if (ts1 == null ? defaultTS : ts1.equals(ts))
                    return requiredMediaType;
            }
        }
        if (acceptOctetStream && ImageReaderFactory.canDecompress(ts)) {
            return MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }
        return requiredMediaType;
    }

    @GET
    @Path("/studies/{StudyInstanceUID}")
    public Response retrieveStudy(
            @PathParam("StudyInstanceUID") String studyInstanceUID)
            throws DicomServiceException  {
        init("retrieveStudy");

        List<ArchiveInstanceLocator> instances = retrieveService
                .calculateMatches(studyInstanceUID, "*", "*", queryParam, false);

        return retrieve(instances);
    }

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    public Response retrieveSeries(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID)
            throws DicomServiceException {
        init("retrieveSeries");

        List<ArchiveInstanceLocator> instances = retrieveService
                .calculateMatches(studyInstanceUID, seriesInstanceUID, "*",
                        queryParam, false);

        return retrieve(instances);
    }

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}")
    public Response retrieveInstance(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID)
            throws DicomServiceException {
        init("retrieveInstance");

        List<ArchiveInstanceLocator> instances = retrieveService
                .calculateMatches(studyInstanceUID, seriesInstanceUID,
                        sopInstanceUID, queryParam, false);

        return retrieve(instances);
    }

    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}/frames/{FrameList}")
    @Produces("multipart/related")
    public Response retrieveFrame(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID,
            @PathParam("FrameList") FrameList frameList) {
        init("retrieveFrame");

        List<ArchiveInstanceLocator> instances = retrieveService
                .calculateMatches(studyInstanceUID, seriesInstanceUID,
                        sopInstanceUID, queryParam, false);

        if (instances == null || instances.size() == 0)
            throw new WebApplicationException(Status.NOT_FOUND);

        return retrievePixelData(instances.get(0), frameList.frames);
    }

    /**
     * BulkDataURI is expected to be a path to a file.
     */
    @GET
    @Path("/bulkdata/{BulkDataPath:.*}")
    @Produces("multipart/related")
    public Response retrieveBulkdata(
            @PathParam("BulkDataPath") String bulkDataPath,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("length") @DefaultValue("-1") int length) {

        init("retrieveBulkdata");

        String bulkDataURI = "file://" + bulkDataPath;

        return (length <= 0) ? retrievePixelDataFromFile(bulkDataURI)
                : retrieveBulkData(new BulkData(bulkDataURI, offset, length,
                        false));

    }

    @GET
    @Path("/studies/{StudyInstanceUID}/metadata")
    public Response retrieveStudyMetadata(
            @PathParam("StudyInstanceUID") String studyInstanceUID)
            throws DicomServiceException {
        init("retrieveMetadata");

        List<ArchiveInstanceLocator> instances = retrieveService
                .calculateMatches(studyInstanceUID, null, null, queryParam, true);

        return retrieveMetadata(instances);
    }

    // create metadata retrieval for Series
    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/metadata")
    public Response retrieveSeriesMetadata(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID)
            throws DicomServiceException {
        init("retrieveMetadata");

        List<ArchiveInstanceLocator> instances = retrieveService
                .calculateMatches(studyInstanceUID, seriesInstanceUID, null,
                        queryParam, true);

        return retrieveMetadata(instances);
    }

    // create metadata retrieval for Instances
    @GET
    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/{SOPInstanceUID}/metadata")
    public Response retrieveInstanceMetadata(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @PathParam("SeriesInstanceUID") String seriesInstanceUID,
            @PathParam("SOPInstanceUID") String sopInstanceUID)
            throws DicomServiceException {
        init("retrieveMetadata");

        List<ArchiveInstanceLocator> instances = retrieveService
                .calculateMatches(studyInstanceUID, seriesInstanceUID,
                        sopInstanceUID, queryParam, true);

        return retrieveMetadata(instances);
    }

    private Response retrieve(List<ArchiveInstanceLocator> refs)
            throws DicomServiceException {

        List<ArchiveInstanceLocator> insts = new ArrayList<ArchiveInstanceLocator>();
        List<ArchiveInstanceLocator> instswarning = new ArrayList<ArchiveInstanceLocator>();
        final List<ArchiveInstanceLocator> instscompleted = new ArrayList<ArchiveInstanceLocator>();
        final List<ArchiveInstanceLocator> instsfailed = new ArrayList<ArchiveInstanceLocator>();

        try {
            if (refs.isEmpty())
                throw new WebApplicationException(Status.NOT_FOUND);
            else
                insts.addAll(refs);
            // check for SOP classes elimination
            if (arcAE.getRetrieveSuppressionCriteria()
                    .isCheckTransferCapabilities())
            {
                List<ArchiveInstanceLocator> adjustedRefs = new ArrayList<ArchiveInstanceLocator>();
                for(ArchiveInstanceLocator ref: refs){
                    if(storescuService.eliminateUnSupportedSOPClasses(ref, context) == null)
                        instsfailed.add(ref);
                    else
                        adjustedRefs.add(ref);
                }
                refs = adjustedRefs;
            }
            // check for suppression criteria
            Map<String, String> suppressionCriteriaMap = arcAE
                    .getRetrieveSuppressionCriteria().getSuppressionCriteriaMap();
            if(context.getRemoteAE() !=null)
            if (suppressionCriteriaMap.containsKey(context.getRemoteAE().getAETitle())) {
                String supressionCriteriaTemplateURI = suppressionCriteriaMap
                        .get(context.getRemoteAE().getAETitle());
                if (supressionCriteriaTemplateURI != null) {
                    List<ArchiveInstanceLocator> adjustedRefs = new ArrayList<ArchiveInstanceLocator>();
                    for(ArchiveInstanceLocator ref: refs){
                        Attributes attrs = getFileAttributes(ref);
                    if(storescuService.applySuppressionCriteria(ref, attrs,
                            supressionCriteriaTemplateURI, context) == null)
                        instsfailed.add(ref);
                    else
                        adjustedRefs.add(ref);
                    }
                    refs = adjustedRefs;
                }
            }
            ArrayList<ArchiveInstanceLocator> external = extractExternalLocators(refs);
            final MultipartRelatedOutput multiPartOutput = new MultipartRelatedOutput();
            final ZipOutput zipOutput = new ZipOutput();
            if(!refs.isEmpty()) {
            addDicomOrBulkDataOrZip(refs, instscompleted, instsfailed,
                    multiPartOutput, zipOutput);
            }
            if(!external.isEmpty()) {
                FetchForwardCallBack fetchCallBack = new FetchForwardCallBack() {
                    @Override
                    public void onFetch(Collection<ArchiveInstanceLocator> instances,
                            BasicCStoreSCUResp resp) {
                        addDicomOrBulkDataOrZip((List<ArchiveInstanceLocator>) instances, instscompleted, instsfailed,
                                multiPartOutput, zipOutput);
                    }
                };
                
                    ArrayList<ArchiveInstanceLocator> failedToFetchForward = new ArrayList<ArchiveInstanceLocator>();
                    failedToFetchForward = fetchForwardService.fetchForward(aetitle, external, fetchCallBack, fetchCallBack);
                    instsfailed.addAll(failedToFetchForward);
            }
            if(!acceptDicom && !acceptBulkdata && (acceptAll || acceptZip))
            return Response.ok().entity(zipOutput)
                    .type(MediaTypes.APPLICATION_ZIP_TYPE).build();
            else
            {
                int status = instsfailed.size() > 0 ? STATUS_PARTIAL_CONTENT : STATUS_OK;
                return Response.status(status).entity(multiPartOutput).build();
            }
        } finally {
            // audit
            retrieveEvent.fire(new RetrieveAfterSendEvent(
                    new GenericParticipant(request.getRemoteAddr(), request
                            .getRemoteUser()), new GenericParticipant(request
                            .getLocalAddr(), null), new GenericParticipant(
                            request.getRemoteAddr(), request.getRemoteUser()),
                    device, insts, instscompleted, instswarning, instsfailed));
        }
    }

    private void addDicomOrBulkDataOrZip(List<ArchiveInstanceLocator> refs,
            List<ArchiveInstanceLocator> instscompleted,
            List<ArchiveInstanceLocator> instsfailed,
            MultipartRelatedOutput multiPartOutput, ZipOutput zipOutput) {
        if (acceptDicom || acceptBulkdata) {
            retrieveDicomOrBulkData(refs, instscompleted,
                    instsfailed, multiPartOutput);
        }
        else { 
            if (!acceptZip && !acceptAll)
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);
        retrieveZIP(refs, instsfailed, instscompleted, zipOutput);
        }
    }

    private void retrieveDicomOrBulkData(List<ArchiveInstanceLocator> refs,
            List<ArchiveInstanceLocator> instscompleted,
            List<ArchiveInstanceLocator> instsfailed,
            MultipartRelatedOutput output) {
        if (acceptedBulkdataMediaTypes.isEmpty()) {

            for (ArchiveInstanceLocator ref : refs)
                if (!addDicomObjectTo(ref, output)) {
                    instsfailed.add((ArchiveInstanceLocator) ref);
                } else
                    instscompleted.add((ArchiveInstanceLocator) ref);
        } else {
            for (InstanceLocator ref : refs)
                if (addPixelDataTo(ref.uri, output) != STATUS_OK) {
                    instsfailed.add((ArchiveInstanceLocator) ref);
                } else
                    instscompleted.add((ArchiveInstanceLocator) ref);
        }

        if (output.getParts().isEmpty())
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);
    }

    private void retrieveZIP(List<ArchiveInstanceLocator> refs,
            List<ArchiveInstanceLocator> instsfailed,
            List<ArchiveInstanceLocator> instscompleted, ZipOutput output) {
        for (ArchiveInstanceLocator ref : refs) {
            try{
            output.addEntry(new DicomObjectOutput(ref, (Attributes) ref
                    .getObject(), ref.tsuid, context, storescuService));
            instscompleted.add(ref);
            }
            catch(Exception e) {
                instsfailed.add(ref);
                LOG.error(
                        "Failed to add zip Entry for instance {} - Exception {}",
                        ref.iuid, e.getMessage());
            }
        }
    }

    private ArrayList<ArchiveInstanceLocator> extractExternalLocators(
            List<ArchiveInstanceLocator> refs) {
        ArrayList<ArchiveInstanceLocator> externalLocators = new ArrayList<ArchiveInstanceLocator>();
        for (Iterator<ArchiveInstanceLocator> iter = refs.iterator(); iter.hasNext();)
        {
            ArchiveInstanceLocator loc = iter.next();
            if (loc.getStorageSystem() == null) {
                externalLocators.add(loc);
                iter.remove();
            }
        }
        return externalLocators;
    }


    private Response retrievePixelData(ArchiveInstanceLocator inst,
            final int... frames) {
        final String fileURI = inst.uri;
        final ArrayList<Integer> status =  new ArrayList<Integer>();
        final MultipartRelatedOutput output = new MultipartRelatedOutput();
        ArrayList<ArchiveInstanceLocator> locations = new ArrayList<ArchiveInstanceLocator>();
        locations.add(inst);
        ArrayList<ArchiveInstanceLocator> external = extractExternalLocators(locations);

        ArrayList<ArchiveInstanceLocator> failedToFetchForward = new ArrayList<ArchiveInstanceLocator>();
        if(!locations.isEmpty()) {
            status.add(addPixelDataTo(fileURI, output, frames));
        }
        if(!external.isEmpty()) {
            FetchForwardCallBack fetchCallBack = new FetchForwardCallBack() {
                @Override
                public void onFetch(Collection<ArchiveInstanceLocator> instances,
                        BasicCStoreSCUResp resp) {
                    status.add(addPixelDataTo(fileURI, output, frames));
                }
            };
            failedToFetchForward = fetchForwardService.fetchForward(aetitle, external, fetchCallBack, fetchCallBack);
        }

        if(!failedToFetchForward.isEmpty())
            throw new WebApplicationException(Status.NOT_FOUND);
        
        if (output.getParts().isEmpty())
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);

        return Response.status( status.get(0).intValue()).entity(output).build();
    }
    private Response retrievePixelDataFromFile(String fileURI) {

        MultipartRelatedOutput output = new MultipartRelatedOutput();
            int status = addPixelDataTo(fileURI, output, new int[]{});
        
        if (output.getParts().isEmpty())
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);

        return Response.status(status).entity(output).build();
    }

    private Response retrieveBulkData(BulkData bulkData) {
        if (!acceptOctetStream)
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);

        MultipartRelatedOutput output = new MultipartRelatedOutput();
        addPart(output, new BulkDataOutput(bulkData),
                MediaType.APPLICATION_OCTET_STREAM_TYPE, uriInfo
                        .getRequestUri().toString(), null);

        return Response.ok(output).build();
    }

    private Response retrieveMetadata(final List<ArchiveInstanceLocator> refs)
            throws DicomServiceException {
        StreamingOutput streamingOutput = null;
        final MultipartRelatedOutput multiPartOutput = new MultipartRelatedOutput();
        ResponseBuilder JSONResponseBuilder = null;
        if (refs.isEmpty())
            throw new WebApplicationException(Status.NOT_FOUND);

        if (!acceptDicomXML && !acceptDicomJSON && !acceptAll)
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);

        if (acceptDicomJSON) {
            ArrayList<ArchiveInstanceLocator> external = extractExternalLocators(refs);

            if(!external.isEmpty()) {
                    ArrayList<ArchiveInstanceLocator> failedToFetchForward = new ArrayList<ArchiveInstanceLocator>();
                    failedToFetchForward = fetchForwardService.fetchForward(aetitle, external, null,null);
                    
                    if(!failedToFetchForward.isEmpty()) {
                        for(Iterator<ArchiveInstanceLocator> iter = external.iterator(); iter.hasNext();) {
                            if(failedToFetchForward.contains(iter.next())) {
                                iter.remove();
                            }
                        }
                    }
                    refs.addAll(external);
            }
                streamingOutput = new DicomJSONOutput(aetitle, uriInfo, refs,
                        context, storescuService);
        } else {
            ArrayList<ArchiveInstanceLocator> external = extractExternalLocators(refs);

            if(!refs.isEmpty()) {
                for (ArchiveInstanceLocator ref : refs)
                    addMetadataTo(ref, multiPartOutput);
            }
            if(!external.isEmpty()) {
                FetchForwardCallBack fetchCallBack = new FetchForwardCallBack() {
                    @Override
                    public void onFetch(Collection<ArchiveInstanceLocator> instances,
                            BasicCStoreSCUResp resp) {
                        for (ArchiveInstanceLocator loc : instances)
                            addMetadataTo(loc, multiPartOutput);
                    }
                };
                
                fetchForwardService.fetchForward(aetitle, external, fetchCallBack, fetchCallBack);
            }
        }

        if (streamingOutput != null && acceptDicomJSON) {
            JSONResponseBuilder = Response.ok(streamingOutput);

            JSONResponseBuilder.header(CONTENT_TYPE,
                    MediaType.APPLICATION_JSON_TYPE);
            JSONResponseBuilder.header(CONTENT_ID,
                    ContentIDUtils.generateContentID());
            return JSONResponseBuilder.build();
        }
        else {
            return Response.ok(multiPartOutput).build();
        }

    }

    private boolean addDicomObjectTo(ArchiveInstanceLocator ref,
            MultipartRelatedOutput output) {
        String tsuid = selectDicomTransferSyntaxes(ref);
        if (tsuid == null) {
            return false;
        }
        Attributes attrs = (Attributes) ref.getObject();
        addPart(output,
                new DicomObjectOutput(ref, attrs, tsuid, context, storescuService),
                MediaType.valueOf("application/dicom;transfer-syntax=" + tsuid),
                null, ref.iuid);
        return true;
    }

    private int addPixelDataTo(String fileURI, MultipartRelatedOutput output,
            int... frameList) {
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream(new File(new URI(fileURI)));
            dis.setIncludeBulkData(IncludeBulkData.URI);
            Attributes fmi = dis.readFileMetaInformation();
            String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID);
            MediaType mediaType = selectBulkdataMediaTypeForTransferSyntax(dis
                    .getTransferSyntax());
            if (mediaType == null) {
                LOG.info(
                        "{}: Failed to retrieve Pixel Data of Instance[uid={}]: Requested Transfer Syntax not supported",
                        method, iuid);
                return STATUS_NOT_ACCEPTABLE;
            }

            if (isMultiframeMediaType(mediaType) && frameList.length > 0) {
                LOG.info(
                        "{}: Failed to retrieve Frame Pixel Data of Instance[uid={}]: Not supported for Content-Type={}",
                        new Object[] { method, iuid, mediaType });
                return STATUS_NOT_ACCEPTABLE;
            }

            Attributes ds = dis.readDataset(-1, -1);
            Object pixeldata = ds.getValue(Tag.PixelData);
            if (pixeldata == null) {
                LOG.info(
                        "{}: Failed to retrieve Pixel Data of Instance[uid={}]: Not an image",
                        method, iuid);
                return STATUS_NOT_ACCEPTABLE;
            }

            int frames = ds.getInt(Tag.NumberOfFrames, 1);
            int[] adjustedFrameList = adjustFrameList(iuid, frameList, frames);

            String bulkDataURI = toBulkDataURI(fileURI);
            if (pixeldata instanceof Fragments) {
                Fragments bulkData = (Fragments) pixeldata;
                if (mediaType == MediaType.APPLICATION_OCTET_STREAM_TYPE) {
                    addDecompressedPixelDataTo(
                            new Decompressor(ds, dis.getTransferSyntax()),
                            adjustedFrameList, output, bulkDataURI, iuid);
                } else {
                    addCompressedPixelDataTo(bulkData, frames,
                            adjustedFrameList, output, mediaType, bulkDataURI,
                            iuid);
                }
            } else {
                BulkData bulkData = (BulkData) pixeldata;
                addUncompressedPixelDataTo(bulkData, ds, adjustedFrameList,
                        output, bulkDataURI, iuid);
            }
            return adjustedFrameList.length < frameList.length ? STATUS_PARTIAL_CONTENT
                    : STATUS_OK;
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        } catch (IOException e) {
            throw new WebApplicationException(e);
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e);
        } finally {
            SafeClose.close(dis);
        }
    }

    private int[] adjustFrameList(String iuid, int[] frameList, int frames) {
        int n = 0;
        for (int i = 0; i < frameList.length; i++) {
            if (frameList[i] <= frames)
                swap(frameList, n++, i);
        }
        if (n == frameList.length)
            return frameList;

        int[] skipped = new int[frameList.length - n];
        System.arraycopy(frameList, n, skipped, 0, skipped.length);
        LOG.info(
                "{}, Failed to retrieve Frames {} of Pixel Data of Instance[uid={}]: NumberOfFrames={}",
                new Object[] { method, Arrays.toString(skipped), iuid, frames });
        if (n == 0)
            throw new WebApplicationException(Status.NOT_FOUND);

        return Arrays.copyOf(frameList, n);
    }

    private static void swap(int[] a, int i, int j) {
        if (i != j) {
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }

    private void addDecompressedPixelDataTo(Decompressor decompressor,
            int[] frameList, MultipartRelatedOutput output, String bulkDataURI,
            String iuid) {
        if (frameList.length == 0) {
            addPart(output, new DecompressedPixelDataOutput(decompressor, -1),
                    MediaType.APPLICATION_OCTET_STREAM_TYPE, bulkDataURI, iuid);
        } else
            for (int frame : frameList) {
                addPart(output, new DecompressedPixelDataOutput(decompressor,
                        frame - 1), MediaType.APPLICATION_OCTET_STREAM_TYPE,
                        bulkDataURI + "/frames/" + frame, iuid);
            }
    }

    private void addPart(MultipartRelatedOutput output, Object entity,
            MediaType mediaType, String contentLocation, String iuid) {
        OutputPart part = output.addPart(entity, mediaType);
        MultivaluedMap<String, Object> headerParams = part.getHeaders();
        headerParams.add(CONTENT_TYPE, mediaType);
        headerParams.add(CONTENT_ID, ContentIDUtils.generateContentID());
        if (contentLocation != null)
            headerParams.add(CONTENT_LOCATION, contentLocation);

        // TODO LOGGGING
        // if (iuid != null)
        // LOG.info("{}: Add Part #{} [uid={}]{}", new Object[] {
        // method,
        // output.getParts().size(),
        // iuid,
        // LogInterceptor.toString(headerParams) });
        // else
        // LOG.info("{}: Add Part #{}{}", new Object[] {
        // method,
        // output.getParts().size(),
        // LogInterceptor.toString(headerParams) });
    }

    private void addCompressedPixelDataTo(Fragments fragments, int frames,
            int[] adjustedFrameList, MultipartRelatedOutput output,
            MediaType mediaType, String bulkDataURI, String iuid) {
        if (frames == 1 || isMultiframeMediaType(mediaType)) {
            addPart(output, new CompressedPixelDataOutput(fragments),
                    mediaType, bulkDataURI, iuid);
        } else if (adjustedFrameList.length == 0) {
            for (int frame = 1; frame <= frames; frame++) {
                addPart(output,
                        new BulkDataOutput((BulkData) fragments.get(frame)),
                        mediaType, bulkDataURI + "/frames/" + frame, iuid);
            }
        } else {
            for (int frame : adjustedFrameList) {
                addPart(output,
                        new BulkDataOutput((BulkData) fragments.get(frame)),
                        mediaType, bulkDataURI + "/frames/" + frame, iuid);
            }
        }
    }

    private void addUncompressedPixelDataTo(BulkData bulkData, Attributes ds,
            int[] adjustedFrameList, MultipartRelatedOutput output,
            String bulkDataURI, String iuid) {
        if (adjustedFrameList.length == 0) {
            addPart(output, new BulkDataOutput(bulkData),
                    MediaType.APPLICATION_OCTET_STREAM_TYPE, bulkDataURI, iuid);
        } else {
            int rows = ds.getInt(Tag.Rows, 0);
            int cols = ds.getInt(Tag.Columns, 0);
            int samples = ds.getInt(Tag.SamplesPerPixel, 0);
            int bitsAllocated = ds.getInt(Tag.BitsAllocated, 8);
            int frameLength = rows * cols * samples * (bitsAllocated >>> 3);
            for (int frame : adjustedFrameList) {
                addPart(output,
                        new BulkDataOutput(new BulkData(bulkData
                                .uriWithoutQuery(), bulkData.offset()
                                + (frame - 1) * frameLength, frameLength, ds
                                .bigEndian())),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE, bulkDataURI
                                + "/frames/" + frame, iuid);
            }
        }
    }

    private void addMetadataTo(ArchiveInstanceLocator ref,
            MultipartRelatedOutput output) {
        Attributes attrs = (Attributes) ref.getObject();
        addPart(output, new DicomXMLOutput(ref, toBulkDataURI(ref.uri), attrs,
                context, storescuService), MediaTypes.APPLICATION_DICOM_XML_TYPE, null, ref.iuid);
    }

    private boolean isMultiframeMediaType(MediaType mediaType) {
        return mediaType.getType().equalsIgnoreCase("video")
                || mediaType.getSubtype().equalsIgnoreCase("dicom+jpeg-jpx");
    }

    private Attributes getFileAttributes(ArchiveInstanceLocator ref) {
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream(storescuService.getFile(ref).toFile());
            dis.setIncludeBulkData(IncludeBulkData.URI);
            Attributes dataset = dis.readDataset(-1, -1);
            return dataset;
        } catch (IOException e) {
            LOG.error(
                    "Unable to read file, Exception {}, using the blob for coercion - (Incomplete Coercion)",
                    e);
            return (Attributes) ref.getObject();
        } finally {
            SafeClose.close(dis);
        }
    }
}
