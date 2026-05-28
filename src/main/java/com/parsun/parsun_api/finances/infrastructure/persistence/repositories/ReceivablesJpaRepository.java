package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReceivablesJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    @Query(value = """
        SELECT COALESCE(SUM(i.subtotal - COALESCE(pa.total_paid, 0)
            - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)), 0)
        FROM invoices i
        LEFT JOIN (
            SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid
            FROM payment_allocations p_allocs
            GROUP BY p_allocs.invoice_id
        ) pa ON i.id = pa.invoice_id
        LEFT JOIN (
            SELECT cn2.invoice_id, SUM(cn2.amount) AS total_credit
            FROM credit_notes cn2
            GROUP BY cn2.invoice_id
        ) cn ON cn.invoice_id = i.id
        LEFT JOIN (
            SELECT dn2.invoice_id, SUM(dn2.amount) AS total_debit
            FROM debit_notes dn2
            GROUP BY dn2.invoice_id
        ) dn ON dn.invoice_id = i.id
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT')
          AND (i.subtotal - COALESCE(pa.total_paid, 0)
            - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
        """, nativeQuery = true)
    java.math.BigDecimal findTotalDebt();

    @Query(value = """
        SELECT COUNT(DISTINCT i.customer_id)
        FROM invoices i
        LEFT JOIN (
            SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid
            FROM payment_allocations p_allocs
            GROUP BY p_allocs.invoice_id
        ) pa ON i.id = pa.invoice_id
        LEFT JOIN (
            SELECT cn2.invoice_id, SUM(cn2.amount) AS total_credit
            FROM credit_notes cn2
            GROUP BY cn2.invoice_id
        ) cn ON cn.invoice_id = i.id
        LEFT JOIN (
            SELECT dn2.invoice_id, SUM(dn2.amount) AS total_debit
            FROM debit_notes dn2
            GROUP BY dn2.invoice_id
        ) dn ON dn.invoice_id = i.id
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT')
          AND (i.subtotal - COALESCE(pa.total_paid, 0)
            - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
        """, nativeQuery = true)
    long countCustomersWithDebt();

    @Query(value = """
        SELECT COUNT(DISTINCT i.id)
        FROM invoices i
        LEFT JOIN (
            SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid
            FROM payment_allocations p_allocs
            GROUP BY p_allocs.invoice_id
        ) pa ON i.id = pa.invoice_id
        LEFT JOIN (
            SELECT cn2.invoice_id, SUM(cn2.amount) AS total_credit
            FROM credit_notes cn2
            GROUP BY cn2.invoice_id
        ) cn ON cn.invoice_id = i.id
        LEFT JOIN (
            SELECT dn2.invoice_id, SUM(dn2.amount) AS total_debit
            FROM debit_notes dn2
            GROUP BY dn2.invoice_id
        ) dn ON dn.invoice_id = i.id
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT')
          AND (i.subtotal - COALESCE(pa.total_paid, 0)
            - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
        """, nativeQuery = true)
    long countInvoicesWithDebt();

    @Query(value = """
        SELECT COALESCE(SUM(p.amount), 0)
        FROM payments p
        WHERE p.status = 'PENDING_APPROVAL'
        """, nativeQuery = true)
    java.math.BigDecimal findTotalPendingValidationAmount();

    @Query(value = """
        SELECT i.customer_id, c.legal_name AS customerName, c.rif AS customerRif,
               SUM(i.subtotal - COALESCE(pa.total_paid, 0)
                 - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) AS totalDebt,
               COALESCE(pending.pending_amount, 0) AS pendingPaymentsAmount,
               COUNT(DISTINCT i.id) AS pendingInvoicesCount,
               MIN(i.created_at) AS oldestInvoiceDate
        FROM invoices i
        JOIN customers c ON c.id = i.customer_id
        LEFT JOIN (
            SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid
            FROM payment_allocations p_allocs
            GROUP BY p_allocs.invoice_id
        ) pa ON i.id = pa.invoice_id
        LEFT JOIN (
            SELECT cn2.invoice_id, SUM(cn2.amount) AS total_credit
            FROM credit_notes cn2
            GROUP BY cn2.invoice_id
        ) cn ON cn.invoice_id = i.id
        LEFT JOIN (
            SELECT dn2.invoice_id, SUM(dn2.amount) AS total_debit
            FROM debit_notes dn2
            GROUP BY dn2.invoice_id
        ) dn ON dn.invoice_id = i.id
        LEFT JOIN (
            SELECT p.customer_id, SUM(p.amount) AS pending_amount
            FROM payments p
            WHERE p.status IN ('PENDING_APPROVAL', 'DRAFT')
            GROUP BY p.customer_id
        ) pending ON pending.customer_id = i.customer_id
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT')
          AND (i.subtotal - COALESCE(pa.total_paid, 0)
            - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
        GROUP BY i.customer_id, c.legal_name, c.rif, pending.pending_amount
        ORDER BY totalDebt DESC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT i.customer_id)
        FROM invoices i
        LEFT JOIN (
            SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid
            FROM payment_allocations p_allocs
            GROUP BY p_allocs.invoice_id
        ) pa ON i.id = pa.invoice_id
        LEFT JOIN (
            SELECT cn2.invoice_id, SUM(cn2.amount) AS total_credit
            FROM credit_notes cn2
            GROUP BY cn2.invoice_id
        ) cn ON cn.invoice_id = i.id
        LEFT JOIN (
            SELECT dn2.invoice_id, SUM(dn2.amount) AS total_debit
            FROM debit_notes dn2
            GROUP BY dn2.invoice_id
        ) dn ON dn.invoice_id = i.id
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT')
          AND (i.subtotal - COALESCE(pa.total_paid, 0)
            - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
        """,
        nativeQuery = true)
    List<Object[]> findCustomersWithDebtRaw(org.springframework.data.domain.Pageable pageable);

    @Query(value = """
        SELECT COUNT(DISTINCT i.customer_id)
        FROM invoices i
        JOIN customers c ON c.id = i.customer_id
        LEFT JOIN (
            SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid
            FROM payment_allocations p_allocs
            GROUP BY p_allocs.invoice_id
        ) pa ON i.id = pa.invoice_id
        LEFT JOIN (
            SELECT cn2.invoice_id, SUM(cn2.amount) AS total_credit
            FROM credit_notes cn2
            GROUP BY cn2.invoice_id
        ) cn ON cn.invoice_id = i.id
        LEFT JOIN (
            SELECT dn2.invoice_id, SUM(dn2.amount) AS total_debit
            FROM debit_notes dn2
            GROUP BY dn2.invoice_id
        ) dn ON dn.invoice_id = i.id
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT')
          AND (i.subtotal - COALESCE(pa.total_paid, 0)
            - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
          AND (c.legal_name LIKE %:search% OR c.rif LIKE %:search%)
        """, nativeQuery = true)
    long countCustomersWithDebtBySearch(@Param("search") String search);

    @Query(value = """
        SELECT i.customer_id, c.legal_name AS customerName, c.rif AS customerRif,
               SUM(i.subtotal - COALESCE(pa.total_paid, 0)
                 - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) AS totalDebt,
               COALESCE(pending.pending_amount, 0) AS pendingPaymentsAmount,
               COUNT(DISTINCT i.id) AS pendingInvoicesCount,
               MIN(i.created_at) AS oldestInvoiceDate
        FROM invoices i
        JOIN customers c ON c.id = i.customer_id
        LEFT JOIN (
            SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid
            FROM payment_allocations p_allocs
            GROUP BY p_allocs.invoice_id
        ) pa ON i.id = pa.invoice_id
        LEFT JOIN (
            SELECT cn2.invoice_id, SUM(cn2.amount) AS total_credit
            FROM credit_notes cn2
            GROUP BY cn2.invoice_id
        ) cn ON cn.invoice_id = i.id
        LEFT JOIN (
            SELECT dn2.invoice_id, SUM(dn2.amount) AS total_debit
            FROM debit_notes dn2
            GROUP BY dn2.invoice_id
        ) dn ON dn.invoice_id = i.id
        LEFT JOIN (
            SELECT p.customer_id, SUM(p.amount) AS pending_amount
            FROM payments p
            WHERE p.status IN ('PENDING_APPROVAL', 'DRAFT')
            GROUP BY p.customer_id
        ) pending ON pending.customer_id = i.customer_id
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT')
          AND (i.subtotal - COALESCE(pa.total_paid, 0)
            - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
          AND (c.legal_name LIKE %:search% OR c.rif LIKE %:search%)
        GROUP BY i.customer_id, c.legal_name, c.rif, pending.pending_amount
        ORDER BY totalDebt DESC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT i.customer_id)
        FROM invoices i
        JOIN customers c ON c.id = i.customer_id
        LEFT JOIN (
            SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid
            FROM payment_allocations p_allocs
            GROUP BY p_allocs.invoice_id
        ) pa ON i.id = pa.invoice_id
        LEFT JOIN (
            SELECT cn2.invoice_id, SUM(cn2.amount) AS total_credit
            FROM credit_notes cn2
            GROUP BY cn2.invoice_id
        ) cn ON cn.invoice_id = i.id
        LEFT JOIN (
            SELECT dn2.invoice_id, SUM(dn2.amount) AS total_debit
            FROM debit_notes dn2
            GROUP BY dn2.invoice_id
        ) dn ON dn.invoice_id = i.id
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT')
          AND (i.subtotal - COALESCE(pa.total_paid, 0)
            - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
          AND (c.legal_name LIKE %:search% OR c.rif LIKE %:search%)
        """,
        nativeQuery = true)
    List<Object[]> findCustomersWithDebtBySearchRaw(@Param("search") String search,
                                                    org.springframework.data.domain.Pageable pageable);
}
