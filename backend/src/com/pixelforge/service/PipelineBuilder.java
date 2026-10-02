package com.pixelforge.service;

import com.pixelforge.core.BaseImage;
import com.pixelforge.core.ProductImage;
import com.pixelforge.decorator.BackgroundRemovalDecorator;
import com.pixelforge.decorator.ColorFilterDecorator;
import com.pixelforge.decorator.CompressionDecorator;
import com.pixelforge.decorator.DiscountDecorator;
import com.pixelforge.decorator.FrameDecorator;
import com.pixelforge.decorator.ResizeDecorator;
import com.pixelforge.decorator.TreatmentType;
import com.pixelforge.decorator.WatermarkDecorator;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Builds the decorator chain AT RUNTIME from the list the user chose.
 *
 * This is where the pattern pays off: instead of having one subclass for every
 * possible combination (64+ classes), we start with the base image and wrap it
 * once per selected treatment, in the order the user picked.
 *
 *   list = [REMOVE_BACKGROUND, RESIZE, WATERMARK]
 *
 *   image = new BaseImage(photo)
 *   image = new BackgroundRemovalDecorator(image)
 *   image = new ResizeDecorator(image, ...)
 *   image = new WatermarkDecorator(image, ...)
 */
public class PipelineBuilder {

    private static final int TREATMENTS_FOR_DISCOUNT = 4;
    private static final int MAX_TREATMENTS = 10;

    public ProductImage build(BufferedImage photo, List<TreatmentRequest> requests) {
        validate(requests);

        ProductImage image = new BaseImage(photo);
        for (TreatmentRequest request : requests) {
            image = wrap(image, request);
        }

        // Business rule: 4 or more treatments -> 10% off. It goes last (outermost)
        // so it can see the total price of everything inside it.
        if (requests.size() >= TREATMENTS_FOR_DISCOUNT) {
            image = new DiscountDecorator(image);
        }
        return image;
    }

    /** Wraps the current image with the decorator that matches the request. */
    private ProductImage wrap(ProductImage image, TreatmentRequest request) {
        String param = request.getParameter();
        switch (request.getType()) {
            case REMOVE_BACKGROUND: return new BackgroundRemovalDecorator(image);
            case RESIZE:            return new ResizeDecorator(image, param);
            case COLOR_FILTER:      return new ColorFilterDecorator(image, param);
            case WATERMARK:         return new WatermarkDecorator(image, param);
            case FRAME:             return new FrameDecorator(image, param);
            case COMPRESSION:       return new CompressionDecorator(image);
            default:
                throw new PipelineValidationException("Treatment not supported: " + request.getType());
        }
    }

    private void validate(List<TreatmentRequest> requests) {
        if (requests.size() > MAX_TREATMENTS) {
            throw new PipelineValidationException("Maximum " + MAX_TREATMENTS + " treatments per image");
        }
        // Business rule: compression must be the last treatment.
        for (int i = 0; i < requests.size() - 1; i++) {
            if (requests.get(i).getType() == TreatmentType.COMPRESSION) {
                throw new PipelineValidationException("Compression must be the last treatment");
            }
        }
    }
}
