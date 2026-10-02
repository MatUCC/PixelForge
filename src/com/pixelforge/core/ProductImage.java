package com.pixelforge.core;

import java.awt.image.BufferedImage;
import java.util.List;

public interface ProductImage {
    BufferedImage process();

    int getCost();

    int getProcessingSeconds();

    String getDescription();

    List<LayerInfo> getLayers();
}
