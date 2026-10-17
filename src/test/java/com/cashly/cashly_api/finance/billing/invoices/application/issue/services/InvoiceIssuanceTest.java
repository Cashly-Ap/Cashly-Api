package com.cashly.cashly_api.finance.billing.invoices.application.issue.services;

import com.cashly.cashly_api.finance.billing.invoices.application.issue.validation.InvoiceIssueValidator;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.*;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.services.InvoiceStatusService;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLineAllocation;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InvoiceIssuanceTest {
    private final InvoiceRepository invoices = mock(InvoiceRepository.class);
    private final ActorIdentityGateway actors = mock(ActorIdentityGateway.class);
    private final CustomerCreditGateway credit = mock(CustomerCreditGateway.class);
    private final CustomerBalanceGateway balances = mock(CustomerBalanceGateway.class);
    private final InvoicePaymentSettlementGateway settlements = mock(InvoicePaymentSettlementGateway.class);
    private final InventoryGateway reservations = mock(InventoryGateway.class);
    private final StockDeductionGateway stock = mock(StockDeductionGateway.class);
    private final InvoiceNotificationGateway notifications = mock(InvoiceNotificationGateway.class);
    private final CashRegisterGateway cash = mock(CashRegisterGateway.class);
    private final UUID actorId = UUID.randomUUID();
    private final UUID methodId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    private IssueInvoiceService service;

    @BeforeEach
    void setUp() {
        service = new IssueInvoiceService(invoices, actors, new InvoiceIssueValidator(credit, actors, cash),
                new InvoicePaymentService(balances, settlements), reservations, stock, notifications, cash);
        when(actors.requireActor(actorId)).thenReturn(new ActorIdentityGateway.Actor(actorId, "Operator"));
        when(invoices.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void persists_the_cash_draft_before_balance_and_payment_references() {
        Invoice invoice = invoice(PaymentType.CASH);
        var trace = new java.util.ArrayList<String>();
        when(invoices.save(invoice)).thenAnswer(invocation -> {
            trace.add("save:" + invoice.getStatus());
            return invoice;
        });
        doAnswer(invocation -> { trace.add("balance"); return null; }).when(balances).consumeInvoiceBalance(any());
        doAnswer(invocation -> { trace.add("payment"); return null; }).when(settlements).settleCashInvoice(any());
        service.issue(invoice, new IssueInvoiceCommand(null, true, new BigDecimal("30"), methodId), InvoiceOrigin.QUOTE, actorId);
        assertThat(trace).containsExactly("save:DRAFT", "balance", "payment", "save:PAID");
    }

    @Test
    void quote_deducts_only_its_existing_allocations() {
        Invoice invoice = invoice(PaymentType.CASH);
        service.issue(invoice, cashCommand(), InvoiceOrigin.QUOTE, actorId);
        verify(stock).deduct(List.of(new StockDeductionGateway.Allocation(warehouseId,
                invoice.getLines().getFirst().getProductId(), 2)));
        verifyNoInteractions(reservations);
    }

    @Test
    void confirmed_order_never_deducts_or_confirms_inventory_twice() {
        service.issue(invoice(PaymentType.CASH), cashCommand(), InvoiceOrigin.CONFIRMED_ORDER, actorId);
        verifyNoInteractions(stock, reservations);
    }

    @Test
    void manual_confirmation_consumes_reservations_instead_of_direct_deduction() {
        service.issue(invoice(PaymentType.CASH), cashCommand(), InvoiceOrigin.MANUAL_DRAFT, actorId);
        verify(reservations).confirmInvoice(any());
        verifyNoInteractions(stock);
    }

    @Test
    void manual_confirmation_preserves_the_existing_invoice_cash_entry() {
        service.issue(invoice(PaymentType.CASH), cashCommand(), InvoiceOrigin.MANUAL_DRAFT, actorId);
        verify(cash).recordInvoice(any());
    }

    @Test
    void credit_emission_does_not_consume_balance_or_settle_cash() {
        when(credit.isCreditAvailable(any(), any())).thenReturn(true);
        service.issue(invoice(PaymentType.CREDIT), new IssueInvoiceCommand(null, true, BigDecimal.TEN, methodId),
                InvoiceOrigin.CONFIRMED_ORDER, actorId);
        verifyNoInteractions(balances, settlements, cash);
    }

    @Test
    void rejected_credit_does_not_persist_or_apply_financial_or_inventory_effects() {
        assertThatThrownBy(() -> service.issue(invoice(PaymentType.CREDIT), cashCommand(), InvoiceOrigin.QUOTE, actorId))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(invoices, balances, settlements, stock, reservations);
    }

    @Test
    void already_issued_invoice_is_rejected_before_any_gateway_is_called() {
        Invoice invoice = invoice(PaymentType.CASH);
        invoice.confirm();
        clearInvocations(actors, invoices);
        assertThatThrownBy(() -> service.issue(invoice, cashCommand(), InvoiceOrigin.QUOTE, actorId))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(actors, invoices, credit, cash, balances, settlements, stock, reservations, notifications);
    }

    @Test
    void missing_cash_method_is_rejected_before_any_mutation() {
        assertThatThrownBy(() -> service.issue(invoice(PaymentType.CASH),
                new IssueInvoiceCommand(null, false, null, null), InvoiceOrigin.QUOTE, actorId))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(invoices, cash, balances, settlements, stock, reservations);
    }

    private IssueInvoiceCommand cashCommand() { return new IssueInvoiceCommand(null, false, null, methodId); }
    private Invoice invoice(PaymentType type) {
        return Invoice.create(InvoiceNumber.of(2026, 1), UUID.randomUUID(), "Customer", "J-12345678-1",
                actorId, "Operator", null, null, type, null,
                List.of(InvoiceLine.create(UUID.randomUUID(), "Product", "SKU", new BigDecimal("2"),
                        new BigDecimal("50"), List.of(InvoiceLineAllocation.create(warehouseId, new BigDecimal("2"))))));
    }
}
