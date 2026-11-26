package com.cashly.cashly_api.finance.treasury.payments.credit.application.services;

import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CreditNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.CustomerCreditContextGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.DebitNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.InvoiceCreditLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model.InvoiceCreditEntry;
import com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.out.model.CustomerCreditContext;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
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
    private final Map<UUID, BigDecimal> creditTotals = new HashMap<>();
    private final Map<UUID, BigDecimal> debitTotals = new HashMap<>();
    private final Map<UUID, BigDecimal> appliedTotals = new HashMap<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        when(customers.findById(customerId)).thenReturn(Optional
                .of(new CustomerCreditContext(customerId, "Cashly Customer",
                        "J-12345678-9", amount("1000"), amount("100"))));
        when(credits.totalsByInvoiceIds(anyList())).thenAnswer(invocation -> select(
                creditTotals, (List<UUID>) invocation.getArgument(0)));
        when(debits.totalsByInvoiceIds(anyList())).thenAnswer(invocation -> select(
                debitTotals, (List<UUID>) invocation.getArgument(0)));
        when(allocations.sumByInvoiceIds(anyList())).thenAnswer(invocation -> select(
                appliedTotals, (List<UUID>) invocation.getArgument(0)));
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
        credit(pending, "30");
        debit(pending, "20");
        applied(pending, "100");
        applied(partial, "30");
        var result = service.getCreditStatus(customerId);
        assertThat(result.pendingBalance()).isEqualByComparingTo("410");
        assertThat(result.availableCredit()).isEqualByComparingTo("690");
        assertThat(result.canCreateCreditInvoice()).isTrue();
    }

    @Test
    void should_use_batched_applied_payment_totals() {
        var invoice = invoice(true, "100", "0", LocalDate.now());
        when(invoices.findByCustomerId(customerId))
                .thenReturn(List.of(invoice));
        applied(invoice, "25");
        assertThat(service.getCreditStatus(customerId).pendingBalance())
                .isEqualByComparingTo("75");
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
        credit(overdue, "15");
        debit(overdue, "5");
        applied(overdue, "30");
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

    private static Map<UUID, BigDecimal> select(Map<UUID, BigDecimal> totals,
            List<UUID> ids) {
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (UUID id : ids) {
            if (totals.containsKey(id)) {
                result.put(id, totals.get(id));
            }
        }
        return result;
    }

    private InvoiceCreditEntry invoice(boolean pending, String subtotal,
            String returned, LocalDate due) {
        UUID id = UUID.randomUUID();
        return new InvoiceCreditEntry(id, "INV-" + id, pending,
                amount(subtotal), amount(returned), due);
    }

    private void credit(InvoiceCreditEntry invoice, String amount) {
        creditTotals.merge(invoice.id(), amount(amount), BigDecimal::add);
    }

    private void debit(InvoiceCreditEntry invoice, String amount) {
        debitTotals.merge(invoice.id(), amount(amount), BigDecimal::add);
    }

    private void applied(InvoiceCreditEntry invoice, String amount) {
        appliedTotals.merge(invoice.id(), amount(amount), BigDecimal::add);
    }

    private BigDecimal amount(String value) {
        return new BigDecimal(value);
    }
}
