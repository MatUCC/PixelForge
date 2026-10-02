package com.pixelforge.core;

public enum TreatmentType {
    BASE            ("Base image",          500,        1,       null),
    REMOVE_BACKGROUND("Remove background",  1500,       4,       null),
    RESIZE          ("Resize 1080x1080",    300,        1,       null),
    COLOR_FILTER    ("Color filter",        400,        1,       "GRAYSCALE | BRIGHTEN | HIGH_CONTRAST (empty = style default)"),
    WATERMARK       ("Watermark",           600,        2,       "text to print"),
    BORDER          ("Border",              200,        1,       "hex color, e.g. FF0000 (empty = style default)"),
    COMPRESSION     ("Web compression",     250,        1,       null),
    DISCOUNT        ("Discount 10%",        0,          0,       null);

    private final String displayName;
    private final int cost;
    private final int seconds;
    private final String parameterHint;

    TreatmentType(String displayName, int cost, int seconds, String parameterHint) {
        this.displayName = displayName;
        this.cost = cost;
        this.seconds = seconds;
        this.parameterHint = parameterHint;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCost() {
        return cost;
    }

    public int getSeconds() {
        return seconds;
    }

    public String getParameterHint() {
        return parameterHint;
    }

    public boolean isSelectable() {
        return this != BASE && this != DISCOUNT;
    }
}
