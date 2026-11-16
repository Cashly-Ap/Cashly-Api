package com.cashly.cashly_api.partners.customers.credit.application.ports.in;

import java.math.BigDecimal;

public record CustomerCreditLimits(BigDecimal creditLimit,
        BigDecimal availableCredit) {
}
