package com.cashly.cashly_api.price_types.application.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProductPriceTypeExistenceQuery {
    Set<UUID> existingIds(List<UUID> ids);
}
