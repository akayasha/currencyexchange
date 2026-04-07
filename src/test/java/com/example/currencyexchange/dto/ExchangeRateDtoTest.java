package com.example.currencyexchange.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ExchangeRateDto validation.
 */
@DisplayName("ExchangeRateDto Validation Tests")
class ExchangeRateDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void shouldPassValidation_WithValidData() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9250"))
                .lastUpdated(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<ExchangeRateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when fromCurrencyCode is null")
    void shouldFailValidation_WhenFromCurrencyCodeIsNull() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .fromCurrencyCode(null)
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9250"))
                .build();

        Set<ConstraintViolation<ExchangeRateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Source currency code is required");
    }

    @Test
    @DisplayName("Should fail validation when fromCurrencyCode is blank")
    void shouldFailValidation_WhenFromCurrencyCodeIsBlank() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .fromCurrencyCode("")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9250"))
                .build();

        Set<ConstraintViolation<ExchangeRateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Source currency code is required");
    }

    @Test
    @DisplayName("Should fail validation when toCurrencyCode is null")
    void shouldFailValidation_WhenToCurrencyCodeIsNull() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode(null)
                .rate(new BigDecimal("0.9250"))
                .build();

        Set<ConstraintViolation<ExchangeRateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Target currency code is required");
    }

    @Test
    @DisplayName("Should fail validation when toCurrencyCode is blank")
    void shouldFailValidation_WhenToCurrencyCodeIsBlank() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("")
                .rate(new BigDecimal("0.9250"))
                .build();

        Set<ConstraintViolation<ExchangeRateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Target currency code is required");
    }

    @Test
    @DisplayName("Should fail validation when rate is null")
    void shouldFailValidation_WhenRateIsNull() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(null)
                .build();

        Set<ConstraintViolation<ExchangeRateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Rate is required");
    }

    @Test
    @DisplayName("Should fail validation when rate is zero")
    void shouldFailValidation_WhenRateIsZero() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<ExchangeRateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Rate must be positive");
    }

    @Test
    @DisplayName("Should fail validation when rate is negative")
    void shouldFailValidation_WhenRateIsNegative() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("-0.5"))
                .build();

        Set<ConstraintViolation<ExchangeRateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Rate must be positive");
    }

    @Test
    @DisplayName("Should fail validation with multiple violations")
    void shouldFailValidation_WithMultipleViolations() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .fromCurrencyCode("")
                .toCurrencyCode(null)
                .rate(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<ExchangeRateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(3);
    }

    @Test
    @DisplayName("Should handle builder pattern correctly")
    void shouldHandleBuilderPatternCorrectly() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime lastUpdated = LocalDateTime.now();

        ExchangeRateDto dto = ExchangeRateDto.builder()
                .uuid(uuid)
                .fromCurrencyCode("GBP")
                .toCurrencyCode("JPY")
                .rate(new BigDecimal("150.0"))
                .lastUpdated(lastUpdated)
                .fromCurrencyName("British Pound")
                .toCurrencyName("Japanese Yen")
                .fromCurrencySymbol("£")
                .toCurrencySymbol("¥")
                .build();

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getFromCurrencyCode()).isEqualTo("GBP");
        assertThat(dto.getToCurrencyCode()).isEqualTo("JPY");
        assertThat(dto.getRate()).isEqualByComparingTo(new BigDecimal("150.0"));
        assertThat(dto.getLastUpdated()).isEqualTo(lastUpdated);
        assertThat(dto.getFromCurrencyName()).isEqualTo("British Pound");
        assertThat(dto.getToCurrencyName()).isEqualTo("Japanese Yen");
        assertThat(dto.getFromCurrencySymbol()).isEqualTo("£");
        assertThat(dto.getToCurrencySymbol()).isEqualTo("¥");
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void shouldHandleGettersAndSettersCorrectly() {
        ExchangeRateDto dto = new ExchangeRateDto();
        UUID uuid = UUID.randomUUID();
        LocalDateTime lastUpdated = LocalDateTime.now();

        dto.setUuid(uuid);
        dto.setFromCurrencyCode("CAD");
        dto.setToCurrencyCode("USD");
        dto.setRate(new BigDecimal("0.75"));
        dto.setLastUpdated(lastUpdated);
        dto.setFromCurrencyName("Canadian Dollar");
        dto.setToCurrencyName("US Dollar");
        dto.setFromCurrencySymbol("C$");
        dto.setToCurrencySymbol("$");

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getFromCurrencyCode()).isEqualTo("CAD");
        assertThat(dto.getToCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getRate()).isEqualByComparingTo(new BigDecimal("0.75"));
        assertThat(dto.getLastUpdated()).isEqualTo(lastUpdated);
        assertThat(dto.getFromCurrencyName()).isEqualTo("Canadian Dollar");
        assertThat(dto.getToCurrencyName()).isEqualTo("US Dollar");
        assertThat(dto.getFromCurrencySymbol()).isEqualTo("C$");
        assertThat(dto.getToCurrencySymbol()).isEqualTo("$");
    }

    @Test
    @DisplayName("Should handle optional fields being null")
    void shouldHandleOptionalFieldsBeingNull() {
        ExchangeRateDto dto = ExchangeRateDto.builder()
                .fromCurrencyCode("EUR")
                .toCurrencyCode("GBP")
                .rate(new BigDecimal("0.85"))
                .build();

        assertThat(dto.getUuid()).isNull();
        assertThat(dto.getLastUpdated()).isNull();
        assertThat(dto.getFromCurrencyName()).isNull();
        assertThat(dto.getToCurrencyName()).isNull();
        assertThat(dto.getFromCurrencySymbol()).isNull();
        assertThat(dto.getToCurrencySymbol()).isNull();
    }
}
