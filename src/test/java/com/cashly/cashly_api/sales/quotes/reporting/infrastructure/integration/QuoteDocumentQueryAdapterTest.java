package com.cashly.cashly_api.sales.quotes.reporting.infrastructure.integration;
import com.cashly.cashly_api.sales.quotes.application.queries.services.QuoteDocumentQueryService;
import com.cashly.cashly_api.sales.quotes.application.list.use_case.GetQuotesUseCase;
import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import com.cashly.cashly_api.sales.quotes.reporting.application.dto.model.QuoteReportFilter;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class QuoteDocumentQueryAdapterTest {
    private final GetQuotesUseCase list = mock(GetQuotesUseCase.class);
    private final QuoteDocumentQueryAdapter adapter = new QuoteDocumentQueryAdapter(new QuoteDocumentQueryService(mock(QuoteRepository.class), list));
    @Test
    void listPreservesAllFiltersOrderingAndAmounts() {
        var filter = new QuoteReportFilter(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), UUID.randomUUID(), "DRAFT", UUID.randomUUID(), UUID.randomUUID());
        var source = mock(QuoteListResponse.class);
        var id = UUID.randomUUID();
        var date = Instant.parse("2026-01-02T10:30:00Z");
        when(source.id()).thenReturn(id);
        when(source.quoteNumber()).thenReturn("DOC-123");
        when(source.customerName()).thenReturn("Customer");
        when(source.status()).thenReturn("DRAFT");
        when(source.subtotal()).thenReturn(new BigDecimal("31.25"));
        when(source.createdAt()).thenReturn(date);
        
        when(list.execute(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.status(), filter.productId(), filter.categoryId(), "createdAt", "desc")).thenReturn(List.of(source));
        var result = adapter.findAll(filter);
        var row = result.getFirst();
        assertEquals(id, row.id());
        assertEquals("DOC-123", row.quoteNumber());
        assertEquals("Customer", row.customerName());
        assertEquals("DRAFT", row.status());
        assertEquals(new BigDecimal("31.25"), row.subtotal());
        assertEquals(date, row.createdAt());
        
        assertThrows(UnsupportedOperationException.class, result::clear);
        verify(list).execute(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.status(), filter.productId(), filter.categoryId(), "createdAt", "desc");
    }
    @Test
    void emptyUnfilteredListPreservesNullFilterSemantics() {
        when(list.execute(null, null, null, null, null, null, "createdAt", "desc")).thenReturn(List.of());
        assertTrue(adapter.findAll(new QuoteReportFilter(null, null, null, null, null, null)).isEmpty());
        verify(list).execute(null, null, null, null, null, null, "createdAt", "desc");
    }
}