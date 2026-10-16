package com.cashly.cashly_api.payments.collection.application.approve.use_case;

import com.cashly.cashly_api.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.payments.collection.application.ports.out.PaymentInvoiceGateway;
import com.cashly.cashly_api.payments.collection.application.ports.out.PaymentNotificationGateway;
import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.payments.collection.application.approve.request.ApprovePaymentRequest;
import com.cashly.cashly_api.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.payments.collection.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentStatus;
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
    @Mock private PaymentInvoiceGateway invoiceGateway;
    @Mock private PaymentNotificationGateway notificationGateway;
    @Mock private CashRegisterGateway cashRegisterGateway;

    private ApprovePaymentUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        PaymentAllocationService paymentAllocationService =
                new PaymentAllocationService(paymentRepository, allocationRepository);
        useCase = new ApprovePaymentUseCase(paymentRepository, allocationRepository,
                invoiceGateway, notificationGateway, cashRegisterGateway, paymentAllocationService);
    }

    private Payment pendingPayment(BigDecimal amount) {
        return Payment.create(
                PaymentNumber.of(2026, 1), customerId, amount, paymentMethodId,
                "REF-001", LocalDate.now(), null, userId, "Admin",
                PaymentStatus.PENDING
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

    private List<PaymentInvoiceGateway.InvoiceSnapshot> snapshots(Invoice... invoices) {
        return java.util.Arrays.stream(invoices).map(invoice -> new PaymentInvoiceGateway.InvoiceSnapshot(
                invoice.getId().value(), invoice.getCustomerId(), invoice.getInvoiceNumber().value(),
                invoice.calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO))).toList();
    }

    private void stubSave() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void should_NotApprovePendingPayment_When_RegisterIsClosed() {
        Payment payment = pendingPayment(new BigDecimal("125.00"));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        doThrow(new IllegalStateException("Debe abrir una caja antes de registrar una operación monetaria"))
                .when(cashRegisterGateway).requireOpenRegister();

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin"));

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void should_AllocateFullAmountToOldestInvoice_When_PaymentMatchesDebt() {
        UUID invoiceId = UUID.randomUUID();
        Payment payment = pendingPayment(new BigDecimal("500.00"));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots(unpaidInvoice(invoiceId, BigDecimal.ONE, new BigDecimal("500.00"))));
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(new BigDecimal("500.00"), response.appliedAmount());
        verify(invoiceGateway).recalculateStatus(invoiceId);
    }

    @Test
    void should_AllocateExactAmount_When_PaymentExactlyCoversTotalDebt() {
        UUID invoice1Id = UUID.randomUUID();
        UUID invoice2Id = UUID.randomUUID();
        Payment payment = pendingPayment(new BigDecimal("500.00"));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots(
                        unpaidInvoice(invoice1Id, BigDecimal.ONE, new BigDecimal("300.00")),
                        unpaidInvoice(invoice2Id, BigDecimal.ONE, new BigDecimal("200.00"))
                ));
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(new BigDecimal("500.00"), response.appliedAmount());
        verify(invoiceGateway).recalculateStatus(invoice1Id);
        verify(invoiceGateway).recalculateStatus(invoice2Id);
    }

    @Test
    void should_AllocateOnlyToOldestInvoice_When_PaymentIsSmallerThanTotalDebt() {
        UUID invoice1Id = UUID.randomUUID();
        UUID invoice2Id = UUID.randomUUID();
        Payment payment = pendingPayment(new BigDecimal("250.00"));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots(
                        unpaidInvoice(invoice1Id, BigDecimal.ONE, new BigDecimal("300.00")),
                        unpaidInvoice(invoice2Id, BigDecimal.ONE, new BigDecimal("200.00"))
                ));
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(new BigDecimal("250.00"), response.appliedAmount());
        verify(invoiceGateway, times(1)).recalculateStatus(invoice1Id);
        verify(invoiceGateway, never()).recalculateStatus(invoice2Id);
    }

    @Test
    void should_AllocateOnlyUpToTotalDebt_When_PaymentIsLargerThanTotalDebt() {
        UUID invoice1Id = UUID.randomUUID();
        UUID invoice2Id = UUID.randomUUID();
        Payment payment = pendingPayment(new BigDecimal("700.00"));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots(
                        unpaidInvoice(invoice1Id, BigDecimal.ONE, new BigDecimal("300.00")),
                        unpaidInvoice(invoice2Id, BigDecimal.ONE, new BigDecimal("200.00"))
                ));
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(new BigDecimal("500.00"), response.appliedAmount());
    }

    @Test
    void should_SucceedWithZeroAppliedAmount_When_CustomerHasNoUnpaidInvoices() {
        Payment payment = pendingPayment(new BigDecimal("500.00"));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots());
        stubSave();

        PaymentResponse response = useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        assertEquals(BigDecimal.ZERO, response.appliedAmount());
        assertEquals(PaymentStatus.APPLIED.name(), response.status());
        verify(allocationRepository, never()).save(any(PaymentAllocation.class));
    }

    @Test
    void should_RecordIncome_When_PendingPaymentIsApproved() {
        Payment payment = pendingPayment(new BigDecimal("125.00"));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots());
        stubSave();

        useCase.execute(payment.getId().value(), new ApprovePaymentRequest("Verificado"), userId, "Admin");

        verify(cashRegisterGateway).recordMovement(new CashRegisterGateway.Movement(
                CashRegisterGateway.Direction.IN, CashRegisterGateway.Type.PAYMENT,
                new CashRegisterGateway.PaymentEntry(new BigDecimal("125.00"), paymentMethodId,
                        null, payment.getId().value(), userId, "Admin")));
        verify(notificationGateway).approved(customerId, payment.getId().value());
    }
}
