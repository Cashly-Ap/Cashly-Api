package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.application.dto.request.ApprovePaymentRequest;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
    @Mock private Payment payment;

    private ApprovePaymentUseCase useCase;
    private final UUID paymentId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ApprovePaymentUseCase(paymentRepository, allocationRepository,
                invoiceRepository, invoiceStatusService, notificationService, cashMovementService);
        when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.of(payment));
    }

    @Test
    void should_NotApprovePendingPayment_When_RegisterIsClosed() {
        doThrow(new IllegalStateException("Debe abrir una caja antes de registrar una operación monetaria"))
                .when(cashMovementService).requireOpenRegister();

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(paymentId, new ApprovePaymentRequest("Verificado"), userId, "Admin"));

        verify(payment, never()).approve("Verificado", userId, "Admin");
        verify(paymentRepository, never()).save(payment);
    }

    @Test
    void should_RecordIncome_When_PendingPaymentIsApproved() {
        when(payment.getNotes()).thenReturn(null);
        when(payment.getId()).thenReturn(PaymentId.of(paymentId));
        when(payment.getPaymentNumber()).thenReturn(PaymentNumber.of(2026, 1));
        when(payment.getAmount()).thenReturn(new BigDecimal("125.00"));
        when(payment.getPaymentMethodId()).thenReturn(paymentMethodId);
        when(payment.getCustomerId()).thenReturn(customerId);
        when(payment.getStatus()).thenReturn(PaymentStatus.APPLIED);
        when(payment.getAppliedAmount()).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(payment)).thenReturn(payment);

        useCase.execute(paymentId, new ApprovePaymentRequest("Verificado"), userId, "Admin");

        verify(cashMovementService).record(
                CashMovementDirection.IN, CashMovementType.PAYMENT,
                new BigDecimal("125.00"), paymentMethodId, null,
                "PAYMENT", paymentId, userId, "Admin");
    }
}
