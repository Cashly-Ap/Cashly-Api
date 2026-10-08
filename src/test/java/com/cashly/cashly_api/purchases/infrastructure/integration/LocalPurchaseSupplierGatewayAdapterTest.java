package com.cashly.cashly_api.purchases.infrastructure.integration;

import com.cashly.cashly_api.purchases.application.contracts.PurchaseSupplierGateway;
import com.cashly.cashly_api.suppliers.application.api.PurchaseSupplierQuery;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalPurchaseSupplierGatewayAdapterTest {

    @Test
    void shouldReturnSupplierDirectoryEntriesForOneBatchQuery() {
        PurchaseSupplierQuery query = mock(PurchaseSupplierQuery.class);
        LocalPurchaseSupplierGatewayAdapter adapter = new LocalPurchaseSupplierGatewayAdapter(query);
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        List<UUID> supplierIds = List.of(firstId, secondId);
        List<PurchaseSupplierQuery.Entry> directoryEntries = List.of(
                new PurchaseSupplierQuery.Entry(firstId, "First Supplier", "J-11111111-1"),
                new PurchaseSupplierQuery.Entry(secondId, "Second Supplier", "J-22222222-2"));
        when(query.findByIds(supplierIds)).thenReturn(directoryEntries);

        List<PurchaseSupplierGateway.Entry> result = adapter.findByIds(supplierIds);

        assertEquals(List.of(
                new PurchaseSupplierGateway.Entry(firstId, "First Supplier", "J-11111111-1"),
                new PurchaseSupplierGateway.Entry(secondId, "Second Supplier", "J-22222222-2")), result);
        verify(query).findByIds(supplierIds);
    }
}
