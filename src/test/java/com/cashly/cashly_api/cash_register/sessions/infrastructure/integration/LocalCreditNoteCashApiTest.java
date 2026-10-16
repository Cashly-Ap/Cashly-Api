package com.cashly.cashly_api.cash_register.sessions.infrastructure.integration;

import com.cashly.cashly_api.cash_register.sessions.application.movements.CashMovementService;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LocalCreditNoteCashApiTest {
    @Test
    void whenRecordingCreditNoteOut_thenUsesExistingCashMovementSemantics() {
        CashMovementService movements = mock(CashMovementService.class);
        LocalCreditNoteCashApi api = new LocalCreditNoteCashApi(movements);
        UUID noteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        api.recordCreditNoteOut(new BigDecimal("40.00"), "CN-1", noteId, userId, "User");

        verify(movements).record(eq(CashMovementDirection.OUT), eq(CashMovementType.CREDIT_NOTE),
                eq(new BigDecimal("40.00")), isNull(), eq("Nota de crédito CN-1"),
                eq("CREDIT_NOTE"), eq(noteId), eq(userId), eq("User"));
    }
}
