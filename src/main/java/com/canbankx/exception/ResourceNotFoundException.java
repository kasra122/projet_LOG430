package com.canbankx.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends CustomException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            "RESOURCE_NOT_FOUND",
            String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue),
            HttpStatus.NOT_FOUND.value()
        );
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND.value());
    }
}
