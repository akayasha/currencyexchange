package com.example.currencyexchange.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the history of currency conversions performed.
 * Tracks each conversion request and its result.
 */
@Entity
@Table(name = "conversion_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversionHistory {

    @Id
    @UuidGenerator
    private UUID uuid;

    @Column(name = "from_currency_code", nullable = false, length = 3)
    private String fromCurrencyCode;

    @Column(name = "to_currency_code", nullable = false, length = 3)
    private String toCurrencyCode;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal convertedAmount;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal rateApplied;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 20)
    private String source; // "db" or "external-api"

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
