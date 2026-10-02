package com.pixelforge.treatments;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.core.TreatmentType;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * DECORATOR PATTERN - Role: CONCRETE DECORATOR
 *
 * Re-encodes the photo as a lighter JPEG for the web.
 * JPEG has no transparency, so transparent areas become white.
 * That is why the business rule says compression must always be the LAST layer.
 */
public class CompressionDecorator extends TreatmentDecorator {

    private static final float JPEG_QUALITY = 0.6f;

    public CompressionDecorator(ProductImage wrapped) {
        super(wrapped, TreatmentType.COMPRESSION);
    }

    @Override
    protected BufferedImage applyTo(BufferedImage image) {
        try {
            byte[] jpegBytes = encodeAsJpeg(flattenOnWhite(image));
            return ImageIO.read(new ByteArrayInputStream(jpegBytes));
        } catch (IOException e) {
            throw new UncheckedIOException("Compression failed", e);
        }
    }

    private BufferedImage flattenOnWhite(BufferedImage image) {
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private byte[] encodeAsJpeg(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(JPEG_QUALITY);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(stream);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
        return output.toByteArray();
    }
}
