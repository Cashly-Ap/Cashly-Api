package com.cashly.cashly_api.catalog.products.application.ports.out;

import java.util.Set;
import java.util.UUID;

public interface ProductReferenceGateway {
    Set<UUID> findReferencedProductIds(Set<UUID> ids);
}
