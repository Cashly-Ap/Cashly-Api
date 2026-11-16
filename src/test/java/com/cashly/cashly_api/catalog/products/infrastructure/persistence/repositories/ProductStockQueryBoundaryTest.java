package com.cashly.cashly_api.catalog.products.infrastructure.persistence.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProductStockQueryBoundaryTest {
    @Test
    void shouldKeepStockFilteringQueriesInsideProducts() {
        for (var method : SpringDataProductRepository.class.getMethods()) {
            Query query = method.getAnnotation(Query.class);
            if (query != null && !query.nativeQuery()) {
                assertFalse(query.value().contains("WarehouseStockEntity"),
                        method.toString());
            }
        }
    }
}
