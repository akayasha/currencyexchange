package com.example.currencyexchange.repository;

import com.example.currencyexchange.model.ExchangeRateHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ExchangeRateHistory entities.
 * Includes JOIN queries with ExchangeRate table.
 */
@Repository
public interface ExchangeRateHistoryRepository extends JpaRepository<ExchangeRateHistory, UUID> {

    /**
     * Find all history records for a specific currency pair.
     */
    List<ExchangeRateHistory> findByFromCurrencyCodeAndToCurrencyCode(
            String fromCode, String toCode);

    /**
     * Find history paginated for a currency pair.
     */
    Page<ExchangeRateHistory> findByFromCurrencyCodeAndToCurrencyCodeOrderByCreatedAtDesc(
            String fromCode, String toCode, Pageable pageable);

    /**
     * Find all history records for a currency pair with pagination.
     */
    Page<ExchangeRateHistory> findByFromCurrencyCodeOrderByCreatedAtDesc(
            String fromCode, Pageable pageable);

    /**
     * JOIN query: Get history with current exchange rate details.
     * This demonstrates a JOIN across two tables (exchange_rate_history + exchange_rate).
     */
    @Query("SELECT h FROM ExchangeRateHistory h " +
            "LEFT JOIN h.exchangeRate er " +
            "WHERE h.fromCurrencyCode = :fromCode AND h.toCurrencyCode = :toCode " +
            "ORDER BY h.createdAt DESC")
    List<ExchangeRateHistory> findHistoryWithExchangeRateDetails(
            @Param("fromCode") String fromCode,
            @Param("toCode") String toCode);

    /**
     * Count total history records for a currency pair.
     */
    long countByFromCurrencyCodeAndToCurrencyCode(String fromCode, String toCode);

    /**
     * Find the most recent history record for a currency pair.
     */
    @Query("SELECT h FROM ExchangeRateHistory h " +
            "WHERE h.fromCurrencyCode = :fromCode AND h.toCurrencyCode = :toCode " +
            "ORDER BY h.createdAt DESC LIMIT 1")
    ExchangeRateHistory findMostRecentHistory(
            @Param("fromCode") String fromCode,
            @Param("toCode") String toCode);
}

