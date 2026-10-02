package com.pixelforge.decorator;

import com.pixelforge.core.ProductImage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - ROLE: Concrete Decorator.
 *
 * Fits the photo inside a square canvas (marketplace format, 1080x1080 by default)
 * keeping its proportions and filling the empty space with white.
 */
public class ResizeDecorator extends TreatmentDecorator {

    private static final int DEFAULT_SIZE = 1080;

    private final int size;

    public ResizeDecorator(ProductImage wrapped, String sizeParameter) {
        super(wrapped, TreatmentType.RESIZE);
        this.size = parseSize(sizeParameter);
    }

    private int parseSize(String value) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return Math.max(100, Math.min(parsed, 2000)); // keep it in a sane range
        } catch (Exception e) {
            return DEFAULT_SIZE;
        }
    }

    @Override
    protected String getLayerName() {
        return type.getLabel() + " (" + size + "x" + size + ")";
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage image) {
        double scale = Math.min((double) size / image.getWidth(), (double) size / image.getHeight());
        int newWidth = (int) Math.round(image.getWidth() * scale);
        int newHeight = (int) Math.round(image.getHeight() * scale);

        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.drawImage(image, (size - newWidth) / 2, (size - newHeight) / 2, newWidth, newHeight, null);
        g.dispose();
        return canvas;
    }
}
