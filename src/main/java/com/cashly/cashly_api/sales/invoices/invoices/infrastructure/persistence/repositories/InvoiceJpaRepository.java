package com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.repositories;

import com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.projections.InvoiceHeaderProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceJpaRepository
        extends
            JpaRepository<InvoiceEntity, UUID> {

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
            AND (:returnStatus IS NULL OR i.returnStatus IN :returnStatus)
            AND (:dueStatus IS NULL OR (:dueStatus = 'OVERDUE' AND i.dueDate < CURRENT_DATE AND i.status IN ('PENDING_PAYMENT', 'PARTIALLY_PAID')) OR (:dueStatus = 'CURRENT' AND (i.dueDate IS NULL OR i.dueDate >= CURRENT_DATE) AND i.status IN ('DRAFT', 'PENDING_PAYMENT', 'PARTIALLY_PAID')))
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
            @Param("returnStatus") List<String> returnStatus,
            @Param("dueStatus") String dueStatus,
            @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId);

    default List<InvoiceEntity> findAllWithFilters(LocalDateTime dateFrom,
            LocalDateTime dateTo, UUID customerId, List<String> statuses,
            List<String> returnStatus, UUID productId, UUID categoryId) {
        return findAllWithFilters(dateFrom, dateTo, customerId, statuses,
                returnStatus, null, productId, categoryId);
    }

    @Query(value = """
            SELECT i FROM InvoiceEntity i
            WHERE (:dateFrom IS NULL OR i.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR i.createdAt <= :dateTo)
            AND (:customerId IS NULL OR i.customerId = :customerId)
            AND (:statuses IS NULL OR i.status IN :statuses)
            AND (:returnStatus IS NULL OR i.returnStatus IN :returnStatus)
            AND (:paymentType IS NULL OR i.paymentType = :paymentType)
            AND (:dueStatus IS NULL OR (:dueStatus = 'OVERDUE' AND i.dueDate < CURRENT_DATE AND i.status IN ('PENDING_PAYMENT', 'PARTIALLY_PAID')) OR (:dueStatus = 'CURRENT' AND (i.dueDate IS NULL OR i.dueDate >= CURRENT_DATE) AND i.status IN ('DRAFT', 'PENDING_PAYMENT', 'PARTIALLY_PAID')))
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM InvoiceLineEntity il WHERE il.invoice = i AND il.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM InvoiceLineEntity il2 WHERE il2.invoice = i AND il2.productId IN (
                    SELECT p.id FROM ProductEntity p WHERE p.categoryId = :categoryId
                )
            ))
            AND (:search IS NULL OR :search = '' OR LOWER(i.invoiceNumber) LIKE :search
                OR LOWER(i.customerName) LIKE :search OR LOWER(i.customerRif) LIKE :search)
            """, countQuery = """
            SELECT COUNT(i) FROM InvoiceEntity i
            WHERE (:dateFrom IS NULL OR i.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR i.createdAt <= :dateTo)
            AND (:customerId IS NULL OR i.customerId = :customerId)
            AND (:statuses IS NULL OR i.status IN :statuses)
            AND (:returnStatus IS NULL OR i.returnStatus IN :returnStatus)
            AND (:paymentType IS NULL OR i.paymentType = :paymentType)
            AND (:dueStatus IS NULL OR (:dueStatus = 'OVERDUE' AND i.dueDate < CURRENT_DATE AND i.status IN ('PENDING_PAYMENT', 'PARTIALLY_PAID')) OR (:dueStatus = 'CURRENT' AND (i.dueDate IS NULL OR i.dueDate >= CURRENT_DATE) AND i.status IN ('DRAFT', 'PENDING_PAYMENT', 'PARTIALLY_PAID')))
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM InvoiceLineEntity il WHERE il.invoice = i AND il.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM InvoiceLineEntity il2 WHERE il2.invoice = i AND il2.productId IN (
                    SELECT p.id FROM ProductEntity p WHERE p.categoryId = :categoryId
                )
            ))
            AND (:search IS NULL OR :search = '' OR LOWER(i.invoiceNumber) LIKE :search
                OR LOWER(i.customerName) LIKE :search OR LOWER(i.customerRif) LIKE :search)
            """)
    Page<InvoiceEntity> findPageWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("statuses") List<String> statuses,
            @Param("returnStatus") List<String> returnStatus,
            @Param("dueStatus") String dueStatus,
            @Param("paymentType") String paymentType,
            @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId,
            @Param("search") String search, Pageable pageable);

    @Query("""
            SELECT i.id AS id, i.customerId AS customerId,
                   i.invoiceNumber AS invoiceNumber, i.status AS status,
                   i.subtotal AS subtotal, i.returnedTotal AS returnedTotal,
                   i.dueDate AS dueDate
            FROM InvoiceEntity i
            WHERE i.customerId = :customerId
            ORDER BY i.createdAt DESC
            """)
    List<InvoiceHeaderProjection> findHeadersByCustomer(
            @Param("customerId") UUID customerId);

    @Query("""
            SELECT i.id AS id, i.customerId AS customerId,
                   i.invoiceNumber AS invoiceNumber, i.status AS status,
                   i.subtotal AS subtotal, i.returnedTotal AS returnedTotal,
                   i.dueDate AS dueDate
            FROM InvoiceEntity i
            WHERE i.customerId = :customerId AND i.status IN :statuses
            ORDER BY i.createdAt DESC
            """)
    List<InvoiceHeaderProjection> findHeadersByCustomerAndStatusIn(
            @Param("customerId") UUID customerId,
            @Param("statuses") List<String> statuses);

    @Query("""
            SELECT i.id AS id, i.customerId AS customerId,
                   i.invoiceNumber AS invoiceNumber, i.status AS status,
                   i.subtotal AS subtotal, i.returnedTotal AS returnedTotal,
                   i.dueDate AS dueDate
            FROM InvoiceEntity i
            ORDER BY i.createdAt DESC
            """)
    List<InvoiceHeaderProjection> findAllHeaders();

    @Query("""
            SELECT l.invoice.id, COUNT(l)
            FROM InvoiceLineEntity l
            WHERE l.invoice.id IN :invoiceIds
            GROUP BY l.invoice.id
            """)
    List<Object[]> countLinesByInvoiceIds(
            @Param("invoiceIds") java.util.Collection<UUID> invoiceIds);

    @Query("""
            SELECT i.id, i.invoiceNumber, i.customerName, i.paymentType,
                   i.status, i.subtotal, i.returnedTotal, i.createdAt
            FROM InvoiceEntity i
            WHERE (:dateFrom IS NULL OR i.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR i.createdAt <= :dateTo)
            AND (:customerId IS NULL OR i.customerId = :customerId)
            AND (:statuses IS NULL OR i.status IN :statuses)
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM InvoiceLineEntity il WHERE il.invoice = i
                AND il.productId = :productId))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM InvoiceLineEntity il2 WHERE il2.invoice = i
                AND il2.productId IN (SELECT p.id FROM ProductEntity p
                    WHERE p.categoryId = :categoryId)))
            ORDER BY i.createdAt DESC
            """)
    List<Object[]> findReportRows(@Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("statuses") List<String> statuses,
            @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId);

    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE InvoiceEntity i SET i.status = :status WHERE i.id IN :ids")
    int updateStatuses(@Param("ids") java.util.Collection<UUID> ids,
            @Param("status") String status);

    @Query("""
            SELECT DISTINCT i FROM InvoiceEntity i
            LEFT JOIN FETCH i.lines
            WHERE i.id IN :ids
            """)
    List<InvoiceEntity> findAllWithLinesByIds(
            @Param("ids") Collection<UUID> ids);
}
