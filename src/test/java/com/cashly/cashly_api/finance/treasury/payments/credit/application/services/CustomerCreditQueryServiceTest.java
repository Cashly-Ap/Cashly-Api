package com.cashly.cashly_api.finance.treasury.payments.credit.application.services;

import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CreditNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CustomerCreditContextGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.DebitNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.InvoiceCreditLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model.InvoiceCreditEntry;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model.CustomerCreditContext;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerCreditQueryServiceTest {
    private final CustomerCreditContextGateway customers = mock(
            CustomerCreditContextGateway.class);
    private final InvoiceCreditLedgerGateway invoices = mock(
            InvoiceCreditLedgerGateway.class);
    private final CreditNoteLedgerGateway credits = mock(
            CreditNoteLedgerGateway.class);
    private final DebitNoteLedgerGateway debits = mock(
            DebitNoteLedgerGateway.class);
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final PaymentAllocationRepository allocations = mock(
            PaymentAllocationRepository.class);
    private final CustomerCreditQueryService service = new CustomerCreditQueryService(
            customers, payments, allocations, invoices, credits, debits);
    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        when(customers.findById(customerId)).thenReturn(Optional
                .of(new CustomerCreditContext(customerId, "Cashly Customer",
                        "J-12345678-9", amount("1000"), amount("100"))));
    }

    @Test
    void should_preserve_identity_credit_limit_and_customer_balance_without_debt() {
        var result = service.getCreditStatus(customerId);
        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.customerName()).isEqualTo("Cashly Customer");
        assertThat(result.customerRif()).isEqualTo("J-12345678-9");
        assertThat(result.creditLimit()).isEqualByComparingTo("1000");
        assertThat(result.customerBalance()).isEqualByComparingTo("100");
        assertThat(result.pendingBalance()).isEqualByComparingTo("0");
        assertThat(result.availableCredit()).isEqualByComparingTo("1100");
        assertThat(result.hasOverdue()).isFalse();
        assertThat(result.canCreateCreditInvoice()).isTrue();
    }

    @Test
    void should_adjust_pending_invoices_by_returns_notes_and_applied_allocations() {
        var pending = invoice(true, "400", "50", LocalDate.now().plusDays(1));
        var partial = invoice(true, "200", "0", LocalDate.now().plusDays(2));
        when(invoices.findByCustomerId(customerId))
                .thenReturn(List.of(pending, partial));
        when(credits.amountsByInvoiceId(pending.id()))
                .thenReturn(List.of(amount("20"), amount("10")));
        when(debits.amountsByInvoiceId(pending.id()))
                .thenReturn(List.of(amount("5"), amount("15")));
        allocation(pending, "100", PaymentStatus.APPLIED);
        allocation(partial, "30", PaymentStatus.PENDING);
        var result = service.getCreditStatus(customerId);
        assertThat(result.pendingBalance()).isEqualByComparingTo("410");
        assertThat(result.availableCredit()).isEqualByComparingTo("690");
        assertThat(result.canCreateCreditInvoice()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = PaymentStatus.class, names = {"APPLIED", "PENDING",
            "REJECTED", "REFUND"})
    void should_preserve_allocations_for_every_non_cancelled_payment(
            PaymentStatus status) {
        var invoice = invoice(true, "100", "0", LocalDate.now());
        when(invoices.findByCustomerId(customerId))
                .thenReturn(List.of(invoice));
        allocation(invoice, "25", status);
        assertThat(service.getCreditStatus(customerId).pendingBalance())
                .isEqualByComparingTo("75");
    }

    @Test
    void should_ignore_cancelled_and_missing_payments() {
        var invoice = invoice(true, "100", "0", LocalDate.now());
        when(invoices.findByCustomerId(customerId))
                .thenReturn(List.of(invoice));
        UUID cancelledId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        var payment = mock(Payment.class);
        when(payment.getStatus()).thenReturn(PaymentStatus.CANCELLED);
        when(payments.findById(PaymentId.of(cancelledId)))
                .thenReturn(Optional.of(payment));
        when(payments.findById(PaymentId.of(missingId)))
                .thenReturn(Optional.empty());
        when(allocations.findByInvoiceId(invoice.id())).thenReturn(List.of(
                PaymentAllocation.create(cancelledId, invoice.id(),
                        amount("10")),
                PaymentAllocation.create(missingId, invoice.id(),
                        amount("20"))));
        assertThat(service.getCreditStatus(customerId).pendingBalance())
                .isEqualByComparingTo("100");
    }

    @Test
    void should_exclude_non_pending_invoices_from_debt_and_overdue() {
        var ignored = invoice(false, "500", "0", LocalDate.now().minusDays(5));
        when(invoices.findByCustomerId(customerId))
                .thenReturn(List.of(ignored));
        var result = service.getCreditStatus(customerId);
        assertThat(result.pendingBalance()).isEqualByComparingTo("0");
        assertThat(result.overdueInvoices()).isEmpty();
        verifyNoInteractions(credits, debits, allocations, payments);
    }

    @Test
    void should_block_credit_for_overdue_and_report_effective_total_without_subtracting_payments() {
        var overdue = invoice(true, "100", "10", LocalDate.now().minusDays(3));
        var dueToday = invoice(true, "20", "0", LocalDate.now());
        when(invoices.findByCustomerId(customerId))
                .thenReturn(List.of(overdue, dueToday));
        when(credits.amountsByInvoiceId(overdue.id()))
                .thenReturn(List.of(amount("15")));
        when(debits.amountsByInvoiceId(overdue.id()))
                .thenReturn(List.of(amount("5")));
        allocation(overdue, "30", PaymentStatus.APPLIED);
        var result = service.getCreditStatus(customerId);
        assertThat(result.pendingBalance()).isEqualByComparingTo("70");
        assertThat(result.hasOverdue()).isTrue();
        assertThat(result.canCreateCreditInvoice()).isFalse();
        assertThat(result.overdueInvoices()).hasSize(1);
        assertThat(result.overdueInvoices().getFirst().invoiceNumber())
                .isEqualTo(overdue.number());
        assertThat(result.overdueInvoices().getFirst().amount())
                .isEqualByComparingTo("80");
        assertThat(result.overdueInvoices().getFirst().daysOverdue())
                .isEqualTo(3);
    }

    @Test
    void should_find_overdue_for_all_customers_in_query_order() {
        var first = invoice(true, "10", "0", LocalDate.now().minusDays(2));
        var second = invoice(true, "20", "0", LocalDate.now().minusDays(1));
        when(invoices.findAll()).thenReturn(List.of(first,
                invoice(false, "30", "0", LocalDate.now().minusDays(9)),
                second));
        assertThat(service.findAllOverdueInvoices())
                .extracting(entry -> entry.invoiceNumber())
                .containsExactly(first.number(), second.number());
        verify(invoices).findAll();
        verify(invoices, never()).findByCustomerId(any());
    }

    @Test
    void should_preserve_null_subtotal_total_as_zero() {
        var invoice = new InvoiceCreditEntry(UUID.randomUUID(), "INV-NULL",
                true, null, BigDecimal.ZERO, LocalDate.now());
        when(invoices.findByCustomerId(customerId))
                .thenReturn(List.of(invoice));
        assertThat(service.getCreditStatus(customerId).pendingBalance())
                .isEqualByComparingTo("0");
    }

    @Test
    void should_preserve_customer_not_found_error() {
        when(customers.findById(customerId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getCreditStatus(customerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer not found: " + customerId);
        verifyNoInteractions(invoices, allocations, payments, credits, debits);
    }

    private InvoiceCreditEntry invoice(boolean pending, String subtotal,
            String returned, LocalDate due) {
        UUID id = UUID.randomUUID();
        return new InvoiceCreditEntry(id, "INV-" + id, pending,
                amount(subtotal), amount(returned), due);
    }
    private void allocation(InvoiceCreditEntry invoice, String amount,
            PaymentStatus status) {
        UUID id = UUID.randomUUID();
        var payment = mock(Payment.class);
        when(payment.getStatus()).thenReturn(status);
        when(payments.findById(PaymentId.of(id)))
                .thenReturn(Optional.of(payment));
        when(allocations.findByInvoiceId(invoice.id())).thenReturn(List.of(
                PaymentAllocation.create(id, invoice.id(), amount(amount))));
    }
    private BigDecimal amount(String value) {
        return new BigDecimal(value);
    }
}
