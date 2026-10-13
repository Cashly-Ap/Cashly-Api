package com.cashly.cashly_api.receivables.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.payments.infrastructure.adapters.out.persistence.entities.PaymentAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReceivablesJpaRepository extends JpaRepository<PaymentAllocationEntity, UUID> {

    @Query(value = """
        SELECT COALESCE(SUM(i.subtotal - COALESCE(i.returned_total, 0) - COALESCE(pa.total_paid, 0)
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
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT', 'PAID')
          AND i.status <> 'FULLY_RETURNED'
          AND (i.subtotal - COALESCE(i.returned_total, 0) - COALESCE(pa.total_paid, 0)
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
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT', 'PAID')
          AND i.status <> 'FULLY_RETURNED'
          AND (i.subtotal - COALESCE(i.returned_total, 0) - COALESCE(pa.total_paid, 0)
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
        WHERE i.status NOT IN ('CANCELLED', 'DRAFT', 'PAID')
          AND i.status <> 'FULLY_RETURNED'
          AND (i.subtotal - COALESCE(i.returned_total, 0) - COALESCE(pa.total_paid, 0)
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
        SELECT COUNT(*)
        FROM customers
        """, nativeQuery = true)
    long countAllCustomers();

    @Query(value = """
        SELECT c.id, c.legal_name AS customerName, c.rif AS customerRif,
               COALESCE(d.total_debt, 0) AS totalDebt,
               COALESCE(pending.pending_amount, 0) AS pendingPaymentsAmount,
               COALESCE(d.invoice_count, 0) AS pendingInvoicesCount,
               d.oldest_invoice_date AS oldestInvoiceDate,
               c.credit_limit AS creditLimit,
               GREATEST(c.credit_limit - COALESCE(d.total_debt, 0), 0) AS availableCredit
        FROM customers c
        LEFT JOIN (
            SELECT i.customer_id,
                   SUM(i.subtotal - COALESCE(i.returned_total, 0) - COALESCE(pa.total_paid, 0)
                       - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) AS total_debt,
                   COUNT(DISTINCT i.id) AS invoice_count,
                   MIN(i.due_date) AS oldest_invoice_date
            FROM invoices i
            LEFT JOIN (
                SELECT invoice_id, SUM(allocated_amount) AS total_paid
                FROM payment_allocations
                GROUP BY invoice_id
            ) pa ON i.id = pa.invoice_id
            LEFT JOIN (
                SELECT invoice_id, SUM(amount) AS total_credit
                FROM credit_notes
                GROUP BY invoice_id
            ) cn ON i.id = cn.invoice_id
            LEFT JOIN (
                SELECT invoice_id, SUM(amount) AS total_debit
                FROM debit_notes
                GROUP BY invoice_id
            ) dn ON i.id = dn.invoice_id
            WHERE i.status NOT IN ('CANCELLED', 'DRAFT', 'PAID')
              AND i.status <> 'FULLY_RETURNED'
            GROUP BY i.customer_id
        ) d ON d.customer_id = c.id
        LEFT JOIN (
            SELECT customer_id, SUM(amount) AS pending_amount
            FROM payments
            WHERE status IN ('PENDING_APPROVAL', 'DRAFT')
            GROUP BY customer_id
        ) pending ON pending.customer_id = c.id
        ORDER BY totalDebt DESC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM customers
        """,
        nativeQuery = true)
    List<Object[]> findCustomersWithDebtRaw(org.springframework.data.domain.Pageable pageable);

    @Query(value = """
        SELECT COUNT(*)
        FROM customers c
        WHERE (c.legal_name LIKE %:search% OR c.rif LIKE %:search%)
        """, nativeQuery = true)
    long countCustomersWithDebtBySearch(@Param("search") String search);

    @Query(value = """
        SELECT c.id, c.legal_name AS customerName, c.rif AS customerRif,
               COALESCE(d.total_debt, 0) AS totalDebt,
               COALESCE(pending.pending_amount, 0) AS pendingPaymentsAmount,
               COALESCE(d.invoice_count, 0) AS pendingInvoicesCount,
               d.oldest_invoice_date AS oldestInvoiceDate,
               c.credit_limit AS creditLimit,
               GREATEST(c.credit_limit - COALESCE(d.total_debt, 0), 0) AS availableCredit
        FROM customers c
        LEFT JOIN (
            SELECT i.customer_id,
                   SUM(i.subtotal - COALESCE(i.returned_total, 0) - COALESCE(pa.total_paid, 0)
                       - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) AS total_debt,
                   COUNT(DISTINCT i.id) AS invoice_count,
                   MIN(i.due_date) AS oldest_invoice_date
            FROM invoices i
            LEFT JOIN (
                SELECT invoice_id, SUM(allocated_amount) AS total_paid
                FROM payment_allocations
                GROUP BY invoice_id
            ) pa ON i.id = pa.invoice_id
            LEFT JOIN (
                SELECT invoice_id, SUM(amount) AS total_credit
                FROM credit_notes
                GROUP BY invoice_id
            ) cn ON i.id = cn.invoice_id
            LEFT JOIN (
                SELECT invoice_id, SUM(amount) AS total_debit
                FROM debit_notes
                GROUP BY invoice_id
            ) dn ON i.id = dn.invoice_id
            WHERE i.status NOT IN ('CANCELLED', 'DRAFT', 'PAID')
              AND i.status <> 'FULLY_RETURNED'
            GROUP BY i.customer_id
        ) d ON d.customer_id = c.id
        LEFT JOIN (
            SELECT customer_id, SUM(amount) AS pending_amount
            FROM payments
            WHERE status IN ('PENDING_APPROVAL', 'DRAFT')
            GROUP BY customer_id
        ) pending ON pending.customer_id = c.id
        WHERE c.legal_name LIKE %:search% OR c.rif LIKE %:search%
        ORDER BY totalDebt DESC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM customers c
        WHERE (c.legal_name LIKE %:search% OR c.rif LIKE %:search%)
        """,
        nativeQuery = true)
    List<Object[]> findCustomersWithDebtBySearchRaw(@Param("search") String search,
                                                    org.springframework.data.domain.Pageable pageable);
}
