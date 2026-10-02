package com.pixelforge.core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * DECORATOR PATTERN - Role: CONCRETE COMPONENT
 *
 * The original photo uploaded by the store. It is the innermost object:
 * every decorator ends up wrapping one of these.
 */
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
        // "Validation and normalization": we always work on an ARGB copy,
        // so decorators can use transparency and the original is never modified.
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
