package com.canbankx.customer.config;

import com.canbankx.customer.service.CustomerService;
import com.canbankx.customer.service.SettlementService;
import com.canbankx.customer.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionService.TransactionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(TransactionService.TransactionException ex) {
        log.error("Transaction error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .error("Transaction Error")
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(SettlementService.SettlementException.class)
    public ResponseEntity<ErrorResponse> handleSettlementException(SettlementService.SettlementException ex) {
        log.error("Settlement error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(404)
                        .error("Settlement Error")
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(CustomerService.CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCustomerAlreadyExistsException(CustomerService.CustomerAlreadyExistsException ex) {
        log.error("Customer already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(409)
                        .error("Customer Already Exists")
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .error("Validation Failed")
                        .message("Invalid request parameters")
                        .details(errors)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(500)
                        .error("Internal Server Error")
                        .message("An unexpected error occurred")
                        .build()
        );
    }

    public static class ErrorResponse {
        public Instant timestamp;
        public int status;
        public String error;
        public String message;
        public Map<String, String> details;

        public ErrorResponse(Instant timestamp, int status, String error, String message, Map<String, String> details) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.details = details;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Instant timestamp;
            private int status;
            private String error;
            private String message;
            private Map<String, String> details;

            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder status(int status) {
                this.status = status;
                return this;
            }

            public Builder error(String error) {
                this.error = error;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder details(Map<String, String> details) {
                this.details = details;
                return this;
            }

            public ErrorResponse build() {
                return new ErrorResponse(timestamp, status, error, message, details);
            }
        }
    }
}
