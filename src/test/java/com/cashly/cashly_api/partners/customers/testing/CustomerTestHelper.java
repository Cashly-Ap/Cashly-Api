package com.cashly.cashly_api.partners.customers.testing;

import com.cashly.cashly_api.partners.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.entity.CustomerWarehouseAccess;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CreditDays;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CreditLimit;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerAlias;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerCode;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerType;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.LegalName;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.Rif;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class CustomerTestHelper {

    private CustomerTestHelper() {
    }

    public static Customer buildCustomer(UUID id) {
        return Customer.reconstitute(new CustomerId(id),
                new CustomerCode(1001L), new LegalName("Test Corp"),
                CustomerType.BUSINESS, new Rif("J-12345678-9"),
                new CustomerAlias("Test"), new CreditDays(30),
                new CreditLimit(BigDecimal.ZERO), true, LocalDateTime.now(),
                LocalDateTime.now(), List.of(), List.of(), List.of(), null,
                null);
    }

    public static Customer buildCustomer(UUID id, boolean active) {
        return Customer.reconstitute(new CustomerId(id),
                new CustomerCode(1001L), new LegalName("Test Corp"),
                CustomerType.BUSINESS, new Rif("J-12345678-9"),
                new CustomerAlias("Test"), new CreditDays(30),
                new CreditLimit(BigDecimal.ZERO), active, LocalDateTime.now(),
                LocalDateTime.now(), List.of(), List.of(), List.of(), null,
                null);
    }

    public static Warehouse buildWarehouse(UUID id) {
        return Warehouse.reconstitute(id, "WH-001", "Main Warehouse",
                "123 Main St", "State", "Municipality", "1000", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static CustomerWarehouseAccess buildAccess(UUID customerId,
            UUID warehouseId) {
        return CustomerWarehouseAccess.create(customerId, warehouseId,
                UUID.randomUUID());
    }
}
