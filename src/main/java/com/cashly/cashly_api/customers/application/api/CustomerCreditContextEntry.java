package com.cashly.cashly_api.customers.application.api;
import java.math.BigDecimal;
import java.util.UUID;
public record CustomerCreditContextEntry(UUID customerId, String name, String rif,
        BigDecimal creditLimit, BigDecimal balance) {}
