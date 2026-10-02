package com.pixelforge.orders;

import com.pixelforge.core.ProductImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory order history (it is cleared when the server restarts; no database needed for the workshop).
 */
public class OrderHistory {

    private final List<Order> orders = new ArrayList<>();
    private int nextId = 1;

    public synchronized Order register(ProductImage processedImage) {
        Order order = new Order(
                nextId++,
                processedImage.getDescription(),
                processedImage.getCost(),
                processedImage.getProcessingSeconds(),
                processedImage.getLayers());
        orders.add(order);
        return order;
    }

    public synchronized List<Order> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }
}
