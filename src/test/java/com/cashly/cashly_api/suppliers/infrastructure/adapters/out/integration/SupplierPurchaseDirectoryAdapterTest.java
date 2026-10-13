package com.cashly.cashly_api.suppliers.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.suppliers.application.ports.in.PurchaseSupplierQuery;
import com.cashly.cashly_api.suppliers.domain.entities.Supplier;
import com.cashly.cashly_api.suppliers.domain.repositories.SupplierRepository;
import com.cashly.cashly_api.suppliers.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SupplierPurchaseDirectoryAdapterTest {

    private final SupplierRepository suppliers = mock(SupplierRepository.class);
    private final SupplierPurchaseDirectoryAdapter adapter = new SupplierPurchaseDirectoryAdapter(suppliers);

    @Test
    void shouldMapDirectoryResultsFromOneRepositoryBatchCall() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        List<UUID> ids = List.of(firstId, secondId);
        List<SupplierId> supplierIds = ids.stream().map(SupplierId::new).toList();
        when(suppliers.findAllById(supplierIds)).thenReturn(List.of(
                supplier(firstId, "First Supplier", "J-11111111-1"),
                supplier(secondId, "Second Supplier", "J-22222222-2")));

        List<PurchaseSupplierQuery.Entry> entries = adapter.findByIds(ids);

        assertEquals(List.of(
                new PurchaseSupplierQuery.Entry(firstId, "First Supplier", "J-11111111-1"),
                new PurchaseSupplierQuery.Entry(secondId, "Second Supplier", "J-22222222-2")), entries);
        verify(suppliers).findAllById(supplierIds);
    }

    @Test
    void shouldReturnEmptyDirectoryWithoutQueryingForEmptyIds() {
        assertEquals(List.of(), adapter.findByIds(List.of()));
        verifyNoInteractions(suppliers);
    }

    private Supplier supplier(UUID id, String name, String rif) {
        LocalDateTime now = LocalDateTime.now();
        return Supplier.reconstitute(new SupplierId(id), SupplierCode.from(100L), new LegalName(name),
                SupplierType.JURIDICO, new Rif(rif), new SupplierAlias(name), null, true, now, now,
                List.of(), List.of(), List.of());
    }
}
