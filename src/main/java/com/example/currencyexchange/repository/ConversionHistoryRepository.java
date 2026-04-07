package com.example.currencyexchange.repository;

import com.example.currencyexchange.model.ConversionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ConversionHistory entities.
 */
@Repository
public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, UUID> {

    /**
     * Find conversion history for a specific currency pair, ordered by timestamp descending.
     */
    Page<ConversionHistory> findByFromCurrencyCodeAndToCurrencyCodeOrderByTimestampDesc(
            String fromCurrencyCode, String toCurrencyCode, Pageable pageable);

    /**
     * Find conversion history for a specific from currency, ordered by timestamp descending.
     */
    Page<ConversionHistory> findByFromCurrencyCodeOrderByTimestampDesc(
            String fromCurrencyCode, Pageable pageable);

    /**
     * Get statistics about conversions.
     */
    @Query("SELECT COUNT(ch), SUM(ch.amount), AVG(ch.amount) FROM ConversionHistory ch")
    List<Object[]> getConversionStatistics();

    /**
     * Get most used from currency.
     */
    @Query("SELECT ch.fromCurrencyCode, COUNT(ch) FROM ConversionHistory ch GROUP BY ch.fromCurrencyCode ORDER BY COUNT(ch) DESC")
    List<Object[]> getMostUsedFromCurrencies(Pageable pageable);

    /**
     * Get most used to currency.
     */
    @Query("SELECT ch.toCurrencyCode, COUNT(ch) FROM ConversionHistory ch GROUP BY ch.toCurrencyCode ORDER BY COUNT(ch) DESC")
    List<Object[]> getMostUsedToCurrencies(Pageable pageable);
}
