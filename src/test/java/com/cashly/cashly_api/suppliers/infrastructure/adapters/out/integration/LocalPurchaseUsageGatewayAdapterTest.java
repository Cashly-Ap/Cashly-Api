package com.cashly.cashly_api.suppliers.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.purchases.infrastructure.config.SupplierPurchaseUsageConfig;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalPurchaseUsageGatewayAdapterTest {
    private final PurchaseRepository purchases = mock(PurchaseRepository.class);
    private final UUID supplierId = UUID.randomUUID();
    private final LocalPurchaseUsageGatewayAdapter adapter = new LocalPurchaseUsageGatewayAdapter(
            new SupplierPurchaseUsageConfig().supplierPurchaseUsageQuery(purchases));

    @Test
    void shouldExposeExistingPurchaseUsageThroughPublicQuery() {
        when(purchases.existsBySupplierId(supplierId)).thenReturn(true);
        assertTrue(adapter.hasPurchases(supplierId));
        verify(purchases).existsBySupplierId(supplierId);
    }

    @Test
    void shouldReturnFalseWhenSupplierHasNoPurchases() {
        assertFalse(adapter.hasPurchases(supplierId));
        verify(purchases).existsBySupplierId(supplierId);
    }

    @Test
    void shouldPropagatePersistenceFailuresWithoutClaimingSupplierIsUnused() {
        IllegalStateException failure = new IllegalStateException("Purchase lookup failed");
        when(purchases.existsBySupplierId(supplierId)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> adapter.hasPurchases(supplierId)));
    }

    @Test
    void shouldReturnSupplierIdsWithPurchasesUsingOneBatchQuery() {
        Set<UUID> supplierIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
        Set<UUID> usedSupplierIds = Set.of(supplierIds.iterator().next());
        when(purchases.findSupplierIdsWithPurchases(supplierIds)).thenReturn(usedSupplierIds);

        assertEquals(usedSupplierIds, adapter.findSupplierIdsWithPurchases(supplierIds));
        verify(purchases).findSupplierIdsWithPurchases(supplierIds);
    }
}
