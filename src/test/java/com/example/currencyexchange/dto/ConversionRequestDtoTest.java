package com.example.currencyexchange.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ConversionRequestDto validation.
 */
@DisplayName("ConversionRequestDto Validation Tests")
class ConversionRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void shouldPassValidation_WithValidData() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        Set<ConstraintViolation<ConversionRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when fromCurrencyCode is null")
    void shouldFailValidation_WhenFromCurrencyCodeIsNull() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode(null)
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        Set<ConstraintViolation<ConversionRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Source currency code is required");
    }

    @Test
    @DisplayName("Should fail validation when fromCurrencyCode is blank")
    void shouldFailValidation_WhenFromCurrencyCodeIsBlank() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode("")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        Set<ConstraintViolation<ConversionRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Source currency code is required");
    }

    @Test
    @DisplayName("Should fail validation when toCurrencyCode is null")
    void shouldFailValidation_WhenToCurrencyCodeIsNull() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode(null)
                .amount(new BigDecimal("100.00"))
                .build();

        Set<ConstraintViolation<ConversionRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Target currency code is required");
    }

    @Test
    @DisplayName("Should fail validation when toCurrencyCode is blank")
    void shouldFailValidation_WhenToCurrencyCodeIsBlank() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("")
                .amount(new BigDecimal("100.00"))
                .build();

        Set<ConstraintViolation<ConversionRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Target currency code is required");
    }

    @Test
    @DisplayName("Should fail validation when amount is null")
    void shouldFailValidation_WhenAmountIsNull() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(null)
                .build();

        Set<ConstraintViolation<ConversionRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount is required");
    }

    @Test
    @DisplayName("Should fail validation when amount is zero")
    void shouldFailValidation_WhenAmountIsZero() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<ConversionRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount must be positive");
    }

    @Test
    @DisplayName("Should fail validation when amount is negative")
    void shouldFailValidation_WhenAmountIsNegative() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("-100.00"))
                .build();

        Set<ConstraintViolation<ConversionRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount must be positive");
    }

    @Test
    @DisplayName("Should fail validation with multiple violations")
    void shouldFailValidation_WithMultipleViolations() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode("")
                .toCurrencyCode(null)
                .amount(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<ConversionRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(3);
    }

    @Test
    @DisplayName("Should handle builder pattern correctly")
    void shouldHandleBuilderPatternCorrectly() {
        ConversionRequestDto dto = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("500.00"))
                .build();

        assertThat(dto.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void shouldHandleGettersAndSettersCorrectly() {
        ConversionRequestDto dto = new ConversionRequestDto();
        dto.setFromCurrencyCode("GBP");
        dto.setToCurrencyCode("JPY");
        dto.setAmount(new BigDecimal("250.00"));

        assertThat(dto.getFromCurrencyCode()).isEqualTo("GBP");
        assertThat(dto.getToCurrencyCode()).isEqualTo("JPY");
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
    }
}
