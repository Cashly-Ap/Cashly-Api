package com.cashly.cashly_api.sales.orders.infrastructure.integration_tests;

import com.cashly.cashly_api.sales.orders.infrastructure.persistence.repositories.OrderJpaRepository;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.repositories.QuoteJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class OrderHistoricalCategoryMySqlIT {
    private final JdbcTemplate jdbc;
    private final OrderJpaRepository orders;
    private final QuoteJpaRepository quotes;

    @Autowired
    OrderHistoricalCategoryMySqlIT(JdbcTemplate jdbc, OrderJpaRepository orders, QuoteJpaRepository quotes) {
        this.jdbc = jdbc;
        this.orders = orders;
        this.quotes = quotes;
    }

    @Test
    void filtersOrdersByHistoricalOrCurrentProductCategory() {
        UUID customerId = UUID.randomUUID();
        UUID oldCategoryId = UUID.randomUUID();
        UUID currentCategoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        insertCustomer(customerId);
        insertCategory(oldCategoryId, "Historical");
        insertCategory(currentCategoryId, "Current");
        insertProduct(productId, currentCategoryId);
        insertOrder(orderId, customerId);
        insertLine(lineId, orderId, productId);
        jdbc.update("insert into order_line_category_snapshots (order_line_id, category_id) values (UUID_TO_BIN(?), UUID_TO_BIN(?))",
                lineId.toString(), oldCategoryId.toString());

        var historicalMatches = orders.findAllWithFilters(null, null, null, null, null, oldCategoryId,
                Set.of(new UUID(0, 0)));
        var currentMatches = orders.findAllWithFilters(null, null, null, null, null, currentCategoryId,
                Set.of(productId));

        assertThat(historicalMatches).extracting(order -> order.getId()).containsExactly(orderId);
        assertThat(currentMatches).extracting(order -> order.getId()).containsExactly(orderId);
    }

    @Test
    void legacyLineWithoutSnapshotStillMatchesCurrentCategory() {
        UUID customerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        insertCustomer(customerId);
        insertCategory(categoryId, "Legacy current");
        insertProduct(productId, categoryId);
        insertOrder(orderId, customerId);
        insertLine(UUID.randomUUID(), orderId, productId);

        var currentMatches = orders.findAllWithFilters(null, null, null, null, null, categoryId, Set.of(productId));

        assertThat(currentMatches).extracting(order -> order.getId()).containsExactly(orderId);
    }

    @Test
    void migrationBackfillsExistingOrderAndQuoteLinesBeforeProductsChangeCategory() throws IOException {
        UUID customerId = UUID.randomUUID();
        UUID historicalCategoryId = UUID.randomUUID();
        UUID currentCategoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID orderLineId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        UUID quoteLineId = UUID.randomUUID();
        insertCustomer(customerId);
        insertCategory(historicalCategoryId, "Migrated historical");
        insertCategory(currentCategoryId, "Migrated current");
        insertProduct(productId, historicalCategoryId);
        insertOrder(orderId, customerId);
        insertLine(orderLineId, orderId, productId);
        insertQuote(quoteId, customerId);
        insertQuoteLine(quoteLineId, quoteId, productId);

        executeMigrationBackfill("order_line_category_snapshots");
        executeMigrationBackfill("quote_line_category_snapshots");
        jdbc.update("update products set category_id = UUID_TO_BIN(?) where id = UUID_TO_BIN(?)",
                currentCategoryId.toString(), productId.toString());

        var historicalOrders = orders.findAllWithFilters(null, null, null, null, null,
                historicalCategoryId, Set.of(new UUID(0, 0)));
        var historicalQuotes = quotes.findAllWithFilters(null, null, customerId, "ALL", null,
                historicalCategoryId, Set.of(new UUID(0, 0)));

        assertThat(historicalOrders).extracting(order -> order.getId()).containsExactly(orderId);
        assertThat(historicalQuotes).extracting(quote -> quote.getId()).containsExactly(quoteId);
    }

    private void executeMigrationBackfill(String table) throws IOException {
        String migration = Files.readString(Path.of(
                "src/main/resources/db/migration/V25__create_sales_line_category_snapshots.sql"));
        Matcher statement = Pattern.compile("(?is)INSERT\\s+INTO\\s+" + Pattern.quote(table) + "\\s*\\(.*?;")
                .matcher(migration);
        assertThat(statement.find()).as("V25 backfills " + table).isTrue();
        jdbc.execute(statement.group());
    }

    private void insertCustomer(UUID id) {
        jdbc.update("insert into customers (id, code, legal_name, type, rif, alias, credit_days, credit_limit, active, created_at, updated_at) values (UUID_TO_BIN(?), ?, ?, 'COMPANY', ?, ?, 0, 0, true, current_timestamp, current_timestamp)",
                id.toString(), Math.abs(id.hashCode()), "Customer " + id, "RIF-" + id, "Alias " + id);
    }

    private void insertCategory(UUID id, String prefix) {
        jdbc.update("insert into categories (id, name, active) values (UUID_TO_BIN(?), ?, true)",
                id.toString(), prefix + " " + id);
    }

    private void insertProduct(UUID id, UUID categoryId) {
        jdbc.update("insert into products (id, code, name, category_id, composite, active, created_at, updated_at) values (UUID_TO_BIN(?), ?, ?, UUID_TO_BIN(?), false, true, current_timestamp, current_timestamp)",
                id.toString(), "SKU-" + id, "Product " + id, categoryId.toString());
    }

    private void insertOrder(UUID id, UUID customerId) {
        jdbc.update("insert into orders (id, order_number, customer_id, customer_name, customer_rif, status, subtotal, created_at, updated_at) values (UUID_TO_BIN(?), ?, UUID_TO_BIN(?), 'Customer', 'RIF', 'DRAFT', 10, current_timestamp, current_timestamp)",
                id.toString(), "ORD-" + id.toString().replace("-", "").substring(0, 16), customerId.toString());
    }

    private void insertQuote(UUID id, UUID customerId) {
        jdbc.update("insert into quotes (id, quote_number, customer_id, customer_name, customer_rif, status, subtotal, created_at, updated_at) values (UUID_TO_BIN(?), ?, UUID_TO_BIN(?), 'Customer', 'RIF', 'DRAFT', 10, current_timestamp, current_timestamp)",
                id.toString(), "QUO-" + id.toString().replace("-", "").substring(0, 16), customerId.toString());
    }

    private void insertQuoteLine(UUID id, UUID quoteId, UUID productId) {
        jdbc.update("insert into quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total) values (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), 'Product', 'SKU', 1, 10, 10)",
                id.toString(), quoteId.toString(), productId.toString());
    }

    private void insertLine(UUID id, UUID orderId, UUID productId) {
        jdbc.update("insert into order_lines (id, order_id, product_id, product_name, product_code, quantity, unit_price, line_total) values (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), 'Product', 'SKU', 1, 10, 10)",
                id.toString(), orderId.toString(), productId.toString());
    }
}
