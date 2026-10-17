package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration;

import com.cashly.cashly_api.catalog.products.application.ports.in.SalesProductCatalogQuery;
import com.cashly.cashly_api.finance.billing.returns.application.ports.in.InvoiceReturnDetailsQuery;
import com.cashly.cashly_api.inventory.warehouses.management.application.ports.in.WarehouseDirectoryEntry;
import com.cashly.cashly_api.inventory.warehouses.management.application.ports.in.WarehouseDirectoryQuery;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalInvoiceReturnDetailsAdapterTest {
    @Test
    void enrichesReturnProjectionUsingPublicProductAndWarehouseQueries() {
        InvoiceReturnDetailsQuery returns = mock(InvoiceReturnDetailsQuery.class);
        SalesProductCatalogQuery products = mock(SalesProductCatalogQuery.class);
        WarehouseDirectoryQuery warehouses = mock(WarehouseDirectoryQuery.class);
        UUID invoiceId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        when(returns.findByInvoiceId(invoiceId)).thenReturn(List.of(new InvoiceReturnDetailsQuery.ReturnDetails(
                UUID.randomUUID(), "R-1", "REFUND", "Damaged", new BigDecimal("8.00"), LocalDateTime.now(), "User",
                List.of(new InvoiceReturnDetailsQuery.Line(productId, BigDecimal.ONE, new BigDecimal("8.00"),
                        new BigDecimal("8.00"), List.of(new InvoiceReturnDetailsQuery.Allocation(warehouseId,
                        BigDecimal.ONE)))))));
        when(products.findProduct(productId)).thenReturn(Optional.of(
                new SalesProductCatalogQuery.ProductReference(productId, "P-1", "Product", null)));
        when(warehouses.findById(warehouseId)).thenReturn(Optional.of(new WarehouseDirectoryEntry(warehouseId, "Main")));

        var result = new LocalInvoiceReturnDetailsAdapter(returns, products, warehouses).findByInvoiceId(invoiceId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().lines().getFirst().productName()).isEqualTo("Product");
        assertThat(result.getFirst().lines().getFirst().allocations().getFirst().warehouseName()).isEqualTo("Main");
    }
}
