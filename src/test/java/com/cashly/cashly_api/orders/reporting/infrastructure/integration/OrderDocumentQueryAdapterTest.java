package com.cashly.cashly_api.orders.reporting.infrastructure.integration;
import com.cashly.cashly_api.orders.application.queries.OrderDocumentQueryService;
import com.cashly.cashly_api.orders.application.list.GetOrdersUseCase;
import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.orders.domain.OrderRepository;
import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportFilter;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OrderDocumentQueryAdapterTest {
    private final GetOrdersUseCase list = mock(GetOrdersUseCase.class);
    private final OrderDocumentQueryAdapter adapter = new OrderDocumentQueryAdapter(new OrderDocumentQueryService(mock(OrderRepository.class), list));
    @Test
    void listPreservesAllFiltersOrderingAndAmounts() {
        var filter = new OrderReportFilter(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), UUID.randomUUID(), "DRAFT", UUID.randomUUID(), UUID.randomUUID());
        var source = mock(OrderListResponse.class);
        var id = UUID.randomUUID();
        var date = LocalDateTime.of(2026, 1, 2, 10, 30);
        when(source.id()).thenReturn(id);
        when(source.orderNumber()).thenReturn("DOC-123");
        when(source.customerName()).thenReturn("Customer");
        when(source.status()).thenReturn("DRAFT");
        when(source.subtotal()).thenReturn(new BigDecimal("31.25"));
        when(source.createdAt()).thenReturn(date);
        when(source.lineCount()).thenReturn(3); when(source.invoiceId()).thenReturn(id);
        when(list.execute(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.status(), filter.productId(), filter.categoryId(), "createdAt", "desc")).thenReturn(List.of(source));
        var result = adapter.findAll(filter);
        var row = result.getFirst();
        assertEquals(id, row.id());
        assertEquals("DOC-123", row.orderNumber());
        assertEquals("Customer", row.customerName());
        assertEquals("DRAFT", row.status());
        assertEquals(new BigDecimal("31.25"), row.subtotal());
        assertEquals(date, row.createdAt());
        assertEquals(3, row.lineCount()); assertEquals(id, row.invoiceId());
        assertThrows(UnsupportedOperationException.class, result::clear);
        verify(list).execute(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.status(), filter.productId(), filter.categoryId(), "createdAt", "desc");
    }
    @Test
    void emptyUnfilteredListPreservesNullFilterSemantics() {
        when(list.execute(null, null, null, null, null, null, "createdAt", "desc")).thenReturn(List.of());
        assertTrue(adapter.findAll(new OrderReportFilter(null, null, null, null, null, null)).isEmpty());
        verify(list).execute(null, null, null, null, null, null, "createdAt", "desc");
    }
}