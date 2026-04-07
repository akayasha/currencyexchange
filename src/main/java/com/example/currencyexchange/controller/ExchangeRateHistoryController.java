package com.example.currencyexchange.controller;

import com.example.currencyexchange.dto.ExchangeRateHistoryDto;
import com.example.currencyexchange.dto.ResponseApi;
import com.example.currencyexchange.service.ExchangeRateHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "Exchange Rate History", description = "Track and view history of exchange rate changes")
public class ExchangeRateHistoryController {

    private final ExchangeRateHistoryService historyService;

    @Operation(summary = "Get history for a currency pair",
            description = "Retrieve paginated history of rate changes for a specific currency pair")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No history found")
    })
    @GetMapping("/pair")
    public ResponseEntity<ResponseApi<Page<ExchangeRateHistoryDto>>> getHistoryForPair(
            @Parameter(description = "Source currency code", example = "USD") @RequestParam String from,
            @Parameter(description = "Target currency code", example = "EUR") @RequestParam String to,
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request) {

        Page<ExchangeRateHistoryDto> history =
                historyService.getHistoryForPair(from.toUpperCase(), to.toUpperCase(), pageable);

        return ResponseEntity.ok(
                ResponseApi.of(200, "History retrieved successfully", history, request.getRequestURI())
        );
    }

    @Operation(summary = "Get history for a base currency",
            description = "Retrieve paginated history of all rate changes for a specific base currency")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No history found")
    })
    @GetMapping("/currency")
    public ResponseEntity<ResponseApi<Page<ExchangeRateHistoryDto>>> getHistoryForCurrency(
            @Parameter(description = "Source currency code", example = "USD") @RequestParam String from,
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request) {

        Page<ExchangeRateHistoryDto> history =
                historyService.getHistoryForCurrency(from.toUpperCase(), pageable);

        return ResponseEntity.ok(
                ResponseApi.of(200, "History retrieved successfully", history, request.getRequestURI())
        );
    }

    @Operation(summary = "Get history with JOIN to exchange rate details",
            description = "Retrieve history joined with current exchange rate information (demonstrates JOIN query)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No history found")
    })
    @GetMapping("/pair/details")
    public ResponseEntity<ResponseApi<List<ExchangeRateHistoryDto>>> getHistoryWithDetails(
            @Parameter(description = "Source currency code", example = "USD") @RequestParam String from,
            @Parameter(description = "Target currency code", example = "EUR") @RequestParam String to,
            HttpServletRequest request) {

        List<ExchangeRateHistoryDto> history =
                historyService.getHistoryWithRateDetails(from.toUpperCase(), to.toUpperCase());

        return ResponseEntity.ok(
                ResponseApi.of(200, "History retrieved successfully", history, request.getRequestURI())
        );
    }

    @Operation(summary = "Get history by ID",
            description = "Retrieve a specific history record by its UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History found"),
            @ApiResponse(responseCode = "404", description = "History not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<ExchangeRateHistoryDto>> getHistoryById(
            @Parameter(description = "History UUID") @PathVariable UUID id,
            HttpServletRequest request) {

        ExchangeRateHistoryDto history = historyService.getHistoryById(id);

        return ResponseEntity.ok(
                ResponseApi.of(200, "History found", history, request.getRequestURI())
        );
    }

    @Operation(summary = "Get most recent history for a currency pair",
            description = "Retrieve the most recent rate change for a specific currency pair")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History found"),
            @ApiResponse(responseCode = "404", description = "No history found")
    })
    @GetMapping("/pair/latest")
    public ResponseEntity<ResponseApi<ExchangeRateHistoryDto>> getMostRecentHistory(
            @Parameter(description = "Source currency code", example = "USD") @RequestParam String from,
            @Parameter(description = "Target currency code", example = "EUR") @RequestParam String to,
            HttpServletRequest request) {

        ExchangeRateHistoryDto history =
                historyService.getMostRecentHistory(from.toUpperCase(), to.toUpperCase());

        return ResponseEntity.ok(
                ResponseApi.of(200, "History found", history, request.getRequestURI())
        );
    }

    @Operation(summary = "Get history statistics for a currency pair",
            description = "Retrieve statistics about the number of rate changes for a currency pair")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @GetMapping("/pair/stats")
    public ResponseEntity<ResponseApi<Map<String, Object>>> getHistoryStats(
            @Parameter(description = "Source currency code", example = "USD") @RequestParam String from,
            @Parameter(description = "Target currency code", example = "EUR") @RequestParam String to,
            HttpServletRequest request) {

        long count = historyService.getHistoryCount(from.toUpperCase(), to.toUpperCase());

        Map<String, Object> stats = new HashMap<>();
        stats.put("fromCurrencyCode", from.toUpperCase());
        stats.put("toCurrencyCode", to.toUpperCase());
        stats.put("totalChanges", count);
        stats.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(
                ResponseApi.of(200, "Statistics retrieved successfully", stats, request.getRequestURI())
        );
    }
}