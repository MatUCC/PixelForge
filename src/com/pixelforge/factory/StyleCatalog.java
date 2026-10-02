package com.pixelforge.factory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StyleCatalog {

    private static final String DEFAULT_STYLE_ID = "CLASSIC";

    private final Map<String, StyleFactory> factories = new LinkedHashMap<>();

    public StyleCatalog() {
        register(new ClassicStyleFactory());
        register(new VibrantStyleFactory());
        register(new FreshStyleFactory());
    }

    public void register(StyleFactory factory) {
        factories.put(factory.getId(), factory);
    }

    public StyleFactory find(String id) {
        String key = id == null || id.isBlank() ? DEFAULT_STYLE_ID : id.trim().toUpperCase();
        StyleFactory factory = factories.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown style: " + id);
        }
        return factory;
    }

    public List<StyleFactory> findAll() {
        return new ArrayList<>(factories.values());
    }
}
