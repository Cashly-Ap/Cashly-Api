package com.cashly.cashly_api.finance.treasury.payables.infrastructure.persistence.adapters;

import com.cashly.cashly_api.finance.treasury.payables.application.customer_debt.use_case.SupplierDebtAction;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.PayablePaymentHistoryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.PayablesSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtListResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierPurchaseDebtResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.ports.out.PayablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public class PayablesRepositoryAdapter implements PayablesQueryGateway {

    private static final String DEBT_SUBQUERY = """
            SELECT p.supplier_id AS supplier_id,
                   COUNT(*) AS purchase_count,
                   SUM(p.subtotal - COALESCE(p.returned_total, 0)
                       - COALESCE(pa.paid, 0)) AS total_debt,
                   MIN(p.due_date) AS oldest_due
            FROM purchases p
            LEFT JOIN (
                SELECT purchase_id, SUM(allocated_amount) AS paid
                FROM supplier_payment_allocations
                GROUP BY purchase_id
            ) pa ON pa.purchase_id = p.id
            WHERE p.status NOT IN ('DRAFT', 'CANCELLED', 'PAID',
                                   'FULLY_RETURNED')
            GROUP BY p.supplier_id
            HAVING total_debt > 0
            """;

    private final EntityManager entityManager;

    public PayablesRepositoryAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public PayablesSummaryResponse findSummary() {
        BigDecimal totalDebt = number(entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(d.total_debt), 0) FROM (" + DEBT_SUBQUERY
                        + ") d")
                .getSingleResult());
        long suppliers = number(entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT d.supplier_id) FROM (" + DEBT_SUBQUERY
                        + ") d")
                .getSingleResult()).longValue();
        long purchases = number(entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(d.purchase_count), 0) FROM ("
                        + DEBT_SUBQUERY + ") d")
                .getSingleResult()).longValue();
        BigDecimal pending = number(entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM supplier_payments WHERE status = 'PENDING'")
                .getSingleResult());
        BigDecimal balances = number(entityManager.createNativeQuery(
                "SELECT COALESCE(SUM(total_balance), 0) FROM supplier_balances")
                .getSingleResult());
        return new PayablesSummaryResponse(totalDebt, suppliers, purchases,
                pending, balances, LocalDateTime.now());
    }

    @Override
    public PageResponse<SupplierDebtListResponse> findSuppliersWithDebt(
            String search, int page, int size, String sort,
            SupplierDebtAction action) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id, s.legal_name, s.rif, d.total_debt,
                       COALESCE(pend.pending_amount, 0) AS pending_amount,
                       d.purchase_count, d.oldest_due,
                       s.credit_days, s.credit_limit,
                       GREATEST(s.credit_limit - d.total_debt
                           + COALESCE(sb.total_balance, 0), 0) AS available_credit,
                       COALESCE(sb.total_balance, 0) AS supplier_balance
                FROM suppliers s
                JOIN (""" + DEBT_SUBQUERY + """
                ) d ON d.supplier_id = s.id
                LEFT JOIN supplier_balances sb ON sb.supplier_id = s.id
                LEFT JOIN (
                    SELECT supplier_id, SUM(amount) AS pending_amount
                    FROM supplier_payments
                    WHERE status = 'PENDING'
                    GROUP BY supplier_id
                ) pend ON pend.supplier_id = s.id
                WHERE 1 = 1
                """);
        if (search != null && !search.isBlank()) {
            sql.append(
                    " AND (s.legal_name LIKE :search OR s.rif LIKE :search) ");
        }
        if (action == SupplierDebtAction.REFUND) {
            sql.append(" AND COALESCE(sb.total_balance, 0) > 0 ");
        }
        sql.append(orderBy(sort));
        Query query = entityManager.createNativeQuery(sql.toString());
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        long total = count("SELECT COUNT(*) FROM (" + sql + ") eligible");
        if (search != null && !search.isBlank()) {
            total = countWithSearch(
                    "SELECT COUNT(*) FROM (" + sql + ") eligible", search);
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<SupplierDebtListResponse> content = rows.stream()
                .map(row -> new SupplierDebtListResponse(toUuid(row[0]),
                        (String) row[1], (String) row[2], number(row[3]),
                        number(row[4]), ((Number) row[5]).longValue(),
                        toLocalDate(row[6]), ((Number) row[7]).intValue(),
                        number(row[8]), number(row[9]), number(row[10])))
                .toList();
        return toPage(content, total, page, size);
    }

    @Override
    public SupplierDebtSummaryResponse findSupplierDebt(UUID supplierId) {
        String sql = """
                SELECT s.id, s.legal_name, s.rif,
                       (SELECT ph.phone FROM supplier_phones ph
                        WHERE ph.supplier_id = s.id LIMIT 1),
                       (SELECT em.email FROM supplier_emails em
                        WHERE em.supplier_id = s.id LIMIT 1),
                       COALESCE(d.total_debt, 0),
                       COALESCE(pend.pending_amount, 0),
                       s.credit_days, s.credit_limit,
                       GREATEST(s.credit_limit - COALESCE(d.total_debt, 0)
                           + COALESCE(sb.total_balance, 0), 0),
                       COALESCE(sb.total_balance, 0),
                       COALESCE(pend.pending_count, 0)
                FROM suppliers s
                LEFT JOIN (""" + DEBT_SUBQUERY + """
                ) d ON d.supplier_id = s.id
                LEFT JOIN supplier_balances sb ON sb.supplier_id = s.id
                LEFT JOIN (
                    SELECT supplier_id, SUM(amount) AS pending_amount,
                           COUNT(*) AS pending_count
                    FROM supplier_payments
                    WHERE status = 'PENDING'
                    GROUP BY supplier_id
                ) pend ON pend.supplier_id = s.id
                WHERE s.id = :supplierId
                """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("supplierId", supplierId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Supplier not found: " + supplierId);
        }
        Object[] row = rows.get(0);
        List<SupplierPurchaseDebtResponse> purchases = findSupplierPurchases(
                supplierId, null, null, 0, 500, false).content();
        BigDecimal totalDebt = number(row[5]);
        BigDecimal creditLimit = number(row[8]);
        BigDecimal balance = number(row[10]);
        BigDecimal available = creditLimit.subtract(totalDebt).add(balance)
                .max(BigDecimal.ZERO);
        return new SupplierDebtSummaryResponse(toUuid(row[0]), (String) row[1],
                (String) row[2], (String) row[3], (String) row[4], totalDebt,
                number(row[6]), available, ((Number) row[7]).intValue(),
                creditLimit, available, balance, purchases,
                ((Number) row[11]).longValue());
    }

    @Override
    public PageResponse<SupplierPurchaseDebtResponse> findSupplierPurchases(
            UUID supplierId, String search, String status, int page, int size,
            boolean includeNonDebt) {
        StringBuilder where = new StringBuilder(" WHERE p.supplier_id = :supplierId ");
        if (!includeNonDebt) {
            where.append(
                    " AND p.status NOT IN ('DRAFT', 'CANCELLED', 'PAID', 'FULLY_RETURNED') ");
            where.append(
                    " AND (p.subtotal - COALESCE(p.returned_total, 0) - COALESCE(pa.paid, 0)) > 0 ");
        }
        if (search != null && !search.isBlank()) {
            where.append(" AND p.purchase_number LIKE :search ");
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND p.status = :status ");
        }
        String select = """
                SELECT p.id, p.purchase_number, p.status, p.payment_type,
                       p.issue_date, p.due_date, p.subtotal,
                       COALESCE(p.returned_total, 0),
                       COALESCE(pa.paid, 0),
                       p.subtotal - COALESCE(p.returned_total, 0),
                       p.subtotal - COALESCE(p.returned_total, 0)
                           - COALESCE(pa.paid, 0),
                       (SELECT sp.id FROM supplier_payment_allocations a
                        JOIN supplier_payments sp ON sp.id = a.payment_id
                        WHERE a.purchase_id = p.id AND sp.status = 'PENDING'
                        LIMIT 1),
                       (SELECT sp.status FROM supplier_payment_allocations a
                        JOIN supplier_payments sp ON sp.id = a.payment_id
                        WHERE a.purchase_id = p.id AND sp.status = 'PENDING'
                        LIMIT 1)
                FROM purchases p
                LEFT JOIN (
                    SELECT purchase_id, SUM(allocated_amount) AS paid
                    FROM supplier_payment_allocations
                    GROUP BY purchase_id
                ) pa ON pa.purchase_id = p.id
                """;
        Query query = entityManager.createNativeQuery(select + where
                + " ORDER BY p.issue_date DESC, p.created_at DESC");
        query.setParameter("supplierId", supplierId);
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }
        if (status != null && !status.isBlank()) {
            query.setParameter("status", status);
        }
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        Query countQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM (" + select + where + ") eligible");
        countQuery.setParameter("supplierId", supplierId);
        if (search != null && !search.isBlank()) {
            countQuery.setParameter("search", "%" + search + "%");
        }
        if (status != null && !status.isBlank()) {
            countQuery.setParameter("status", status);
        }
        long total = ((Number) countQuery.getSingleResult()).longValue();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<SupplierPurchaseDebtResponse> content = rows.stream()
                .map(row -> new SupplierPurchaseDebtResponse(toUuid(row[0]),
                        (String) row[1], (String) row[2], (String) row[3],
                        toLocalDate(row[4]), toLocalDate(row[5]),
                        number(row[6]), number(row[7]), number(row[8]),
                        number(row[9]), number(row[10]), toUuid(row[11]),
                        (String) row[12]))
                .toList();
        return toPage(content, total, page, size);
    }

    @Override
    public PageResponse<PayablePaymentHistoryResponse> findPaymentHistory(
            String search, int page, int size) {
        return payments(null, null, search, page, size);
    }

    @Override
    public PageResponse<PayablePaymentHistoryResponse> findSupplierPaymentHistory(
            UUID supplierId, UUID purchaseId, int page, int size) {
        return payments(supplierId, purchaseId, null, page, size);
    }

    @Override
    public BigDecimal findSupplierBalance(UUID supplierId) {
        List<?> result = entityManager.createNativeQuery(
                "SELECT COALESCE(total_balance, 0) FROM supplier_balances WHERE supplier_id = :supplierId")
                .setParameter("supplierId", supplierId).getResultList();
        return result.isEmpty() ? BigDecimal.ZERO : number(result.get(0));
    }

    private PageResponse<PayablePaymentHistoryResponse> payments(
            UUID supplierId, UUID purchaseId, String search, int page,
            int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        if (supplierId != null) {
            where.append(" AND sp.supplier_id = :supplierId ");
        }
        if (purchaseId != null) {
            where.append(
                    " AND EXISTS (SELECT 1 FROM supplier_payment_allocations a WHERE a.payment_id = sp.id AND a.purchase_id = :purchaseId) ");
        }
        if (search != null && !search.isBlank()) {
            where.append(
                    " AND (sp.payment_number LIKE :search OR s.legal_name LIKE :search) ");
        }
        String select = """
                SELECT sp.id, sp.payment_number, sp.supplier_id, s.legal_name,
                       sp.amount, COALESCE(sp.applied_amount, 0), pm.name,
                       sp.status, sp.source, sp.reference_number,
                       sp.payment_date, sp.notes, sp.created_at,
                       sp.created_by_name,
                       (SELECT p.purchase_number
                        FROM supplier_payment_allocations a
                        JOIN purchases p ON p.id = a.purchase_id
                        WHERE a.payment_id = sp.id LIMIT 1)
                FROM supplier_payments sp
                JOIN suppliers s ON s.id = sp.supplier_id
                LEFT JOIN payment_methods pm ON pm.id = sp.payment_method_id
                """;
        Query query = entityManager.createNativeQuery(select + where
                + " ORDER BY sp.created_at DESC");
        bindPaymentParams(query, supplierId, purchaseId, search);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        Query countQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM supplier_payments sp JOIN suppliers s ON s.id = sp.supplier_id"
                        + where);
        bindPaymentParams(countQuery, supplierId, purchaseId, search);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<PayablePaymentHistoryResponse> content = rows.stream()
                .map(row -> new PayablePaymentHistoryResponse(toUuid(row[0]),
                        (String) row[1], toUuid(row[2]), (String) row[3],
                        number(row[4]), number(row[5]), (String) row[6],
                        (String) row[7], (String) row[8], (String) row[9],
                        toLocalDate(row[10]), (String) row[11],
                        toLocalDateTime(row[12]), (String) row[13],
                        (String) row[14]))
                .toList();
        return toPage(content, total, page, size);
    }

    private void bindPaymentParams(Query query, UUID supplierId,
            UUID purchaseId, String search) {
        if (supplierId != null) {
            query.setParameter("supplierId", supplierId);
        }
        if (purchaseId != null) {
            query.setParameter("purchaseId", purchaseId);
        }
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }
    }

    private String orderBy(String sort) {
        String field = sort == null || sort.isBlank() ? "totalDebt" : sort;
        int comma = field.indexOf(',');
        if (comma > 0) {
            field = field.substring(0, comma);
        }
        String column = switch (field) {
            case "supplierName" -> "s.legal_name";
            case "creditLimit" -> "s.credit_limit";
            case "availableCredit" -> "available_credit";
            case "pendingPurchasesCount" -> "d.purchase_count";
            default -> "d.total_debt";
        };
        boolean desc = sort == null || sort.isBlank() || !sort.contains("asc");
        return " ORDER BY " + column + (desc ? " DESC " : " ASC ");
    }

    private long count(String sql) {
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult())
                .longValue();
    }

    private long countWithSearch(String sql, String search) {
        return ((Number) entityManager.createNativeQuery(sql)
                .setParameter("search", "%" + search + "%").getSingleResult())
                .longValue();
    }

    private <T> PageResponse<T> toPage(List<T> content, long total, int page,
            int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResponse<>(content, total, totalPages, size, page,
                page == 0, page >= totalPages - 1, content.isEmpty());
    }

    private BigDecimal number(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value instanceof BigDecimal decimal
                ? decimal
                : new BigDecimal(value.toString());
    }

    private UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof byte[] bytes && bytes.length == 16) {
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(bytes);
            return new UUID(buffer.getLong(), buffer.getLong());
        }
        return UUID.fromString(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof Date date) {
            return new java.sql.Date(date.getTime()).toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.parse(value.toString());
    }
}
