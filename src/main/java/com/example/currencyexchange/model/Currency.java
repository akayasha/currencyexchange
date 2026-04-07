package com.example.currencyexchange.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "currency")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "exchangeRates")
public class Currency {

    @Id
    @UuidGenerator
    private UUID uuid;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Column(unique = true, nullable = false, length = 3)
    private String code;

    @NotBlank(message = "Currency name is required")
    @Column(nullable = false)
    private String name;

    @Column(length = 5)
    private String symbol;

    @Column
    private String region;

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "fromCurrency", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExchangeRate> exchangeRates;


}
