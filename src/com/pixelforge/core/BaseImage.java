package com.pixelforge.core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BaseImage implements ProductImage {
    private final BufferedImage original;

    public BaseImage(BufferedImage original) {
        if (original == null) {
            throw new IllegalArgumentException("The image could not be read (only JPG or PNG are supported)");
        }
        this.original = original;
    }

    @Override
    public BufferedImage process() {
        BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(original, 0, 0, null);
        return copy;
    }

    @Override
    public int getCost() {
        return TreatmentType.BASE.getCost();
    }

    @Override
    public int getProcessingSeconds() {
        return TreatmentType.BASE.getSeconds();
    }

    @Override
    public String getDescription() {
        return TreatmentType.BASE.getDisplayName();
    }

    @Override
    public List<LayerInfo> getLayers() {
        List<LayerInfo> layers = new ArrayList<>();
        layers.add(new LayerInfo(getDescription(), getCost(), getProcessingSeconds()));
        return layers;
    }
}
