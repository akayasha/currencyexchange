package com.example.currencyexchange.repository;

import com.example.currencyexchange.model.Currency;
import com.example.currencyexchange.model.ExchangeRate;
import com.example.currencyexchange.dto.ExchangeRateWithCurrencyDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ExchangeRateRepository.
 */
@DataJpaTest
@DisplayName("ExchangeRateRepository Tests")
class ExchangeRateRepositoryTest {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Currency usd;
    private Currency eur;
    private Currency gbp;
    private ExchangeRate usdToEur;
    private ExchangeRate usdToGbp;

    @BeforeEach
    void setUp() {
        exchangeRateRepository.deleteAll();
        currencyRepository.deleteAll();

        usd = currencyRepository.save(Currency.builder()
                .code("USD")
                .name("United States Dollar")
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build());

        eur = currencyRepository.save(Currency.builder()
                .code("EUR")
                .name("Euro")
                .symbol("€")
                .region("Europe")
                .isActive(true)
                .build());

        gbp = currencyRepository.save(Currency.builder()
                .code("GBP")
                .name("British Pound")
                .symbol("£")
                .region("Europe")
                .isActive(true)
                .build());

        usdToEur = exchangeRateRepository.save(ExchangeRate.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9250"))
                .lastUpdated(LocalDateTime.now())
                .build());

        usdToGbp = exchangeRateRepository.save(ExchangeRate.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("GBP")
                .rate(new BigDecimal("0.7950"))
                .lastUpdated(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("Should find exchange rate by currency pair")
    void shouldFindExchangeRateByCurrencyPair() {
        Optional<ExchangeRate> result = exchangeRateRepository
                .findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR");

        assertThat(result).isPresent();
        assertThat(result.get().getRate()).isEqualByComparingTo(new BigDecimal("0.9250"));
    }

    @Test
    @DisplayName("Should return empty when currency pair not found")
    void shouldReturnEmpty_WhenCurrencyPairNotFound() {
        Optional<ExchangeRate> result = exchangeRateRepository
                .findByFromCurrencyCodeAndToCurrencyCode("EUR", "USD");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should check if exchange rate exists")
    void shouldCheckIfExchangeRateExists() {
        boolean exists = exchangeRateRepository
                .existsByFromCurrencyCodeAndToCurrencyCode("USD", "EUR");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when exchange rate does not exist")
    void shouldReturnFalse_WhenExchangeRateDoesNotExist() {
        boolean exists = exchangeRateRepository
                .existsByFromCurrencyCodeAndToCurrencyCode("EUR", "USD");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find rates by from currency code")
    void shouldFindRatesByFromCurrencyCode() {
        List<ExchangeRate> results = exchangeRateRepository.findByFromCurrencyCode("USD");

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.getFromCurrencyCode().equals("USD"));
    }

    @Test
    @DisplayName("Should find rates with currency details")
    void shouldFindRatesWithCurrencyDetails() {
        List<ExchangeRateWithCurrencyDto> results = exchangeRateRepository
                .findRatesWithCurrencyDetails("USD", "EUR");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFromCurrencyCode()).isEqualTo("USD");
        assertThat(results.get(0).getFromCurrencyName()).isEqualTo("United States Dollar");
        assertThat(results.get(0).getToCurrencyName()).isEqualTo("Euro");
    }

    @Test
    @DisplayName("Should find all rates when no filters provided")
    void shouldFindAllRatesWhenNoFiltersProvided() {
        List<ExchangeRateWithCurrencyDto> results = exchangeRateRepository
                .findRatesWithCurrencyDetails(null, null);

        assertThat(results).isNotEmpty();
        assertThat(results.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should search exchange rates by keyword")
    void shouldSearchExchangeRatesByKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeRate> result = exchangeRateRepository.searchExchangeRates("USD", pageable);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Should get rate statistics")
    void shouldGetRateStatistics() {
        List<Object[]> statistics = exchangeRateRepository.getRateStatistics();

        assertThat(statistics).isNotEmpty();
        assertThat(statistics.get(0)).hasSize(3); // min, max, avg
    }

    @Test
    @DisplayName("Should count by from currency code")
    void shouldCountByFromCurrencyCode() {
        List<Object[]> results = exchangeRateRepository.countByFromCurrencyCode();

        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("Should count by to currency code")
    void shouldCountByToCurrencyCode() {
        List<Object[]> results = exchangeRateRepository.countByToCurrencyCode();

        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("Should save exchange rate")
    void shouldSaveExchangeRate() {
        ExchangeRate eurToGbp = ExchangeRate.builder()
                .fromCurrencyCode("EUR")
                .toCurrencyCode("GBP")
                .rate(new BigDecimal("0.8600"))
                .build();

        ExchangeRate saved = exchangeRateRepository.save(eurToGbp);

        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getRate()).isEqualByComparingTo(new BigDecimal("0.8600"));
    }

    @Test
    @DisplayName("Should update exchange rate")
    void shouldUpdateExchangeRate() {
        ExchangeRate rate = exchangeRateRepository
                .findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR").get();
        rate.setRate(new BigDecimal("0.9500"));

        exchangeRateRepository.save(rate);

        ExchangeRate updated = exchangeRateRepository
                .findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR").get();
        assertThat(updated.getRate()).isEqualByComparingTo(new BigDecimal("0.9500"));
    }

    @Test
    @DisplayName("Should delete exchange rate by id")
    void shouldDeleteExchangeRateById() {
        UUID rateId = usdToEur.getUuid();
        exchangeRateRepository.deleteById(rateId);

        Optional<ExchangeRate> result = exchangeRateRepository.findById(rateId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all exchange rates")
    void shouldFindAllExchangeRates() {
        List<ExchangeRate> all = exchangeRateRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Should search with pagination")
    void shouldSearchWithPagination() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<ExchangeRate> result = exchangeRateRepository.searchExchangeRates(null, pageable);

        assertThat(result.getSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle timestamp in exchange rate")
    void shouldHandleTimestampInExchangeRate() {
        ExchangeRate rate = exchangeRateRepository
                .findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR").get();

        assertThat(rate.getLastUpdated()).isNotNull();
    }

    @Test
    @DisplayName("Should update timestamp on save")
    void shouldUpdateTimestampOnSave() {
        ExchangeRate rate = exchangeRateRepository
                .findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR").get();
        LocalDateTime oldTimestamp = rate.getLastUpdated();

        rate.setRate(new BigDecimal("0.9600"));
        exchangeRateRepository.save(rate);

        ExchangeRate updated = exchangeRateRepository
                .findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR").get();
        assertThat(updated.getLastUpdated()).isNotNull();
    }
}

