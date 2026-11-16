package com.cashly.cashly_api.catalog.products.application.ports.out;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProductPriceTypeExistenceGateway {
    Set<UUID> existingIds(List<UUID> ids);
}
