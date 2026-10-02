package com.pixelforge.pipeline;

import com.pixelforge.core.ProductImage;
import com.pixelforge.factory.StyleFactory;

@FunctionalInterface
public interface DecoratorCreator {

    ProductImage create(ProductImage wrapped, String parameter, StyleFactory style);
}
