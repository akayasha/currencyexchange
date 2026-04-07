package com.example.currencyexchange.controller;

import com.example.currencyexchange.dto.ConversionRequestDto;
import com.example.currencyexchange.dto.ConversionResponseDto;
import com.example.currencyexchange.exception.GlobalExceptionHandler;
import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.service.ConversionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ConversionController.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ConversionController Tests")
class ConversionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private ConversionController conversionController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ConversionResponseDto conversionResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(conversionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        conversionResponse = ConversionResponseDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("92.50"))
                .rateApplied(new BigDecimal("0.9250"))
                .timestamp(LocalDateTime.now())
                .source("db")
                .build();
    }

    @Test
    @DisplayName("POST /api/convert - Should convert currency successfully")
    void shouldConvertCurrencySuccessfully() throws Exception {
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        when(conversionService.convert(any())).thenReturn(conversionResponse);

        mockMvc.perform(post("/api/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromCurrencyCode").value("USD"))
                .andExpect(jsonPath("$.data.toCurrencyCode").value("EUR"))
                .andExpect(jsonPath("$.data.convertedAmount").value(92.50));
    }

    @Test
    @DisplayName("POST /api/convert - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        ConversionRequestDto invalidRequest = ConversionRequestDto.builder()
                .fromCurrencyCode("")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/convert - Should convert using query parameters")
    void shouldConvertUsingQueryParameters() throws Exception {
        when(conversionService.convert(any())).thenReturn(conversionResponse);

        mockMvc.perform(get("/api/convert")
                .param("from", "USD")
                .param("to", "EUR")
                .param("amount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.convertedAmount").value(92.50));
    }

    @Test
    @DisplayName("POST /api/convert/bulk - Should convert multiple amounts")
    void shouldConvertMultipleAmounts() throws Exception {
        List<ConversionRequestDto> requests = Arrays.asList(
                ConversionRequestDto.builder()
                        .fromCurrencyCode("USD")
                        .toCurrencyCode("EUR")
                        .amount(new BigDecimal("100.00"))
                        .build(),
                ConversionRequestDto.builder()
                        .fromCurrencyCode("USD")
                        .toCurrencyCode("GBP")
                        .amount(new BigDecimal("200.00"))
                        .build()
        );

        ConversionResponseDto response2 = ConversionResponseDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("GBP")
                .amount(new BigDecimal("200.00"))
                .convertedAmount(new BigDecimal("159.00"))
                .rateApplied(new BigDecimal("0.7950"))
                .timestamp(LocalDateTime.now())
                .source("db")
                .build();

        when(conversionService.convert(any()))
                .thenReturn(conversionResponse)
                .thenReturn(response2)
                .thenReturn(conversionResponse);

        mockMvc.perform(post("/api/convert/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/convert - Should indicate source as db")
    void shouldIndicateSourceAsDb() throws Exception {
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        when(conversionService.convert(any())).thenReturn(conversionResponse);

        mockMvc.perform(post("/api/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source").value("db"));
    }

    @Test
    @DisplayName("POST /api/convert - Should calculate conversion correctly")
    void shouldCalculateConversionCorrectly() throws Exception {
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("JPY")
                .amount(new BigDecimal("10.00"))
                .build();

        ConversionResponseDto response = ConversionResponseDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("JPY")
                .amount(new BigDecimal("10.00"))
                .convertedAmount(new BigDecimal("1495.00"))
                .rateApplied(new BigDecimal("149.50"))
                .timestamp(LocalDateTime.now())
                .source("db")
                .build();

        when(conversionService.convert(any())).thenReturn(response);

        mockMvc.perform(post("/api/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.convertedAmount").value(1495.00));
    }

    @Test
    @DisplayName("POST /api/convert - Should include timestamp in response")
    void shouldIncludeTimestampInResponse() throws Exception {
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        when(conversionService.convert(any())).thenReturn(conversionResponse);

        mockMvc.perform(post("/api/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.timestamp").exists());
    }
}
