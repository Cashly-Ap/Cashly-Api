package com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model;
import java.math.BigDecimal;
import java.util.UUID;
public record CustomerCreditContext(UUID customerId, String name, String rif,
        BigDecimal creditLimit, BigDecimal balance) {
}
