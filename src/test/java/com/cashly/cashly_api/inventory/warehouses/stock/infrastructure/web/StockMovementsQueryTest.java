package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.web;

import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementGroupBy;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StockMovementsQuery Tests")
class StockMovementsQueryTest {

    @Test
    @DisplayName("global open view: absent values fall back to defaults")
    void shouldDefaultAbsentValues() {
        StockMovementFilter filter = new StockMovementsQuery(null, null, null,
                null, null, null, null).toFilter();

        assertThat(filter.warehouseId()).isNull();
        assertThat(filter.productId()).isNull();
        assertThat(filter.movementType()).isNull();
        assertThat(filter.search()).isEmpty();
        assertThat(filter.page()).isZero();
        assertThat(filter.size()).isEqualTo(20);
        assertThat(filter.groupBy()).isEqualTo(StockMovementGroupBy.NONE);
    }

    @Test
    @DisplayName("blank optional filters are treated as absent")
    void shouldTreatBlankValuesAsAbsent() {
        StockMovementFilter filter = new StockMovementsQuery(" ", "", "\t",
                "   ", "", "", " ").toFilter();

        assertThat(filter.warehouseId()).isNull();
        assertThat(filter.productId()).isNull();
        assertThat(filter.movementType()).isNull();
        assertThat(filter.search()).isEmpty();
        assertThat(filter.page()).isZero();
        assertThat(filter.size()).isEqualTo(20);
        assertThat(filter.groupBy()).isEqualTo(StockMovementGroupBy.NONE);
    }

    @Test
    @DisplayName("trims and parses every supported filter")
    void shouldParseEveryFilter() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        StockMovementFilter filter = new StockMovementsQuery(
                " " + warehouseId + " ", " " + productId + " ", " exit ",
                "  M-01  ", " 2 ", " 25 ", " movement_type ").toFilter();

        assertThat(filter.warehouseId()).isEqualTo(warehouseId);
        assertThat(filter.productId()).isEqualTo(productId);
        assertThat(filter.movementType()).isEqualTo(MovementType.EXIT);
        assertThat(filter.search()).isEqualTo("M-01");
        assertThat(filter.page()).isEqualTo(2);
        assertThat(filter.size()).isEqualTo(25);
        assertThat(filter.groupBy())
                .isEqualTo(StockMovementGroupBy.MOVEMENT_TYPE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NONE", "PRODUCT", "WAREHOUSE", "MOVEMENT_TYPE",
            "none", "product", "warehouse", "movement_type"})
    @DisplayName("parses every documented grouping value regardless of case")
    void shouldParseEveryGroupingValue(String rawGroupBy) {
        StockMovementFilter filter = new StockMovementsQuery(null, null, null,
                null, null, null, rawGroupBy).toFilter();

        assertThat(filter.groupBy()).isEqualTo(
                StockMovementGroupBy.valueOf(
                        rawGroupBy.toUpperCase(java.util.Locale.ROOT)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ENTRY", "EXIT", "ADJUSTMENT", "TRANSFER_IN",
            "TRANSFER_OUT", "COMPONENT_EXIT", "RETURN_ENTRY", "CANCEL_ENTRY",
            "SALE_EXIT"})
    @DisplayName("parses every movement type")
    void shouldParseEveryMovementType(String rawMovementType) {
        StockMovementFilter filter = new StockMovementsQuery(null, null,
                rawMovementType, null, null, null, null).toFilter();

        assertThat(filter.movementType())
                .isEqualTo(MovementType.valueOf(rawMovementType));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @DisplayName("accepts missing or blank paging values")
    void shouldAcceptMissingPaging(String raw) {
        StockMovementFilter filter = new StockMovementsQuery(null, null, null,
                null, raw, raw, null).toFilter();

        assertThat(filter.page()).isZero();
        assertThat(filter.size()).isEqualTo(20);
    }

    @Test
    @DisplayName("rejects a malformed UUID naming the parameter")
    void shouldRejectMalformedUuid() {
        assertThatThrownBy(() -> new StockMovementsQuery("not-a-uuid", null,
                null, null, null, null, null).toFilter())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("warehouseId");
    }

    @Test
    @DisplayName("rejects an unknown movement type naming the parameter")
    void shouldRejectUnknownMovementType() {
        assertThatThrownBy(() -> new StockMovementsQuery(null, null, "INBOUND",
                null, null, null, null).toFilter())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("movementType");
    }

    @Test
    @DisplayName("rejects an unknown grouping naming the parameter and options")
    void shouldRejectUnknownGrouping() {
        assertThatThrownBy(() -> new StockMovementsQuery(null, null, null,
                null, null, null, "SKU").toFilter())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("groupBy")
                .hasMessageContaining("PRODUCT")
                .hasMessageContaining("MOVEMENT_TYPE");
    }

    @Test
    @DisplayName("rejects non numeric paging values")
    void shouldRejectNonNumericPaging() {
        assertThatThrownBy(() -> new StockMovementsQuery(null, null, null,
                null, "one", null, null).toFilter())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
        assertThatThrownBy(() -> new StockMovementsQuery(null, null, null,
                null, null, "large", null).toFilter())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }
}
