package com.cashly.cashly_api.products.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.products.application.ports.out.ProductReferenceGateway;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalProductReferenceGatewayAdapter implements ProductReferenceGateway {
    private final com.cashly.cashly_api.orders.application.ports.in.ProductReferenceQuery orders;
    private final com.cashly.cashly_api.invoices.application.ports.in.ProductReferenceQuery invoices;
    private final com.cashly.cashly_api.returns.application.ports.in.ProductReferenceQuery returns;
    private final com.cashly.cashly_api.warehouses.application.ports.in.ProductReferenceQuery warehouses;
    public LocalProductReferenceGatewayAdapter(
            com.cashly.cashly_api.orders.application.ports.in.ProductReferenceQuery orders,
            com.cashly.cashly_api.invoices.application.ports.in.ProductReferenceQuery invoices,
            com.cashly.cashly_api.returns.application.ports.in.ProductReferenceQuery returns,
            com.cashly.cashly_api.warehouses.application.ports.in.ProductReferenceQuery warehouses) {
        this.orders = orders;
        this.invoices = invoices;
        this.returns = returns;
        this.warehouses = warehouses;
    }
    @Override
    public Set<UUID> findReferencedProductIds(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Set.of();
        }
        Set<UUID> referenced = new HashSet<>(orders.findReferencedProductIds(ids));
        referenced.addAll(invoices.findReferencedProductIds(ids));
        referenced.addAll(returns.findReferencedProductIds(ids));
        referenced.addAll(warehouses.findReferencedProductIds(ids));
        return Set.copyOf(referenced);
    }
}
