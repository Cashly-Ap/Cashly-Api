package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.debit_notes.application.ports.out.DebitNoteInvoiceGateway;
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
@DisplayName("DeleteDebitNoteUseCase Unit Tests")
class DeleteDebitNoteUseCaseTest {

    @Mock
    private DebitNoteRepository debitNoteRepository;
    @Mock
    private DebitNoteInvoiceGateway invoices;

    private DeleteDebitNoteUseCase deleteDebitNoteUseCase;

    @BeforeEach
    void setUp() {
        deleteDebitNoteUseCase = new DeleteDebitNoteUseCase(
                debitNoteRepository, invoices
        );
    }

    @Nested
    @DisplayName("Successful deletion")
    class SuccessfulDeletion {

        @Test
        void should_DeleteDebitNote_When_ItExists() {
            UUID noteId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            DebitNote debitNote = mock(DebitNote.class);
            when(debitNote.getInvoiceId()).thenReturn(invoiceId);
            when(debitNoteRepository.findById(new DebitNoteId(noteId)))
                    .thenReturn(Optional.of(debitNote));

            deleteDebitNoteUseCase.execute(noteId);

            verify(debitNoteRepository).delete(new DebitNoteId(noteId));
            verify(invoices).recalculate(invoiceId);
        }

        @Test
        void should_ThrowException_When_DebitNoteNotFound() {
            UUID noteId = UUID.randomUUID();
            when(debitNoteRepository.findById(new DebitNoteId(noteId)))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> deleteDebitNoteUseCase.execute(noteId));
            verify(debitNoteRepository, never()).delete(any());
        }
    }
}
