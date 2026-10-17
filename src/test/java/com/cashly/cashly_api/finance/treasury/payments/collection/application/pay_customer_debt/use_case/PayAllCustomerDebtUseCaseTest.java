package com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_customer_debt.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentCustomerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentInvoiceGateway;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_customer_debt.request.PayCustomerDebtRequest;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentResultResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
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
    private PaymentInvoiceGateway invoiceGateway;

    @Mock
    private PaymentCustomerGateway customerGateway;

    @Mock
    private CashRegisterGateway cashRegisterGateway;

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
                invoiceGateway, customerGateway, cashRegisterGateway,
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

    private List<PaymentInvoiceGateway.InvoiceSnapshot> snapshots(Invoice... invoices) {
        return java.util.Arrays.stream(invoices).map(invoice -> new PaymentInvoiceGateway.InvoiceSnapshot(
                invoice.getId().value(), invoice.getCustomerId(), invoice.getInvoiceNumber().value(),
                invoice.calculateTotal(BigDecimal.ZERO, BigDecimal.ZERO))).toList();
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

        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots(invoice1, invoice2));
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
        verify(cashRegisterGateway).recordMovement(new CashRegisterGateway.Movement(
                CashRegisterGateway.Direction.IN, CashRegisterGateway.Type.PAYMENT,
                new CashRegisterGateway.PaymentEntry(new BigDecimal("800.00"), paymentMethodId,
                        "Pago total de deuda del cliente", saved.getId().value(), authenticatedUserId,
                        authenticatedUserName)));
    }

    @Test
    void should_Throw_When_NoUnpaidInvoices() {
        PayCustomerDebtRequest request = new PayCustomerDebtRequest(
                paymentMethodId, "REF-001", LocalDate.now(),
                false, null
        );

        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots());

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

        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots(invoice));

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

        when(invoiceGateway.findUnpaidByCustomerId(customerId)).thenReturn(snapshots(invoice1));
        when(paymentRepository.getNextSequenceForYear(LocalDate.now().getYear())).thenReturn(1);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(customerId, request, authenticatedUserId, authenticatedUserName);

        verify(invoiceGateway).recalculateStatus(invoice1.getId().value());
    }
}
