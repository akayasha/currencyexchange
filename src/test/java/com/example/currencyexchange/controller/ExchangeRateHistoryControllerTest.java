package com.example.currencyexchange.controller;

import com.example.currencyexchange.dto.ExchangeRateHistoryDto;
import com.example.currencyexchange.exception.GlobalExceptionHandler;
import com.example.currencyexchange.service.ExchangeRateHistoryService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ExchangeRateHistoryController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateHistoryController Tests")
class ExchangeRateHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExchangeRateHistoryService historyService;

    @InjectMocks
    private ExchangeRateHistoryController historyController;

    private ExchangeRateHistoryDto historyDto;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        mockMvc = MockMvcBuilders.standaloneSetup(historyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        historyDto = ExchangeRateHistoryDto.builder()
                .uuid(testUuid)
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9200"))
                .newRate(new BigDecimal("0.9250"))
                .changeReason("Market update")
                .createdAt(LocalDateTime.now())
                .createdBy("SYSTEM")
                .build();
    }

    @Test
    @DisplayName("GET /api/history/pair - Should get history for currency pair")
    void getHistoryForPair_ShouldReturn200() throws Exception {
        Page<ExchangeRateHistoryDto> page = new PageImpl<>(List.of(historyDto), PageRequest.of(0, 10), 1);
        when(historyService.getHistoryForPair("USD", "EUR", PageRequest.of(0, 10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/history/pair")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fromCurrencyCode").value("USD"))
                .andExpect(jsonPath("$.data.content[0].toCurrencyCode").value("EUR"));
    }

    @Test
    @DisplayName("GET /api/history/pair - Should convert currency codes to uppercase")
    void getHistoryForPair_ShouldConvertToUppercase() throws Exception {
        Page<ExchangeRateHistoryDto> page = new PageImpl<>(List.of(historyDto), PageRequest.of(0, 10), 1);
        when(historyService.getHistoryForPair("USD", "EUR", PageRequest.of(0, 10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/history/pair")
                .param("from", "usd")
                .param("to", "eur"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/history/currency - Should get history for base currency")
    void getHistoryForCurrency_ShouldReturn200() throws Exception {
        Page<ExchangeRateHistoryDto> page = new PageImpl<>(List.of(historyDto), PageRequest.of(0, 10), 1);
        when(historyService.getHistoryForCurrency("USD", PageRequest.of(0, 10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/history/currency")
                .param("from", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fromCurrencyCode").value("USD"));
    }

    @Test
    @DisplayName("GET /api/history/pair/details - Should get history with exchange rate details")
    void getHistoryWithDetails_ShouldReturn200() throws Exception {
        when(historyService.getHistoryWithRateDetails("USD", "EUR"))
                .thenReturn(List.of(historyDto));

        mockMvc.perform(get("/api/history/pair/details")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fromCurrencyCode").value("USD"));
    }

    @Test
    @DisplayName("GET /api/history/{id} - Should get history by id")
    void getHistoryById_ShouldReturn200() throws Exception {
        when(historyService.getHistoryById(testUuid)).thenReturn(historyDto);

        mockMvc.perform(get("/api/history/" + testUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.changeReason").value("Market update"));
    }

    @Test
    @DisplayName("GET /api/history/pair/latest - Should get most recent history")
    void getMostRecentHistory_ShouldReturn200() throws Exception {
        when(historyService.getMostRecentHistory("USD", "EUR")).thenReturn(historyDto);

        mockMvc.perform(get("/api/history/pair/latest")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newRate").value(0.9250));
    }

    @Test
    @DisplayName("GET /api/history/pair/stats - Should get history statistics")
    void getHistoryStats_ShouldReturn200() throws Exception {
        when(historyService.getHistoryCount("USD", "EUR")).thenReturn(5L);

        mockMvc.perform(get("/api/history/pair/stats")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromCurrencyCode").value("USD"))
                .andExpect(jsonPath("$.data.toCurrencyCode").value("EUR"))
                .andExpect(jsonPath("$.data.totalChanges").value(5));
    }

    @Test
    @DisplayName("GET /api/history/pair/stats - Should include timestamp")
    void getHistoryStats_ShouldIncludeTimestamp() throws Exception {
        when(historyService.getHistoryCount("USD", "EUR")).thenReturn(3L);

        mockMvc.perform(get("/api/history/pair/stats")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/history/pair - Should return paginated results")
    void getHistoryForPair_ShouldReturnPaginatedResults() throws Exception {
        ExchangeRateHistoryDto history2 = ExchangeRateHistoryDto.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9250"))
                .newRate(new BigDecimal("0.9300"))
                .changeReason("Rate adjustment")
                .createdAt(LocalDateTime.now().minusHours(1))
                .createdBy("SYSTEM")
                .build();

        Page<ExchangeRateHistoryDto> page = new PageImpl<>(
                List.of(historyDto, history2),
                PageRequest.of(0, 10),
                2
        );
        when(historyService.getHistoryForPair("USD", "EUR", PageRequest.of(0, 10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/history/pair")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/history/pair - Should order by timestamp descending")
    void getHistoryForPair_ShouldOrderByTimestampDescending() throws Exception {
        ExchangeRateHistoryDto newerHistory = ExchangeRateHistoryDto.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9250"))
                .newRate(new BigDecimal("0.9300"))
                .changeReason("Recent update")
                .createdAt(LocalDateTime.now())
                .createdBy("SYSTEM")
                .build();

        ExchangeRateHistoryDto olderHistory = ExchangeRateHistoryDto.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9200"))
                .newRate(new BigDecimal("0.9250"))
                .changeReason("Old update")
                .createdAt(LocalDateTime.now().minusHours(1))
                .createdBy("SYSTEM")
                .build();

        Page<ExchangeRateHistoryDto> page = new PageImpl<>(
                List.of(newerHistory, olderHistory),
                PageRequest.of(0, 10),
                2
        );
        when(historyService.getHistoryForPair("USD", "EUR", PageRequest.of(0, 10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/history/pair")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].changeReason").value("Recent update"));
    }

    @Test
    @DisplayName("GET /api/history/pair - Should store rate change correctly")
    void getHistoryForPair_ShouldStoreRateChangeCorrectly() throws Exception {
        Page<ExchangeRateHistoryDto> page = new PageImpl<>(List.of(historyDto), PageRequest.of(0, 10), 1);
        when(historyService.getHistoryForPair("USD", "EUR", PageRequest.of(0, 10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/history/pair")
                .param("from", "USD")
                .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].oldRate").value(0.9200))
                .andExpect(jsonPath("$.data.content[0].newRate").value(0.9250));
    }

    @Test
    @DisplayName("GET /api/history/{id} - Should include all history details")
    void getHistoryById_ShouldIncludeAllDetails() throws Exception {
        when(historyService.getHistoryById(testUuid)).thenReturn(historyDto);

        mockMvc.perform(get("/api/history/" + testUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromCurrencyCode").value("USD"))
                .andExpect(jsonPath("$.data.toCurrencyCode").value("EUR"))
                .andExpect(jsonPath("$.data.oldRate").value(0.9200))
                .andExpect(jsonPath("$.data.newRate").value(0.9250))
                .andExpect(jsonPath("$.data.createdBy").value("SYSTEM"));
    }

    @Test
    @DisplayName("GET /api/history/pair/stats - Should convert currency codes to uppercase")
    void getHistoryStats_ShouldConvertToUppercase() throws Exception {
        when(historyService.getHistoryCount("USD", "EUR")).thenReturn(5L);

        mockMvc.perform(get("/api/history/pair/stats")
                .param("from", "usd")
                .param("to", "eur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromCurrencyCode").value("USD"));
    }

    @Test
    @DisplayName("GET /api/history/pair/stats - Should return zero count when no history")
    void getHistoryStats_ShouldReturnZeroCountWhenNoHistory() throws Exception {
        when(historyService.getHistoryCount("EUR", "USD")).thenReturn(0L);

        mockMvc.perform(get("/api/history/pair/stats")
                .param("from", "EUR")
                .param("to", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalChanges").value(0));
    }
}
