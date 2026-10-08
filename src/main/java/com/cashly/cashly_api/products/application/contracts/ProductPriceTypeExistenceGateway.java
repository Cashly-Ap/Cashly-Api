package com.cashly.cashly_api.products.application.contracts;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProductPriceTypeExistenceGateway {
    Set<UUID> existingIds(List<UUID> ids);
}
