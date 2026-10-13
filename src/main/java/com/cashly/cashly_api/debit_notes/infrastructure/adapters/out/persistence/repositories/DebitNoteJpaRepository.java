package com.cashly.cashly_api.debit_notes.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.debit_notes.infrastructure.adapters.out.persistence.entities.DebitNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DebitNoteJpaRepository extends JpaRepository<DebitNoteEntity, UUID> {

    @Query("""
            SELECT d FROM DebitNoteEntity d
            WHERE (:dateFrom IS NULL OR d.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR d.createdAt <= :dateTo)
            AND (:customerId IS NULL OR d.customerId = :customerId)
            ORDER BY d.createdAt DESC
            """)
    List<DebitNoteEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId
    );

    List<DebitNoteEntity> findByInvoiceId(UUID invoiceId);

    @Query("SELECT d.invoiceId, SUM(d.amount) FROM DebitNoteEntity d WHERE d.invoiceId IN :invoiceIds GROUP BY d.invoiceId")
    List<Object[]> sumByInvoiceIds(@Param("invoiceIds") List<UUID> invoiceIds);
}
