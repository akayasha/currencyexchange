package com.example.currencyexchange.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for ExchangeRate.
 * Used for request/response payloads, decoupled from the entity layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateDto {

    private UUID uuid;

    @NotBlank(message = "Source currency code is required")
    private String fromCurrencyCode;

    @NotBlank(message = "Target currency code is required")
    private String toCurrencyCode;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be positive")
    private BigDecimal rate;

    private LocalDateTime lastUpdated;

    // Enriched fields from joined Currency table
    private String fromCurrencyName;
    private String toCurrencyName;
    private String fromCurrencySymbol;
    private String toCurrencySymbol;
}
