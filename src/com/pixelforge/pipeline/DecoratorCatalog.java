package com.pixelforge.pipeline;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentType;
import com.pixelforge.factory.StyleFactory;
import com.pixelforge.treatments.CompressionDecorator;
import com.pixelforge.treatments.RemoveBackgroundDecorator;
import com.pixelforge.treatments.ResizeDecorator;

import java.util.EnumMap;
import java.util.Map;

public class DecoratorCatalog {

    private final Map<TreatmentType, DecoratorCreator> creators = new EnumMap<>(TreatmentType.class);

    public DecoratorCatalog() {
        register(TreatmentType.REMOVE_BACKGROUND, (wrapped, parameter, style) -> new RemoveBackgroundDecorator(wrapped));
        register(TreatmentType.RESIZE, (wrapped, parameter, style) -> new ResizeDecorator(wrapped));
        register(TreatmentType.COLOR_FILTER, (wrapped, parameter, style) -> style.createFilter(wrapped, parameter));
        register(TreatmentType.WATERMARK, (wrapped, parameter, style) -> style.createWatermark(wrapped, parameter));
        register(TreatmentType.BORDER, (wrapped, parameter, style) -> style.createBorder(wrapped, parameter));
        register(TreatmentType.COMPRESSION, (wrapped, parameter, style) -> new CompressionDecorator(wrapped));
    }

    public void register(TreatmentType type, DecoratorCreator creator) {
        creators.put(type, creator);
    }

    public ProductImage create(TreatmentRequest request, ProductImage wrapped, StyleFactory style) {
        DecoratorCreator creator = creators.get(request.getType());
        if (creator == null) {
            throw new IllegalArgumentException("Treatment not supported: " + request.getType());
        }
        return creator.create(wrapped, request.getParameter(), style);
    }
}
