package com.cashly.cashly_api.orders.application.contracts;

import java.util.Optional;
import java.util.UUID;

public interface OrderCustomerGateway {
    Optional<Customer> findById(UUID customerId);

    record Customer(UUID id, String legalName, String rif) {}
}
