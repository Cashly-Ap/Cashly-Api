package com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.adapters.JpaProductRepository;

import com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.entities.ProductEntity;
import com.cashly.cashly_api.products.infrastructure.adapters.out.persistence.entities.ProductPriceEntity;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProductPricePersistenceBoundaryTest {
    @Test
    void shouldKeepForeignPriceTypeEntitiesAndRepositoriesOutOfProductPersistence() {
        for (Class<?> type : List.of(ProductEntity.class, ProductPriceEntity.class, JpaProductRepository.class)) {
            for (var field : type.getDeclaredFields()) {
                assertFalse(field.getGenericType().getTypeName().contains("com.cashly.cashly_api.price_types"), field.toString());
            }
            for (var method : type.getDeclaredMethods()) {
                for (var parameter : method.getGenericParameterTypes()) {
                    assertFalse(parameter.getTypeName().contains("com.cashly.cashly_api.price_types"), method.toString());
                }
            }
            for (var constructor : type.getConstructors()) {
                for (var parameter : constructor.getParameterTypes()) {
                    assertFalse(parameter.getPackageName().startsWith("com.cashly.cashly_api.price_types"), constructor.toString());
                }
            }
        }
    }
}
