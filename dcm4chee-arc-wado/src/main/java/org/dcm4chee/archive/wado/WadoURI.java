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

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.image.PaletteColorModel;
import org.dcm4che.image.PixelAspectRatio;
import org.dcm4che.imageio.codec.ImageReaderFactory;
import org.dcm4che.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che.imageio.plugins.dcm.DicomMetaData;
import org.dcm4che.imageio.stream.OutputStreamAdapter;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Device;
import org.dcm4che.net.QueryOption;
import org.dcm4che.net.service.InstanceLocator;
import org.dcm4che.util.SafeClose;
import org.dcm4che.util.StringUtils;
import org.dcm4che.ws.rs.MediaTypes;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.entity.InstanceFileRef;
import org.dcm4chee.archive.retrieve.RetrieveService;

/**
 * WADO service implementing DICOM PS 3.18-2009.
 * @see ftp://medical.nema.org/medical/dicom/2009/09_18pu.pdf
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 */
@RequestScoped
@Path("/wado/{AETitle}")
public class WadoURI extends Object  {

    private static final int STATUS_NOT_IMPLEMENTED = 501;

    public enum Anonymize { yes }

    public enum Annotation { patient, technique }

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

    private String aetitle;
    
    private ArchiveAEExtension arcAE;
    
    @Inject
    private Device device;

    @Inject
    private RetrieveService retrieveService;
    
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
    
    /**
     * Setter for the AETitle property, automatically invoked
     * by the CDI container. The setter initializes the ArchiveAEExtension
     * as well.
     */
    @PathParam("AETitle")
    public void setAETitle(String aet) {
        this.aetitle=aet;
        ApplicationEntity ae = device.getApplicationEntity(aet);
        if (ae == null || !ae.isInstalled()
                || (arcAE = ae.getAEExtension(ArchiveAEExtension.class)) == null) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
    }

    @GET
    public Response retrieve() throws WebApplicationException {
        checkRequest();
        
        org.dcm4chee.archive.conf.QueryParam queryParam = arcAE.
                getQueryParam(EnumSet.noneOf(QueryOption.class), getAccessControlIDs());
        
        List<InstanceLocator> ref =
                retrieveService.calculateMatches(studyUID, seriesUID, objectUID, queryParam);
        if (ref == null)
            throw new WebApplicationException(Status.NOT_FOUND);
        
        if (ref.size() != 1)
            throw new WebApplicationException(Status.BAD_REQUEST);
        
        InstanceLocator instance = ref.get(0);
        Attributes attrs = (Attributes)instance.getObject();

        MediaType mediaType = selectMediaType(instance.tsuid, instance.cuid, attrs);

        if (!isAccepted(mediaType))
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);

        //TODO Audit        
        //AuditUtils.logWADORetrieve(ref, attrs, request);

        if (mediaType == MediaTypes.APPLICATION_DICOM_TYPE)
            return retrieveNativeDicomObject(instance, attrs);

        if (mediaType == MediaTypes.IMAGE_JPEG_TYPE)
            return retrieveJPEG(instance, attrs);

