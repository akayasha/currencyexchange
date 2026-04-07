package com.example.currencyexchange.controller;

import com.example.currencyexchange.dto.ResponseApi;
import com.example.currencyexchange.dto.CurrencyDto;
import com.example.currencyexchange.exception.GlobalExceptionHandler;
import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Currency CRUD operations.
 * Demonstrates GET, POST, PUT, PATCH, DELETE HTTP methods.
 */
@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Tag(name = "Currency",
        description = "Manage currencies — create, read, update, delete, and search ISO currency records")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Operation(summary = "List all currencies", description = "Returns a paginated list of all currencies. Use ?page, ?size, and ?sort query parameters.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public ResponseEntity<ResponseApi<Page<CurrencyDto>>> getAllCurrencies(
            @PageableDefault(size = 10, sort = "code") Pageable pageable) {
        Page<CurrencyDto> currencies = currencyService.getAllCurrencies(pageable);
        return ResponseEntity.ok(ResponseApi.success(200, "Currencies retrieved successfully", currencies));
    }

    @Operation(summary = "Get currency by ID", description = "Returns a single currency by its database ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Currency found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Currency not found",
                    content = @Content(schema = @Schema(implementation = ResponseApi.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseApi<CurrencyDto>> getCurrencyById(
            @Parameter(description = "Database ID of the currency", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {

        CurrencyDto currency = currencyService.getCurrencyById(id);

        return ResponseEntity.ok(
                ResponseApi.success(200, "Currency retrieved successfully", currency)
        );
    }


    @Operation(summary = "Get currency by code", description = "Returns a currency by its ISO 3-letter code (e.g. USD, EUR, GBP).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currency found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency not found")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseApi<CurrencyDto>> getCurrencyByCode(
            @Parameter(description = "ISO 3-letter currency code", example = "USD") @PathVariable String code) {
        CurrencyDto currency = currencyService.getCurrencyByCode(code);
        return ResponseEntity.ok(ResponseApi.success(200, "Currency retrieved successfully", currency));
    }

    @Operation(summary = "Search currencies with pagination",
            description = "Search currencies by keyword (matches code, name, or region). Supports pagination via ?page and ?size.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned")
    @GetMapping("/search")
    public ResponseEntity<ResponseApi<Page<CurrencyDto>>> searchCurrencies(
            @Parameter(description = "Search keyword (matches code, name, region)", example = "europe")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CurrencyDto> results = currencyService.searchCurrencies(keyword, pageable);
        return ResponseEntity.ok(ResponseApi.success(200, "Search completed successfully", results));
    }

    @Operation(summary = "Create a new currency", description = "Creates a new currency record. The code must be unique and exactly 3 characters.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Currency created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Currency code already exists")
    })
    @PostMapping
    public ResponseEntity<ResponseApi<CurrencyDto>> createCurrency(@Valid @RequestBody CurrencyDto dto) {
        CurrencyDto created = currencyService.createCurrency(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.success(201, "Currency created successfully", created));
    }

    @Operation(summary = "Update a currency", description = "Replaces all updatable fields of an existing currency.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currency updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResponseApi<CurrencyDto>> updateCurrency(
            @Parameter(description = "Database ID of the currency to update", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id,
            @Valid @RequestBody CurrencyDto dto) {
        CurrencyDto updated = currencyService.updateCurrency(id, dto);
        return ResponseEntity.ok(ResponseApi.success(200, "Currency updated successfully", updated));
    }

    @Operation(summary = "Toggle currency active status (PATCH)",
            description = "Partially updates a currency — enables or disables it without changing other fields.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency not found")
    })
    @PatchMapping("/{uuid}")
    public ResponseEntity<ResponseApi<CurrencyDto>> patchCurrencyStatus(
            @Parameter(description = "Database ID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID uuid,
            @Parameter(description = "Set true to activate, false to deactivate", example = "false")
            @RequestParam boolean isActive) {
        CurrencyDto updated = currencyService.patchCurrencyStatus(uuid, isActive);
        return ResponseEntity.ok(ResponseApi.success(200, "Currency status updated successfully", updated));
    }

    @Operation(summary = "Delete a currency", description = "Permanently removes a currency by its database ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseApi<Void>> deleteCurrency(
            @Parameter(description = "Database ID of the currency to delete", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.ok(ResponseApi.success(200, "Currency deleted successfully", null));
    }

    @Operation(summary = "Create multiple currencies at once",
            description = "Bulk create currencies. Useful for importing multiple currencies from external sources.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Currencies created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid currency data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Some currencies already exist")
    })
    @PostMapping("/bulk")
    public ResponseEntity<ResponseApi<Map<String, Object>>> createCurrenciesBulk(
            @Parameter(description = "List of currencies to create") @RequestBody List<CurrencyDto> currencies) {
        List<CurrencyDto> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (CurrencyDto dto : currencies) {
            try {
                CurrencyDto createdCurrency = currencyService.createCurrency(dto);
                created.add(createdCurrency);
            } catch (Exception e) {
                errors.add("Failed to create currency " + dto.getCode() + ": " + e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("createdCount", created.size());
        response.put("errorCount", errors.size());
        response.put("createdCurrencies", created);
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseApi.success(201, "Bulk currency creation completed", response));
    }

    @Operation(summary = "Update currency status in bulk",
            description = "Enable or disable multiple currencies at once")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currencies updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Some currencies not found")
    })
    @PatchMapping("/bulk/status")
    public ResponseEntity<ResponseApi<Map<String, Object>>> updateCurrenciesStatusBulk(
            @Parameter(description = "Currency codes and their new status") @RequestBody Map<String, Boolean> statusUpdates) {
        List<String> updated = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : statusUpdates.entrySet()) {
            try {
                CurrencyDto currency = currencyService.getCurrencyByCode(entry.getKey().toUpperCase());
                currency.setActive(entry.getValue().booleanValue());
                currencyService.updateCurrency(currency.getUuid(), currency);
                updated.add(entry.getKey());
            } catch (ResourceNotFoundException e) {
                notFound.add(entry.getKey());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("updatedCount", updated.size());
        response.put("notFoundCount", notFound.size());
        response.put("updatedCurrencies", updated);
        if (!notFound.isEmpty()) {
            response.put("notFoundCurrencies", notFound);
        }

        return ResponseEntity.ok(ResponseApi.success(200, "Bulk status update completed", response));
    }

    @Operation(summary = "Get currencies by region",
            description = "Retrieve all currencies for a specific region")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currencies retrieved successfully")
    })
    @GetMapping("/region/{region}")
    public ResponseEntity<ResponseApi<List<CurrencyDto>>> getCurrenciesByRegion(
            @Parameter(description = "Region name", example = "Europe") @PathVariable String region) {
        List<CurrencyDto> currencies = currencyService.getCurrenciesByRegion(region);
        return ResponseEntity.ok(ResponseApi.success(200, "Currencies retrieved successfully", currencies));
    }

    @Operation(summary = "Get active currencies only",
            description = "Retrieve all currencies that are currently active")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active currencies retrieved successfully")
    })
    @GetMapping("/active")
    public ResponseEntity<ResponseApi<List<CurrencyDto>>> getActiveCurrencies() {
        List<CurrencyDto> currencies = currencyService.getActiveCurrencies();
        return ResponseEntity.ok(ResponseApi.success(200, "Active currencies retrieved successfully", currencies));
    }

    @Operation(summary = "Get currency statistics",
            description = "Get statistics about currencies in the system")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @GetMapping("/stats")
    public ResponseEntity<ResponseApi<Map<String, Object>>> getCurrencyStats() {
        Map<String, Object> stats = currencyService.getCurrencyStats();
        return ResponseEntity.ok(ResponseApi.success(200, "Statistics retrieved successfully", stats));
    }
}
