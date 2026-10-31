package com.cashly.cashly_api.finance.treasury.payments.collection.domain.services;

import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
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
        service = new PaymentAllocationService(paymentRepository,
                allocationRepository);
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
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.of(payment));

            List<PaymentAllocationService.AllocationRequest> allocations = List
                    .of(new PaymentAllocationService.AllocationRequest(
                            invoiceId, new BigDecimal("300.00")),
                            new PaymentAllocationService.AllocationRequest(
                                    UUID.randomUUID(),
                                    new BigDecimal("200.00")));

            service.applyPaymentToInvoices(paymentId, allocations);

            verify(allocationRepository, times(2))
                    .save(any(PaymentAllocation.class));
            verify(payment).apply(new BigDecimal("500.00"));
            verify(paymentRepository).save(payment);
        }

        @Test
        void should_ThrowException_When_PaymentNotFound() {
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.applyPaymentToInvoices(paymentId, List.of()));
        }

        @Test
        void should_ThrowException_When_PaymentIsCancelled() {
            when(payment.getStatus()).thenReturn(PaymentStatus.CANCELLED);
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.of(payment));

            assertThrows(IllegalStateException.class,
                    () -> service.applyPaymentToInvoices(paymentId, List.of()));
        }

        @Test
        void should_ThrowException_When_AllocationExceedsRemaining() {
            UUID invoiceId = UUID.randomUUID();
            when(payment.getAmount()).thenReturn(new BigDecimal("500.00"));
            when(payment.getAppliedAmount())
                    .thenReturn(new BigDecimal("400.00"));
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.of(payment));

            List<PaymentAllocationService.AllocationRequest> allocations = List
                    .of(new PaymentAllocationService.AllocationRequest(
                            invoiceId, new BigDecimal("200.00")));

            assertThrows(IllegalArgumentException.class, () -> service
                    .applyPaymentToInvoices(paymentId, allocations));
        }
    }

    @Nested
    @DisplayName("cancelPayment")
    class CancelPayment {

        @Test
        void should_CancelPayment_When_ItExists() {
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.of(payment));

            service.cancelPayment(paymentId);

            verify(allocationRepository).deleteByPaymentId(paymentId);
            verify(payment).cancel();
            verify(paymentRepository).save(payment);
        }

        @Test
        void should_ThrowException_When_PaymentNotFound() {
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.empty());

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
            when(payment.getAppliedAmount())
                    .thenReturn(new BigDecimal("200.00"));
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.of(payment));

            service.applyAdvanceToInvoice(paymentId, invoiceId,
                    new BigDecimal("300.00"));

            verify(allocationRepository).save(any(PaymentAllocation.class));
            verify(payment).apply(new BigDecimal("300.00"));
            verify(paymentRepository).save(payment);
        }

        @Test
        void should_ThrowException_When_AdvanceNotFound() {
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.applyAdvanceToInvoice(paymentId,
                            UUID.randomUUID(), BigDecimal.TEN));
        }

        @Test
        void should_ThrowException_When_AdvanceIsCancelled() {
            when(payment.getStatus()).thenReturn(PaymentStatus.CANCELLED);
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.of(payment));

            assertThrows(IllegalStateException.class,
                    () -> service.applyAdvanceToInvoice(paymentId,
                            UUID.randomUUID(), BigDecimal.TEN));
        }

        @Test
        void should_ThrowException_When_AmountExceedsRemaining() {
            UUID invoiceId = UUID.randomUUID();
            when(payment.getAmount()).thenReturn(new BigDecimal("500.00"));
            when(payment.getAppliedAmount())
                    .thenReturn(new BigDecimal("400.00"));
            when(paymentRepository.findById(PaymentId.of(paymentId)))
                    .thenReturn(Optional.of(payment));

            assertThrows(IllegalArgumentException.class,
                    () -> service.applyAdvanceToInvoice(paymentId, invoiceId,
                            new BigDecimal("200.00")));
        }
    }

    @Nested
    @DisplayName("allocateOldestFirst")
    class AllocateOldestFirst {

        @Test
        void should_AllocateExactAmount_When_PaymentExactlyCoversTotalDebt() {
            UUID invoice1Id = UUID.randomUUID();
            UUID invoice2Id = UUID.randomUUID();
            List<com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance> invoices = List
                    .of(new com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance(
                            invoice1Id, new BigDecimal("300.00")),
                            new com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance(
                                    invoice2Id, new BigDecimal("200.00")));

            List<PaymentAllocation> allocations = service.allocateOldestFirst(
                    paymentId, invoices, new BigDecimal("500.00"));

            assertEquals(2, allocations.size());
            assertEquals(invoice1Id, allocations.get(0).getInvoiceId());
            assertEquals(new BigDecimal("300.00"),
                    allocations.get(0).getAllocatedAmount());
            assertEquals(invoice2Id, allocations.get(1).getInvoiceId());
            assertEquals(new BigDecimal("200.00"),
                    allocations.get(1).getAllocatedAmount());
        }

        @Test
        void should_AllocateOnlyToOldestInvoice_When_AmountIsSmallerThanTotalDebt() {
            UUID invoice1Id = UUID.randomUUID();
            UUID invoice2Id = UUID.randomUUID();
            List<com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance> invoices = List
                    .of(new com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance(
                            invoice1Id, new BigDecimal("300.00")),
                            new com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance(
                                    invoice2Id, new BigDecimal("200.00")));

            List<PaymentAllocation> allocations = service.allocateOldestFirst(
                    paymentId, invoices, new BigDecimal("250.00"));

            assertEquals(1, allocations.size());
            assertEquals(invoice1Id, allocations.get(0).getInvoiceId());
            assertEquals(new BigDecimal("250.00"),
                    allocations.get(0).getAllocatedAmount());
        }

        @Test
        void should_AllocateOnlyUpToTotalDebt_When_AmountExceedsTotalDebt() {
            UUID invoice1Id = UUID.randomUUID();
            UUID invoice2Id = UUID.randomUUID();
            List<com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance> invoices = List
                    .of(new com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance(
                            invoice1Id, new BigDecimal("300.00")),
                            new com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance(
                                    invoice2Id, new BigDecimal("200.00")));

            List<PaymentAllocation> allocations = service.allocateOldestFirst(
                    paymentId, invoices, new BigDecimal("700.00"));

            BigDecimal totalAllocated = allocations.stream()
                    .map(PaymentAllocation::getAllocatedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertEquals(2, allocations.size());
            assertEquals(new BigDecimal("500.00"), totalAllocated);
        }

        @Test
        void should_ReturnEmptyList_When_NoInvoicesProvided() {
            List<PaymentAllocation> allocations = service.allocateOldestFirst(
                    paymentId, List.of(), new BigDecimal("100.00"));

            assertTrue(allocations.isEmpty());
        }

        @Test
        void should_AllocateFullAmount_When_SingleInvoiceProvided() {
            UUID invoiceId = UUID.randomUUID();
            List<com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance> invoices = List
                    .of(new com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance(
                            invoiceId, new BigDecimal("500.00")));

            List<PaymentAllocation> allocations = service.allocateOldestFirst(
                    paymentId, invoices, new BigDecimal("500.00"));

            assertEquals(1, allocations.size());
            assertEquals(new BigDecimal("500.00"),
                    allocations.get(0).getAllocatedAmount());
        }

        @Test
        void should_SkipInvoice_When_RemainingAmountIsZero() {
            UUID invoice1Id = UUID.randomUUID();
            UUID invoice2Id = UUID.randomUUID();
            List<com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance> invoices = List
                    .of(new com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance(
                            invoice1Id, BigDecimal.ZERO),
                            new com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.InvoiceRemainingBalance(
                                    invoice2Id, new BigDecimal("200.00")));

            List<PaymentAllocation> allocations = service.allocateOldestFirst(
                    paymentId, invoices, new BigDecimal("200.00"));

            assertEquals(1, allocations.size());
            assertEquals(invoice2Id, allocations.get(0).getInvoiceId());
        }
    }
}
