package com.pixelforge.treatments;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.core.TreatmentType;

import java.awt.image.BufferedImage;

public class ColorFilterDecorator extends TreatmentDecorator {
    public enum FilterType {
        GRAYSCALE, BRIGHTEN, HIGH_CONTRAST;

        public static FilterType fromText(String text) {
            if (text == null) {
                throw new IllegalArgumentException("COLOR_FILTER needs a type, e.g. COLOR_FILTER:GRAYSCALE");
            }
            try {
                return valueOf(text.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown filter: " + text + " (use GRAYSCALE, BRIGHTEN or HIGH_CONTRAST)");
            }
        }
    }

    private final FilterType filter;

    public ColorFilterDecorator(ProductImage wrapped, FilterType filter) {
        super(wrapped, TreatmentType.COLOR_FILTER);
        this.filter = filter;
    }

    @Override
    protected String getOwnName() {
        return super.getOwnName() + " (" + filter + ")";
    }

    @Override
    protected BufferedImage applyTo(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, transformPixel(image.getRGB(x, y)));
            }
        }
        return image;
    }

    private int transformPixel(int argb) {
        int alpha = (argb >> 24) & 0xFF;
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        switch (filter) {
            case GRAYSCALE:
                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                red = gray;
                green = gray;
                blue = gray;
                break;
            case BRIGHTEN:
                red = clamp(red + 40);
                green = clamp(green + 40);
                blue = clamp(blue + 40);
                break;
            case HIGH_CONTRAST:
                red = clamp((int) ((red - 128) * 1.5 + 128));
                green = clamp((int) ((green - 128) * 1.5 + 128));
                blue = clamp((int) ((blue - 128) * 1.5 + 128));
                break;
        }
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
