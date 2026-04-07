package com.example.currencyexchange.controller;

import com.example.currencyexchange.dto.CurrencyDto;
import com.example.currencyexchange.exception.GlobalExceptionHandler;
import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.service.CurrencyService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyController Unit Tests")
class CurrencyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyController currencyController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CurrencyDto usdDto;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        usdDto = CurrencyDto.builder()
                .uuid(testUuid)
                .code("USD")
                .name("US Dollar")
                .symbol("$")
                .region("North America")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/currencies - Should return paginated list")
    void getAllCurrencies_ShouldReturn200() throws Exception {
        Page<CurrencyDto> page = new PageImpl<>(List.of(usdDto), PageRequest.of(0, 10), 1);
        when(currencyService.getAllCurrencies(any())).thenReturn(page);

        mockMvc.perform(get("/api/currencies"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("USD"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/currencies/{id} - Should return currency")
    void getCurrencyById_WhenExists_ShouldReturn200() throws Exception {
        when(currencyService.getCurrencyById(testUuid)).thenReturn(usdDto);

        mockMvc.perform(get("/api/currencies/" + testUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("USD"))
                .andExpect(jsonPath("$.data.name").value("US Dollar"));
    }

    @Test
    @DisplayName("GET /api/currencies/{id} - Should return 404 when not found")
    void getCurrencyById_WhenNotExists_ShouldReturn404() throws Exception {
        UUID notFoundUuid = UUID.randomUUID();
        when(currencyService.getCurrencyById(notFoundUuid))
                .thenThrow(new ResourceNotFoundException("Currency", "id", notFoundUuid));

        mockMvc.perform(get("/api/currencies/" + notFoundUuid))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/currencies/search - Should return search results")
    void searchCurrencies_ShouldReturnResults() throws Exception {
        Page<CurrencyDto> page = new PageImpl<>(List.of(usdDto), PageRequest.of(0, 10), 1);
        when(currencyService.searchCurrencies(eq("dollar"), any())).thenReturn(page);

        mockMvc.perform(get("/api/currencies/search").param("keyword", "dollar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("US Dollar"));
    }

    @Test
    @DisplayName("POST /api/currencies - Should create currency and return 201")
    void createCurrency_ShouldReturn201() throws Exception {
        when(currencyService.createCurrency(any(CurrencyDto.class))).thenReturn(usdDto);

        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usdDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("USD"));
    }

    @Test
    @DisplayName("POST /api/currencies - Should return 400 on invalid input")
    void createCurrency_WithInvalidInput_ShouldReturn400() throws Exception {
        CurrencyDto invalid = CurrencyDto.builder().build(); // Missing required fields

        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/currencies/{id} - Should update and return 200")
    void updateCurrency_ShouldReturn200() throws Exception {
        when(currencyService.updateCurrency(eq(testUuid), any(CurrencyDto.class))).thenReturn(usdDto);

        mockMvc.perform(put("/api/currencies/" + testUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usdDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("USD"));
    }

    @Test
    @DisplayName("DELETE /api/currencies/{id} - Should return 204")
    void deleteCurrency_ShouldReturn204() throws Exception {
        doNothing().when(currencyService).deleteCurrency(testUuid);

        mockMvc.perform(delete("/api/currencies/" + testUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Currency deleted successfully"));

        verify(currencyService).deleteCurrency(testUuid);
    }
}
