package com.example.currencyexchange.service;

import com.example.currencyexchange.dto.ConversionRequestDto;
import com.example.currencyexchange.dto.ConversionResponseDto;
import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.model.ConversionHistory;
import com.example.currencyexchange.model.ExchangeRate;
import com.example.currencyexchange.repository.ConversionHistoryRepository;
import com.example.currencyexchange.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for converting currency amounts.
 *
 * - First tries to use the exchange rate from the H2 database.
 * - If not found, calls the external exchange rate API (open.er-api.com).
 * - This demonstrates calling an external API from a service class method.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ConversionHistoryRepository conversionHistoryRepository;
    private final RestTemplate restTemplate;

    @Value("${exchange.rate.api.base-url}")
    private String externalApiBaseUrl;

    /**
     * Converts an amount from one currency to another.
     * Checks DB first; falls back to external API if rate not found locally.
     */
    public ConversionResponseDto convert(ConversionRequestDto request) {
        ConversionResponseDto response = performConversion(request);
        saveConversionHistory(response);
        return response;
    }

    private ConversionResponseDto performConversion(ConversionRequestDto request) {
        String fromCode = request.getFromCurrencyCode().toUpperCase();
        String toCode = request.getToCurrencyCode().toUpperCase();
        BigDecimal amount = request.getAmount();

        // Try fetching from the local H2 database
        Optional<ExchangeRate> dbRate =
                exchangeRateRepository.findByFromCurrencyCodeAndToCurrencyCode(fromCode, toCode);

        if (dbRate.isPresent()) {
            BigDecimal rate = dbRate.get().getRate();
            BigDecimal converted = amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
            log.info("Conversion (DB): {} {} -> {} {} at rate {}", amount, fromCode, converted, toCode, rate);

            return ConversionResponseDto.builder()
                    .fromCurrencyCode(fromCode)
                    .toCurrencyCode(toCode)
                    .amount(amount)
                    .convertedAmount(converted)
                    .rateApplied(rate)
                    .timestamp(LocalDateTime.now())
                    .source("db")
                    .build();
        }

        // Fall back to external API
        log.info("Rate not in DB for {}/{}. Calling external API...", fromCode, toCode);
        return convertViaExternalApi(fromCode, toCode, amount);
    }

    /**
     * Calls the external exchange rate API to fetch live rates.
     * Demonstrates external API call from a service class method.
     */
    @SuppressWarnings("unchecked")
    public ConversionResponseDto convertViaExternalApi(String fromCode, String toCode, BigDecimal amount) {
        String url = externalApiBaseUrl + "/" + fromCode;
        log.info("Calling external exchange rate API: {}", url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"success".equals(response.get("result"))) {
                throw new ResourceNotFoundException(
                        "Could not retrieve exchange rate from external API for: " + fromCode);
            }

            Map<String, Object> rates = (Map<String, Object>) response.get("rates");
            if (rates == null || !rates.containsKey(toCode)) {
                throw new ResourceNotFoundException(
                        "Exchange rate for target currency '" + toCode + "' not found in external API");
            }

            BigDecimal rate = new BigDecimal(rates.get(toCode).toString());
            BigDecimal converted = amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);

            log.info("Conversion (External API): {} {} -> {} {} at rate {}", amount, fromCode, converted, toCode, rate);

            return ConversionResponseDto.builder()
                    .fromCurrencyCode(fromCode)
                    .toCurrencyCode(toCode)
                    .amount(amount)
                    .convertedAmount(converted)
                    .rateApplied(rate)
                    .timestamp(LocalDateTime.now())
                    .source("external-api")
                    .build();

        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("External API call failed for {}/{}: {}", fromCode, toCode, ex.getMessage());
            throw new ResourceNotFoundException(
                    "Exchange rate not found for pair: " + fromCode + "/" + toCode);
        }
    }

    /**
     * Convert currency using the existing convert method.
     */
    public ConversionResponseDto convertCurrency(ConversionRequestDto request) {
        return convert(request);
    }

    /**
     * Get conversion history.
     */
    public Page<ConversionResponseDto> getConversionHistory(Pageable pageable) {
        Page<ConversionHistory> historyPage = conversionHistoryRepository.findAll(pageable);
        return historyPage.map(this::mapToConversionResponseDto);
    }

    private ConversionResponseDto mapToConversionResponseDto(ConversionHistory history) {
        return ConversionResponseDto.builder()
                .fromCurrencyCode(history.getFromCurrencyCode())
                .toCurrencyCode(history.getToCurrencyCode())
                .amount(history.getAmount())
                .convertedAmount(history.getConvertedAmount())
                .rateApplied(history.getRateApplied())
                .timestamp(history.getTimestamp())
                .source(history.getSource())
                .build();
    }

    /**
     * Get conversion statistics.
     */
    public Map<String, Object> getConversionStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get basic statistics
        List<Object[]> basicStats = conversionHistoryRepository.getConversionStatistics();
        if (!basicStats.isEmpty()) {
            Object[] row = basicStats.get(0);
            stats.put("totalConversions", row[0]);
            stats.put("totalAmountConverted", row[1]);
            stats.put("averageConversionAmount", row[2]);
        } else {
            stats.put("totalConversions", 0L);
            stats.put("totalAmountConverted", BigDecimal.ZERO);
            stats.put("averageConversionAmount", BigDecimal.ZERO);
        }

        // Get most used from currency
        List<Object[]> fromCurrencies = conversionHistoryRepository.getMostUsedFromCurrencies(Pageable.ofSize(1));
        if (!fromCurrencies.isEmpty()) {
            stats.put("mostUsedFromCurrency", fromCurrencies.get(0)[0]);
        } else {
            stats.put("mostUsedFromCurrency", null);
        }

        // Get most used to currency
        List<Object[]> toCurrencies = conversionHistoryRepository.getMostUsedToCurrencies(Pageable.ofSize(1));
        if (!toCurrencies.isEmpty()) {
            stats.put("mostUsedToCurrency", toCurrencies.get(0)[0]);
        } else {
            stats.put("mostUsedToCurrency", null);
        }

        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    private void saveConversionHistory(ConversionResponseDto response) {
        ConversionHistory history = ConversionHistory.builder()
                .fromCurrencyCode(response.getFromCurrencyCode())
                .toCurrencyCode(response.getToCurrencyCode())
                .amount(response.getAmount())
                .convertedAmount(response.getConvertedAmount())
                .rateApplied(response.getRateApplied())
                .timestamp(response.getTimestamp())
                .source(response.getSource())
                .build();
        conversionHistoryRepository.save(history);
        log.debug("Saved conversion history: {}", history.getUuid());
    }
}
