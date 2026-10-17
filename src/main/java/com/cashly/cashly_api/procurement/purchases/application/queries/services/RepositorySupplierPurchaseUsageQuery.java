package com.cashly.cashly_api.procurement.purchases.application.queries.services;

import com.cashly.cashly_api.procurement.purchases.application.ports.in.SupplierPurchaseUsageQuery;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import java.util.UUID;
import java.util.Collection;
import java.util.Set;

public class RepositorySupplierPurchaseUsageQuery implements SupplierPurchaseUsageQuery {
    private final PurchaseRepository purchases;

    public RepositorySupplierPurchaseUsageQuery(PurchaseRepository purchases) {
        this.purchases = purchases;
    }

    @Override
    public boolean hasPurchases(UUID supplierId) {
        return purchases.existsBySupplierId(supplierId);
    }

    @Override
    public Set<UUID> findSupplierIdsWithPurchases(Collection<UUID> supplierIds) {
        if (supplierIds.isEmpty()) {
            return Set.of();
        }
        return purchases.findSupplierIdsWithPurchases(supplierIds);
    }
}
