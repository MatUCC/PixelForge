package com.pixelforge.prototype;

import com.pixelforge.core.TreatmentType;
import com.pixelforge.pipeline.TreatmentRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PresetRegistry {

    private static final String DEFAULT_STORE_NAME = "My Store";

    private final Map<String, PipelinePreset> prototypes = new LinkedHashMap<>();

    public PresetRegistry() {
        register(new PipelinePreset("MARKETPLACE", "Marketplace ready",
                "Transparent background, square size, store watermark and light file",
                Arrays.asList(
                        request(TreatmentType.REMOVE_BACKGROUND, null),
                        request(TreatmentType.RESIZE, null),
                        request(TreatmentType.WATERMARK, DEFAULT_STORE_NAME),
                        request(TreatmentType.COMPRESSION, null))));
        register(new PipelinePreset("SOCIAL_MEDIA", "Social media post",
                "Square photo with the style filter, the style border and your store name",
                Arrays.asList(
                        request(TreatmentType.RESIZE, null),
                        request(TreatmentType.COLOR_FILTER, null),
                        request(TreatmentType.BORDER, null),
                        request(TreatmentType.WATERMARK, DEFAULT_STORE_NAME))));
        register(new PipelinePreset("CLEAN_CATALOG", "Clean catalog",
                "Clean cut-out with the style border and a light file",
                Arrays.asList(
                        request(TreatmentType.REMOVE_BACKGROUND, null),
                        request(TreatmentType.RESIZE, null),
                        request(TreatmentType.BORDER, null),
                        request(TreatmentType.COMPRESSION, null))));
    }

    public void register(PipelinePreset prototype) {
        prototypes.put(prototype.getId(), prototype);
    }

    public PipelinePreset createCopy(String id) {
        PipelinePreset prototype = id == null ? null : prototypes.get(id.trim().toUpperCase());
        if (prototype == null) {
            throw new IllegalArgumentException("Unknown preset: " + id);
        }
        return prototype.copy();
    }

    public List<PipelinePreset> createAllCopies() {
        List<PipelinePreset> copies = new ArrayList<>();
        for (PipelinePreset prototype : prototypes.values()) {
            copies.add(prototype.copy());
        }
        return copies;
    }

    private TreatmentRequest request(TreatmentType type, String parameter) {
        return new TreatmentRequest(type, parameter);
    }
}
