package com.cashly.cashly_api.catalog.products.infrastructure.integration;

import com.cashly.cashly_api.catalog.products.application.ports.out.ProductPriceTypeExistenceGateway;
import com.cashly.cashly_api.catalog.price_types.application.ports.in.ProductPriceTypeExistenceQuery;
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
