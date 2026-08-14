package com.parsun.parsun_api.credit_notes.application.usecases;

import com.parsun.parsun_api.credit_notes.application.dto.request.CreateCreditNoteRequest;
import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCreditNoteUseCase Unit Tests")
class CreateCreditNoteUseCaseTest {

    @Mock
    private CreditNoteRepository creditNoteRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceStatusService invoiceStatusService;
    @Mock
    private PaymentAllocationRepository paymentAllocationRepository;
    @Mock
    private DebitNoteRepository debitNoteRepository;
    @Mock
    private AddToCustomerBalanceUseCase addToCustomerBalanceUseCase;
    @Mock
    private CashMovementService cashMovementService;

    private CreateCreditNoteUseCase createCreditNoteUseCase;
    private UUID authenticatedUserId;
    private UUID invoiceId;

    @BeforeEach
    void setUp() {
        createCreditNoteUseCase = new CreateCreditNoteUseCase(
                creditNoteRepository, invoiceRepository, invoiceStatusService,
                paymentAllocationRepository, debitNoteRepository, addToCustomerBalanceUseCase,
                cashMovementService
        );
        authenticatedUserId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();
    }

    @Test
    void should_CreateCreditNote_When_ValidRequest() {
        CreateCreditNoteRequest request = new CreateCreditNoteRequest(
                invoiceId, UUID.randomUUID(), new BigDecimal("150.00"), "Product damaged"
        );
        Invoice invoice = mock(Invoice.class);
        when(invoice.getStatus()).thenReturn(InvoiceStatus.PENDING_PAYMENT);
        when(invoice.calculateTotal(any(), any())).thenReturn(new BigDecimal("1000.00"));

        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));
        when(creditNoteRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(creditNoteRepository.save(any(CreditNote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(creditNoteRepository.findByInvoiceId(any())).thenReturn(List.of());
        when(debitNoteRepository.findByInvoiceId(any())).thenReturn(List.of());
        when(paymentAllocationRepository.findByInvoiceId(any())).thenReturn(List.of());

        CreditNoteResponse response = createCreditNoteUseCase.execute(request, authenticatedUserId);

        assertNotNull(response);
        verify(creditNoteRepository).save(any(CreditNote.class));
        verify(invoiceStatusService).recalculate(any(UUID.class));
        verify(cashMovementService).record(
                eq(CashMovementDirection.OUT), eq(CashMovementType.CREDIT_NOTE),
                eq(new BigDecimal("150.00")), isNull(), contains("CN-"),
                eq("CREDIT_NOTE"), any(UUID.class), eq(authenticatedUserId), eq("User"));
    }

    @Test
    void should_ThrowException_When_InvoiceNotFound() {
        CreateCreditNoteRequest request = new CreateCreditNoteRequest(
                invoiceId, UUID.randomUUID(), new BigDecimal("150.00"), "Product damaged"
        );

        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> createCreditNoteUseCase.execute(request, authenticatedUserId));
    }

    @Test
    void should_ThrowException_When_InvoiceIsCancelled() {
        CreateCreditNoteRequest request = new CreateCreditNoteRequest(
                invoiceId, UUID.randomUUID(), new BigDecimal("150.00"), "Product damaged"
        );
        Invoice invoice = mock(Invoice.class);
        InvoiceNumber invNum = mock(InvoiceNumber.class);
        when(invoice.getStatus()).thenReturn(InvoiceStatus.CANCELLED);
        when(invoice.getInvoiceNumber()).thenReturn(invNum);
        when(invNum.value()).thenReturn("FAC-001");

        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));

        assertThrows(IllegalStateException.class,
                () -> createCreditNoteUseCase.execute(request, authenticatedUserId));
    }

    @Test
    void should_ThrowException_When_InvoiceIsFullyReturned() {
        CreateCreditNoteRequest request = new CreateCreditNoteRequest(
                invoiceId, UUID.randomUUID(), new BigDecimal("150.00"), "Product damaged"
        );
        Invoice invoice = mock(Invoice.class);
        InvoiceNumber invNum = mock(InvoiceNumber.class);
        when(invoice.getStatus()).thenReturn(InvoiceStatus.FULLY_RETURNED);
        when(invoice.getInvoiceNumber()).thenReturn(invNum);
        when(invNum.value()).thenReturn("FAC-001");

        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));

        assertThrows(IllegalStateException.class,
                () -> createCreditNoteUseCase.execute(request, authenticatedUserId));
    }
}
