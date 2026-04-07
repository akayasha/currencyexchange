package com.example.currencyexchange.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an exchange rate between two currencies.
 * The fromCurrencyCode and toCurrencyCode columns create a join
 * relationship with the Currency table.
 */
@Entity
@Table(name = "exchange_rate",
        uniqueConstraints = @UniqueConstraint(columnNames = {"from_currency_code", "to_currency_code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @UuidGenerator
    private UUID uuid;

    @NotBlank(message = "Source currency code is required")
    @Column(name = "from_currency_code", nullable = false, length = 3)
    private String fromCurrencyCode;

    @NotBlank(message = "Target currency code is required")
    @Column(name = "to_currency_code", nullable = false, length = 3)
    private String toCurrencyCode;

    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be positive")
    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal rate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    /**
     * Join relationship: ExchangeRate -> Currency (from side)
     * Mapped via fromCurrencyCode referencing Currency.code
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_currency_code", referencedColumnName = "code",
            insertable = false, updatable = false)
    private Currency fromCurrency;

    /**
     * Join relationship: ExchangeRate -> Currency (to side)
     * Mapped via toCurrencyCode referencing Currency.code
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_currency_code", referencedColumnName = "code",
            insertable = false, updatable = false)
    private Currency toCurrency;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }
}
