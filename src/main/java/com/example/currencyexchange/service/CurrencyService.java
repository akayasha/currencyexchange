package com.example.currencyexchange.service;

import com.example.currencyexchange.dto.CurrencyDto;
import com.example.currencyexchange.exception.DuplicateResourceException;
import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.model.Currency;
import com.example.currencyexchange.repository.CurrencyRepository;
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
 * Service layer for Currency operations.
 * Handles business logic: validation, mapping, persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    /**
     * GET all currencies with pagination.
     */
    public Page<CurrencyDto> getAllCurrencies(Pageable pageable) {
        return currencyRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * GET currency by ID.
     */
    public CurrencyDto getCurrencyById(UUID id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "id", id));
        return toDto(currency);
    }

    /**
     * GET currency by code (e.g. "USD").
     */
    public CurrencyDto getCurrencyByCode(String code) {
        Currency currency = currencyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "code", code));
        return toDto(currency);
    }

    /**
     * Search currencies by keyword with pagination.
     * Demonstrates paginated search method requirement.
     */
    public Page<CurrencyDto> searchCurrencies(String keyword, Pageable pageable) {
        return currencyRepository.searchCurrencies(keyword, pageable).map(this::toDto);
    }

    /**
     * POST - Create a new currency.
     */
    @Transactional
    public CurrencyDto createCurrency(CurrencyDto dto) {
        String code = dto.getCode().toUpperCase();
        if (currencyRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Currency with code '" + code + "' already exists");
        }
        Currency currency = fromDto(dto);
        currency.setCode(code);
        Currency saved = currencyRepository.save(currency);
        log.info("Created currency: {}", saved.getCode());
        return toDto(saved);
    }

    /**
     * PUT - Update an existing currency.
     */
    @Transactional
    public CurrencyDto updateCurrency(UUID id, CurrencyDto dto) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "id", id));
        currency.setName(dto.getName());
        currency.setSymbol(dto.getSymbol());
        currency.setRegion(dto.getRegion());
        currency.setActive(dto.isActive());
        Currency saved = currencyRepository.save(currency);
        log.info("Updated currency: {}", saved.getCode());
        return toDto(saved);
    }

    /**
     * PATCH - Partially update currency (toggle active status).
     */
    @Transactional
    public CurrencyDto patchCurrencyStatus(UUID uuid, boolean isActive) {
        Currency currency = currencyRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "id", uuid));
        currency.setActive(isActive);
        Currency saved = currencyRepository.save(currency);
        log.info("Patched currency {} active status to: {}", saved.getCode(), isActive);
        return toDto(saved);
    }

    /**
     * DELETE - Remove a currency by ID.
     */
    @Transactional
    public void deleteCurrency(UUID id) {
        if (!currencyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Currency", "id", id);
        }
        currencyRepository.deleteById(id);
        log.info("Deleted currency with id: {}", id);
    }

    /**
     * Get currencies by region.
     */
    public List<CurrencyDto> getCurrenciesByRegion(String region) {
        List<Currency> currencies = currencyRepository.findByRegion(region);
        return currencies.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Get active currencies only.
     */
    public List<CurrencyDto> getActiveCurrencies() {
        List<Currency> currencies = currencyRepository.findByIsActive(true);
        return currencies.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Get currency statistics.
     */
    public Map<String, Object> getCurrencyStats() {
        long totalCurrencies = currencyRepository.count();
        long activeCurrencies = currencyRepository.countByIsActive(true);
        long inactiveCurrencies = totalCurrencies - activeCurrencies;

        // Count by region
        List<Object[]> regionStats = currencyRepository.countByRegion();
        Map<String, Long> currenciesByRegion = new HashMap<>();
        for (Object[] stat : regionStats) {
            currenciesByRegion.put((String) stat[0], (Long) stat[1]);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCurrencies", totalCurrencies);
        stats.put("activeCurrencies", activeCurrencies);
        stats.put("inactiveCurrencies", inactiveCurrencies);
        stats.put("currenciesByRegion", currenciesByRegion);
        stats.put("timestamp", System.currentTimeMillis());

        return stats;
    }

    // --- Mapping helpers ---

    private CurrencyDto toDto(Currency currency) {
        return CurrencyDto.builder()
                .uuid(currency.getUuid())
                .code(currency.getCode())
                .name(currency.getName())
                .symbol(currency.getSymbol())
                .region(currency.getRegion())
                .isActive(currency.isActive())
                .build();
    }

    private Currency fromDto(CurrencyDto dto) {
        return Currency.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .symbol(dto.getSymbol())
                .region(dto.getRegion())
                .isActive(dto.isActive())
                .build();
    }
}
