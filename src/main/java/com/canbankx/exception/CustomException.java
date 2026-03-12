package com.canbankx.exception;

public class CustomException extends RuntimeException {
    private final String code;
    private final int httpStatus;

    public CustomException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
