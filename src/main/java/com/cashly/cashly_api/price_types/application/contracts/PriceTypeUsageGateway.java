package com.cashly.cashly_api.price_types.application.contracts;

import java.util.Set;
import java.util.UUID;

public interface PriceTypeUsageGateway {
    boolean isInUseByProducts(UUID priceTypeId);
    Set<UUID> findIdsInUseByProducts(Set<UUID> priceTypeIds);
}
