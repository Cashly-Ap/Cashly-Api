package com.cashly.cashly_api.catalog.products.infrastructure.persistence.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProductReferenceQueryBoundaryTest {
    @Test
    void shouldKeepForeignTablesOutOfProductQueries() {
        for (var method : SpringDataProductRepository.class.getMethods()) {
            Query query = method.getAnnotation(Query.class);
            if (query != null) {
                for (String table : List.of("order_lines", "warehouse_stock",
                        "stock_movements", "invoice_lines", "return_lines")) {
                    assertFalse(query.value().contains(table), table);
                }
            }
        }
    }
}
