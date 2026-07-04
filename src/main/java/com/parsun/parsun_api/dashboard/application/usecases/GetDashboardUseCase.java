package com.parsun.parsun_api.dashboard.application.usecases;

import com.parsun.parsun_api.dashboard.application.dto.response.DashboardSummaryResponse;
import com.parsun.parsun_api.dashboard.application.dto.response.MonthlySalesResponse;
import com.parsun.parsun_api.dashboard.application.dto.response.TopItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
            "SELECT COALESCE(SUM(subtotal), 0) FROM invoices WHERE status IN ('PENDING_PAYMENT', 'PARTIALLY_PAID')",
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
            "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COALESCE(SUM(subtotal), 0) AS total " +
            "FROM invoices WHERE status NOT IN ('CANCELLED', 'DRAFT') " +
            "AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m') ORDER BY month",
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
}
