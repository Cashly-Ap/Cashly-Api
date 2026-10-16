package com.cashly.cashly_api.products.application.architecture;

import com.cashly.cashly_api.products.application.stock.GetProductStockUseCase;
import com.cashly.cashly_api.products.application.transactions.GetProductsForTransactionUseCase;

import com.cashly.cashly_api.products.application.stock.ProductStockByWarehouseResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProductStockBoundaryTest {
    @Test
    void shouldRejectWarehouseAndCustomerTypesInProductStockCore() {
        for (Class<?> type : List.of(GetProductStockUseCase.class, GetProductsForTransactionUseCase.class, ProductStockByWarehouseResponse.class)) {
            for (var constructor : type.getConstructors()) {
                for (Class<?> parameter : constructor.getParameterTypes()) {
                    assertOwnType(parameter);
                }
            }
            for (var method : type.getDeclaredMethods()) {
                for (Class<?> parameter : method.getParameterTypes()) {
                    assertOwnType(parameter);
                }
            }
        }
    }
    private void assertOwnType(Class<?> type) {
        assertFalse(type.getPackageName().startsWith("com.cashly.cashly_api.warehouses"), type.getName());
        assertFalse(type.getPackageName().startsWith("com.cashly.cashly_api.customers"), type.getName());
    }
}
