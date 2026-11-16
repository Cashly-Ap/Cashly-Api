package com.cashly.cashly_api.inventory.warehouses.stock.application.architecture;

import com.cashly.cashly_api.inventory.warehouses.stock.application.adjustment.use_case.StockAdjustmentUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.entry.use_case.StockEntryUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exit.use_case.StockExitUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.use_case.GetStockMovementsUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.use_case.GetWarehouseStockUseCase;
import com.cashly.cashly_api.inventory.warehouses.stock.application.transfer.use_case.TransferStockUseCase;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ManualStockCatalogBoundaryTest {
    @Test
    void shouldRejectForeignProductTypesInManualStockCore() {
        for (Class<?> type : List.of(StockEntryUseCase.class,
                StockExitUseCase.class, StockAdjustmentUseCase.class,
                TransferStockUseCase.class, GetWarehouseStockUseCase.class,
                GetStockMovementsUseCase.class)) {
            for (var constructor : type.getConstructors()) {
                for (Class<?> parameter : constructor.getParameterTypes()) {
                    assertFalse(
                            parameter.getPackageName().startsWith(
                                    "com.cashly.cashly_api.catalog.products"),
                            parameter.getName());
                }
            }
            for (var field : type.getDeclaredFields()) {
                assertFalse(
                        field.getType().getPackageName().startsWith(
                                "com.cashly.cashly_api.catalog.products"),
                        field.toString());
            }
        }
    }
}
