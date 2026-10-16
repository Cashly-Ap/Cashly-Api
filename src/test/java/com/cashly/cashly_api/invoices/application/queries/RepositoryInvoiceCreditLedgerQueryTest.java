package com.cashly.cashly_api.invoices.application.queries;
import com.cashly.cashly_api.invoices.domain.Invoice;
import com.cashly.cashly_api.invoices.domain.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class RepositoryInvoiceCreditLedgerQueryTest {
    private final InvoiceRepository invoices = mock(InvoiceRepository.class);
    private final RepositoryInvoiceCreditLedgerQuery query = new RepositoryInvoiceCreditLedgerQuery(invoices);
    @ParameterizedTest
    @EnumSource(InvoiceStatus.class)
    void should_project_only_pending_or_partial_as_credit_debt(InvoiceStatus status) {
        var invoice = mock(Invoice.class, RETURNS_DEEP_STUBS);
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(invoice.getId().value()).thenReturn(id);
        when(invoice.getInvoiceNumber().value()).thenReturn("INV-01");
        when(invoice.getStatus()).thenReturn(status);
        when(invoice.getSubtotal()).thenReturn(new BigDecimal("200"));
        when(invoice.getReturnedTotal()).thenReturn(new BigDecimal("25"));
        when(invoice.getDueDate()).thenReturn(LocalDate.of(2026, 10, 1));
        when(invoices.findAllWithFilters(null, null, customerId, null, null, null, null, "createdAt", "desc")).thenReturn(List.of(invoice));
        var result = query.findByCustomerId(customerId).getFirst();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.number()).isEqualTo("INV-01");
        assertThat(result.pendingPayment()).isEqualTo(status == InvoiceStatus.PENDING_PAYMENT || status == InvoiceStatus.PARTIALLY_PAID);
        assertThat(result.subtotal()).isEqualByComparingTo("200");
        assertThat(result.returnedTotal()).isEqualByComparingTo("25");
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 10, 1));
    }
    @Test
    void should_preserve_global_query_filters_and_sort() {
        assertThat(query.findAll()).isEmpty();
        verify(invoices).findAllWithFilters(null, null, null, null, null, null, null, "createdAt", "desc");
    }
}
