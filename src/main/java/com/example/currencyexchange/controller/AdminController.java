package com.example.currencyexchange.controller;

import com.example.currencyexchange.dto.ResponseApi;
import com.example.currencyexchange.service.DataInitializationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin operations for database initialization")
public class AdminController {

    private final DataInitializationService dataInitializationService;

    @Operation(
            summary = "Initialize database with popular currencies and exchange rates",
            description = "Populate database with initial currency data"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Database initialized successfully"),
            @ApiResponse(responseCode = "500", description = "Error during initialization")
    })
    @PostMapping("/init-database")
    public ResponseEntity<ResponseApi<Map<String, Object>>> initializeDatabase(HttpServletRequest request) {

        Map<String, Object> result = dataInitializationService.initializeDatabase();

        return ResponseEntity.ok(
                ResponseApi.of(
                        200,
                        "Database initialized successfully",
                        result,
                        request.getRequestURI()
                )
        );
    }

    @Operation(
            summary = "Sync exchange rates from external API",
            description = "Update exchange rates from external API"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exchange rates synced successfully"),
            @ApiResponse(responseCode = "404", description = "No currencies found"),
            @ApiResponse(responseCode = "500", description = "Error during sync")
    })
    @PostMapping("/sync-exchange-rates")
    public ResponseEntity<ResponseApi<Map<String, Object>>> syncExchangeRates(HttpServletRequest request) {

        Map<String, Object> result = dataInitializationService.syncExchangeRatesFromApi();

        return ResponseEntity.ok(
                ResponseApi.of(
                        200,
                        "Exchange rates synced successfully",
                        result,
                        request.getRequestURI()
                )
        );
    }
}