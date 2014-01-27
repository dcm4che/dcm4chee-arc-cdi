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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.lf5.util.StreamUtils;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.Attributes.Visitor;
import org.dcm4che.data.BulkData;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.io.SAXReader;
import org.dcm4che.io.SAXTransformer;
import org.dcm4che.mime.MultipartInputStream;
import org.dcm4che.mime.MultipartParser;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Device;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.TransferCapability.Role;
import org.dcm4che.net.service.DicomServiceException;
import org.dcm4che.ws.rs.MediaTypes;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.FileSystem;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@javax.ws.rs.Path("/stow/{AETitle}")
public class StowRS implements MultipartParser.Handler, StreamingOutput {

    static Logger LOG = LoggerFactory.getLogger(StowRS.class);

    public static final int TRANSFER_SYNTAX_NOT_SUPPORTED = 0xC122;
    public static final int DIFF_STUDY_INSTANCE_UID = 0xC409;
    public static final int METADATA_NOT_PARSEABLE = 0xC901;
    public static final int MISSING_BULKDATA = 0xC902;
    public static final String NOT_PARSEABLE_CUID = "1.2.40.0.13.1.15.10.99";
    public static final String NOT_PARSEABLE_IUID = "1.2.40.0.13.1.15.10.99.1";

    @Context
    private HttpServletRequest request;

    @Context
    private UriInfo uriInfo;

    @Inject
    private Device device;

    private ApplicationEntity ae;

    private ArchiveAEExtension arcAE;

    @Inject
    private StoreService storeService;

    private String studyInstanceUID;

    private Creator creator;
    
    private FileSystem fileSystem;

    private Path spoolDirectory;

    private MessageDigest digest;

    private final Attributes response = new Attributes();

    private Sequence sopSequence;

    private Sequence failedSOPSequence;

    private ArrayList<PathWithObject<byte[]>> files =
            new ArrayList<PathWithObject<byte[]>>();
 
    private HashMap<String,PathWithObject<String>> bulkdata =
            new HashMap<String,PathWithObject<String>>();

    private String wadoURL;

    private HttpServletRequestSource source;

    public void setAETitle(@PathParam("AETitle") String aet) {
        ae = device.getApplicationEntity(aet);
        if (ae == null || !ae.isInstalled()
                || (arcAE = ae.getAEExtension(ArchiveAEExtension.class)) == null) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
    }

    @POST
    @javax.ws.rs.Path("/studies")
    @Consumes({"multipart/related","multipart/form-data"})
    public Response storeInstances(
            @HeaderParam("Content-Type") MediaType contentType,
            InputStream in) throws Exception {
        return storeInstances(null, contentType, in);
    }

    @POST
    @javax.ws.rs.Path("/studies/{StudyInstanceUID}")
    @Consumes("multipart/related")
    public Response storeInstances(
            @PathParam("StudyInstanceUID") String studyInstanceUID,
            @HeaderParam("Content-Type") MediaType contentType,
            InputStream in) throws Exception {
        this.studyInstanceUID = studyInstanceUID;
        String boundary = contentType.getParameters().get("boundary");
        if (boundary == null)
            throw new WebApplicationException("Missing Boundary Parameter",
                    Response.Status.BAD_REQUEST);

        creator = Creator.valueOf(contentType);
        source = new HttpServletRequestSource(request);
        fileSystem = storeService.selectFileSystem(source, arcAE);
        spoolDirectory = Files.createTempDirectory(
                fileSystem.getPath().resolve(arcAE.getSpoolDirectoryPath()),
                null);
        digest = arcAE.getMessageDigest();

        try {
            new MultipartParser(boundary).parse(in, this);
            initResponse();
            for (PathWithObject<byte[]> file : files) {
                creator.store(this, file);
            }
            return buildResponse();
        } finally {
            deleteSpoolDirectory();
        }
    }

