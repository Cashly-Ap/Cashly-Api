package com.cashly.cashly_api.partners.suppliers.application.ports.out;

import java.util.UUID;
import java.util.Collection;
import java.util.Set;

public interface PurchaseUsageGateway {
    boolean hasPurchases(UUID supplierId);
    Set<UUID> findSupplierIdsWithPurchases(Collection<UUID> supplierIds);
}
