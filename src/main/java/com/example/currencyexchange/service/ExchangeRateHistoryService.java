package com.example.currencyexchange.service;

import com.example.currencyexchange.dto.ExchangeRateHistoryDto;
import com.example.currencyexchange.model.ExchangeRateHistory;
import com.example.currencyexchange.repository.ExchangeRateHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for ExchangeRateHistory operations.
 * Tracks and manages the history of exchange rate changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExchangeRateHistoryService {

    private final ExchangeRateHistoryRepository historyRepository;

    /**
     * Create a history record when exchange rate changes.
     */
    @Transactional
    public ExchangeRateHistoryDto recordRateChange(String fromCode, String toCode,
                                                    BigDecimal oldRate, BigDecimal newRate,
                                                    String changeReason) {
        ExchangeRateHistory history = ExchangeRateHistory.builder()
                .fromCurrencyCode(fromCode)
                .toCurrencyCode(toCode)
                .oldRate(oldRate)
                .newRate(newRate)
                .changeReason(changeReason)
                .createdBy("SYSTEM")
                .build();

        ExchangeRateHistory saved = historyRepository.save(history);
        log.info("Recorded history: {} -> {} | {} -> {}", fromCode, toCode, oldRate, newRate);
        return toDto(saved);
    }

    /**
     * Get all history for a currency pair with pagination.
     */
    public Page<ExchangeRateHistoryDto> getHistoryForPair(String fromCode, String toCode, Pageable pageable) {
        return historyRepository.findByFromCurrencyCodeAndToCurrencyCodeOrderByCreatedAtDesc(fromCode, toCode, pageable)
                .map(this::toDto);
    }

    /**
     * Get all history for a base currency with pagination.
     */
    public Page<ExchangeRateHistoryDto> getHistoryForCurrency(String fromCode, Pageable pageable) {
        return historyRepository.findByFromCurrencyCodeOrderByCreatedAtDesc(fromCode, pageable)
                .map(this::toDto);
    }

    /**
     * Get history with JOIN to exchange rate details.
     * Demonstrates JOIN query across 2 tables.
     */
    public List<ExchangeRateHistoryDto> getHistoryWithRateDetails(String fromCode, String toCode) {
        List<ExchangeRateHistory> history = historyRepository.findHistoryWithExchangeRateDetails(fromCode, toCode);
        return history.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Get history by ID.
     */
    public ExchangeRateHistoryDto getHistoryById(UUID id) {
        ExchangeRateHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("History not found with id: " + id));
        return toDto(history);
    }

    /**
     * Get count of history records for a currency pair.
     */
    public long getHistoryCount(String fromCode, String toCode) {
        return historyRepository.countByFromCurrencyCodeAndToCurrencyCode(fromCode, toCode);
    }

    /**
     * Get most recent history for a currency pair.
     */
    public ExchangeRateHistoryDto getMostRecentHistory(String fromCode, String toCode) {
        ExchangeRateHistory history = historyRepository.findMostRecentHistory(fromCode, toCode);
        if (history == null) {
            throw new RuntimeException("No history found for " + fromCode + "/" + toCode);
        }
        return toDto(history);
    }

    /**
     * Convert entity to DTO.
     */
    private ExchangeRateHistoryDto toDto(ExchangeRateHistory history) {
        return ExchangeRateHistoryDto.builder()
                .uuid(history.getUuid())
                .fromCurrencyCode(history.getFromCurrencyCode())
                .toCurrencyCode(history.getToCurrencyCode())
                .oldRate(history.getOldRate())
                .newRate(history.getNewRate())
                .changeReason(history.getChangeReason())
                .createdAt(history.getCreatedAt())
                .createdBy(history.getCreatedBy())
                .build();
    }
}

