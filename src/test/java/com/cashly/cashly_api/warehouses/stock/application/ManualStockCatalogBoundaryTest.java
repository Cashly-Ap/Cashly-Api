package com.cashly.cashly_api.warehouses.stock.application;

import com.cashly.cashly_api.warehouses.stock.application.adjustment.StockAdjustmentUseCase;
import com.cashly.cashly_api.warehouses.stock.application.entry.StockEntryUseCase;
import com.cashly.cashly_api.warehouses.stock.application.exit.StockExitUseCase;
import com.cashly.cashly_api.warehouses.stock.application.movements.GetStockMovementsUseCase;
import com.cashly.cashly_api.warehouses.stock.application.queries.GetWarehouseStockUseCase;
import com.cashly.cashly_api.warehouses.stock.application.transfer.TransferStockUseCase;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ManualStockCatalogBoundaryTest {
    @Test
    void shouldRejectForeignProductTypesInManualStockCore() {
        for (Class<?> type : List.of(StockEntryUseCase.class, StockExitUseCase.class,
                StockAdjustmentUseCase.class, TransferStockUseCase.class, GetWarehouseStockUseCase.class, GetStockMovementsUseCase.class)) {
            for (var constructor : type.getConstructors()) {
                for (Class<?> parameter : constructor.getParameterTypes()) {
                    assertFalse(parameter.getPackageName().startsWith("com.cashly.cashly_api.products"), parameter.getName());
                }
            }
            for (var field : type.getDeclaredFields()) {
                assertFalse(field.getType().getPackageName().startsWith("com.cashly.cashly_api.products"), field.toString());
            }
        }
    }
}
