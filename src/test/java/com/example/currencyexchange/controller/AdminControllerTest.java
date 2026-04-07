package com.example.currencyexchange.controller;

import com.example.currencyexchange.exception.GlobalExceptionHandler;
import com.example.currencyexchange.service.DataInitializationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AdminController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController Tests")
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DataInitializationService dataInitializationService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("POST /api/admin/init-database - Should initialize database")
    void initializeDatabase_ShouldReturn200() throws Exception {
        Map<String, Object> initResult = new HashMap<>();
        initResult.put("message", "Database initialized successfully");
        initResult.put("currenciesAdded", 10);
        initResult.put("ratesAdded", 45);
        initResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.initializeDatabase()).thenReturn(initResult);

        mockMvc.perform(post("/api/admin/init-database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Database initialized successfully"))
                .andExpect(jsonPath("$.data.currenciesAdded").value(10))
                .andExpect(jsonPath("$.data.ratesAdded").value(45));
    }

    @Test
    @DisplayName("POST /api/admin/init-database - Should indicate successful initialization")
    void initializeDatabase_ShouldReturnSuccessMessage() throws Exception {
        Map<String, Object> initResult = new HashMap<>();
        initResult.put("message", "Database initialized successfully");
        initResult.put("currenciesAdded", 10);
        initResult.put("ratesAdded", 45);
        initResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.initializeDatabase()).thenReturn(initResult);

        mockMvc.perform(post("/api/admin/init-database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/admin/init-database - Should return currencies added count")
    void initializeDatabase_ShouldReturnCurrenciesAddedCount() throws Exception {
        Map<String, Object> initResult = new HashMap<>();
        initResult.put("message", "Database initialized successfully");
        initResult.put("currenciesAdded", 5);
        initResult.put("ratesAdded", 20);
        initResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.initializeDatabase()).thenReturn(initResult);

        mockMvc.perform(post("/api/admin/init-database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currenciesAdded").exists());
    }

    @Test
    @DisplayName("POST /api/admin/init-database - Should return rates added count")
    void initializeDatabase_ShouldReturnRatesAddedCount() throws Exception {
        Map<String, Object> initResult = new HashMap<>();
        initResult.put("message", "Database initialized successfully");
        initResult.put("currenciesAdded", 10);
        initResult.put("ratesAdded", 30);
        initResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.initializeDatabase()).thenReturn(initResult);

        mockMvc.perform(post("/api/admin/init-database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ratesAdded").value(30));
    }

    @Test
    @DisplayName("POST /api/admin/init-database - Should include timestamp")
    void initializeDatabase_ShouldIncludeTimestamp() throws Exception {
        Map<String, Object> initResult = new HashMap<>();
        initResult.put("message", "Database initialized successfully");
        initResult.put("currenciesAdded", 10);
        initResult.put("ratesAdded", 45);
        initResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.initializeDatabase()).thenReturn(initResult);

        mockMvc.perform(post("/api/admin/init-database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /api/admin/sync-exchange-rates - Should sync exchange rates")
    void syncExchangeRates_ShouldReturn200() throws Exception {
        Map<String, Object> syncResult = new HashMap<>();
        syncResult.put("message", "Exchange rates synced successfully");
        syncResult.put("ratesUpdated", 45);
        syncResult.put("ratesCreated", 0);
        syncResult.put("ratesFailed", 0);
        syncResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.syncExchangeRatesFromApi()).thenReturn(syncResult);

        mockMvc.perform(post("/api/admin/sync-exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Exchange rates synced successfully"))
                .andExpect(jsonPath("$.data.ratesUpdated").value(45));
    }

    @Test
    @DisplayName("POST /api/admin/sync-exchange-rates - Should return sync message")
    void syncExchangeRates_ShouldReturnSuccessMessage() throws Exception {
        Map<String, Object> syncResult = new HashMap<>();
        syncResult.put("message", "Exchange rates synced successfully");
        syncResult.put("ratesUpdated", 30);
        syncResult.put("ratesCreated", 5);
        syncResult.put("ratesFailed", 0);
        syncResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.syncExchangeRatesFromApi()).thenReturn(syncResult);

        mockMvc.perform(post("/api/admin/sync-exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/admin/sync-exchange-rates - Should return rates updated count")
    void syncExchangeRates_ShouldReturnRatesUpdatedCount() throws Exception {
        Map<String, Object> syncResult = new HashMap<>();
        syncResult.put("message", "Exchange rates synced successfully");
        syncResult.put("ratesUpdated", 25);
        syncResult.put("ratesCreated", 10);
        syncResult.put("ratesFailed", 0);
        syncResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.syncExchangeRatesFromApi()).thenReturn(syncResult);

        mockMvc.perform(post("/api/admin/sync-exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ratesUpdated").value(25));
    }

    @Test
    @DisplayName("POST /api/admin/sync-exchange-rates - Should return rates created count")
    void syncExchangeRates_ShouldReturnRatesCreatedCount() throws Exception {
        Map<String, Object> syncResult = new HashMap<>();
        syncResult.put("message", "Exchange rates synced successfully");
        syncResult.put("ratesUpdated", 30);
        syncResult.put("ratesCreated", 10);
        syncResult.put("ratesFailed", 0);
        syncResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.syncExchangeRatesFromApi()).thenReturn(syncResult);

        mockMvc.perform(post("/api/admin/sync-exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ratesCreated").value(10));
    }

    @Test
    @DisplayName("POST /api/admin/sync-exchange-rates - Should return rates failed count")
    void syncExchangeRates_ShouldReturnRatesFailedCount() throws Exception {
        Map<String, Object> syncResult = new HashMap<>();
        syncResult.put("message", "Exchange rates synced successfully");
        syncResult.put("ratesUpdated", 40);
        syncResult.put("ratesCreated", 0);
        syncResult.put("ratesFailed", 5);
        syncResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.syncExchangeRatesFromApi()).thenReturn(syncResult);

        mockMvc.perform(post("/api/admin/sync-exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ratesFailed").value(5));
    }

    @Test
    @DisplayName("POST /api/admin/sync-exchange-rates - Should include timestamp")
    void syncExchangeRates_ShouldIncludeTimestamp() throws Exception {
        Map<String, Object> syncResult = new HashMap<>();
        syncResult.put("message", "Exchange rates synced successfully");
        syncResult.put("ratesUpdated", 45);
        syncResult.put("ratesCreated", 0);
        syncResult.put("ratesFailed", 0);
        syncResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.syncExchangeRatesFromApi()).thenReturn(syncResult);

        mockMvc.perform(post("/api/admin/sync-exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /api/admin/init-database - Should handle zero currencies")
    void initializeDatabase_ShouldHandleZeroCurrencies() throws Exception {
        Map<String, Object> initResult = new HashMap<>();
        initResult.put("message", "Database initialized successfully");
        initResult.put("currenciesAdded", 0);
        initResult.put("ratesAdded", 0);
        initResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.initializeDatabase()).thenReturn(initResult);

        mockMvc.perform(post("/api/admin/init-database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currenciesAdded").value(0));
    }

    @Test
    @DisplayName("POST /api/admin/sync-exchange-rates - Should handle zero updates")
    void syncExchangeRates_ShouldHandleZeroUpdates() throws Exception {
        Map<String, Object> syncResult = new HashMap<>();
        syncResult.put("message", "Exchange rates synced successfully");
        syncResult.put("ratesUpdated", 0);
        syncResult.put("ratesCreated", 0);
        syncResult.put("ratesFailed", 0);
        syncResult.put("timestamp", System.currentTimeMillis());

        when(dataInitializationService.syncExchangeRatesFromApi()).thenReturn(syncResult);

        mockMvc.perform(post("/api/admin/sync-exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ratesUpdated").value(0));
    }
}
