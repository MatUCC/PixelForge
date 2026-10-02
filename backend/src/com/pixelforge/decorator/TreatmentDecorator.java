package com.pixelforge.decorator;

import com.pixelforge.core.Layer;
import com.pixelforge.core.ProductImage;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * DECORATOR PATTERN - ROLE: Decorator (abstract).
 *
 * Key idea: a TreatmentDecorator IS a ProductImage (implements the interface)
 * and HAS a ProductImage (the "wrapped" field). That is why decorators can be
 * stacked: a decorator can wrap the base image or another decorator.
 *
 *   new WatermarkDecorator( new ResizeDecorator( new BaseImage(photo) ) )
 *
 * Every method first asks the wrapped object for its result and then adds
 * its own contribution (same idea as super.play() in the class example).
 */
public abstract class TreatmentDecorator implements ProductImage {

    protected final ProductImage wrapped;
    protected final TreatmentType type;

    protected TreatmentDecorator(ProductImage wrapped, TreatmentType type) {
        this.wrapped = wrapped;
        this.type = type;
    }

    /** Each concrete decorator only has to say what it does to the picture. */
    protected abstract BufferedImage applyEffect(BufferedImage image);

    /** Text shown in the description and breakdown, e.g. "Watermark (MyStore)". */
    protected String getLayerName() {
        return type.getLabel();
    }

    /** What THIS layer adds to the price. Can be overridden (see DiscountDecorator). */
    protected double getOwnCost() {
        return type.getCost();
    }

    @Override
    public BufferedImage render() {
        // 1) let the inner layers do their work  2) apply this layer on top
        BufferedImage inner = wrapped.render();
        return applyEffect(inner);
    }

    @Override
    public double getCost() {
        return wrapped.getCost() + getOwnCost();
    }

    @Override
    public int getProcessingSeconds() {
        return wrapped.getProcessingSeconds() + type.getSeconds();
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + " + getLayerName();
    }

    @Override
    public List<Layer> getLayers() {
        List<Layer> layers = wrapped.getLayers();
        layers.add(new Layer(getLayerName(), getOwnCost(), type.getSeconds()));
        return layers;
    }
}
