package com.example.currencyexchange.controller;

import com.example.currencyexchange.dto.ExchangeRateDto;
import com.example.currencyexchange.dto.ExchangeRateWithCurrencyDto;
import com.example.currencyexchange.exception.GlobalExceptionHandler;
import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ExchangeRateController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateController Tests")
class ExchangeRateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ExchangeRateController exchangeRateController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ExchangeRateDto usdToEurDto;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        mockMvc = MockMvcBuilders.standaloneSetup(exchangeRateController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        usdToEurDto = ExchangeRateDto.builder()
                .uuid(testUuid)
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9250"))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/exchange-rates - Should return paginated list")
    void getAllRates_ShouldReturn200WithPaginatedList() throws Exception {
        Page<ExchangeRateDto> page = new PageImpl<>(List.of(usdToEurDto), PageRequest.of(0, 10), 1);
        when(exchangeRateService.getAllRates(any())).thenReturn(page);

        mockMvc.perform(get("/api/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fromCurrencyCode").value("USD"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/exchange-rates/{id} - Should return exchange rate")
    void getRateById_WhenExists_ShouldReturn200() throws Exception {
        when(exchangeRateService.getRateById(testUuid)).thenReturn(usdToEurDto);

        mockMvc.perform(get("/api/exchange-rates/" + testUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromCurrencyCode").value("USD"))
                .andExpect(jsonPath("$.data.toCurrencyCode").value("EUR"));
    }

    @Test
    @DisplayName("GET /api/exchange-rates/{id} - Should return 404 when not found")
    void getRateById_WhenNotExists_ShouldReturn404() throws Exception {
        UUID notFoundUuid = UUID.randomUUID();
        when(exchangeRateService.getRateById(notFoundUuid))
                .thenThrow(new ResourceNotFoundException("ExchangeRate", "id", notFoundUuid));

        mockMvc.perform(get("/api/exchange-rates/" + notFoundUuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/exchange-rates/pair - Should get rate by currency pair")
    void getRateByPair_ShouldReturn200() throws Exception {
        when(exchangeRateService.getRateByPair("USD", "EUR")).thenReturn(usdToEurDto);

        mockMvc.perform(get("/api/exchange-rates/pair")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rate").value(0.9250));
    }

    @Test
    @DisplayName("GET /api/exchange-rates/details - Should return rates with currency details")
    void getRatesWithDetails_ShouldReturnEnrichedData() throws Exception {
        ExchangeRateWithCurrencyDto withDetails = ExchangeRateWithCurrencyDto.builder()
                .uuid(testUuid)
                .fromCurrencyCode("USD")
                .fromCurrencyName("United States Dollar")
                .fromCurrencySymbol("$")
                .toCurrencyCode("EUR")
                .toCurrencyName("Euro")
                .toCurrencySymbol("€")
                .rate(new BigDecimal("0.9250"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(exchangeRateService.getRatesWithCurrencyDetails("USD", "EUR"))
                .thenReturn(List.of(withDetails));

        mockMvc.perform(get("/api/exchange-rates/details")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fromCurrencyName").value("United States Dollar"))
                .andExpect(jsonPath("$.data[0].toCurrencyName").value("Euro"));
    }

    @Test
    @DisplayName("GET /api/exchange-rates/search - Should search rates by keyword")
    void searchRates_ShouldReturnMatchingRates() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeRateDto> page = new PageImpl<>(List.of(usdToEurDto), pageable, 1);
        when(exchangeRateService.searchRates(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/exchange-rates/search")
                .param("keyword", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fromCurrencyCode").value("USD"));
    }

    @Test
    @DisplayName("POST /api/exchange-rates - Should create new exchange rate")
    void createRate_ShouldReturn201() throws Exception {
        ExchangeRateDto newRate = ExchangeRateDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("GBP")
                .rate(new BigDecimal("0.7950"))
                .build();

        ExchangeRateDto created = ExchangeRateDto.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("USD")
                .toCurrencyCode("GBP")
                .rate(new BigDecimal("0.7950"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(exchangeRateService.createRate(any())).thenReturn(created);

        mockMvc.perform(post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fromCurrencyCode").value("USD"))
                .andExpect(jsonPath("$.data.toCurrencyCode").value("GBP"));
    }

    @Test
    @DisplayName("POST /api/exchange-rates - Should return 400 on validation failure")
    void createRate_WithInvalidData_ShouldReturn400() throws Exception {
        ExchangeRateDto invalidRate = ExchangeRateDto.builder()
                .fromCurrencyCode("")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9250"))
                .build();

        mockMvc.perform(post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/exchange-rates/{id} - Should update exchange rate")
    void updateRate_ShouldReturn200() throws Exception {
        ExchangeRateDto updateDto = ExchangeRateDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9500"))
                .build();

        ExchangeRateDto updated = ExchangeRateDto.builder()
                .uuid(testUuid)
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9500"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(exchangeRateService.updateRate(org.mockito.ArgumentMatchers.eq(testUuid), any(ExchangeRateDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/exchange-rates/" + testUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rate").value(0.9500));
    }

    @Test
    @DisplayName("DELETE /api/exchange-rates/{id} - Should delete exchange rate")
    void deleteRate_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/exchange-rates/" + testUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted successfully"));
    }

    @Test
    @DisplayName("POST /api/exchange-rates/bulk - Should create multiple rates")
    void createBulkRates_ShouldReturn201() throws Exception {
        List<ExchangeRateDto> rates = Arrays.asList(
                ExchangeRateDto.builder()
                        .fromCurrencyCode("USD")
                        .toCurrencyCode("EUR")
                        .rate(new BigDecimal("0.9250"))
                        .build(),
                ExchangeRateDto.builder()
                        .fromCurrencyCode("USD")
                        .toCurrencyCode("GBP")
                        .rate(new BigDecimal("0.7950"))
                        .build()
        );

        mockMvc.perform(post("/api/exchange-rates/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rates)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/exchange-rates/base/{fromCode} - Should get rates for base currency")
    void getRatesForBaseCurrency_ShouldReturn200() throws Exception {
        when(exchangeRateService.getRatesForBaseCurrency("USD"))
                .thenReturn(List.of(usdToEurDto));

        mockMvc.perform(get("/api/exchange-rates/base/USD"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/exchange-rates/analytics - Should return analytics")
    void getAnalytics_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/exchange-rates/analytics"))
                .andExpect(status().isOk());
    }
}
