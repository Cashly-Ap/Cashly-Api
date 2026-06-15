package com.parsun.parsun_api.payments.domain.services;

import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentId;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("PaymentAllocationService Unit Tests")
class PaymentAllocationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentAllocationRepository allocationRepository;

    private PaymentAllocationService service;
    private UUID paymentId;
    private Payment payment;

    @BeforeEach
    void setUp() {
        service = new PaymentAllocationService(paymentRepository, allocationRepository);
        paymentId = UUID.randomUUID();
        payment = mock(Payment.class);
    }

    @Nested
    @DisplayName("applyPaymentToInvoices")
    class ApplyPaymentToInvoices {

        @Test
        void should_ApplyAllocations_When_ValidPayment() {
            UUID invoiceId = UUID.randomUUID();
            when(payment.getStatus()).thenReturn(PaymentStatus.APPLIED);
            when(payment.getAmount()).thenReturn(new BigDecimal("500.00"));
            when(payment.getAppliedAmount()).thenReturn(BigDecimal.ZERO);
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.of(payment));

            List<PaymentAllocationService.AllocationRequest> allocations = List.of(
                    new PaymentAllocationService.AllocationRequest(invoiceId, new BigDecimal("300.00")),
                    new PaymentAllocationService.AllocationRequest(UUID.randomUUID(), new BigDecimal("200.00"))
            );

            service.applyPaymentToInvoices(paymentId, allocations);

            verify(allocationRepository, times(2)).save(any(PaymentAllocation.class));
            verify(payment).apply(new BigDecimal("500.00"));
            verify(paymentRepository).save(payment);
        }

        @Test
        void should_ThrowException_When_PaymentNotFound() {
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.applyPaymentToInvoices(paymentId, List.of()));
        }

        @Test
        void should_ThrowException_When_PaymentIsCancelled() {
            when(payment.getStatus()).thenReturn(PaymentStatus.CANCELLED);
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.of(payment));

            assertThrows(IllegalStateException.class,
                    () -> service.applyPaymentToInvoices(paymentId, List.of()));
        }

        @Test
        void should_ThrowException_When_AllocationExceedsRemaining() {
            UUID invoiceId = UUID.randomUUID();
            when(payment.getAmount()).thenReturn(new BigDecimal("500.00"));
            when(payment.getAppliedAmount()).thenReturn(new BigDecimal("400.00"));
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.of(payment));

            List<PaymentAllocationService.AllocationRequest> allocations = List.of(
                    new PaymentAllocationService.AllocationRequest(invoiceId, new BigDecimal("200.00"))
            );

            assertThrows(IllegalArgumentException.class,
                    () -> service.applyPaymentToInvoices(paymentId, allocations));
        }
    }

    @Nested
    @DisplayName("cancelPayment")
    class CancelPayment {

        @Test
        void should_CancelPayment_When_ItExists() {
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.of(payment));

            service.cancelPayment(paymentId);

            verify(allocationRepository).deleteByPaymentId(paymentId);
            verify(payment).cancel();
            verify(paymentRepository).save(payment);
        }

        @Test
        void should_ThrowException_When_PaymentNotFound() {
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.cancelPayment(paymentId));
        }
    }

    @Nested
    @DisplayName("applyAdvanceToInvoice")
    class ApplyAdvanceToInvoice {

        @Test
        void should_ApplyAdvance_When_Valid() {
            UUID invoiceId = UUID.randomUUID();
            when(payment.getStatus()).thenReturn(PaymentStatus.APPLIED);
            when(payment.getAmount()).thenReturn(new BigDecimal("1000.00"));
            when(payment.getAppliedAmount()).thenReturn(new BigDecimal("200.00"));
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.of(payment));

            service.applyAdvanceToInvoice(paymentId, invoiceId, new BigDecimal("300.00"));

            verify(allocationRepository).save(any(PaymentAllocation.class));
            verify(payment).apply(new BigDecimal("300.00"));
            verify(paymentRepository).save(payment);
        }

        @Test
        void should_ThrowException_When_AdvanceNotFound() {
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.applyAdvanceToInvoice(paymentId, UUID.randomUUID(), BigDecimal.TEN));
        }

        @Test
        void should_ThrowException_When_AdvanceIsCancelled() {
            when(payment.getStatus()).thenReturn(PaymentStatus.CANCELLED);
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.of(payment));

            assertThrows(IllegalStateException.class,
                    () -> service.applyAdvanceToInvoice(paymentId, UUID.randomUUID(), BigDecimal.TEN));
        }

        @Test
        void should_ThrowException_When_AmountExceedsRemaining() {
            UUID invoiceId = UUID.randomUUID();
            when(payment.getAmount()).thenReturn(new BigDecimal("500.00"));
            when(payment.getAppliedAmount()).thenReturn(new BigDecimal("400.00"));
            when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.of(payment));

            assertThrows(IllegalArgumentException.class,
                    () -> service.applyAdvanceToInvoice(paymentId, invoiceId, new BigDecimal("200.00")));
        }
    }
}
