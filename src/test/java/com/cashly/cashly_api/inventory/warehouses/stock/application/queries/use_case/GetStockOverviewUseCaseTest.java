package com.cashly.cashly_api.inventory.warehouses.stock.application.queries.use_case;

import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockOverviewQueryGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewGroupBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetStockOverviewUseCaseTest {
    private final StockOverviewQueryGateway gateway = mock(
            StockOverviewQueryGateway.class);
    private final GetStockOverviewUseCase useCase = new GetStockOverviewUseCase(
            gateway);

    @Test
    void passesNormalizedSearchAndPaginationToTheReadGateway() {
        useCase.execute(null, null, "  APP  ", 2, 10);
        verify(gateway)
                .search(new StockOverviewFilter(null, null, "APP", 2, 10));
    }

    @Test
    void defaultsToUngroupedWhenGroupingIsNotProvided() {
        useCase.execute(null, null, null, 0, 10);

        verify(gateway).search(new StockOverviewFilter(null, null, "", 0, 10,
                StockOverviewGroupBy.NONE));
    }

    @ParameterizedTest
    @EnumSource(StockOverviewGroupBy.class)
    void forwardsEverySupportedGroupingStrategy(
            StockOverviewGroupBy groupBy) {
        var warehouseId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();

        useCase.execute(warehouseId, categoryId, "  APP  ", 3, 25, groupBy);

        verify(gateway).search(new StockOverviewFilter(warehouseId, categoryId,
                "APP", 3, 25, groupBy));
    }

    @Test
    void treatsNullGroupingAsUngrouped() {
        useCase.execute(null, null, "APP", 0, 10, null);

        verify(gateway).search(new StockOverviewFilter(null, null, "APP", 0,
                10, StockOverviewGroupBy.NONE));
    }

    @ParameterizedTest
    @CsvSource({"-1,10", "0,0", "0,-1", "0,101"})
    void rejectsUnboundedOrNegativePagination(int page, int size) {
        assertThatThrownBy(() -> useCase.execute(null, null, null, page, size))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(gateway);
    }

    @Test
    void rejectsOversizedSearchBeforeQuerying() {
        assertThatThrownBy(
                () -> useCase.execute(null, null, "a".repeat(201), 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(gateway);
    }
}
