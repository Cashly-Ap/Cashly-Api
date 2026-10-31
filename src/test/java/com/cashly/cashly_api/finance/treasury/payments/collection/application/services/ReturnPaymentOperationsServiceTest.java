package com.cashly.cashly_api.finance.treasury.payments.collection.application.services;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in.ReturnPaymentOperations;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReturnPaymentOperationsServiceTest {
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final PaymentAllocationRepository allocations = mock(
            PaymentAllocationRepository.class);
    private final PaymentAllocationService cancellations = mock(
            PaymentAllocationService.class);
    private final ReturnPaymentOperationsService service = new ReturnPaymentOperationsService(
            payments, allocations, cancellations);
    @Test
    void should_preserve_every_allocation_snapshot_and_cancel_each_distinct_payment_once() {
        UUID invoiceId = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        when(allocations.findByInvoiceId(invoiceId)).thenReturn(List.of(
                PaymentAllocation.create(first, invoiceId, new BigDecimal("2")),
                PaymentAllocation.create(first, invoiceId, new BigDecimal("3")),
                PaymentAllocation.create(second, invoiceId,
                        new BigDecimal("4"))));
        when(allocations.getAllocatedAmountByInvoiceId(invoiceId))
                .thenReturn(new BigDecimal("9"));
        assertThat(service.allocatedAmount(invoiceId))
                .isEqualByComparingTo("9");
        assertThat(service.capture(invoiceId)).containsExactly(
                new ReturnPaymentOperations.AllocationSnapshot(first, invoiceId,
                        new BigDecimal("2")),
                new ReturnPaymentOperations.AllocationSnapshot(first, invoiceId,
                        new BigDecimal("3")),
                new ReturnPaymentOperations.AllocationSnapshot(second,
                        invoiceId, new BigDecimal("4")));
        service.cancelInvoicePayments(invoiceId);
        verify(cancellations).cancelPayment(first, "Return created");
        verify(cancellations).cancelPayment(second, "Return created");
        verifyNoMoreInteractions(cancellations);
    }
    @Test
    void should_reactivate_real_payment_then_restore_exact_allocation() {
        UUID invoiceId = UUID.randomUUID();
        var payment = Payment.create(PaymentNumber.of(2026, 1),
                UUID.randomUUID(), new BigDecimal("25"), UUID.randomUUID(),
                null, LocalDate.now(), "Payment", UUID.randomUUID(), "Operator",
                PaymentStatus.PENDING);
        payment.cancel("Return created");
        UUID id = payment.getId().value();
        when(payments.findById(PaymentId.of(id)))
                .thenReturn(Optional.of(payment));
        service.restore(List.of(new ReturnPaymentOperations.AllocationSnapshot(
                id, invoiceId, new BigDecimal("15"))));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPLIED);
        var ordered = inOrder(payments, allocations);
        ordered.verify(payments).findById(PaymentId.of(id));
        ordered.verify(payments).save(payment);
        ordered.verify(allocations)
                .save(argThat(allocation -> allocation.getPaymentId().equals(id)
                        && allocation.getInvoiceId().equals(invoiceId)
                        && allocation.getAllocatedAmount()
                                .compareTo(new BigDecimal("15")) == 0));
        ordered.verifyNoMoreInteractions();
    }
    @Test
    void should_preserve_missing_payment_error_and_stop_before_writes() {
        UUID paymentId = UUID.randomUUID();
        assertThatThrownBy(() -> service
                .restore(List.of(new ReturnPaymentOperations.AllocationSnapshot(
                        paymentId, UUID.randomUUID(), BigDecimal.TEN))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment not found: " + paymentId);
        verify(payments, never()).save(any());
        verifyNoInteractions(allocations);
    }
}
