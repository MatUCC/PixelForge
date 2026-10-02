package com.pixelforge.core;

/**
 * Simple data object describing one layer of the decorator stack.
 * Used to show the cost/time breakdown per treatment.
 */
public class LayerInfo {

    private final String name;
    private final int cost;
    private final int seconds;

    public LayerInfo(String name, int cost, int seconds) {
        this.name = name;
        this.cost = cost;
        this.seconds = seconds;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getSeconds() {
        return seconds;
    }
}
