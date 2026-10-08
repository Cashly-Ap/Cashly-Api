package com.cashly.cashly_api.warehouses.application.api;

import java.util.Set;
import java.util.UUID;

public interface ProductReferenceQuery {
    Set<UUID> findReferencedProductIds(Set<UUID> ids);
}