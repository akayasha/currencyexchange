package com.example.currencyexchange.controller;

import com.example.currencyexchange.dto.ConversionRequestDto;
import com.example.currencyexchange.dto.ConversionResponseDto;
import com.example.currencyexchange.dto.ResponseApi;
import com.example.currencyexchange.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/convert")
@RequiredArgsConstructor
@Tag(name = "Conversion", description = "Convert amounts between currencies using stored rates or a live external API fallback")
public class ConversionController {

    private final ConversionService conversionService;

    @Operation(
            summary = "Convert currency amount (POST)",
            description = """
                    Converts an amount from one currency to another.

                    **Logic:**
                    1. Looks up the exchange rate in the H2 database first.
                    2. If not found locally, calls the external API at `open.er-api.com` for a live rate.

                    The `source` field in the response indicates whether the rate came from `"db"` or `"external-api"`.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversion successful"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found in DB or external API")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = ConversionRequestDto.class),
                    examples = {
                            @ExampleObject(name = "USD to EUR (DB rate)", value = """
                                {"fromCurrencyCode":"USD","toCurrencyCode":"EUR","amount":100.00}"""),
                            @ExampleObject(name = "USD to KRW (external API fallback)", value = """
                                {"fromCurrencyCode":"USD","toCurrencyCode":"KRW","amount":500.00}""")
                    }
            )
    )
    @PostMapping
    public ResponseEntity<ResponseApi<ConversionResponseDto>> convert(
            @Valid @RequestBody ConversionRequestDto request,
            HttpServletRequest httpRequest) {

        ConversionResponseDto result = conversionService.convert(request);

        return ResponseEntity.ok(
                ResponseApi.of(200, "Conversion successful", result, httpRequest.getRequestURI())
        );
    }

    @Operation(
            summary = "Convert currency amount (GET)",
            description = "Convenience GET endpoint for quick conversions via query parameters. Same logic as the POST endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversion successful"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found")
    })
    @GetMapping
    public ResponseEntity<ResponseApi<ConversionResponseDto>> convertGet(
            @Parameter(description = "Source currency code", example = "GBP") @RequestParam String from,
            @Parameter(description = "Target currency code", example = "USD") @RequestParam String to,
            @Parameter(description = "Amount to convert", example = "250.00") @RequestParam BigDecimal amount,
            HttpServletRequest httpRequest) {

        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrencyCode(from)
                .toCurrencyCode(to)
                .amount(amount)
                .build();

        ConversionResponseDto result = conversionService.convert(request);

        return ResponseEntity.ok(
                ResponseApi.of(200, "Conversion successful", result, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "Convert multiple amounts at once",
            description = "Bulk currency conversion for multiple amounts and currency pairs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversions completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid conversion data")
    })
    @PostMapping("/bulk")
    public ResponseEntity<ResponseApi<List<ConversionResponseDto>>> convertBulk(
            @Parameter(description = "List of conversion requests") @RequestBody List<ConversionRequestDto> requests,
            HttpServletRequest httpRequest) {

        List<ConversionResponseDto> responses = new ArrayList<>();

        for (ConversionRequestDto request : requests) {
            try {
                ConversionResponseDto response = conversionService.convertCurrency(request);
                responses.add(response);
            } catch (Exception e) {
                ConversionResponseDto errorResponse = ConversionResponseDto.builder()
                        .fromCurrencyCode(request.getFromCurrencyCode())
                        .toCurrencyCode(request.getToCurrencyCode())
                        .amount(request.getAmount())
                        .source("error")
                        .build();
                responses.add(errorResponse);
            }
        }

        return ResponseEntity.ok(
                ResponseApi.of(200, "Bulk conversion completed", responses, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "Get conversion history",
            description = "Retrieve paginated history of currency conversions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversion history retrieved successfully")
    })
    @GetMapping("/history")
    public ResponseEntity<ResponseApi<Page<ConversionResponseDto>>> getConversionHistory(
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable,
            HttpServletRequest httpRequest) {

        Page<ConversionResponseDto> history = conversionService.getConversionHistory(pageable);

        return ResponseEntity.ok(
                ResponseApi.of(200, "Conversion history retrieved successfully", history, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "Get conversion statistics",
            description = "Get statistics about currency conversions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @GetMapping("/stats")
    public ResponseEntity<ResponseApi<Map<String, Object>>> getConversionStats(
            HttpServletRequest httpRequest) {

        Map<String, Object> stats = conversionService.getConversionStats();

        return ResponseEntity.ok(
                ResponseApi.of(200, "Statistics retrieved successfully", stats, httpRequest.getRequestURI())
        );
    }
}