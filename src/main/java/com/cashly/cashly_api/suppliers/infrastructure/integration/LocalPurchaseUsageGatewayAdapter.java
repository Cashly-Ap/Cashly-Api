package com.cashly.cashly_api.suppliers.infrastructure.integration;

import com.cashly.cashly_api.purchases.application.api.SupplierPurchaseUsageQuery;
import com.cashly.cashly_api.suppliers.application.contracts.PurchaseUsageGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.Collection;
import java.util.Set;

@Component
public class LocalPurchaseUsageGatewayAdapter implements PurchaseUsageGateway {
    private final SupplierPurchaseUsageQuery purchases;

    public LocalPurchaseUsageGatewayAdapter(SupplierPurchaseUsageQuery purchases) {
        this.purchases = purchases;
    }

    @Override
    public boolean hasPurchases(UUID supplierId) {
        return purchases.hasPurchases(supplierId);
    }

    @Override
    public Set<UUID> findSupplierIdsWithPurchases(Collection<UUID> supplierIds) {
        if (supplierIds.isEmpty()) {
            return Set.of();
        }
        return purchases.findSupplierIdsWithPurchases(supplierIds);
    }
}
