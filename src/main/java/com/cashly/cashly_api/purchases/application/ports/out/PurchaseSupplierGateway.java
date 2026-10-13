package com.cashly.cashly_api.purchases.application.ports.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseSupplierGateway {
    Optional<Entry> findById(UUID id);
    List<Entry> findByIds(Collection<UUID> ids);
    record Entry(UUID id, String name, String rif) {}
}