        throw new WebApplicationException(STATUS_NOT_IMPLEMENTED);

    }
    
    private void checkRequest()
            throws WebApplicationException {
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
        if (applicationDicom
                ? (annotation != null || rows != 0 || columns != 0
                    || region != null || windowCenter != 0 || windowWidth != 0
                    || frameNumber != 0 || imageQuality != 0
                    || presentationUID != null || presentationSeriesUID != null)
                : (anonymize != null || !transferSyntax.isEmpty() 
                    || rows < 0 || columns < 0
                    || imageQuality < 0 || imageQuality > 100
                    || presentationUID != null && presentationSeriesUID == null))
            throw new WebApplicationException(Status.BAD_REQUEST);
    }

    private boolean isAccepted(MediaType mediaType) {
        for (MediaType accepted : headers.getAcceptableMediaTypes())
            if (mediaType.isCompatible(accepted))
                return true;
        return false;
    }


    private Response retrieveNativeDicomObject(InstanceLocator ref,
            Attributes attrs) {
        String tsuid = selectTransferSyntax(ref.tsuid);
        MediaType mediaType = MediaType.valueOf(
                "application/dicom;transfer-syntax=" + tsuid);
        return Response.ok(new DicomObjectOutput(ref, attrs, tsuid),
                mediaType).build();
    }

    private String selectTransferSyntax(String tsuid) {
        return transferSyntax.contains("*") 
                || transferSyntax.contains(tsuid)
                || !ImageReaderFactory.canDecompress(tsuid)
                ? tsuid
                : UID.ExplicitVRLittleEndian;
    }

    private Response retrieveJPEG(final InstanceLocator ref, 
            final Attributes attrs) {
        final MediaType mediaType = MediaTypes.IMAGE_JPEG_TYPE;
        return Response.ok(new StreamingOutput() {
            
            @Override
            public void write(OutputStream out) throws IOException,
                    WebApplicationException {
                ImageInputStream iis = ImageIO.createImageInputStream(ref.getFile());
                BufferedImage bi;
                try {
                    bi = readImage(iis, attrs);
                } finally {
                    SafeClose.close(iis);
                }
                writeJPEG(bi, new OutputStreamAdapter(out));
            }
        }, mediaType).build();
    }

    private BufferedImage readImage(ImageInputStream iis, Attributes attrs)
            throws IOException {
        Iterator<ImageReader> readers = 
                ImageIO.getImageReadersByFormatName("DICOM");
        if (!readers.hasNext()) {
            ImageIO.scanForPlugins();
            readers = ImageIO.getImageReadersByFormatName("DICOM");
        }
        ImageReader reader = readers.next();
        try {
            reader.setInput(iis);
            DicomMetaData metaData = (DicomMetaData) reader.getStreamMetadata();
            metaData.getAttributes().addAll(attrs);
            DicomImageReadParam param = (DicomImageReadParam)
                    reader.getDefaultReadParam();
            init(param);
            return rescale(
                    reader.read(frameNumber > 0 ? frameNumber-1 : 0, param),
                    metaData.getAttributes(), param.getPresentationState());
        } finally {
            reader.dispose();
        }
    }

    private BufferedImage rescale(BufferedImage src, Attributes imgAttrs,
            Attributes psAttrs) {
        int r = rows;
        int c = columns;
        float sy = psAttrs != null 
                ? PixelAspectRatio.forPresentationState(psAttrs)
                : PixelAspectRatio.forImage(imgAttrs);
        if (r == 0 && c == 0 && sy == 1f)
            return src;

        float sx = 1f;
        if (r != 0 || c != 0) {
            if (r != 0 && c != 0)
                if (r * src.getWidth() > c * src.getHeight() * sy)
                    r = 0;
                else
                    c = 0;
            sx = r != 0 
                    ? r / (src.getHeight() * sy)
                    : c / src.getWidth();
            sy *= sx;
        }
        AffineTransformOp op = new AffineTransformOp(
                AffineTransform.getScaleInstance(sx, sy),
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(src,
                op.createCompatibleDestImage(src, src.getColorModel()));
    }

    private void init(DicomImageReadParam param)
            throws WebApplicationException, IOException {
        
        org.dcm4chee.archive.conf.QueryParam queryParam = arcAE.
                getQueryParam(EnumSet.noneOf(QueryOption.class), getAccessControlIDs());
        
        param.setWindowCenter(windowCenter);
        param.setWindowWidth(windowWidth);
        if (presentationUID != null) {
            List<InstanceLocator> ref = retrieveService.calculateMatches(
                    studyUID, presentationSeriesUID, presentationUID,queryParam);
            if (ref == null || ref.size()==0)
                throw new WebApplicationException(Status.NOT_FOUND);

            if (ref.size() !=1)
                throw new WebApplicationException(Status.BAD_REQUEST);
                
            DicomInputStream dis = new DicomInputStream(ref.get(0).getFile());
            try {
                param.setPresentationState(dis.readDataset(-1, -1));
            } finally {
                SafeClose.close(dis);
            }
        }
    }

    private void writeJPEG(BufferedImage bi, ImageOutputStream ios)
            throws IOException {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof PaletteColorModel)
            bi = ((PaletteColorModel) cm).convertToIntDiscrete(bi.getData());
        ImageWriter imageWriter =
                ImageIO.getImageWritersByFormatName("JPEG").next();
        try {
            ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
            if (imageQuality > 0) {
                imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                imageWriteParam.setCompressionQuality(imageQuality / 100f);
            }
            imageWriter.setOutput(ios);
            imageWriter.write(null, new IIOImage(bi, null, null), imageWriteParam);
        } finally {
            imageWriter.dispose();
        }
    }
    
    private String[] getAccessControlIDs()
    {
        //TODO Access Control to be implemented
        
        return new String[0];
    }
    
    private MediaType selectMediaType(String transferSyntaxUID,
            String sopClassUID, Attributes attrs) {
        
        List<MediaType> supportedMediaTypes =
                supportedMediaTypesOf(transferSyntaxUID, sopClassUID, attrs);
        
        if (contentType != null)
            for (MediaType requestedType : contentType.values)
                for (MediaType supportedType : supportedMediaTypes)
                    if (requestedType.isCompatible(supportedType))
                        return supportedType;
        return supportedMediaTypes.get(0);
    }
    
    public static List<MediaType> supportedMediaTypesOf(String transferSyntaxUID,
            String sopClassUID, Attributes attrs) {
        List<MediaType> list = new ArrayList<MediaType>(4);
        if (attrs.contains(Tag.BitsAllocated)) {
            if (attrs.getInt(Tag.NumberOfFrames, 1) > 1) {
                list.add(MediaTypes.APPLICATION_DICOM_TYPE);
                MediaType mediaType;
                if (UID.MPEG2.equals(transferSyntaxUID)
                        || UID.MPEG2MainProfileHighLevel
                        .equals(transferSyntaxUID))
                    mediaType = MediaTypes.VIDEO_MPEG_TYPE;
                else if (UID.MPEG4AVCH264HighProfileLevel41
                        .equals(transferSyntaxUID)
                        || UID.MPEG4AVCH264BDCompatibleHighProfileLevel41
                        .equals(transferSyntaxUID))
                    mediaType = MediaTypes.VIDEO_MP4_TYPE;
                else
                    mediaType= MediaTypes.IMAGE_JPEG_TYPE;
                list.add(mediaType);
            } else {
                list.add(MediaTypes.IMAGE_JPEG_TYPE);
                list.add(MediaTypes.APPLICATION_DICOM_TYPE);
            }
        } else if (attrs.contains(Tag.ContentSequence)) {
            list.add(MediaType.TEXT_HTML_TYPE);
            list.add(MediaType.TEXT_PLAIN_TYPE);
//            list.add(APPLICATION_PDF_TYPE);
            list.add(MediaTypes.APPLICATION_DICOM_TYPE);
        } else {
            list.add(MediaTypes.APPLICATION_DICOM_TYPE);
            if (UID.EncapsulatedPDFStorage.equals(sopClassUID))
                list.add(MediaTypes.APPLICATION_PDF_TYPE);
            else if (UID.EncapsulatedCDAStorage.equals(sopClassUID))
                list.add(MediaType.TEXT_XML_TYPE);
        }
        return list ;
    }

}
