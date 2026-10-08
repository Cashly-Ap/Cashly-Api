package com.cashly.cashly_api.purchases.application.api;

import java.util.UUID;
import java.util.Collection;
import java.util.Set;

public interface SupplierPurchaseUsageQuery {
    boolean hasPurchases(UUID supplierId);
    Set<UUID> findSupplierIdsWithPurchases(Collection<UUID> supplierIds);
}
