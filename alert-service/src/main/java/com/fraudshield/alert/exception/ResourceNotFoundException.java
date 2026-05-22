package com.fraudshield.alert.exception;

public class ResourceNotFoundException
        extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
