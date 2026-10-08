package com.cashly.cashly_api.products.application.usecases;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProductCatalogBoundaryTest {
    @Test
    void shouldRejectForeignCategoryAndPricingDependenciesInCore() {
        for (Class<?> type : List.of(CreateProductUseCase.class, UpdateProductUseCase.class,
                GetProductUseCase.class, ListProductsUseCase.class, UploadProductPhotoUseCase.class,
                GetProductsForTransactionUseCase.class)) {
            for (var constructor : type.getConstructors()) {
                for (Class<?> parameter : constructor.getParameterTypes()) {
                    assertFalse(parameter.getPackageName().startsWith("com.cashly.cashly_api.categories"), parameter.getName());
                    assertFalse(parameter.getPackageName().startsWith("com.cashly.cashly_api.price_types"), parameter.getName());
                }
            }
        }
    }
}
