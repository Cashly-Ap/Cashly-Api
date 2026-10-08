package com.cashly.cashly_api.suppliers.application.contracts;

import java.util.UUID;
import java.util.Collection;
import java.util.Set;

public interface PurchaseUsageGateway {
    boolean hasPurchases(UUID supplierId);
    Set<UUID> findSupplierIdsWithPurchases(Collection<UUID> supplierIds);
}
