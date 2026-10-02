package com.pixelforge.service;

import com.pixelforge.core.ProductImage;
import com.pixelforge.orders.Order;
import com.pixelforge.orders.OrderHistory;
import com.pixelforge.pipeline.PipelineBuilder;
import com.pixelforge.pipeline.TreatmentRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the use case "process a product photo".
 * Used by both the HTTP API and the console demo, so the logic lives in one place.
 */
public class ImageProcessingService {

    public static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024; // 5 MB

    private final PipelineBuilder pipelineBuilder = new PipelineBuilder();
    private final OrderHistory orderHistory = new OrderHistory();

    /**
     * @param imageBytes    raw JPG/PNG file
     * @param treatmentList comma-separated list, e.g. "REMOVE_BACKGROUND,RESIZE,WATERMARK:MyStore"
     */
    public ProcessingResult process(byte[] imageBytes, String treatmentList) throws IOException {
        if (imageBytes.length == 0 || imageBytes.length > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("The image must be between 1 byte and 5 MB");
        }
        BufferedImage photo = ImageIO.read(new ByteArrayInputStream(imageBytes));

        ProductImage pipeline = pipelineBuilder.build(photo, parseList(treatmentList));
        BufferedImage finalImage = pipeline.process();
        Order order = orderHistory.register(pipeline);

        return new ProcessingResult(order, finalImage);
    }

    public List<Order> getOrders() {
        return orderHistory.findAll();
    }

    private List<TreatmentRequest> parseList(String treatmentList) {
        List<TreatmentRequest> requests = new ArrayList<>();
        if (treatmentList == null || treatmentList.isBlank()) {
            return requests;
        }
        for (String item : treatmentList.split(",")) {
            if (!item.isBlank()) {
                requests.add(TreatmentRequest.parse(item));
            }
        }
        return requests;
    }

    /** What the customer gets back: the saved order and the final image. */
    public static class ProcessingResult {

        private final Order order;
        private final BufferedImage image;

        public ProcessingResult(Order order, BufferedImage image) {
            this.order = order;
            this.image = image;
        }

        public Order getOrder() {
            return order;
        }

        public BufferedImage getImage() {
            return image;
        }

        /** JPEG if the image was compressed (no transparency), PNG otherwise. */
        public String getFormat() {
            return image.getColorModel().hasAlpha() ? "png" : "jpg";
        }

        public byte[] getImageBytes() throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, getFormat(), output);
            return output.toByteArray();
        }
    }
}
