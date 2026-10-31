package com.cashly.cashly_api.finance.treasury.payments.credit.domain.services;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerCreditPolicyTest {
    private final CustomerCreditPolicy policy = new CustomerCreditPolicy();

    @Test
    void should_add_customer_balance_and_subtract_pending_debt() {
        var decision = policy.evaluate(new BigDecimal("1000"),
                new BigDecimal("250"), new BigDecimal("700"), false);

        assertThat(decision.availableCredit()).isEqualByComparingTo("550");
        assertThat(decision.canCreateCreditInvoice()).isTrue();
    }

    @Test
    void should_clamp_credit_to_zero_when_debt_exceeds_limit_and_balance() {
        var decision = policy.evaluate(new BigDecimal("100"), BigDecimal.ZERO,
                new BigDecimal("200"), false);

        assertThat(decision.availableCredit())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_preserve_zero_credit_eligibility_without_overdue_invoices() {
        var decision = policy.evaluate(BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, false);

        assertThat(decision.canCreateCreditInvoice()).isTrue();
    }

    @Test
    void should_block_credit_invoices_when_overdue_even_with_available_credit() {
        var decision = policy.evaluate(new BigDecimal("1000"), BigDecimal.ZERO,
                BigDecimal.ZERO, true);

        assertThat(decision.availableCredit()).isEqualByComparingTo("1000");
        assertThat(decision.canCreateCreditInvoice()).isFalse();
    }
}
