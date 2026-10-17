package com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_invoice.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentCustomerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentInvoiceGateway;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_invoice.request.PayInvoiceRequest;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentResultResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayInvoiceUseCase Unit Tests")
class PayInvoiceUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentAllocationRepository allocationRepository;

    @Mock
    private PaymentInvoiceGateway invoiceGateway;

    @Mock
    private PaymentCustomerGateway customerGateway;

    @Mock
    private CashRegisterGateway cashRegisterGateway;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<PaymentAllocation> allocationCaptor;

    private PayInvoiceUseCase useCase;

    private final UUID invoiceId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();
    private final UUID authenticatedUserId = UUID.randomUUID();
    private final String authenticatedUserName = "Test User";

    @BeforeEach
    void setUp() {
        useCase = new PayInvoiceUseCase(
                paymentRepository, allocationRepository,
                invoiceGateway, customerGateway, cashRegisterGateway
        );
    }

    private Invoice invoiceWithTotal(BigDecimal total) {
        return Invoice.reconstitute(
                InvoiceId.of(invoiceId),
                InvoiceNumber.of(2026, 1),
                customerId,
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Creator",
                null, null,
                InvoiceStatus.PENDING_PAYMENT,
                PaymentType.CASH,
                null,
                BigDecimal.ZERO,
                null,
                null, null,
                List.of(InvoiceLine.create(
                        UUID.randomUUID(), "Product", "P001",
                        total, BigDecimal.ONE, List.of()
                )),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atStartOfDay()
        );
    }

    private PaymentInvoiceGateway.InvoiceSnapshot snapshot(Invoice invoice) {
        return new PaymentInvoiceGateway.InvoiceSnapshot(invoice.getId().value(), invoice.getCustomerId(),
                invoice.getInvoiceNumber().value(), invoice.calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void should_PayInvoice_When_ValidRequest() {
        Invoice invoice = invoiceWithTotal(new BigDecimal("500.00"));
        PayInvoiceRequest request = new PayInvoiceRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                new BigDecimal("500.00"), false, null
        );
        when(invoiceGateway.findById(invoiceId))
                .thenReturn(Optional.of(snapshot(invoice)));
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResultResponse response = useCase.execute(invoiceId, request, authenticatedUserId, authenticatedUserName);

        assertNotNull(response);
        assertEquals(PaymentStatus.APPLIED.name(), response.status());
        assertEquals("Pago registrado exitosamente", response.message());
        verify(paymentRepository, org.mockito.Mockito.atLeast(1)).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertEquals(new BigDecimal("500.00"), saved.getAmount());
        verify(cashRegisterGateway).recordMovement(new CashRegisterGateway.Movement(
                CashRegisterGateway.Direction.IN, CashRegisterGateway.Type.PAYMENT,
                new CashRegisterGateway.PaymentEntry(new BigDecimal("500.00"), paymentMethodId,
                        "Pago de factura " + invoice.getInvoiceNumber().value(), saved.getId().value(),
                        authenticatedUserId, authenticatedUserName)));
    }

    @Test
    void should_PayInvoiceWithBalance_When_UseBalanceIsTrue() {
        Invoice invoice = invoiceWithTotal(new BigDecimal("500.00"));
        BigDecimal balanceAmount = new BigDecimal("200.00");
        PayInvoiceRequest request = new PayInvoiceRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                new BigDecimal("500.00"), true, balanceAmount
        );
        when(invoiceGateway.findById(invoiceId))
                .thenReturn(Optional.of(snapshot(invoice)));
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1, 2);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResultResponse response = useCase.execute(invoiceId, request, authenticatedUserId, authenticatedUserName);

        assertNotNull(response);
        assertNotNull(response.message());
        verify(customerGateway).consumeInvoiceBalance(new PaymentCustomerGateway.BalanceConsumption(
                customerId, balanceAmount, invoiceId, invoice.getInvoiceNumber().value(), authenticatedUserId));
    }

    @Test
    void should_NotRequireCashRegister_When_InvoiceIsPaidOnlyWithCustomerBalance() {
        Invoice invoice = invoiceWithTotal(new BigDecimal("200.00"));
        PayInvoiceRequest request = new PayInvoiceRequest(
                null, null, LocalDate.now(),
                new BigDecimal("200.00"), true, new BigDecimal("200.00")
        );
        when(invoiceGateway.findById(invoiceId)).thenReturn(Optional.of(snapshot(invoice)));
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(invoiceId, request, authenticatedUserId, authenticatedUserName);

        verify(cashRegisterGateway, never()).requireOpenRegister();
        verify(cashRegisterGateway, never()).recordMovement(any());
    }

    @Test
    void should_CalculateAmountFromInvoice_When_AmountIsNull() {
        Invoice invoice = invoiceWithTotal(new BigDecimal("500.00"));
        PayInvoiceRequest request = new PayInvoiceRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                null, false, null
        );
        when(invoiceGateway.findById(invoiceId))
                .thenReturn(Optional.of(snapshot(invoice)));
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResultResponse response = useCase.execute(invoiceId, request, authenticatedUserId, authenticatedUserName);

        assertNotNull(response);
        assertNotNull(response.message());
    }

    @Test
    void should_Throw_When_InvoiceNotFound() {
        PayInvoiceRequest request = new PayInvoiceRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                new BigDecimal("500.00"), false, null
        );
        when(invoiceGateway.findById(invoiceId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(invoiceId, request, authenticatedUserId, authenticatedUserName)
        );

        assertTrue(exception.getMessage().contains("Invoice not found"));
    }

    @Test
    void should_Throw_When_PaymentAmountIsZeroAndNoBalance() {
        Invoice invoice = invoiceWithTotal(new BigDecimal("500.00"));
        PayInvoiceRequest request = new PayInvoiceRequest(
                null, null, LocalDate.now(),
                BigDecimal.ZERO, false, null
        );
        when(invoiceGateway.findById(invoiceId))
                .thenReturn(Optional.of(snapshot(invoice)));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(invoiceId, request, authenticatedUserId, authenticatedUserName)
        );

        assertTrue(exception.getMessage().contains("Payment amount must be greater than zero"));
    }

    @Test
    void should_Throw_When_PaymentMethodIsMissingAndAmountDue() {
        Invoice invoice = invoiceWithTotal(new BigDecimal("500.00"));
        PayInvoiceRequest request = new PayInvoiceRequest(
                null, null, LocalDate.now(),
                new BigDecimal("500.00"), false, null
        );
        when(invoiceGateway.findById(invoiceId))
                .thenReturn(Optional.of(snapshot(invoice)));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(invoiceId, request, authenticatedUserId, authenticatedUserName)
        );

        assertTrue(exception.getMessage().contains("Payment method is required"));
    }

    @Test
    void should_RecalculateInvoiceStatus_After_Payment() {
        Invoice invoice = invoiceWithTotal(new BigDecimal("500.00"));
        PayInvoiceRequest request = new PayInvoiceRequest(
                paymentMethodId, null, LocalDate.now(),
                new BigDecimal("500.00"), false, null
        );
        when(invoiceGateway.findById(invoiceId))
                .thenReturn(Optional.of(snapshot(invoice)));
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(invoiceId, request, authenticatedUserId, authenticatedUserName);

        verify(invoiceGateway).recalculateStatus(invoiceId);
    }

    @Test
    void should_SaveAllocation_When_PaymentIsCash() {
        Invoice invoice = invoiceWithTotal(new BigDecimal("500.00"));
        PayInvoiceRequest request = new PayInvoiceRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                new BigDecimal("500.00"), false, null
        );
        when(invoiceGateway.findById(invoiceId))
                .thenReturn(Optional.of(snapshot(invoice)));
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(invoiceId, request, authenticatedUserId, authenticatedUserName);

        verify(allocationRepository).save(allocationCaptor.capture());
        PaymentAllocation allocation = allocationCaptor.getValue();
        assertEquals(invoiceId, allocation.getInvoiceId());
        assertEquals(new BigDecimal("500.00"), allocation.getAllocatedAmount());
    }
}
