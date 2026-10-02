package com.pixelforge.factory;

import com.pixelforge.treatments.ColorFilterDecorator.FilterType;

import java.awt.Color;

public class VibrantStyleFactory extends AbstractStyleFactory {

    public VibrantStyleFactory() {
        super("VIBRANT", "Vibrant");
    }

    @Override
    protected FilterType defaultFilter() {
        return FilterType.HIGH_CONTRAST;
    }

    @Override
    protected String defaultBorderColor() {
        return "FF3B30";
    }

    @Override
    protected Color watermarkColor() {
        return new Color(0xFFD60A);
    }
}
