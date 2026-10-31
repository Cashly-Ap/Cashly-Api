package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.migrations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationReconciliationMigrationTest {
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:"
                + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa",
                "");
        jdbc = new JdbcTemplate(source);
        jdbc.execute(
                "CREATE TABLE warehouse_stock (id INT PRIMARY KEY, warehouse_id INT, product_id INT, quantity INT, reserved INT, updated_at TIMESTAMP)");
        jdbc.execute(
                "CREATE TABLE orders (id INT PRIMARY KEY, status VARCHAR(20))");
        jdbc.execute(
                "CREATE TABLE order_lines (id INT PRIMARY KEY, order_id INT, product_id INT)");
        jdbc.execute(
                "CREATE TABLE order_line_allocations (id INT PRIMARY KEY, order_line_id INT, warehouse_id INT, allocated DECIMAL(15,4))");
        jdbc.execute(
                "CREATE TABLE invoices (id INT PRIMARY KEY, status VARCHAR(20))");
        jdbc.execute(
                "CREATE TABLE invoice_lines (id INT PRIMARY KEY, invoice_id INT, product_id INT)");
        jdbc.execute(
                "CREATE TABLE invoice_line_allocations (id INT PRIMARY KEY, invoice_line_id INT, warehouse_id INT, allocated DECIMAL(15,4))");
    }

    @Test
    void repairs_reservations_from_active_draft_documents() throws IOException {
        jdbc.update(
                "INSERT INTO warehouse_stock VALUES (1, 10, 20, 100, 1, CURRENT_TIMESTAMP)");
        jdbc.update(
                "INSERT INTO warehouse_stock VALUES (2, 10, 21, 100, 9, CURRENT_TIMESTAMP)");
        jdbc.update(
                "INSERT INTO orders VALUES (100, 'DRAFT'), (101, 'CONFIRMED')");
        jdbc.update(
                "INSERT INTO order_lines VALUES (200, 100, 20), (201, 101, 20)");
        jdbc.update(
                "INSERT INTO order_line_allocations VALUES (300, 200, 10, 3), (301, 201, 10, 7)");
        jdbc.update(
                "INSERT INTO invoices VALUES (400, 'DRAFT'), (401, 'CONFIRMED')");
        jdbc.update(
                "INSERT INTO invoice_lines VALUES (500, 400, 20), (501, 401, 20)");
        jdbc.update(
                "INSERT INTO invoice_line_allocations VALUES (600, 500, 10, 2), (601, 501, 10, 11)");

        jdbc.execute(migrationSql());

        assertThat(reserved(1)).isEqualTo(5);
        assertThat(reserved(2)).isZero();
    }

    private int reserved(int stockId) {
        return jdbc.queryForObject(
                "SELECT reserved FROM warehouse_stock WHERE id = ?",
                Integer.class, stockId);
    }

    private String migrationSql() throws IOException {
        return new ClassPathResource(
                "db/migration/V37__reconcile_active_stock_reservations.sql")
                .getContentAsString(StandardCharsets.UTF_8);
    }
}
