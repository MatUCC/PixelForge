package com.pixelforge.treatments;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.core.TreatmentType;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - Role: CONCRETE DECORATOR
 *
 * Fits the photo into a 1080x1080 square (the usual marketplace format),
 * keeping its proportions and centering it.
 */
public class ResizeDecorator extends TreatmentDecorator {

    private static final int TARGET_SIZE = 1080;

    public ResizeDecorator(ProductImage wrapped) {
        super(wrapped, TreatmentType.RESIZE);
    }

    @Override
    protected BufferedImage applyTo(BufferedImage image) {
        double scale = Math.min((double) TARGET_SIZE / image.getWidth(), (double) TARGET_SIZE / image.getHeight());
        int newWidth = (int) Math.round(image.getWidth() * scale);
        int newHeight = (int) Math.round(image.getHeight() * scale);
        int offsetX = (TARGET_SIZE - newWidth) / 2;
        int offsetY = (TARGET_SIZE - newHeight) / 2;

        BufferedImage resized = new BufferedImage(TARGET_SIZE, TARGET_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, offsetX, offsetY, newWidth, newHeight, null);
        g.dispose();
        return resized;
    }
}
