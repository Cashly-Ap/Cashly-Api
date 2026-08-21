package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.application.dto.request.ApprovePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.services.PaymentAllocationService;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovePaymentUseCaseTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentAllocationRepository allocationRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceStatusService invoiceStatusService;
    @Mock private NotificationService notificationService;
    @Mock private CashMovementService cashMovementService;
    @Mock private CreditNoteRepository creditNoteRepository;
    @Mock private DebitNoteRepository debitNoteRepository;

    private ApprovePaymentUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        PaymentAllocationService paymentAllocationService =
                new PaymentAllocationService(paymentRepository, allocationRepository);
        useCase = new ApprovePaymentUseCase(paymentRepository, allocationRepository,
                invoiceRepository, invoiceStatusService, notificationService, cashMovementService,
                creditNoteRepository, debitNoteRepository, paymentAllocationService);
    }

    private Payment pendingPayment(BigDecimal amount, UUID targetInvoiceId) {
        return Payment.create(
                PaymentNumber.of(2026, 1), customerId, amount, paymentMethodId,
                "REF-001", LocalDate.now(), null, userId, "Admin",
                PaymentStatus.PENDING, targetInvoiceId, null
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

    private void stubSave() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void should_NotApprovePendingPayment_When_RegisterIsClosed() {
        Payment payment = pendingPayment(new BigDecimal("125.00"), null);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        doThrow(new IllegalStateException("Debe abrir una caja antes de registrar una operación monetaria"))
                .when(cashMovementService).requireOpenRegister();

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin"));

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void should_AllocateFullAmountToTargetInvoice_When_TargetInvoiceIdIsSet() {
        UUID invoiceId = UUID.randomUUID();
        Payment payment = pendingPayment(new BigDecimal("500.00"), invoiceId);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceRepository.findById(new InvoiceId(invoiceId))).thenReturn(Optional.of(unpaidInvoice(invoiceId, BigDecimal.ONE, new BigDecimal("500.00"))));
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(new BigDecimal("500.00"), response.appliedAmount());
        verify(invoiceStatusService).recalculate(invoiceId);
    }

    @Test
    void should_AllocateExactAmount_When_PaymentExactlyCoversTotalDebt() {
        UUID invoice1Id = UUID.randomUUID();
        UUID invoice2Id = UUID.randomUUID();
        Payment payment = pendingPayment(new BigDecimal("500.00"), null);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of(
                        unpaidInvoice(invoice1Id, BigDecimal.ONE, new BigDecimal("300.00")),
                        unpaidInvoice(invoice2Id, BigDecimal.ONE, new BigDecimal("200.00"))
                ));
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(new BigDecimal("500.00"), response.appliedAmount());
        verify(invoiceStatusService).recalculate(invoice1Id);
        verify(invoiceStatusService).recalculate(invoice2Id);
    }

    @Test
    void should_AllocateOnlyToOldestInvoice_When_PaymentIsSmallerThanTotalDebt() {
        UUID invoice1Id = UUID.randomUUID();
        UUID invoice2Id = UUID.randomUUID();
        Payment payment = pendingPayment(new BigDecimal("250.00"), null);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of(
                        unpaidInvoice(invoice1Id, BigDecimal.ONE, new BigDecimal("300.00")),
                        unpaidInvoice(invoice2Id, BigDecimal.ONE, new BigDecimal("200.00"))
                ));
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(new BigDecimal("250.00"), response.appliedAmount());
        verify(invoiceStatusService, times(1)).recalculate(invoice1Id);
        verify(invoiceStatusService, never()).recalculate(invoice2Id);
    }

    @Test
    void should_AllocateOnlyUpToTotalDebt_When_PaymentIsLargerThanTotalDebt() {
        UUID invoice1Id = UUID.randomUUID();
        UUID invoice2Id = UUID.randomUUID();
        Payment payment = pendingPayment(new BigDecimal("700.00"), null);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of(
                        unpaidInvoice(invoice1Id, BigDecimal.ONE, new BigDecimal("300.00")),
                        unpaidInvoice(invoice2Id, BigDecimal.ONE, new BigDecimal("200.00"))
                ));
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(new BigDecimal("500.00"), response.appliedAmount());
    }

    @Test
    void should_SucceedWithZeroAppliedAmount_When_CustomerHasNoUnpaidInvoices() {
        Payment payment = pendingPayment(new BigDecimal("500.00"), null);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of());
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(BigDecimal.ZERO, response.appliedAmount());
        assertEquals(PaymentStatus.APPLIED.name(), response.status());
        verify(allocationRepository, never()).save(any(PaymentAllocation.class));
    }

    @Test
    void should_RecordIncome_When_PendingPaymentIsApproved() {
        Payment payment = pendingPayment(new BigDecimal("125.00"), null);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of());
        stubSave();

        useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        verify(cashMovementService).record(
                CashMovementDirection.IN, CashMovementType.PAYMENT,
                new BigDecimal("125.00"), paymentMethodId, null,
                "PAYMENT", payment.getId().value(), userId, "Admin");
    }
}
