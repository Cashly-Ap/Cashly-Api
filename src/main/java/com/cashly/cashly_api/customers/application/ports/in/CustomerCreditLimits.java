package com.cashly.cashly_api.customers.application.ports.in;

import java.math.BigDecimal;

public record CustomerCreditLimits(BigDecimal creditLimit, BigDecimal availableCredit) {}
