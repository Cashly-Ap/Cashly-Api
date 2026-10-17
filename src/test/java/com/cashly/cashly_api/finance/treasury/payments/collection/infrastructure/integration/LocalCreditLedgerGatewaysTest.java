package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration.LocalCreditNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration.LocalCustomerCreditContextGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration.LocalDebitNoteLedgerGateway;
import com.cashly.cashly_api.finance.treasury.payments.credit.infrastructure.integration.LocalInvoiceCreditLedgerGateway;
import com.cashly.cashly_api.partners.customers.credit.application.ports.in.CustomerCreditContextQuery;
import com.cashly.cashly_api.partners.customers.credit.application.ports.in.CustomerCreditContextEntry;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceCreditLedgerQuery;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.in.InvoiceCreditLedgerEntry;
import com.cashly.cashly_api.finance.billing.credit_notes.application.ports.in.CreditNoteLedgerQuery;
import com.cashly.cashly_api.finance.billing.debit_notes.application.ports.in.DebitNoteLedgerQuery;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class LocalCreditLedgerGatewaysTest {
    @Test
    void should_map_customer_snapshot_and_preserve_missing_customer() {
        var query = mock(CustomerCreditContextQuery.class);
        UUID id = UUID.randomUUID();
        var gateway = new LocalCustomerCreditContextGateway(query);
        assertThat(gateway.findById(id)).isEmpty();
        when(query.findById(id)).thenReturn(Optional.of(new CustomerCreditContextEntry(id, "Customer", "RIF", new BigDecimal("200"), new BigDecimal("50"))));
        var result = gateway.findById(id).orElseThrow();
        assertThat(result.customerId()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Customer");
        assertThat(result.rif()).isEqualTo("RIF");
        assertThat(result.creditLimit()).isEqualByComparingTo("200");
        assertThat(result.balance()).isEqualByComparingTo("50");
    }
    @Test
    void should_map_customer_and_global_invoice_queries_without_foreign_models() {
        var query = mock(InvoiceCreditLedgerQuery.class);
        UUID customerId = UUID.randomUUID();
        var entry = new InvoiceCreditLedgerEntry(UUID.randomUUID(), "INV-01", true, new BigDecimal("100"), new BigDecimal("20"), LocalDate.now());
        when(query.findByCustomerId(customerId)).thenReturn(List.of(entry));
        when(query.findAll()).thenReturn(List.of(entry));
        var gateway = new LocalInvoiceCreditLedgerGateway(query);
        var result = gateway.findByCustomerId(customerId).getFirst();
        assertThat(result.id()).isEqualTo(entry.id());
        assertThat(result.number()).isEqualTo(entry.number());
        assertThat(result.pendingPayment()).isTrue();
        assertThat(result.subtotal()).isEqualTo(entry.subtotal());
        assertThat(result.returnedTotal()).isEqualTo(entry.returnedTotal());
        assertThat(result.dueDate()).isEqualTo(entry.dueDate());
        assertThat(gateway.findAll()).containsExactly(result);
    }
    @Test
    void should_delegate_note_amount_queries_to_public_provider_contracts() {
        UUID invoiceId = UUID.randomUUID();
        var credits = mock(CreditNoteLedgerQuery.class);
        var debits = mock(DebitNoteLedgerQuery.class);
        when(credits.amountsByInvoiceId(invoiceId)).thenReturn(List.of(new BigDecimal("40")));
        when(debits.amountsByInvoiceId(invoiceId)).thenReturn(List.of(new BigDecimal("10")));
        assertThat(new LocalCreditNoteLedgerGateway(credits).amountsByInvoiceId(invoiceId)).containsExactly(new BigDecimal("40"));
        assertThat(new LocalDebitNoteLedgerGateway(debits).amountsByInvoiceId(invoiceId)).containsExactly(new BigDecimal("10"));
    }
}
