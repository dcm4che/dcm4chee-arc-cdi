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

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.SAXWriter;
import org.dcm4che3.io.TemplatesCache;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.service.BasicCStoreSCUResp;
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
import org.dcm4chee.task.ImageProcessingTaskTypes;
import org.dcm4chee.task.MemoryConsumingTask;
import org.dcm4chee.task.TaskType;
import org.dcm4chee.task.WeightWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Service implementing DICOM PS 3.18-2011 (WADO), URI based communication.
 * 
 * @see <a href=
 *      "http://medical.nema.org/medical/dicom/current/output/html/part18.html">
 *      DICOM PS3.18 2015c - Web Services</a>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@RequestScoped
@Path("/wado/{AETitle}")
public class WadoURI extends Wado {

    private static final Logger LOG = LoggerFactory.getLogger(WadoURI.class);

    @Inject
    private Event<RetrieveAfterSendEvent> retrieveEvent;

    @Inject
    private HostAECache hostAECache;

    @Inject
    private WeightWatcher weightWatcher;

    private CStoreSCUContext context;

    private static final int STATUS_NOT_IMPLEMENTED = 501;

    public enum Anonymize {
        yes
    }

    public enum Annotation {
        patient, technique
    }

    public static final class Strings {
        final String[] values;

        public Strings(String s) {
            values = StringUtils.split(s, ',');
        }
    }

    public static final class ContentTypes {
        final MediaType[] values;

        public ContentTypes(String s) {
            String[] ss = StringUtils.split(s, ',');
            values = new MediaType[ss.length];
            for (int i = 0; i < ss.length; i++)
                values[i] = MediaType.valueOf(ss[i]);
        }
    }

    public static final class Annotations {
        final Annotation[] values;

        public Annotations(String s) {
            String[] ss = StringUtils.split(s, ',');
            values = new Annotation[ss.length];
            for (int i = 0; i < ss.length; i++)
                values[i] = Annotation.valueOf(ss[i]);
        }
    }

    public static final class Region {
        final double left;
        final double top;
        final double right;
        final double bottom;

        public Region(String s) {
            String[] ss = StringUtils.split(s, ',');
            if (ss.length != 4)
                throw new IllegalArgumentException(s);
            left = Double.parseDouble(ss[0]);
            top = Double.parseDouble(ss[1]);
            right = Double.parseDouble(ss[2]);
            bottom = Double.parseDouble(ss[3]);
            if (left < 0. || right > 1. || top < 0. || bottom > 1.
                    || left >= right || top >= bottom)
                throw new IllegalArgumentException(s);
        }
    }

    @Context
    private HttpServletRequest request;

    @Context
    private HttpHeaders headers;

    @QueryParam("requestType")
    private String requestType;

    @QueryParam("studyUID")
    private String studyUID;

    @QueryParam("seriesUID")
    private String seriesUID;

    @QueryParam("objectUID")
    private String objectUID;

    @QueryParam("contentType")
    private ContentTypes contentType;

    @QueryParam("charset")
    private Strings charset;

    @QueryParam("anonymize")
    private Anonymize anonymize;

    @QueryParam("annotation")
    private Annotations annotation;

    @QueryParam("rows")
    private int rows;

    @QueryParam("columns")
    private int columns;

    @QueryParam("region")
    private Region region;

    @QueryParam("windowCenter")
    private float windowCenter;

    @QueryParam("windowWidth")
    private float windowWidth;

    @QueryParam("frameNumber")
    private int frameNumber;

    @QueryParam("imageQuality")
    private int imageQuality;

    @QueryParam("presentationUID")
    private String presentationUID;

    @QueryParam("presentationSeriesUID")
    private String presentationSeriesUID;

    @QueryParam("transferSyntax")
    private List<String> transferSyntax;
    
    @QueryParam("overlays")
    private boolean overlays;

    @Inject
    private FetchForwardService fetchForwardService;

