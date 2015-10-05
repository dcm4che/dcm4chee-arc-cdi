/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4chee.archive.wado;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.image.BufferedImageUtils;
import org.dcm4che3.image.PixelAspectRatio;
import org.dcm4che3.imageio.codec.ImageParams;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.plugins.dcm.DicomMetaData;

import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Streams a rendered image (e.g. JPEG/PNG/GIF) or a sequence of rendered images (animated GIF).
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 * @since Aug 2015
 */
public class RenderedImageOutput implements StreamingOutput {
    private static final float DEF_FRAME_TIME = 1000.f;
    private static final byte[] LOOP_FOREVER = {1, 0, 0};

    private final ImageReader reader;
    private final DicomImageReadParam readParam;
    private final int rows;
    private final int columns;
    private final int imageIndex;
    private final ImageWriter writer;
    private final ImageWriteParam writeParam;

    public RenderedImageOutput(ImageReader reader, DicomImageReadParam readParam, int rows, int columns,
                               int imageIndex, ImageWriter writer, ImageWriteParam writeParam) {
        this.reader = reader;
        this.readParam = readParam;
        this.rows = rows;
        this.columns = columns;
        this.imageIndex = imageIndex;
        this.writer = writer;
        this.writeParam = writeParam;
    }

    @Override
    public void write(OutputStream out) throws IOException, WebApplicationException {
        // Note: when changing the logic here, please also consider the getEstimatedNeededMemory() method

        ImageOutputStream imageOut = null;
        try {
            imageOut = new MemoryCacheImageOutputStream(out);
            writer.setOutput(imageOut);
            if (imageIndex < 0) {
                IIOMetadata metadata = null;
                int numImages = reader.getNumImages(false);
                writer.prepareWriteSequence(null);
                BufferedImage bi = null;
                for (int i = 0; i < numImages; i++) {
                    readParam.setDestination(bi);
                    bi = reader.read(i, readParam);
                    BufferedImage adjustedBi = adjust(bi);
                    if (metadata == null)
                        metadata = createAnimatedGIFMetadata(adjustedBi, writeParam, frameTime());
                    writer.writeToSequence(
                            new IIOImage(adjustedBi, null, metadata),
                            writeParam);
                    imageOut.flushBefore(imageOut.length());
                }
                writer.endWriteSequence();
            } else {
                BufferedImage bi = reader.read(imageIndex, readParam);
                bi = adjust(bi);
                writer.write(null, new IIOImage(bi, null, null), writeParam);
            }
        } finally {
            writer.dispose();
            reader.dispose();
            if (imageOut != null)
                imageOut.close();
        }
    }

    private float frameTime() throws IOException {
        DicomMetaData metaData = getStreamMetadata();
        Attributes attrs = metaData.getAttributes();
        return attrs.getFloat(Tag.FrameTime, DEF_FRAME_TIME);
    }

    private DicomMetaData getStreamMetadata() throws IOException {
        return (DicomMetaData) reader.getStreamMetadata();
    }

