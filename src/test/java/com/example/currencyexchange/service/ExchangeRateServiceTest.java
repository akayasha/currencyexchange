package com.example.currencyexchange.service;

import com.example.currencyexchange.dto.ExchangeRateDto;
import com.example.currencyexchange.dto.ExchangeRateHistoryDto;
import com.example.currencyexchange.dto.ExchangeRateWithCurrencyDto;
import com.example.currencyexchange.exception.DuplicateResourceException;
import com.example.currencyexchange.exception.InactiveCurrencyException;
import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.model.Currency;
import com.example.currencyexchange.model.ExchangeRate;
import com.example.currencyexchange.repository.CurrencyRepository;
import com.example.currencyexchange.repository.ExchangeRateRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ExchangeRateService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateService Tests")
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExchangeRateHistoryService historyService;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private ExchangeRate usdToEur;
    private ExchangeRateDto usdToEurDto;
    private Currency usdCurrency;
    private Currency eurCurrency;
    private UUID uuid;
    private ExchangeRateHistoryDto historyDto;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        usdToEur = ExchangeRate.builder()
                .uuid(uuid)
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9250"))
                .lastUpdated(now)
                .build();

        usdToEurDto = ExchangeRateDto.builder()
                .uuid(uuid)
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .rate(new BigDecimal("0.9250"))
                .lastUpdated(now)
                .build();

        usdCurrency = Currency.builder()
                .uuid(UUID.randomUUID())
                .code("USD")
                .name("US Dollar")
                .isActive(true)
                .build();

        eurCurrency = Currency.builder()
                .uuid(UUID.randomUUID())
                .code("EUR")
                .name("Euro")
                .isActive(true)
                .build();

        historyDto = ExchangeRateHistoryDto.builder()
                .fromCurrencyCode("USD")
                .toCurrencyCode("EUR")
                .oldRate(new BigDecimal("0.9250"))
                .newRate(new BigDecimal("0.9250"))
                .changeReason("MANUAL_CREATE")
                .build();
    }

    @Test
    @DisplayName("Should get all rates with pagination")
    void shouldGetAllRatesWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeRate> page = new PageImpl<>(List.of(usdToEur));
        when(exchangeRateRepository.findAll(pageable)).thenReturn(page);

        Page<ExchangeRateDto> result = exchangeRateService.getAllRates(pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(exchangeRateRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get rate by id")
    void shouldGetRateById() {
        when(exchangeRateRepository.findById(uuid)).thenReturn(Optional.of(usdToEur));

        ExchangeRateDto result = exchangeRateService.getRateById(uuid);

        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getFromCurrencyCode()).isEqualTo("USD");
        verify(exchangeRateRepository).findById(uuid);
    }

    @Test
    @DisplayName("Should throw exception when rate not found by id")
    void shouldThrowException_WhenRateNotFoundById() {
        when(exchangeRateRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.getRateById(uuid))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get rate by currency pair")
    void shouldGetRateByCurrencyPair() {
        when(exchangeRateRepository.findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR"))
                .thenReturn(Optional.of(usdToEur));

        ExchangeRateDto result = exchangeRateService.getRateByPair("usd", "eur");

        assertThat(result.getFromCurrencyCode()).isEqualTo("USD");
        assertThat(result.getToCurrencyCode()).isEqualTo("EUR");
        verify(exchangeRateRepository).findByFromCurrencyCodeAndToCurrencyCode("USD", "EUR");
    }

    @Test
    @DisplayName("Should throw exception when currency pair not found")
    void shouldThrowException_WhenCurrencyPairNotFound() {
        when(exchangeRateRepository.findByFromCurrencyCodeAndToCurrencyCode("EUR", "USD"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.getRateByPair("EUR", "USD"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get rates with currency details")
    void shouldGetRatesWithCurrencyDetails() {
        ExchangeRateWithCurrencyDto withCurrencyDetails = ExchangeRateWithCurrencyDto.builder()
                .uuid(uuid)
                .fromCurrencyCode("USD")
                .fromCurrencyName("United States Dollar")
                .fromCurrencySymbol("$")
                .toCurrencyCode("EUR")
                .toCurrencyName("Euro")
                .toCurrencySymbol("€")
                .rate(new BigDecimal("0.9250"))
                .lastUpdated(LocalDateTime.now())
                .build();

        when(exchangeRateRepository.findRatesWithCurrencyDetails("USD", "EUR"))
                .thenReturn(List.of(withCurrencyDetails));

        List<ExchangeRateWithCurrencyDto> result = exchangeRateService.getRatesWithCurrencyDetails("USD", "EUR");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFromCurrencyName()).isEqualTo("United States Dollar");
    }

    @Test
    @DisplayName("Should search rates by keyword")
    void shouldSearchRatesByKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeRate> page = new PageImpl<>(List.of(usdToEur));
        when(exchangeRateRepository.searchExchangeRates("USD", pageable)).thenReturn(page);

        Page<ExchangeRateDto> result = exchangeRateService.searchRates("USD", pageable);

        assertThat(result).isNotEmpty();
        verify(exchangeRateRepository).searchExchangeRates("USD", pageable);
    }

    @Test
    @DisplayName("Should create exchange rate")
    void shouldCreateExchangeRate() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(exchangeRateRepository.existsByFromCurrencyCodeAndToCurrencyCode("USD", "EUR"))
                .thenReturn(false);
        when(exchangeRateRepository.save(any())).thenReturn(usdToEur);
        when(historyService.recordRateChange(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(historyDto);

        ExchangeRateDto result = exchangeRateService.createRate(usdToEurDto);

        assertThat(result.getFromCurrencyCode()).isEqualTo("USD");
        verify(currencyRepository, times(2)).findByCode(anyString());
        verify(exchangeRateRepository).existsByFromCurrencyCodeAndToCurrencyCode("USD", "EUR");
        verify(exchangeRateRepository).save(any());
        verify(historyService).recordRateChange("USD", "EUR", usdToEur.getRate(), usdToEur.getRate(), "MANUAL_CREATE");
    }

    @Test
    @DisplayName("Should throw exception when from currency not found on create")
    void shouldThrowException_WhenFromCurrencyNotFoundOnCreate() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.createRate(usdToEurDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when to currency not found on create")
    void shouldThrowException_WhenToCurrencyNotFoundOnCreate() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.createRate(usdToEurDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when duplicate rate pair on create")
    void shouldThrowException_WhenDuplicateRatePairOnCreate() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eurCurrency));
        when(exchangeRateRepository.existsByFromCurrencyCodeAndToCurrencyCode("USD", "EUR"))
                .thenReturn(true);

        assertThatThrownBy(() -> exchangeRateService.createRate(usdToEurDto))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should reject creation when currency inactive")
    void shouldRejectCreateWhenCurrencyInactive() {
        Currency inactiveEur = Currency.builder()
                .uuid(UUID.randomUUID())
                .code("EUR")
                .name("Euro")
                .isActive(false)
                .build();

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(inactiveEur));

        assertThatThrownBy(() -> exchangeRateService.createRate(usdToEurDto))
                .isInstanceOf(InactiveCurrencyException.class);
    }

    @Test
    @DisplayName("Should update exchange rate")
    void shouldUpdateExchangeRate() {
        ExchangeRateDto updateDto = ExchangeRateDto.builder()
                .rate(new BigDecimal("0.9500"))
                .build();

        when(exchangeRateRepository.findById(uuid)).thenReturn(Optional.of(usdToEur));
        when(exchangeRateRepository.save(any())).thenReturn(usdToEur);
        when(historyService.recordRateChange(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(historyDto);

        ExchangeRateDto result = exchangeRateService.updateRate(uuid, updateDto);

        assertThat(result).isNotNull();
        verify(exchangeRateRepository).findById(uuid);
        verify(exchangeRateRepository).save(any());
        verify(historyService).recordRateChange("USD", "EUR", new BigDecimal("0.9250"), new BigDecimal("0.9500"), "MANUAL_UPDATE");
    }

    @Test
    @DisplayName("Should throw exception when rate not found on update")
    void shouldThrowException_WhenRateNotFoundOnUpdate() {
        when(exchangeRateRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.updateRate(uuid, usdToEurDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete exchange rate")
    void shouldDeleteExchangeRate() {
        when(exchangeRateRepository.existsById(uuid)).thenReturn(true);

        exchangeRateService.deleteRate(uuid);

        verify(exchangeRateRepository).existsById(uuid);
        verify(exchangeRateRepository).deleteById(uuid);
    }

    @Test
    @DisplayName("Should throw exception when rate not found on delete")
    void shouldThrowException_WhenRateNotFoundOnDelete() {
        when(exchangeRateRepository.existsById(uuid)).thenReturn(false);

        assertThatThrownBy(() -> exchangeRateService.deleteRate(uuid))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get rates for base currency")
    void shouldGetRatesForBaseCurrency() {
        when(exchangeRateRepository.findByFromCurrencyCode("USD")).thenReturn(List.of(usdToEur));

        List<ExchangeRateDto> result = exchangeRateService.getRatesForBaseCurrency("USD");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFromCurrencyCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should get exchange rate analytics")
    void shouldGetExchangeRateAnalytics() {
        when(exchangeRateRepository.count()).thenReturn(5L);
        List<Object[]> rateStats = new ArrayList<>();
        rateStats.add(new Object[]{new BigDecimal("0.8"), new BigDecimal("1.2"), new BigDecimal("1.0")});
        when(exchangeRateRepository.getRateStatistics()).thenReturn(rateStats);

        List<Object[]> fromStats = new ArrayList<>();
        fromStats.add(new Object[]{"USD", 3L});
        fromStats.add(new Object[]{"EUR", 2L});
        when(exchangeRateRepository.countByFromCurrencyCode()).thenReturn(fromStats);

        List<Object[]> toStats = new ArrayList<>();
        toStats.add(new Object[]{"EUR", 2L});
        toStats.add(new Object[]{"GBP", 1L});
        when(exchangeRateRepository.countByToCurrencyCode()).thenReturn(toStats);

        Map<String, Object> result = exchangeRateService.getExchangeRateAnalytics();

        assertThat(result).containsKeys("totalRates", "rateStatistics", "ratesByBaseCurrency", "ratesByTargetCurrency", "timestamp");
        assertThat(result.get("totalRates")).isEqualTo(5L);
    }
}
