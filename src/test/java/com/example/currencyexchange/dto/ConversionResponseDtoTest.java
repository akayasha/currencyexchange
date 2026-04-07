package com.example.currencyexchange.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ConversionResponseDto.
 */
@DisplayName("ConversionResponseDto Tests")
class ConversionResponseDtoTest {

    @Test
    @DisplayName("Should create valid ConversionResponseDto with builder")
    void shouldCreateValidConversionResponseDto_WithBuilder() {
        LocalDateTime timestamp = LocalDateTime.now();

        ConversionResponseDto dto = ConversionResponseDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("92.50"))
                .rateApplied(new BigDecimal("0.9250"))
                .timestamp(timestamp)
                .source("db")
                .build();

        assertThat(dto.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("92.50"));
        assertThat(dto.getRateApplied()).isEqualByComparingTo(new BigDecimal("0.9250"));
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getSource()).isEqualTo("db");
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void shouldHandleGettersAndSettersCorrectly() {
        ConversionResponseDto dto = new ConversionResponseDto();
        LocalDateTime timestamp = LocalDateTime.now();

        dto.setFromCurrencyCode("GBP");
        dto.setToCurrencyCode("JPY");
        dto.setAmount(new BigDecimal("200.00"));
        dto.setConvertedAmount(new BigDecimal("25000.00"));
        dto.setRateApplied(new BigDecimal("125.00"));
        dto.setTimestamp(timestamp);
        dto.setSource("external-api");

        assertThat(dto.getFromCurrencyCode()).isEqualTo("GBP");
        assertThat(dto.getToCurrencyCode()).isEqualTo("JPY");
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(dto.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(dto.getRateApplied()).isEqualByComparingTo(new BigDecimal("125.00"));
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getSource()).isEqualTo("external-api");
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void shouldHandleNullValuesCorrectly() {
        ConversionResponseDto dto = new ConversionResponseDto();

        assertThat(dto.getFromCurrencyCode()).isNull();
        assertThat(dto.getToCurrencyCode()).isNull();
        assertThat(dto.getAmount()).isNull();
        assertThat(dto.getConvertedAmount()).isNull();
        assertThat(dto.getRateApplied()).isNull();
        assertThat(dto.getTimestamp()).isNull();
        assertThat(dto.getSource()).isNull();
    }

    @Test
    @DisplayName("Should handle no-args constructor")
    void shouldHandleNoArgsConstructor() {
        ConversionResponseDto dto = new ConversionResponseDto();

        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Should handle all-args constructor")
    void shouldHandleAllArgsConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();

        ConversionResponseDto dto = new ConversionResponseDto(
                "USD", "EUR",
                new BigDecimal("100.00"),
                new BigDecimal("92.50"),
                new BigDecimal("0.9250"),
                timestamp,
                "db"
        );

        assertThat(dto.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(dto.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("92.50"));
        assertThat(dto.getRateApplied()).isEqualByComparingTo(new BigDecimal("0.9250"));
        assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        assertThat(dto.getSource()).isEqualTo("db");
    }

    @Test
    @DisplayName("Should handle different source values")
    void shouldHandleDifferentSourceValues() {
        ConversionResponseDto dbSource = ConversionResponseDto.builder()
                .source("db")
                .build();

        ConversionResponseDto externalSource = ConversionResponseDto.builder()
                .source("external-api")
                .build();

        assertThat(dbSource.getSource()).isEqualTo("db");
        assertThat(externalSource.getSource()).isEqualTo("external-api");
    }
}
