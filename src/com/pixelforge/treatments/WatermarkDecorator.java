package com.pixelforge.treatments;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.core.TreatmentType;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class WatermarkDecorator extends TreatmentDecorator {
    private final String text;
    private final Color textColor;

    public WatermarkDecorator(ProductImage wrapped, String text) {
        this(wrapped, text, Color.WHITE);
    }

    public WatermarkDecorator(ProductImage wrapped, String text, Color textColor) {
        super(wrapped, TreatmentType.WATERMARK);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Watermark needs a text, e.g. WATERMARK:MyStore");
        }
        this.text = text;
        this.textColor = textColor;
    }

    @Override
    protected String getOwnName() {
        return super.getOwnName() + " (\"" + text + "\")";
    }

    @Override
    protected BufferedImage applyTo(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int fontSize = Math.max(12, image.getWidth() / 15);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));

        int textWidth = g.getFontMetrics().stringWidth(text);
        int margin = fontSize / 2;
        int x = image.getWidth() - textWidth - margin;
        int y = image.getHeight() - margin;

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g.setColor(Color.BLACK);
        g.drawString(text, x + 2, y + 2);
        g.setColor(textColor);
        g.drawString(text, x, y);
        g.dispose();
        return image;
    }
}
