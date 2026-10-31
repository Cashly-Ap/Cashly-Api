package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.queries;

import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementGroupBy;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementGroup;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcStockMovementQueryAdapterTest {

    private JdbcTemplate jdbc;
    private JdbcStockMovementQueryAdapter adapter;
    private final UUID alpha = UUID.randomUUID();
    private final UUID beta = UUID.randomUUID();
    private final UUID apple = UUID.randomUUID();
    private final UUID bolt = UUID.randomUUID();
    private final UUID motors = UUID.randomUUID();
    private final UUID hardware = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:"
                + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa",
                "");
        jdbc = new JdbcTemplate(source);
        adapter = new JdbcStockMovementQueryAdapter(
                new NamedParameterJdbcTemplate(source));
        jdbc.execute(
                "CREATE TABLE warehouses (id BINARY(16) PRIMARY KEY, name VARCHAR(100))");
        jdbc.execute(
                "CREATE TABLE categories (id BINARY(16) PRIMARY KEY, name VARCHAR(100))");
        jdbc.execute("""
                CREATE TABLE products (
                    id BINARY(16) PRIMARY KEY,
                    code VARCHAR(50),
                    name VARCHAR(200),
                    category_id BINARY(16)
                )
                """);
        jdbc.execute("""
                CREATE TABLE stock_movements (
                    id BINARY(16) PRIMARY KEY,
                    warehouse_id BINARY(16),
                    product_id BINARY(16),
                    movement_type VARCHAR(30),
                    quantity INT,
                    previous_stock INT,
                    new_stock INT,
                    origin_movement_id BINARY(16),
                    transfer_batch_id VARCHAR(50),
                    reference_type VARCHAR(20),
                    reference_id BINARY(16),
                    reference_number VARCHAR(100),
                    notes VARCHAR(500),
                    created_at TIMESTAMP
                )
                """);
        jdbc.update("INSERT INTO warehouses VALUES (?, ?), (?, ?)",
                bytes(alpha), "Alpha", bytes(beta), "Beta");
        jdbc.update("INSERT INTO categories VALUES (?, ?), (?, ?)",
                bytes(motors), "Motors", bytes(hardware), "Hardware");
        jdbc.update("INSERT INTO products VALUES (?, ?, ?, ?), (?, ?, ?, ?)",
                bytes(apple), "APP", "Apple", bytes(motors), bytes(bolt),
                "B-100", "Bolt", bytes(hardware));
        movement(alpha, apple, MovementType.ENTRY, 10, 0, 10,
                LocalDateTime.of(2026, 1, 1, 10, 0));
        movement(beta, apple, MovementType.EXIT, 4, 10, 6,
                LocalDateTime.of(2026, 1, 2, 10, 0));
        movement(alpha, bolt, MovementType.ENTRY, 7, 0, 7,
                LocalDateTime.of(2026, 1, 3, 10, 0));
        movement(alpha, apple, MovementType.ADJUSTMENT, 2, 10, 12,
                LocalDateTime.of(2026, 1, 4, 10, 0));
    }

    @Test
    void loadsGlobalMovementsOrderedNewestFirstWithCorrectPagination() {
        StockMovementsResponse response = adapter
                .search(new StockMovementFilter(null, null, null, null, 0, 2,
                        StockMovementGroupBy.NONE));

        assertThat(response.groupBy()).isEqualTo(StockMovementGroupBy.NONE);
        assertThat(response.groups()).isNull();
        assertThat(response.movements().content()).hasSize(2);
        assertThat(response.movements().totalElements()).isEqualTo(4);
        assertThat(response.movements().totalPages()).isEqualTo(2);
        assertThat(response.movements().number()).isZero();
        assertThat(response.movements().first()).isTrue();
        assertThat(response.movements().last()).isFalse();
        assertThat(response.movements().content()).extracting(
                movement -> movement.movementType()).containsExactly(
                        MovementType.ADJUSTMENT, MovementType.ENTRY);
        assertThat(response.movements().content().getFirst().warehouseName())
                .isEqualTo("Alpha");
        assertThat(response.movements().content().getFirst().categoryName())
                .isEqualTo("Motors");

        StockMovementsResponse second = adapter
                .search(new StockMovementFilter(null, null, null, null, 1, 2,
                        StockMovementGroupBy.NONE));

        assertThat(second.movements().content()).hasSize(2);
        assertThat(second.movements().content()).extracting(
                movement -> movement.movementType()).containsExactly(
                        MovementType.EXIT, MovementType.ENTRY);
        assertThat(second.movements().last()).isTrue();
    }

    @Test
    void summarizesEveryMatchingMovementWithEntryExitAndNetQuantities() {
        StockMovementsResponse response = adapter
                .search(new StockMovementFilter(null, null, null, null, 0, 20,
                        StockMovementGroupBy.NONE));

        assertThat(response.summary().movementCount()).isEqualTo(4);
        assertThat(response.summary().productCount()).isEqualTo(2);
        assertThat(response.summary().warehouseCount()).isEqualTo(2);
        assertThat(response.summary().entryQuantity()).isEqualTo(17);
        assertThat(response.summary().exitQuantity()).isEqualTo(4);
        assertThat(response.summary().netQuantity()).isEqualTo(13);

        StockMovementsResponse filtered = adapter
                .search(new StockMovementFilter(alpha, null, null, null, 0, 20,
                        StockMovementGroupBy.NONE));

        assertThat(filtered.summary().movementCount()).isEqualTo(3);
        assertThat(filtered.summary().productCount()).isEqualTo(2);
        assertThat(filtered.summary().warehouseCount()).isEqualTo(1);
        assertThat(filtered.summary().entryQuantity()).isEqualTo(17);
        assertThat(filtered.summary().exitQuantity()).isZero();
        assertThat(filtered.summary().netQuantity()).isEqualTo(17);
    }

    @Test
    void publishesWarehouseAndProductOptionsForTheFilterSelectors() {
        StockMovementsResponse response = adapter
                .search(new StockMovementFilter(alpha, null, null, null, 0, 20,
                        StockMovementGroupBy.NONE));

        assertThat(response.warehouses()).extracting(option -> option.name())
                .containsExactly("Alpha", "Beta");
        assertThat(response.warehouses()).extracting(option -> option.id())
                .containsExactly(alpha.toString(), beta.toString());
        assertThat(response.products()).extracting(option -> option.name())
                .containsExactly("Apple", "Bolt");
        assertThat(response.products()).extracting(option -> option.code())
                .containsExactly("APP", "B-100");
        assertThat(response.products()).extracting(option -> option.id())
                .containsExactly(apple.toString(), bolt.toString());
    }

    @Test
    void filtersGlobalMovementsByWarehouse() {
        StockMovementsResponse response = adapter
                .search(new StockMovementFilter(alpha, null, null, null, 0, 20,
                        StockMovementGroupBy.NONE));

        assertThat(response.movements().totalElements()).isEqualTo(3);
        assertThat(response.movements().content()).extracting(
                movement -> movement.warehouseId())
                .containsOnly(alpha.toString());
    }

    @Test
    void filtersGlobalMovementsByMovementType() {
        StockMovementsResponse response = adapter
                .search(new StockMovementFilter(null, null, MovementType.EXIT,
                        null, 0, 20, StockMovementGroupBy.NONE));

        assertThat(response.movements().totalElements()).isEqualTo(1);
        assertThat(response.movements().content().getFirst().movementType())
                .isEqualTo(MovementType.EXIT);
        assertThat(response.movements().content().getFirst().warehouseId())
                .isEqualTo(beta.toString());
    }

    @Test
    void filtersGlobalMovementsByProductAndSearchesNamesCodesAndWarehouses() {
        assertThat(adapter
                .search(new StockMovementFilter(null, bolt, null, null, 0, 20,
                        StockMovementGroupBy.NONE))
                .movements().totalElements()).isEqualTo(1);
        assertThat(adapter
                .search(new StockMovementFilter(null, null, null, "b-100", 0,
                        20, StockMovementGroupBy.NONE))
                .movements().totalElements()).isEqualTo(1);
        assertThat(adapter
                .search(new StockMovementFilter(null, null, null, "  BETA  ", 0,
                        20, StockMovementGroupBy.NONE))
                .movements().totalElements()).isEqualTo(1);
        assertThat(adapter
                .search(new StockMovementFilter(null, null, null, "apple", 0, 20,
                        StockMovementGroupBy.NONE))
                .movements().totalElements()).isEqualTo(3);
    }

    @Test
    void groupsByProductWithServerSidePaginationAndCompleteChildren() {
        StockMovementsResponse first = adapter
                .search(new StockMovementFilter(null, null, null, null, 0, 1,
                        StockMovementGroupBy.PRODUCT));

        assertThat(first.movements()).isNull();
        assertThat(first.groups().totalElements()).isEqualTo(2);
        assertThat(first.groups().totalPages()).isEqualTo(2);
        StockMovementGroup appleGroup = first.groups().content().getFirst();
        assertThat(appleGroup.id()).isEqualTo(apple.toString());
        assertThat(appleGroup.name()).isEqualTo("Apple");
        assertThat(appleGroup.productCode()).isEqualTo("APP");
        assertThat(appleGroup.categoryName()).isEqualTo("Motors");
        assertThat(appleGroup.movementCount()).isEqualTo(3);
        assertThat(appleGroup.entryQuantity()).isEqualTo(10);
        assertThat(appleGroup.exitQuantity()).isEqualTo(4);
        assertThat(appleGroup.netQuantity()).isEqualTo(6);
        assertThat(appleGroup.items()).hasSize(3);
        assertThat(appleGroup.items()).extracting(
                movement -> movement.movementType()).containsExactly(
                        MovementType.ADJUSTMENT, MovementType.EXIT,
                        MovementType.ENTRY);
        assertThat(appleGroup.items()).extracting(
                movement -> movement.categoryName()).containsOnly("Motors");

        StockMovementsResponse second = adapter
                .search(new StockMovementFilter(null, null, null, null, 1, 1,
                        StockMovementGroupBy.PRODUCT));

        assertThat(second.groups().content()).hasSize(1);
        assertThat(second.groups().content().getFirst().id())
                .isEqualTo(bolt.toString());
        assertThat(second.groups().content().getFirst().categoryName())
                .isEqualTo("Hardware");
        assertThat(second.groups().content().getFirst().entryQuantity())
                .isEqualTo(7);
        assertThat(second.groups().content().getFirst().exitQuantity()).isZero();
        assertThat(second.groups().content().getFirst().netQuantity())
                .isEqualTo(7);
        assertThat(second.groups().content().getFirst().items()).hasSize(1);
        assertThat(second.groups().last()).isTrue();
    }

    @Test
    void groupsByWarehouseWithAllMatchingMovements() {
        StockMovementsResponse response = adapter
                .search(new StockMovementFilter(null, null, null, null, 0, 10,
                        StockMovementGroupBy.WAREHOUSE));

        assertThat(response.movements()).isNull();
        assertThat(response.groups().totalElements()).isEqualTo(2);
        StockMovementGroup alphaGroup = response.groups().content().getFirst();
        assertThat(alphaGroup.id()).isEqualTo(alpha.toString());
        assertThat(alphaGroup.name()).isEqualTo("Alpha");
        assertThat(alphaGroup.warehouseName()).isEqualTo("Alpha");
        assertThat(alphaGroup.categoryName()).isNull();
        assertThat(alphaGroup.movementCount()).isEqualTo(3);
        assertThat(alphaGroup.entryQuantity()).isEqualTo(17);
        assertThat(alphaGroup.exitQuantity()).isZero();
        assertThat(alphaGroup.netQuantity()).isEqualTo(17);
        assertThat(alphaGroup.items()).hasSize(3);
        StockMovementGroup betaGroup = response.groups().content().get(1);
        assertThat(betaGroup.id()).isEqualTo(beta.toString());
        assertThat(betaGroup.movementCount()).isEqualTo(1);
        assertThat(betaGroup.entryQuantity()).isZero();
        assertThat(betaGroup.exitQuantity()).isEqualTo(4);
        assertThat(betaGroup.netQuantity()).isEqualTo(-4);
    }

    @Test
    void groupsByMovementTypeAndKeepsFiltersAppliedToCountsAndChildren() {
        StockMovementsResponse response = adapter
                .search(new StockMovementFilter(null, null, null, null, 0, 2,
                        StockMovementGroupBy.MOVEMENT_TYPE));

        assertThat(response.groups().totalElements()).isEqualTo(3);
        assertThat(response.groups().totalPages()).isEqualTo(2);
        assertThat(response.groups().content()).extracting(
                group -> group.id()).containsExactly("ADJUSTMENT", "ENTRY");
        StockMovementGroup entryGroup = response.groups().content().get(1);
        assertThat(entryGroup.movementCount()).isEqualTo(2);
        assertThat(entryGroup.entryQuantity()).isEqualTo(17);
        assertThat(entryGroup.exitQuantity()).isZero();
        assertThat(entryGroup.netQuantity()).isEqualTo(17);
        assertThat(entryGroup.movementType()).isEqualTo(MovementType.ENTRY);
        assertThat(entryGroup.items()).hasSize(2);

        StockMovementsResponse filtered = adapter
                .search(new StockMovementFilter(alpha, null, MovementType.ENTRY,
                        null, 0, 10, StockMovementGroupBy.MOVEMENT_TYPE));

        assertThat(filtered.groups().totalElements()).isEqualTo(1);
        assertThat(filtered.groups().content().getFirst().items()).hasSize(2);
        assertThat(filtered.groups().content().getFirst().movementCount())
                .isEqualTo(2);
        assertThat(filtered.groups().content().getFirst().entryQuantity())
                .isEqualTo(17);
    }

    @Test
    void returnsEmptyResponsesForMissingResults() {
        StockMovementsResponse flat = adapter
                .search(new StockMovementFilter(null, null, null, "missing", 0,
                        10, StockMovementGroupBy.NONE));

        assertThat(flat.movements().content()).isEmpty();
        assertThat(flat.movements().totalElements()).isZero();
        assertThat(flat.movements().totalPages()).isZero();
        assertThat(flat.movements().empty()).isTrue();
        assertThat(flat.movements().first()).isTrue();
        assertThat(flat.movements().last()).isTrue();
        assertThat(flat.summary().movementCount()).isZero();
        assertThat(flat.summary().entryQuantity()).isZero();
        assertThat(flat.summary().exitQuantity()).isZero();
        assertThat(flat.summary().netQuantity()).isZero();
        assertThat(flat.warehouses()).hasSize(2);
        assertThat(flat.products()).hasSize(2);

        StockMovementsResponse grouped = adapter
                .search(new StockMovementFilter(null, null, null, "missing", 0,
                        10, StockMovementGroupBy.WAREHOUSE));

        assertThat(grouped.groups().content()).isEmpty();
        assertThat(grouped.groups().totalElements()).isZero();
        assertThat(grouped.groups().empty()).isTrue();
        assertThat(grouped.summary().movementCount()).isZero();
    }

    @Test
    void treatsSearchWildcardsAsLiteralCharactersAndIgnoresBlankSearch() {
        UUID percent = UUID.randomUUID();
        jdbc.update("INSERT INTO products VALUES (?, ?, ?, ?)", bytes(percent),
                "PCT", "100% item", bytes(hardware));
        movement(alpha, percent, MovementType.ENTRY, 1, 0, 1,
                LocalDateTime.of(2026, 1, 5, 10, 0));

        assertThat(adapter
                .search(new StockMovementFilter(null, null, null, "%", 0, 20,
                        StockMovementGroupBy.NONE))
                .movements().totalElements()).isEqualTo(1);
        assertThat(adapter
                .search(new StockMovementFilter(null, null, null, "   ", 0, 20,
                        StockMovementGroupBy.NONE))
                .movements().totalElements()).isEqualTo(5);
    }

    @Test
    void appliesEveryFilterTogetherToCountsGroupsAndChildren() {
        StockMovementsResponse byProduct = adapter
                .search(new StockMovementFilter(alpha, apple,
                        MovementType.ADJUSTMENT, "apple", 0, 1,
                        StockMovementGroupBy.PRODUCT));

        assertThat(byProduct.groups().totalElements()).isEqualTo(1);
        StockMovementGroup group = byProduct.groups().content().getFirst();
        assertThat(group.id()).isEqualTo(apple.toString());
        assertThat(group.items()).hasSize(1);
        assertThat(group.items().getFirst().movementType())
                .isEqualTo(MovementType.ADJUSTMENT);
        assertThat(group.movementCount()).isEqualTo(1);
        assertThat(group.entryQuantity()).isZero();
        assertThat(group.exitQuantity()).isZero();
        assertThat(group.netQuantity()).isZero();
        assertThat(byProduct.summary().movementCount()).isEqualTo(1);
        assertThat(byProduct.summary().entryQuantity()).isZero();
        assertThat(byProduct.summary().exitQuantity()).isZero();

        StockMovementsResponse byWarehouse = adapter
                .search(new StockMovementFilter(alpha, apple,
                        MovementType.ENTRY, "APP", 0, 1,
                        StockMovementGroupBy.WAREHOUSE));

        assertThat(byWarehouse.groups().totalElements()).isEqualTo(1);
        assertThat(byWarehouse.groups().content().getFirst().id())
                .isEqualTo(alpha.toString());
        assertThat(byWarehouse.groups().content().getFirst().entryQuantity())
                .isEqualTo(10);
        assertThat(byWarehouse.groups().content().getFirst().items()).hasSize(1);
    }

    @Test
    void returnsEmptyGroupsForOutOfRangePagesWithoutLosingCounts() {
        StockMovementsResponse beyond = adapter
                .search(new StockMovementFilter(null, null, null, null, 5, 2,
                        StockMovementGroupBy.WAREHOUSE));

        assertThat(beyond.groups().content()).isEmpty();
        assertThat(beyond.groups().totalElements()).isEqualTo(2);
        assertThat(beyond.groups().number()).isEqualTo(5);
        assertThat(beyond.groups().last()).isTrue();
        assertThat(beyond.groups().empty()).isTrue();
        assertThat(beyond.summary().movementCount()).isEqualTo(4);
    }

    private void movement(UUID warehouseId, UUID productId,
            MovementType movementType, int quantity, int previousStock,
            int newStock, LocalDateTime createdAt) {
        jdbc.update("""
                INSERT INTO stock_movements (id, warehouse_id, product_id,
                    movement_type, quantity, previous_stock, new_stock,
                    created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, bytes(UUID.randomUUID()), bytes(warehouseId),
                bytes(productId), movementType.name(), quantity, previousStock,
                newStock, Timestamp.valueOf(createdAt));
    }

    private static byte[] bytes(UUID value) {
        return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits())
                .putLong(value.getLeastSignificantBits()).array();
    }
}
