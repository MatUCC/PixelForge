package com.pixelforge.factory;

import com.pixelforge.treatments.ColorFilterDecorator.FilterType;

import java.awt.Color;

public class ClassicStyleFactory extends AbstractStyleFactory {

    public ClassicStyleFactory() {
        super("CLASSIC", "Classic");
    }

    @Override
    protected FilterType defaultFilter() {
        return FilterType.GRAYSCALE;
    }

    @Override
    protected String defaultBorderColor() {
        return "000000";
    }

    @Override
    protected Color watermarkColor() {
        return new Color(0xFFFFFF);
    }
}
