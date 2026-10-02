package com.pixelforge.pipeline;

import com.pixelforge.core.ProductImage;
import com.pixelforge.factory.StyleFactory;

import java.awt.image.BufferedImage;

public interface PipelineBuilder {

    PipelineBuilder reset();

    PipelineBuilder photo(BufferedImage photo);

    PipelineBuilder style(StyleFactory style);

    PipelineBuilder addTreatment(TreatmentRequest request);

    ProductImage build();
}
