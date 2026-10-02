package com.pixelforge.pipeline;

import com.pixelforge.core.TreatmentType;

public class TreatmentRequest {
    private final TreatmentType type;
    private final String parameter;

    public TreatmentRequest(TreatmentType type, String parameter) {
        this.type = type;
        this.parameter = parameter;
    }

    public static TreatmentRequest parse(String text) {
        String[] parts = text.trim().split(":", 2);
        String typeName = parts[0].trim().toUpperCase();
        String parameter = parts.length > 1 ? parts[1].trim() : null;

        TreatmentType type;
        try {
            type = TreatmentType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown treatment: " + typeName);
        }
        if (!type.isSelectable()) {
            throw new IllegalArgumentException(typeName + " is added by the system and cannot be requested");
        }
        return new TreatmentRequest(type, parameter);
    }

    public TreatmentRequest copy() {
        return new TreatmentRequest(type, parameter);
    }

    public TreatmentType getType() {
        return type;
    }

    public String getParameter() {
        return parameter;
    }
}
