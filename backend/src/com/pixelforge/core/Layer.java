package com.pixelforge.core;

/**
 * Simple data object describing ONE layer of the decorator chain:
 * its name, what it adds to the cost and what it adds to the processing time.
 */
public class Layer {

    private final String name;
    private final double cost;
    private final int seconds;

    public Layer(String name, double cost, int seconds) {
        this.name = name;
        this.cost = cost;
        this.seconds = seconds;
    }

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public int getSeconds() {
        return seconds;
    }
}
