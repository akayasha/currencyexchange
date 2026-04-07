package com.example.currencyexchange.service;

import com.example.currencyexchange.dto.ExchangeRateDto;
import com.example.currencyexchange.dto.ExchangeRateWithCurrencyDto;
import com.example.currencyexchange.exception.DuplicateResourceException;
import com.example.currencyexchange.exception.InactiveCurrencyException;
import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.model.ExchangeRate;
import com.example.currencyexchange.repository.CurrencyRepository;
import com.example.currencyexchange.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for ExchangeRate operations.
 * Demonstrates JOIN queries and paginated search.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateHistoryService historyService;

    /**
     * GET all exchange rates with pagination.
     */
    public Page<ExchangeRateDto> getAllRates(Pageable pageable) {
        return exchangeRateRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * GET exchange rate by ID.
     */
    public ExchangeRateDto getRateById(UUID id) {
        ExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExchangeRate", "id", id));
        return toDto(rate);
    }

    /**
     * GET rate between two specific currencies.
     */
    public ExchangeRateDto getRateByPair(String fromCode, String toCode) {
        ExchangeRate rate = exchangeRateRepository
                .findByFromCurrencyCodeAndToCurrencyCode(fromCode.toUpperCase(), toCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ExchangeRate for pair", "from/to", fromCode + "/" + toCode));
        return toDto(rate);
    }

    /**
     * GET rates with full currency details via JOIN query.
     * This is the sample query that joins 2 tables (exchange_rate + currency x2).
     */
    public List<ExchangeRateWithCurrencyDto> getRatesWithCurrencyDetails(String fromCode, String toCode) {
        return exchangeRateRepository.findRatesWithCurrencyDetails(
                fromCode != null ? fromCode.toUpperCase() : null,
                toCode != null ? toCode.toUpperCase() : null
        );
    }

    /**
     * Search exchange rates by keyword with pagination.
     */
    public Page<ExchangeRateDto> searchRates(String keyword, Pageable pageable) {
        return exchangeRateRepository.searchExchangeRates(keyword, pageable).map(this::toDto);
    }

    /**
     * POST - Create a new exchange rate.
     */
    @Transactional
    public ExchangeRateDto createRate(ExchangeRateDto dto) {
        String fromCode = dto.getFromCurrencyCode().toUpperCase();
        String toCode = dto.getToCurrencyCode().toUpperCase();

        validateCurrencyActive(fromCode);
        validateCurrencyActive(toCode);

        if (exchangeRateRepository.existsByFromCurrencyCodeAndToCurrencyCode(fromCode, toCode)) {
            throw new DuplicateResourceException(
                    "Exchange rate for pair " + fromCode + "/" + toCode + " already exists");
        }

        ExchangeRate rate = ExchangeRate.builder()
                .fromCurrencyCode(fromCode)
                .toCurrencyCode(toCode)
                .rate(dto.getRate())
                .build();
        ExchangeRate saved = exchangeRateRepository.save(rate);
        historyService.recordRateChange(
                fromCode,
                toCode,
                saved.getRate(),
                saved.getRate(),
                "MANUAL_CREATE");
        log.info("Created exchange rate: {}/{} = {}", fromCode, toCode, saved.getRate());
        return toDto(saved);
    }

    /**
     * PUT - Update an existing exchange rate.
     */
    @Transactional
    public ExchangeRateDto updateRate(UUID id, ExchangeRateDto dto) {
        ExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExchangeRate", "id", id));

        var oldRate = rate.getRate();
        var newRate = dto.getRate();

        // Only record and persist if value actually changes
        if (newRate != null && oldRate != null && newRate.compareTo(oldRate) != 0) {
            rate.setRate(newRate);
            ExchangeRate saved = exchangeRateRepository.save(rate);
            historyService.recordRateChange(
                    rate.getFromCurrencyCode(),
                    rate.getToCurrencyCode(),
                    oldRate,
                    newRate,
                    "MANUAL_UPDATE");
            log.info("Updated exchange rate id={} to {}", id, saved.getRate());
            return toDto(saved);
        }

        log.info("No change detected for exchange rate id={}, skipping update", id);
        return toDto(rate);
    }

    /**
     * DELETE - Remove an exchange rate by ID.
     */
    @Transactional
    public void deleteRate(UUID id) {
        if (!exchangeRateRepository.existsById(id)) {
            throw new ResourceNotFoundException("ExchangeRate", "id", id);
        }
        exchangeRateRepository.deleteById(id);
        log.info("Deleted exchange rate with id: {}", id);
    }

    /**
     * Get exchange rates for a specific base currency.
     */
    public List<ExchangeRateDto> getRatesForBaseCurrency(String fromCode) {
        List<ExchangeRate> rates = exchangeRateRepository.findByFromCurrencyCode(fromCode);
        return rates.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Get exchange rate analytics and statistics.
     */
    public Map<String, Object> getExchangeRateAnalytics() {
        long totalRates = exchangeRateRepository.count();

        // Get rate statistics
        List<Object[]> rateStats = exchangeRateRepository.getRateStatistics();
        Map<String, Object> rateStatistics = new HashMap<>();
        if (!rateStats.isEmpty()) {
            Object[] stats = rateStats.get(0);
            rateStatistics.put("minRate", stats[0]);
            rateStatistics.put("maxRate", stats[1]);
            rateStatistics.put("avgRate", stats[2]);
        }

        // Count rates by base currency
        List<Object[]> baseCurrencyStats = exchangeRateRepository.countByFromCurrencyCode();
        Map<String, Long> ratesByBaseCurrency = new HashMap<>();
        for (Object[] stat : baseCurrencyStats) {
            ratesByBaseCurrency.put((String) stat[0], (Long) stat[1]);
        }

        // Count rates by target currency
        List<Object[]> targetCurrencyStats = exchangeRateRepository.countByToCurrencyCode();
        Map<String, Long> ratesByTargetCurrency = new HashMap<>();
        for (Object[] stat : targetCurrencyStats) {
            ratesByTargetCurrency.put((String) stat[0], (Long) stat[1]);
        }

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalRates", totalRates);
        analytics.put("rateStatistics", rateStatistics);
        analytics.put("ratesByBaseCurrency", ratesByBaseCurrency);
        analytics.put("ratesByTargetCurrency", ratesByTargetCurrency);
        analytics.put("timestamp", System.currentTimeMillis());

        return analytics;
    }

    private void validateCurrencyActive(String code) {
        var currency = currencyRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "code", code));
        if (!currency.isActive()) {
            throw new InactiveCurrencyException(code);
        }
    }

    private ExchangeRateDto toDto(ExchangeRate rate) {
        return ExchangeRateDto.builder()
                .uuid(rate.getUuid())
                .fromCurrencyCode(rate.getFromCurrencyCode())
                .toCurrencyCode(rate.getToCurrencyCode())
                .rate(rate.getRate())
                .lastUpdated(rate.getLastUpdated())
                .build();
    }
}
