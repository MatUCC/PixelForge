package com.pixelforge.decorator;

import com.pixelforge.core.ProductImage;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - ROLE: Concrete Decorator.
 *
 * Replaces the background with pure white, which is what most marketplaces ask for.
 *
 * Simplified algorithm (no AI): the color of the top-left corner is taken as the
 * background color, and every pixel similar to it becomes white. It works well
 * for product photos taken on a plain surface.
 */
public class BackgroundRemovalDecorator extends TreatmentDecorator {

    private static final int TOLERANCE = 60;

    public BackgroundRemovalDecorator(ProductImage wrapped) {
        super(wrapped, TreatmentType.REMOVE_BACKGROUND);
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage image) {
        Color background = new Color(image.getRGB(0, 0));
        int white = Color.WHITE.getRGB();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color pixel = new Color(image.getRGB(x, y));
                if (isSimilar(pixel, background)) {
                    image.setRGB(x, y, white);
                }
            }
        }
        return image;
    }

    private boolean isSimilar(Color a, Color b) {
        int distance = Math.abs(a.getRed() - b.getRed())
                + Math.abs(a.getGreen() - b.getGreen())
                + Math.abs(a.getBlue() - b.getBlue());
        return distance < TOLERANCE;
    }
}
