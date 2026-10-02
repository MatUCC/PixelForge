package com.pixelforge.factory;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;

public interface StyleFactory {

    String getId();

    String getDisplayName();

    TreatmentDecorator createFilter(ProductImage wrapped, String parameter);

    TreatmentDecorator createBorder(ProductImage wrapped, String parameter);

    TreatmentDecorator createWatermark(ProductImage wrapped, String text);
}
