package com.cashly.cashly_api.customers.balance.infrastructure.integration;

import com.cashly.cashly_api.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.domain.valueobjects.BalanceType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

class LocalCreditNoteBalanceApiTest {
    @Test
    void whenAddingCreditNoteSurplus_thenUsesCreditNoteBalanceTypes() {
        AddToCustomerBalanceUseCase balances = mock(AddToCustomerBalanceUseCase.class);
        LocalCreditNoteBalanceApi api = new LocalCreditNoteBalanceApi(balances);
        UUID customerId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        api.addCredit(customerId, new BigDecimal("7.00"), noteId, "CN-1", "FAC-1", userId);

        verify(balances).execute(customerId, BalanceType.CREDIT_NOTE, new BigDecimal("7.00"),
                BalanceType.CREDIT_NOTE, noteId, "CN-1", "FAC-1", userId);
    }
}
