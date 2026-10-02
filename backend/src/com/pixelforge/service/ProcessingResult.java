package com.pixelforge.service;

/** What the service returns after processing: the saved order and the final picture as PNG bytes. */
public class ProcessingResult {

    private final Order order;
    private final byte[] pngBytes;

    public ProcessingResult(Order order, byte[] pngBytes) {
        this.order = order;
        this.pngBytes = pngBytes;
    }

    public Order getOrder() {
        return order;
    }

    public byte[] getPngBytes() {
        return pngBytes;
    }
}
