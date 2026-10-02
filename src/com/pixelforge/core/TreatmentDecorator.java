package com.pixelforge.core;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * DECORATOR PATTERN - Role: DECORATOR (abstract)
 *
 * Every treatment IS a ProductImage (implements the interface) and HAS a
 * ProductImage inside (the "wrapped" field). That is the whole trick:
 * it can wrap the base image or another treatment, as many times as needed.
 *
 * Each method first asks the wrapped object for its result and then adds
 * its own part on top (its transformation, its cost, its time, its name).
 */
public abstract class TreatmentDecorator implements ProductImage {

    protected final ProductImage wrapped;
    private final TreatmentType type;

    protected TreatmentDecorator(ProductImage wrapped, TreatmentType type) {
        this.wrapped = wrapped;
        this.type = type;
    }

    @Override
    public BufferedImage process() {
        BufferedImage previousResult = wrapped.process(); // 1. let the inner layers work
        return applyTo(previousResult);                   // 2. add this treatment on top
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

    /** Cost of THIS layer only. Subclasses may override it (see DiscountDecorator). */
    protected int getOwnCost() {
        return type.getCost();
    }

    /** Name of THIS layer only. Subclasses may override it to show their parameter. */
    protected String getOwnName() {
        return type.getDisplayName();
    }

    /** The image transformation specific to each concrete treatment. */
    protected abstract BufferedImage applyTo(BufferedImage image);
}
