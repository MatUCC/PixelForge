package com.pixelforge.orders;

import com.pixelforge.core.LayerInfo;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private final int id;
    private final LocalDateTime createdAt;
    private final String description;
    private final int totalCost;
    private final int totalSeconds;
    private final List<LayerInfo> layers;

    public Order(int id, String description, int totalCost, int totalSeconds, List<LayerInfo> layers) {
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

    public int getTotalCost() {
        return totalCost;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public List<LayerInfo> getLayers() {
        return layers;
    }
}
