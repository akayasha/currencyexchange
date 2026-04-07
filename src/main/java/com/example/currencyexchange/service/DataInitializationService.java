package com.example.currencyexchange.service;

import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.model.Currency;
import com.example.currencyexchange.model.ExchangeRate;
import com.example.currencyexchange.repository.CurrencyRepository;
import com.example.currencyexchange.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for initializing the database with popular currencies and exchange rates
 * from external API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService {

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateHistoryService historyService;
    private final RestTemplate restTemplate;

    private static final Map<String, Map<String, String>> POPULAR_CURRENCIES = new HashMap<>();

    static {
        // Popular currencies: code -> (name, symbol)
        POPULAR_CURRENCIES.put("USD", Map.of("name", "United States Dollar", "symbol", "$"));
        POPULAR_CURRENCIES.put("EUR", Map.of("name", "Euro", "symbol", "€"));
        POPULAR_CURRENCIES.put("GBP", Map.of("name", "British Pound", "symbol", "£"));
        POPULAR_CURRENCIES.put("JPY", Map.of("name", "Japanese Yen", "symbol", "¥"));
        POPULAR_CURRENCIES.put("INR", Map.of("name", "Indian Rupee", "symbol", "₹"));
        POPULAR_CURRENCIES.put("CAD", Map.of("name", "Canadian Dollar", "symbol", "C$"));
        POPULAR_CURRENCIES.put("AUD", Map.of("name", "Australian Dollar", "symbol", "A$"));
        POPULAR_CURRENCIES.put("CHF", Map.of("name", "Swiss Franc", "symbol", "CHF"));
        POPULAR_CURRENCIES.put("CNY", Map.of("name", "Chinese Yuan", "symbol", "¥"));
        POPULAR_CURRENCIES.put("SGD", Map.of("name", "Singapore Dollar", "symbol", "S$"));
    }

    /**
     * Initialize database with popular currencies and exchange rates from external API.
     * If currencies already exist, only fetches new exchange rates.
     */
    @Transactional
    public Map<String, Object> initializeDatabase() {
        log.info("Starting database initialization...");
        int currenciesAdded = 0;
        int ratesAdded = 0;

        // Step 1: Add popular currencies if they don't exist
        for (String code : POPULAR_CURRENCIES.keySet()) {
            if (!currencyRepository.existsByCode(code)) {
                Map<String, String> currencyInfo = POPULAR_CURRENCIES.get(code);
                Currency currency = Currency.builder()
                        .code(code)
                        .name(currencyInfo.get("name"))
                        .symbol(currencyInfo.get("symbol"))
                        .region(getRegionForCode(code))
                        .isActive(true)
                        .build();
                currencyRepository.save(currency);
                currenciesAdded++;
                log.info("Added currency: {}", code);
            }
        }

        // Step 2: Fetch exchange rates from external API and save them
        // Use USD as base currency
        String baseCode = "USD";
        ratesAdded = fetchAndSaveExchangeRates(baseCode);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Database initialized successfully");
        result.put("currenciesAdded", currenciesAdded);
        result.put("ratesAdded", ratesAdded);
        result.put("timestamp", System.currentTimeMillis());

        log.info("Database initialization complete. Added {} currencies and {} rates",
                currenciesAdded, ratesAdded);

        return result;
    }

    /**
     * Fetch exchange rates for a base currency from external API and save to database.
     */
    @SuppressWarnings("unchecked")
    private int fetchAndSaveExchangeRates(String fromCode) {
        int ratesAdded = 0;

        try {
            String url = "https://open.er-api.com/v6/latest/" + fromCode;
            log.info("Fetching exchange rates from: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"success".equals(response.get("result"))) {
                log.warn("Failed to fetch rates from external API");
                return 0;
            }

            Map<String, Object> rates = (Map<String, Object>) response.get("rates");

            if (rates == null) {
                log.warn("No rates in external API response");
                return 0;
            }

            // Save exchange rates for all popular currencies
            for (String toCode : POPULAR_CURRENCIES.keySet()) {
                if (!fromCode.equals(toCode) && rates.containsKey(toCode)) {
                    // Check if rate pair already exists
                    if (!exchangeRateRepository.existsByFromCurrencyCodeAndToCurrencyCode(fromCode, toCode)) {
                        BigDecimal rate = new BigDecimal(rates.get(toCode).toString());

                        ExchangeRate exchangeRate = ExchangeRate.builder()
                                .fromCurrencyCode(fromCode)
                                .toCurrencyCode(toCode)
                                .rate(rate)
                                .build();

                        exchangeRateRepository.save(exchangeRate);
                        ratesAdded++;
                        log.debug("Added exchange rate: {} -> {} = {}", fromCode, toCode, rate);
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Error fetching exchange rates from external API: {}", ex.getMessage());
        }

        return ratesAdded;
    }

    /**
     * Sync exchange rates from external API and update the database.
     * Fetches current rates for all existing currency pairs and updates them.
     */
    @Transactional
    public Map<String, Object> syncExchangeRatesFromApi() {
        log.info("Starting exchange rate sync from external API...");

        // Get all currencies from database
        List<Currency> currencies = currencyRepository.findAll();
        if (currencies.isEmpty()) {
            log.warn("No currencies found in database. Please initialize database first.");
            throw new ResourceNotFoundException("No currencies found", "currencies", "empty");
        }

        Map<String, Object> result = new HashMap<>();
        int ratesUpdated = 0;
        int ratesCreated = 0;
        int ratesFailed = 0;
        Map<String, String> failedCurrencies = new HashMap<>();

        // For each currency, fetch rates from external API
        for (Currency baseCurrency : currencies) {
            try {
                RateUpdateStats stats = fetchAndUpdateRatesForCurrency(baseCurrency.getCode(), currencies);
                ratesUpdated += stats.updated();
                ratesCreated += stats.created();
                log.info("Updated {} exchange rates for base currency: {} (created: {})",
                        stats.updated(), baseCurrency.getCode(), stats.created());
            } catch (Exception ex) {
                ratesFailed++;
                failedCurrencies.put(baseCurrency.getCode(), ex.getMessage());
                log.error("Failed to fetch rates for currency {}: {}", baseCurrency.getCode(), ex.getMessage());
            }
        }

        result.put("message", "Exchange rate sync completed");
        result.put("ratesUpdated", ratesUpdated);
        result.put("ratesCreated", ratesCreated);
        result.put("ratesFailed", ratesFailed);
        result.put("totalCurrencies", currencies.size());
        result.put("timestamp", System.currentTimeMillis());
        if (!failedCurrencies.isEmpty()) {
            result.put("failedCurrencies", failedCurrencies);
        }

        log.info("Exchange rate sync complete. Updated: {}, Failed: {}", ratesUpdated, ratesFailed);
        return result;
    }

    /**
     * Fetch rates for a specific base currency from external API and update database.
     */
    @SuppressWarnings("unchecked")
    private RateUpdateStats fetchAndUpdateRatesForCurrency(String baseCode, List<Currency> allCurrencies) {
        int updated = 0;
        int created = 0;

        try {
            String url = "https://open.er-api.com/v6/latest/" + baseCode;
            log.debug("Fetching rates from: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"success".equals(response.get("result"))) {
                throw new Exception("API returned unsuccessful status");
            }

            Map<String, Object> rates = (Map<String, Object>) response.get("rates");
            if (rates == null) {
                throw new Exception("No rates in API response");
            }

            // Update rates for each currency pair
            for (Currency targetCurrency : allCurrencies) {
                String targetCode = targetCurrency.getCode();

                if (!baseCode.equals(targetCode) && rates.containsKey(targetCode)) {
                    BigDecimal newRate = new BigDecimal(rates.get(targetCode).toString());

                    // Try to find existing rate
                    var existingRate = exchangeRateRepository
                            .findByFromCurrencyCodeAndToCurrencyCode(baseCode, targetCode);

                    if (existingRate.isPresent()) {
                        // Update existing rate
                        ExchangeRate rate = existingRate.get();
                        var oldRate = rate.getRate();
                        if (oldRate == null || newRate.compareTo(oldRate) != 0) {
                            rate.setRate(newRate);
                            exchangeRateRepository.save(rate);
                            historyService.recordRateChange(baseCode, targetCode, oldRate, newRate, "API_SYNC_UPDATE");
                            updated++;
                            log.debug("Updated rate: {} -> {} = {}", baseCode, targetCode, newRate);
                        }
                    } else {
                        // Create new rate
                        ExchangeRate rate = ExchangeRate.builder()
                                .fromCurrencyCode(baseCode)
                                .toCurrencyCode(targetCode)
                                .rate(newRate)
                                .build();
                        exchangeRateRepository.save(rate);
                        historyService.recordRateChange(baseCode, targetCode, newRate, newRate, "API_SYNC_CREATE");
                        created++;
                        log.debug("Created new rate: {} -> {} = {}", baseCode, targetCode, newRate);
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Error fetching/updating rates for {}: {}", baseCode, ex.getMessage());
            throw new RuntimeException("Failed to sync rates for " + baseCode, ex);
        }

        return new RateUpdateStats(updated, created);
    }

    private record RateUpdateStats(int updated, int created) {}

    /**
     * Get region for currency code.
     */
    private String getRegionForCode(String code) {
        return switch (code) {
            case "USD", "CAD" -> "North America";
            case "EUR" -> "Europe";
            case "GBP" -> "Europe";
            case "JPY" -> "Asia";
            case "INR" -> "Asia";
            case "AUD" -> "Oceania";
            case "CHF" -> "Europe";
            case "CNY" -> "Asia";
            case "SGD" -> "Asia";
            default -> "World";
        };
    }
}
