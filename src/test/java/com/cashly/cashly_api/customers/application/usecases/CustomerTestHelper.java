package com.cashly.cashly_api.customers.application.usecases;

import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.entities.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.domain.valueobjects.*;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

final class CustomerTestHelper {

    private CustomerTestHelper() {
    }

    static Customer buildCustomer(UUID id) {
        return Customer.reconstitute(
                new CustomerId(id),
                new CustomerCode(1001L),
                new LegalName("Test Corp"),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Test"),
                new CreditDays(30),
                new CreditLimit(BigDecimal.ZERO),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(), List.of(), List.of(),
                null, null
        );
    }

    static Customer buildCustomer(UUID id, boolean active) {
        return Customer.reconstitute(
                new CustomerId(id),
                new CustomerCode(1001L),
                new LegalName("Test Corp"),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Test"),
                new CreditDays(30),
                new CreditLimit(BigDecimal.ZERO),
                active,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(), List.of(), List.of(),
                null, null
        );
    }

    static Warehouse buildWarehouse(UUID id) {
        return Warehouse.reconstitute(
                id, "WH-001", "Main Warehouse", "123 Main St",
                "State", "Municipality", "1000", true,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    static CustomerWarehouseAccess buildAccess(UUID customerId, UUID warehouseId) {
        return CustomerWarehouseAccess.create(customerId, warehouseId, UUID.randomUUID());
    }
}
