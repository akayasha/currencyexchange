package com.example.currencyexchange.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Projection DTO for exchange rates joined with currency details.
 * Used for the JOIN query result between exchange_rate and currency tables.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateWithCurrencyDto {

    private UUID uuid;
    private String fromCurrencyCode;
    private String fromCurrencyName;
    private String fromCurrencySymbol;
    private String toCurrencyCode;
    private String toCurrencyName;
    private String toCurrencySymbol;
    private BigDecimal rate;
    private LocalDateTime lastUpdated;
}
