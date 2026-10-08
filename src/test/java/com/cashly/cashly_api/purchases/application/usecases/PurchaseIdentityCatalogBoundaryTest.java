package com.cashly.cashly_api.purchases.application.usecases;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PurchaseIdentityCatalogBoundaryTest {
    @Test
    void shouldKeepForeignIdentitySupplierAndProductTypesOutOfUseCases() {
        for (Class<?> type : List.of(CreatePurchaseUseCase.class, UpdatePurchaseUseCase.class, GetProductsForPurchaseUseCase.class)) {
            for (var constructor : type.getConstructors()) {
                for (Class<?> parameter : constructor.getParameterTypes()) {
                    for (String module : List.of("auth", "suppliers", "products", "categories")) {
                        assertFalse(parameter.getPackageName().startsWith("com.cashly.cashly_api." + module + "."), constructor.toString());
                    }
                }
            }
        }
    }
}

