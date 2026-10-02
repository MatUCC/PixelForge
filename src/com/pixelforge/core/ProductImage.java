package com.pixelforge.core;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * DECORATOR PATTERN - Role: COMPONENT
 *
 * Common interface for "a product photo that can be processed".
 * Both the original image (BaseImage) and every treatment (decorators)
 * implement it, so the client never knows how many layers are stacked.
 */
public interface ProductImage {

    /** Returns the final image after applying every layer, in order. */
    BufferedImage process();

    /** Total cost (COP) of all layers. */
    int getCost();

    /** Total estimated processing time (seconds) of all layers. */
    int getProcessingSeconds();

    /** Human-readable description, e.g. "Base image + Remove background + Watermark". */
    String getDescription();

    /** One entry per layer, from the innermost (base image) to the outermost. */
    List<LayerInfo> getLayers();
}
