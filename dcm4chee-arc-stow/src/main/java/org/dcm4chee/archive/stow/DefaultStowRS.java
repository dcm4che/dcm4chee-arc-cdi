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

package org.dcm4chee.archive.stow;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.stream.StreamResult;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Attributes.Visitor;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.ws.rs.MediaTypes;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.rs.HostAECache;
import org.dcm4chee.archive.rs.HttpSource;
import org.dcm4chee.archive.rs.MetaDataPathTSTuple;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.dcm4chee.archive.web.StowRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Alessio Roselli <alessio.roselli@agfa.com>
 */
@RequestScoped
public class DefaultStowRS implements StowRS {

    public static final int TRANSFER_SYNTAX_NOT_SUPPORTED = 0xC122;
    public static final int DIFF_STUDY_INSTANCE_UID = 0xC409;
    public static final int METADATA_NOT_PARSEABLE = 0xC901;
    public static final int MISSING_BULKDATA = 0xC902;
    public static final String NOT_PARSEABLE_CUID = "1.2.40.0.13.1.15.10.99";
    public static final String NOT_PARSEABLE_IUID = "1.2.40.0.13.1.15.10.99.1";

    static Logger LOG = LoggerFactory.getLogger(DefaultStowRS.class);

    @Context
    private HttpServletRequest request;

    @Context
    private UriInfo uriInfo;

    @HeaderParam("Content-Type")
    private MediaType contentType;

    @PathParam("AETitle")
    private String aeTitle;

    @Inject
    private StoreService storeService;

    @Inject
    private Device device;

    @Inject
    private HostAECache aeCache;
    
    private ApplicationEntity ae;

    private ArchiveAEExtension arcAE;

    private String studyInstanceUID;

    private String boundary;

    private CreatorType creatorType;

    private ArrayList<MetaDataPathTSTuple> metadata;

    private HashMap<String,BulkdataPath> bulkdata;

    private String wadoURL;

    private final Attributes response = new Attributes();

    private Sequence sopSequence;

    private Sequence failedSOPSequence;

    private void init() {
        this.ae = device.getApplicationEntity(aeTitle);
        if (ae == null || !ae.isInstalled()
                || (arcAE = ae.getAEExtension(ArchiveAEExtension.class)) == null)
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);

        this.boundary = contentType.getParameters().get("boundary");
        if (boundary == null)
            throw new WebApplicationException("Missing Boundary Parameter",
                    Response.Status.BAD_REQUEST);

