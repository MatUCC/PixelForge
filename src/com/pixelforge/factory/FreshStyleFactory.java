package com.pixelforge.factory;

import com.pixelforge.treatments.ColorFilterDecorator.FilterType;

import java.awt.Color;

public class FreshStyleFactory extends AbstractStyleFactory {

    public FreshStyleFactory() {
        super("FRESH", "Fresh");
    }

    @Override
    protected FilterType defaultFilter() {
        return FilterType.BRIGHTEN;
    }

    @Override
    protected String defaultBorderColor() {
        return "34C759";
    }

    @Override
    protected Color watermarkColor() {
        return new Color(0xB9FBC0);
    }
}
