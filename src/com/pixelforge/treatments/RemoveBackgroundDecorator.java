package com.pixelforge.treatments;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.core.TreatmentType;

import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - Role: CONCRETE DECORATOR
 *
 * Makes the background transparent. Simplified approach (no AI):
 * the color of the top-left corner is taken as "the background", and every
 * pixel similar to it becomes transparent. Works well with studio photos.
 */
public class RemoveBackgroundDecorator extends TreatmentDecorator {

    /** How different a pixel can be from the background color and still count as background. */
    private static final int TOLERANCE = 60;

    public RemoveBackgroundDecorator(ProductImage wrapped) {
        super(wrapped, TreatmentType.REMOVE_BACKGROUND);
    }

    @Override
    protected BufferedImage applyTo(BufferedImage image) {
        int backgroundColor = image.getRGB(0, 0);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (isSimilar(image.getRGB(x, y), backgroundColor)) {
                    image.setRGB(x, y, 0x00000000); // fully transparent
                }
            }
        }
        return image;
    }

    private boolean isSimilar(int colorA, int colorB) {
        int redDiff = Math.abs(((colorA >> 16) & 0xFF) - ((colorB >> 16) & 0xFF));
        int greenDiff = Math.abs(((colorA >> 8) & 0xFF) - ((colorB >> 8) & 0xFF));
        int blueDiff = Math.abs((colorA & 0xFF) - (colorB & 0xFF));
        return redDiff + greenDiff + blueDiff <= TOLERANCE;
    }
}
