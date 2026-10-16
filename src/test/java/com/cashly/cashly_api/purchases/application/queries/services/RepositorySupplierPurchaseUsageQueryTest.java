package com.cashly.cashly_api.purchases.application.queries.services;

import com.cashly.cashly_api.purchases.domain.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositorySupplierPurchaseUsageQueryTest {

    @Test
    void shouldReturnSupplierIdsWithPurchasesInOneRepositoryCall() {
        PurchaseRepository purchases = mock(PurchaseRepository.class);
        RepositorySupplierPurchaseUsageQuery query = new RepositorySupplierPurchaseUsageQuery(purchases);
        Set<UUID> supplierIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
        Set<UUID> usedSupplierIds = Set.of(supplierIds.iterator().next());
        when(purchases.findSupplierIdsWithPurchases(supplierIds)).thenReturn(usedSupplierIds);

        assertEquals(usedSupplierIds, query.findSupplierIdsWithPurchases(supplierIds));
        verify(purchases).findSupplierIdsWithPurchases(supplierIds);
    }
}
