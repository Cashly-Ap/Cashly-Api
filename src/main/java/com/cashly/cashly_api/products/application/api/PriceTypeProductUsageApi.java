package com.cashly.cashly_api.products.application.api;

import java.util.Set;
import java.util.UUID;

public interface PriceTypeProductUsageApi {
    boolean isInUseByProducts(UUID priceTypeId);
    Set<UUID> findIdsInUseByProducts(Set<UUID> priceTypeIds);
}
