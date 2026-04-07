package com.example.currencyexchange.service;

import com.example.currencyexchange.dto.CurrencyDto;
import com.example.currencyexchange.exception.DuplicateResourceException;
import com.example.currencyexchange.exception.ResourceNotFoundException;
import com.example.currencyexchange.model.Currency;
import com.example.currencyexchange.repository.CurrencyRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyService Unit Tests")
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    private Currency usd;
    private CurrencyDto usdDto;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        usd = Currency.builder()
                .uuid(testUuid)
                .code("USD")
                .name("US Dollar")
                .symbol("$")
                .region("North America")
                .isActive(true)
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
    @DisplayName("Should return all currencies with pagination")
    void getAllCurrencies_ShouldReturnPagedResult() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Currency> page = new PageImpl<>(List.of(usd), pageable, 1);
        when(currencyRepository.findAll(pageable)).thenReturn(page);

        Page<CurrencyDto> result = currencyService.getAllCurrencies(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("USD");
        verify(currencyRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return currency by ID")
    void getCurrencyById_WhenExists_ShouldReturnDto() {
        when(currencyRepository.findById(testUuid)).thenReturn(Optional.of(usd));

        CurrencyDto result = currencyService.getCurrencyById(testUuid);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("USD");
        assertThat(result.getName()).isEqualTo("US Dollar");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when currency not found by ID")
    void getCurrencyById_WhenNotExists_ShouldThrow() {
        UUID notFoundUuid = UUID.randomUUID();
        when(currencyRepository.findById(notFoundUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.getCurrencyById(notFoundUuid))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Currency");
    }

    @Test
    @DisplayName("Should return currency by code")
    void getCurrencyByCode_WhenExists_ShouldReturnDto() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usd));

        CurrencyDto result = currencyService.getCurrencyByCode("USD");

        assertThat(result.getCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should return paginated search results")
    void searchCurrencies_ShouldReturnMatchingResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Currency> page = new PageImpl<>(List.of(usd), pageable, 1);
        when(currencyRepository.searchCurrencies("dollar", pageable)).thenReturn(page);

        Page<CurrencyDto> result = currencyService.searchCurrencies("dollar", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("US Dollar");
    }

    @Test
    @DisplayName("Should create a new currency")
    void createCurrency_WhenNotExists_ShouldReturnCreated() {
        when(currencyRepository.existsByCode("USD")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(usd);

        CurrencyDto result = currencyService.createCurrency(usdDto);

        assertThat(result.getCode()).isEqualTo("USD");
        verify(currencyRepository).save(any(Currency.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when currency code already exists")
    void createCurrency_WhenCodeExists_ShouldThrow() {
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        assertThatThrownBy(() -> currencyService.createCurrency(usdDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("USD");
    }

    @Test
    @DisplayName("Should update currency successfully")
    void updateCurrency_WhenExists_ShouldReturnUpdated() {
        CurrencyDto updateDto = CurrencyDto.builder()
                .uuid(testUuid)
                .name("United States Dollar")
                .symbol("$")
                .region("Americas")
                .isActive(true)
                .build();
        when(currencyRepository.findById(testUuid)).thenReturn(Optional.of(usd));
        when(currencyRepository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));

        CurrencyDto result = currencyService.updateCurrency(testUuid, updateDto);

        assertThat(result.getName()).isEqualTo("United States Dollar");
        assertThat(result.getRegion()).isEqualTo("Americas");
    }

    @Test
    @DisplayName("Should patch currency active status")
    void patchCurrencyStatus_ShouldUpdateStatus() {
        when(currencyRepository.findById(testUuid)).thenReturn(Optional.of(usd));
        when(currencyRepository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));

        CurrencyDto result = currencyService.patchCurrencyStatus(testUuid, false);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should delete currency by ID")
    void deleteCurrency_WhenExists_ShouldDelete() {
        when(currencyRepository.existsById(testUuid)).thenReturn(true);

        currencyService.deleteCurrency(testUuid);

        verify(currencyRepository).deleteById(testUuid);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent currency")
    void deleteCurrency_WhenNotExists_ShouldThrow() {
        UUID notFoundUuid = UUID.randomUUID();
        when(currencyRepository.existsById(notFoundUuid)).thenReturn(false);

        assertThatThrownBy(() -> currencyService.deleteCurrency(notFoundUuid))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
