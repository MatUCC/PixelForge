package com.pixelforge.treatments;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.core.TreatmentType;

import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - Role: CONCRETE DECORATOR
 *
 * Special decorator: it does NOT touch the image, it only changes the price
 * (10% off everything it wraps). It shows that a decorator can extend just
 * one part of the behavior and leave the rest unchanged.
 *
 * The customer never picks it; PipelineBuilder adds it when 4+ treatments are chosen.
 */
public class DiscountDecorator extends TreatmentDecorator {

    private static final int DISCOUNT_PERCENT = 10;

    public DiscountDecorator(ProductImage wrapped) {
        super(wrapped, TreatmentType.DISCOUNT);
    }

    @Override
    protected int getOwnCost() {
        return -(wrapped.getCost() * DISCOUNT_PERCENT / 100); // negative cost = discount
    }

    @Override
    protected BufferedImage applyTo(BufferedImage image) {
        return image; // the image passes through unchanged
    }
}
