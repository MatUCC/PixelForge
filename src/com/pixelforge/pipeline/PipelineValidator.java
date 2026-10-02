package com.pixelforge.pipeline;

import com.pixelforge.core.TreatmentType;

import java.util.List;

public class PipelineValidator {

    public void validate(List<TreatmentRequest> requests) {
        if (requests.isEmpty()) {
            throw new IllegalArgumentException("Choose at least one treatment");
        }
        for (int i = 0; i < requests.size() - 1; i++) {
            if (requests.get(i).getType() == TreatmentType.COMPRESSION) {
                throw new IllegalArgumentException("COMPRESSION must be the last treatment");
            }
        }
    }
}
