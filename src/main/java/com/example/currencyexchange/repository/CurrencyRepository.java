package com.example.currencyexchange.repository;

import com.example.currencyexchange.model.Currency;
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
 * Repository for Currency entities.
 * Includes a paginated search method and a lookup by code.
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, UUID> {

    Optional<Currency> findByCode(String code);

    boolean existsByCode(String code);

    /**
     * Search currencies by code, name, or region with pagination support.
     */
    @Query("SELECT c FROM Currency c WHERE " +
            "(:keyword IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(:keyword IS NULL OR LOWER(c.region) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Currency> searchCurrencies(@Param("keyword") String keyword, Pageable pageable);

    Page<Currency> findByIsActive(boolean isActive, Pageable pageable);

    List<Currency> findByRegion(String region);

    List<Currency> findByIsActive(boolean isActive);

    long countByIsActive(boolean isActive);

    @Query("SELECT c.region, COUNT(c) FROM Currency c GROUP BY c.region")
    List<Object[]> countByRegion();
}
