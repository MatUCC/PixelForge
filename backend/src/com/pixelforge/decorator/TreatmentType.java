package com.pixelforge.decorator;

/**
 * Catalog of every treatment PixelForge sells.
 *
 * Keeping price, time and labels here means:
 *  - each concrete decorator does not repeat this data, and
 *  - the frontend can ask the API for the catalog and build its menu dynamically.
 *
 * To add a NEW treatment: add a constant here + create its decorator class
 * + add one line in PipelineBuilder. No existing decorator has to change
 * (Open/Closed principle).
 */
public enum TreatmentType {

    //                 label                cost   sec  parameter hint (null = no parameter)
    REMOVE_BACKGROUND("Remove background",  1500,  4,   null),
    RESIZE(           "Resize",              300,  1,   "Size in px (e.g. 1080)"),
    COLOR_FILTER(     "Color filter",        400,  1,   "GRAYSCALE, SEPIA, BRIGHTNESS or CONTRAST"),
    WATERMARK(        "Watermark",           600,  2,   "Store name"),
    FRAME(            "Frame",               200,  1,   "Hex color (e.g. #1E3A8A)"),
    COMPRESSION(      "Compression",         250,  1,   null),
    DISCOUNT(         "Volume discount 10%",   0,  0,   null); // added automatically, not selectable

    private final String label;
    private final double cost;
    private final int seconds;
    private final String parameterHint;

    TreatmentType(String label, double cost, int seconds, String parameterHint) {
        this.label = label;
        this.cost = cost;
        this.seconds = seconds;
        this.parameterHint = parameterHint;
    }

    public String getLabel() {
        return label;
    }

    public double getCost() {
        return cost;
    }

    public int getSeconds() {
        return seconds;
    }

    public String getParameterHint() {
        return parameterHint;
    }

    /** The discount is applied by the system, the user cannot pick it. */
    public boolean isSelectableByUser() {
        return this != DISCOUNT;
    }
}
