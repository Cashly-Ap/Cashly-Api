package com.cashly.cashly_api.catalog.products.infrastructure.integration;

import com.cashly.cashly_api.catalog.products.application.ports.out.CustomerProductContextGateway;
import com.cashly.cashly_api.partners.customers.profile.application.ports.in.CustomerProductPricingQuery;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.in.CustomerWarehouseAccessQuery;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.CustomerNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.List;
import java.util.UUID;

@Component
public class LocalCustomerProductContextGatewayAdapter
        implements
            CustomerProductContextGateway {
    private final CustomerProductPricingQuery pricing;
    private final CustomerWarehouseAccessQuery access;

    public LocalCustomerProductContextGatewayAdapter(
            CustomerProductPricingQuery pricing,
            CustomerWarehouseAccessQuery access) {
        this.pricing = pricing;
        this.access = access;
    }

    @Override
    public UUID requirePriceType(List<UUID> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            throw new CustomerNotFoundException(
                    "Debe indicar al menos un cliente");
        }
        UUID customerId = customerIds.getFirst();
        return pricing.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Cliente no encontrado: " + customerId))
                .priceTypeId();
    }

    @Override
    public Set<UUID> accessibleWarehouseIds(UUID customerId) {
        return access.accessibleWarehouseIds(customerId);
    }

}
