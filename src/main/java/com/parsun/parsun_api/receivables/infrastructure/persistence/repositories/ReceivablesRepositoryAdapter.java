package com.parsun.parsun_api.receivables.infrastructure.persistence.repositories;

import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerEntity;
import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerPhoneEntity;
import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerEmailEntity;
import com.parsun.parsun_api.receivables.application.dto.response.*;
import com.parsun.parsun_api.receivables.domain.repositories.ReceivablesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ReceivablesRepositoryAdapter implements ReceivablesRepository {

    private final EntityManager entityManager;
    private final ReceivablesJpaRepository receivablesJpaRepository;

    public ReceivablesRepositoryAdapter(EntityManager entityManager,
                                         ReceivablesJpaRepository receivablesJpaRepository) {
        this.entityManager = entityManager;
        this.receivablesJpaRepository = receivablesJpaRepository;
    }

    @Override
    public ReceivablesSummaryResponse findSummary() {
        BigDecimal totalDebt = receivablesJpaRepository.findTotalDebt();
        if (totalDebt == null) totalDebt = BigDecimal.ZERO;
        long customersWithDebt = receivablesJpaRepository.countCustomersWithDebt();
        long invoicesWithDebt = receivablesJpaRepository.countInvoicesWithDebt();
        BigDecimal pendingValidation = receivablesJpaRepository.findTotalPendingValidationAmount();
        if (pendingValidation == null) pendingValidation = BigDecimal.ZERO;

        return new ReceivablesSummaryResponse(
                totalDebt, customersWithDebt, invoicesWithDebt,
                pendingValidation, LocalDateTime.now()
        );
    }

    @Override
    public Page<CustomerDebtListResponse> findCustomersWithDebt(String search, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        List<Object[]> rows;
        long total;

        if (search != null && !search.isBlank()) {
            total = receivablesJpaRepository.countCustomersWithDebtBySearch(search);
            rows = receivablesJpaRepository.findCustomersWithDebtBySearchRaw(search, pageable);
        } else {
            total = receivablesJpaRepository.countCustomersWithDebt();
            rows = receivablesJpaRepository.findCustomersWithDebtRaw(pageable);
        }

        List<CustomerDebtListResponse> content = rows.stream()
                .map(row -> {
                    UUID customerId = toUuid(row[0]);
                    String customerName = (String) row[1];
                    String customerRif = (String) row[2];
                    BigDecimal totalDebt = (BigDecimal) row[3];
                    BigDecimal pendingAmount = (BigDecimal) row[4];
                    long invoicedCount = ((Number) row[5]).longValue();
                    Date oldestDate = (Date) row[6];
                    LocalDate oldestInvoiceDate = oldestDate != null
                            ? new java.sql.Date(oldestDate.getTime()).toLocalDate()
                            : null;
                    return new CustomerDebtListResponse(
                            customerId, customerName, customerRif,
                            totalDebt, pendingAmount, invoicedCount, oldestInvoiceDate
                    );
                })
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public CustomerDebtSummaryResponse findCustomerDebt(UUID customerId) {
        CustomerEntity customer = entityManager.find(CustomerEntity.class, customerId);

        Query debtQuery = entityManager.createNativeQuery("""
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
            WHERE i.customer_id = :customerId
              AND i.status NOT IN ('CANCELLED', 'DRAFT')
              AND (i.subtotal - COALESCE(pa.total_paid, 0)
                - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
            """);
        debtQuery.setParameter("customerId", customerId);
        BigDecimal totalDebt = (BigDecimal) debtQuery.getSingleResult();
        if (totalDebt == null) totalDebt = BigDecimal.ZERO;

        Query pendingQuery = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM payments p
            WHERE p.customer_id = :customerId
              AND p.status IN ('PENDING_APPROVAL', 'DRAFT')
            """);
        pendingQuery.setParameter("customerId", customerId);
        BigDecimal pendingAmount = (BigDecimal) pendingQuery.getSingleResult();
        if (pendingAmount == null) pendingAmount = BigDecimal.ZERO;

        BigDecimal availableDebt = customer != null
                ? customer.getCreditLimit().subtract(totalDebt).max(BigDecimal.ZERO)
                : BigDecimal.ZERO;

        Query countQuery = entityManager.createNativeQuery("""
            SELECT COUNT(*)
            FROM payments p
            WHERE p.customer_id = :customerId
              AND p.status IN ('PENDING_APPROVAL', 'DRAFT')
            """);
        countQuery.setParameter("customerId", customerId);
        long pendingPaymentsCount = ((Number) countQuery.getSingleResult()).longValue();

        Query invoicesQuery = entityManager.createNativeQuery("""
            SELECT i.id, i.invoice_number, i.status, i.created_at,
                   i.subtotal,
                   COALESCE(pa.total_paid, 0) AS total_paid,
                   (i.subtotal - COALESCE(pa.total_paid, 0)
                     - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) AS balance,
                   p_all.payment_id AS pending_payment_id,
                   p_all.payment_status AS pending_payment_status
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
            LEFT JOIN (
                SELECT pa2.invoice_id, pa2.payment_id, p2.status AS payment_status
                FROM payment_allocations pa2
                JOIN payments p2 ON p2.id = pa2.payment_id
                WHERE p2.status IN ('PENDING_APPROVAL', 'DRAFT')
            ) p_all ON p_all.invoice_id = i.id
            WHERE i.customer_id = :customerId
              AND i.status NOT IN ('CANCELLED', 'DRAFT')
              AND (i.subtotal - COALESCE(pa.total_paid, 0)
                - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0)) > 0
            ORDER BY i.created_at DESC
            """);
        invoicesQuery.setParameter("customerId", customerId);
        @SuppressWarnings("unchecked")
        List<Object[]> invoiceRows = invoicesQuery.getResultList();

        List<CustomerInvoiceDebtResponse> invoices = invoiceRows.stream()
                .map(row -> new CustomerInvoiceDebtResponse(
                        toUuid(row[0]),
                        (String) row[1],
                        (String) row[2],
                        row[3] != null ? ((java.sql.Timestamp) row[3]).toLocalDateTime().toLocalDate() : null,
                        (BigDecimal) row[4],
                        (BigDecimal) row[5],
                        (BigDecimal) row[6],
                        toUuid(row[7]),
                        (String) row[8]
                ))
                .toList();

        String phone = customer != null && !customer.getPhones().isEmpty()
                ? customer.getPhones().get(0).getPhone()
                : null;
        String email = customer != null && !customer.getEmails().isEmpty()
                ? customer.getEmails().get(0).getEmail()
                : null;

        return new CustomerDebtSummaryResponse(
                customerId,
                customer != null ? customer.getLegalName() : null,
                customer != null ? customer.getRif() : null,
                phone,
                email,
                totalDebt,
                pendingAmount,
                availableDebt,
                invoices,
                pendingPaymentsCount
        );
    }

    @Override
    public Page<CustomerInvoiceDebtResponse> findCustomerInvoices(UUID customerId, String search, String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        String notesJoinClause = """
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
            """;

        String balanceExpr = "(i.subtotal - COALESCE(pa.total_paid, 0)"
            + " - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0))";

        String partialBalanceExpr = "(i.subtotal - COALESCE(pa.total_paid, 0)"
            + " - COALESCE(cn.total_credit, 0) + COALESCE(dn.total_debit, 0))";

        String whereClauseCond = "i.customer_id = :customerId AND i.status NOT IN ('CANCELLED', 'DRAFT')";
        if (status != null && !status.isBlank()) {
            whereClauseCond += switch (status) {
                case "PENDING" -> " AND i.status IN ('PENDING_PAYMENT') AND " + balanceExpr + " > 0";
                case "PARTIAL" -> " AND i.status IN ('PARTIALLY_PAID', 'PENDING_PAYMENT') AND " + partialBalanceExpr + " > 0 AND " + partialBalanceExpr + " < i.subtotal";
                case "PAID" -> " AND i.status = 'PAID'";
                default -> " AND " + balanceExpr + " > 0";
            };
        } else {
            whereClauseCond += " AND " + balanceExpr + " > 0";
        }
        if (search != null && !search.isBlank()) {
            whereClauseCond += " AND (i.invoice_number LIKE :search)";
        }

        String countSql = "SELECT COUNT(DISTINCT i.id)\n" +
            "            FROM invoices i\n" +
            "            LEFT JOIN (\n" +
            "                SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid\n" +
            "                FROM payment_allocations p_allocs\n" +
            "                GROUP BY p_allocs.invoice_id\n" +
            "            ) pa ON i.id = pa.invoice_id\n" +
            notesJoinClause +
            "            WHERE " + whereClauseCond;
        Query countQ = entityManager.createNativeQuery(countSql);
        countQ.setParameter("customerId", customerId);
        if (search != null && !search.isBlank()) {
            countQ.setParameter("search", "%" + search + "%");
        }
        long total = ((Number) countQ.getSingleResult()).longValue();

        String dataSql = "SELECT i.id, i.invoice_number, i.status, i.created_at,\n" +
            "                   i.subtotal,\n" +
            "                   COALESCE(pa.total_paid, 0) AS total_paid,\n" +
            "                   " + balanceExpr + " AS balance,\n" +
            "                   p_all.payment_id AS pending_payment_id,\n" +
            "                   p_all.payment_status AS pending_payment_status\n" +
            "            FROM invoices i\n" +
            "            LEFT JOIN (\n" +
            "                SELECT p_allocs.invoice_id, SUM(p_allocs.allocated_amount) AS total_paid\n" +
            "                FROM payment_allocations p_allocs\n" +
            "                GROUP BY p_allocs.invoice_id\n" +
            "            ) pa ON i.id = pa.invoice_id\n" +
            notesJoinClause +
            "            LEFT JOIN (\n" +
            "                SELECT pa2.invoice_id, pa2.payment_id, p2.status AS payment_status\n" +
            "                FROM payment_allocations pa2\n" +
            "                JOIN payments p2 ON p2.id = pa2.payment_id\n" +
            "                WHERE p2.status IN ('PENDING_APPROVAL', 'DRAFT')\n" +
            "            ) p_all ON p_all.invoice_id = i.id\n" +
            "            WHERE " + whereClauseCond + "\n" +
            "            ORDER BY i.created_at DESC";
        Query dataQ = entityManager.createNativeQuery(dataSql);
        dataQ.setParameter("customerId", customerId);
        if (search != null && !search.isBlank()) {
            dataQ.setParameter("search", "%" + search + "%");
        }
        dataQ.setFirstResult(page * size);
        dataQ.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQ.getResultList();
        List<CustomerInvoiceDebtResponse> content = rows.stream()
                .map(row -> new CustomerInvoiceDebtResponse(
                        toUuid(row[0]),
                        (String) row[1],
                        (String) row[2],
                        row[3] != null ? ((java.sql.Timestamp) row[3]).toLocalDateTime().toLocalDate() : null,
                        (BigDecimal) row[4],
                        (BigDecimal) row[5],
                        (BigDecimal) row[6],
                        toUuid(row[7]),
                        (String) row[8]
                ))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<PendingPaymentResponse> findPendingPayments(String status, String search, int page, int size) {
        return findPaymentsWithFilters(null, status, search, page, size, false);
    }

    @Override
    public Page<PendingPaymentResponse> findMerchantPayments(UUID merchantId, String status, String search,
                                                              int page, int size) {
        return findPaymentsWithFilters(merchantId, status, search, page, size, true);
    }

    @Override
    public Page<PendingPaymentResponse> findPaymentHistory(String search, int page, int size) {
        return findPaymentsWithFilters(null, null, search, page, size, false);
    }

    @Override
    public Page<PendingPaymentResponse> findCustomerPaymentHistory(UUID customerId, UUID invoiceId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        String whereClauseCond = "p.customer_id = :customerId";
        if (invoiceId != null) {
            whereClauseCond += " AND EXISTS (SELECT 1 FROM payment_allocations pa3 WHERE pa3.payment_id = p.id AND pa3.invoice_id = :invoiceId)";
        }

        String countSql = "SELECT COUNT(DISTINCT p.id)\n" +
            "            FROM payments p\n" +
            "            LEFT JOIN payment_allocations pa ON pa.payment_id = p.id\n" +
            "            LEFT JOIN invoices i ON i.id = pa.invoice_id\n" +
            "            LEFT JOIN payment_methods pm ON pm.id = p.payment_method_id\n" +
            "            WHERE " + whereClauseCond;
        Query countQ = entityManager.createNativeQuery(countSql);
        countQ.setParameter("customerId", customerId);
        if (invoiceId != null) {
            countQ.setParameter("invoiceId", invoiceId);
        }
        long total = ((Number) countQ.getSingleResult()).longValue();

        String dataSql = "SELECT DISTINCT p.id, p.payment_number, p.customer_id, c.legal_name,\n" +
            "                   p.amount, pm.name, p.status, p.payment_date, p.created_at,\n" +
            "                   i.invoice_number, p.voucher_path, p.approval_status,\n" +
            "                   p.rejection_reason, p.commission_percentage\n" +
            "            FROM payments p\n" +
            "            JOIN customers c ON c.id = p.customer_id\n" +
            "            LEFT JOIN payment_allocations pa ON pa.payment_id = p.id\n" +
            "            LEFT JOIN invoices i ON i.id = pa.invoice_id\n" +
            "            LEFT JOIN payment_methods pm ON pm.id = p.payment_method_id\n" +
            "            WHERE " + whereClauseCond + "\n" +
            "            ORDER BY p.created_at DESC";
        Query dataQ = entityManager.createNativeQuery(dataSql);
        dataQ.setParameter("customerId", customerId);
        if (invoiceId != null) {
            dataQ.setParameter("invoiceId", invoiceId);
        }
        dataQ.setFirstResult(page * size);
        dataQ.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQ.getResultList();
        List<PendingPaymentResponse> content = mapPendingPaymentRows(rows);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<PendingPaymentResponse> findPendingPaymentById(UUID paymentId) {
        Query query = entityManager.createNativeQuery("""
            SELECT p.id, p.payment_number, p.customer_id, c.legal_name,
                   p.amount, pm.name, p.status, p.payment_date, p.created_at,
                   i.invoice_number, p.voucher_path, p.approval_status,
                   p.rejection_reason, p.commission_percentage
            FROM payments p
            JOIN customers c ON c.id = p.customer_id
            LEFT JOIN payment_allocations pa ON pa.payment_id = p.id
            LEFT JOIN invoices i ON i.id = pa.invoice_id
            LEFT JOIN payment_methods pm ON pm.id = p.payment_method_id
            WHERE p.id = :paymentId
            LIMIT 1
            """);
        query.setParameter("paymentId", paymentId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapPendingPaymentRow(rows.get(0)));
    }

    private Page<PendingPaymentResponse> findPaymentsWithFilters(UUID merchantId, String status,
                                                                   String search, int page, int size,
                                                                   boolean isMerchant) {
        PageRequest pageable = PageRequest.of(page, size);
        List<String> conditions = new ArrayList<>();

        if (isMerchant && merchantId != null) {
            conditions.add("p.created_by = :merchantId");
        }
        if (status != null && !status.isBlank()) {
            conditions.add("p.status = :status");
        }
        if (search != null && !search.isBlank()) {
            conditions.add("(p.payment_number LIKE :search OR c.legal_name LIKE :search OR i.invoice_number LIKE :search)");
        }

        String conditionsStr = conditions.isEmpty() ? "1=1" : String.join(" AND ", conditions);
        String countSql = "SELECT COUNT(DISTINCT p.id)\n" +
            "            FROM payments p\n" +
            "            JOIN customers c ON c.id = p.customer_id\n" +
            "            LEFT JOIN payment_allocations pa ON pa.payment_id = p.id\n" +
            "            LEFT JOIN invoices i ON i.id = pa.invoice_id\n" +
            "            WHERE " + conditionsStr;
        Query countQ = entityManager.createNativeQuery(countSql);
        applyParams(countQ, merchantId, status, search, isMerchant);

        long total = ((Number) countQ.getSingleResult()).longValue();

        String dataSql = "SELECT DISTINCT p.id, p.payment_number, p.customer_id, c.legal_name,\n" +
            "                   p.amount, pm.name, p.status, p.payment_date, p.created_at,\n" +
            "                   i.invoice_number, p.voucher_path, p.approval_status,\n" +
            "                   p.rejection_reason, p.commission_percentage\n" +
            "            FROM payments p\n" +
            "            JOIN customers c ON c.id = p.customer_id\n" +
            "            LEFT JOIN payment_allocations pa ON pa.payment_id = p.id\n" +
            "            LEFT JOIN invoices i ON i.id = pa.invoice_id\n" +
            "            LEFT JOIN payment_methods pm ON pm.id = p.payment_method_id\n" +
            "            WHERE " + conditionsStr + "\n" +
            "            ORDER BY p.created_at DESC";
        Query dataQ = entityManager.createNativeQuery(dataSql);
        applyParams(dataQ, merchantId, status, search, isMerchant);
        dataQ.setFirstResult(page * size);
        dataQ.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQ.getResultList();
        List<PendingPaymentResponse> content = mapPendingPaymentRows(rows);

        return new PageImpl<>(content, pageable, total);
    }

    private void applyParams(Query query, UUID merchantId, String status, String search, boolean isMerchant) {
        if (isMerchant && merchantId != null) {
            query.setParameter("merchantId", merchantId);
        }
        if (status != null && !status.isBlank()) {
            query.setParameter("status", status);
        }
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }
    }

    private List<PendingPaymentResponse> mapPendingPaymentRows(List<Object[]> rows) {
        return rows.stream()
                .map(this::mapPendingPaymentRow)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private PendingPaymentResponse mapPendingPaymentRow(Object[] row) {
        return new PendingPaymentResponse(
                toUuid(row[0]),       
                (String) row[1],     
                toUuid(row[2]),       
                (String) row[3],     
                (BigDecimal) row[4], 
                (String) row[5],     
                (String) row[6],     
                row[7] != null ? ((java.sql.Date) row[7]).toLocalDate() : null,   
                row[8] != null ? ((Timestamp) row[8]).toLocalDateTime() : null,   
                (String) row[9],     
                (String) row[10],    
                (String) row[11],    
                (String) row[12],    
                (BigDecimal) row[13] 
        );
    }

    private UUID toUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        if (value instanceof byte[] bytes) {
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            return new UUID(bb.getLong(), bb.getLong());
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to UUID");
    }
}
