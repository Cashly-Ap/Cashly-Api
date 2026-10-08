package com.cashly.cashly_api.products.infrastructure.integration;

import com.cashly.cashly_api.products.application.contracts.ProductPriceTypeExistenceGateway;
import com.cashly.cashly_api.price_types.application.api.ProductPriceTypeExistenceQuery;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalProductPriceTypeExistenceGatewayAdapter implements ProductPriceTypeExistenceGateway {
    private final ProductPriceTypeExistenceQuery priceTypes;

    public LocalProductPriceTypeExistenceGatewayAdapter(ProductPriceTypeExistenceQuery priceTypes) {
        this.priceTypes = priceTypes;
    }

    @Override
    public Set<UUID> existingIds(List<UUID> ids) {
        return Set.copyOf(priceTypes.existingIds(ids));
    }
}
