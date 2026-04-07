package com.example.currencyexchange.controller;

import com.example.currencyexchange.dto.*;
import com.example.currencyexchange.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
@Tag(name = "Exchange Rates", description = "Manage exchange rates between currency pairs; includes a JOIN query endpoint for enriched data")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Operation(summary = "List all exchange rates", description = "Returns a paginated list of all stored exchange rates.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public ResponseEntity<ResponseApi<Page<ExchangeRateDto>>> getAllRates(
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request) {

        return ResponseEntity.ok(
                ResponseApi.of(200, "Successfully retrieved list",
                        exchangeRateService.getAllRates(pageable),
                        request.getRequestURI())
        );
    }

    @Operation(summary = "Get exchange rate by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rate found"),
            @ApiResponse(responseCode = "404", description = "Rate not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<ExchangeRateDto>> getRateById(
            @PathVariable UUID id,
            HttpServletRequest request) {

        return ResponseEntity.ok(
                ResponseApi.of(200, "Rate found",
                        exchangeRateService.getRateById(id),
                        request.getRequestURI())
        );
    }

    @Operation(summary = "Get rate for a specific currency pair",
            description = "Returns the exchange rate for a given FROM/TO currency pair (e.g. USD → EUR).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rate found"),
            @ApiResponse(responseCode = "404", description = "No rate exists for this pair")
    })
    @GetMapping("/pair")
    public ResponseEntity<ResponseApi<ExchangeRateDto>> getRateByPair(
            @RequestParam String from,
            @RequestParam String to,
            HttpServletRequest request) {

        return ResponseEntity.ok(
                ResponseApi.of(200, "Rate found",
                        exchangeRateService.getRateByPair(from, to),
                        request.getRequestURI())
        );
    }

    @Operation(summary = "Get rates with full currency details (JOIN query)",
            description = "Returns exchange rates enriched with currency names and symbols by joining the exchange_rate table with the currency table twice.")
    @ApiResponse(responseCode = "200", description = "Enriched exchange rate data returned")
    @GetMapping("/details")
    public ResponseEntity<ResponseApi<List<ExchangeRateWithCurrencyDto>>> getRatesWithDetails(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            HttpServletRequest request) {

        return ResponseEntity.ok(
                ResponseApi.of(200, "Data retrieved",
                        exchangeRateService.getRatesWithCurrencyDetails(from, to),
                        request.getRequestURI())
        );
    }

    @Operation(summary = "Search exchange rates with pagination",
            description = "Searches exchange rates by currency code keyword with full pagination support.")
    @ApiResponse(responseCode = "200", description = "Search results returned")
    @GetMapping("/search")
    public ResponseEntity<ResponseApi<Page<ExchangeRateDto>>> searchRates(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request) {

        return ResponseEntity.ok(
                ResponseApi.of(200, "Search results returned",
                        exchangeRateService.searchRates(keyword, pageable),
                        request.getRequestURI())
        );
    }

    @Operation(summary = "Create a new exchange rate")
    @PostMapping
    public ResponseEntity<ResponseApi<ExchangeRateDto>> createRate(
            @Valid @RequestBody ExchangeRateDto dto,
            HttpServletRequest request) {

        ExchangeRateDto created = exchangeRateService.createRate(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseApi.of(201, "Rate created", created, request.getRequestURI())
        );
    }

    @Operation(summary = "Create multiple exchange rates at once")
    @PostMapping("/bulk")
    public ResponseEntity<ResponseApi<Map<String, Object>>> createExchangeRatesBulk(
            @RequestBody List<ExchangeRateDto> rates,
            HttpServletRequest request) {

        List<ExchangeRateDto> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (ExchangeRateDto dto : rates) {
            try {
                created.add(exchangeRateService.createRate(dto));
            } catch (Exception e) {
                errors.add("Failed to create rate " + dto.getFromCurrencyCode() + "/" + dto.getToCurrencyCode() + ": " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("createdCount", created.size());
        response.put("errorCount", errors.size());
        response.put("createdRates", created);
        if (!errors.isEmpty()) response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseApi.of(201, "Bulk exchange rate creation completed", response, request.getRequestURI())
        );
    }

    @Operation(summary = "Update an exchange rate")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseApi<ExchangeRateDto>> updateRate(
            @PathVariable UUID id,
            @Valid @RequestBody ExchangeRateDto dto,
            HttpServletRequest request) {

        return ResponseEntity.ok(
                ResponseApi.of(200, "Rate updated",
                        exchangeRateService.updateRate(id, dto),
                        request.getRequestURI())
        );
    }

    @Operation(summary = "Update exchange rates in bulk")
    @PutMapping("/bulk")
    public ResponseEntity<ResponseApi<Map<String, Object>>> updateExchangeRatesBulk(
            @RequestBody List<ExchangeRateDto> rates,
            HttpServletRequest request) {

        List<ExchangeRateDto> updated = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (ExchangeRateDto dto : rates) {
            try {
                ExchangeRateDto existing = exchangeRateService.getRateByPair(dto.getFromCurrencyCode(), dto.getToCurrencyCode());
                updated.add(exchangeRateService.updateRate(existing.getUuid(), dto));
            } catch (Exception e) {
                errors.add("Failed to update rate " + dto.getFromCurrencyCode() + "/" + dto.getToCurrencyCode() + ": " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("updatedCount", updated.size());
        response.put("errorCount", errors.size());
        response.put("updatedRates", updated);
        if (!errors.isEmpty()) response.put("errors", errors);

        return ResponseEntity.ok(
                ResponseApi.of(200, "Bulk exchange rate update completed", response, request.getRequestURI())
        );
    }

    @Operation(summary = "Delete an exchange rate")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseApi<Void>> deleteRate(
            @PathVariable UUID id,
            HttpServletRequest request) {

        exchangeRateService.deleteRate(id);

        return ResponseEntity.ok(
                ResponseApi.success(200, "Deleted successfully")
        );
    }

    @Operation(summary = "Get exchange rates for base currency")
    @GetMapping("/base/{fromCode}")
    public ResponseEntity<ResponseApi<List<ExchangeRateDto>>> getRatesForBaseCurrency(
            @PathVariable String fromCode,
            HttpServletRequest request) {

        return ResponseEntity.ok(
                ResponseApi.of(200, "Exchange rates retrieved successfully",
                        exchangeRateService.getRatesForBaseCurrency(fromCode.toUpperCase()),
                        request.getRequestURI())
        );
    }

    @Operation(summary = "Get exchange rate analytics")
    @GetMapping("/analytics")
    public ResponseEntity<ResponseApi<Map<String, Object>>> getExchangeRateAnalytics(
            HttpServletRequest request) {

        return ResponseEntity.ok(
                ResponseApi.of(200, "Analytics retrieved successfully",
                        exchangeRateService.getExchangeRateAnalytics(),
                        request.getRequestURI())
        );
    }
}