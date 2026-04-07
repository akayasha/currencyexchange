package com.example.currencyexchange.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the history of exchange rate changes.
 * Tracks when rates were updated and what the old values were.
 */
@Entity
@Table(name = "exchange_rate_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateHistory {

    @Id
    @UuidGenerator
    private UUID uuid;

    @Column(name = "from_currency_code", nullable = false, length = 3)
    private String fromCurrencyCode;

    @Column(name = "to_currency_code", nullable = false, length = 3)
    private String toCurrencyCode;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal oldRate;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal newRate;

    @Column(nullable = false, length = 50)
    private String changeReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * JOIN relationship: ExchangeRateHistory -> ExchangeRate
     * Join via currency pair codes
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "from_currency_code", referencedColumnName = "from_currency_code", insertable = false, updatable = false),
            @JoinColumn(name = "to_currency_code", referencedColumnName = "to_currency_code", insertable = false, updatable = false)
    })
    private ExchangeRate exchangeRate;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.createdBy == null) {
            this.createdBy = "SYSTEM";
        }
    }
}

