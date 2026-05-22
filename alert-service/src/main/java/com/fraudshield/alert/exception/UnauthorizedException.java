package com.fraudshield.alert.exception;

public class UnauthorizedException
        extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}