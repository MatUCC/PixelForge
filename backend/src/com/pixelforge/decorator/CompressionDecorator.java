package com.pixelforge.decorator;

import com.pixelforge.core.ProductImage;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * DECORATOR PATTERN - ROLE: Concrete Decorator.
 *
 * Re-encodes the picture as JPEG with lower quality to make it lighter for the web.
 * Business rule: it must be the LAST treatment (validated in PipelineBuilder),
 * because any treatment applied after it would undo the compression.
 */
public class CompressionDecorator extends TreatmentDecorator {

    private static final float JPEG_QUALITY = 0.5f;

    public CompressionDecorator(ProductImage wrapped) {
        super(wrapped, TreatmentType.COMPRESSION);
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage image) {
        try {
            byte[] jpegBytes = toJpeg(image);
            return ImageIO.read(new ByteArrayInputStream(jpegBytes));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not compress the image", e);
        }
    }

    private byte[] toJpeg(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(JPEG_QUALITY);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }
}