        if (contentType.isCompatible(MediaTypes.MULTIPART_RELATED_TYPE)) {
            String type = contentType.getParameters().get("type");
            if (type == null)
                throw new WebApplicationException("Missing Type Parameter",
                        Response.Status.BAD_REQUEST);
            try {
                MediaType rootBodyMediaType = MediaType.valueOf(type);
                if (rootBodyMediaType.isCompatible(MediaTypes.APPLICATION_DICOM_TYPE))
                    creatorType = CreatorType.BINARY;
                else if (rootBodyMediaType.isCompatible(MediaTypes.APPLICATION_DICOM_XML_TYPE)) {
                    creatorType = CreatorType.XML_BULKDATA;
                    metadata = new ArrayList<MetaDataPathTSTuple>();
                    bulkdata = new HashMap<String,BulkdataPath>();
                }
                else if (rootBodyMediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                    creatorType = CreatorType.JSON_BULKDATA;
                    metadata = new ArrayList<MetaDataPathTSTuple>();
                    bulkdata = new HashMap<String,BulkdataPath>();
                }else
                    throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
            } catch (IllegalArgumentException e) {
                throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
            }
        } else {
            creatorType = CreatorType.BINARY;
        }
        wadoURL = uriInfo.getBaseUri() + ae.getAETitle() + "/studies/";
        if (studyInstanceUID != null)
            response.setString(Tag.RetrieveURL, VR.UR, wadoURL + studyInstanceUID);
        else
            response.setNull(Tag.RetrieveURL, VR.UR);
        sopSequence = response.newSequence(Tag.ReferencedSOPSequence, 10);
    }

    @Override
    public Response storeInstances(String studyInstanceUID,
            InputStream in) throws Exception {
        this.studyInstanceUID = studyInstanceUID;
        return storeInstances(in);
    }

    private static String getHeaderParamValue(
            Map<String, List<String>> headerParams, String key) {
        List<String> list = headerParams.get(key);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }
    
    @Context
    Request req;

    @Override
    public Response storeInstances(InputStream in) throws Exception {
        String str = req.toString();
        LOG.info(str);
        init();
        final StoreSession session = storeService.createStoreSession(storeService);
        session.setSource(new HttpSource(request));
        ApplicationEntity sourceAE;
        try {
            sourceAE = aeCache.findAE(new HttpSource(request));
        } catch (ConfigurationException e) {
            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
        }
        session.setRemoteAET(sourceAE.getAETitle());
        session.setArchiveAEExtension(arcAE);
        storeService.init(session);
        try {
            new MultipartParser(boundary).parse(in, new MultipartParser.Handler() {
                
                @Override
                public void bodyPart(int partNumber, MultipartInputStream in)
                        throws IOException {
                    Map<String, List<String>> headerParams = in.readHeaderParams();
                    String transferSyntax = null;
                    LOG.info("storeInstances: Extract Part #{}{}",
                            partNumber, headerParams);
                    String contentType = getHeaderParamValue(headerParams,
                            "content-type");
                    String contentLocation = getHeaderParamValue(headerParams,
                            "content-location");
                    MediaType mediaType;
                    try {
                        mediaType = contentType == null
                                ? MediaType.TEXT_PLAIN_TYPE
                                : MediaType.valueOf(contentType);
                    } catch (IllegalArgumentException e) {
                        LOG.info("storeInstances: Ignore Part with illegal Content-Type={}",
                                contentType);
                        in.skipAll();
                        return;
                    }
                    //check for metadata transfer syntax
                    
                    if(contentLocation == null) {
                        transferSyntax = contentType.contains("transfer-syntax=")
                                ?contentType.split("transfer-syntax=")[1]:null;
                    }
                    if (!creatorType.readBodyPart(DefaultStowRS.this, session, in,
                            mediaType, contentLocation, transferSyntax)) {
                        LOG.info("storeInstances: Ignore Part with Content-Type={}",
                                mediaType);
                        in.skipAll();
                    }
                }
            });
            creatorType.storeMetadataAndBulkdata(this, session);
        } finally {
            storeService.onClose(session);
        }
        return buildResponse();
    }

    private Response buildResponse() {
//        if (sopSequence.isEmpty())
//            throw new WebApplicationException(Status.CONFLICT);

        return Response.status(
                sopSequence.isEmpty()?Status.CONFLICT:failedSOPSequence == null ? Status.OK : Status.ACCEPTED)
                .entity(new StreamingOutput() {
                    
                    @Override
                    public void write(OutputStream out) throws IOException,
                            WebApplicationException {
                        try {
                            SAXTransformer.getSAXWriter(new StreamResult(out)).write(response);
                        } catch (Exception e) {
                            throw new WebApplicationException(e);
                        }
                    }
                })
                .type(MediaTypes.APPLICATION_DICOM_XML_TYPE)
                .build();
    }

    private enum CreatorType {
        BINARY {
            @Override
            boolean readBodyPart(DefaultStowRS stowRS, StoreSession session,
                    MultipartInputStream in, MediaType mediaType,
                    String contentLocation, String transferSyntax) throws IOException {
                if (!mediaType.getType().equalsIgnoreCase("application"))
                    return false;

                if (in.isZIP()) {
                    ZipInputStream zip = new ZipInputStream(in);
                    ZipEntry zipEntry;
                    while ((zipEntry = zip.getNextEntry()) != null) {
                        if (!zipEntry.isDirectory())
                            stowRS.storeDicomObject(session, zip);
                    }
                } else {
                    stowRS.storeDicomObject(session, in);
                }
                return true;
            }
        }, 
        XML_BULKDATA {
            @Override
            boolean readBodyPart(DefaultStowRS stowRS, StoreSession session,
                    MultipartInputStream in, MediaType mediaType,
                    String contentLocation, String transferSyntax) throws IOException {
                if (mediaType.isCompatible(MediaTypes.APPLICATION_DICOM_XML_TYPE) && transferSyntax != null) {
                    stowRS.spoolMetaData(session, in, false, transferSyntax);
                    return true;
                }
                if (contentLocation != null) {
                    stowRS.spoolBulkdata(session, in, contentLocation, mediaType);
                    return true;
                }
                return false;
            }
            @Override
            void storeMetadataAndBulkdata(DefaultStowRS stowRS, StoreSession session) {
                stowRS.storeMetadataAndBulkdata(session);
            }

        },
        JSON_BULKDATA {
            @Override
            boolean readBodyPart(DefaultStowRS stowRS, StoreSession session,
                    MultipartInputStream in, MediaType mediaType,
                    String contentLocation, String transferSyntax) throws IOException {
                if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                    stowRS.spoolMetaData(session, in, true, transferSyntax);
                    return true;
                }
                if (contentLocation != null) {
                    stowRS.spoolBulkdata(session, in, contentLocation, mediaType);
                    return true;
                }
                return false;
            }
            @Override
            void storeMetadataAndBulkdata(DefaultStowRS stowRS, StoreSession session) {
                stowRS.storeMetadataAndBulkdata(session);
            }

        };

        abstract boolean readBodyPart(DefaultStowRS stowRS, StoreSession session,
                MultipartInputStream in, MediaType mediaType, String contentLocation, String transferSyntax)
                        throws IOException;

        void storeMetadataAndBulkdata(DefaultStowRS stowRS, StoreSession session) {}
    }

    private void storeDicomObject(StoreSession session, InputStream in)
            throws DicomServiceException {
        StoreContext context;
        try {
            context = storeService.createStoreContext(session);
            context.setInputStream(in);
            storeService.writeSpoolFile(context, null, in);
        } catch (DicomServiceException e) {
            if (e.getStatus() == StoreService.DATA_SET_NOT_PARSEABLE) {
                storageFailed(NOT_PARSEABLE_IUID, NOT_PARSEABLE_CUID,
                        METADATA_NOT_PARSEABLE);
                return;
            }
            throw e;
        }
        Attributes attrs = context.getOriginalAttributes();
        try {
            checkStudyInstanceUID(attrs.getString(Tag.StudyInstanceUID));
            checkTransferCapability(attrs.getString(Tag.SOPClassUID),context.getTransferSyntax());
            storeService.store(context);
            sopSequence.add(sopRef(context));
        } catch (DicomServiceException e) {
            storageFailed(
                    attrs.getString(Tag.SOPInstanceUID),
                    attrs.getString(Tag.SOPClassUID),
                    e.getStatus());
        }
    }

    private void checkStudyInstanceUID(String siuid) throws DicomServiceException {
        if (studyInstanceUID != null
                && !studyInstanceUID.equals(siuid))
            throw new DicomServiceException(DIFF_STUDY_INSTANCE_UID);
    }

    private void checkTransferCapability(String cuid, String tsuid) throws DicomServiceException {
        TransferCapability tc = ae.getTransferCapabilityFor(cuid, TransferCapability.Role.SCP);
        if (tc == null) {
            throw new DicomServiceException(org.dcm4che3.net.Status.SOPclassNotSupported);
        }
        if (!tc.containsTransferSyntax(tsuid)) {
            throw new DicomServiceException(TRANSFER_SYNTAX_NOT_SUPPORTED);
        }
    }

    private void spoolMetaData(StoreSession session, InputStream in, boolean json, String transferSyntax) throws IOException {
        if(json)
            metadata.add(new MetaDataPathTSTuple(transferSyntax,storeService.spool(session, in, ".json")));    
        else
        metadata.add(new MetaDataPathTSTuple(transferSyntax,storeService.spool(session, in, ".xml")));
    }

    private void spoolBulkdata(StoreSession session, InputStream in,
            String contentLocation, MediaType mediaType) throws IOException {
        bulkdata.put(contentLocation, 
                new BulkdataPath(storeService.spool(session, in, ".blk"), mediaType));
    }

    private void storeMetadataAndBulkdata(StoreSession session) {
        for (MetaDataPathTSTuple part: metadata) {
            storeMetadataAndBulkdata(session, part);
        }
    }

    public  Attributes parseJSON(String fname) throws Exception {
        Attributes attrs = new Attributes();
        parseJSON(fname, attrs);
        return attrs;
    }

    private  JSONReader parseJSON(String fname, Attributes attrs)
            throws IOException {
        InputStream in =new FileInputStream(
                fname);
        try {
            JSONReader reader = new JSONReader(
                    Json.createParser(new InputStreamReader(in, "UTF-8")));
            reader.readDataset(attrs);
            return reader;
        } finally {
            if (in != System.in)
                SafeClose.close(in);
        }
    }
    private void storeMetadataAndBulkdata(StoreSession session,
            MetaDataPathTSTuple part) {
        Attributes ds = null;
        if(creatorType == CreatorType.JSON_BULKDATA)
        {
            try {
                ds = parseJSON(part.getPath().toFile().getPath());
            } catch (Exception e) {
                storageFailed(NOT_PARSEABLE_IUID, NOT_PARSEABLE_CUID,
                        METADATA_NOT_PARSEABLE);
                return;
            } 
        }
        else
        {
            try {
                ds = SAXReader.parse(part.getPath().toUri().toString());
            } catch (Exception e) {
                storageFailed(NOT_PARSEABLE_IUID, NOT_PARSEABLE_CUID,
                        METADATA_NOT_PARSEABLE);
                return;
            }    
        }
        
        String iuid = ds.getString(Tag.SOPInstanceUID);
        String cuid = ds.getString(Tag.SOPClassUID);
        Attributes fmi = ds.createFileMetaInformation(part.getTransferSyntax());
        if (!resolveBulkdata(session, fmi, ds)) {
            storageFailed(iuid, cuid, MISSING_BULKDATA);
            return;
        }
        try {
            checkStudyInstanceUID(ds.getString(Tag.StudyInstanceUID));
            checkTransferCapability(cuid, fmi.getString(Tag.TransferSyntaxUID));
            StoreContext context = storeService.createStoreContext(session);
            storeService.writeSpoolFile(context,fmi,ds);
            storeService.store(context);
            sopSequence.add(sopRef(context));
        } catch (DicomServiceException e) {
            storageFailed(iuid, cuid, e.getStatus());
        }
    }

    private Attributes sopRef(StoreContext ctx) {
        StoreSession session = ctx.getStoreSession();
        Attributes attrs = ctx.getAttributes();
        Attributes sopRef = new Attributes(5);
        String cuid = attrs.getString(Tag.SOPClassUID);
        String iuid = attrs.getString(Tag.SOPInstanceUID);
        String series_iuid = attrs.getString(Tag.SeriesInstanceUID);
        String study_iuid = attrs.getString(Tag.StudyInstanceUID);
        sopRef.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
        sopRef.setString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        sopRef.setString(Tag.RetrieveURL, VR.UR, wadoURL
                + study_iuid + "/series/"
                + series_iuid + "/instances/"
                + iuid);
        Attributes coercedAttrs = ctx.getCoercedOriginalAttributes();
        if (!coercedAttrs.isEmpty()) {
            sopRef.setInt(Tag.WarningReason, VR.US,
                          org.dcm4che3.net.Status.CoercionOfDataElements);
            Attributes item = new Attributes(4);
            Sequence origAttrsSeq = sopRef.ensureSequence(
                    Tag.OriginalAttributesSequence, 1);
            origAttrsSeq.add(item);
            item.setString(Tag.ReasonForTheAttributeModification, VR.CS, "COERCE");
            item.setDate(Tag.AttributeModificationDateTime, VR.DT, new Date());
            item.setString(Tag.ModifyingSystem, VR.LO,
                    session.getStoreParam().getModifyingSystem());
            item.setString(Tag.SourceOfPreviousValues, VR.LO,
                    session.getRemoteAET());
            item.newSequence(Tag.ModifiedAttributesSequence, 1).add(
                    coercedAttrs);
        }
        return sopRef;
    }

    private void storageFailed(String iuid, String cuid, int failureReason) {
        Attributes sopRef = new Attributes(3);
        sopRef.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
        sopRef.setString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        sopRef.setInt(Tag.FailureReason, VR.US, failureReason);
        if (failedSOPSequence == null)
            failedSOPSequence =
                response.newSequence(Tag.FailedSOPSequence, 10);

        failedSOPSequence.add(sopRef);
    }

    private boolean resolveBulkdata(final StoreSession session,
            final Attributes fmi, Attributes attrs) {
        try {
            return attrs.accept(new Visitor() {
                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value) {
                    if (!(value instanceof BulkData))
                        return true;

                    String uri = ((BulkData) value).uri;
                    BulkdataPath bulkdataPath = bulkdata.get(uri);
                    if (bulkdataPath == null) {
                        LOG.info("{}: Missing Bulkdata {}", session, uri);
                        return false;
                    }

                    java.nio.file.Path path = bulkdataPath.path;
                    BulkData bd = new BulkData(
                            path.toUri().toString(),
                            0, (int) path.toFile().length(),
                            attrs.bigEndian());

                    MediaType mediaType = bulkdataPath.mediaType;
                    if (mediaType.isCompatible(
                            MediaType.APPLICATION_OCTET_STREAM_TYPE)||
                            mediaType.isCompatible(
                                    MediaTypes.APPLICATION_PDF_TYPE)) {
                        attrs.setValue(tag, vr, bd);
                        return true;
                    }
                    
                    if (!(attrs.isRoot() && tag == Tag.PixelData)) {
                        LOG.info("{}: Invalid Mediatype of Bulkdata - {}",
                                session, mediaType);
                        return false;
                    }

                    try {
                        fmi.setString(Tag.TransferSyntaxUID, VR.UI,
                                MediaTypes.transferSyntaxOf(mediaType));
                    } catch (IllegalArgumentException e) {
                        LOG.info("{}: Invalid Mediatype of Bulkdata - {}",
                                session, mediaType);
                        return false;
                    }

                    Fragments frags = attrs.newFragments(Tag.PixelData, VR.OB, 2);
                    frags.add(null);
                    frags.add(bd);
                    return true;
                }
            }, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final class BulkdataPath {
        final java.nio.file.Path path;
        final MediaType mediaType;
        BulkdataPath(java.nio.file.Path path, MediaType mediaType) {
            super();
            this.path = path;
            this.mediaType = mediaType;
        }
    }
}
