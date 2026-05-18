package com.parsun.parsun_api.invoices.infrastructure.persistence.repositories;

import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, UUID> {

    @Query("""
            SELECT DISTINCT i FROM InvoiceEntity i
            LEFT JOIN FETCH i.lines
            WHERE i.id = :id
            """)
    Optional<InvoiceEntity> findWithLinesById(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT i FROM InvoiceEntity i
            LEFT JOIN FETCH i.lines
            WHERE (:dateFrom IS NULL OR i.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR i.createdAt <= :dateTo)
            AND (:customerId IS NULL OR i.customerId = :customerId)
            AND (:statuses IS NULL OR i.status IN :statuses)
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM InvoiceLineEntity il WHERE il.invoice = i AND il.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM InvoiceLineEntity il2 WHERE il2.invoice = i AND il2.productId IN (
                    SELECT p.id FROM ProductEntity p WHERE p.categoryId = :categoryId
                )
            ))
            """)
    List<InvoiceEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("statuses") List<String> statuses,
            @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId
    );
}
