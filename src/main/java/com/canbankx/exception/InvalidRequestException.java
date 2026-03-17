package com.canbankx.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends CustomException {
    public InvalidRequestException(String message) {
        super("INVALID_REQUEST", message, HttpStatus.BAD_REQUEST.value());
    }

    public InvalidRequestException(String fieldName, String reason) {
        super(
            "INVALID_REQUEST",
            String.format("Invalid %s: %s", fieldName, reason),
            HttpStatus.BAD_REQUEST.value()
        );
    }
}
