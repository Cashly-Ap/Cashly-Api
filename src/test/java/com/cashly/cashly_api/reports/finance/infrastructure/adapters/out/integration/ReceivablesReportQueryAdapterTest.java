package com.cashly.cashly_api.reports.finance.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.receivables.application.ports.out.model.AccountsReceivableRow;
import com.cashly.cashly_api.receivables.application.ports.out.ReceivablesQueryGateway;
import com.cashly.cashly_api.receivables.application.services.AccountsReceivableReportQueryService;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReceivablesReportQueryAdapterTest {
    private final ReceivablesQueryGateway query = mock(ReceivablesQueryGateway.class);
    private final ReceivablesReportQueryAdapter adapter = new ReceivablesReportQueryAdapter(new AccountsReceivableReportQueryService(query));
    private BigDecimal amount(int value) { return BigDecimal.valueOf(value); }
    @Test void filtersAndEveryCreditAgingAmountArePreserved() {
        var id = UUID.randomUUID();
        var row = new AccountsReceivableRow(id, "Customer", "RIF-123", amount(100), amount(120), amount(-20), amount(30), amount(20), amount(10), amount(40), amount(20));
        when(query.findAccountsReceivableForReport(id, true)).thenReturn(List.of(row));
        var rows = adapter.findAccountsReceivableForReport(id, true);
        var result = rows.getFirst();
        assertEquals(id, result.customerId());
        assertEquals("Customer", result.customerName());
        assertEquals("RIF-123", result.rif());
        assertEquals(amount(100), result.creditLimit());
        assertEquals(amount(120), result.pendingBalance());
        assertEquals(amount(-20), result.available());
        assertEquals(amount(30), result.age0_30());
        assertEquals(amount(20), result.age31_60());
        assertEquals(amount(10), result.age61_90());
        assertEquals(amount(40), result.age91_120());
        assertEquals(amount(20), result.age120Plus());
        assertThrows(UnsupportedOperationException.class, rows::clear);
        verify(query).findAccountsReceivableForReport(id, true);
    }
    @Test void unfilteredEmptyResultPreservesQueryArguments() {
        when(query.findAccountsReceivableForReport(null, false)).thenReturn(List.of());
        assertTrue(adapter.findAccountsReceivableForReport(null, false).isEmpty());
        verify(query).findAccountsReceivableForReport(null, false);
    }
    @Test void queryFailurePreservesOriginalError() {
        var failure = new IllegalStateException("Query failed");
        when(query.findAccountsReceivableForReport(null, true)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> adapter.findAccountsReceivableForReport(null, true)));
    }
}
