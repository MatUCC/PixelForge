package com.pixelforge.pipeline;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentType;
import com.pixelforge.factory.StyleFactory;

import java.awt.image.BufferedImage;
import java.util.List;

public class PipelineDirector {

    private final PipelineBuilder builder;

    public PipelineDirector(PipelineBuilder builder) {
        this.builder = builder;
    }

    public ProductImage construct(BufferedImage photo, StyleFactory style, List<TreatmentRequest> requests) {
        builder.reset().photo(photo).style(style);
        for (TreatmentRequest request : requests) {
            builder.addTreatment(request);
        }
        return builder.build();
    }

    public ProductImage constructMarketplacePhoto(BufferedImage photo, StyleFactory style, String storeName) {
        return builder.reset()
                .photo(photo)
                .style(style)
                .addTreatment(new TreatmentRequest(TreatmentType.REMOVE_BACKGROUND, null))
                .addTreatment(new TreatmentRequest(TreatmentType.RESIZE, null))
                .addTreatment(new TreatmentRequest(TreatmentType.WATERMARK, storeName))
                .addTreatment(new TreatmentRequest(TreatmentType.COMPRESSION, null))
                .build();
    }
}
