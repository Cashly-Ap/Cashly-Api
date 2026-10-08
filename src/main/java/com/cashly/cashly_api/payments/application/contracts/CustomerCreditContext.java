package com.cashly.cashly_api.payments.application.contracts;
import java.math.BigDecimal;
import java.util.UUID;
public record CustomerCreditContext(UUID customerId, String name, String rif,
        BigDecimal creditLimit, BigDecimal balance) {}
