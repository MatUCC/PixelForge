package com.pixelforge.factory;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.treatments.BorderDecorator;
import com.pixelforge.treatments.ColorFilterDecorator;
import com.pixelforge.treatments.ColorFilterDecorator.FilterType;
import com.pixelforge.treatments.WatermarkDecorator;

import java.awt.Color;

public abstract class AbstractStyleFactory implements StyleFactory {

    private final String id;
    private final String displayName;

    protected AbstractStyleFactory(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public TreatmentDecorator createFilter(ProductImage wrapped, String parameter) {
        FilterType filter = isBlank(parameter) ? defaultFilter() : FilterType.fromText(parameter);
        return new ColorFilterDecorator(wrapped, filter);
    }

    @Override
    public TreatmentDecorator createBorder(ProductImage wrapped, String parameter) {
        String color = isBlank(parameter) ? defaultBorderColor() : parameter;
        return new BorderDecorator(wrapped, color);
    }

    @Override
    public TreatmentDecorator createWatermark(ProductImage wrapped, String text) {
        return new WatermarkDecorator(wrapped, text, watermarkColor());
    }

    protected abstract FilterType defaultFilter();

    protected abstract String defaultBorderColor();

    protected abstract Color watermarkColor();

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
