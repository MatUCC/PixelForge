package com.pixelforge.core;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * DECORATOR PATTERN - ROLE: Concrete Component.
 *
 * The original photo uploaded by the store owner. It is always the innermost
 * object of the chain: every decorator ends up wrapping one of these.
 *
 * The base service (validating and normalizing the file) has a fixed price.
 */
public class BaseImage implements ProductImage {

    private static final double BASE_COST = 500;
    private static final int BASE_SECONDS = 1;

    private final BufferedImage original;

    public BaseImage(BufferedImage uploaded) {
        // Normalize: every image is converted to RGB so all decorators work the same way.
        this.original = new BufferedImage(uploaded.getWidth(), uploaded.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = original.createGraphics();
        g.drawImage(uploaded, 0, 0, java.awt.Color.WHITE, null);
        g.dispose();
    }

    @Override
    public BufferedImage render() {
        // Return a copy so decorators never modify the original upload.
        BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return copy;
    }

    @Override
    public double getCost() {
        return BASE_COST;
    }

    @Override
    public int getProcessingSeconds() {
        return BASE_SECONDS;
    }

    @Override
    public String getDescription() {
        return "Base image";
    }

    @Override
    public List<Layer> getLayers() {
        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer("Base image", BASE_COST, BASE_SECONDS));
        return layers;
    }
}
