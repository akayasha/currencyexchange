package com.example.currencyexchange.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for a currency conversion response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversionResponseDto {

    private String fromCurrencyCode;
    private String toCurrencyCode;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
    private BigDecimal rateApplied;
    private LocalDateTime timestamp;
    private String source; // "db" or "external-api"
}
