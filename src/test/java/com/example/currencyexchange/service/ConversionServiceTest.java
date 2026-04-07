package com.example.currencyexchange.service;

import com.example.currencyexchange.dto.ConversionRequestDto;
import com.example.currencyexchange.dto.ConversionResponseDto;
import com.example.currencyexchange.model.ExchangeRate;
import com.example.currencyexchange.model.ConversionHistory;
import com.example.currencyexchange.repository.ExchangeRateRepository;
import com.example.currencyexchange.repository.ConversionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversionService Unit Tests")
class ConversionServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ConversionHistoryRepository conversionHistoryRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ConversionService conversionService;

    private ExchangeRate usdToEur;

    @BeforeEach
    void setUp() {
        usdToEur = ExchangeRate.builder()
                .uuid(UUID.randomUUID())
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9250"))
                .build();

        // Mock the save method for conversion history
        lenient().when(conversionHistoryRepository.save(any(ConversionHistory.class))).thenReturn(null);
    }

    @Test
    @DisplayName("Should convert using DB rate when available")
    void convert_WhenRateInDb_ShouldUseDbRate() {
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("100"))
                .build();
        when(exchangeRateRepository.findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR"))
                .thenReturn(Optional.of(usdToEur));

        ConversionResponseDto result = conversionService.convert(request);

        assertThat(result).isNotNull();
        assertThat(result.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(result.getToCurrencyCode()).isEqualTo("EUR");
        assertThat(result.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("92.5000"));
        assertThat(result.getRateApplied()).isEqualByComparingTo(new BigDecimal("0.9250"));
        assertThat(result.getSource()).isEqualTo("db");
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    @DisplayName("Should use source 'db' label when rate found in database")
    void convert_WhenRateFound_ShouldSetSourceToDb() {
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("200"))
                .build();
        when(exchangeRateRepository.findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR"))
                .thenReturn(Optional.of(usdToEur));

        ConversionResponseDto result = conversionService.convert(request);

        assertThat(result.getSource()).isEqualTo("db");
    }

    @Test
    @DisplayName("Should include timestamp in conversion result")
    void convert_ShouldIncludeTimestamp() {
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .amount(new BigDecimal("50"))
                .build();
        when(exchangeRateRepository.findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR"))
                .thenReturn(Optional.of(usdToEur));

        ConversionResponseDto result = conversionService.convert(request);

        assertThat(result.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should correctly calculate converted amount")
    void convert_ShouldCalculateCorrectly() {
        ExchangeRate jpyRate = ExchangeRate.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("JPY")
                .rate(new BigDecimal("149.50"))
                .build();
        ConversionRequestDto request = ConversionRequestDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("JPY")
                .amount(new BigDecimal("10"))
                .build();
        when(exchangeRateRepository.findByFromCurrencyCodeAndToCurrencyCode("USD", "JPY"))
                .thenReturn(Optional.of(jpyRate));

        ConversionResponseDto result = conversionService.convert(request);

        assertThat(result.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("1495.0000"));
    }
}
