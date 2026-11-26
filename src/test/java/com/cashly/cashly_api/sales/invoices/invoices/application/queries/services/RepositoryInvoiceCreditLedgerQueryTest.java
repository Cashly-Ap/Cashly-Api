package com.cashly.cashly_api.sales.invoices.invoices.application.queries.services;

import com.cashly.cashly_api.sales.invoices.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.sales.invoices.invoices.domain.valueobjects.InvoiceStatus;
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
    private final RepositoryInvoiceCreditLedgerQuery query = new RepositoryInvoiceCreditLedgerQuery(
            invoices);

    @ParameterizedTest
    @EnumSource(InvoiceStatus.class)
    void should_project_only_pending_or_partial_as_credit_debt(
            InvoiceStatus status) {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        var header = new InvoiceRepository.InvoiceHeader(id, customerId,
                "INV-01", status.name(), new BigDecimal("200"),
                new BigDecimal("25"), LocalDate.of(2026, 10, 1));
        when(invoices.findHeaders(customerId, null))
                .thenReturn(List.of(header));
        var result = query.findByCustomerId(customerId).getFirst();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.number()).isEqualTo("INV-01");
        assertThat(result.pendingPayment())
                .isEqualTo(status == InvoiceStatus.PENDING_PAYMENT
                        || status == InvoiceStatus.PARTIALLY_PAID);
        assertThat(result.subtotal()).isEqualByComparingTo("200");
        assertThat(result.returnedTotal()).isEqualByComparingTo("25");
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void should_use_lightweight_header_query_for_global_list() {
        assertThat(query.findAll()).isEmpty();
        verify(invoices).findAllHeaders();
    }
}
