package com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.in.CreditNoteCashApi;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.out.CreditNoteEffectsGateway;
import com.cashly.cashly_api.partners.customers.balance.application.ports.in.CreditNoteBalanceApi;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LocalCreditNoteEffectsGateway implements CreditNoteEffectsGateway {
    private final CreditNoteCashApi cash;
    private final CreditNoteBalanceApi balances;

    public LocalCreditNoteEffectsGateway(CreditNoteCashApi cash,
            CreditNoteBalanceApi balances) {
        this.cash = cash;
        this.balances = balances;
    }

    @Override
    public void recordCashOut(BigDecimal amount, String noteNumber, UUID noteId,
            UUID authenticatedUserId, String createdByName) {
        cash.recordCreditNoteOut(amount, noteNumber, noteId,
                authenticatedUserId, createdByName);
    }

    @Override
    public void addCustomerCredit(UUID customerId, BigDecimal amount,
            UUID noteId, String noteNumber, String invoiceNumber,
            UUID authenticatedUserId) {
        balances.addCredit(customerId, amount, noteId, noteNumber,
                invoiceNumber, authenticatedUserId);
    }
}
