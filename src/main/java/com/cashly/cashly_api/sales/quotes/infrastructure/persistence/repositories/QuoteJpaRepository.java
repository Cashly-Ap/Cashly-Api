package com.cashly.cashly_api.sales.quotes.infrastructure.persistence.repositories;

import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteLineJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteNumberSequenceJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteJpaRepository
        extends
            JpaRepository<QuoteJpaEntity, UUID> {

    default List<QuoteJpaEntity> findAllWithFilters(Instant dateFrom,
            Instant dateTo, UUID customerId, String status,
            UUID productId, UUID categoryId) {
        return findAllWithFilters(dateFrom, dateTo, customerId, status,
                productId, categoryId, java.util.Set.of(new UUID(0, 0)));
    }

    boolean existsByQuoteNumber(String quoteNumber);

    @Query("SELECT DISTINCT q FROM QuoteJpaEntity q LEFT JOIN FETCH q.lines l LEFT JOIN FETCH l.categorySnapshot WHERE q.id = :id")
    Optional<QuoteJpaEntity> findByIdWithLines(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT q FROM QuoteJpaEntity q
            LEFT JOIN FETCH q.lines l LEFT JOIN FETCH l.categorySnapshot
            WHERE q.id = :id
            AND q.status = 'DRAFT'
            AND NOT EXISTS (
                SELECT 1 FROM InvoiceEntity invoice
                JOIN invoice.quoteIds linkedQuoteId
                WHERE linkedQuoteId = q.id
            )
            """)
    Optional<QuoteJpaEntity> findEligibleForDeletion(@Param("id") UUID id);

    @Query("SELECT DISTINCT q FROM QuoteJpaEntity q LEFT JOIN FETCH q.lines l LEFT JOIN FETCH l.categorySnapshot")
    List<QuoteJpaEntity> findAllWithLines();

    @Query("SELECT COALESCE(MAX(q.nextValue), 1) FROM QuoteNumberSequenceJpaEntity q WHERE q.year = :year")
    int findNextSequenceValueForYear(@Param("year") int year);

    @Query("""
            SELECT DISTINCT q FROM QuoteJpaEntity q
            LEFT JOIN FETCH q.lines l LEFT JOIN FETCH l.categorySnapshot
            WHERE (:dateFrom IS NULL OR q.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR q.createdAt <= :dateTo)
            AND (:customerId IS NULL OR q.customerId = :customerId)
            AND (:status = 'ALL' OR q.status = :status OR (:status IS NULL AND q.status = 'DRAFT'))
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM QuoteLineJpaEntity ql WHERE ql.quote = q AND ql.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM QuoteLineJpaEntity ql2 LEFT JOIN ql2.categorySnapshot categorySnapshot
                WHERE ql2.quote = q AND
                (categorySnapshot.categoryId = :categoryId OR ql2.productId IN :currentCategoryProductIds)
            ))
            """)
    List<QuoteJpaEntity> findAllWithFilters(
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("customerId") UUID customerId,
            @Param("status") String status, @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId,
            @Param("currentCategoryProductIds") java.util.Set<UUID> currentCategoryProductIds);

    @Query("""
            SELECT DISTINCT q FROM QuoteJpaEntity q
            LEFT JOIN FETCH q.lines l LEFT JOIN FETCH l.categorySnapshot
            WHERE q.status = 'DRAFT'
            AND (:dateFrom IS NULL OR q.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR q.createdAt <= :dateTo)
            AND (:customerId IS NULL OR q.customerId = :customerId)
            AND (:status = 'ALL' OR :status IS NULL OR q.status = :status)
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM QuoteLineJpaEntity ql WHERE ql.quote = q AND ql.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM QuoteLineJpaEntity ql2 LEFT JOIN ql2.categorySnapshot categorySnapshot
                WHERE ql2.quote = q AND
                (categorySnapshot.categoryId = :categoryId OR ql2.productId IN :currentCategoryProductIds)
            ))
            """)
    List<QuoteJpaEntity> findAllEligibleForDeletion(
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("customerId") UUID customerId,
            @Param("status") String status,
            @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId,
            @Param("currentCategoryProductIds") java.util.Set<UUID> currentCategoryProductIds);
}