    @Override
    public void bodyPart(int partNumber, MultipartInputStream in)
            throws IOException {
        Map<String, List<String>> headerParams = in.readHeaderParams();
        LOG.info("storeInstances: Extract Part #{}{}",
                partNumber, headerParams);
        String contentType = getHeaderParamValue(headerParams,
                "content-type");
        String contentLocation = getHeaderParamValue(headerParams,
                "content-location");
        try {
            MediaType mediaType = contentType == null
                    ? MediaType.TEXT_PLAIN_TYPE
                    : MediaType.valueOf(contentType);
            if (!creator.spool(this, mediaType, contentLocation, in)) {
                LOG.info("storeInstances: Ignore Part with Content-Type={}",
                        mediaType);
                in.skipAll();
            }
        } catch (IllegalArgumentException e) {
            LOG.info("storeInstances: Ignore Part with illegal Content-Type={}",
                    contentType);
            in.skipAll();
        }
    }

    private void deleteSpoolDirectory() throws IOException {
        for (Path path : Files.newDirectoryStream(spoolDirectory))
            Files.delete(path);
        Files.delete(spoolDirectory);
    }

    private static String getHeaderParamValue(
            Map<String, List<String>> headerParams, String key) {
        List<String> list = headerParams.get(key);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    private enum Creator {
        DICOM {
            @Override
            boolean spool(StowRS stowRS, MediaType mediaType,
                    String contentLocation, MultipartInputStream in)
                    throws IOException {
                if (mediaType.getType().equalsIgnoreCase("application")) {
                    if (in.isZIP()) {
                        ZipInputStream zip = new ZipInputStream(in);
                        ZipEntry zipEntry;
                        while ((zipEntry = zip.getNextEntry()) != null) {
                            if (!zipEntry.isDirectory())
                                stowRS.spoolDicomObject(zip);
                        }
                    } else {
                        stowRS.spoolDicomObject(in);
                    }
                    return true;
                }
                return false;
            }

            @Override
            void store(StowRS stowRS, PathWithObject<byte[]> file) {
                stowRS.storeDicomObject(file.path, file.object);
            }
        },
        METADATA_BULKDATA {
            @Override
            boolean spool(StowRS stowRS, MediaType mediaType,
                    String contentLocation, MultipartInputStream in)
                    throws IOException {
                if (mediaType.isCompatible(MediaTypes.APPLICATION_DICOM_XML_TYPE)) {
                    stowRS.spoolMetaData(in);
                    return true;
                }
                if (contentLocation != null) {
                    try {
                        stowRS.spoolBulkdata(
                                MediaTypes.transferSyntaxOf(mediaType), 
                                contentLocation, in);
                        return true;
                    } catch (IllegalArgumentException e) {
                        // skip unsupported MediaType
                    }
                }
                return false;
            }

            @Override
            void store(StowRS stowRS, PathWithObject<byte[]> file) {
                stowRS.storeMetadataAndBulkdata(file);
            }
        };

        static Creator valueOf(MediaType contentType) {
            String type = contentType.getParameters().get("type");
            if (type == null)
                throw new WebApplicationException("Missing Type Parameter",
                        Response.Status.BAD_REQUEST);

            try {
                MediaType rootBodyMediaType = MediaType.valueOf(type);
                if (rootBodyMediaType.isCompatible(MediaTypes.APPLICATION_DICOM_TYPE))
                    return DICOM;
                if (rootBodyMediaType.isCompatible(MediaTypes.APPLICATION_DICOM_XML_TYPE))
                    return METADATA_BULKDATA;
                throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
            } catch (IllegalArgumentException e) {
                throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
            }
         }

        abstract boolean spool(StowRS stowRS, MediaType mediaType,
                String contentLocation, MultipartInputStream in)
                throws IOException;

        abstract void store(StowRS stowRS, PathWithObject<byte[]> file);
    }

    private void spoolDicomObject(InputStream in) throws IOException {
        Path path = spool(in, ".dcm", digest);
        files.add(new PathWithObject<byte[]>(path, digest()));
    }

    private byte[] digest() {
        return digest == null ? digest.digest() : null;
    }

    private void spoolMetaData(InputStream in) throws IOException {
        Path path = spool(in, ".xml", null);
        files.add(new PathWithObject<byte[]>(path, null));
    }

    private void spoolBulkdata(String tsuid, String contentLocation,
            InputStream in) throws IOException {
        Path path = spool(in, ".blk", null);
        bulkdata.put(contentLocation, new PathWithObject<String>(path, tsuid));
    }

    private Path spool(InputStream in, String suffix, MessageDigest digest)
            throws IOException {
        Path path = Files.createTempFile(spoolDirectory, null, suffix);
        try (OutputStream out = newDigestOutputStream(path)) {
            StreamUtils.copy(in, out);
        }
        return path;
    }

    private OutputStream newDigestOutputStream(Path path) throws IOException {
        OutputStream out = Files.newOutputStream(path);
        if (digest == null)
            return out;

        digest.reset();
        return new DigestOutputStream(out, digest);
    }

    private void storeDicomObject(Path file, byte[] digest) {
        StoreContext storeContext = storeService.createStoreContext(
                storeService, source, arcAE, fileSystem, file, digest);
        String iuid = NOT_PARSEABLE_IUID;
        String cuid = NOT_PARSEABLE_CUID;
        try {
            storeService.parseAttributes(storeContext);
            checkStudyInstanceUID(storeContext);
            checkTransferCapability(storeContext);
            storeService.coerceAttributes(storeContext);
            storeService.moveFile(storeContext);
            storeService.updateDB(storeContext);
            sopSequence.add(sopRef(storeContext));
        } catch (DicomServiceException e) {
            storageFailed(iuid, cuid, e.getStatus());
        }
    }

    private void storeMetadataAndBulkdata(PathWithObject<byte[]> file) {
        Attributes ds;
        String iuid, cuid;
        try {
            ds = SAXReader.parse(file.path.toUri().toString());
            iuid = ds.getString(Tag.SOPInstanceUID);
            cuid = ds.getString(Tag.SOPClassUID);
        } catch (Exception e) {
            storageFailed(NOT_PARSEABLE_IUID, NOT_PARSEABLE_CUID,
                    METADATA_NOT_PARSEABLE);
            return;
        }
        String[] tsuids = { UID.ExplicitVRLittleEndian };
        if (!resolveBulkdata(ds, tsuids)) {
            storageFailed(iuid, cuid, MISSING_BULKDATA);
            return;
        }
        Path path;
        try {
            path = spool(ds.createFileMetaInformation(tsuids[0]), ds);
        } catch (IOException e) {
            storageFailed(iuid, cuid, org.dcm4che.net.Status.ProcessingFailure);
            return;
        }
        storeDicomObject(path, digest());
    }

    private Attributes sopRef(StoreContext ctx) {
        Attributes sopRef = new Attributes(5);
        sopRef.setString(Tag.ReferencedSOPClassUID, VR.UI, ctx.getSOPClassUID());
        sopRef.setString(Tag.ReferencedSOPInstanceUID, VR.UI, ctx.getSOPInstanceUID());
        sopRef.setString(Tag.RetrieveURL, VR.UT, wadoURL
                + ctx.getStudyInstanceUID() + "/series/"
                + ctx.getSeriesInstanceUID() + "/instances/"
                + ctx.getSOPInstanceUID());
        if (!ctx.getCoercedAttributes().isEmpty()) {
            sopRef.setInt(Tag.WarningReason, VR.US,
                          org.dcm4che.net.Status.CoercionOfDataElements);
            if (arcAE.isStoreOriginalAttributes()) {
                Sequence seq = ctx.getAttributes()
                        .getSequence(Tag.OriginalAttributesSequence);
                sopRef.newSequence(Tag.OriginalAttributesSequence, 1)
                    .add(new Attributes(seq.get(seq.size()-1)));
            }
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
                response.newSequence(Tag.FailedSOPSequence, files.size());

        failedSOPSequence.add(sopRef);
    }

    private void checkStudyInstanceUID(StoreContext ctx) throws DicomServiceException {
        if (studyInstanceUID != null
                && !studyInstanceUID.equals(ctx.getStudyInstanceUID()))
            throw new DicomServiceException(DIFF_STUDY_INSTANCE_UID);
    }

    private void checkTransferCapability(StoreContext ctx) throws DicomServiceException {
        TransferCapability tc = ae.getTransferCapabilityFor(ctx.getSOPClassUID(), Role.SCP);
        if (tc == null) {
            throw new DicomServiceException(org.dcm4che.net.Status.SOPclassNotSupported);
        }
        if (!tc.containsTransferSyntax(ctx.getTransferSyntax())) {
            throw new DicomServiceException(TRANSFER_SYNTAX_NOT_SUPPORTED);
        }
    }

    private Path spool(Attributes fmi, Attributes ds) throws IOException {
        Path path = Files.createTempFile(spoolDirectory, null, ".dcm");
        try (
            DicomOutputStream out = new DicomOutputStream(
                    new BufferedOutputStream(newDigestOutputStream(path)),
                    UID.ExplicitVRLittleEndian)
        ) {
            out.writeDataset(fmi, ds);
        }
        return path;
    }

    private boolean resolveBulkdata(Attributes attrs, final String[] tsuids) {
        final boolean[] resolved = { true };
        attrs.accept(new Visitor() {
            @Override
            public void visit(Attributes attrs, int tag, VR vr, Object value) {
                if (value instanceof Sequence) {
                    Sequence sq = (Sequence) value;
                    for (Attributes item : sq)
                        resolveBulkdata(item, tsuids);
                } else if (value instanceof BulkData) {
                    PathWithObject<String> pathWithTS = bulkdata.get(((BulkData) value).uri);
                    if (pathWithTS == null) {
                        resolved[0] = false;
                    } else {
                        Path path = pathWithTS.path;
                        String tsuid = pathWithTS.object;
                        BulkData bd = new BulkData(
                                path.toUri().toString(),
                                0, (int) path.toFile().length(),
                                attrs.bigEndian());
                        if (tsuid.equals(UID.ExplicitVRLittleEndian)) {
                            attrs.setValue(tag, vr, bd);
                        } else {
                            Fragments frags = attrs.newFragments(tag, vr, 2);
                            frags.add(null);
                            frags.add(bd);
                            tsuids[0] = tsuid;
                        }
                    }
                }
            }
        });
        return resolved[0];
    }

    private void initResponse() {
        wadoURL = uriInfo.getBaseUri() + "wado/" + ae.getAETitle() + "/studies/";
        if (studyInstanceUID != null)
            response.setString(Tag.RetrieveURL, VR.UT, wadoURL + studyInstanceUID);
        else
            response.setNull(Tag.RetrieveURL, VR.UT);
        sopSequence = response.newSequence(Tag.ReferencedSOPSequence, files.size());
    }

    private Response buildResponse() {
        if (sopSequence.isEmpty())
            throw new WebApplicationException(Status.CONFLICT);

        return Response.status(
                failedSOPSequence == null ? Status.OK : Status.ACCEPTED)
                .entity(this)
                .type(MediaTypes.APPLICATION_DICOM_XML_TYPE)
                .build();
    }

    @Override
    public void write(OutputStream out) throws IOException,
            WebApplicationException {
        try {
            SAXTransformer.getSAXWriter(new StreamResult(out)).write(response);
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private static final class PathWithObject<T> {

        public final Path path;
        public final T object;

        public PathWithObject(Path path, T object) {
            this.path = path;
            this.object = object;
        }

    }

}
