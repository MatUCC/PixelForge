package com.pixelforge.service;

import com.pixelforge.decorator.TreatmentType;

/**
 * One treatment chosen by the user in the frontend: its type and an optional
 * parameter (store name, filter, color, size...).
 *
 * It arrives from the frontend as text with the format "TYPE:parameter",
 * for example "WATERMARK:Tienda Doña Rosa" or "COMPRESSION".
 */
public class TreatmentRequest {

    private final TreatmentType type;
    private final String parameter;

    public TreatmentRequest(TreatmentType type, String parameter) {
        this.type = type;
        this.parameter = parameter == null ? "" : parameter;
    }

    public static TreatmentRequest fromText(String text) {
        String[] parts = text.split(":", 2);
        TreatmentType type;
        try {
            type = TreatmentType.valueOf(parts[0].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PipelineValidationException("Unknown treatment: " + parts[0]);
        }
        if (!type.isSelectableByUser()) {
            throw new PipelineValidationException("Treatment " + type + " cannot be selected manually");
        }
        String parameter = parts.length > 1 ? parts[1] : "";
        return new TreatmentRequest(type, parameter);
    }

    public TreatmentType getType() {
        return type;
    }

    public String getParameter() {
        return parameter;
    }
}
