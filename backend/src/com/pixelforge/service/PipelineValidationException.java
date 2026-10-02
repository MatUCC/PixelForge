package com.pixelforge.service;

/** Thrown when an order breaks a business rule. The API returns it as HTTP 400. */
public class PipelineValidationException extends RuntimeException {

    public PipelineValidationException(String message) {
        super(message);
    }
}
