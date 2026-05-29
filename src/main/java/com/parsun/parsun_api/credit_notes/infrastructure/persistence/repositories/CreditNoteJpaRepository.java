package com.parsun.parsun_api.credit_notes.infrastructure.persistence.repositories;

import com.parsun.parsun_api.credit_notes.infrastructure.persistence.entities.CreditNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CreditNoteJpaRepository extends JpaRepository<CreditNoteEntity, UUID> {

    @Query("""
            SELECT c FROM CreditNoteEntity c
            WHERE (:dateFrom IS NULL OR c.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR c.createdAt <= :dateTo)
            AND (:customerId IS NULL OR c.customerId = :customerId)
            ORDER BY c.createdAt DESC
            """)
    List<CreditNoteEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId
    );

    List<CreditNoteEntity> findByInvoiceId(UUID invoiceId);

    @Query("SELECT c.invoiceId, SUM(c.amount) FROM CreditNoteEntity c WHERE c.invoiceId IN :invoiceIds GROUP BY c.invoiceId")
    List<Object[]> sumByInvoiceIds(@Param("invoiceIds") List<UUID> invoiceIds);
}
