package com.cashly.cashly_api.partners.suppliers.infrastructure.integration;

import com.cashly.cashly_api.partners.suppliers.application.ports.in.PurchaseSupplierQuery;
import com.cashly.cashly_api.partners.suppliers.domain.entity.Supplier;
import com.cashly.cashly_api.partners.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SupplierPurchaseDirectoryAdapter implements PurchaseSupplierQuery {
    private final SupplierRepository suppliers;

    public SupplierPurchaseDirectoryAdapter(SupplierRepository suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public Optional<Entry> findById(UUID id) {
        return suppliers.findById(new SupplierId(id)).map(SupplierPurchaseDirectoryAdapter::toEntry);
    }

    @Override
    public List<Entry> findByIds(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        List<SupplierId> supplierIds = ids.stream().map(SupplierId::new).toList();
        return suppliers.findAllById(supplierIds).stream()
                .map(SupplierPurchaseDirectoryAdapter::toEntry)
                .toList();
    }

    private static Entry toEntry(Supplier supplier) {
        return new Entry(supplier.getId().getValue(), supplier.getLegalName().getValue(), supplier.getRif().getValue());
    }
}
