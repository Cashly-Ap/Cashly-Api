package com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LocalDebitNoteCashApiTest {
    @Test
    void whenRecordingDebitNoteIn_thenPreservesCashMovementTypeAndReference() {
        CashMovementService movements = mock(CashMovementService.class);
        LocalDebitNoteCashApi api = new LocalDebitNoteCashApi(movements);
        UUID noteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        api.recordDebitNoteIn(new BigDecimal("40.00"), "DN-1", noteId, userId,
                "User");

        verify(movements).record(eq(CashMovementDirection.IN),
                eq(CashMovementType.DEBIT_NOTE), eq(new BigDecimal("40.00")),
                isNull(), eq("Nota de débito DN-1"), eq("DEBIT_NOTE"),
                eq(noteId), eq(userId), eq("User"));
    }
}
