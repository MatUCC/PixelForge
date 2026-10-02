package com.pixelforge.service;

import com.pixelforge.core.Layer;

import java.time.LocalDateTime;
import java.util.List;

/** A processed order, kept in the history: what was applied, how much it cost and when. */
public class Order {

    private final int id;
    private final LocalDateTime createdAt;
    private final String description;
    private final double totalCost;
    private final int totalSeconds;
    private final List<Layer> layers;

    public Order(int id, String description, double totalCost, int totalSeconds, List<Layer> layers) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.description = description;
        this.totalCost = totalCost;
        this.totalSeconds = totalSeconds;
        this.layers = layers;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public List<Layer> getLayers() {
        return layers;
    }
}
