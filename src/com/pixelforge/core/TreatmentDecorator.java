package com.pixelforge.core;

import java.awt.image.BufferedImage;
import java.util.List;

public abstract class TreatmentDecorator implements ProductImage {
    protected final ProductImage wrapped;
    private final TreatmentType type;

    protected TreatmentDecorator(ProductImage wrapped, TreatmentType type) {
        this.wrapped = wrapped;
        this.type = type;
    }

    @Override
    public BufferedImage process() {
        BufferedImage previousResult = wrapped.process();
        return applyTo(previousResult);
    }

    @Override
    public int getCost() {
        return wrapped.getCost() + getOwnCost();
    }

    @Override
    public int getProcessingSeconds() {
        return wrapped.getProcessingSeconds() + type.getSeconds();
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + " + getOwnName();
    }

    @Override
    public List<LayerInfo> getLayers() {
        List<LayerInfo> layers = wrapped.getLayers();
        layers.add(new LayerInfo(getOwnName(), getOwnCost(), type.getSeconds()));
        return layers;
    }

    protected int getOwnCost() {
        return type.getCost();
    }

    protected String getOwnName() {
        return type.getDisplayName();
    }

    protected abstract BufferedImage applyTo(BufferedImage image);
}
