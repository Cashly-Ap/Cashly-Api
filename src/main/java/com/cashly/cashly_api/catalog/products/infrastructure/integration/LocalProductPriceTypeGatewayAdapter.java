package com.cashly.cashly_api.catalog.products.infrastructure.integration;

import com.cashly.cashly_api.catalog.products.application.ports.out.ProductPriceTypeGateway;
import com.cashly.cashly_api.catalog.price_types.application.ports.in.ProductPriceTypeQuery;
import com.cashly.cashly_api.catalog.price_types.application.exceptions.PriceTypeNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalProductPriceTypeGatewayAdapter implements ProductPriceTypeGateway {
    private final ProductPriceTypeQuery query;

    public LocalProductPriceTypeGatewayAdapter(ProductPriceTypeQuery query) {
        this.query = query;
    }

    @Override
    public Optional<Entry> findById(UUID id) {
        return query.findById(id).map(entry -> new Entry(entry.id(), entry.name()));
    }

    @Override
    public Entry require(UUID id) {
        return findById(id).orElseThrow(() -> new PriceTypeNotFoundException(id));
    }
}
