package com.cashly.cashly_api.customers.application.ports.in;
import java.math.BigDecimal;
import java.util.UUID;
public record CustomerCreditContextEntry(UUID customerId, String name, String rif,
        BigDecimal creditLimit, BigDecimal balance) {}
