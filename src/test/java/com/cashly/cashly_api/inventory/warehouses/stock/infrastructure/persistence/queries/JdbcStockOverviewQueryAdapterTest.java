package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.queries;

import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewGroupBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.nio.ByteBuffer;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class JdbcStockOverviewQueryAdapterTest {
    private JdbcTemplate jdbc;
    private JdbcStockOverviewQueryAdapter adapter;
    private final UUID alpha = UUID.randomUUID();
    private final UUID beta = UUID.randomUUID();
    private final UUID fruit = UUID.randomUUID();
    private final UUID hardware = UUID.randomUUID();
    private final UUID apple = UUID.randomUUID();
    private final UUID bolt = UUID.randomUUID();
    private final UUID kit = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:"
                + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa",
                "");
        jdbc = new JdbcTemplate(source);
        adapter = new JdbcStockOverviewQueryAdapter(
                new NamedParameterJdbcTemplate(source));
        jdbc.execute(
                "CREATE TABLE warehouses (id BINARY(16) PRIMARY KEY, name VARCHAR(100))");
        jdbc.execute(
                "CREATE TABLE categories (id BINARY(16) PRIMARY KEY, name VARCHAR(100))");
        jdbc.execute(
                "CREATE TABLE products (id BINARY(16) PRIMARY KEY, code VARCHAR(50), name VARCHAR(200), category_id BINARY(16), composite BOOLEAN)");
        jdbc.execute(
                "CREATE TABLE warehouse_stock (id BINARY(16) PRIMARY KEY, warehouse_id BINARY(16), product_id BINARY(16), quantity INT, reserved INT)");
        jdbc.update("INSERT INTO warehouses VALUES (?, ?), (?, ?)",
                bytes(alpha), "Alpha", bytes(beta), "Beta");
        jdbc.update("INSERT INTO categories VALUES (?, ?), (?, ?)",
                bytes(fruit), "Fruit", bytes(hardware), "Hardware");
        jdbc.update("INSERT INTO products VALUES (?, ?, ?, ?, ?)", bytes(apple),
                "APP", "Apple", bytes(fruit), false);
        jdbc.update("INSERT INTO products VALUES (?, ?, ?, ?, ?)", bytes(bolt),
                "B-100", "Bolt", bytes(hardware), false);
        jdbc.update("INSERT INTO products VALUES (?, ?, ?, ?, ?)", bytes(kit),
                "KIT", "Kit", null, true);
        stock(alpha, apple, 10, 3);
        stock(beta, apple, 20, 5);
        stock(alpha, bolt, 5, 1);
        stock(beta, kit, 0, 0);
    }

    @Test
    void aggregatesAllFilteredRowsBeforePaginationAndCountsDistinctProducts() {
        var response = adapter
                .search(new StockOverviewFilter(null, null, null, 0, 1));
        assertThat(response.stock().content()).hasSize(1);
        assertThat(response.stock().totalElements()).isEqualTo(4);
        assertThat(response.stock().totalPages()).isEqualTo(4);
        assertThat(response.summary().productCount()).isEqualTo(3);
        assertThat(response.summary().warehouseCount()).isEqualTo(2);
        assertThat(response.summary().totalQuantity()).isEqualTo(35);
        assertThat(response.summary().reservedQuantity()).isEqualTo(9);
        assertThat(response.summary().availableQuantity()).isEqualTo(26);
        var row = response.stock().content().getFirst();
        assertThat(row.warehouseName()).isEqualTo("Alpha");
        assertThat(row.productName()).isEqualTo("Apple");
        assertThat(row.reserved()).isEqualTo(3);
        assertThat(row.available()).isEqualTo(7);
        assertThat(row.categoryId()).isEqualTo(fruit.toString());
        var second = adapter
                .search(new StockOverviewFilter(null, null, null, 1, 1));
        assertThat(second.stock().content().getFirst().productName())
                .isEqualTo("Bolt");
        assertThat(second.summary()).isEqualTo(response.summary());
    }

    @Test
    void appliesWarehouseAndCategoryFiltersToCountRowsAndSummary() {
        var response = adapter
                .search(new StockOverviewFilter(beta, fruit, null, 0, 10));
        assertThat(response.stock().totalElements()).isEqualTo(1);
        assertThat(response.stock().content().getFirst().warehouseId())
                .isEqualTo(beta.toString());
        assertThat(response.summary().productCount()).isEqualTo(1);
        assertThat(response.summary().warehouseCount()).isEqualTo(1);
        assertThat(response.summary().totalQuantity()).isEqualTo(20);
        assertThat(response.summary().reservedQuantity()).isEqualTo(5);
        assertThat(response.summary().availableQuantity()).isEqualTo(15);
        assertThat(response.warehouses()).hasSize(2);
        assertThat(response.categories()).hasSize(2);
    }

    @Test
    void searchesNamesCodesAndWarehousesOnTheServer() {
        assertThat(adapter
                .search(new StockOverviewFilter(null, null, "b-100", 0, 10))
                .stock().totalElements()).isEqualTo(1);
        assertThat(adapter
                .search(new StockOverviewFilter(null, null, "apple", 0, 10))
                .summary().totalQuantity()).isEqualTo(30);
        assertThat(adapter
                .search(new StockOverviewFilter(null, null, "  BETA  ", 0, 10))
                .stock().totalElements()).isEqualTo(2);
    }

    @Test
    void retainsCompositeZeroStockAndNullableCategory() {
        var response = adapter
                .search(new StockOverviewFilter(null, null, "KIT", 0, 10));
        var row = response.stock().content().getFirst();
        assertThat(row.composite()).isTrue();
        assertThat(row.categoryId()).isNull();
        assertThat(row.categoryName()).isNull();
        assertThat(row.quantity()).isZero();
        assertThat(row.reserved()).isZero();
        assertThat(row.available()).isZero();
        assertThat(response.summary().productCount()).isEqualTo(1);
    }

    @Test
    void handlesEmptyAndOutOfRangePagesWithoutLosingSummaryOrOptions() {
        var empty = adapter
                .search(new StockOverviewFilter(null, null, "unknown", 0, 10));
        assertThat(empty.stock().empty()).isTrue();
        assertThat(empty.stock().totalElements()).isZero();
        assertThat(empty.summary().totalQuantity()).isZero();
        assertThat(empty.summary().availableQuantity()).isZero();
        assertThat(empty.warehouses()).hasSize(2);
        var beyond = adapter
                .search(new StockOverviewFilter(null, null, null, 10, 10));
        assertThat(beyond.stock().content()).isEmpty();
        assertThat(beyond.stock().totalElements()).isEqualTo(4);
        assertThat(beyond.summary().totalQuantity()).isEqualTo(35);
    }

    @Test
    void treatsSearchWildcardsAsLiteralCharacters() {
        UUID percent = UUID.randomUUID();
        jdbc.update("INSERT INTO products VALUES (?, ?, ?, ?, ?)",
                bytes(percent), "PCT", "100% item", bytes(fruit), false);
        stock(alpha, percent, 2, 1);
        var response = adapter
                .search(new StockOverviewFilter(null, null, "%", 0, 10));
        assertThat(response.stock().totalElements()).isEqualTo(1);
        assertThat(response.stock().content().getFirst().productName())
                .isEqualTo("100% item");
        assertThat(adapter.search(
                new StockOverviewFilter(null, null, "' OR 1=1 --", 0, 10))
                .stock().content()).isEmpty();
    }

    @Test
    void paginatesProductsWithoutSplittingTheirWarehouseStock() {
        var first = adapter.search(new StockOverviewFilter(null, null, null, 0,
                1, StockOverviewGroupBy.PRODUCT));
        assertThat(first.stock()).isNull();
        assertThat(first.groupBy()).isEqualTo(StockOverviewGroupBy.PRODUCT);
        assertThat(first.groups().totalElements()).isEqualTo(3);
        assertThat(first.groups().totalPages()).isEqualTo(3);
        var product = first.groups().content().getFirst();
        assertThat(product.id()).isEqualTo(apple.toString());
        assertThat(product.productCode()).isEqualTo("APP");
        assertThat(product.categoryName()).isEqualTo("Fruit");
        assertThat(product.items()).hasSize(2);
        assertThat(product.items()).extracting(item -> item.warehouseId())
                .containsExactly(alpha.toString(), beta.toString());
        assertThat(product.quantity()).isEqualTo(30);
        assertThat(product.reserved()).isEqualTo(8);
        assertThat(product.available()).isEqualTo(22);
        assertThat(product.productCount()).isEqualTo(1);
        assertThat(product.warehouseCount()).isEqualTo(2);
        var second = adapter.search(new StockOverviewFilter(null, null, null, 1,
                1, StockOverviewGroupBy.PRODUCT));
        assertThat(second.groups().content()).extracting(group -> group.id())
                .containsExactly(bolt.toString());
        assertThat(second.groups().content().getFirst().items()).hasSize(1);
        assertThat(second.summary()).isEqualTo(first.summary());
        assertThat(first.summary().totalQuantity()).isEqualTo(35);
    }

    @Test
    void paginatesWarehousesWithAllTheirMatchingProducts() {
        var first = adapter.search(new StockOverviewFilter(null, null, null, 0,
                1, StockOverviewGroupBy.WAREHOUSE));
        assertThat(first.groups().totalElements()).isEqualTo(2);
        assertThat(first.groups().totalPages()).isEqualTo(2);
        var warehouse = first.groups().content().getFirst();
        assertThat(warehouse.id()).isEqualTo(alpha.toString());
        assertThat(warehouse.name()).isEqualTo("Alpha");
        assertThat(warehouse.items()).extracting(item -> item.productId())
                .containsExactly(apple.toString(), bolt.toString());
        assertThat(warehouse.quantity()).isEqualTo(15);
        assertThat(warehouse.reserved()).isEqualTo(4);
        assertThat(warehouse.available()).isEqualTo(11);
        assertThat(warehouse.productCount()).isEqualTo(2);
        assertThat(warehouse.warehouseCount()).isEqualTo(1);
        var second = adapter.search(new StockOverviewFilter(null, null, null, 1,
                1, StockOverviewGroupBy.WAREHOUSE));
        assertThat(second.groups().content().getFirst().items())
                .extracting(item -> item.productId())
                .containsExactly(apple.toString(), kit.toString());
        assertThat(second.groups().last()).isTrue();
        assertThat(second.summary()).isEqualTo(first.summary());
    }

    @Test
    void appliesEveryFilterToGroupCountsChildrenAndCompleteSummary() {
        var products = adapter.search(new StockOverviewFilter(beta, fruit,
                "APP", 0, 1, StockOverviewGroupBy.PRODUCT));
        assertThat(products.groups().totalElements()).isEqualTo(1);
        var product = products.groups().content().getFirst();
        assertThat(product.items()).hasSize(1);
        assertThat(product.items().getFirst().warehouseId())
                .isEqualTo(beta.toString());
        assertThat(product.quantity()).isEqualTo(20);
        assertThat(products.summary().totalQuantity()).isEqualTo(20);
        var warehouses = adapter.search(new StockOverviewFilter(null, fruit,
                "apple", 0, 1, StockOverviewGroupBy.WAREHOUSE));
        assertThat(warehouses.groups().totalElements()).isEqualTo(2);
        assertThat(warehouses.groups().content().getFirst().items()).hasSize(1);
        assertThat(warehouses.summary().totalQuantity()).isEqualTo(30);
        assertThat(warehouses.summary().productCount()).isEqualTo(1);
        assertThat(warehouses.summary().warehouseCount()).isEqualTo(2);
    }

    @Test
    void returnsEmptyGroupsForMissingAndOutOfRangeResults() {
        var empty = adapter.search(new StockOverviewFilter(null, null,
                "missing", 0, 1, StockOverviewGroupBy.PRODUCT));
        assertThat(empty.groups().content()).isEmpty();
        assertThat(empty.groups().totalElements()).isZero();
        assertThat(empty.summary().totalQuantity()).isZero();
        var beyond = adapter.search(new StockOverviewFilter(null, null, null, 3,
                1, StockOverviewGroupBy.WAREHOUSE));
        assertThat(beyond.groups().content()).isEmpty();
        assertThat(beyond.groups().totalElements()).isEqualTo(2);
        assertThat(beyond.summary().totalQuantity()).isEqualTo(35);
    }

    @Test
    void defaultsToTheGlobalUngroupedView() {
        var response = adapter
                .search(new StockOverviewFilter(null, null, null, 0, 10));
        assertThat(response.groupBy()).isEqualTo(StockOverviewGroupBy.NONE);
        assertThat(response.stock()).isNotNull();
        assertThat(response.groups()).isNull();
        assertThat(response.stock().totalElements()).isEqualTo(4);
    }

    @Test
    void keepsTheGlobalSummaryAcrossEveryGroupingMode() {
        var ungrouped = adapter
                .search(new StockOverviewFilter(null, null, null, 0, 10))
                .summary();
        var byProduct = adapter.search(new StockOverviewFilter(null, null, null,
                0, 10, StockOverviewGroupBy.PRODUCT)).summary();
        var byWarehouse = adapter.search(new StockOverviewFilter(null, null,
                null, 0, 10, StockOverviewGroupBy.WAREHOUSE)).summary();
        assertThat(byProduct).isEqualTo(ungrouped);
        assertThat(byWarehouse).isEqualTo(ungrouped);
        assertThat(ungrouped.totalQuantity()).isEqualTo(35);
    }

    @Test
    void includesCompletePaginationMetadataForUngroupedRows() {
        var first = adapter
                .search(new StockOverviewFilter(null, null, null, 0, 2)).stock();
        assertThat(first.totalElements()).isEqualTo(4);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.size()).isEqualTo(2);
        assertThat(first.number()).isZero();
        assertThat(first.first()).isTrue();
        assertThat(first.last()).isFalse();
        assertThat(first.empty()).isFalse();
        var last = adapter
                .search(new StockOverviewFilter(null, null, null, 1, 2)).stock();
        assertThat(last.content()).hasSize(2);
        assertThat(last.number()).isEqualTo(1);
        assertThat(last.first()).isFalse();
        assertThat(last.last()).isTrue();
    }

    @Test
    void treatsWhitespaceOnlySearchAsNoFilter() {
        var response = adapter
                .search(new StockOverviewFilter(null, null, "   ", 0, 10));
        assertThat(response.stock().totalElements()).isEqualTo(4);
        assertThat(response.summary().totalQuantity()).isEqualTo(35);
    }

    @Test
    void treatsUnderscoreWildcardsAsLiteralCharacters() {
        UUID underscore = UUID.randomUUID();
        jdbc.update("INSERT INTO products VALUES (?, ?, ?, ?, ?)",
                bytes(underscore), "A_C", "A_C item", bytes(fruit), false);
        stock(alpha, underscore, 1, 0);
        var response = adapter
                .search(new StockOverviewFilter(null, null, "_", 0, 10));
        assertThat(response.stock().totalElements()).isEqualTo(1);
        assertThat(response.stock().content().getFirst().productName())
                .isEqualTo("A_C item");
    }

    private void stock(UUID warehouse, UUID product, int quantity,
            int reserved) {
        jdbc.update("INSERT INTO warehouse_stock VALUES (?, ?, ?, ?, ?)",
                bytes(UUID.randomUUID()), bytes(warehouse), bytes(product),
                quantity, reserved);
    }

    private static byte[] bytes(UUID value) {
        return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits())
                .putLong(value.getLeastSignificantBits()).array();
    }
}
