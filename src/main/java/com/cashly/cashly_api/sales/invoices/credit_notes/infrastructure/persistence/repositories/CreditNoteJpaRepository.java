package com.cashly.cashly_api.sales.invoices.credit_notes.infrastructure.persistence.repositories;

import com.cashly.cashly_api.sales.invoices.credit_notes.infrastructure.persistence.entities.CreditNoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CreditNoteJpaRepository
        extends
            JpaRepository<CreditNoteEntity, UUID> {

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
            @Param("customerId") UUID customerId);

    @Query(value = """
            SELECT c FROM CreditNoteEntity c
            WHERE (:dateFrom IS NULL OR c.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR c.createdAt <= :dateTo)
            AND (:customerId IS NULL OR c.customerId = :customerId)
            AND (:search IS NULL OR :search = '' OR LOWER(c.noteNumber) LIKE :search
                OR LOWER(c.reason) LIKE :search
                OR EXISTS (SELECT 1 FROM InvoiceEntity i WHERE i.id = c.invoiceId
                    AND (LOWER(i.invoiceNumber) LIKE :search
                        OR LOWER(i.customerName) LIKE :search
                        OR LOWER(i.customerRif) LIKE :search)))
            """, countQuery = """
            SELECT COUNT(c) FROM CreditNoteEntity c
            WHERE (:dateFrom IS NULL OR c.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR c.createdAt <= :dateTo)
            AND (:customerId IS NULL OR c.customerId = :customerId)
            AND (:search IS NULL OR :search = '' OR LOWER(c.noteNumber) LIKE :search
                OR LOWER(c.reason) LIKE :search
                OR EXISTS (SELECT 1 FROM InvoiceEntity i WHERE i.id = c.invoiceId
                    AND (LOWER(i.invoiceNumber) LIKE :search
                        OR LOWER(i.customerName) LIKE :search
                        OR LOWER(i.customerRif) LIKE :search)))
            """)
    Page<CreditNoteEntity> findPageWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("search") String search, Pageable pageable);

    List<CreditNoteEntity> findByInvoiceId(UUID invoiceId);

    @Query("SELECT c.invoiceId, SUM(c.amount) FROM CreditNoteEntity c WHERE c.invoiceId IN :invoiceIds GROUP BY c.invoiceId")
    List<Object[]> sumByInvoiceIds(@Param("invoiceIds") List<UUID> invoiceIds);
}
