package com.example.currencyexchange.repository;

import com.example.currencyexchange.model.ConversionHistory;
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
 * Unit tests for ConversionHistoryRepository.
 */
@DataJpaTest
@DisplayName("ConversionHistoryRepository Tests")
class ConversionHistoryRepositoryTest {

    @Autowired
    private ConversionHistoryRepository conversionHistoryRepository;

    private ConversionHistory conversion1;
    private ConversionHistory conversion2;
    private ConversionHistory conversion3;

    @BeforeEach
    void setUp() {
        conversionHistoryRepository.deleteAll();

        conversion1 = conversionHistoryRepository.save(ConversionHistory.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("92.50"))
                .rateApplied(new BigDecimal("0.9250"))
                .timestamp(LocalDateTime.now().minusHours(2))
                .source("db")
                .build());

        conversion2 = conversionHistoryRepository.save(ConversionHistory.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("200.00"))
                .convertedAmount(new BigDecimal("185.00"))
                .rateApplied(new BigDecimal("0.9250"))
                .timestamp(LocalDateTime.now().minusHours(1))
                .source("external-api")
                .build());

        conversion3 = conversionHistoryRepository.save(ConversionHistory.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("GBP")
                .amount(new BigDecimal("150.00"))
                .convertedAmount(new BigDecimal("119.25"))
                .rateApplied(new BigDecimal("0.7950"))
                .timestamp(LocalDateTime.now())
                .source("db")
                .build());
    }

    @Test
    @DisplayName("Should find conversion history by currency pair")
    void shouldFindConversionHistoryByCurrencyPair() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversionHistory> result = conversionHistoryRepository
                .findByFromCurrencyCodeAndToCurrencyCodeOrderByTimestampDesc("USD", "EUR", pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(c ->
            c.getFromCurrencyCode().equals("USD") && c.getToCurrencyCode().equals("EUR"));
    }

    @Test
    @DisplayName("Should order conversion history by timestamp descending")
    void shouldOrderConversionHistoryByTimestampDescending() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversionHistory> result = conversionHistoryRepository
                .findByFromCurrencyCodeAndToCurrencyCodeOrderByTimestampDesc("USD", "EUR", pageable);

        List<ConversionHistory> conversions = result.getContent();
        assertThat(conversions).isNotEmpty();
        assertThat(conversions.get(0).getTimestamp())
                .isAfterOrEqualTo(conversions.get(conversions.size() - 1).getTimestamp());
    }

    @Test
    @DisplayName("Should find conversion history by from currency")
    void shouldFindConversionHistoryByFromCurrency() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversionHistory> result = conversionHistoryRepository
                .findByFromCurrencyCodeOrderByTimestampDesc("USD", pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).allMatch(c -> c.getFromCurrencyCode().equals("USD"));
    }

    @Test
    @DisplayName("Should get conversion statistics")
    void shouldGetConversionStatistics() {
        List<Object[]> statistics = conversionHistoryRepository.getConversionStatistics();

        assertThat(statistics).isNotEmpty();
        assertThat(statistics.get(0)).hasSize(3); // count, sum, avg
    }

    @Test
    @DisplayName("Should get most used from currencies")
    void shouldGetMostUsedFromCurrencies() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> results = conversionHistoryRepository.getMostUsedFromCurrencies(pageable);

        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("Should get most used to currencies")
    void shouldGetMostUsedToCurrencies() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> results = conversionHistoryRepository.getMostUsedToCurrencies(pageable);

        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("Should save conversion history")
    void shouldSaveConversionHistory() {
        ConversionHistory conversion = ConversionHistory.builder()
                .fromCurrencyCode("EUR")
                .toCurrencyCode("GBP")
                .amount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("86.00"))
                .rateApplied(new BigDecimal("0.8600"))
                .timestamp(LocalDateTime.now())
                .source("db")
                .build();

        ConversionHistory saved = conversionHistoryRepository.save(conversion);

        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getFromCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Should find conversion history by id")
    void shouldFindConversionHistoryById() {
        Optional<ConversionHistory> result = conversionHistoryRepository.findById(conversion1.getUuid());

        assertThat(result).isPresent();
        assertThat(result.get().getFromCurrencyCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should update conversion history")
    void shouldUpdateConversionHistory() {
        conversion1.setSource("external-api");
        conversionHistoryRepository.save(conversion1);

        ConversionHistory updated = conversionHistoryRepository.findById(conversion1.getUuid()).get();

        assertThat(updated.getSource()).isEqualTo("external-api");
    }

    @Test
    @DisplayName("Should delete conversion history by id")
    void shouldDeleteConversionHistoryById() {
        UUID conversionId = conversion1.getUuid();
        conversionHistoryRepository.deleteById(conversionId);

        Optional<ConversionHistory> result = conversionHistoryRepository.findById(conversionId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all conversion history")
    void shouldFindAllConversionHistory() {
        List<ConversionHistory> all = conversionHistoryRepository.findAll();

        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should handle pagination")
    void shouldHandlePagination() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<ConversionHistory> result = conversionHistoryRepository
                .findByFromCurrencyCodeOrderByTimestampDesc("USD", pageable);

        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should handle multiple conversions for same currency pair")
    void shouldHandleMultipleConversionsForSameCurrencyPair() {
        ConversionHistory anotherUsdToEur = conversionHistoryRepository.save(ConversionHistory.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("50.00"))
                .convertedAmount(new BigDecimal("46.25"))
                .rateApplied(new BigDecimal("0.9250"))
                .timestamp(LocalDateTime.now().minusMinutes(30))
                .source("db")
                .build());

        Pageable pageable = PageRequest.of(0, 10);
        Page<ConversionHistory> result = conversionHistoryRepository
                .findByFromCurrencyCodeAndToCurrencyCodeOrderByTimestampDesc("USD", "EUR", pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should store conversion amount correctly")
    void shouldStoreConversionAmountCorrectly() {
        ConversionHistory retrieved = conversionHistoryRepository.findById(conversion1.getUuid()).get();

        assertThat(retrieved.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(retrieved.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("92.50"));
    }

    @Test
    @DisplayName("Should store conversion source correctly")
    void shouldStoreConversionSourceCorrectly() {
        ConversionHistory dbSource = conversionHistoryRepository.findById(conversion1.getUuid()).get();
        ConversionHistory apiSource = conversionHistoryRepository.findById(conversion2.getUuid()).get();

        assertThat(dbSource.getSource()).isEqualTo("db");
        assertThat(apiSource.getSource()).isEqualTo("external-api");
    }

    @Test
    @DisplayName("Should handle timestamp correctly")
    void shouldHandleTimestampCorrectly() {
        ConversionHistory retrieved = conversionHistoryRepository.findById(conversion1.getUuid()).get();

        assertThat(retrieved.getTimestamp()).isNotNull();
    }
}

