package com.cashly.cashly_api.purchases.application.usecases;

import com.cashly.cashly_api.categories.application.api.ProductCategoryQuery;
import com.cashly.cashly_api.products.application.queries.RepositoryPurchaseProductCatalogQuery;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.purchases.infrastructure.adapters.LocalPurchaseProductCatalogGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseProductCatalogContractTest {
    @Test
    void shouldKeepActiveNonCompositeFiltersAndCategoryFallbackAcrossPublicContracts() {
        var products = mock(ProductRepository.class);
        var categories = mock(ProductCategoryQuery.class);
        var productId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var product = Product.reconstitute(productId, "P-1", "Tea", null, categoryId, false, true, null,
                new BigDecimal("9.25"), List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());
        when(products.findAllWithFilters("tea", categoryId, true, false, 1, 20, "name", "asc"))
                .thenReturn(new PageResponse<>(List.of(product), 25, 2, 20, 1, false, true, false));
        when(categories.findById(categoryId)).thenReturn(Optional.empty());
        var gateway = new LocalPurchaseProductCatalogGateway(new RepositoryPurchaseProductCatalogQuery(products), categories);
        var result = gateway.find("tea", categoryId, 1, 20);
        assertEquals("", result.content().getFirst().categoryName());
        assertEquals(productId, result.content().getFirst().id());
        assertEquals(new BigDecimal("9.25"), result.content().getFirst().purchasePrice());
        assertEquals(25, result.totalElements());
        verify(products).findAllWithFilters("tea", categoryId, true, false, 1, 20, "name", "asc");
        when(categories.findById(categoryId)).thenReturn(Optional.of(new ProductCategoryQuery.Entry(categoryId, "Drinks")));
        assertEquals("Drinks", gateway.find("tea", categoryId, 1, 20).content().getFirst().categoryName());
    }
}
