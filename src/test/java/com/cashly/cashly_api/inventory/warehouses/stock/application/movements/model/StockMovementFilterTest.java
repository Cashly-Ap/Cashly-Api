package com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model;

import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockMovementFilterTest {

    @Test
    void defaultsToNoGroupingAndBlankSearch() {
        StockMovementFilter filter = new StockMovementFilter(null, null, null,
                null, 0, 20, null);

        assertThat(filter.groupBy()).isEqualTo(StockMovementGroupBy.NONE);
        assertThat(filter.search()).isEmpty();
    }

    @Test
    void trimsSearchAndKeepsEveryFilter() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        StockMovementFilter filter = new StockMovementFilter(warehouseId,
                productId, MovementType.EXIT, "  BOLT  ", 1, 25,
                StockMovementGroupBy.PRODUCT);

        assertThat(filter.warehouseId()).isEqualTo(warehouseId);
        assertThat(filter.productId()).isEqualTo(productId);
        assertThat(filter.movementType()).isEqualTo(MovementType.EXIT);
        assertThat(filter.search()).isEqualTo("BOLT");
        assertThat(filter.page()).isEqualTo(1);
        assertThat(filter.size()).isEqualTo(25);
        assertThat(filter.groupBy()).isEqualTo(StockMovementGroupBy.PRODUCT);
    }

    @Test
    void defaultsToNoGroupingWithTheCompactConstructor() {
        StockMovementFilter filter = new StockMovementFilter(null, null, null,
                0, 20);

        assertThat(filter.groupBy()).isEqualTo(StockMovementGroupBy.NONE);
        assertThat(filter.search()).isEmpty();
    }

    @Test
    void acceptsTheLargestAllowedPageSize() {
        StockMovementFilter filter = new StockMovementFilter(null, null, null,
                "APP", 0, 100, StockMovementGroupBy.NONE);

        assertThat(filter.size()).isEqualTo(100);
    }

    @ParameterizedTest
    @CsvSource({"-1,20", "0,0", "0,-1", "0,101"})
    void rejectsInvalidPagination(int page, int size) {
        assertThatThrownBy(() -> new StockMovementFilter(null, null, null,
                null, page, size, StockMovementGroupBy.NONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsOversizedSearch() {
        assertThatThrownBy(() -> new StockMovementFilter(null, null, null,
                "a".repeat(201), 0, 20, StockMovementGroupBy.NONE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