    @GET
    public Response retrieve() throws WebApplicationException {

        List<ArchiveInstanceLocator> insts = new ArrayList<ArchiveInstanceLocator>();
        List<ArchiveInstanceLocator> instswarning = new ArrayList<ArchiveInstanceLocator>();
        List<ArchiveInstanceLocator> instscompleted = new ArrayList<ArchiveInstanceLocator>();
        List<ArchiveInstanceLocator> instsfailed = new ArrayList<ArchiveInstanceLocator>();

        try {
            
            ApplicationEntity sourceAE;
            try {
                sourceAE = hostAECache.findAE(new HttpSource(request));
            } catch (ConfigurationException e) {
                throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
            }

            context = new CStoreSCUContext(arcAE.getApplicationEntity(), sourceAE, ServiceType.WADOSERVICE);

            checkRequest();

            final List<ArchiveInstanceLocator> ref = retrieveService.calculateMatches(studyUID, seriesUID, objectUID, queryParam, false);

            if (ref == null || ref.size() == 0)
                throw new WebApplicationException(Status.NOT_FOUND);
            else
                insts.addAll(ref);

            if (ref.size() != 1) {
                instsfailed.addAll(ref);
                throw new WebApplicationException(Status.BAD_REQUEST);
            }
            //internal
            ArchiveInstanceLocator instance = null;
            Attributes attrs = null;
            Response resp = null;
            
            if (ref.get(0).getStorageSystem() != null) {
                instance = ref.get(0);
                attrs = (Attributes) instance.getObject();
                resp = retrieve(instscompleted, instsfailed, ref, instance, attrs);
            }
            //external
            else {
                //try to forward by redirecting HTTP request
                ApplicationEntity forwardingAE = null;
                if ((forwardingAE = fetchForwardService.getPrefersForwardingAE(aetitle, ref)) != null) {
                    Response redirectResponse = fetchForwardService.redirectRequest(forwardingAE, request.getQueryString());
                    if( redirectResponse != null && redirectResponse.getStatus() != Status.CONFLICT.getStatusCode()) {
                        return redirectResponse;
                    }
                }
                //fetch
                FetchForwardCallBack fetchCallBack = new FetchForwardCallBack() {
                    
                    @Override
                    public void onFetch(Collection<ArchiveInstanceLocator> instances,
                            BasicCStoreSCUResp basicCStoreSCUresp) {
                        ref.clear();
                        ref.addAll(instances);
                    }
                };

                instsfailed = fetchForwardService.fetchForward(aetitle, ref, fetchCallBack, fetchCallBack);
                instance = ref.get(0);
                attrs = (Attributes) instance.getObject();
                resp = retrieve(instscompleted, instsfailed, ref, instance, attrs);
            }
           
            if (resp == null) {
                throw new WebApplicationException(STATUS_NOT_IMPLEMENTED);
            } else {
                return resp;
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

    private Response retrieve(List<ArchiveInstanceLocator> instscompleted,
            List<ArchiveInstanceLocator> instsfailed,
            List<ArchiveInstanceLocator> ref, ArchiveInstanceLocator instance,
            Attributes attrs) {
        if(!instsfailed.isEmpty())
            throw new WebApplicationException(Status.CONFLICT);
        MediaType mediaType = selectMediaType(instance.tsuid,
                instance.cuid, attrs);
        if (!isAccepted(mediaType)) {
            instsfailed.addAll(ref);
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);
        }

        if (mediaType == MediaTypes.APPLICATION_DICOM_TYPE) {
            instscompleted.addAll(ref);
            return retrieveNativeDicomObject(instance, attrs);
        }

        if (mediaType == MediaTypes.IMAGE_JPEG_TYPE
                || mediaType == MediaTypes.IMAGE_PNG_TYPE
                || mediaType == MediaTypes.IMAGE_GIF_TYPE) {
            instscompleted.addAll(ref);
            return retrieveImage(instance, attrs, mediaType);
        }

        if (mediaType.isCompatible(MediaTypes.APPLICATION_PDF_TYPE)
                || mediaType.isCompatible(MediaType.TEXT_XML_TYPE)
//                  || mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE)
                || mediaType.isCompatible(MediaTypes.TEXT_RTF_TYPE)) {
            instscompleted.addAll(ref);
            return retrievePDFXMLOrText(mediaType, instance);
        }
        if (mediaType == MediaType.TEXT_HTML_TYPE) {
            try {
                instscompleted.addAll(ref);
                return retrieveSRHTML(instance, attrs);
            } catch (TransformerConfigurationException e) {
                return retrieveNativeDicomObject(instance, attrs);
            }
        }
        return null;
    }

    private boolean isSupportedSR(String cuid) {
        for (String c_uid : arcAE.getWadoSupportedSRClasses()) {
            if (c_uid.equals(cuid))
                return true;
        }
        return false;
    }

    private void checkRequest() throws WebApplicationException {
        ApplicationEntity ae = device.getApplicationEntity(aetitle);
        if (ae == null || !ae.isInstalled())
            throw new WebApplicationException(Status.SERVICE_UNAVAILABLE);

        if (!"WADO".equals(requestType))
            throw new WebApplicationException(Status.BAD_REQUEST);
        if (studyUID == null || seriesUID == null || objectUID == null)
            throw new WebApplicationException(Status.BAD_REQUEST);

        boolean applicationDicom = false;
        if (contentType != null) {
            for (MediaType mediaType : contentType.values) {
                if (!isAccepted(mediaType))
                    throw new WebApplicationException(Status.BAD_REQUEST);
                if (mediaType.isCompatible(MediaTypes.APPLICATION_DICOM_TYPE))
                    applicationDicom = true;
            }
        }
        if (applicationDicom) {
            if (annotation != null || rows != 0 || columns != 0 || region != null || windowCenter != 0 || windowWidth != 0 || frameNumber != 0 || imageQuality != 0 || presentationUID != null || presentationSeriesUID != null) {
                throw new WebApplicationException(Status.BAD_REQUEST);
            }
        } else {
            if (anonymize != null || !transferSyntax.isEmpty() || rows < 0 || columns < 0 || imageQuality < 0 || imageQuality > 100 || presentationUID != null && presentationSeriesUID == null) {
                throw new WebApplicationException(Status.BAD_REQUEST);
            }
        }
    }

    private boolean isAccepted(MediaType mediaType) {
        for (MediaType accepted : headers.getAcceptableMediaTypes())
            if (mediaType.isCompatible(accepted))
                return true;
        return false;
    }

    private Response retrieveSRHTML(final ArchiveInstanceLocator ref, final Attributes attrs)
            throws TransformerConfigurationException {
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException {
                BufferedOutputStream bout = new BufferedOutputStream(out);
                Templates templates = null;
                SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
                TransformerHandler th = null;
                try {
                    String uri = StringUtils.replaceSystemProperties(
                                    arcAE.getWadoSRTemplateURI());
                    templates = TemplatesCache.getDefault().get(uri);
                } catch (TransformerConfigurationException e) {
                    
                    LOG.error("Unable to apply transformation for SR - check archive AE Configuration",e);
                    
                }
                try {
                    th = factory.newTransformerHandler(templates);
                } catch (TransformerConfigurationException e2) {
                    LOG.error("Error configuring transformer handler - reason {}",e2);
                }
                Transformer tr = th.getTransformer();
                String wado = request.getRequestURL().toString();
                tr.setParameter("wadoURL", wado);
                SAXWriter w = null;
                th.setResult(new StreamResult(bout));
                w = new SAXWriter(th);
                Attributes data = readAttributes(ref);
                data.addAll(attrs);
                try {
                    w.write(data);
                } catch (SAXException e) {
                    LOG.error("Unable to write SR using defined template - reason {}",e);
                }
            }
        }, MediaType.TEXT_HTML_TYPE).build();
    }

