package com.cashly.cashly_api.procurement.purchases.application.ports.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseSupplierGateway {
    Optional<Entry> findById(UUID id);
    List<Entry> findByIds(Collection<UUID> ids);
    default int creditDays(UUID id) {
        return 30;
    }
    record Entry(UUID id, String name, String rif) {
    }
}
