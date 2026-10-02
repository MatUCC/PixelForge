package com.pixelforge.pipeline;

import com.pixelforge.core.BaseImage;
import com.pixelforge.core.ProductImage;
import com.pixelforge.factory.ClassicStyleFactory;
import com.pixelforge.factory.StyleFactory;
import com.pixelforge.treatments.DiscountDecorator;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DecoratorPipelineBuilder implements PipelineBuilder {

    public static final int DISCOUNT_THRESHOLD = 4;

    private final DecoratorCatalog catalog;
    private final PipelineValidator validator;
    private final List<TreatmentRequest> requests = new ArrayList<>();
    private BufferedImage photo;
    private StyleFactory style = new ClassicStyleFactory();

    public DecoratorPipelineBuilder(DecoratorCatalog catalog, PipelineValidator validator) {
        this.catalog = catalog;
        this.validator = validator;
    }

    @Override
    public PipelineBuilder reset() {
        requests.clear();
        photo = null;
        style = new ClassicStyleFactory();
        return this;
    }

    @Override
    public PipelineBuilder photo(BufferedImage photo) {
        this.photo = photo;
        return this;
    }

    @Override
    public PipelineBuilder style(StyleFactory style) {
        this.style = style;
        return this;
    }

    @Override
    public PipelineBuilder addTreatment(TreatmentRequest request) {
        requests.add(request);
        return this;
    }

    @Override
    public ProductImage build() {
        validator.validate(requests);

        ProductImage result = new BaseImage(photo);
        for (TreatmentRequest request : requests) {
            result = catalog.create(request, result, style);
        }
        if (requests.size() >= DISCOUNT_THRESHOLD) {
            result = new DiscountDecorator(result);
        }
        return result;
    }
}
