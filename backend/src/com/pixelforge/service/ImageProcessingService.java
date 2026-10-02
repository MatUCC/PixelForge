package com.pixelforge.service;

import com.pixelforge.core.ProductImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Use case "process a product photo":
 *   1. read the uploaded bytes
 *   2. ask PipelineBuilder for the decorator chain
 *   3. render it and save the order in the history
 *
 * The orders are kept in memory (no database) to keep the workshop simple.
 */
public class ImageProcessingService {

    private static final int MAX_UPLOAD_BYTES = 5 * 1024 * 1024; // 5 MB

    private final PipelineBuilder pipelineBuilder = new PipelineBuilder();
    private final List<Order> history = new ArrayList<>();
    private int nextOrderId = 1;

    public synchronized ProcessingResult process(byte[] uploadedBytes, List<TreatmentRequest> requests) throws IOException {
        if (uploadedBytes.length == 0) {
            throw new PipelineValidationException("No image was uploaded");
        }
        if (uploadedBytes.length > MAX_UPLOAD_BYTES) {
            throw new PipelineValidationException("The image is larger than 5 MB");
        }
        BufferedImage photo = ImageIO.read(new ByteArrayInputStream(uploadedBytes));
        if (photo == null) {
            throw new PipelineValidationException("The file is not a valid JPG or PNG image");
        }

        // From here on we only use the ProductImage interface,
        // no matter how many decorators are wrapped around the base image.
        ProductImage product = pipelineBuilder.build(photo, requests);
        BufferedImage finalPicture = product.render();

        Order order = new Order(nextOrderId++, product.getDescription(),
                product.getCost(), product.getProcessingSeconds(), product.getLayers());
        history.add(0, order); // newest first

        return new ProcessingResult(order, toPng(finalPicture));
    }

    public synchronized List<Order> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    private byte[] toPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
