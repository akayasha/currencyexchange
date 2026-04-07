package com.example.currencyexchange.repository;

import com.example.currencyexchange.dto.ExchangeRateWithCurrencyDto;
import com.example.currencyexchange.model.ExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ExchangeRate entities.
 * Contains a JPQL JOIN query between exchange_rate and currency tables.
 */
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    Optional<ExchangeRate> findByFromCurrencyCodeAndToCurrencyCode(String fromCode, String toCode);

    boolean existsByFromCurrencyCodeAndToCurrencyCode(String fromCode, String toCode);

    List<ExchangeRate> findByFromCurrencyCode(String fromCode);

    /**
     * JOIN query: Fetches exchange rates enriched with full currency details
     * by joining the exchange_rate table with the currency table twice
     * (once for the source currency, once for the target currency).
     */
    @Query("SELECT new com.example.currencyexchange.dto.ExchangeRateWithCurrencyDto(" +
            "  er.uuid, " +
            "  er.fromCurrencyCode, fc.name, fc.symbol, " +
            "  er.toCurrencyCode, tc.name, tc.symbol, " +
            "  er.rate, er.lastUpdated" +
            ") " +
            "FROM ExchangeRate er " +
            "JOIN Currency fc ON er.fromCurrencyCode = fc.code " +
            "JOIN Currency tc ON er.toCurrencyCode = tc.code " +
            "WHERE (:fromCode IS NULL OR er.fromCurrencyCode = :fromCode) " +
            "AND (:toCode IS NULL OR er.toCurrencyCode = :toCode)")
    List<ExchangeRateWithCurrencyDto> findRatesWithCurrencyDetails(
            @Param("fromCode") String fromCode,
            @Param("toCode") String toCode);

    /**
     * Paginated search across exchange rates by currency codes.
     */
    @Query("SELECT er FROM ExchangeRate er " +
            "WHERE (:keyword IS NULL OR LOWER(er.fromCurrencyCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "OR (:keyword IS NULL OR LOWER(er.toCurrencyCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ExchangeRate> searchExchangeRates(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT MIN(e.rate), MAX(e.rate), AVG(e.rate) FROM ExchangeRate e")
    List<Object[]> getRateStatistics();

    @Query("SELECT e.fromCurrencyCode, COUNT(e) FROM ExchangeRate e GROUP BY e.fromCurrencyCode")
    List<Object[]> countByFromCurrencyCode();

    @Query("SELECT e.toCurrencyCode, COUNT(e) FROM ExchangeRate e GROUP BY e.toCurrencyCode")
    List<Object[]> countByToCurrencyCode();
}
