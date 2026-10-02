package com.pixelforge.service;

import com.pixelforge.core.ProductImage;
import com.pixelforge.orders.Order;
import com.pixelforge.orders.OrderHistory;
import com.pixelforge.factory.StyleCatalog;
import com.pixelforge.factory.StyleFactory;
import com.pixelforge.pipeline.DecoratorCatalog;
import com.pixelforge.pipeline.DecoratorPipelineBuilder;
import com.pixelforge.pipeline.PipelineDirector;
import com.pixelforge.pipeline.PipelineValidator;
import com.pixelforge.pipeline.TreatmentRequest;
import com.pixelforge.prototype.PipelinePreset;
import com.pixelforge.prototype.PresetRegistry;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessingService {
    public static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    private final DecoratorCatalog decoratorCatalog = new DecoratorCatalog();
    private final PipelineValidator validator = new PipelineValidator();
    private final StyleCatalog styleCatalog = new StyleCatalog();
    private final PresetRegistry presetRegistry = new PresetRegistry();
    private final OrderHistory orderHistory = new OrderHistory();

    public ProcessingResult process(byte[] imageBytes, String treatmentList, String styleId) throws IOException {
        if (imageBytes.length == 0 || imageBytes.length > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("The image must be between 1 byte and 5 MB");
        }
        BufferedImage photo = ImageIO.read(new ByteArrayInputStream(imageBytes));

        StyleFactory style = styleCatalog.find(styleId);
        PipelineDirector director = new PipelineDirector(new DecoratorPipelineBuilder(decoratorCatalog, validator));
        ProductImage pipeline = director.construct(photo, style, parseList(treatmentList));
        BufferedImage finalImage = pipeline.process();
        Order order = orderHistory.register(pipeline);

        return new ProcessingResult(order, finalImage);
    }

    public List<StyleFactory> getStyles() {
        return styleCatalog.findAll();
    }

    public List<PipelinePreset> getPresets() {
        return presetRegistry.createAllCopies();
    }

    public PipelinePreset createPreset(String presetId, String storeName) {
        return presetRegistry.createCopy(presetId).withStoreName(storeName);
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
