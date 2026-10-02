package com.pixelforge.decorator;

import com.pixelforge.core.ProductImage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * DECORATOR PATTERN - ROLE: Concrete Decorator.
 *
 * Writes the store name, semi-transparent, in the bottom-right corner.
 * The font size depends on the image width, so the ORDER matters:
 * watermark before resizing = the text gets scaled too; after = it keeps its size.
 */
public class WatermarkDecorator extends TreatmentDecorator {

    private final String text;

    public WatermarkDecorator(ProductImage wrapped, String text) {
        super(wrapped, TreatmentType.WATERMARK);
        this.text = (text == null || text.isBlank()) ? "PixelForge" : text.trim();
    }

    @Override
    protected String getLayerName() {
        return type.getLabel() + " (\"" + text + "\")";
    }

    @Override
    protected BufferedImage applyEffect(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int fontSize = Math.max(14, image.getWidth() / 18);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        FontMetrics metrics = g.getFontMetrics();

        int margin = fontSize / 2;
        int x = image.getWidth() - metrics.stringWidth(text) - margin;
        int y = image.getHeight() - margin;

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g.setColor(Color.BLACK);
        g.drawString(text, x + 2, y + 2); // shadow so it is readable on white
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
        g.dispose();
        return image;
    }
}
