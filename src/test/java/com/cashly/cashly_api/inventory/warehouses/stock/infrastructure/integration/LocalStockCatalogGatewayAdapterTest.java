package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.integration;

import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.entity.ProductComponent;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.infrastructure.config.StockCatalogConfig;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalStockCatalogGatewayAdapterTest {
    private final ProductRepository products = mock(ProductRepository.class);
    private final UUID productId = UUID.randomUUID();
    private final LocalStockCatalogGatewayAdapter adapter = new LocalStockCatalogGatewayAdapter(
            new StockCatalogConfig().stockCatalogQuery(products));

    @Test
    void shouldMapCompositeProductAndComponentsWithoutForeignEntities() {
        UUID componentId = UUID.randomUUID();
        Product product = mock(Product.class);
        ProductComponent component = mock(ProductComponent.class);
        when(product.getId()).thenReturn(productId);
        when(product.getCode()).thenReturn("SKU");
        when(product.getName()).thenReturn("Composite");
        when(product.isComposite()).thenReturn(true);
        when(product.getComponents()).thenReturn(List.of(component));
        when(component.getComponentProductId()).thenReturn(componentId);
        when(component.getQuantity()).thenReturn(3);
        when(products.findById(productId)).thenReturn(Optional.of(product));
        var result = adapter.getProduct(productId);
        assertEquals(productId, result.id());
        assertEquals("SKU", result.code());
        assertEquals("Composite", result.name());
        assertTrue(result.composite());
        assertEquals(componentId, result.components().getFirst().productId());
        assertEquals(3, result.components().getFirst().quantity());
        assertThrows(UnsupportedOperationException.class, () -> result.components().clear());
    }

    @Test
    void shouldExposeMissingProductAsEmptyForHistoricalReads() {
        assertTrue(adapter.findProduct(productId).isEmpty());
        verify(products).findById(productId);
    }
    @Test
    void shouldPreserveMissingProductException() {
        assertThrows(ProductNotFoundException.class, () -> adapter.getProduct(productId));
        verify(products).findById(productId);
    }

    @Test
    void shouldPropagateCatalogFailures() {
        IllegalStateException failure = new IllegalStateException("Catalog failed");
        when(products.findById(productId)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> adapter.getProduct(productId)));
    }
}
