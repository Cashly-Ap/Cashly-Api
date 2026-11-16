package com.cashly.cashly_api.procurement.purchases.infrastructure.integration;

import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseProductGateway;
import com.cashly.cashly_api.catalog.products.application.ports.in.StockCatalogQuery;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalPurchaseProductGatewayAdapter
        implements
            PurchaseProductGateway {
    private final StockCatalogQuery query;
    public LocalPurchaseProductGatewayAdapter(StockCatalogQuery query) {
        this.query = query;
    }
    @Override
    public Optional<Entry> findById(UUID id) {
        return query.findProduct(id).map(
                entry -> new Entry(entry.id(), entry.code(), entry.name()));
    }
}