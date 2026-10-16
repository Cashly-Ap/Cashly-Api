package com.cashly.cashly_api.products.application.architecture;

import com.cashly.cashly_api.products.application.create.CreateProductUseCase;
import com.cashly.cashly_api.products.application.photos.UploadProductPhotoUseCase;
import com.cashly.cashly_api.products.application.queries.GetProductUseCase;
import com.cashly.cashly_api.products.application.queries.ListProductsUseCase;
import com.cashly.cashly_api.products.application.transactions.GetProductsForTransactionUseCase;
import com.cashly.cashly_api.products.application.update.UpdateProductUseCase;

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
