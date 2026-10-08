package com.cashly.cashly_api.products.application.contracts;

import java.util.Set;
import java.util.UUID;

public interface ProductReferenceGateway {
    Set<UUID> findReferencedProductIds(Set<UUID> ids);
}
