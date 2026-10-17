package com.cashly.cashly_api.catalog.price_types.application.ports.in;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProductPriceTypeExistenceQuery {
    Set<UUID> existingIds(List<UUID> ids);
}
