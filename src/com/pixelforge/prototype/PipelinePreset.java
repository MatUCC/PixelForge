package com.pixelforge.prototype;

import com.pixelforge.core.TreatmentType;
import com.pixelforge.pipeline.TreatmentRequest;

import java.util.ArrayList;
import java.util.List;

public class PipelinePreset implements Prototype<PipelinePreset> {

    private final String id;
    private final String name;
    private final String description;
    private final List<TreatmentRequest> treatments;

    public PipelinePreset(String id, String name, String description, List<TreatmentRequest> treatments) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.treatments = new ArrayList<>(treatments);
    }

    @Override
    public PipelinePreset copy() {
        List<TreatmentRequest> treatmentsCopy = new ArrayList<>();
        for (TreatmentRequest treatment : treatments) {
            treatmentsCopy.add(treatment.copy());
        }
        return new PipelinePreset(id, name, description, treatmentsCopy);
    }

    public PipelinePreset withStoreName(String storeName) {
        if (storeName == null || storeName.isBlank()) {
            return this;
        }
        for (int i = 0; i < treatments.size(); i++) {
            if (treatments.get(i).getType() == TreatmentType.WATERMARK) {
                treatments.set(i, new TreatmentRequest(TreatmentType.WATERMARK, storeName.trim()));
            }
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<TreatmentRequest> getTreatments() {
        return new ArrayList<>(treatments);
    }
}
