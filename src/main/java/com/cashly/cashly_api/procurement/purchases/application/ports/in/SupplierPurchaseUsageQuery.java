package com.cashly.cashly_api.procurement.purchases.application.ports.in;

import java.util.UUID;
import java.util.Collection;
import java.util.Set;

public interface SupplierPurchaseUsageQuery {
    boolean hasPurchases(UUID supplierId);
    Set<UUID> findSupplierIdsWithPurchases(Collection<UUID> supplierIds);
}
