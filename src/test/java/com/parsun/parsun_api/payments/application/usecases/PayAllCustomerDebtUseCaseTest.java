package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.payments.application.dto.request.PayCustomerDebtRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.services.PaymentAllocationService;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayAllCustomerDebtUseCase Unit Tests")
class PayAllCustomerDebtUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentAllocationRepository allocationRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceStatusService invoiceStatusService;

    @Mock
    private CreditNoteRepository creditNoteRepository;

    @Mock
    private DebitNoteRepository debitNoteRepository;

    @Mock
    private ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;

    @Mock
    private CashMovementService cashMovementService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    private PayAllCustomerDebtUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();
    private final UUID authenticatedUserId = UUID.randomUUID();
    private final String authenticatedUserName = "Test User";

    @BeforeEach
    void setUp() {
        PaymentAllocationService paymentAllocationService =
                new PaymentAllocationService(paymentRepository, allocationRepository);
        useCase = new PayAllCustomerDebtUseCase(
                paymentRepository, allocationRepository,
                invoiceRepository, invoiceStatusService,
                creditNoteRepository, debitNoteRepository,
                consumeCustomerBalanceUseCase, cashMovementService,
                paymentAllocationService
        );
    }

    private Invoice unpaidInvoice(UUID id, BigDecimal lineQty, BigDecimal unitPrice) {
        return Invoice.reconstitute(
                InvoiceId.of(id),
                InvoiceNumber.of(2026, 1),
                customerId,
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Creator",
                null, null,
                InvoiceStatus.PENDING_PAYMENT,
                PaymentType.CREDIT,
                null,
                BigDecimal.ZERO,
                null,
                null, null,
                List.of(InvoiceLine.create(UUID.randomUUID(), "Product", "P001", lineQty, unitPrice, List.of())),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atStartOfDay()
        );
    }

    @Test
    void should_PayAllCustomerDebt_When_UnpaidInvoicesExist() {
        UUID invoice1Id = UUID.randomUUID();
        UUID invoice2Id = UUID.randomUUID();
        Invoice invoice1 = unpaidInvoice(invoice1Id, new BigDecimal("2"), new BigDecimal("250.00"));
        Invoice invoice2 = unpaidInvoice(invoice2Id, new BigDecimal("1"), new BigDecimal("300.00"));

        PayCustomerDebtRequest request = new PayCustomerDebtRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                false, null
        );

        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of(invoice1, invoice2));
        when(creditNoteRepository.findByInvoiceId(invoice1Id)).thenReturn(List.of());
        when(creditNoteRepository.findByInvoiceId(invoice2Id)).thenReturn(List.of());
        when(debitNoteRepository.findByInvoiceId(invoice1Id)).thenReturn(List.of());
        when(debitNoteRepository.findByInvoiceId(invoice2Id)).thenReturn(List.of());
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResultResponse response = useCase.execute(customerId, request, authenticatedUserId, authenticatedUserName);

        assertNotNull(response);
        assertNotNull(response.message());
        assertEquals(PaymentStatus.APPLIED.name(), response.status());
        verify(paymentRepository, org.mockito.Mockito.atLeast(1)).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertEquals(new BigDecimal("800.00"), saved.getAmount());
        verify(cashMovementService).record(
                CashMovementDirection.IN, CashMovementType.PAYMENT,
                new BigDecimal("800.00"), paymentMethodId,
                "Pago total de deuda del cliente", "PAYMENT",
                saved.getId().value(), authenticatedUserId, authenticatedUserName);
    }

    @Test
    void should_Throw_When_NoUnpaidInvoices() {
        PayCustomerDebtRequest request = new PayCustomerDebtRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                false, null
        );

        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(customerId, request, authenticatedUserId, authenticatedUserName)
        );

        assertTrue(exception.getMessage().contains("Customer has no unpaid invoices"));
    }

    @Test
    void should_Throw_When_PaymentMethodMissingAndAmountDue() {
        Invoice invoice = unpaidInvoice(UUID.randomUUID(), new BigDecimal("2"), new BigDecimal("100.00"));

        PayCustomerDebtRequest request = new PayCustomerDebtRequest(
                null, null, LocalDate.now(),
                false, null
        );

        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of(invoice));
        when(creditNoteRepository.findByInvoiceId(invoice.getId().value())).thenReturn(List.of());
        when(debitNoteRepository.findByInvoiceId(invoice.getId().value())).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(customerId, request, authenticatedUserId, authenticatedUserName)
        );

        assertTrue(exception.getMessage().contains("Payment method is required"));
    }

    @Test
    void should_RecalculateAllInvoiceStatuses_After_Payment() {
        UUID invoice1Id = UUID.randomUUID();
        Invoice invoice1 = unpaidInvoice(invoice1Id, new BigDecimal("1"), new BigDecimal("100.00"));

        PayCustomerDebtRequest request = new PayCustomerDebtRequest(
                paymentMethodId, null, LocalDate.now(),
                false, null
        );

        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of(invoice1));
        when(creditNoteRepository.findByInvoiceId(invoice1Id)).thenReturn(List.of());
        when(debitNoteRepository.findByInvoiceId(invoice1Id)).thenReturn(List.of());
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(customerId, request, authenticatedUserId, authenticatedUserName);

        verify(invoiceStatusService).recalculate(invoice1.getId().value());
    }
}
