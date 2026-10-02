package com.pixelforge.decorator;

import com.pixelforge.core.ProductImage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - ROLE: Concrete Decorator.
 *
 * Draws a colored frame around the picture. Thickness is 3% of the width.
 */
public class FrameDecorator extends TreatmentDecorator {

    private static final Color DEFAULT_COLOR = new Color(0x1E3A8A);

    private final Color color;
    private final String colorText;

    public FrameDecorator(ProductImage wrapped, String hexColor) {
        super(wrapped, TreatmentType.FRAME);
        this.color = parseColor(hexColor);
        this.colorText = String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    private Color parseColor(String hex) {
        try {
            return Color.decode(hex.trim().startsWith("#") ? hex.trim() : "#" + hex.trim());
        } catch (Exception e) {
            return DEFAULT_COLOR;
        }
    }

    @Override
    protected String getLayerName() {
        return type.getLabel() + " (" + colorText + ")";
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage image) {
        int thickness = Math.max(4, image.getWidth() * 3 / 100);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness));
        int half = thickness / 2;
        g.drawRect(half, half, image.getWidth() - thickness, image.getHeight() - thickness);
        g.dispose();
        return image;
    }
}
