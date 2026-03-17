package com.canbankx.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends CustomException {
    public InsufficientFundsException(String accountId, String currentBalance, String requestedAmount) {
        super(
            "INSUFFICIENT_FUNDS",
            String.format("Account %s has balance %s but requested %s", accountId, currentBalance, requestedAmount),
            HttpStatus.PAYMENT_REQUIRED.value()
        );
    }

    public InsufficientFundsException(String message) {
        super("INSUFFICIENT_FUNDS", message, HttpStatus.PAYMENT_REQUIRED.value());
    }
}
