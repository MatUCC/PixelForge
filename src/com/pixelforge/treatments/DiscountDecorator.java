package com.pixelforge.treatments;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.core.TreatmentType;

import java.awt.image.BufferedImage;

public class DiscountDecorator extends TreatmentDecorator {
    private static final int DISCOUNT_PERCENT = 10;

    public DiscountDecorator(ProductImage wrapped) {
        super(wrapped, TreatmentType.DISCOUNT);
    }

    @Override
    protected int getOwnCost() {
        return -(wrapped.getCost() * DISCOUNT_PERCENT / 100);
    }

    @Override
    protected BufferedImage applyTo(BufferedImage image) {
        return image;
    }
}
