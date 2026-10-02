package com.pixelforge.decorator;

import com.pixelforge.core.ProductImage;

import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - ROLE: Concrete Decorator.
 *
 * Special decorator: it does NOT touch the picture, it only changes the price.
 * It proves that a decorator can extend just one part of the behavior.
 *
 * It is added automatically by PipelineBuilder when the order has 4 or more
 * treatments, and it must be the outermost layer so it sees the full price.
 */
public class DiscountDecorator extends TreatmentDecorator {

    private static final double DISCOUNT_RATE = 0.10;

    public DiscountDecorator(ProductImage wrapped) {
        super(wrapped, TreatmentType.DISCOUNT);
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage image) {
        return image; // picture unchanged
    }

    @Override
    protected double getOwnCost() {
        // Negative cost: 10% of everything that is inside this decorator.
        return -wrapped.getCost() * DISCOUNT_RATE;
    }
}
