package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.InvoiceStockAvailabilityAdapter;
import com.cashly.cashly_api.sales.orders.infrastructure.integration.OrderStockAvailabilityAdapter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.in.StockAvailabilityQuery;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StockAvailabilityConsumerAdaptersTest {
    @Test
    void should_translate_allocations_and_enrichment_without_changing_stock_values() {
        var query = mock(StockAvailabilityQuery.class);
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        var allocations = List.of(new StockAvailabilityQuery.AllocationData(warehouseId, productId, 7));
        when(query.enrich(allocations)).thenReturn(List.of(new StockAvailabilityQuery.EnrichedStock(
                warehouseId, productId, "WH-1", "Warehouse", 42)));

        var invoiceAdapter = new InvoiceStockAvailabilityAdapter(query);
        var orderAdapter = new OrderStockAvailabilityAdapter(query);
        var invoiceResult = invoiceAdapter.enrich(List.of(new com.cashly.cashly_api.finance.billing.invoices.application.ports.out
                .StockAvailabilityGateway.AllocationData(warehouseId, productId, 7)));
        var orderResult = orderAdapter.enrich(List.of(new com.cashly.cashly_api.sales.orders.application.ports.out
                .StockAvailabilityGateway.AllocationData(warehouseId, productId, 7)));
        assertThat(invoiceResult).singleElement().satisfies(item -> {
            assertThat(item.warehouseId()).isEqualTo(warehouseId);
            assertThat(item.productId()).isEqualTo(productId);
            assertThat(item.warehouseCode()).isEqualTo("WH-1");
            assertThat(item.warehouseName()).isEqualTo("Warehouse");
            assertThat(item.availableStock()).isEqualTo(42);
        });
        assertThat(orderResult).singleElement().satisfies(item -> {
            assertThat(item.warehouseId()).isEqualTo(warehouseId);
            assertThat(item.productId()).isEqualTo(productId);
            assertThat(item.warehouseCode()).isEqualTo("WH-1");
            assertThat(item.warehouseName()).isEqualTo("Warehouse");
            assertThat(item.availableStock()).isEqualTo(42);
        });
        verify(query, times(2)).enrich(allocations);
    }

    @Test
    void should_preserve_all_stock_counters_and_empty_product_entries() {
        var query = mock(StockAvailabilityQuery.class);
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID missingProductId = UUID.randomUUID();
        var products = List.of(productId, missingProductId);
        when(query.getAllStockByProducts(customerId, products)).thenReturn(Map.of(productId,
                List.of(new StockAvailabilityQuery.WarehouseStockInfo(warehouseId, "WH", "Name", 10, 3, 7)),
                missingProductId, List.of()));
        var invoiceResult = new InvoiceStockAvailabilityAdapter(query).getAllStockByProducts(customerId, products);
        var orderResult = new OrderStockAvailabilityAdapter(query).getAllStockByProducts(customerId, products);
        assertThat(invoiceResult.get(productId)).singleElement().satisfies(item -> {
            assertThat(item.warehouseId()).isEqualTo(warehouseId);
            assertThat(item.warehouseCode()).isEqualTo("WH");
            assertThat(item.warehouseName()).isEqualTo("Name");
            assertThat(item.quantity()).isEqualTo(10);
            assertThat(item.reserved()).isEqualTo(3);
            assertThat(item.available()).isEqualTo(7);
        });
        assertThat(orderResult.get(productId)).singleElement().satisfies(item -> {
            assertThat(item.warehouseId()).isEqualTo(warehouseId);
            assertThat(item.warehouseCode()).isEqualTo("WH");
            assertThat(item.warehouseName()).isEqualTo("Name");
            assertThat(item.quantity()).isEqualTo(10);
            assertThat(item.reserved()).isEqualTo(3);
            assertThat(item.available()).isEqualTo(7);
        });
        assertThat(invoiceResult.get(missingProductId)).isEmpty();
        assertThat(orderResult.get(missingProductId)).isEmpty();
        verify(query, times(2)).getAllStockByProducts(customerId, products);
    }
}
