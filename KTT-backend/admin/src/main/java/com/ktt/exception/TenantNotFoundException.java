package com.ktt.exception;


public class TenantNotFoundException extends RuntimeException {
    // Constructor that takes the ID directly
    public TenantNotFoundException(Long id) {
        super("Tenant not found with id: " + id);  // Simple string concatenation
    }

    // Alternative constructor that takes a custom message
    public TenantNotFoundException(String message) {
        super(message);
    }
}