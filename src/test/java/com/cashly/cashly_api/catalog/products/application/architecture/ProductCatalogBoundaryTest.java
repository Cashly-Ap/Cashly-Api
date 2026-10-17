package com.cashly.cashly_api.catalog.products.application.architecture;

import com.cashly.cashly_api.catalog.products.application.create.use_case.CreateProductUseCase;
import com.cashly.cashly_api.catalog.products.application.photos.use_case.UploadProductPhotoUseCase;
import com.cashly.cashly_api.catalog.products.application.queries.use_case.GetProductUseCase;
import com.cashly.cashly_api.catalog.products.application.queries.use_case.ListProductsUseCase;
import com.cashly.cashly_api.catalog.products.application.transactions.use_case.GetProductsForTransactionUseCase;
import com.cashly.cashly_api.catalog.products.application.update.use_case.UpdateProductUseCase;

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
                    assertFalse(parameter.getPackageName().startsWith("com.cashly.cashly_api.catalog.categories"), parameter.getName());
                    assertFalse(parameter.getPackageName().startsWith("com.cashly.cashly_api.catalog.price_types"), parameter.getName());
                }
            }
        }
    }
}