    private Response retrievePDFXMLOrText(MediaType mediaType,
            final ArchiveInstanceLocator ref) {

        return Response.ok(new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException,
                    WebApplicationException {
                Attributes attrs = readAttributes(ref);
                try (BufferedOutputStream bOut = new BufferedOutputStream(out)) {
                    bOut.write(attrs.getBytes(Tag.EncapsulatedDocument));
                }
            }
        }, mediaType).build();
    }

    private Attributes readAttributes(ArchiveInstanceLocator ref) throws IOException {
        for (;;)
            try (DicomInputStream in = new DicomInputStream(
                    storescuService.getFile(ref).toFile())) {
                return  in.readDataset(-1, -1);
            } catch (IOException e) {
                LOG.info("Failed to read Data Set with iuid={} from {}@{}",
                        ref.iuid, ref.getFilePath(), ref.getStorageSystem(), e);
                ref = ref.getFallbackLocator();
                if (ref == null) {
                    throw e;
                }
                LOG.info("Try read Data Set from alternative location");
            }
    }

    private Response retrieveNativeDicomObject(ArchiveInstanceLocator ref,
            Attributes attrs) {
        String tsuid = selectTransferSyntax(ref.tsuid);
        MediaType mediaType = MediaType
                .valueOf("application/dicom;transfer-syntax=" + tsuid);
        return Response.ok(new DicomObjectOutput(ref, attrs, tsuid, context, storescuService, weightWatcher), mediaType)
                .build();
    }

    private String selectTransferSyntax(String tsuid) {

        // no need of decompression
        if (transferSyntax.contains("*") || transferSyntax.contains(tsuid))
            return tsuid;

        // cannot decompress to required ts
        if (!ImageReaderFactory.canDecompress(tsuid))
            return tsuid;

        // select first requested uncompressed ts
        for (String singleTransferSyntax : transferSyntax)
            if (TransferSyntaxType.forUID(singleTransferSyntax).equals(TransferSyntaxType.NATIVE))
                return singleTransferSyntax;

        //default
        return UID.ExplicitVRLittleEndian;
    }

    private Response retrieveImage(ArchiveInstanceLocator ref, final Attributes attrs, final MediaType mediaType) {
        ImageInputStream iis = null;
        ImageReader reader = null;
        ImageWriter imageWriter = null;
        try {
            DicomImageReadParam param;
            try {
                iis = getImageInputStream(ref);

                reader = getDicomImageReader();

                reader.setInput(iis);
                DicomMetaData metaData = (DicomMetaData) reader.getStreamMetadata();
                metaData.getAttributes().addAll(attrs);

                param = (DicomImageReadParam) reader.getDefaultReadParam();

                init(param);
            } catch (IOException e) {
                throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
            }

            imageWriter = ImageWriterFactory.getImageWriterForMimeType(mediaType.toString());

            ImageWriteParam imageWriteParam = getImageWriterParam(imageWriter);

            int numberOfFrames = attrs.getInt(Tag.NumberOfFrames, 1);

            int frameNumberZeroBased;
            if (numberOfFrames == 1) { // single frame
                if (frameNumber < 0 || frameNumber > 1)
                    throw new WebApplicationException(Status.NOT_FOUND);

                frameNumberZeroBased = 0; // first frame
            } else { // multi frame
                if (frameNumber != 0) {
                    if (frameNumber < 0 || frameNumber > numberOfFrames)
                        throw new WebApplicationException(Status.NOT_FOUND);

                    frameNumberZeroBased = frameNumber - 1;
                } else {
                    if (mediaType == MediaTypes.IMAGE_GIF_TYPE) // animated GIF case
                        frameNumberZeroBased = -1; // all frames
                    else
                        frameNumberZeroBased = 0; // first frame
                }
            }

            RenderedImageOutput renderedImageOutput = new RenderedImageOutput(reader, param, rows, columns, frameNumberZeroBased, imageWriter, imageWriteParam);

            StreamingOutputWrapper wrapper = new StreamingOutputWrapper(renderedImageOutput, iis);

            // make sure the stream/reader/writer is not closed early, but later on when doing the streaming
            iis = null;
            reader = null;
            imageWriter = null;

            return Response.ok(wrapper, mediaType).build();

        } finally {
            if (imageWriter != null)
                imageWriter.dispose();
            if (reader != null)
                reader.dispose();
            if (iis != null) {
                try {
                    iis.close();
                } catch (IOException e) {
                    LOG.error("Error closing input stream", e);
                }
            }
        }
    }

    private ImageInputStream getImageInputStream(ArchiveInstanceLocator ref) throws IOException {
        ImageInputStream iis = null;
        for (; ; ) {
            try {
                iis = createImageInputStream(ref);
                break;
            } catch (IOException e) {
                SafeClose.close(iis);
                LOG.info("Failed to read image with iuid={} from {}@{}", ref.iuid, ref.getFilePath(), ref.getStorageSystem(), e);
                ref = ref.getFallbackLocator();
                if (ref == null) {
                    throw e;
                }
                LOG.info("Try read image from alternative location");
            }
        }
        return iis;
    }

    private class StreamingOutputWrapper implements StreamingOutput {

        private final RenderedImageOutput renderedImageOutput;
        private final ImageInputStream inputStream;

        public StreamingOutputWrapper(RenderedImageOutput renderedImageOutput, ImageInputStream inputStream) {
            this.renderedImageOutput = renderedImageOutput;
            this.inputStream = inputStream;
        }

        @Override
        public void write(OutputStream output) throws IOException {
            // we wrap the RenderedImageOutput for two reasons:
            // 1) we need to close the input stream in a finally
            // 2) we want to run it through the WeightWatcher

            try {
                weightWatcher.execute(new RenditionTask(renderedImageOutput, output));
            } catch (Exception e) {
                if (e instanceof IOException)
                    throw (IOException) e;
                else if (e instanceof RuntimeException)
                    throw (RuntimeException) e;
                else
                    throw new RuntimeException(e); // should not happen
            } finally {
                inputStream.close();
            }
        }
    }

    private static class RenditionTask implements MemoryConsumingTask<Void> {
        private final RenderedImageOutput renderedImageOutput;
        private final OutputStream output;

        public RenditionTask(RenderedImageOutput renderedImageOutput, OutputStream output) {
            this.renderedImageOutput = renderedImageOutput;
            this.output = output;
        }

        @Override
        public TaskType getTaskType() {
            return ImageProcessingTaskTypes.TRANSCODE_OUTGOING;
        }

        @Override
        public long getEstimatedWeight() {
            try {
                return renderedImageOutput.getEstimatedNeededMemory();
            } catch (IOException e) {
                // shouldn't happen in this case, as the dicom stream metadata was already read before
                throw new RuntimeException(e);
            }
        }

        @Override
        public Void call() throws IOException {
            renderedImageOutput.write(output);
            return null;
        }
    }

    private ImageInputStream createImageInputStream(ArchiveInstanceLocator ref) throws IOException {
        return ImageIO.createImageInputStream(storescuService.getFile(ref).toFile());
    }

    private static ImageReader getDicomImageReader() {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("DICOM");
        if (!readers.hasNext()) {
            ImageIO.scanForPlugins();
            readers = ImageIO.getImageReadersByFormatName("DICOM");
        }
        return readers.next();
    }

    private void init(DicomImageReadParam param)
            throws WebApplicationException, IOException {

        if (!request.getQueryString().contains("overlays"))
            overlays = arcAE.isWadoOverlayRendering();
        //set overlay activation mask
        param.setOverlayActivationMask(overlays ? 0xf : 0x0);
        param.setWindowCenter(windowCenter);
        param.setWindowWidth(windowWidth);
        if (presentationUID != null) {
            List<ArchiveInstanceLocator> ref = retrieveService
                    .calculateMatches(studyUID, presentationSeriesUID,
                            presentationUID, queryParam, false);
            if (ref == null || ref.size() == 0)
                throw new WebApplicationException(Status.NOT_FOUND);

            if (ref.size() != 1)
                throw new WebApplicationException(Status.BAD_REQUEST);

            param.setPresentationState(readAttributes(ref.get(0)));
        }
    }

    private ImageWriteParam getImageWriterParam(ImageWriter imageWriter) {
        ImageWriteParam imageWriteParam = imageWriter
                .getDefaultWriteParam();
        if (imageQuality > 0) {
            imageWriteParam
                    .setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(imageQuality / 100f);
        }
        return imageWriteParam;
    }

    /**
     * Returns the first media type that is supported and compatible with the contentType
     * request parameter. If none is compatible an exception is thrown.
     */
    private MediaType selectMediaType(String transferSyntaxUID,
            String sopClassUID, Attributes attrs) {

        List<MediaType> supportedMediaTypes = supportedMediaTypesOf(
                transferSyntaxUID, sopClassUID, attrs);

        if (contentType != null) {
            for (MediaType requestedType : contentType.values)
                for (MediaType supportedType : supportedMediaTypes)
                    if (requestedType.isCompatible(supportedType))
                        return supportedType;
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);
        } else {
            return supportedMediaTypes.get(0);
        }
    }

    public List<MediaType> supportedMediaTypesOf(String transferSyntaxUID,
            String sopClassUID, Attributes attrs) {
        List<MediaType> list = new ArrayList<MediaType>(4);
        if (attrs.contains(Tag.BitsAllocated)) {
            if (attrs.getInt(Tag.NumberOfFrames, 1) > 1) {
                list.add(MediaTypes.APPLICATION_DICOM_TYPE);
                if (UID.MPEG2.equals(transferSyntaxUID)
                        || UID.MPEG2MainProfileHighLevel
                                .equals(transferSyntaxUID))
                    list.add(MediaTypes.VIDEO_MPEG_TYPE);
                else if (UID.MPEG4AVCH264HighProfileLevel41
                        .equals(transferSyntaxUID)
                        || UID.MPEG4AVCH264BDCompatibleHighProfileLevel41
                                .equals(transferSyntaxUID))
                    list.add(MediaTypes.VIDEO_MP4_TYPE);
                else{
                    list.addAll(getRenderedImageMediaTypes());
                }
            } else {
                list.addAll(getRenderedImageMediaTypes());
                list.add(MediaTypes.APPLICATION_DICOM_TYPE);
            }
        } else if (isSupportedSR(sopClassUID)) {
            list.add(MediaType.TEXT_HTML_TYPE);
            list.add(MediaType.TEXT_PLAIN_TYPE);
            list.add(MediaTypes.APPLICATION_DICOM_TYPE);
        } else {
            list.add(MediaTypes.APPLICATION_DICOM_TYPE);
            if (UID.EncapsulatedPDFStorage.equals(sopClassUID))
                list.add(MediaTypes.APPLICATION_PDF_TYPE);
            else if (UID.EncapsulatedCDAStorage.equals(sopClassUID))
                list.add(MediaType.TEXT_XML_TYPE);
            else{
                String encapsulatedMimeType = attrs.getString(Tag.MIMETypeOfEncapsulatedDocument);
                if (encapsulatedMimeType != null) {
                    MediaType mimeType = MediaType.valueOf(encapsulatedMimeType);
                    list.add(mimeType);
                }
            }
        }
        return list;
    }

    public List<MediaType> getRenderedImageMediaTypes() {
        return Arrays.asList(MediaTypes.IMAGE_JPEG_TYPE, MediaTypes.IMAGE_GIF_TYPE, MediaTypes.IMAGE_PNG_TYPE);
    }

}
