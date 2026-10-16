package com.cashly.cashly_api.payments.credit.domain.services;

import java.math.BigDecimal;

public class CustomerCreditPolicy {
    public Decision evaluate(BigDecimal creditLimit, BigDecimal customerBalance,
                             BigDecimal pendingBalance, boolean hasOverdue) {
        BigDecimal availableCredit = creditLimit.add(customerBalance)
                .subtract(pendingBalance).max(BigDecimal.ZERO);
        return new Decision(availableCredit, !hasOverdue);
    }

    public record Decision(BigDecimal availableCredit, boolean canCreateCreditInvoice) {}
}
