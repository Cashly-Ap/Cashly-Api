package com.cashly.cashly_api.finance.treasury.receivables.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceivablesCustomerProfileGateway {
    Optional<CustomerProfile> findById(UUID customerId);

    record CustomerProfile(String legalName, String rif, BigDecimal creditLimit,
                           BigDecimal currentBalance, List<String> phones, List<String> emails) {}
}
