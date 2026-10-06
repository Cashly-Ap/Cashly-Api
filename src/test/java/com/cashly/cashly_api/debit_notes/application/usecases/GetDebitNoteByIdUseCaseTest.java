package com.cashly.cashly_api.debit_notes.application.usecases;

import com.cashly.cashly_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.debit_notes.domain.entities.DebitNote;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDebitNoteByIdUseCase Unit Tests")
class GetDebitNoteByIdUseCaseTest {

    @Mock
    private DebitNoteRepository debitNoteRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    private GetDebitNoteByIdUseCase getDebitNoteByIdUseCase;

    @BeforeEach
    void setUp() {
        getDebitNoteByIdUseCase = new GetDebitNoteByIdUseCase(debitNoteRepository, invoiceRepository);
    }

    @Test
    void should_ReturnDebitNoteResponse_When_DebitNoteExists() {
        UUID id = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        DebitNoteNumber noteNumber = DebitNoteNumber.of(2026, 1);

        DebitNote debitNote = DebitNote.reconstitute(
                DebitNoteId.of(id), noteNumber, UUID.randomUUID(), invoiceId,
                BigDecimal.valueOf(1500), "Defect", UUID.randomUUID(), "User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        Invoice invoice = mock(Invoice.class);
        InvoiceNumber invoiceNumber = mock(InvoiceNumber.class);
        when(invoice.getCustomerName()).thenReturn("Test Customer");
        when(invoice.getCustomerRif()).thenReturn("J-12345678-9");
        when(invoice.getInvoiceNumber()).thenReturn(invoiceNumber);
        when(invoiceNumber.value()).thenReturn("INV-2026-00001");

        when(debitNoteRepository.findById(DebitNoteId.of(id))).thenReturn(Optional.of(debitNote));
        when(invoiceRepository.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.of(invoice));

        DebitNoteResponse response = getDebitNoteByIdUseCase.execute(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("DN-2026-0001", response.noteNumber());
        assertEquals("Test Customer", response.customerName());
        assertEquals("J-12345678-9", response.customerRif());
        assertEquals("INV-2026-00001", response.invoiceNumber());
        verify(debitNoteRepository).findById(DebitNoteId.of(id));
        verify(invoiceRepository).findById(InvoiceId.of(invoiceId));
    }

    @Test
    void should_ReturnResponseWithNullInvoiceFields_When_InvoiceNotFound() {
        UUID id = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        DebitNote debitNote = DebitNote.reconstitute(
                DebitNoteId.of(id), DebitNoteNumber.of(2026, 1), UUID.randomUUID(), invoiceId,
                BigDecimal.valueOf(1500), "Defect", UUID.randomUUID(), "User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(debitNoteRepository.findById(DebitNoteId.of(id))).thenReturn(Optional.of(debitNote));
        when(invoiceRepository.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.empty());

        DebitNoteResponse response = getDebitNoteByIdUseCase.execute(id);

        assertNotNull(response);
        assertNull(response.customerName());
        assertNull(response.customerRif());
        assertNull(response.invoiceNumber());
    }

    @Test
    void should_ThrowIllegalArgumentException_When_DebitNoteNotFound() {
        UUID id = UUID.randomUUID();

        when(debitNoteRepository.findById(DebitNoteId.of(id))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getDebitNoteByIdUseCase.execute(id)
        );

        assertEquals("Debit note not found: " + id, exception.getMessage());
        verify(debitNoteRepository).findById(DebitNoteId.of(id));
        verifyNoInteractions(invoiceRepository);
    }
}
