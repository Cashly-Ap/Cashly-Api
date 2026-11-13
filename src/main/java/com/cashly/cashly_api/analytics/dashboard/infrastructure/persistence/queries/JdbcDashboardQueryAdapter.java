package com.cashly.cashly_api.analytics.dashboard.infrastructure.persistence.queries;

import com.cashly.cashly_api.analytics.dashboard.application.dto.response.DashboardSummaryResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.InvoiceDueResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.MonthlySalesResponse;
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
        BigDecimal monthlySales = jdbc.queryForObject(
                "SELECT COALESCE(SUM(subtotal), 0) FROM invoices WHERE status NOT IN ('CANCELLED', 'DRAFT') AND MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())",
                BigDecimal.class);

        long pendingOrders = jdbc.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE status IN ('PENDING', 'CONFIRMED')",
                Long.class);

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
                pendingOrders,
                overdueReceivables != null
                        ? overdueReceivables
                        : BigDecimal.ZERO,
                lowStockProducts);
    }

    public List<MonthlySalesResponse> getMonthlySales() {
        return getSalesTrend("MONTH");
    }

    public List<MonthlySalesResponse> getSalesTrend(String period) {
        String normalized = period == null ? "MONTH"
                : period.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("WEEK", "MONTH", "YEAR").contains(normalized)) {
            normalized = "MONTH";
        }
        LocalDate today = LocalDate.now();
        SalesWindow window = salesWindow(normalized, today);
        Map<String, BigDecimal> totals = new HashMap<>();
        jdbc.query("SELECT " + window.bucketExpression() + " AS bucket, "
                + "COALESCE(SUM(i.subtotal), 0) AS total FROM invoices i "
                + "WHERE i.status NOT IN ('CANCELLED', 'DRAFT') "
                + "AND i.created_at >= ? AND i.created_at < ? GROUP BY bucket",
                rs -> {
                    totals.put(rs.getString("bucket"),
                            rs.getBigDecimal("total"));
                }, window.start(), window.end());
        List<MonthlySalesResponse> series = new ArrayList<>();
        for (String bucket : window.buckets()) {
            series.add(new MonthlySalesResponse(bucket,
                    totals.getOrDefault(bucket, BigDecimal.ZERO)));
        }
        return series;
    }

    private record SalesWindow(List<String> buckets, String bucketExpression,
            LocalDate start, LocalDate end) {
    }

    private SalesWindow salesWindow(String period, LocalDate today) {
        List<String> buckets = new ArrayList<>();
        return switch (period) {
            case "WEEK" -> {
                int year = today.getYear();
                int month = today.getMonthValue();
                for (int week = 1; week <= 4; week++) {
                    buckets.add(String.format("%d-%02d-W%d", year, month,
                            week));
                }
                LocalDate start = today.withDayOfMonth(1);
                yield new SalesWindow(buckets,
                        "CONCAT(DATE_FORMAT(i.created_at, '%Y-%m'), '-W', "
                                + "LEAST(CEIL(DAY(i.created_at) / 7), 4))",
                        start, start.plusMonths(1));
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
                        "DATE_FORMAT(i.created_at, '%Y')",
                        LocalDate.of(years.first(), 1, 1),
                        LocalDate.of(currentYear + 1, 1, 1));
            }
            default -> {
                int year = today.getYear();
                for (int month = 1; month <= 12; month++) {
                    buckets.add(String.format("%d-%02d", year, month));
                }
                yield new SalesWindow(buckets,
                        "DATE_FORMAT(i.created_at, '%Y-%m')",
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year + 1, 1, 1));
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

    public List<InvoiceDueResponse> getInvoiceDueSchedule() {
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
