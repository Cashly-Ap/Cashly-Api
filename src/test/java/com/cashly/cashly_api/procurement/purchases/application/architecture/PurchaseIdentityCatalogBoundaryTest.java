package com.cashly.cashly_api.procurement.purchases.application.architecture;

import com.cashly.cashly_api.procurement.purchases.application.create.use_case.CreatePurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.products.use_case.GetProductsForPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.update.use_case.UpdatePurchaseUseCase;

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

