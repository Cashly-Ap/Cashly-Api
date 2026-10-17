package com.cashly.cashly_api.catalog.products.application.ports.in;

import java.util.Set;
import java.util.UUID;

public interface PriceTypeProductUsageApi {
    boolean isInUseByProducts(UUID priceTypeId);
    Set<UUID> findIdsInUseByProducts(Set<UUID> priceTypeIds);
}
