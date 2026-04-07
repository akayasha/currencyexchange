package com.example.currencyexchange.exception;

import com.example.currencyexchange.dto.ResponseApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseApi<Object>> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseApi.of(
                        404,
                        ex.getMessage(),
                        null,
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ResponseApi<Object>> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        log.warn("Duplicate resource: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseApi.of(
                        409,
                        ex.getMessage(),
                        null,
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseApi<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        for (var error : ex.getBindingResult().getAllErrors()) {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        }

        log.warn("Validation failed: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseApi.of(
                        400,
                        "Validation failed",
                        errors,
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(InactiveCurrencyException.class)
    public ResponseEntity<ResponseApi<Object>> handleInactiveCurrency(
            InactiveCurrencyException ex,
            HttpServletRequest request) {

        log.warn("Inactive currency: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseApi.of(
                        400,
                        ex.getMessage(),
                        null,
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseApi<Object>> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        log.debug("Static resource not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseApi.of(
                        404,
                        ex.getMessage(),
                        null,
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ResponseApi.of(
                        500,
                        "An unexpected error occurred",
                        null,
                        request.getRequestURI()
                )
        );
    }
}
