package com.example.currencyexchange.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ExchangeRateHistoryDto.
 */
@DisplayName("ExchangeRateHistoryDto Tests")
class ExchangeRateHistoryDtoTest {

    @Test
    @DisplayName("Should create valid ExchangeRateHistoryDto with builder")
    void shouldCreateValidExchangeRateHistoryDto_WithBuilder() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        ExchangeRateHistoryDto dto = ExchangeRateHistoryDto.builder()
                .uuid(uuid)
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9000"))
                .newRate(new BigDecimal("0.9250"))
                .changeReason("Market update")
                .createdAt(createdAt)
                .createdBy("system")
                .build();

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(dto.getOldRate()).isEqualByComparingTo(new BigDecimal("0.9000"));
        assertThat(dto.getNewRate()).isEqualByComparingTo(new BigDecimal("0.9250"));
        assertThat(dto.getChangeReason()).isEqualTo("Market update");
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getCreatedBy()).isEqualTo("system");
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void shouldHandleGettersAndSettersCorrectly() {
        ExchangeRateHistoryDto dto = new ExchangeRateHistoryDto();
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        dto.setUuid(uuid);
        dto.setFromCurrencyCode("GBP");
        dto.setToCurrencyCode("JPY");
        dto.setOldRate(new BigDecimal("140.0"));
        dto.setNewRate(new BigDecimal("150.0"));
        dto.setChangeReason("Bank rate update");
        dto.setCreatedAt(createdAt);
        dto.setCreatedBy("admin");

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getFromCurrencyCode()).isEqualTo("GBP");
        assertThat(dto.getToCurrencyCode()).isEqualTo("JPY");
        assertThat(dto.getOldRate()).isEqualByComparingTo(new BigDecimal("140.0"));
        assertThat(dto.getNewRate()).isEqualByComparingTo(new BigDecimal("150.0"));
        assertThat(dto.getChangeReason()).isEqualTo("Bank rate update");
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void shouldHandleNullValuesCorrectly() {
        ExchangeRateHistoryDto dto = new ExchangeRateHistoryDto();

        assertThat(dto.getUuid()).isNull();
        assertThat(dto.getFromCurrencyCode()).isNull();
        assertThat(dto.getToCurrencyCode()).isNull();
        assertThat(dto.getOldRate()).isNull();
        assertThat(dto.getNewRate()).isNull();
        assertThat(dto.getChangeReason()).isNull();
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getCreatedBy()).isNull();
    }

    @Test
    @DisplayName("Should handle no-args constructor")
    void shouldHandleNoArgsConstructor() {
        ExchangeRateHistoryDto dto = new ExchangeRateHistoryDto();

        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Should handle all-args constructor")
    void shouldHandleAllArgsConstructor() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        ExchangeRateHistoryDto dto = new ExchangeRateHistoryDto(
                uuid, "USD", "EUR",
                new BigDecimal("0.9000"),
                new BigDecimal("0.9250"),
                "Market update",
                createdAt,
                "system"
        );

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(dto.getOldRate()).isEqualByComparingTo(new BigDecimal("0.9000"));
        assertThat(dto.getNewRate()).isEqualByComparingTo(new BigDecimal("0.9250"));
        assertThat(dto.getChangeReason()).isEqualTo("Market update");
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getCreatedBy()).isEqualTo("system");
    }

    @Test
    @DisplayName("Should handle rate changes correctly")
    void shouldHandleRateChangesCorrectly() {
        ExchangeRateHistoryDto increaseDto = ExchangeRateHistoryDto.builder()
                .oldRate(new BigDecimal("1.0"))
                .newRate(new BigDecimal("1.05"))
                .changeReason("Rate increase")
                .build();

        ExchangeRateHistoryDto decreaseDto = ExchangeRateHistoryDto.builder()
                .oldRate(new BigDecimal("1.2"))
                .newRate(new BigDecimal("1.15"))
                .changeReason("Rate decrease")
                .build();

        assertThat(increaseDto.getOldRate()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(increaseDto.getNewRate()).isEqualByComparingTo(new BigDecimal("1.05"));
        assertThat(decreaseDto.getOldRate()).isEqualByComparingTo(new BigDecimal("1.2"));
        assertThat(decreaseDto.getNewRate()).isEqualByComparingTo(new BigDecimal("1.15"));
    }
}
