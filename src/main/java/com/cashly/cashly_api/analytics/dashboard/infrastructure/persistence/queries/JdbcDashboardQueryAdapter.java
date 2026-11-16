package com.cashly.cashly_api.analytics.dashboard.infrastructure.persistence.queries;

import com.cashly.cashly_api.analytics.dashboard.application.dto.response.DashboardSummaryResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.InvoiceDueResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.MonthlySalesResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.ProfitSummaryResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.TopItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.cashly.cashly_api.analytics.dashboard.application.ports.out.DashboardQueryGateway;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class JdbcDashboardQueryAdapter implements DashboardQueryGateway {

    private final JdbcTemplate jdbc;

    public JdbcDashboardQueryAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public DashboardSummaryResponse getSummary() {
        YearMonth currentMonth = YearMonth.from(LocalDate.now());
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.plusMonths(1).atDay(1);
        BigDecimal monthlySales = sumBilling(start, end);
        BigDecimal monthlyExpenses = sumExpenses(start, end);

        long pendingOrders = jdbc.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE status IN ('PENDING', 'CONFIRMED')",
                Long.class);

        long pendingInvoices = countOutstandingInvoices(">=");
        long overdueInvoices = countOutstandingInvoices("<");

        BigDecimal overdueReceivables = jdbc
                .queryForObject("SELECT COALESCE(SUM(balance), 0) FROM ("
                        + "  SELECT i.id, (i.subtotal - COALESCE(i.returned_total, 0)"
                        + "    - COALESCE(pa.total_paid, 0) - COALESCE(cn.total_credit, 0)"
                        + "    + COALESCE(dn.total_debit, 0)) AS balance"
                        + "  FROM invoices i"
                        + "  LEFT JOIN (SELECT invoice_id, SUM(allocated_amount) AS total_paid FROM payment_allocations GROUP BY invoice_id) pa ON pa.invoice_id = i.id"
                        + "  LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_credit FROM credit_notes GROUP BY invoice_id) cn ON cn.invoice_id = i.id"
                        + "  LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_debit FROM debit_notes GROUP BY invoice_id) dn ON dn.invoice_id = i.id"
                        + "  WHERE i.status IN ('PENDING_PAYMENT', 'PARTIALLY_PAID')"
                        + "    AND i.due_date < CURDATE()"
                        + ") t WHERE t.balance > 0", BigDecimal.class);

        long lowStockProducts = jdbc.queryForObject(
                "SELECT COUNT(DISTINCT ws.product_id) FROM warehouse_stock ws WHERE ws.quantity <= 5 AND ws.quantity > 0",
                Long.class);

        return new DashboardSummaryResponse(
                monthlySales != null ? monthlySales : BigDecimal.ZERO,
                monthlyExpenses != null ? monthlyExpenses : BigDecimal.ZERO,
                (monthlySales != null ? monthlySales : BigDecimal.ZERO)
                        .subtract(monthlyExpenses != null ? monthlyExpenses
                                : BigDecimal.ZERO),
                pendingOrders,
                pendingInvoices,
                overdueInvoices,
                overdueReceivables != null
                        ? overdueReceivables
                        : BigDecimal.ZERO,
                lowStockProducts);
    }

    public ProfitSummaryResponse getProfitSummary(String period) {
        String normalized = normalizePeriod(period);
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end;
        switch (normalized) {
            case "DAY" -> {
                start = today;
                end = today.plusDays(1);
            }
            case "WEEK" -> {
                start = today.with(java.time.DayOfWeek.MONDAY);
                end = start.plusWeeks(1);
            }
            case "YEAR" -> {
                start = today.withDayOfYear(1);
                end = start.plusYears(1);
            }
            default -> {
                start = today.withDayOfMonth(1);
                end = start.plusMonths(1);
            }
        }
        BigDecimal billing = sumBilling(start, end);
        BigDecimal expenses = sumExpenses(start, end);
        return new ProfitSummaryResponse(billing, expenses,
                billing.subtract(expenses));
    }

    private String normalizePeriod(String period) {
        String normalized = period == null ? "MONTH"
                : period.trim().toUpperCase(Locale.ROOT);
        return Set.of("DAY", "WEEK", "MONTH", "YEAR").contains(normalized)
                ? normalized
                : "MONTH";
    }

    private BigDecimal sumBilling(LocalDate start, LocalDate end) {
        BigDecimal total = jdbc.queryForObject(
                "SELECT COALESCE(SUM(delta), 0) FROM ("
                        + "  SELECT i.subtotal AS delta FROM invoices i"
                        + "   WHERE i.status NOT IN ('CANCELLED', 'DRAFT')"
                        + "     AND i.created_at >= ? AND i.created_at < ?"
                        + "  UNION ALL"
                        + "  SELECT -cn.amount FROM credit_notes cn"
                        + "   WHERE cn.created_at >= ? AND cn.created_at < ?"
                        + "  UNION ALL"
                        + "  SELECT -r.total FROM returns r"
                        + "   WHERE r.created_at >= ? AND r.created_at < ?"
                        + "  UNION ALL"
                        + "  SELECT dn.amount FROM debit_notes dn"
                        + "   WHERE dn.created_at >= ? AND dn.created_at < ?"
                        + ") t",
                BigDecimal.class, start.atStartOfDay(), end.atStartOfDay(),
                start.atStartOfDay(), end.atStartOfDay(),
                start.atStartOfDay(), end.atStartOfDay(),
                start.atStartOfDay(), end.atStartOfDay());
        return total != null ? total : BigDecimal.ZERO;
    }

    private BigDecimal sumExpenses(LocalDate start, LocalDate end) {
        BigDecimal total = jdbc.queryForObject(
                "SELECT COALESCE(SUM(delta), 0) FROM ("
                        + "  SELECT p.subtotal AS delta FROM purchases p"
                        + "   WHERE p.status NOT IN ('CANCELLED', 'DRAFT')"
                        + "     AND p.created_at >= ? AND p.created_at < ?"
                        + "  UNION ALL"
                        + "  SELECT -pr.total FROM purchase_returns pr"
                        + "   WHERE pr.created_at >= ? AND pr.created_at < ?"
                        + "  UNION ALL"
                        + "  SELECT -pcn.amount FROM purchase_credit_notes pcn"
                        + "   WHERE pcn.created_at >= ? AND pcn.created_at < ?"
                        + "  UNION ALL"
                        + "  SELECT pdn.amount FROM purchase_debit_notes pdn"
                        + "   WHERE pdn.created_at >= ? AND pdn.created_at < ?"
                        + ") t",
                BigDecimal.class, start.atStartOfDay(), end.atStartOfDay(),
                start.atStartOfDay(), end.atStartOfDay(),
                start.atStartOfDay(), end.atStartOfDay(),
                start.atStartOfDay(), end.atStartOfDay());
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<MonthlySalesResponse> getMonthlySales() {
        return getSalesTrend("MONTH");
    }

    public List<MonthlySalesResponse> getSalesTrend(String period) {
        String normalized = normalizePeriod(period);
        SalesWindow window = salesWindow(normalized, LocalDate.now());
        Map<String, BigDecimal> totals = new HashMap<>();
        String sql = "SELECT bucket, COALESCE(SUM(delta), 0) AS total FROM ("
                + " SELECT " + bucket(normalized, "i.created_at")
                + " AS bucket, i.subtotal AS delta FROM invoices i"
                + " WHERE i.status NOT IN ('CANCELLED', 'DRAFT')"
                + " AND i.created_at >= ? AND i.created_at < ?"
                + " UNION ALL SELECT " + bucket(normalized, "cn.created_at")
                + " AS bucket, -cn.amount AS delta FROM credit_notes cn"
                + " WHERE cn.created_at >= ? AND cn.created_at < ?"
                + " UNION ALL SELECT " + bucket(normalized, "r.created_at")
                + " AS bucket, -r.total AS delta FROM returns r"
                + " WHERE r.created_at >= ? AND r.created_at < ?"
                + " UNION ALL SELECT " + bucket(normalized, "dn.created_at")
                + " AS bucket, dn.amount AS delta FROM debit_notes dn"
                + " WHERE dn.created_at >= ? AND dn.created_at < ?"
                + ") t GROUP BY bucket";
        jdbc.query(sql,
                (org.springframework.jdbc.core.RowCallbackHandler) rs -> totals
                        .put(rs.getString("bucket"),
                                rs.getBigDecimal("total")),
                window.start().atStartOfDay(), window.end().atStartOfDay(),
                window.start().atStartOfDay(), window.end().atStartOfDay(),
                window.start().atStartOfDay(), window.end().atStartOfDay(),
                window.start().atStartOfDay(), window.end().atStartOfDay());
        List<MonthlySalesResponse> series = new ArrayList<>();
        for (String bucket : window.buckets()) {
            series.add(new MonthlySalesResponse(bucket,
                    totals.getOrDefault(bucket, BigDecimal.ZERO)));
        }
        return series;
    }

    private String bucket(String period, String column) {
        return switch (period) {
            case "DAY" -> "DATE_FORMAT(" + column + ", '%Y-%m-%d')";
            case "WEEK" -> "CONCAT(DATE_FORMAT(" + column
                    + ", '%Y-%m'), '-W', LEAST(CEIL(DAY(" + column
                    + ") / 7), 4))";
            case "YEAR" -> "DATE_FORMAT(" + column + ", '%Y')";
            default -> "DATE_FORMAT(" + column + ", '%Y-%m')";
        };
    }

    private record SalesWindow(List<String> buckets, LocalDate start,
            LocalDate end) {
    }

    private SalesWindow salesWindow(String period, LocalDate today) {
        List<String> buckets = new ArrayList<>();
        return switch (period) {
            case "DAY" -> {
                LocalDate start = today.with(java.time.DayOfWeek.MONDAY);
                for (int index = 0; index < 7; index++) {
                    buckets.add(start.plusDays(index).toString());
                }
                yield new SalesWindow(buckets, start, start.plusDays(7));
            }
            case "WEEK" -> {
                int year = today.getYear();
                int month = today.getMonthValue();
                for (int week = 1; week <= 4; week++) {
                    buckets.add(String.format("%d-%02d-W%d", year, month,
                            week));
                }
                LocalDate start = today.withDayOfMonth(1);
                yield new SalesWindow(buckets, start, start.plusMonths(1));
            }
            case "YEAR" -> {
                int currentYear = today.getYear();
                java.util.TreeSet<Integer> years = new java.util.TreeSet<>();
                jdbc.query("SELECT DISTINCT YEAR(created_at) FROM invoices "
                        + "WHERE status NOT IN ('CANCELLED', 'DRAFT')",
                        rs -> {
                            years.add(rs.getInt(1));
                        });
                years.add(currentYear);
                years.add(currentYear - 1);
                years.forEach(year -> buckets.add(String.valueOf(year)));
                yield new SalesWindow(buckets,
                        LocalDate.of(years.first(), 1, 1),
                        LocalDate.of(currentYear + 1, 1, 1));
            }
            default -> {
                YearMonth firstMonth = YearMonth.from(today).minusMonths(11);
                for (int index = 0; index < 12; index++) {
                    buckets.add(firstMonth.plusMonths(index).toString());
                }
                yield new SalesWindow(buckets, firstMonth.atDay(1),
                        YearMonth.from(today).plusMonths(1).atDay(1));
            }
        };
    }

    public List<TopItemResponse> getTopProducts(int limit) {
        return jdbc.query(
                "SELECT p.id, p.name, COALESCE(SUM(il.quantity * il.unit_price), 0) AS total "
                        + "FROM invoice_lines il JOIN products p ON p.id = il.product_id "
                        + "JOIN invoices i ON i.id = il.invoice_id "
                        + "WHERE i.status NOT IN ('CANCELLED', 'DRAFT') "
                        + "GROUP BY p.id, p.name ORDER BY total DESC LIMIT ?",
                (rs, rowNum) -> new TopItemResponse(rs.getString("id"),
                        rs.getString("name"), rs.getBigDecimal("total")),
                limit);
    }

    public List<TopItemResponse> getTopCustomers(int limit) {
        return jdbc.query(
                "SELECT c.id, c.legal_name AS name, COALESCE(SUM(i.subtotal), 0) AS total "
                        + "FROM invoices i JOIN customers c ON c.id = i.customer_id "
                        + "WHERE i.status NOT IN ('CANCELLED', 'DRAFT') "
                        + "GROUP BY c.id, c.legal_name ORDER BY total DESC LIMIT ?",
                (rs, rowNum) -> new TopItemResponse(rs.getString("id"),
                        rs.getString("name"), rs.getBigDecimal("total")),
                limit);
    }

    public List<Map<String, Object>> getRecentActivity() {
        return jdbc.queryForList(
                "(SELECT 'invoice' AS type, i.id, i.invoice_number AS number, c.legal_name AS customer, i.subtotal AS amount, i.created_at "
                        + "FROM invoices i JOIN customers c ON c.id = i.customer_id ORDER BY i.created_at DESC LIMIT 5) "
                        + "UNION ALL "
                        + "(SELECT 'order' AS type, o.id, o.order_number AS number, c.legal_name AS customer, o.subtotal AS amount, o.created_at "
                        + "FROM orders o JOIN customers c ON c.id = o.customer_id ORDER BY o.created_at DESC LIMIT 5) "
                        + "ORDER BY created_at DESC LIMIT 10");
    }

    private long countOutstandingInvoices(String dueComparator) {
        String balanceExpression = "(i.subtotal - COALESCE(i.returned_total, 0) "
                + "- COALESCE(pa.total_paid, 0) - COALESCE(cn.total_credit, 0) "
                + "+ COALESCE(dn.total_debit, 0))";
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ("
                        + "SELECT i.id, " + balanceExpression + " AS balance "
                        + "FROM invoices i "
                        + "LEFT JOIN (SELECT invoice_id, SUM(allocated_amount) AS total_paid FROM payment_allocations GROUP BY invoice_id) pa ON pa.invoice_id = i.id "
                        + "LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_credit FROM credit_notes GROUP BY invoice_id) cn ON cn.invoice_id = i.id "
                        + "LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_debit FROM debit_notes GROUP BY invoice_id) dn ON dn.invoice_id = i.id "
                        + "WHERE i.status IN ('PENDING_PAYMENT', 'PARTIALLY_PAID') "
                        + "AND i.due_date " + dueComparator + " CURDATE()"
                        + ") t WHERE t.balance > 0",
                Long.class);
        return count != null ? count : 0;
    }

    public List<InvoiceDueResponse> getInvoiceDueSchedule() {
        return getInvoiceDueSchedule("ALL");
    }

    public List<InvoiceDueResponse> getInvoiceDueSchedule(String scope) {
        String normalized = scope == null ? "ALL"
                : scope.trim().toUpperCase(Locale.ROOT);
        String dueFilter = switch (normalized) {
            case "UPCOMING" -> " AND i.due_date >= CURDATE() ";
            case "OVERDUE" -> " AND i.due_date < CURDATE() ";
            default -> "";
        };
        String balanceExpression = "(i.subtotal - COALESCE(i.returned_total, 0) "
                + "- COALESCE(pa.total_paid, 0) - COALESCE(cn.total_credit, 0) "
                + "+ COALESCE(dn.total_debit, 0))";

        return jdbc.query(
                "SELECT BIN_TO_UUID(i.id) AS invoice_id, i.invoice_number, BIN_TO_UUID(c.id) AS customer_id, c.legal_name, "
                        + "i.due_date, " + balanceExpression + " AS balance "
                        + "FROM invoices i "
                        + "JOIN customers c ON c.id = i.customer_id "
                        + "LEFT JOIN (SELECT invoice_id, SUM(allocated_amount) AS total_paid FROM payment_allocations GROUP BY invoice_id) pa ON pa.invoice_id = i.id "
                        + "LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_credit FROM credit_notes GROUP BY invoice_id) cn ON cn.invoice_id = i.id "
                        + "LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_debit FROM debit_notes GROUP BY invoice_id) dn ON dn.invoice_id = i.id "
                        + "WHERE i.status IN ('PENDING_PAYMENT', 'PARTIALLY_PAID') "
                        + "AND " + balanceExpression + " > 0 "
                        + dueFilter
                        + "ORDER BY due_date ASC, c.legal_name ASC, i.invoice_number ASC "
                        + "LIMIT 10",
                (rs, rowNum) -> new InvoiceDueResponse(
                        UUID.fromString(rs.getString("invoice_id")),
                        rs.getString("invoice_number"),
                        UUID.fromString(rs.getString("customer_id")),
                        rs.getString("legal_name"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getBigDecimal("balance")));
    }
}