    private IIOMetadata createAnimatedGIFMetadata(BufferedImage bi, ImageWriteParam param, float frameTime)
            throws IOException {
        ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromRenderedImage(bi);
        IIOMetadata metadata = writer.getDefaultImageMetadata(imageType, param);
        String formatName = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(formatName);
        IIOMetadataNode graphicControlExt =
                (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
        graphicControlExt.setAttribute("delayTime", Integer.toString(Math.round(frameTime() / 10)));
        IIOMetadataNode appExts = new IIOMetadataNode("ApplicationExtensions");
        IIOMetadataNode appExt = new IIOMetadataNode("ApplicationExtension");
        appExt.setAttribute("applicationID", "NETSCAPE");
        appExt.setAttribute("authenticationCode", "2.0");
        appExt.setUserObject(LOOP_FOREVER);
        appExts.appendChild(appExt);
        root.appendChild(appExts);
        metadata.setFromTree(formatName, root);
        return metadata;
    }

    private BufferedImage adjust(BufferedImage bi) throws IOException {
        if (bi.getColorModel().getNumComponents() == 3)
            bi = BufferedImageUtils.convertToIntRGB(bi);
        return rescale(bi);
    }

    private BufferedImage rescale(BufferedImage bi) throws IOException {
        if (!needsRescaling())
            return bi;

        Point2D.Double scalingFactors = getScalingFactors(bi.getHeight(), bi.getWidth());

        AffineTransformOp op = new AffineTransformOp(
                AffineTransform.getScaleInstance(scalingFactors.getX(), scalingFactors.getY()),
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(bi, null);
    }

    private Point2D.Double getScalingFactors(int origRows, int origColumns) throws IOException {
        int r = rows;
        int c = columns;
        float sy = getPixelAspectRatio();

        float sx = 1f;
        if (r != 0 || c != 0) {
            if (r != 0 && c != 0)
                if (r * origColumns > c * origRows * sy)
                    r = 0;
                else
                    c = 0;
            sx = r != 0 ? r / (origRows * sy) : c / (float) origColumns;
            sy *= sx;
        }

        return new Point2D.Double(sx, sy);
    }

    private boolean needsRescaling() throws IOException {
        return rows != 0 || columns != 0 || getPixelAspectRatio() != 1f;
    }

    private float getPixelAspectRatio() throws IOException {
        Attributes prAttrs = readParam.getPresentationState();
        return prAttrs != null ? PixelAspectRatio.forPresentationState(prAttrs)
                : PixelAspectRatio.forImage(getAttributes());
    }

    private Attributes getAttributes() throws IOException {
        return getStreamMetadata().getAttributes();
    }

    /**
     * @return (Pessimistic) estimation of the heap memory (in bytes) that will be needed at any moment in time during
     * decompression, rendering and compression.
     */
    public long getEstimatedNeededMemory() throws IOException {
        DicomMetaData dicomMetaData = getStreamMetadata();
        Attributes attributes = dicomMetaData.getAttributes();
        ImageParams imageParams = new ImageParams(attributes);

        long uncompressedFrameLength = imageParams.getFrameLength();

        long memoryNeededForDecompression = 0;

        Attributes fmi = dicomMetaData.getFileMetaInformation();
        if (fmi != null && TransferSyntaxType.forUID(fmi.getString(Tag.TransferSyntaxUID)) != TransferSyntaxType.NATIVE) {
            // Memory needed for reading one compressed frame
            // (For now: pessimistic assumption that same memory as for the uncompressed frame is needed. This very much
            // depends on the compression algorithm and properties.)
            // Actually it might be much less, if the decompressor supports streaming in the compressed data.
            long sourceCompressedFrameLength = uncompressedFrameLength;

            memoryNeededForDecompression += sourceCompressedFrameLength;
        }

        // size for intermediate un/decompressed buffered image
        memoryNeededForDecompression += uncompressedFrameLength;

        long memoryNeededForApplyingLUT = 0;

        memoryNeededForApplyingLUT += uncompressedFrameLength;

        long readImageLength;

        // in most cases (if it is a monochrome image) the DicomImageReader will apply a LUT
        if (imageParams.getPhotometricInterpretation().isMonochrome()) {
            // the resulting buffered image has one byte per sample (see DicomImageReader.read)
            long uncompressedFrameAppliedLUTLength = imageParams.getRows() * imageParams.getColumns();

            // Note: in some cases (if it is already 8-bit) the raster of the original uncompressed image will also be
            // re-used for the version with the applied LUT. This is not (yet) considered here.
            memoryNeededForApplyingLUT += uncompressedFrameAppliedLUTLength;

            // the final read image is the one is the applied lut
            readImageLength = uncompressedFrameAppliedLUTLength;
        } else {
            readImageLength = uncompressedFrameLength; // no LUT to apply
        }
        // Note: additional memory needed for applying overlays is currently not considered

        long memoryNeededForReading = Math.max(memoryNeededForDecompression, memoryNeededForApplyingLUT);

        long memoryNeededForAdjustingAndCompression = 0;

        memoryNeededForAdjustingAndCompression += readImageLength;

        long sizeOfColorAdjustedImage;

        // for color images in some cases a new INT-RGB buffered image is allocated (BufferedImageUtils.convertToIntRGB())
        boolean needsColorConversion = attributes.getInt(Tag.SamplesPerPixel, 0) == 3;
        if (needsColorConversion) {
            sizeOfColorAdjustedImage = imageParams.getRows() * imageParams.getColumns() * 4; // int has 4 bytes

            memoryNeededForAdjustingAndCompression += sizeOfColorAdjustedImage;
        } else {
            sizeOfColorAdjustedImage = readImageLength; // no color conversion
        }

        long rescaledImageSize;

        // rescaling is sometimes required
        if (needsRescaling()) {
            Point2D.Double scalingFactors = getScalingFactors(imageParams.getRows(), imageParams.getColumns());

            rescaledImageSize = Math.round(Math.ceil(sizeOfColorAdjustedImage * scalingFactors.getX() * scalingFactors.getY()));

            memoryNeededForAdjustingAndCompression += rescaledImageSize;
        } else {
            rescaledImageSize = sizeOfColorAdjustedImage; // no rescaling
        }

        // memory for a resulting compressed frame
        // (For now: pessimistic assumption that same memory as for the uncompressed frame is needed. This very much
        // depends on the compression algorithm and properties.)
        // Actually it might be much less, if the decompressor supports streaming out the compressed data.
        long compressedFrameLength = rescaledImageSize;

        memoryNeededForAdjustingAndCompression += compressedFrameLength;

        // reading and adjusting/compression happen sequentially (in between GC can run),
        // therefore we have to consider the maximum of the two
        return Math.max(memoryNeededForReading, memoryNeededForAdjustingAndCompression);
    }
}
