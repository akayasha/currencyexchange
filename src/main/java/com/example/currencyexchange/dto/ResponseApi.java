package com.example.currencyexchange.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper for all endpoints.
 * Provides a consistent response format across the entire application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API Response wrapper")
public class ResponseApi<T> {

    @Schema(description = "HTTP status code", example = "200")
    private int code;

    @Schema(description = "Response message describing the result", example = "Operation successful")
    private String message;

    @Schema(description = "Response data payload")
    private T data;

    @Schema(description = "Timestamp when the response was created")
    private LocalDateTime timestamp;

    @Schema(description = "Error details if applicable")
    private String error;

    /**
     * Create a success response with data
     */
    public static <T> ResponseApi<T> success(int code, String message, T data) {
        return ResponseApi.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response without data
     */
    public static <T> ResponseApi<T> success(int code, String message) {
        return ResponseApi.<T>builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response
     */
    public static <T> ResponseApi<T> error(int code, String message, String error) {
        return ResponseApi.<T>builder()
                .code(code)
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a response with all fields
     */
    public static <T> ResponseApi<T> of(int code, String message, T data, String path) {
        return ResponseApi.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

