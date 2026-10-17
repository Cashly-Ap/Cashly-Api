package com.cashly.cashly_api.partners.suppliers.application.ports.in;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PurchaseSupplierQuery {
    Optional<Entry> findById(UUID id);
    List<Entry> findByIds(Collection<UUID> ids);
    record Entry(UUID id, String name, String rif) {}
}
