package com.example.currencyexchange.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for a currency conversion request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversionRequestDto {

    @NotBlank(message = "Source currency code is required")
    private String fromCurrencyCode;

    @NotBlank(message = "Target currency code is required")
    private String toCurrencyCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;
}
