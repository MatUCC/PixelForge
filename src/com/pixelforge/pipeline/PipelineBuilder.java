package com.pixelforge.pipeline;

import com.pixelforge.core.BaseImage;
import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentType;
import com.pixelforge.treatments.BorderDecorator;
import com.pixelforge.treatments.ColorFilterDecorator;
import com.pixelforge.treatments.CompressionDecorator;
import com.pixelforge.treatments.DiscountDecorator;
import com.pixelforge.treatments.RemoveBackgroundDecorator;
import com.pixelforge.treatments.ResizeDecorator;
import com.pixelforge.treatments.WatermarkDecorator;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Builds the decorator chain from the list the customer sent, in the same order.
 *
 * For [REMOVE_BACKGROUND, RESIZE, WATERMARK:MyStore] it produces:
 *
 *   new WatermarkDecorator(
 *       new ResizeDecorator(
 *           new RemoveBackgroundDecorator(
 *               new BaseImage(photo))), "MyStore")
 *
 * It also enforces the business rules of the case study.
 */
public class PipelineBuilder {

    /** With this many treatments or more, a 10% discount is applied. */
    public static final int DISCOUNT_THRESHOLD = 4;

    public ProductImage build(BufferedImage photo, List<TreatmentRequest> requests) {
        validate(requests);

        ProductImage result = new BaseImage(photo);
        for (TreatmentRequest request : requests) {
            result = wrap(result, request); // each loop adds one more layer on top
        }

        if (requests.size() >= DISCOUNT_THRESHOLD) {
            result = new DiscountDecorator(result);
        }
        return result;
    }

    private ProductImage wrap(ProductImage current, TreatmentRequest request) {
        switch (request.getType()) {
            case REMOVE_BACKGROUND:
                return new RemoveBackgroundDecorator(current);
            case RESIZE:
                return new ResizeDecorator(current);
            case COLOR_FILTER:
                return new ColorFilterDecorator(current, parseFilter(request.getParameter()));
            case WATERMARK:
                return new WatermarkDecorator(current, request.getParameter());
            case BORDER:
                return new BorderDecorator(current, request.getParameter());
            case COMPRESSION:
                return new CompressionDecorator(current);
            default:
                throw new IllegalArgumentException("Treatment not supported: " + request.getType());
        }
    }

    /** Business rules that are checked BEFORE processing anything. */
    private void validate(List<TreatmentRequest> requests) {
        if (requests.isEmpty()) {
            throw new IllegalArgumentException("Choose at least one treatment");
        }
        for (int i = 0; i < requests.size() - 1; i++) {
            if (requests.get(i).getType() == TreatmentType.COMPRESSION) {
                throw new IllegalArgumentException("COMPRESSION must be the last treatment");
            }
        }
    }

    private ColorFilterDecorator.FilterType parseFilter(String parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("COLOR_FILTER needs a type, e.g. COLOR_FILTER:GRAYSCALE");
        }
        try {
            return ColorFilterDecorator.FilterType.valueOf(parameter.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown filter: " + parameter + " (use GRAYSCALE, BRIGHTEN or HIGH_CONTRAST)");
        }
    }
}
