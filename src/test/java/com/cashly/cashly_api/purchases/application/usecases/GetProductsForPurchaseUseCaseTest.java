package com.cashly.cashly_api.purchases.application.usecases;

import com.cashly.cashly_api.purchases.application.ports.out.PurchaseProductCatalogGateway;
import com.cashly.cashly_api.purchases.application.ports.out.PurchaseWarehouseGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetProductsForPurchaseUseCaseTest {
    @Test
    void shouldPreservePaginationPricesAndActiveWarehouseStockIncludingMissingRows() {
        var products = mock(PurchaseProductCatalogGateway.class);
        var warehouses = mock(PurchaseWarehouseGateway.class);
        var productId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        when(products.find("Tea", categoryId, 2, 5)).thenReturn(new PageResponse<>(List.of(
                new PurchaseProductCatalogGateway.Entry(productId, "P-1", "Tea", categoryId, "Drinks", true,
                        new BigDecimal("7.50"))), 12, 3, 5, 2, false, true, false));
        when(warehouses.activeWarehouses()).thenReturn(List.of(
                new PurchaseWarehouseGateway.Warehouse(first, "W-1", "First"),
                new PurchaseWarehouseGateway.Warehouse(second, "W-2", "Second")));
        when(warehouses.findByProductId(productId)).thenReturn(List.of(
                new PurchaseWarehouseGateway.Stock(first, 10, 3, 7),
                new PurchaseWarehouseGateway.Stock(UUID.randomUUID(), 99, 0, 99)));
        var result = new GetProductsForPurchaseUseCase(products, warehouses).execute("Tea", categoryId, 2, 5);
        var product = result.content().getFirst();
        assertEquals(12, result.totalElements());
        assertEquals(3, result.totalPages());
        assertEquals(2, result.number());
        assertFalse(result.first());
        assertTrue(result.last());
        assertEquals("Drinks", product.categoryName());
        assertEquals(new BigDecimal("7.50"), product.purchasePrice());
        assertEquals(10, product.stock());
        assertEquals(2, product.stockItems().size());
        assertEquals(3, product.stockItems().getFirst().reserved());
        assertEquals(7, product.stockItems().getFirst().available());
        assertEquals(0, product.stockItems().getLast().quantity());
        assertEquals("W-2", product.stockItems().getLast().warehouseCode());
    }

    @Test
    void shouldPreserveEmptyPageWithoutQueryingProductStock() {
        var products = mock(PurchaseProductCatalogGateway.class);
        var warehouses = mock(PurchaseWarehouseGateway.class);
        when(products.find(null, null, 0, 10)).thenReturn(new PageResponse<>(List.of(), 0, 0, 10, 0, true, true, true));
        when(warehouses.activeWarehouses()).thenReturn(List.of());
        var result = new GetProductsForPurchaseUseCase(products, warehouses).execute(null, null, 0, 10);
        assertTrue(result.empty());
        assertEquals(10, result.size());
        verify(warehouses).activeWarehouses();
        verifyNoMoreInteractions(warehouses);
    }
}
