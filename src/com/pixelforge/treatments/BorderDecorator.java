package com.pixelforge.treatments;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.core.TreatmentType;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - Role: CONCRETE DECORATOR
 *
 * Draws a colored frame around the photo.
 */
public class BorderDecorator extends TreatmentDecorator {

    private final Color color;

    /** @param hexColor color like "FF0000" or "#FF0000" */
    public BorderDecorator(ProductImage wrapped, String hexColor) {
        super(wrapped, TreatmentType.BORDER);
        this.color = parseColor(hexColor);
    }

    @Override
    protected String getOwnName() {
        return super.getOwnName() + String.format(" (#%06X)", color.getRGB() & 0xFFFFFF);
    }

    @Override
    protected BufferedImage applyTo(BufferedImage image) {
        int thickness = Math.max(4, image.getWidth() / 40);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness));
        g.drawRect(thickness / 2, thickness / 2, image.getWidth() - thickness, image.getHeight() - thickness);
        g.dispose();
        return image;
    }

    private static Color parseColor(String hexColor) {
        if (hexColor == null || hexColor.isBlank()) {
            return Color.BLACK; // default frame color
        }
        try {
            return new Color(Integer.parseInt(hexColor.replace("#", ""), 16));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid border color: " + hexColor + " (use something like FF0000)");
        }
    }
}
