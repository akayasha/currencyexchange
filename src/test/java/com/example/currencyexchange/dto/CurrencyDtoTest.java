package com.example.currencyexchange.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CurrencyDto validation.
 */
@DisplayName("CurrencyDto Validation Tests")
class CurrencyDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void shouldPassValidation_WithValidData() {
        CurrencyDto dto = CurrencyDto.builder()
                .uuid(UUID.randomUUID())
                .code("USD")
                .name("United States Dollar")
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build();

        Set<ConstraintViolation<CurrencyDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when code is null")
    void shouldFailValidation_WhenCodeIsNull() {
        CurrencyDto dto = CurrencyDto.builder()
                .code(null)
                .name("United States Dollar")
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build();

        Set<ConstraintViolation<CurrencyDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Currency code is required");
    }

    @Test
    @DisplayName("Should fail validation when code is blank")
    void shouldFailValidation_WhenCodeIsBlank() {
        CurrencyDto dto = CurrencyDto.builder()
                .code("")
                .name("United States Dollar")
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build();

        Set<ConstraintViolation<CurrencyDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2); // blank and size validation
    }

    @Test
    @DisplayName("Should fail validation when code is too short")
    void shouldFailValidation_WhenCodeIsTooShort() {
        CurrencyDto dto = CurrencyDto.builder()
                .code("US")
                .name("United States Dollar")
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build();

        Set<ConstraintViolation<CurrencyDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Currency code must be exactly 3 characters");
    }

    @Test
    @DisplayName("Should fail validation when code is too long")
    void shouldFailValidation_WhenCodeIsTooLong() {
        CurrencyDto dto = CurrencyDto.builder()
                .code("USDD")
                .name("United States Dollar")
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build();

        Set<ConstraintViolation<CurrencyDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Currency code must be exactly 3 characters");
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void shouldFailValidation_WhenNameIsNull() {
        CurrencyDto dto = CurrencyDto.builder()
                .code("USD")
                .name(null)
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build();

        Set<ConstraintViolation<CurrencyDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Currency name is required");
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void shouldFailValidation_WhenNameIsBlank() {
        CurrencyDto dto = CurrencyDto.builder()
                .code("USD")
                .name("")
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build();

        Set<ConstraintViolation<CurrencyDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Currency name is required");
    }

    @Test
    @DisplayName("Should pass validation with minimal required fields")
    void shouldPassValidation_WithMinimalRequiredFields() {
        CurrencyDto dto = CurrencyDto.builder()
                .code("EUR")
                .name("Euro")
                .isActive(false)
                .build();

        Set<ConstraintViolation<CurrencyDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should handle builder pattern correctly")
    void shouldHandleBuilderPatternCorrectly() {
        UUID uuid = UUID.randomUUID();

        CurrencyDto dto = CurrencyDto.builder()
                .uuid(uuid)
                .code("GBP")
                .name("British Pound")
                .symbol("£")
                .region("Europe")
                .isActive(true)
                .build();

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getCode()).isEqualTo("GBP");
        assertThat(dto.getName()).isEqualTo("British Pound");
        assertThat(dto.getSymbol()).isEqualTo("£");
        assertThat(dto.getRegion()).isEqualTo("Europe");
        assertThat(dto.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle getters and setters correctly")
    void shouldHandleGettersAndSettersCorrectly() {
        CurrencyDto dto = new CurrencyDto();
        UUID uuid = UUID.randomUUID();

        dto.setUuid(uuid);
        dto.setCode("JPY");
        dto.setName("Japanese Yen");
        dto.setSymbol("¥");
        dto.setRegion("Asia");
        dto.setActive(false);

        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getCode()).isEqualTo("JPY");
        assertThat(dto.getName()).isEqualTo("Japanese Yen");
        assertThat(dto.getSymbol()).isEqualTo("¥");
        assertThat(dto.getRegion()).isEqualTo("Asia");
        assertThat(dto.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle optional fields being null")
    void shouldHandleOptionalFieldsBeingNull() {
        CurrencyDto dto = CurrencyDto.builder()
                .code("CAD")
                .name("Canadian Dollar")
                .isActive(true)
                .build();

        assertThat(dto.getUuid()).isNull();
        assertThat(dto.getSymbol()).isNull();
        assertThat(dto.getRegion()).isNull();
    }
}
