package com.cashly.cashly_api.orders.application.api;

import java.util.Set;
import java.util.UUID;

public interface ProductReferenceQuery {
    Set<UUID> findReferencedProductIds(Set<UUID> ids);
}