package com.example.productapi.exception;

/**
 * Thrown when a requested resource (e.g. a Product by ID) does not exist.
 * The global exception handler maps this to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
