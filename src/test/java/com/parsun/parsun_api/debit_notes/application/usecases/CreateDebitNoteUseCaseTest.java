package com.parsun.parsun_api.debit_notes.application.usecases;

import com.parsun.parsun_api.debit_notes.application.dto.request.CreateDebitNoteRequest;
import com.parsun.parsun_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateDebitNoteUseCase Unit Tests")
class CreateDebitNoteUseCaseTest {

    @Mock
    private DebitNoteRepository debitNoteRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceStatusService invoiceStatusService;

    private CreateDebitNoteUseCase createDebitNoteUseCase;
    private UUID authenticatedUserId;
    private UUID invoiceId;

    @BeforeEach
    void setUp() {
        createDebitNoteUseCase = new CreateDebitNoteUseCase(
                debitNoteRepository, invoiceRepository, invoiceStatusService
        );
        authenticatedUserId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
    }

    @Test
    void should_CreateDebitNote_When_ValidRequest() {
        CreateDebitNoteRequest request = new CreateDebitNoteRequest(
                invoiceId, UUID.randomUUID(), new BigDecimal("50.00"), "Price adjustment"
        );
        Invoice invoice = mock(Invoice.class);
        when(invoice.getStatus()).thenReturn(InvoiceStatus.PENDING_PAYMENT);

        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));
        when(debitNoteRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(debitNoteRepository.save(any(DebitNote.class))).thenAnswer(inv -> inv.getArgument(0));

        DebitNoteResponse response = createDebitNoteUseCase.execute(request, authenticatedUserId);

        assertNotNull(response);
        verify(debitNoteRepository).save(any(DebitNote.class));
        verify(invoiceStatusService).recalculate(any(UUID.class));
    }

    @Test
    void should_ThrowException_When_InvoiceNotFound() {
        CreateDebitNoteRequest request = new CreateDebitNoteRequest(
                invoiceId, UUID.randomUUID(), new BigDecimal("50.00"), "Price adjustment"
        );

        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> createDebitNoteUseCase.execute(request, authenticatedUserId));
    }

    @Test
    void should_ThrowException_When_InvoiceIsCancelled() {
        CreateDebitNoteRequest request = new CreateDebitNoteRequest(
                invoiceId, UUID.randomUUID(), new BigDecimal("50.00"), "Price adjustment"
        );
        Invoice invoice = mock(Invoice.class);
        InvoiceNumber invNum = mock(InvoiceNumber.class);
        when(invoice.getStatus()).thenReturn(InvoiceStatus.CANCELLED);
        when(invoice.getInvoiceNumber()).thenReturn(invNum);
        when(invNum.value()).thenReturn("FAC-001");

        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));

        assertThrows(IllegalStateException.class,
                () -> createDebitNoteUseCase.execute(request, authenticatedUserId));
    }

    @Test
    void should_ThrowException_When_InvoiceIsFullyReturned() {
        CreateDebitNoteRequest request = new CreateDebitNoteRequest(
                invoiceId, UUID.randomUUID(), new BigDecimal("50.00"), "Price adjustment"
        );
        Invoice invoice = mock(Invoice.class);
        InvoiceNumber invNum = mock(InvoiceNumber.class);
        when(invoice.getStatus()).thenReturn(InvoiceStatus.FULLY_RETURNED);
        when(invoice.getInvoiceNumber()).thenReturn(invNum);
        when(invNum.value()).thenReturn("FAC-001");

        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));

        assertThrows(IllegalStateException.class,
                () -> createDebitNoteUseCase.execute(request, authenticatedUserId));
    }
}
