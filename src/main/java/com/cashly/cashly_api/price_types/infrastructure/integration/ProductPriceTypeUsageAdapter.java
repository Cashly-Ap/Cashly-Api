package com.cashly.cashly_api.price_types.infrastructure.integration;

import com.cashly.cashly_api.price_types.application.contracts.PriceTypeUsageGateway;
import com.cashly.cashly_api.products.application.api.PriceTypeProductUsageApi;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class ProductPriceTypeUsageAdapter implements PriceTypeUsageGateway {
    private final PriceTypeProductUsageApi usageApi;

    public ProductPriceTypeUsageAdapter(PriceTypeProductUsageApi usageApi) {
        this.usageApi = usageApi;
    }

    @Override
    public boolean isInUseByProducts(UUID priceTypeId) {
        return usageApi.isInUseByProducts(priceTypeId);
    }

    @Override
    public Set<UUID> findIdsInUseByProducts(Set<UUID> priceTypeIds) {
        return usageApi.findIdsInUseByProducts(priceTypeIds);
    }
}
