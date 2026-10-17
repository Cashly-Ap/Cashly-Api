package com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.in.CreditNoteCashApi;
import com.cashly.cashly_api.customers.balance.application.ports.in.CreditNoteBalanceApi;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

class LocalCreditNoteEffectsGatewayTest {
    @Test
    void whenRecordingCreditNoteEffects_thenDelegatesCashOutAndCustomerCredit() {
        CreditNoteCashApi cash = mock(CreditNoteCashApi.class);
        CreditNoteBalanceApi balances = mock(CreditNoteBalanceApi.class);
        LocalCreditNoteEffectsGateway gateway = new LocalCreditNoteEffectsGateway(cash, balances);
        UUID customerId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        gateway.recordCashOut(new BigDecimal("25.00"), "CN-2026-1", noteId, userId, "User");
        gateway.addCustomerCredit(customerId, new BigDecimal("5.00"), noteId,
                "CN-2026-1", "FAC-1", userId);

        verify(cash).recordCreditNoteOut(new BigDecimal("25.00"), "CN-2026-1", noteId, userId, "User");
        verify(balances).addCredit(customerId, new BigDecimal("5.00"), noteId,
                "CN-2026-1", "FAC-1", userId);
    }
}
