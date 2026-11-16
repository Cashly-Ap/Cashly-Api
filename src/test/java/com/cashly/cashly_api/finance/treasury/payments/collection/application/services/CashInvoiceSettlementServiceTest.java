package com.cashly.cashly_api.finance.treasury.payments.collection.application.services;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in.CashInvoiceSettlement;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class CashInvoiceSettlementServiceTest {
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final PaymentAllocationRepository allocations = mock(
            PaymentAllocationRepository.class);
    private final CashRegisterGateway cashRegister = mock(
            CashRegisterGateway.class);
    private final CashInvoiceSettlementService service = new CashInvoiceSettlementService(
            payments, allocations, cashRegister);
    private final CashInvoiceSettlement.Command command = new CashInvoiceSettlement.Command(
            UUID.randomUUID(), UUID.randomUUID(), "INV-2026-0001",
            new BigDecimal("125.50"), UUID.randomUUID(), UUID.randomUUID(),
            "Operator");
    @Test
    void should_preserve_sequence_payment_metadata_allocation_and_double_save_order() {
        List<BigDecimal> appliedSnapshots = new ArrayList<>();
        when(payments.getNextSequenceForYear(LocalDate.now().getYear()))
                .thenReturn(42);
        when(payments.save(any())).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            appliedSnapshots.add(payment.getAppliedAmount());
            return payment;
        });
        service.settle(command);
        var paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        var allocationCaptor = ArgumentCaptor.forClass(PaymentAllocation.class);
        var cashCaptor = ArgumentCaptor
                .forClass(CashRegisterGateway.PaymentEntry.class);
        var ordered = inOrder(payments, cashRegister, allocations);
        ordered.verify(payments)
                .getNextSequenceForYear(LocalDate.now().getYear());
        ordered.verify(payments).save(paymentCaptor.capture());
        ordered.verify(cashRegister).recordPayment(cashCaptor.capture());
        ordered.verify(allocations).save(allocationCaptor.capture());
        ordered.verify(payments).save(paymentCaptor.capture());
        ordered.verifyNoMoreInteractions();
        Payment payment = paymentCaptor.getValue();
        assertThat(paymentCaptor.getAllValues().getFirst()).isSameAs(payment);
        assertThat(appliedSnapshots).containsExactly(BigDecimal.ZERO,
                command.amount());
        assertThat(payment.getPaymentNumber())
                .isEqualTo(PaymentNumber.of(LocalDate.now().getYear(), 42));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPLIED);
        assertThat(payment.getSource()).isEqualTo(PaymentSource.PAYMENT_METHOD);
        assertThat(payment.getCustomerId()).isEqualTo(command.customerId());
        assertThat(payment.getAmount()).isEqualTo(command.amount());
        assertThat(payment.getAppliedAmount()).isEqualTo(command.amount());
        assertThat(payment.getPaymentMethodId())
                .isEqualTo(command.paymentMethodId());
        assertThat(payment.getCreatedBy()).isEqualTo(command.actorId());
        assertThat(payment.getCreatedByName()).isEqualTo(command.actorName());
        assertThat(payment.getPaymentDate()).isEqualTo(LocalDate.now());
        assertThat(payment.getNotes())
                .isEqualTo("Pago de factura " + command.invoiceNumber());
        assertThat(payment.getReferenceNumber()).isNull();
        assertThat(cashCaptor.getValue()).isEqualTo(
                new CashRegisterGateway.PaymentEntry(command.amount(),
                        command.paymentMethodId(), payment.getNotes(),
                        payment.getId().value(), command.actorId(),
                        command.actorName()));
        assertThat(allocationCaptor.getValue().getInvoiceId())
                .isEqualTo(command.invoiceId());
        assertThat(allocationCaptor.getValue().getPaymentId())
                .isEqualTo(payment.getId().value());
        assertThat(allocationCaptor.getValue().getAllocatedAmount())
                .isEqualTo(command.amount());
    }
    @Test
    void should_reference_first_saved_payment_but_apply_and_save_original_aggregate() {
        when(payments.getNextSequenceForYear(LocalDate.now().getYear()))
                .thenReturn(1);
        var saved = Payment.create(
                PaymentNumber.of(LocalDate.now().getYear(), 1),
                command.customerId(), command.amount(),
                command.paymentMethodId(), null, LocalDate.now(),
                "Persisted payment", command.actorId(), command.actorName(),
                PaymentStatus.APPLIED);
        when(payments.save(any())).thenReturn(saved);
        service.settle(command);
        var captured = ArgumentCaptor.forClass(Payment.class);
        verify(payments, times(2)).save(captured.capture());
        assertThat(captured.getAllValues().getFirst())
                .isSameAs(captured.getAllValues().getLast()).isNotSameAs(saved);
        assertThat(captured.getValue().getAppliedAmount())
                .isEqualTo(command.amount());
        assertThat(saved.getAppliedAmount()).isEqualByComparingTo("0");
        verify(cashRegister).recordPayment(new CashRegisterGateway.PaymentEntry(
                saved.getAmount(), saved.getPaymentMethodId(), saved.getNotes(),
                saved.getId().value(), command.actorId(), command.actorName()));
        verify(allocations).save(argThat(allocation -> allocation.getPaymentId()
                .equals(saved.getId().value())
                && allocation.getInvoiceId().equals(command.invoiceId())
                && allocation.getAllocatedAmount().equals(command.amount())));
    }
    @Test
    void should_stop_before_allocation_and_second_save_when_cash_recording_fails() {
        when(payments.getNextSequenceForYear(LocalDate.now().getYear()))
                .thenReturn(1);
        when(payments.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new IllegalStateException("Cash failure")).when(cashRegister)
                .recordPayment(any());
        assertThatThrownBy(() -> service.settle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cash failure");
        verify(payments, times(1)).save(any());
        verifyNoInteractions(allocations);
    }
}
