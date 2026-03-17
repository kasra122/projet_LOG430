package com.canbankx.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED.value());
    }

    public UnauthorizedException() {
        super("UNAUTHORIZED", "Invalid credentials or token", HttpStatus.UNAUTHORIZED.value());
    }
}
