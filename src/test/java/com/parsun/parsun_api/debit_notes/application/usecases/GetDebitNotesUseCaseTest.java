package com.parsun.parsun_api.debit_notes.application.usecases;

import com.parsun.parsun_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDebitNotesUseCase Unit Tests")
class GetDebitNotesUseCaseTest {

    @Mock
    private DebitNoteRepository debitNoteRepository;

    @Mock
    private InvoiceJpaRepository invoiceJpaRepository;

    private GetDebitNotesUseCase getDebitNotesUseCase;

    @BeforeEach
    void setUp() {
        getDebitNotesUseCase = new GetDebitNotesUseCase(debitNoteRepository, invoiceJpaRepository);
    }

    @Test
    void should_ReturnDebitNoteResponses_When_DebitNotesExist() {
        UUID invoiceId = UUID.randomUUID();

        DebitNote debitNote = DebitNote.reconstitute(
                DebitNoteId.of(UUID.randomUUID()), DebitNoteNumber.of(2026, 1),
                UUID.randomUUID(), invoiceId,
                BigDecimal.valueOf(1500), "Defect",
                UUID.randomUUID(), "User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId(invoiceId);
        invoice.setCustomerName("Test Customer");
        invoice.setCustomerRif("J-12345678-9");
        invoice.setInvoiceNumber("INV-2026-00001");

        when(debitNoteRepository.findAllWithFilters(any(), any(), any())).thenReturn(List.of(debitNote));
        when(invoiceJpaRepository.findAllById(List.of(invoiceId))).thenReturn(List.of(invoice));

        List<DebitNoteResponse> responses = getDebitNotesUseCase.execute(null, null, null);

        assertEquals(1, responses.size());
        DebitNoteResponse response = responses.get(0);
        assertEquals("DN-2026-0001", response.noteNumber());
        assertEquals("Test Customer", response.customerName());
        assertEquals("J-12345678-9", response.customerRif());
        assertEquals("INV-2026-00001", response.invoiceNumber());
        verify(debitNoteRepository).findAllWithFilters(null, null, null);
        verify(invoiceJpaRepository).findAllById(List.of(invoiceId));
    }

    @Test
    void should_ReturnDebitNoteResponsesWithNullInvoiceFields_When_InvoiceNotFound() {
        UUID invoiceId = UUID.randomUUID();

        DebitNote debitNote = DebitNote.reconstitute(
                DebitNoteId.of(UUID.randomUUID()), DebitNoteNumber.of(2026, 1),
                UUID.randomUUID(), invoiceId,
                BigDecimal.valueOf(1500), "Defect",
                UUID.randomUUID(), "User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(debitNoteRepository.findAllWithFilters(null, null, null)).thenReturn(List.of(debitNote));
        when(invoiceJpaRepository.findAllById(List.of(invoiceId))).thenReturn(List.of());

        List<DebitNoteResponse> responses = getDebitNotesUseCase.execute(null, null, null);

        assertEquals(1, responses.size());
        DebitNoteResponse response = responses.get(0);
        assertEquals("DN-2026-0001", response.noteNumber());
        assertNull(response.customerName());
        assertNull(response.customerRif());
        assertNull(response.invoiceNumber());
    }

    @Test
    void should_ReturnEmptyList_When_NoDebitNotesExist() {
        when(debitNoteRepository.findAllWithFilters(any(), any(), any())).thenReturn(List.of());
        when(invoiceJpaRepository.findAllById(List.of())).thenReturn(List.of());

        List<DebitNoteResponse> responses = getDebitNotesUseCase.execute(null, null, null);

        assertTrue(responses.isEmpty());
        verify(debitNoteRepository).findAllWithFilters(null, null, null);
        verify(invoiceJpaRepository).findAllById(List.of());
    }
}
