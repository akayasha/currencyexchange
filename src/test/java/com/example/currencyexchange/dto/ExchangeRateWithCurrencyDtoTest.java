package com.example.currencyexchange.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ExchangeRateWithCurrencyDto.
 */
@DisplayName("ExchangeRateWithCurrencyDto Tests")
class ExchangeRateWithCurrencyDtoTest {

    @Test
    @DisplayName("Should create valid ExchangeRateWithCurrencyDto with builder")
    void shouldCreateValidExchangeRateWithCurrencyDto_WithBuilder() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime lastUpdated = LocalDateTime.now();

        ExchangeRateWithCurrencyDto dto = ExchangeRateWithCurrencyDto.builder()
                .uuid(uuid)
                .fromCurrencyCode("USD")
                .fromCurrencyName("United States Dollar")
                .fromCurrencySymbol("$")
                .toCurrencyCode("EUR")
                .toCurrencyName("Euro")
                .toCurrencySymbol("€")
                .rate(new BigDecimal("0.9250"))
                .lastUpdated(lastUpdated)
                .build();

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getFromCurrencyName()).isEqualTo("United States Dollar");
        assertThat(dto.getFromCurrencySymbol()).isEqualTo("$");
        assertThat(dto.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(dto.getToCurrencyName()).isEqualTo("Euro");
        assertThat(dto.getToCurrencySymbol()).isEqualTo("€");
        assertThat(dto.getRate()).isEqualByComparingTo(new BigDecimal("0.9250"));
        assertThat(dto.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void shouldHandleGettersAndSettersCorrectly() {
        ExchangeRateWithCurrencyDto dto = new ExchangeRateWithCurrencyDto();
        UUID uuid = UUID.randomUUID();
        LocalDateTime lastUpdated = LocalDateTime.now();

        dto.setUuid(uuid);
        dto.setFromCurrencyCode("GBP");
        dto.setFromCurrencyName("British Pound");
        dto.setFromCurrencySymbol("£");
        dto.setToCurrencyCode("JPY");
        dto.setToCurrencyName("Japanese Yen");
        dto.setToCurrencySymbol("¥");
        dto.setRate(new BigDecimal("150.5"));
        dto.setLastUpdated(lastUpdated);

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getFromCurrencyCode()).isEqualTo("GBP");
        assertThat(dto.getFromCurrencyName()).isEqualTo("British Pound");
        assertThat(dto.getFromCurrencySymbol()).isEqualTo("£");
        assertThat(dto.getToCurrencyCode()).isEqualTo("JPY");
        assertThat(dto.getToCurrencyName()).isEqualTo("Japanese Yen");
        assertThat(dto.getToCurrencySymbol()).isEqualTo("¥");
        assertThat(dto.getRate()).isEqualByComparingTo(new BigDecimal("150.5"));
        assertThat(dto.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void shouldHandleNullValuesCorrectly() {
        ExchangeRateWithCurrencyDto dto = new ExchangeRateWithCurrencyDto();

        assertThat(dto.getUuid()).isNull();
        assertThat(dto.getFromCurrencyCode()).isNull();
        assertThat(dto.getFromCurrencyName()).isNull();
        assertThat(dto.getFromCurrencySymbol()).isNull();
        assertThat(dto.getToCurrencyCode()).isNull();
        assertThat(dto.getToCurrencyName()).isNull();
        assertThat(dto.getToCurrencySymbol()).isNull();
        assertThat(dto.getRate()).isNull();
        assertThat(dto.getLastUpdated()).isNull();
    }

    @Test
    @DisplayName("Should handle no-args constructor")
    void shouldHandleNoArgsConstructor() {
        ExchangeRateWithCurrencyDto dto = new ExchangeRateWithCurrencyDto();

        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Should handle all-args constructor")
    void shouldHandleAllArgsConstructor() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime lastUpdated = LocalDateTime.now();

        ExchangeRateWithCurrencyDto dto = new ExchangeRateWithCurrencyDto(
                uuid,
                "USD",
                "United States Dollar",
                "$",
                "EUR",
                "Euro",
                "€",
                new BigDecimal("0.9250"),
                lastUpdated
        );

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getFromCurrencyName()).isEqualTo("United States Dollar");
        assertThat(dto.getFromCurrencySymbol()).isEqualTo("$");
        assertThat(dto.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(dto.getToCurrencyName()).isEqualTo("Euro");
        assertThat(dto.getToCurrencySymbol()).isEqualTo("€");
        assertThat(dto.getRate()).isEqualByComparingTo(new BigDecimal("0.9250"));
        assertThat(dto.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Should handle multiple currency pairs")
    void shouldHandleMultipleCurrencyPairs() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime lastUpdated = LocalDateTime.now();

        ExchangeRateWithCurrencyDto usdToEur = ExchangeRateWithCurrencyDto.builder()
                .uuid(uuid)
                .fromCurrencyCode("USD")
                .fromCurrencyName("United States Dollar")
                .fromCurrencySymbol("$")
                .toCurrencyCode("EUR")
                .toCurrencyName("Euro")
                .toCurrencySymbol("€")
                .rate(new BigDecimal("0.9250"))
                .lastUpdated(lastUpdated)
                .build();

        ExchangeRateWithCurrencyDto gbpToJpy = ExchangeRateWithCurrencyDto.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("GBP")
                .fromCurrencyName("British Pound")
                .fromCurrencySymbol("£")
                .toCurrencyCode("JPY")
                .toCurrencyName("Japanese Yen")
                .toCurrencySymbol("¥")
                .rate(new BigDecimal("150.5"))
                .lastUpdated(lastUpdated)
                .build();

        assertThat(usdToEur.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(usdToEur.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(gbpToJpy.getFromCurrencyCode()).isEqualTo("GBP");
        assertThat(gbpToJpy.getToCurrencyCode()).isEqualTo("JPY");
    }

    @Test
    @DisplayName("Should handle different rate values")
    void shouldHandleDifferentRateValues() {
        ExchangeRateWithCurrencyDto lowRateDto = ExchangeRateWithCurrencyDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("INR")
                .rate(new BigDecimal("83.50"))
                .build();

        ExchangeRateWithCurrencyDto highRateDto = ExchangeRateWithCurrencyDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("VEF")
                .rate(new BigDecimal("2000.0"))
                .build();

        assertThat(lowRateDto.getRate()).isEqualByComparingTo(new BigDecimal("83.50"));
        assertThat(highRateDto.getRate()).isEqualByComparingTo(new BigDecimal("2000.0"));
        assertThat(lowRateDto.getRate()).isLessThan(highRateDto.getRate());
    }
}

