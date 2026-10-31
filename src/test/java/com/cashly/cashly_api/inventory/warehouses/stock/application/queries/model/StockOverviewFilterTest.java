package com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockOverviewFilterTest {

    @Test
    void defaultsToUngroupedWhenGroupingIsOmitted() {
        var filter = new StockOverviewFilter(null, null, null, 0, 10);

        assertThat(filter.groupBy()).isEqualTo(StockOverviewGroupBy.NONE);
        assertThat(filter.search()).isEmpty();
    }

    @Test
    void defaultsNullGroupingToUngrouped() {
        var filter = new StockOverviewFilter(null, null, "APP", 0, 10, null);

        assertThat(filter.groupBy()).isEqualTo(StockOverviewGroupBy.NONE);
    }

    @Test
    void defaultsNullSearchToEmpty() {
        var filter = new StockOverviewFilter(null, null, null, 0, 10);

        assertThat(filter.search()).isEmpty();
    }

    @Test
    void trimsSearchAroundTheActualTerm() {
        var filter = new StockOverviewFilter(null, null, "  M-01  ", 0, 10);

        assertThat(filter.search()).isEqualTo("M-01");
    }

    @Test
    void preservesCombinedFiltersAndPagination() {
        var warehouseId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();

        var filter = new StockOverviewFilter(warehouseId, categoryId, " APP ",
                2, 25, StockOverviewGroupBy.WAREHOUSE);

        assertThat(filter.warehouseId()).isEqualTo(warehouseId);
        assertThat(filter.categoryId()).isEqualTo(categoryId);
        assertThat(filter.search()).isEqualTo("APP");
        assertThat(filter.page()).isEqualTo(2);
        assertThat(filter.size()).isEqualTo(25);
        assertThat(filter.groupBy()).isEqualTo(StockOverviewGroupBy.WAREHOUSE);
    }

    @ParameterizedTest
    @CsvSource({"-1,10", "0,0", "0,-1", "0,101"})
    void rejectsInvalidPagination(int page, int size) {
        assertThatThrownBy(
                () -> new StockOverviewFilter(null, null, null, page, size))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsTheLargestAllowedPageSize() {
        var filter = new StockOverviewFilter(null, null, null, 0, 100);

        assertThat(filter.size()).isEqualTo(100);
    }

    @Test
    void rejectsSearchBeyondTheAllowedLength() {
        assertThatThrownBy(() -> new StockOverviewFilter(null, null,
                "a".repeat(201), 0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("200");
    }
}
