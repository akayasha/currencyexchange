package com.example.currencyexchange.repository;

import com.example.currencyexchange.model.Currency;
import com.example.currencyexchange.model.ExchangeRate;
import com.example.currencyexchange.model.ExchangeRateHistory;
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
 * Unit tests for ExchangeRateHistoryRepository.
 */
@DataJpaTest
@DisplayName("ExchangeRateHistoryRepository Tests")
class ExchangeRateHistoryRepositoryTest {

    @Autowired
    private ExchangeRateHistoryRepository exchangeRateHistoryRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Currency usd;
    private Currency eur;
    private ExchangeRate usdToEur;
    private ExchangeRateHistory history1;
    private ExchangeRateHistory history2;

    @BeforeEach
    void setUp() {
        exchangeRateHistoryRepository.deleteAll();
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

        usdToEur = exchangeRateRepository.save(ExchangeRate.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9500"))
                .lastUpdated(LocalDateTime.now())
                .build());

        history1 = exchangeRateHistoryRepository.save(ExchangeRateHistory.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9200"))
                .newRate(new BigDecimal("0.9250"))
                .changeReason("Market update")
                .createdAt(LocalDateTime.now().minusHours(2))
                .createdBy("system")
                .build());

        history2 = exchangeRateHistoryRepository.save(ExchangeRateHistory.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9250"))
                .newRate(new BigDecimal("0.9500"))
                .changeReason("Rate adjustment")
                .createdAt(LocalDateTime.now().minusHours(1))
                .createdBy("admin")
                .build());
    }

    @Test
    @DisplayName("Should find history by currency pair")
    void shouldFindHistoryByCurrencyPair() {
        List<ExchangeRateHistory> result = exchangeRateHistoryRepository
                .findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR");

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(h ->
            h.getFromCurrencyCode().equals("USD") && h.getToCurrencyCode().equals("EUR"));
    }

    @Test
    @DisplayName("Should find history paginated and ordered by creation date")
    void shouldFindHistoryPaginatedAndOrderedByCreationDate() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeRateHistory> result = exchangeRateHistoryRepository
                .findByFromCurrencyCodeAndToCurrencyCodeOrderByCreatedAtDesc("USD", "EUR", pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(2);
        List<ExchangeRateHistory> content = result.getContent();
        assertThat(content.get(0).getCreatedAt()).isAfterOrEqualTo(content.get(content.size() - 1).getCreatedAt());
    }

    @Test
    @DisplayName("Should find history by from currency code ordered by creation date")
    void shouldFindHistoryByFromCurrencyCodeOrderedByCreationDate() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeRateHistory> result = exchangeRateHistoryRepository
                .findByFromCurrencyCodeOrderByCreatedAtDesc("USD", pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).allMatch(h -> h.getFromCurrencyCode().equals("USD"));
    }

    @Test
    @DisplayName("Should find history with exchange rate details")
    void shouldFindHistoryWithExchangeRateDetails() {
        List<ExchangeRateHistory> result = exchangeRateHistoryRepository
                .findHistoryWithExchangeRateDetails("USD", "EUR");

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Should count history for currency pair")
    void shouldCountHistoryForCurrencyPair() {
        long count = exchangeRateHistoryRepository
                .countByFromCurrencyCodeAndToCurrencyCode("USD", "EUR");

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return zero count for non-existent currency pair")
    void shouldReturnZeroCountForNonExistentCurrencyPair() {
        long count = exchangeRateHistoryRepository
                .countByFromCurrencyCodeAndToCurrencyCode("EUR", "USD");

        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should find most recent history")
    void shouldFindMostRecentHistory() {
        ExchangeRateHistory result = exchangeRateHistoryRepository
                .findMostRecentHistory("USD", "EUR");

        assertThat(result).isNotNull();
        assertThat(result.getNewRate()).isEqualByComparingTo(new BigDecimal("0.9500"));
    }

    @Test
    @DisplayName("Should return null for most recent history when not found")
    void shouldReturnNullForMostRecentHistoryWhenNotFound() {
        ExchangeRateHistory result = exchangeRateHistoryRepository
                .findMostRecentHistory("EUR", "USD");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should save exchange rate history")
    void shouldSaveExchangeRateHistory() {
        ExchangeRateHistory history = ExchangeRateHistory.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("GBP")
                .oldRate(new BigDecimal("0.7900"))
                .newRate(new BigDecimal("0.8000"))
                .changeReason("Rate correction")
                .createdAt(LocalDateTime.now())
                .createdBy("system")
                .build();

        ExchangeRateHistory saved = exchangeRateHistoryRepository.save(history);

        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getFromCurrencyCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should find history by id")
    void shouldFindHistoryById() {
        Optional<ExchangeRateHistory> result = exchangeRateHistoryRepository.findById(history1.getUuid());

        assertThat(result).isPresent();
        assertThat(result.get().getChangeReason()).isEqualTo("Market update");
    }

    @Test
    @DisplayName("Should update history")
    void shouldUpdateHistory() {
        history1.setChangeReason("Updated reason");
        exchangeRateHistoryRepository.save(history1);

        ExchangeRateHistory updated = exchangeRateHistoryRepository.findById(history1.getUuid()).get();

        assertThat(updated.getChangeReason()).isEqualTo("Updated reason");
    }

    @Test
    @DisplayName("Should delete history by id")
    void shouldDeleteHistoryById() {
        UUID historyId = history1.getUuid();
        exchangeRateHistoryRepository.deleteById(historyId);

        Optional<ExchangeRateHistory> result = exchangeRateHistoryRepository.findById(historyId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all history records")
    void shouldFindAllHistoryRecords() {
        List<ExchangeRateHistory> all = exchangeRateHistoryRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Should store rate change correctly")
    void shouldStoreRateChangeCorrectly() {
        ExchangeRateHistory retrieved = exchangeRateHistoryRepository.findById(history1.getUuid()).get();

        assertThat(retrieved.getOldRate()).isEqualByComparingTo(new BigDecimal("0.9200"));
        assertThat(retrieved.getNewRate()).isEqualByComparingTo(new BigDecimal("0.9250"));
    }

    @Test
    @DisplayName("Should store created by information")
    void shouldStoreCreatedByInformation() {
        ExchangeRateHistory retrieved = exchangeRateHistoryRepository.findById(history1.getUuid()).get();

        assertThat(retrieved.getCreatedBy()).isEqualTo("system");
    }

    @Test
    @DisplayName("Should store creation timestamp")
    void shouldStoreCreationTimestamp() {
        ExchangeRateHistory retrieved = exchangeRateHistoryRepository.findById(history1.getUuid()).get();

        assertThat(retrieved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle pagination for history")
    void shouldHandlePaginationForHistory() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<ExchangeRateHistory> result = exchangeRateHistoryRepository
                .findByFromCurrencyCodeAndToCurrencyCodeOrderByCreatedAtDesc("USD", "EUR", pageable);

        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should track multiple rate changes for same pair")
    void shouldTrackMultipleRateChangesForSamePair() {
        ExchangeRateHistory history3 = exchangeRateHistoryRepository.save(ExchangeRateHistory.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9500"))
                .newRate(new BigDecimal("0.9600"))
                .changeReason("Final adjustment")
                .createdAt(LocalDateTime.now())
                .createdBy("admin")
                .build());

        long count = exchangeRateHistoryRepository
                .countByFromCurrencyCodeAndToCurrencyCode("USD", "EUR");

        assertThat(count).isEqualTo(3);
    }
}

