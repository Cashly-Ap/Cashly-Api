package com.parsun.parsun_api.payments.application.usecases;

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
import com.parsun.parsun_api.payments.application.dto.request.PayPartialDebtRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
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
@DisplayName("PayPartialDebtUseCase Unit Tests")
class PayPartialDebtUseCaseTest {

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

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<PaymentAllocation> allocationCaptor;

    private PayPartialDebtUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();
    private final UUID authenticatedUserId = UUID.randomUUID();
    private final String authenticatedUserName = "Test User";

    @BeforeEach
    void setUp() {
        useCase = new PayPartialDebtUseCase(
                paymentRepository, allocationRepository,
                invoiceRepository, invoiceStatusService,
                creditNoteRepository, debitNoteRepository,
                consumeCustomerBalanceUseCase
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
    void should_PayPartialDebt_When_ValidRequest() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = unpaidInvoice(invoiceId, new BigDecimal("5"), new BigDecimal("200.00"));

        PayPartialDebtRequest request = new PayPartialDebtRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                new BigDecimal("500.00"), false, null
        );

        when(invoiceRepository.findAllWithFilters(
                null, null, customerId, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null, null))
                .thenReturn(List.of(invoice));
        when(creditNoteRepository.findByInvoiceId(invoiceId)).thenReturn(List.of());
        when(debitNoteRepository.findByInvoiceId(invoiceId)).thenReturn(List.of());
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResultResponse response = useCase.execute(customerId, request, authenticatedUserId, authenticatedUserName);

        assertNotNull(response);
        assertNotNull(response.message());
        assertEquals(PaymentStatus.APPLIED.name(), response.status());
        verify(paymentRepository, org.mockito.Mockito.times(2)).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getAllValues().get(1);
        assertEquals(new BigDecimal("500.00"), saved.getAmount());
    }

    @Test
    void should_Throw_When_NoUnpaidInvoices() {
        PayPartialDebtRequest request = new PayPartialDebtRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                new BigDecimal("500.00"), false, null
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
    void should_Throw_When_PaymentAmountIsZero() {
        PayPartialDebtRequest request = new PayPartialDebtRequest(
                null, null, LocalDate.now(),
                BigDecimal.ZERO, false, null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(customerId, request, authenticatedUserId, authenticatedUserName)
        );

        assertTrue(exception.getMessage().contains("Payment amount must be greater than zero"));
    }

    @Test
    void should_Throw_When_PaymentMethodMissing() {
        PayPartialDebtRequest request = new PayPartialDebtRequest(
                null, null, LocalDate.now(),
                new BigDecimal("500.00"), false, null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(customerId, request, authenticatedUserId, authenticatedUserName)
        );

        assertTrue(exception.getMessage().contains("Payment method is required"));
    }

    @Test
    void should_AllocateRemainingToInvoices_When_PartialPayment() {
        UUID invoice1Id = UUID.randomUUID();
        UUID invoice2Id = UUID.randomUUID();
        Invoice invoice1 = unpaidInvoice(invoice1Id, new BigDecimal("1"), new BigDecimal("300.00"));
        Invoice invoice2 = unpaidInvoice(invoice2Id, new BigDecimal("1"), new BigDecimal("300.00"));

        PayPartialDebtRequest request = new PayPartialDebtRequest(
                paymentMethodId, null, LocalDate.now(),
                new BigDecimal("400.00"), false, null
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

        useCase.execute(customerId, request, authenticatedUserId, authenticatedUserName);

        verify(paymentRepository, org.mockito.Mockito.times(2)).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getAllValues().get(1);
        assertEquals(new BigDecimal("400.00"), saved.getAmount());
    }
}
