package com.pixelforge.core;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * DECORATOR PATTERN - ROLE: Component.
 *
 * Common interface for a product photo, whether it is the plain original
 * image or an image wrapped by one or more treatments (decorators).
 *
 * The client code (PipelineBuilder, the API) only talks to this interface,
 * so it never needs to know how many layers are wrapped around the image.
 */
public interface ProductImage {

    /** Returns the final picture after applying every layer, from the inside out. */
    BufferedImage render();

    /** Total cost in Colombian pesos (COP) of this image plus all its layers. */
    double getCost();

    /** Estimated total processing time in seconds. */
    int getProcessingSeconds();

    /** Human readable summary, e.g. "Base image + Remove background + Watermark". */
    String getDescription();

    /** One entry per layer (innermost first). Used by the frontend to draw the breakdown. */
    List<Layer> getLayers();
}
