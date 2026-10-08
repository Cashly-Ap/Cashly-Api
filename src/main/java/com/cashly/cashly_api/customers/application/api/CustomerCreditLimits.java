package com.cashly.cashly_api.customers.application.api;

import java.math.BigDecimal;

public record CustomerCreditLimits(BigDecimal creditLimit, BigDecimal availableCredit) {}
