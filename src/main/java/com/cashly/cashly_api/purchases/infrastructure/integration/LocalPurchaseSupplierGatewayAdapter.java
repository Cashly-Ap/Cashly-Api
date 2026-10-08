package com.cashly.cashly_api.purchases.infrastructure.integration;

import com.cashly.cashly_api.purchases.application.contracts.PurchaseSupplierGateway;
import com.cashly.cashly_api.suppliers.application.api.PurchaseSupplierQuery;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalPurchaseSupplierGatewayAdapter implements PurchaseSupplierGateway {
    private final PurchaseSupplierQuery query;
    public LocalPurchaseSupplierGatewayAdapter(PurchaseSupplierQuery query) {
        this.query = query;
    }
    @Override
    public Optional<Entry> findById(UUID id) {
        return query.findById(id).map(entry -> new Entry(entry.id(), entry.name(), entry.rif()));
    }

    @Override
    public List<Entry> findByIds(Collection<UUID> ids) {
        return query.findByIds(ids).stream()
                .map(entry -> new Entry(entry.id(), entry.name(), entry.rif()))
                .toList();
    }
}
