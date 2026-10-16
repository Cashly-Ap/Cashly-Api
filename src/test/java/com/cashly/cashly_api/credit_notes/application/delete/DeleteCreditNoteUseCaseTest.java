package com.cashly.cashly_api.credit_notes.application.delete;

import com.cashly.cashly_api.credit_notes.domain.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.CreditNoteRepository;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.credit_notes.application.ports.out.CreditNoteInvoiceGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCreditNoteUseCase Unit Tests")
class DeleteCreditNoteUseCaseTest {

    @Mock
    private CreditNoteRepository creditNoteRepository;
    @Mock
    private CreditNoteInvoiceGateway invoices;

    private DeleteCreditNoteUseCase deleteCreditNoteUseCase;

    @BeforeEach
    void setUp() {
        deleteCreditNoteUseCase = new DeleteCreditNoteUseCase(
                creditNoteRepository, invoices
        );
    }

    @Nested
    @DisplayName("Successful deletion")
    class SuccessfulDeletion {

        @Test
        void should_DeleteCreditNote_When_ItExists() {
            UUID noteId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            CreditNote creditNote = mock(CreditNote.class);
            when(creditNote.getInvoiceId()).thenReturn(invoiceId);
            when(creditNoteRepository.findById(new CreditNoteId(noteId)))
                    .thenReturn(Optional.of(creditNote));

            deleteCreditNoteUseCase.execute(noteId);

            verify(creditNoteRepository).delete(new CreditNoteId(noteId));
            verify(invoices).recalculate(invoiceId);
        }

        @Test
        void should_ThrowException_When_CreditNoteNotFound() {
            UUID noteId = UUID.randomUUID();
            when(creditNoteRepository.findById(new CreditNoteId(noteId)))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> deleteCreditNoteUseCase.execute(noteId));
            verify(creditNoteRepository, never()).delete(any());
        }
    }
}
