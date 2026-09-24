package com.parsun.parsun_api.dashboard.application.usecases;

import com.parsun.parsun_api.dashboard.application.dto.response.DashboardSummaryResponse;
import com.parsun.parsun_api.dashboard.application.dto.response.InvoiceDueResponse;
import com.parsun.parsun_api.dashboard.application.dto.response.MonthlySalesResponse;
import com.parsun.parsun_api.dashboard.application.dto.response.TopItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GetDashboardUseCase {

    private final JdbcTemplate jdbc;

    public GetDashboardUseCase(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public DashboardSummaryResponse getSummary() {
        BigDecimal monthlySales = jdbc.queryForObject(
            "SELECT COALESCE(SUM(subtotal), 0) FROM invoices WHERE status NOT IN ('CANCELLED', 'DRAFT') AND MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())",
            BigDecimal.class
        );

        long pendingOrders = jdbc.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE status IN ('PENDING', 'CONFIRMED')",
            Long.class
        );

        BigDecimal overdueReceivables = jdbc.queryForObject(
            "SELECT COALESCE(SUM(balance), 0) FROM (" +
            "  SELECT i.id, (i.subtotal - COALESCE(i.returned_total, 0)" +
            "    - COALESCE(pa.total_paid, 0) - COALESCE(cn.total_credit, 0)" +
            "    + COALESCE(dn.total_debit, 0)) AS balance" +
            "  FROM invoices i" +
            "  LEFT JOIN (SELECT invoice_id, SUM(allocated_amount) AS total_paid FROM payment_allocations GROUP BY invoice_id) pa ON pa.invoice_id = i.id" +
            "  LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_credit FROM credit_notes GROUP BY invoice_id) cn ON cn.invoice_id = i.id" +
            "  LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_debit FROM debit_notes GROUP BY invoice_id) dn ON dn.invoice_id = i.id" +
            "  WHERE i.status IN ('PENDING_PAYMENT', 'PARTIALLY_PAID')" +
            ") t WHERE t.balance > 0",
            BigDecimal.class
        );

        long lowStockProducts = jdbc.queryForObject(
                "SELECT COUNT(DISTINCT ws.product_id) FROM warehouse_stock ws WHERE ws.quantity <= 5 AND ws.quantity > 0",
            Long.class
        );

        return new DashboardSummaryResponse(
            monthlySales != null ? monthlySales : BigDecimal.ZERO,
            pendingOrders,
            overdueReceivables != null ? overdueReceivables : BigDecimal.ZERO,
            lowStockProducts
        );
    }

    public List<MonthlySalesResponse> getMonthlySales() {
        return jdbc.query(
            "SELECT months.month, COALESCE(SUM(i.subtotal), 0) AS total " +
            "FROM (" +
            "  SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m') AS month UNION ALL " +
            "  SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m') UNION ALL " +
            "  SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m') UNION ALL " +
            "  SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m') UNION ALL " +
            "  SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m') UNION ALL " +
            "  SELECT DATE_FORMAT(CURDATE(), '%Y-%m')" +
            ") months " +
            "LEFT JOIN invoices i ON DATE_FORMAT(i.created_at, '%Y-%m') = months.month " +
            "AND i.status NOT IN ('CANCELLED', 'DRAFT') " +
            "GROUP BY months.month ORDER BY months.month",
            (rs, rowNum) -> new MonthlySalesResponse(
                rs.getString("month"),
                rs.getBigDecimal("total")
            )
        );
    }

    public List<TopItemResponse> getTopProducts(int limit) {
        return jdbc.query(
            "SELECT p.id, p.name, COALESCE(SUM(il.quantity * il.unit_price), 0) AS total " +
            "FROM invoice_lines il JOIN products p ON p.id = il.product_id " +
            "JOIN invoices i ON i.id = il.invoice_id " +
            "WHERE i.status NOT IN ('CANCELLED', 'DRAFT') " +
            "GROUP BY p.id, p.name ORDER BY total DESC LIMIT ?",
            (rs, rowNum) -> new TopItemResponse(
                rs.getString("id"),
                rs.getString("name"),
                rs.getBigDecimal("total")
            ),
            limit
        );
    }

    public List<TopItemResponse> getTopCustomers(int limit) {
        return jdbc.query(
            "SELECT c.id, c.legal_name AS name, COALESCE(SUM(i.subtotal), 0) AS total " +
            "FROM invoices i JOIN customers c ON c.id = i.customer_id " +
            "WHERE i.status NOT IN ('CANCELLED', 'DRAFT') " +
            "GROUP BY c.id, c.legal_name ORDER BY total DESC LIMIT ?",
            (rs, rowNum) -> new TopItemResponse(
                rs.getString("id"),
                rs.getString("name"),
                rs.getBigDecimal("total")
            ),
            limit
        );
    }

    public List<Map<String, Object>> getRecentActivity() {
        return jdbc.queryForList(
            "(SELECT 'invoice' AS type, i.id, i.invoice_number AS number, c.legal_name AS customer, i.subtotal AS amount, i.created_at " +
            "FROM invoices i JOIN customers c ON c.id = i.customer_id ORDER BY i.created_at DESC LIMIT 5) " +
            "UNION ALL " +
            "(SELECT 'order' AS type, o.id, o.order_number AS number, c.legal_name AS customer, o.subtotal AS amount, o.created_at " +
            "FROM orders o JOIN customers c ON c.id = o.customer_id ORDER BY o.created_at DESC LIMIT 5) " +
            "ORDER BY created_at DESC LIMIT 10"
        );
    }

    public List<InvoiceDueResponse> getInvoiceDueSchedule() {
        String balanceExpression = "(i.subtotal - COALESCE(i.returned_total, 0) "
                + "- COALESCE(pa.total_paid, 0) - COALESCE(cn.total_credit, 0) "
                + "+ COALESCE(dn.total_debit, 0))";

        return jdbc.query(
            "SELECT BIN_TO_UUID(i.id) AS invoice_id, i.invoice_number, BIN_TO_UUID(c.id) AS customer_id, c.legal_name, "
                        + "DATE_ADD(DATE(i.created_at), INTERVAL c.credit_days DAY) AS due_date, "
                        + balanceExpression + " AS balance "
                        + "FROM invoices i "
                        + "JOIN customers c ON c.id = i.customer_id "
                        + "LEFT JOIN (SELECT invoice_id, SUM(allocated_amount) AS total_paid FROM payment_allocations GROUP BY invoice_id) pa ON pa.invoice_id = i.id "
                        + "LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_credit FROM credit_notes GROUP BY invoice_id) cn ON cn.invoice_id = i.id "
                        + "LEFT JOIN (SELECT invoice_id, SUM(amount) AS total_debit FROM debit_notes GROUP BY invoice_id) dn ON dn.invoice_id = i.id "
                        + "WHERE i.status IN ('PENDING_PAYMENT', 'PARTIALLY_PAID') "
                        + "AND " + balanceExpression + " > 0 "
                        + "ORDER BY due_date ASC, c.legal_name ASC, i.invoice_number ASC",
                (rs, rowNum) -> new InvoiceDueResponse(
                    UUID.fromString(rs.getString("invoice_id")),
                        rs.getString("invoice_number"),
                        UUID.fromString(rs.getString("customer_id")),
                        rs.getString("legal_name"),
                        rs.getDate("due_date").toLocalDate(),
                        rs.getBigDecimal("balance")
                )
        );
    }
}
