package com.parsun.parsun_api.quotes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteJpaRepository extends JpaRepository<QuoteJpaEntity, UUID> {

    boolean existsByQuoteNumber(String quoteNumber);

    @Query("SELECT DISTINCT q FROM QuoteJpaEntity q LEFT JOIN FETCH q.lines WHERE q.id = :id")
    Optional<QuoteJpaEntity> findByIdWithLines(@Param("id") UUID id);

    @Query("SELECT DISTINCT q FROM QuoteJpaEntity q LEFT JOIN FETCH q.lines")
    List<QuoteJpaEntity> findAllWithLines();

    @Query("SELECT COALESCE(MAX(q.nextValue), 1) FROM QuoteNumberSequenceJpaEntity q WHERE q.year = :year")
    int findNextSequenceValueForYear(@Param("year") int year);

    @Query("""
            SELECT DISTINCT q FROM QuoteJpaEntity q
            LEFT JOIN FETCH q.lines l
            WHERE (:dateFrom IS NULL OR q.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR q.createdAt <= :dateTo)
            AND (:customerId IS NULL OR q.customerId = :customerId)
            AND (:status = 'ALL' OR q.status = :status OR (:status IS NULL AND q.status IN ('DRAFT', 'CUSTOMER_DRAFT')))
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM QuoteLineJpaEntity ql WHERE ql.quote = q AND ql.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM QuoteLineJpaEntity ql2 WHERE ql2.quote = q AND ql2.productId IN (
                    SELECT p.id FROM ProductEntity p WHERE p.categoryId = :categoryId
                )
            ))
            """)
    List<QuoteJpaEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("status") String status,
            @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId
    );
}
