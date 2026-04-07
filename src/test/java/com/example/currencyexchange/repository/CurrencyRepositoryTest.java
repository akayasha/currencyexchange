package com.example.currencyexchange.repository;

import com.example.currencyexchange.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CurrencyRepository.
 */
@DataJpaTest
@DisplayName("CurrencyRepository Tests")
class CurrencyRepositoryTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    private Currency usd;
    private Currency eur;
    private Currency gbp;

    @BeforeEach
    void setUp() {
        currencyRepository.deleteAll();

        usd = Currency.builder()
                .code("USD")
                .name("United States Dollar")
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build();

        eur = Currency.builder()
                .code("EUR")
                .name("Euro")
                .symbol("€")
                .region("Europe")
                .isActive(true)
                .build();

        gbp = Currency.builder()
                .code("GBP")
                .name("British Pound")
                .symbol("£")
                .region("Europe")
                .isActive(true)
                .build();

        currencyRepository.saveAll(List.of(usd, eur, gbp));
    }

    @Test
    @DisplayName("Should find currency by code")
    void shouldFindCurrencyByCode() {
        Optional<Currency> result = currencyRepository.findByCode("USD");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("USD");
        assertThat(result.get().getName()).isEqualTo("United States Dollar");
    }

    @Test
    @DisplayName("Should return empty when currency code not found")
    void shouldReturnEmpty_WhenCurrencyCodeNotFound() {
        Optional<Currency> result = currencyRepository.findByCode("XYZ");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should check if currency code exists")
    void shouldCheckIfCurrencyCodeExists() {
        boolean exists = currencyRepository.existsByCode("EUR");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when currency code does not exist")
    void shouldReturnFalse_WhenCurrencyCodeDoesNotExist() {
        boolean exists = currencyRepository.existsByCode("ABC");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should search currencies by keyword")
    void shouldSearchCurrenciesByKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Currency> result = currencyRepository.searchCurrencies("dollar", pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should find currencies by active status")
    void shouldFindCurrenciesByActiveStatus() {
        List<Currency> activeCurrencies = currencyRepository.findByIsActive(true);

        assertThat(activeCurrencies).isNotEmpty();
        assertThat(activeCurrencies).allMatch(Currency::isActive);
    }

    @Test
    @DisplayName("Should find inactive currencies")
    void shouldFindInactiveCurrencies() {
        Currency inactiveCurrency = Currency.builder()
                .code("INR")
                .name("Indian Rupee")
                .symbol("₹")
                .region("Asia")
                .isActive(false)
                .build();
        currencyRepository.save(inactiveCurrency);

        List<Currency> inactiveCurrencies = currencyRepository.findByIsActive(false);

        assertThat(inactiveCurrencies).isNotEmpty();
        assertThat(inactiveCurrencies).allMatch(c -> !c.isActive());
    }

    @Test
    @DisplayName("Should find currencies by region")
    void shouldFindCurrenciesByRegion() {
        List<Currency> europeCurrencies = currencyRepository.findByRegion("Europe");

        assertThat(europeCurrencies)
                .isNotEmpty()
                .extracting(Currency::getCode)
                .contains("EUR", "GBP");
    }

    @Test
    @DisplayName("Should count active currencies")
    void shouldCountActiveCurrencies() {
        long activeCurrencies = currencyRepository.countByIsActive(true);

        assertThat(activeCurrencies).isEqualTo(3);
    }

    @Test
    @DisplayName("Should count by region")
    void shouldCountByRegion() {
        List<Object[]> results = currencyRepository.countByRegion();

        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("Should save currency with valid data")
    void shouldSaveCurrencyWithValidData() {
        Currency jpy = Currency.builder()
                .code("JPY")
                .name("Japanese Yen")
                .symbol("¥")
                .region("Asia")
                .isActive(true)
                .build();

        Currency saved = currencyRepository.save(jpy);

        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("JPY");
    }

    @Test
    @DisplayName("Should delete currency by id")
    void shouldDeleteCurrencyById() {
        UUID currencyId = usd.getUuid();
        currencyRepository.deleteById(currencyId);

        Optional<Currency> result = currencyRepository.findById(currencyId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should update currency")
    void shouldUpdateCurrency() {
        Currency currency = currencyRepository.findByCode("USD").get();
        currency.setName("United States Dollar Updated");

        currencyRepository.save(currency);

        Currency updated = currencyRepository.findByCode("USD").get();
        assertThat(updated.getName()).isEqualTo("United States Dollar Updated");
    }

    @Test
    @DisplayName("Should find all currencies")
    void shouldFindAllCurrencies() {
        List<Currency> all = currencyRepository.findAll();

        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should search with pagination")
    void shouldSearchWithPagination() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Currency> result = currencyRepository.searchCurrencies(null, pageable);

        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find by active status with pagination")
    void shouldFindByActiveStatusWithPagination() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Currency> result = currencyRepository.findByIsActive(true, pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
    }
}

