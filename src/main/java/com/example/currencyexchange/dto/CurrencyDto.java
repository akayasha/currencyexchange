package com.example.currencyexchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Data Transfer Object for Currency.
 * Used for request/response payloads, decoupled from the entity layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyDto {

    private UUID uuid;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String code;

    @NotBlank(message = "Currency name is required")
    private String name;

    private String symbol;

    private String region;

    @JsonProperty("isActive")
    @Builder.Default
    private boolean isActive = true;
}
