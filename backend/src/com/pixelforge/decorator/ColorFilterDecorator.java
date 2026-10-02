package com.pixelforge.decorator;

import com.pixelforge.core.ProductImage;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - ROLE: Concrete Decorator.
 *
 * Applies a color filter to every pixel. The same decorator can be added
 * several times with different filters (e.g. BRIGHTNESS and then CONTRAST),
 * which shows that decorators can be stacked freely.
 */
public class ColorFilterDecorator extends TreatmentDecorator {

    public enum Filter { GRAYSCALE, SEPIA, BRIGHTNESS, CONTRAST }

    private final Filter filter;

    public ColorFilterDecorator(ProductImage wrapped, String filterParameter) {
        super(wrapped, TreatmentType.COLOR_FILTER);
        this.filter = parseFilter(filterParameter);
    }

    private Filter parseFilter(String value) {
        try {
            return Filter.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return Filter.GRAYSCALE;
        }
    }

    @Override
    protected String getLayerName() {
        return type.getLabel() + " (" + filter + ")";
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = new Color(image.getRGB(x, y));
                image.setRGB(x, y, transform(c).getRGB());
            }
        }
        return image;
    }

    private Color transform(Color c) {
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        switch (filter) {
            case GRAYSCALE: {
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                return new Color(gray, gray, gray);
            }
            case SEPIA: {
                int sr = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int sg = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int sb = (int) (0.272 * r + 0.534 * g + 0.131 * b);
                return new Color(clamp(sr), clamp(sg), clamp(sb));
            }
            case BRIGHTNESS:
                return new Color(clamp(r + 40), clamp(g + 40), clamp(b + 40));
            case CONTRAST:
            default: {
                double factor = 1.4;
                return new Color(clamp((int) ((r - 128) * factor + 128)),
                                 clamp((int) ((g - 128) * factor + 128)),
                                 clamp((int) ((b - 128) * factor + 128)));
            }
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
