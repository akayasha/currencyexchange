package com.example.currencyexchange.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for exchange rate history.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateHistoryDto {

    private UUID uuid;
    private String fromCurrencyCode;
    private String toCurrencyCode;
    private BigDecimal oldRate;
    private BigDecimal newRate;
    private String changeReason;
    private LocalDateTime createdAt;
    private String createdBy;
}

