package com.cashly.cashly_api.products.application.ports.out;

import java.util.Set;
import java.util.List;
import java.util.UUID;

public interface CustomerProductContextGateway {
    UUID requirePriceType(List<UUID> customerIds);
    Set<UUID> accessibleWarehouseIds(UUID customerId);
}
