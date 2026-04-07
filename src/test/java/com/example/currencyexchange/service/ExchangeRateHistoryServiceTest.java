package com.example.currencyexchange.service;

import com.example.currencyexchange.dto.ExchangeRateHistoryDto;
import com.example.currencyexchange.model.ExchangeRateHistory;
import com.example.currencyexchange.repository.ExchangeRateHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ExchangeRateHistoryService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateHistoryService Tests")
class ExchangeRateHistoryServiceTest {

    @Mock
    private ExchangeRateHistoryRepository historyRepository;

    @InjectMocks
    private ExchangeRateHistoryService historyService;

    private ExchangeRateHistory history;
    private ExchangeRateHistoryDto historyDto;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        history = ExchangeRateHistory.builder()
                .uuid(uuid)
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9200"))
                .newRate(new BigDecimal("0.9250"))
                .changeReason("Market update")
                .createdAt(now)
                .createdBy("SYSTEM")
                .build();

        historyDto = ExchangeRateHistoryDto.builder()
                .uuid(uuid)
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9200"))
                .newRate(new BigDecimal("0.9250"))
                .changeReason("Market update")
                .createdAt(now)
                .createdBy("SYSTEM")
                .build();
    }

    @Test
    @DisplayName("Should record rate change")
    void shouldRecordRateChange() {
        when(historyRepository.save(any())).thenReturn(history);

        ExchangeRateHistoryDto result = historyService.recordRateChange(
                "USD", "EUR",
                new BigDecimal("0.9200"), new BigDecimal("0.9250"),
                "Market update"
        );

        assertThat(result.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(result.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(result.getOldRate()).isEqualByComparingTo(new BigDecimal("0.9200"));
        assertThat(result.getNewRate()).isEqualByComparingTo(new BigDecimal("0.9250"));
        verify(historyRepository).save(any());
    }

    @Test
    @DisplayName("Should get history for currency pair with pagination")
    void shouldGetHistoryForCurrencyPairWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeRateHistory> page = new PageImpl<>(List.of(history));
        when(historyRepository.findByFromCurrencyCodeAndToCurrencyCodeOrderByCreatedAtDesc(
                "USD", "EUR", pageable)).thenReturn(page);

        Page<ExchangeRateHistoryDto> result = historyService.getHistoryForPair("USD", "EUR", pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(historyRepository).findByFromCurrencyCodeAndToCurrencyCodeOrderByCreatedAtDesc("USD", "EUR", pageable);
    }

    @Test
    @DisplayName("Should get history for currency with pagination")
    void shouldGetHistoryForCurrencyWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeRateHistory> page = new PageImpl<>(List.of(history));
        when(historyRepository.findByFromCurrencyCodeOrderByCreatedAtDesc("USD", pageable)).thenReturn(page);

        Page<ExchangeRateHistoryDto> result = historyService.getHistoryForCurrency("USD", pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getFromCurrencyCode()).isEqualTo("USD");
        verify(historyRepository).findByFromCurrencyCodeOrderByCreatedAtDesc("USD", pageable);
    }

    @Test
    @DisplayName("Should get history with rate details")
    void shouldGetHistoryWithRateDetails() {
        when(historyRepository.findHistoryWithExchangeRateDetails("USD", "EUR"))
                .thenReturn(List.of(history));

        List<ExchangeRateHistoryDto> result = historyService.getHistoryWithRateDetails("USD", "EUR");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFromCurrencyCode()).isEqualTo("USD");
        verify(historyRepository).findHistoryWithExchangeRateDetails("USD", "EUR");
    }

    @Test
    @DisplayName("Should get history by id")
    void shouldGetHistoryById() {
        when(historyRepository.findById(uuid)).thenReturn(Optional.of(history));

        ExchangeRateHistoryDto result = historyService.getHistoryById(uuid);

        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getChangeReason()).isEqualTo("Market update");
        verify(historyRepository).findById(uuid);
    }

    @Test
    @DisplayName("Should throw exception when history not found by id")
    void shouldThrowException_WhenHistoryNotFoundById() {
        when(historyRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.getHistoryById(uuid))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should get history count for currency pair")
    void shouldGetHistoryCountForCurrencyPair() {
        when(historyRepository.countByFromCurrencyCodeAndToCurrencyCode("USD", "EUR")).thenReturn(5L);

        long result = historyService.getHistoryCount("USD", "EUR");

        assertThat(result).isEqualTo(5L);
        verify(historyRepository).countByFromCurrencyCodeAndToCurrencyCode("USD", "EUR");
    }

    @Test
    @DisplayName("Should get most recent history")
    void shouldGetMostRecentHistory() {
        when(historyRepository.findMostRecentHistory("USD", "EUR")).thenReturn(history);

        ExchangeRateHistoryDto result = historyService.getMostRecentHistory("USD", "EUR");

        assertThat(result.getNewRate()).isEqualByComparingTo(new BigDecimal("0.9250"));
        verify(historyRepository).findMostRecentHistory("USD", "EUR");
    }

    @Test
    @DisplayName("Should throw exception when most recent history not found")
    void shouldThrowException_WhenMostRecentHistoryNotFound() {
        when(historyRepository.findMostRecentHistory("EUR", "USD")).thenReturn(null);

        assertThatThrownBy(() -> historyService.getMostRecentHistory("EUR", "USD"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle rate increase correctly")
    void shouldHandleRateIncreaseCorrectly() {
        ExchangeRateHistory increaseHistory = ExchangeRateHistory.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9200"))
                .newRate(new BigDecimal("0.9500"))
                .changeReason("Rate increase")
                .createdAt(LocalDateTime.now())
                .createdBy("SYSTEM")
                .build();

        when(historyRepository.save(any())).thenReturn(increaseHistory);

        ExchangeRateHistoryDto result = historyService.recordRateChange(
                "USD", "EUR",
                new BigDecimal("0.9200"), new BigDecimal("0.9500"),
                "Rate increase"
        );

        assertThat(result.getOldRate()).isLessThan(result.getNewRate());
    }

    @Test
    @DisplayName("Should handle rate decrease correctly")
    void shouldHandleRateDecreaseCorrectly() {
        ExchangeRateHistory decreaseHistory = ExchangeRateHistory.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9500"))
                .newRate(new BigDecimal("0.9200"))
                .changeReason("Rate decrease")
                .createdAt(LocalDateTime.now())
                .createdBy("SYSTEM")
                .build();

        when(historyRepository.save(any())).thenReturn(decreaseHistory);

        ExchangeRateHistoryDto result = historyService.recordRateChange(
                "USD", "EUR",
                new BigDecimal("0.9500"), new BigDecimal("0.9200"),
                "Rate decrease"
        );

        assertThat(result.getOldRate()).isGreaterThan(result.getNewRate());
    }

    @Test
    @DisplayName("Should track created by information")
    void shouldTrackCreatedByInformation() {
        when(historyRepository.findById(uuid)).thenReturn(Optional.of(history));

        ExchangeRateHistoryDto result = historyService.getHistoryById(uuid);

        assertThat(result.getCreatedBy()).isEqualTo("SYSTEM");
    }

    @Test
    @DisplayName("Should track creation timestamp")
    void shouldTrackCreationTimestamp() {
        when(historyRepository.findById(uuid)).thenReturn(Optional.of(history));

        ExchangeRateHistoryDto result = historyService.getHistoryById(uuid);

        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle multiple history records for same pair")
    void shouldHandleMultipleHistoryRecordsForSamePair() {
        ExchangeRateHistory history2 = ExchangeRateHistory.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9250"))
                .newRate(new BigDecimal("0.9400"))
                .changeReason("Adjustment")
                .createdAt(LocalDateTime.now().minusHours(1))
                .createdBy("SYSTEM")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeRateHistory> page = new PageImpl<>(List.of(history, history2));
        when(historyRepository.findByFromCurrencyCodeAndToCurrencyCodeOrderByCreatedAtDesc(
                "USD", "EUR", pageable)).thenReturn(page);

        Page<ExchangeRateHistoryDto> result = historyService.getHistoryForPair("USD", "EUR", pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}

